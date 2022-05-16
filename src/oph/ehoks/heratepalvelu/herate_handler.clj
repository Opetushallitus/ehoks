(ns oph.ehoks.heratepalvelu.herate-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [clojure.tools.logging :as log]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.heratepalvelu.heratepalvelu :as hp]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.oppijaindex :as op]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops])
  (:import (java.time LocalDate)))

(def routes
  "Herätepalvelun reitit"
  (c-api/context "/heratepalvelu" []
    :tags ["heratepalvelu"]
    :header-params [ticket :- s/Str
                    caller-id :- s/Str]

    (route-middleware
      [wrap-user-details m/wrap-require-service-user
       wrap-audit-logger m/wrap-require-oph-privileges]

      (c-api/GET "/tyoelamajaksot" []
        :summary "Päättyneet työelämäjaksot"
        :query-params [start :- LocalDate
                       end :- LocalDate
                       limit :- (s/maybe s/Int)]
        (let [l (or limit 10)
              periods (hp/process-finished-workplace-periods start end l)]
          (restful/rest-ok (count periods))))

      (c-api/GET "/kasittelemattomat-heratteet" []
        :summary "HOKSit, joilla on käsittelemättömiä herätteitä"
        :query-params [start :- LocalDate
                       end :- LocalDate
                       limit :- (s/maybe s/Int)]
        (let [l (or limit 10)
              hoksit (hp/process-hoksit-without-kyselylinkit start end l)]
          (restful/rest-ok (count hoksit))))

      (c-api/PATCH "/osaamisenhankkimistavat/:id/kasitelty" []
        :path-params [id :- s/Int]
        (hp/set-tep-kasitelty id true)
        (response/no-content))

      (c-api/PATCH "/hoksit/:id/aloitusherate-kasitelty" []
        :path-params [id :- s/Int]
        (hp/set-aloitusherate-kasitelty id true)
        (response/no-content))

      (c-api/PATCH "/hoksit/:id/paattoherate-kasitelty" []
        :path-params [id :- s/Int]
        (hp/set-paattoherate-kasitelty id true)
        (response/no-content))

      (c-api/POST "/hoksit/resend-aloitusherate" request
        :summary "Lähettää uudet aloituskyselyherätteet herätepalveluun"
        :header-params [caller-id :- s/Str]
        :query-params [from :- LocalDate
                       to :- LocalDate]
        (let [count (hp/resend-aloituskyselyherate-between from to)]
          (restful/rest-ok {:count count})))

      (c-api/POST "/hoksit/resend-paattoherate" request
        :summary "Lähettää uudet päättökyselyherätteet herätepalveluun"
        :header-params [caller-id :- s/Str]
        :query-params [from :- LocalDate
                       to :- LocalDate]
        (let [count (hp/resend-paattokyselyherate-between from to)]
          (restful/rest-ok {:count count})))

      (c-api/POST "/opiskeluoikeus-update" request
        :summary "Päivittää aktiivisten hoksien opiskeluoikeudet Koskesta"
        :header-params [caller-id :- s/Str]
        (future (h/update-opiskeluoikeudet))
        (response/no-content))

      (c-api/POST "/onrmodify" request
        :summary "Tarkastaa päivitetyn henkilön tiedot eHoksissa
            ja tekee tarvittaessa muutokset.
            Huom: Opiskeluoikeudet taulun oppija-oid päivittyy on update cascade
            säännön kautta."
        :header-params [caller-id :- s/Str]
        :query-params [oid :- s/Str]
        (if-let [oppija (op/get-oppija-by-oid oid)]
          ;; Jos päivitetyn oppijan oid löytyy ehoksista, niin tiedetään
          ;; että kyseessä ei ole oid-tiedon päivitys ja voidaan vain tarkastaa,
          ;; että nimi täsmää ONR:n tiedon kanssa.
          (let [onr-oppija (:body (onr/find-student-by-oid-no-cache oid))
                ehoks-oppija-nimi (:nimi oppija)
                onr-oppija-nimi (op/format-oppija-name onr-oppija)]
            (when (not= ehoks-oppija-nimi onr-oppija-nimi)
              (log/infof "Updating changed name for oppija %s" oid)
              (op/update-oppija! oid true)))
          ;; Jos oppijaa ei löydy päivitetyllä oidilla ehoksista,
          ;; niin ensin tarkastetaan, ettei kyseessä ole duplicate/slave oid.
          ;; (tämä saattaa olla turha tarkastus, mutta ainakin se estää sen,
          ;; että koskaan päivitettäisiin slave oideja ehoksin tauluihin.
          ;;
          ;; Sitten haetaan kyseisen master-oidin slavet ja niiden oideilla
          ;; oppijat ehoksin oppijat-taulusta.
          ;;
          ;; Jos oppijoita löytyy slave oideilla, niin päivitetään niiden
          ;; hokseihin, opiskeluoikeuksiin ja oppijat-taulun riviin uusi
          ;; master-oid.
          (let [onr-oppija (:body (onr/find-student-by-oid-no-cache oid))]
            (when-not (:duplicate onr-oppija)
              (let [slave-oppija-oids
                    (map
                      :oidHenkilo
                      (:body (onr/get-slaves-of-master-oppija-oid oid)))
                    oppijas-from-oppijaindex-by-slave-oids
                    (remove nil?
                            (flatten
                              (map
                                #(:oid (op/get-oppija-by-oid %))
                                slave-oppija-oids)))]
                (when (seq oppijas-from-oppijaindex-by-slave-oids)
                  (jdbc/with-db-transaction
                    [db-conn (db-ops/get-db-connection)]
                    (doseq [oppija-oid oppijas-from-oppijaindex-by-slave-oids]
                      (log/infof (str "Changing duplicate oppija-oid %s to %s "
                                      "for tables hoksit, oppijat and "
                                      "opiskeluoikeudet.")
                                 oppija-oid oid)
                      (db-hoks/update-hoks-by-oppija-oid! oppija-oid
                                                          {:oppija-oid oid}
                                                          db-conn)
                      (db-oppija/update-oppija!
                        oppija-oid
                        {:oid  oid
                         :nimi (op/format-oppija-name onr-oppija)}))))))))
        (response/no-content))

      (c-api/GET "/tyoelamajaksot-active-between" []
        :summary "Työelämäjaksot voimassa aikavälin sisällä tietyllä oppijalla"
        :query-params [oppija :- s/Str
                       start :- LocalDate
                       end :- LocalDate]
        (restful/rest-ok
          (hp/select-tyoelamajaksot-active-between oppija start end))))))
