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

      (c-api/PATCH "/hoksit/:id/aloitusherate-kasitelty" []
        :path-params [id :- s/Int]
        (hp/set-aloitusherate-kasitelty id true)
        (response/no-content))

      (c-api/PATCH "/hoksit/:id/paattoherate-kasitelty" []
        :path-params [id :- s/Int]
        (hp/set-paattoherate-kasitelty id true)
        (response/no-content))

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
