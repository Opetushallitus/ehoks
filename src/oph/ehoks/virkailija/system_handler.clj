(ns oph.ehoks.virkailija.system-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.virkailija.schema :as virkailija-schema]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.oppijaindex :as op]
            [oph.ehoks.heratepalvelu.heratepalvelu :as hp]
            [clojure.core.async :as a]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [schema.core :as s]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.external.aws-sqs :as sqs])
  (:import (java.time LocalDate)))

(def routes
  (route-middleware
    [m/wrap-oph-super-user]

    (c-api/GET "/system-info" []
      :summary "Järjestelmän tiedot"
      :header-params [caller-id :- s/Str]
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
      :header-params [caller-id :- s/Str]
      (a/go
        (op/update-oppijat-without-index!)
        (op/update-opiskeluoikeudet-without-index!)
        (op/update-opiskeluoikeudet-without-tutkinto!)
        (response/ok)))

    (c-api/DELETE "/cache" []
      :summary "Välimuistin tyhjennys"
      :header-params [caller-id :- s/Str]
      (c/clear-cache!)
      (response/ok))

    (c-api/PUT "/oppija/update" request
      :summary "Päivittää oppijan tiedot oppija-indeksiin"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOppija]
      (if (empty? (db-hoks/select-hoks-by-oppija-oid (:oppija-oid data)))
        (response/not-found {:error "Tällä oppija-oidilla ei löydy hoksia
        ehoks-järjestelmästä"})
        (do
          (if (some? (db-oppija/select-oppija-by-oid (:oppija-oid data)))
            (op/update-oppija! (:oppija-oid data))
            (op/add-oppija-without-error-forwarding! (:oppija-oid data)))
          (response/no-content))))

    (c-api/GET "/opiskeluoikeus/:opiskeluoikeus-oid" request
      :summary "Palauttaa HOKSin opiskeluoikeuden oidilla"
      :header-params [caller-id :- s/Str]
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

    (c-api/PUT "/opiskeluoikeus/update" request
      :summary "Poistaa ja hakee uudelleen tiedot opiskeluoikeusindeksiin"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOpiskeluoikeus]
      (if (empty? (db-hoks/select-hoksit-by-opiskeluoikeus-oid
                    (:opiskeluoikeus-oid data)))
        (response/not-found {:error "Tällä opiskeluoikeudella ei löydy hoksia
        ehoks-järjestelmästä"})
        (do
          (db-opiskeluoikeus/delete-opiskeluoikeus-from-index!
            (:opiskeluoikeus-oid data))
          (a/go
            (op/update-opiskeluoikeudet-without-index!)
            (response/no-content)))))

    (c-api/PUT "/opiskeluoikeudet/update" request
      :summary "Poistaa ja hakee uudelleen tiedot opiskeluoikeusindeksiin
      koulutustoimijan perusteella"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOpiskeluoikeudet]
      (if (pos? (first
                  (db-opiskeluoikeus/delete-from-index-by-koulutustoimija!
                    (:koulutustoimija-oid data))))
        (a/go
          (op/update-opiskeluoikeudet-without-index!)
          (response/no-content))
        (response/not-found {:error "No opiskeluoikeus found with given oid"})))

    (c-api/GET "/opiskeluoikeudet/:koulutustoimija-oid/deletion-info" request
      :summary "Palauttaa opiskeluoikeuksien määrän poistamisen varmistusta
      varten"
      :header-params [caller-id :- s/Str]
      :path-params [koulutustoimija-oid :- s/Str]
      :return (restful/response s/Int)
      (if-let [info (db-opiskeluoikeus/select-opiskeluoikeus-delete-confirm-info
                      koulutustoimija-oid)]
        (restful/rest-ok info)
        (response/not-found {:error "No opiskeluoikeus found
                                     with given koulutustoimija-id"})))

    (c-api/GET "/hoks/:hoks-id" request
      :summary "Palauttaa HOKSin hoks-id:llä"
      :header-params [caller-id :- s/Str]
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
      :summary "Palauttaa tietoja HOKSista, opiskeluoikeudesta ja oppijasta
                poistamisen varmistusta varten"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response virkailija-schema/DeleteConfirmInfo)
      (if-let [info (db-hoks/select-hoks-delete-confirm-info hoks-id)]
        (restful/rest-ok info)
        (response/not-found {:error "No HOKS or opiskeluoikeus found
                                     with given hoks-id"})))

    (c-api/DELETE "/hoks/:hoks-id" request
      :summary "Poistaa HOKSin hoks-id:llä"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response {})
      (if (pos? (first (db-hoks/delete-hoks-by-hoks-id hoks-id)))
        (restful/rest-ok {})
        (response/not-found {:error "No HOKS found with given hoks-id"})))

    (c-api/POST "/hoks/:hoks-id/resend-aloitusherate" request
      :summary "Lähettää uuden aloituskyselyherätteen herätepalveluun"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      (let [hoks (db-hoks/select-hoks-by-id hoks-id)]
        (if hoks
          (if (:osaamisen-hankkimisen-tarve hoks)
            (do
              (sqs/send-amis-palaute-message (sqs/build-hoks-hyvaksytty-msg
                                               hoks-id hoks))
              (response/no-content))
            (do
              (log/warn "Osaamisen hankkimisen tarve false "
                        hoks-id)
              (response/bad-request
                {:error "Osaamisen hankkimisen tarve false"})))
          (do
            (log/warn "No HOKS found with given hoks-id "
                      hoks-id)
            (response/not-found
              {:error "No HOKS found with given hoks-id"})))))

    (c-api/POST "/hoks/resend-aloitusherate" request
      :summary "Lähettää uudet aloituskyselyherätteet herätepalveluun"
      :header-params [caller-id :- s/Str]
      :query-params [from :- LocalDate
                     to :- LocalDate]
      (let [count (hp/resend-aloituskyselyherate-between from to)]
        (restful/rest-ok {:count count})))))
