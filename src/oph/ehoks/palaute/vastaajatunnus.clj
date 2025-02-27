(ns oph.ehoks.palaute.vastaajatunnus
  (:require [medley.core :refer [find-first map-vals]]
            [clojure.walk :refer [walk]]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.db :as db]
            [oph.ehoks.utils.date :as date]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute.opiskelija :as amis]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.palaute.tyoelama :as tep])
  (:import (clojure.lang ExceptionInfo)
           (java.util UUID)))

(defn- enrich-ctx!
  "Lisää muista palveluista saatavia tietoja kontekstiin myöhempää
  käsittelyä varten."
  [{:keys [hoks existing-palaute] :as ctx}]
  (let [opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                         (:opiskeluoikeus-oid hoks))
        suoritus (find-first suoritus/ammatillinen?
                             (:suoritukset opiskeluoikeus))]
    (assoc ctx
           :niputuspvm            (tep/next-niputus-date (date/now))
           :vastaamisajan-alkupvm (tep/next-niputus-date
                                    (:heratepvm existing-palaute))
           :opiskeluoikeus opiskeluoikeus
           :suoritus suoritus
           :hk-toteuttaja (delay (palaute/hankintakoulutuksen-toteuttaja! hoks))
           :koulutustoimija (palaute/koulutustoimija-oid! opiskeluoikeus)
           :toimipiste (palaute/toimipiste-oid! suoritus))))

(defn- handle-exception
  "Handles an ExceptionInfo `ex` based on `:type` in `ex-data`. If `ex` doesn't
  match any of the cases below, re-throws `ex`."
  [{:keys [existing-palaute] :as ctx} ex]
  (let [ex-params (ex-data ex)
        ex-type (:type ex-params)
        tunnus  (:arvo-tunnus ex-params)]
    (log/infof ex "Handling exception in tunnus handling")
    (when tunnus
      (log/infof "Trying to delete jaksotunnus `%s` from Arvo" tunnus)
      (arvo/delete-jaksotunnus tunnus))
    (case ex-type
      ::koski/opiskeluoikeus-not-found
      (do (log/warnf "%s. Setting `tila` to \"ei_laheteta\" for palaute `%d`."
                     (ex-message ex)
                     (:id existing-palaute))
          (palaute/update-tila!
            ctx "ei_laheteta" ex-type (select-keys existing-palaute
                                                   [:opiskeluoikeus-oid])))

      ::tep/jakso-keskeytynyt
      (do (log/warnf "%s. Setting `tila` to `ei_laheteta` for palaute `%d`. "
                     (ex-message ex)
                     (:id existing-palaute))
          (palaute/update-tila!
            ctx "ei_laheteta" ex-type
            (walk (fn [[k v]] [(name k) (str v)])
                  identity
                  (select-keys ex-params [:keskeytymisajanjaksot]))))

      ::arvo-kutsu-epaonnistui
      (let [arvo-ex (ex-cause ex)]
        (log/warn "Arvo response:" (ex-message arvo-ex))
        (tapahtuma/build-and-insert! (:ctx ex-params) :arvo-kutsu-epaonnistui
                                     {:errormsg (ex-message arvo-ex)
                                      :body     (:body (ex-data arvo-ex))}))

      ::arvo/no-jaksotunnus-created
      (log/logf (if (:configured-to-call-arvo? ex-params) :error :warn)
                (str (ex-message ex) " Skipping processing for palaute `%d`")
                (:id existing-palaute))

      ::heratepalvelu/tpo-nippu-sync-failed
      (do (log/errorf (str "%s. Trying to delete jakso corresponding to "
                           "palaute `%d` from Herätepalvelu")
                      (ex-message ex)
                      (:id existing-palaute))
          (heratepalvelu/delete-jakso-herate! existing-palaute)
          (throw ex))

      ;; FIXME: tapahtuma
      (throw ex))))

(defn build-ctx
  "Creates a full information context (i.e. background information)
  for a given palaute."
  [palaute]
  (let [hoks (hoks/get-by-id (:hoks-id palaute))
        jakso (some->>
                (:jakson-yksiloiva-tunniste palaute)
                (oht/osaamisen-hankkimistapa-by-yksiloiva-tunniste hoks))]
    (enrich-ctx! {:hoks hoks :jakso jakso :existing-palaute palaute
                  :tapahtumatyyppi :arvo-luonti})))

