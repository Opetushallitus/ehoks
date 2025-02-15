(ns oph.ehoks.heratepalvelu.herate-handler
  (:require [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.heratepalvelu :as hp]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.schema.oid :as oid-schema]
            [ring.util.http-response :as response]
            [schema.core :as s])
  (:import (java.time LocalDate)))

(def routes
  "Herätepalvelun reitit"
  (c-api/context "/heratepalvelu" []
    :tags ["heratepalvelu"]
    :header-params [ticket :- s/Str
                    caller-id :- s/Str]

    (route-middleware
      [wrap-user-details m/wrap-require-service-user
       audit/wrap-logger m/wrap-require-oph-privileges]

      (c-api/GET "/tyoelamajaksot" []
        :summary "Päättyneet työelämäjaksot"
        :query-params [start :- LocalDate
                       end :- LocalDate
                       limit :- (s/maybe s/Int)]
        :return (restful/response s/Int)
        (let [l (or limit 10)
              periods (tep/process-finished-workplace-periods! start end l)]
          (restful/ok (count periods))))

      (c-api/GET "/kasittelemattomat-heratteet" []
        :summary
        "lähettää uudestaan herätepalveluun herätteet HOKSeista,
        joilla on käsittelemättömiä herätteitä.  Herätepalvelu
        kutsuu tätä säännöllisesti."
        :query-params [start :- LocalDate
                       end :- LocalDate
                       limit :- (s/maybe s/Int)]
        :return (restful/response s/Int)
        (let [l (or limit 10)
              hoksit (hp/process-hoksit-without-kyselylinkit start end l)]
          (restful/ok (count hoksit))))

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
        :summary
        "Lähettää uudet aloituskyselyherätteet herätepalveluun.
        Herätepalvelu kutsuu tätä viikon välein saadakseen
        varmistettua kahden viime viikon herätteet (jotka lähetetään
        uudestaan)."
        :header-params [caller-id :- s/Str]
        :query-params [from :- LocalDate
                       to :- LocalDate]
        :return (restful/response {:count s/Int})
        (let [result (op/reinitiate-hoksit-between! :aloituskysely from to)]
          (restful/ok {:count result})))

      (c-api/POST "/hoksit/resend-paattoherate" request
        :summary
        "Lähettää uudet päättökyselyherätteet herätepalveluun.
        Herätepalvelu kutsuu tätä viikon välein saadakseen
        varmistettua kahden viime viikon herätteet (jotka lähetetään
        uudestaan)."
        :header-params [caller-id :- s/Str]
        :query-params [from :- LocalDate
                       to :- LocalDate]
        :return (restful/response {:count s/Int})
        (let [result (op/reinitiate-hoksit-between! :paattokysely from to)]
          (restful/ok {:count result})))

      (c-api/POST "/opiskeluoikeus-update" request
        :summary "Päivittää aktiivisten hoksien opiskeluoikeudet Koskesta"
        :header-params [caller-id :- s/Str]
        (future (hoks/update-opiskeluoikeudet))
        (response/no-content))

      (c-api/POST "/onrmodify" request
        :summary "Tarkastaa päivitetyn henkilön tiedot eHoksissa
            ja tekee tarvittaessa muutokset.
            Huom: Opiskeluoikeudet taulun oppija-oid päivittyy on update cascade
            säännön kautta."
        :header-params [caller-id :- s/Str]
        :query-params [oid :- oid-schema/OppijaOID]
        (oi/handle-onrmodified oid)
        ; TODO refaktoroi onr-käsittelyä auditlokitusystävällisemmäksi (OY-4523)
        (response/no-content))

      (c-api/GET "/tyoelamajaksot-active-between" []
        :summary "Työelämäjaksot voimassa aikavälin sisällä tietyllä oppijalla"
        :query-params [oppija :- oid-schema/OppijaOID
                       start :- LocalDate
                       end :- LocalDate]
        (restful/ok (hp/select-tyoelamajaksot-active-between oppija start end)))

      (c-api/DELETE "/tyopaikkaohjaajan-yhteystiedot" []
        :summary "Poistaa työpaikkaohjaajan yhteystiedot yli kolme kuukautta
            sitten päättyneistä työpaikkajaksoista. Käsittelee max 100 jaksoa
            kerrallaan. Palauttaa kyseisten jaksojen id:t (hankkimistapa-id)
            herätepalvelua varten."
        :header-params [caller-id :- s/Str]
        :return (restful/response {:hankkimistapa-ids [s/Int]})
        (let [hankkimistavat (db-hoks/delete-tyopaikkaohjaajan-yhteystiedot!)]
          (assoc
            (restful/ok {:hankkimistapa-ids hankkimistavat})
            ::audit/target {:oht-ids hankkimistavat})))

      (c-api/DELETE "/opiskelijan-yhteystiedot" []
        :summary "Poistaa opiskelijan yhteystiedot yli kolme kuukautta
            sitten päättyneistä hokseista. Käsittelee max 100 opiskelijan
            tiedot kerrallaan. Palauttaa kyseisten tapausten hoks id:t
            herätepalvelua varten."
        :header-params [caller-id :- s/Str]
        :return (restful/response {:hoks-ids [s/Int]})
        (let [hoks-ids (db-hoks/delete-opiskelijan-yhteystiedot!)]
          (assoc
            (restful/ok {:hoks-ids hoks-ids})
            :audit/target {:hoks-ids hoks-ids}))))))
