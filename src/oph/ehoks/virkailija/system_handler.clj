(ns oph.ehoks.virkailija.system-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.virkailija.schema :as virkailija-schema]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.oppijaindex :as op]
            [oph.ehoks.heratepalvelu.heratepalvelu :as hp]
            [oph.ehoks.hoks.hoks :as h]
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
  "System handlerin reitit"
  (route-middleware
    [m/wrap-oph-super-user]

    (c-api/context "/system-info" []
      :tags ["system-info"]

      (c-api/GET "/cache" []
        :summary "Järjestelmän tiedot: Cache."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoCache)
        (restful/rest-ok
          {:size (c/size)}))

      (c-api/GET "/memory" []
        :summary "Järjestelmän tiedot: Muisti."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoMemory)
        (let [runtime (Runtime/getRuntime)]
          (restful/rest-ok
            {:total (.totalMemory runtime)
             :free (.freeMemory runtime)
             :max (.maxMemory runtime)})))

      (c-api/GET "/oppijaindex" []
        :summary "Järjestelmän tiedot: Oppijaindex."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoOppijaindex)
        (restful/rest-ok
          {:unindexedOppijat
           (op/get-oppijat-without-index-count)
           :unindexedOpiskeluoikeudet
           (op/get-opiskeluoikeudet-without-index-count)
           :unindexedTutkinnot
           (op/get-opiskeluoikeudet-without-tutkinto-count)}))

      (c-api/GET "/hoksit" []
        :summary "Järjestelmän tiedot: Hoksit."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoHoksit)
        (restful/rest-ok {:amount (:count (op/get-amount-of-hoks))})))

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
            (log/warn "No HOKS found with given hoks-id" hoks-id)
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

    (c-api/PATCH "/hoks/:hoks-id/undo-shallow-delete" request
      :summary "Poistaa deleted_at arvon hoksilta, joka on asetettu
      käyttöliittymän poista-ominaisuudella."
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response {})
      (if (pos? (first (db-hoks/undo-shallow-delete hoks-id)))
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
              (h/send-aloituskysely hoks-id hoks)
              (response/no-content))
            (do
              (log/info (str "Did not resend aloitusheräte "
                             "(osaamisen-hankkimisen-tarve=false) for hoks-id "
                             hoks-id))
              (response/bad-request
                {:error "Osaamisen hankkimisen tarve false"})))
          (do
            (log/warn "No HOKS found with given hoks-id "
                      hoks-id)
            (response/not-found
              {:error "No HOKS found with given hoks-id"})))))

    (c-api/POST "/hoks/:hoks-id/resend-paattoherate" request
      :summary "Lähettää uuden päättökyselyherätteen herätepalveluun"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      (let [hoks (db-hoks/select-hoks-by-id hoks-id)]
        (if hoks
          (if (and (:osaamisen-hankkimisen-tarve hoks)
                   (:osaamisen-saavuttamisen-pvm hoks))
            (do
              (sqs/send-amis-palaute-message (hp/paatto-build-msg hoks))
              (response/no-content))
            (do
              (log/info (str "Did not resend päättöheräte "
                             "(osaamisen-saavuttamisen-pvm="
                             (:osaamisen-saavuttamisen-pvm hoks)
                             ", osaamisen-hankkimisen-tarve="
                             (:osaamisen-hankkimisen-tarve hoks)
                             ") for hoks-id "
                             hoks-id))
              (response/bad-request
                {:error (str "No osaamisen saavuttamisen pvm or osaamisen "
                             "hankkimisen tarve is false")})))
          (do
            (log/warn "No HOKS found with given hoks-id:" hoks-id)
            (response/not-found {:error "No HOKS found with given hoks-id"})))))

    (c-api/POST "/hoks/resend-aloitusherate" request
      :summary "Lähettää uudet aloituskyselyherätteet herätepalveluun"
      :header-params [caller-id :- s/Str]
      :query-params [from :- LocalDate
                     to :- LocalDate]
      :return {:count s/Int}
      (let [count (hp/resend-aloituskyselyherate-between from to)]
        (restful/rest-ok {:count count})))

    (c-api/POST "/hoks/resend-paattoherate" request
      :summary "Lähettää uudet päättökyselyherätteet herätepalveluun"
      :header-params [caller-id :- s/Str]
      :query-params [from :- LocalDate
                     to :- LocalDate]
      :return {:count s/Int}
      (let [count (hp/resend-paattokyselyherate-between from to)]
        (restful/rest-ok {:count count})))))
