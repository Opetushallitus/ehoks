(ns oph.ehoks.heratepalvelu.herate-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
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
            ja tekee tarvittaessa muutokset"
        :header-params [caller-id :- s/Str]
        :query-params [oid :- s/Str]
        (if-let [oppija (op/get-oppija-by-oid oid)]
          (let [onr-oppija (:body (onr/find-student-by-oid-no-cache oid))
                ehoks-oppija-nimi (:nimi oppija)
                onr-oppija-nimi (format "%s %s"
                                        (:etunimet onr-oppija)
                                        (:sukunimi onr-oppija))]
            (println (str "oppija löytyi " oid))
            (println (str "ehoks nimi " ehoks-oppija-nimi))
            (println (str "onr nimi " onr-oppija-nimi))
            (when (not= ehoks-oppija-nimi onr-oppija-nimi)
              (op/update-oppija! oid true)))
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
                (println (str "Ei oppijaa ehoksissa, eikä slave " oid))
                (println slave-oppija-oids)
                (println oppijas-from-oppijaindex-by-slave-oids)
                (when (seq oppijas-from-oppijaindex-by-slave-oids)
                  (jdbc/with-db-transaction
                    [db-conn (db-ops/get-db-connection)]
                    (doseq [oppija-oid oppijas-from-oppijaindex-by-slave-oids]
                      (println (str "päivitetään hoksit oidille "
                                    oppija-oid
                                    " oidiin "
                                    oid))
                      (db-hoks/update-hoks-by-oppija-oid! oppija-oid
                                                          {:oppija-oid oid}
                                                          db-conn)
                      (println (str "päivitetään oppija oidille "
                                    oppija-oid
                                    " oidiin "
                                    oid))
                      (db-oppija/update-oppija!
                        oppija-oid
                        {:oid  oid
                         :nimi (format "%s %s"
                                       (:etunimet onr-oppija)
                                       (:sukunimi onr-oppija))}))))))))
        (response/no-content))

      (c-api/GET "/tyoelamajaksot-active-between" []
        :summary "Työelämäjaksot voimassa aikavälin sisällä tietyllä oppijalla"
        :query-params [oppija :- s/Str
                       start :- LocalDate
                       end :- LocalDate]
        (restful/rest-ok
          (hp/select-tyoelamajaksot-active-between oppija start end))))))