(defn make-kysely-type
  "Map DB-level kyselytyyppi back into what is expected by
  initial-palaute-state-and-reason"
  [palaute]
  (-> (:kyselytyyppi palaute)
      ({"aloittaneet" :aloituskysely
        "valmistuneet" :paattokysely
        "osia_suorittaneet" :paattokysely
        "tyopaikkajakson_suorittaneet" :ohjaajakysely})
      (or (throw (ex-info (str "Unknown kyselytyyppi: "
                               (:kyselytyyppi palaute))
                          {:palaute palaute})))))

(defn create-and-save-tunnus!
  "Create and save vastaajatunnus for given palaute context."
  [ctx {:keys [arvo-builder arvo-caller] :as handlers}]
  (let [request-id (str (UUID/randomUUID))
        arvo-req (arvo-builder ctx request-id)
        response
        (try (arvo-caller arvo-req)
             (catch ExceptionInfo e
               (throw (ex-info "Arvo call failed"
                               {:type ::arvo-kutsu-epaonnistui :ctx ctx}
                               e))))
        tunnus (palaute/save-arvo-tunniste! ctx response)]
    (assoc ctx
           :arvo-tunnus tunnus
           :arvo-response response
           :request-id request-id)))

(defn palaute-check-call-arvo-save-and-sync!
  "Check that palaute is part of kohderyhmä and create and save
  vastaajatunnus if so, using the given functions for appropriately
  handling different amis- and tep-palaute."
  [{:keys [existing-palaute hoks jakso] :as ctx}
   {:keys [check-palaute heratepalvelu-builder heratepalvelu-caller
           extra-handlers] :as handlers}]
  (let [[state field reason]
        (check-palaute ctx (make-kysely-type existing-palaute))]
    (if (not= :odottaa-kasittelya state)
      (->> (select-keys (or hoks jakso) [field])
           (map-vals str)
           (palaute/update-tila! ctx "ei_laheteta" reason))
      (let [new-ctx (create-and-save-tunnus! ctx handlers)
            tunnus (:arvo-tunnus new-ctx)]
        (try
          (heratepalvelu-caller (heratepalvelu-builder new-ctx))
          (doseq [handler extra-handlers] (handler new-ctx))
          (catch ExceptionInfo e
            (throw (ex-info
                     (str "Failed to sync palaute " (:id existing-palaute)
                          " to Herätepalvelu")
                     (assoc (ex-data e) :arvo-tunnus tunnus)
                     e))))
        tunnus))))

(defn handle-palaute-waiting-for-heratepvm!
  "Check that palaute is part of kohderyhmä and create and save
  vastaajatunnus if so."
  [palaute]
  (jdbc/with-db-transaction
    [tx db/spec]
    (try
      (log/info "Creating vastaajatunnus for" (:kyselytyyppi palaute)
                "palaute" (:id palaute))
      (palaute-check-call-arvo-save-and-sync!
        (assoc (build-ctx palaute) :tx tx)
        (if (:jakson-yksiloiva-tunniste palaute) tep/handlers amis/handlers))
      (catch ExceptionInfo e
        (handle-exception
          {:existing-palaute palaute :tx tx :tapahtumatyyppi :arvo-luonti} e))
      (catch Exception e
        (log/warn e "Error processing palaute" palaute)
        (throw e)))))

(defn handle-palautteet-waiting-for-heratepvm!
  "Fetch all unhandled palautteet whose heratepvm has come, check that
  they are part of kohderyhmä now (on their handling date) and create
  and save vastaajatunnus if so."
  [kyselytyypit]
  (log/info "Creating vastaajatunnukset for kyselytyypit" kyselytyypit)
  (doall (map handle-palaute-waiting-for-heratepvm!
              (palaute/get-palautteet-waiting-for-vastaajatunnus!
                db/spec {:kyselytyypit kyselytyypit}))))

(defn create-and-save-arvo-kyselylinkki-for-all-needed!
  "Create kyselylinkki for palautteet whose herätepvm has come but
  which don't have a kyselylinkki yet."
  [_]
  (handle-palautteet-waiting-for-heratepvm!
    ["aloittaneet" "valmistuneet" "osia_suorittaneet"]))

(defn handle-all-palautteet-waiting-for-vastaajatunnus!
  "Creates vastaajatunnus for all herates that are waiting for processing,
  do not have vastaajatunnus and have heratepvm today or in the past."
  [_]
  (handle-palautteet-waiting-for-heratepvm! ["tyopaikkajakson_suorittaneet"]))
