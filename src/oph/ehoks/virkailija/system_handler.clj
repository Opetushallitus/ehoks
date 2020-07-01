(ns oph.ehoks.virkailija.system-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.virkailija.schema :as virkailija-schema]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.oppijaindex :as op]
            [clojure.core.async :as a]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [schema.core :as s]))

(def routes
  (route-middleware
    [m/wrap-oph-super-user]

    (c-api/GET "/system-info" []
      :summary "Järjestelmän tiedot"
      :return (restful/response virkailija-schema/SystemInfo)
      (let [runtime (Runtime/getRuntime)]
        (restful/rest-ok
          {:cache {:size (c/size)}
           :memory {:total (.totalMemory runtime)
                    :free (.freeMemory runtime)
                    :max (.maxMemory runtime)}
           :oppijaindex
           {:unindexedOppijat
            (op/get-oppijat-without-index-count)
            :unindexedOpiskeluoikeudet
            (op/get-opiskeluoikeudet-without-index-count)
            :unindexedTutkinnot
            (op/get-opiskeluoikeudet-without-tutkinto-count)}
           :hoksit {:amount (:count (op/get-amount-of-hoks))}})))

    (c-api/POST "/index" []
      :summary "Indeksoi oppijat ja opiskeluoikeudet"
      (a/go
        (op/update-oppijat-without-index!)
        (op/update-opiskeluoikeudet-without-index!)
        (op/update-opiskeluoikeudet-without-tutkinto!)
        (response/ok)))

    (c-api/DELETE "/cache" []
      :summary "Välimuistin tyhjennys"
      (c/clear-cache!)
      (response/ok))

    (c-api/GET "/opiskeluoikeus/:opiskeluoikeus-oid" request
      :summary "Palauttaa HOKSin opiskeluoikeuden oidilla"
      :path-params [opiskeluoikeus-oid :- s/Str]
      :return (restful/response {:id s/Int})
      (let [hoks (first (db-hoks/select-hoksit-by-opiskeluoikeus-oid
                          opiskeluoikeus-oid))]
        (if hoks
          (restful/rest-ok {:id (:id hoks)})
          (do
            (log/warn "No HOKS found with given opiskeluoikeus "
                      opiskeluoikeus-oid)
            (response/not-found
              {:error "No HOKS found with given opiskeluoikeus"})))))

    (c-api/GET "/hoks/:hoks-id" request
      :summary "Palauttaa HOKSin hoks-id:llä"
      :path-params [hoks-id :- s/Int]
      :return (restful/response {:opiskeluoikeus-oid s/Str
                                 :oppija-oid s/Str})
      (let [hoks (db-hoks/select-hoks-by-id
                   hoks-id)]
        (if hoks
          (restful/rest-ok {:opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
                            :oppija-oid (:oppija-oid hoks)})
          (do
            (log/warn "No HOKS found with given hoks-id "
                      hoks-id)
            (response/not-found
              {:error "No HOKS found with given hoks-id"})))))

    (c-api/GET "/hoks/:hoks-id/deletion-info" request
      :summary "Palauttaa poistettava HOKSin varmistustietoja hoks-id:llä"
      :path-params [hoks-id :- s/Int]
      :return (restful/response virkailija-schema/DeleteConfirmInfo)
      (if-let [info (db-hoks/select-hoks-delete-confirm-info hoks-id)]
        (restful/rest-ok info)
        (response/not-found {:error "No HOKS found with given hoks-id"})))

    (c-api/DELETE "/hoks/:hoks-id" request
      :summary "Poistaa HOKSin hoks-id:llä"
      :path-params [hoks-id :- s/Int]
      :return (restful/response {})
      (if-let [_ (db-hoks/delete-hoks-by-hoks-id hoks-id)]
        (restful/rest-ok {})
        (response/not-found {:error "No HOKS found with given hoks-id"})))))
