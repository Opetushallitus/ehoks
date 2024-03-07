(ns oph.ehoks.virkailija.system-handler
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]
            [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.opiskelijapalaute :as op]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.virkailija.schema :as virkailija-schema]
            [ring.util.http-response :as response]
            [schema.core :as s])
  (:import
    (java.time LocalDate)))

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
        (assoc (restful/rest-ok {:size (c/size)})
               :audit-data {:target {:system-info ::cache}}))

      (c-api/GET "/memory" []
        :summary "Järjestelmän tiedot: Muisti."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoMemory)
        (assoc (let [runtime (Runtime/getRuntime)]
                 (restful/rest-ok
                   {:total (.totalMemory runtime)
                    :free (.freeMemory runtime)
                    :max (.maxMemory runtime)}))
               :audit-data {:target {:system-info ::memory}}))

      (c-api/GET "/oppijaindex" []
        :summary "Järjestelmän tiedot: Oppijaindex."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoOppijaindex)
        (assoc (restful/rest-ok
                 {:unindexedOppijat
                  (oi/get-oppijat-without-index-count)
                  :unindexedOpiskeluoikeudet
                  (oi/get-opiskeluoikeudet-without-index-count)
                  :unindexedTutkinnot
                  (oi/get-opiskeluoikeudet-without-tutkinto-count)})
               :audit-data {:target {:system-info ::oppija-index}}))

      (c-api/GET "/hoksit" []
        :summary "Järjestelmän tiedot: Hoksit."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoHoksit)
        (assoc
          (restful/rest-ok {:amount (:count (oi/get-amount-of-hoks))})
          :audit-data {:target {:system-info ::hoks-count}})))

    (c-api/POST "/index" []
      :summary "Indeksoi oppijat ja opiskeluoikeudet"
      :header-params [caller-id :- s/Str]
      (a/go
        (oi/update-oppijat-without-index!)
        (oi/update-opiskeluoikeudet-without-index!)
        (oi/update-opiskeluoikeudet-without-tutkinto!)
        (assoc (response/ok)
               :audit-data
               {:operation
                :system-operation/index-oppijat-and-opiskeluoikeudet})))

    (c-api/DELETE "/cache" []
      :summary "Välimuistin tyhjennys"
      :header-params [caller-id :- s/Str]
      (c/clear-cache!)
      (assoc (response/ok)
             :audit-data
             {:operation :system-operation/clear-cache}))

    (c-api/PUT "/oppija/update" request
      :summary "Päivittää oppijan tiedot oppija-indeksiin"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOppija]
      (let [oppija-oid (:oppija-oid data)]
        (assoc
          (if (empty? (db-hoks/select-hoks-by-oppija-oid oppija-oid))
            (response/not-found
              {:error (str "Tällä oppija-oidilla ei löydy hoksia "
                           "ehoks-järjestelmästä")})
            (do
              (if (some? (db-oppija/select-oppija-by-oid oppija-oid))
                (oi/update-oppija! oppija-oid)
                (oi/add-oppija-without-error-forwarding! oppija-oid))
              (response/no-content)))
          :audit-data {:operation :system-operation/update-oppija
                       :target    {:oppija-oid oppija-oid}})))

    (c-api/POST "/onrmodify" request
      :summary "Tarkastaa päivitetyn henkilön tiedot eHoksissa
          ja tekee tarvittaessa muutokset.
          Huom: Opiskeluoikeudet taulun oppija-oid päivittyy on update cascade
          säännön kautta."
      :header-params [caller-id :- s/Str]
      :query-params [oid :- s/Str]
      (oi/handle-onrmodified oid)
      (assoc (response/no-content)
             :audit-log {:operation :system-operation/update-oppija-onr
                         :target    {:oppija-oid oid}}))

    (c-api/GET "/opiskeluoikeus/:opiskeluoikeus-oid" request
      :summary "Palauttaa HOKSin opiskeluoikeuden oidilla"
      :header-params [caller-id :- s/Str]
      :path-params [opiskeluoikeus-oid :- s/Str]
      :return (restful/response {:id s/Int})
      (if-let [hoks (-> (db-hoks/select-hoksit-by-opiskeluoikeus-oid)
                        first
                        not-empty)]
        (assoc (restful/rest-ok {:id (:id hoks)})
               :audit-data {:target {:hoks-id            (:id hoks)
                                     :oppija-oid         (:oppija-oid hoks)
                                     :opiskeluoikeus-oid opiskeluoikeus-oid}})
        (do
          (log/warn "No HOKS found with given opiskeluoikeus "
                    opiskeluoikeus-oid)
          (assoc (response/not-found
                   {:error "No HOKS found with given opiskeluoikeus"})
                 :audit-data
                 {:target {:opiskeluoikeus-oid opiskeluoikeus-oid}}))))

    (c-api/PUT "/opiskeluoikeus/update" request
      :summary "Poistaa ja hakee uudelleen tiedot opiskeluoikeusindeksiin"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOpiskeluoikeus]
      (if (empty? (db-hoks/select-hoksit-by-opiskeluoikeus-oid
                    (:opiskeluoikeus-oid data)))
        (response/not-found
          {:error (str "Tällä opiskeluoikeudella ei löydy hoksia "
                       "ehoks-järjestelmästä")})
        (do
          (db-opiskeluoikeus/delete-opiskeluoikeus-from-index!
            (:opiskeluoikeus-oid data))
          (a/go
            (oi/update-opiskeluoikeudet-without-index!)
            (assoc (response/no-content)
                   :audit-data
                   {:operation :system-operation/update-opiskeluoikeus-in-index
                    :target    {:opiskeluoikeus-oid
                                (:opiskeluoikeus-oid data)}})))))

    (c-api/PUT "/opiskeluoikeudet/update" request
      :summary "Poistaa ja hakee uudelleen tiedot opiskeluoikeusindeksiin
      koulutustoimijan perusteella"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOpiskeluoikeudet]
      (assoc (if (-> (:koulutustoimija-oid data)
                     db-opiskeluoikeus/delete-from-index-by-koulutustoimija!
                     first
                     pos?)
               (a/go (oi/update-opiskeluoikeudet-without-index!)
                     (response/no-content))
               (response/not-found
                 {:error "No opiskeluoikeus found with given oid"}))
             :audit-data
             {:operation :system-operation/reset-opiskeluoikeus-index}))

    (c-api/GET "/opiskeluoikeudet/:koulutustoimija-oid/deletion-info" request
      :summary "Palauttaa opiskeluoikeuksien määrän poistamisen varmistusta
      varten"
      :header-params [caller-id :- s/Str]
      :path-params [koulutustoimija-oid :- s/Str]
      :return (restful/response s/Int)
      (assoc
        (if-let [info
                 (db-opiskeluoikeus/select-opiskeluoikeus-delete-confirm-info
                   koulutustoimija-oid)]
          (restful/rest-ok info)
          (response/not-found {:error "No opiskeluoikeus found
                                       with given koulutustoimija-id"}))
        :audit-data {:target {:koulutustoimija-oid koulutustoimija-oid}}))

    (c-api/GET "/hoks/:hoks-id" request
      :summary "Palauttaa HOKSin hoks-id:llä"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response {:opiskeluoikeus-oid s/Str
                                 :oppija-oid s/Str})
      (if-let [hoks (db-hoks/select-hoks-by-id hoks-id)]
        (restful/rest-ok {:opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
                          :oppija-oid         (:oppija-oid hoks)})
        (do (log/warn "No HOKS found with given hoks-id" hoks-id)
            (response/not-found {:error "No HOKS found with given hoks-id"}))))

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
      (let [hoks (h/get-hoks-by-id hoks-id)]
        (if (pos? (first (db-hoks/delete-hoks-by-hoks-id hoks-id)))
          (assoc (restful/rest-ok {}) :audit-data {:old hoks})
          (response/not-found {:error "No HOKS found with given hoks-id"}))))

    (c-api/PATCH "/hoks/:hoks-id/undo-shallow-delete" request
      :summary "Poistaa deleted_at arvon hoksilta, joka on asetettu
      käyttöliittymän poista-ominaisuudella."
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response {})
      (if (pos? (first (db-hoks/undo-soft-delete hoks-id)))
        (assoc (restful/rest-ok {})
               :audit-data {:old {:id hoks-id}
                            :new {:id hoks-id :deleted_at "*REMOVED*"}})
        (response/not-found {:error "No HOKS found with given hoks-id"})))

    (c-api/POST "/hoks/:hoks-id/resend-aloitusherate" request
      :summary "Lähettää uuden aloituskyselyherätteen herätepalveluun"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      (if-let [hoks (h/get-hoks-with-hankittavat-koulutuksen-osat! hoks-id)]
        (if (op/send-if-needed! :aloituskysely hoks)
          (response/no-content)
          (response/bad-request
            {:error (str "Either `osaamisen-hankkimisen-tarve` is `false` or "
                         "HOKS is TUVA related.")}))
        (do (log/warn "No HOKS found with given hoks-id " hoks-id)
            (response/not-found {:error "No HOKS found with given hoks-id"}))))

    (c-api/POST "/hoks/:hoks-id/resend-paattoherate" request
      :summary "Lähettää uuden päättökyselyherätteen herätepalveluun"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      (if-let [hoks (h/get-hoks-with-hankittavat-koulutuksen-osat! hoks-id)]
        (if (op/send-if-needed! :paattokysely hoks)
          (response/no-content)
          (response/bad-request
            {:error (str "Either `osaamisen-hankkimisen-tarve` is `false`, "
                         "`osaamisen-hankkimisen-pvm` has not been set or "
                         "HOKS is TUVA related.")}))
        (do (log/warn "No HOKS found with given hoks-id:" hoks-id)
            (response/not-found {:error "No HOKS found with given hoks-id"}))))

    (c-api/POST "/hoks/resend-aloitusherate" request
      :summary "Lähettää uudet aloituskyselyherätteet herätepalveluun"
      :header-params [caller-id :- s/Str]
      :query-params [from :- LocalDate
                     to :- LocalDate]
      :return {:count s/Int}
      (let [hoksit (db-hoks/select-non-tuva-hoksit-created-between from to)
            count  (op/send-every-needed! :aloituskysely hoksit)]
        (assoc (restful/rest-ok {:count count})
               :audit-data
               {:operation :system-operation/resend-aloitusherate})))

    (c-api/POST "/hoks/resend-paattoherate" request
      :summary "Lähettää uudet päättökyselyherätteet herätepalveluun"
      :header-params [caller-id :- s/Str]
      :query-params [from :- LocalDate
                     to :- LocalDate]
      :return {:count s/Int}
      (let [hoksit (db-hoks/select-non-tuva-hoksit-finished-between from to)
            count  (op/send-every-needed! :paattokysely hoksit)]
        (assoc (restful/rest-ok {:count count})
               :audit-data
               {:operation :system-operation/resend-paattoherate})))))
