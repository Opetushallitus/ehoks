(ns oph.ehoks.virkailija.system-handler
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]
            [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.schema.oid :as oid-schema]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.virkailija.schema :as virkailija-schema]
            [ring.util.http-response :as response]
            [schema.core :as s])
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
        (assoc (restful/ok {:size (c/size)})
               ::audit/target {:system/info :cache}))

      (c-api/GET "/memory" []
        :summary "Järjestelmän tiedot: Muisti."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoMemory)
        (let [runtime (Runtime/getRuntime)]
          (assoc (restful/ok {:total (.totalMemory runtime)
                              :free  (.freeMemory runtime)
                              :max   (.maxMemory runtime)})
                 ::audit/target {:system/info :system/memory})))

      (c-api/GET "/oppijaindex" []
        :summary "Järjestelmän tiedot: Oppijaindex."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoOppijaindex)
        (assoc
          (restful/ok
            {:unindexedOppijat
             (oi/get-oppijat-without-index-count)
             :unindexedOpiskeluoikeudet
             (oi/get-opiskeluoikeudet-without-index-count)
             :unindexedTutkinnot
             (oi/get-opiskeluoikeudet-without-tutkinto-count)})
          ::audit/target {:system/info :system/oppija-index}))

      (c-api/GET "/hoksit" []
        :summary "Järjestelmän tiedot: Hoksit."
        :header-params [caller-id :- s/Str]
        :return (restful/response virkailija-schema/SystemInfoHoksit)
        (assoc (restful/ok {:amount (:count (oi/get-amount-of-hoks))})
               ::audit/target {:system/info :system/hoks-count})))

    (c-api/POST "/index" []
      :summary "Indeksoi oppijat ja opiskeluoikeudet"
      :header-params [caller-id :- s/Str]
      (a/go
        (oi/update-oppijat-without-index!)
        (oi/update-opiskeluoikeudet-without-index!)
        (oi/update-opiskeluoikeudet-without-tutkinto!)
        (assoc (response/ok)
               ::audit/operation :system/index-oppijat-and-opiskeluoikeudet)))

    (c-api/DELETE "/cache" []
      :summary "Välimuistin tyhjennys"
      :header-params [caller-id :- s/Str]
      (c/clear-cache!)
      (assoc (response/ok) ::audit/operation :system/clear-cache))

    (c-api/PUT "/oppija/update" request
      :summary "Päivittää oppijan tiedot oppija-indeksiin"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOppija]
      (let [oppija-oid (:oppija-oid data)]
        (assoc
          (if (empty? (db-hoks/select-hoks-by-oppija-oid oppija-oid))
            (response/not-found {:error "Tällä oppija-oidilla ei löydy hoksia
            ehoks-järjestelmästä"})
            (do
              (if (some? (db-oppija/select-oppija-by-oid oppija-oid))
                (oi/update-oppija! oppija-oid)
                (oi/add-oppija-without-error-forwarding! oppija-oid))
              (response/no-content)))
          ::audit/operation :system/update-oppija
          ::audit/target    {:oppija-oid oppija-oid})))

    (c-api/POST "/onrmodify" request
      :summary "Tarkastaa päivitetyn henkilön tiedot eHoksissa
          ja tekee tarvittaessa muutokset.
          Huom: Opiskeluoikeudet taulun oppija-oid päivittyy on update cascade
          säännön kautta."
      :header-params [caller-id :- s/Str]
      :query-params [oid :- oid-schema/OppijaOID]
      (oi/handle-onrmodified oid)
      ; TODO refaktoroi onr-käsittelyä auditlokitusystävällisemmäksi (OY-4523)
      (assoc (response/no-content)
             ::audit/operation :system/update-oppija-onr
             ::audit/target    {:oppija-oid oid}))

    (c-api/GET "/opiskeluoikeus/:opiskeluoikeus-oid" request
      :summary "Palauttaa HOKSin opiskeluoikeuden oidilla"
      :header-params [caller-id :- s/Str]
      :path-params [opiskeluoikeus-oid :- oid-schema/OpiskeluoikeusOID]
      :return (restful/response {:id s/Int})
      (if-let [hoks (first (db-hoks/select-hoksit-by-opiskeluoikeus-oid
                             opiskeluoikeus-oid))]
        (assoc (restful/ok {:id (:id hoks)})
               ::audit/target (audit/hoks-target-data hoks))
        (do (log/warn "No HOKS found with given opiskeluoikeus "
                      opiskeluoikeus-oid)
            (assoc (response/not-found
                     {:error "No HOKS found with given opiskeluoikeus"})
                   ::audit/target {:opiskeluoikeus-oid opiskeluoikeus-oid}))))

    (c-api/PUT "/opiskeluoikeus/update" request
      :summary "Poistaa ja hakee uudelleen tiedot opiskeluoikeusindeksiin"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOpiskeluoikeus]
      (let [opiskeluoikeus-oid (:opiskeluoikeus-oid data)]
        (if (empty? (db-hoks/select-hoksit-by-opiskeluoikeus-oid
                      opiskeluoikeus-oid))
          (assoc (response/not-found
                   {:error (str "Tällä opiskeluoikeudella ei löydy hoksia "
                                "ehoks-järjestelmästä")})
                 ::audit/operation :system/update-opiskeluoikeus-in-index
                 ::audit/target    {:opiskeluoikeus-oid opiskeluoikeus-oid})
          (do (db-opiskeluoikeus/delete-opiskeluoikeus-from-index!
                opiskeluoikeus-oid)
              (a/go (oi/update-opiskeluoikeudet-without-index!)
                    (assoc
                      (response/no-content)
                      ::audit/operation :system/update-opiskeluoikeus-in-index
                      ::audit/target    {:opiskeluoikeus-oid
                                         opiskeluoikeus-oid}))))))

    (c-api/PUT "/opiskeluoikeudet/update" request
      :summary "Poistaa ja hakee uudelleen tiedot opiskeluoikeusindeksiin
      koulutustoimijan perusteella"
      :header-params [caller-id :- s/Str]
      :body [data virkailija-schema/UpdateOpiskeluoikeudet]
      (let [koulutustoimija-oid (:koulutustoimija-oid data)]
        (if (-> koulutustoimija-oid
                db-opiskeluoikeus/delete-from-index-by-koulutustoimija!
                first
                pos?)
          (a/go (oi/update-opiskeluoikeudet-without-index!)
                (assoc
                  (response/no-content)
                  ::audit/operation :system/reset-opiskeluoikeus-index
                  ::audit/target    {:koulutustoimija-oid
                                     koulutustoimija-oid}))
          (assoc
            (response/not-found
              {:error "No opiskeluoikeus found with given oid"})
            ::audit/operation :system/reset-opiskeluoikeus-index
            ::audit/target    {:koulutustoimija-oid koulutustoimija-oid}))))

    (c-api/GET "/opiskeluoikeudet/:koulutustoimija-oid/deletion-info" request
      :summary "Palauttaa opiskeluoikeuksien määrän poistamisen varmistusta
      varten"
      :header-params [caller-id :- s/Str]
      :path-params [koulutustoimija-oid :- oid-schema/OrganisaatioOID]
      :return (restful/response s/Int)
      (assoc
        (if-let
         [info (db-opiskeluoikeus/select-opiskeluoikeus-delete-confirm-info
                 koulutustoimija-oid)]
          (restful/ok info)
          (response/not-found {:error "No opiskeluoikeus found
                                      with given koulutustoimija-id"}))
        ::audit/target {:koulutustoimija-oid koulutustoimija-oid}))

    (c-api/GET "/hoks/:hoks-id" request
      :summary "Palauttaa HOKSin hoks-id:llä"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response {:opiskeluoikeus-oid s/Str
                                 :oppija-oid s/Str})
      (if-let [hoks (db-hoks/select-hoks-by-id hoks-id)]
        (assoc
          (restful/ok {:opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
                       :oppija-oid         (:oppija-oid hoks)})
          ::audit/target (audit/hoks-target-data hoks))
        (do
          (log/warn "No HOKS found with given hoks-id" hoks-id)
          (assoc
            (response/not-found {:error "No HOKS found with given hoks-id"})
            ::audit/target {:hoks-id hoks-id}))))

    (c-api/GET "/hoks/:hoks-id/deletion-info" request
      :summary "Palauttaa tietoja HOKSista, opiskeluoikeudesta ja oppijasta
                poistamisen varmistusta varten"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response virkailija-schema/DeleteConfirmInfo)
      (assoc
        (if-let [info (db-hoks/select-hoks-delete-confirm-info hoks-id)]
          (restful/ok info)
          (response/not-found {:error "No HOKS or opiskeluoikeus found
                                      with given hoks-id"}))
        ::audit/target {:hoks-id hoks-id}))

    (c-api/DELETE "/hoks/:hoks-id" request
      :summary "Poistaa HOKSin hoks-id:llä"
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response {})
      (let [hoks (hoks/get-by-id hoks-id)]
        (if (pos? (first (db-hoks/delete-hoks-by-hoks-id hoks-id)))
          (assoc (restful/ok {})
                 ::audit/changes {:old hoks}
                 ::audit/target  (audit/hoks-target-data hoks))
          (assoc
            (response/not-found {:error "No HOKS found with given hoks-id"})
            ::audit/target {:hoks-id hoks-id}))))

    (c-api/PATCH "/hoks/:hoks-id/undo-shallow-delete" request
      :summary "Poistaa deleted_at arvon hoksilta, joka on asetettu
      käyttöliittymän poista-ominaisuudella."
      :header-params [caller-id :- s/Str]
      :path-params [hoks-id :- s/Int]
      :return (restful/response {})
      (if (pos? (first (db-hoks/undo-soft-delete hoks-id)))
        (assoc (restful/ok {})
               ::audit/changes {:old {:id hoks-id}
                                :new {:id hoks-id :deleted_at "*REMOVED*"}}
               ::audit/target  (audit/hoks-target-data hoks-id))
        (assoc
          (response/not-found {:error "No HOKS found with given hoks-id"})
          ::audit/target {:hoks-id hoks-id})))))
