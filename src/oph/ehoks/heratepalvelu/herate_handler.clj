(ns oph.ehoks.heratepalvelu.herate-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            ;[oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.heratepalvelu.heratepalvelu :as hp]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.hoks.hoks :as h])
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
        (hp/handle-onrmodified oid)
        (response/no-content))

      (c-api/GET "/tyoelamajaksot-active-between" []
        :summary "Työelämäjaksot voimassa aikavälin sisällä tietyllä oppijalla"
        :query-params [oppija :- s/Str
                       start :- LocalDate
                       end :- LocalDate]
        (restful/rest-ok
          (hp/select-tyoelamajaksot-active-between oppija start end)))

      (c-api/DELETE "/tyopaikkaohjaajan-yhteystiedot" []
        :summary "Poistaa työpaikkaohjaajan yhteystiedot yli kolme kuukautta
            sitten päättyneistä työpaikkajaksoista. Käsittelee max 5000 jaksoa
            kerrallaan. Palauttaa kyseisten jaksojen id:t (hankkimistapa-id)
            herätepalvelua varten. POISTETTU KÄYTÖSTÄ TILAPÄISESTI."
        :header-params [caller-id :- s/Str]

        (let [hankkimistavat
              []] ;(db-hoks/delete-tyopaikkaohjaajan-yhteystiedot!)]
          (restful/rest-ok {:hankkimistapa-ids hankkimistavat})))

      (c-api/DELETE "/opiskelijan-yhteystiedot" []
        :summary "Poistaa opiskelijan yhteystiedot yli kolme kuukautta
            sitten päättyneistä hokseista. Käsittelee max 500 opiskelijan
            tiedot kerrallaan. Palauttaa kyseisten tapausten hoks id:t
            herätepalvelua varten. POISTETTU KÄYTÖSTÄ TILAPÄISESTI."
        :header-params [caller-id :- s/Str]
        (let [hoks-ids []] ;(db-hoks/delete-opiskelijan-yhteystiedot!)]
          (restful/rest-ok {:hoks-ids hoks-ids}))))))
