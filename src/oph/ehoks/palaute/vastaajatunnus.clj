(ns oph.ehoks.palaute.vastaajatunnus
  (:require [medley.core :refer [find-first map-vals]]
            [clojure.walk :refer [walk]]
            [clojure.set]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.db :as db]
            [oph.ehoks.utils :as utils]
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
  [{:keys [existing-palaute hoks] :as ctx} ex]
  (let [ex-params (ex-data ex)
        ex-type (:type ex-params)
        tunnus  (:arvo-tunnus ex-params)
        tunnus-cleanup-handler
        (if (:jakson-yksiloiva-tunniste existing-palaute)
          arvo/delete-jaksotunnus arvo/delete-kyselytunnus)]
    (log/infof ex "Handling exception in tunnus handling")
    (when (and tunnus (not (.startsWith ^String tunnus "<dummy-")))
      (try
        (log/infof "Trying to delete vastaajatunnus `%s` from Arvo" tunnus)
        (tunnus-cleanup-handler tunnus)
        (catch Exception e
          (log/error e "While trying to clean up tunnus from Arvo"))))
    (case ex-type
      (::koski/opiskeluoikeus-not-found ::arvossa-ei-kyselya)
      (do (log/warnf "%s. Setting `tila` to `ei_laheteta` for palaute `%d`."
                     (ex-message ex) (:id existing-palaute))
          (palaute/update-tila!
            ctx "ei_laheteta" ex-type
            {:opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
             :heratepvm (str (:heratepvm existing-palaute))}))

      (::arvo-kutsu-epaonnistui ::tunnus-tallennus-epaonnistui)
      (let [cause (ex-cause ex)]
        (log/error "Error" ex-type "because of" (ex-message cause)
                   "with error body" (:body (ex-data cause))
                   "for palaute" (:id existing-palaute))
        (tapahtuma/build-and-insert!
          ctx ex-type {:errormsg (ex-message cause)
                       :body     (:body (ex-data cause))}))

      ::heratepalvelu-sync-epaonnistui
      (let [ddb-ex (ex-cause ex)]
        (log/errorf (str "%s. Trying to delete jakso corresponding to "
                         "palaute `%d` from Herätepalvelu")
                    (ex-message ddb-ex) (:id existing-palaute))
        (tapahtuma/build-and-insert!
          ctx ex-type {:errormsg (ex-message ddb-ex)
                       :body     (ex-data ddb-ex)})
        (try
          (heratepalvelu/delete-jakso-herate! existing-palaute)
          (catch Exception e
            (log/error e "While cleaning up jakso from Herätepalvelu"))))

      (let [cause-ex (ex-cause ex)]
        (log/error "Unknown exception while processing palaute "
                   (:id existing-palaute))
        (tapahtuma/build-and-insert! ctx :tuntematon-virhe
                                     {:errormsg (ex-message ex)
                                      :causemsg (ex-message cause-ex)})))))

(defn build-ctx
  "Creates a full information context (i.e. background information)
  for a given palaute."
  [palaute]
  (let [hoks (hoks/get-by-id (:hoks-id palaute))
        jakso (some->>
                (:jakson-yksiloiva-tunniste palaute)
                (oht/osaamisen-hankkimistapa-by-yksiloiva-tunniste hoks))]
    (enrich-ctx! {:hoks hoks :jakso jakso :existing-palaute palaute
                  :tapahtumatyyppi :arvo-luonti
                  :request-id (str (UUID/randomUUID))})))

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

(defn save-arvo-tunniste!
  [{:keys [tx existing-palaute] :as ctx} arvo-response]
  {:pre [(:tunnus arvo-response)]}
  (log/info "Saving Arvo response" arvo-response
            "for palaute" (:id existing-palaute))
  (let [new-state (if (:jakson-yksiloiva-tunniste existing-palaute)
                    :vastaajatunnus-muodostettu :kysely-muodostettu)]
    (try (-> arvo-response
             (clojure.set/rename-keys {:kysely_linkki :url})
             (assoc :id   (:id existing-palaute)
                    :tila (utils/to-underscore-str new-state))
             (update :url identity)  ; ensure key exists
             (->> (palaute/update-arvo-tunniste! tx))
             (assert))
         (tapahtuma/build-and-insert!
           ctx new-state :arvo-kutsu-onnistui {:arvo_response arvo-response})
         (catch Exception e
           (log/error "Postgres error:" (ex-message e))
           (throw (ex-info "Failed to save Arvo-tunnus to DB"
                           {:type        ::tunnus-tallennus-epaonnistui
                            :ctx         ctx
                            :arvo-tunnus (:tunnus arvo-response)}
                           e)))))
  (:tunnus arvo-response))

(defn create-and-save-tunnus!
  "Create and save vastaajatunnus for given palaute context."
  [{:keys [existing-palaute] :as ctx}
   {:keys [arvo-builder arvo-caller] :as handlers}]
  (log/info "Calling arvo for palaute" (:id existing-palaute)
            "of type" (:kyselytyyppi existing-palaute)
            "of HOKS" (:hoks-id existing-palaute))
  (let [arvo-req (arvo-builder ctx)
        response
        (try (arvo-caller arvo-req)
             (catch ExceptionInfo e
               (log/warn "Arvo call error:" (ex-message e) (:body (ex-data e))
                         "for request:" arvo-req)
               (if (and (= 404 (:status (ex-data e)))
                        (re-find #"\"ei-kyselya\"" (:body (ex-data e))))
                 (throw (ex-info "No kysely open for this oppilaitos"
                                 {:type ::arvossa-ei-kyselya :ctx ctx}
                                 e))
                 (throw (ex-info "Arvo call failed"
                                 {:type ::arvo-kutsu-epaonnistui :ctx ctx}
                                 e)))))
        tunnus (save-arvo-tunniste! ctx response)]
    (assoc ctx :arvo-tunnus tunnus :arvo-response response)))

(defn sync-to-heratepalvelu!
  "Replicate information about just formed vastaajatunnus to heratepalvelu."
  [{:keys [existing-palaute arvo-tunnus] :as ctx}
   {:keys [heratepalvelu-builder heratepalvelu-caller extra-handlers]
    :as handlers}]
  (log/info "Replicating palaute" (:id existing-palaute) "to herätepalvelu")
  (try
    (heratepalvelu-caller (heratepalvelu-builder ctx))
    (doseq [handler extra-handlers] (handler ctx))
    (catch Exception e
      (log/warn "Herätepalvelu sync error:" (ex-message e) (:body (ex-data e)))
      (throw (ex-info
               (str "Failed to sync palaute " (:id existing-palaute)
                    " to Herätepalvelu")
               (assoc (ex-data e)
                      :type ::heratepalvelu-sync-epaonnistui
                      :ctx ctx
                      :arvo-tunnus arvo-tunnus)
               e))))
  arvo-tunnus)

(defn palaute-check-call-arvo-save-and-sync!
  "Check that palaute is part of kohderyhmä and create and save
  vastaajatunnus if so, using the given functions for appropriately
  handling different amis- and tep-palaute."
  [{:keys [existing-palaute hoks jakso] :as ctx}
   {:keys [check-palaute] :as handlers}]
  (let [[state field reason]
        (check-palaute ctx (make-kysely-type existing-palaute))]
    (log/info "Requested state for palaute" (:id existing-palaute)
              "of HOKS" (:id hoks) "is" (or state :ei-kasitella)
              "because of" reason "in" field)
    (if (not= :odottaa-kasittelya state)
      (->> (select-keys (merge jakso hoks) [field])
           (map-vals str)
           (palaute/update-tila! ctx "ei_laheteta" reason))
      (-> ctx
          (create-and-save-tunnus! handlers)
          (sync-to-heratepalvelu! handlers)))))

(defn handle-palaute-waiting-for-heratepvm!
  "Check that palaute is part of kohderyhmä and create and save
  vastaajatunnus if so."
  [palaute]
  (try
    (jdbc/with-db-transaction
      [tx db/spec]
      (log/info "Creating vastaajatunnus for" (:kyselytyyppi palaute)
                "palaute" (:id palaute))
      (palaute-check-call-arvo-save-and-sync!
        (assoc (build-ctx palaute) :tx tx)
        (if (:jakson-yksiloiva-tunniste palaute) tep/handlers amis/handlers)))
    (catch ExceptionInfo e
      (jdbc/with-db-transaction
        [tx db/spec]
        (-> (:ctx (ex-data e))
            (or {:existing-palaute palaute})
            (assoc :tapahtumatyyppi :arvo-luonti :tx tx)
            (handle-exception e)))
      nil) ; no arvo-tunnus created
    (catch Exception e
      (log/error e "Unknown error processing palaute" palaute))))

(defn handle-palautteet-waiting-for-heratepvm!
  "Fetch all unhandled palautteet whose heratepvm has come, check that
  they are part of kohderyhmä now (on their handling date) and create
  and save vastaajatunnus if so."
  [kyselytyypit]
  (log/info "Creating vastaajatunnukset for kyselytyypit" kyselytyypit)
  (doall (map handle-palaute-waiting-for-heratepvm!
              (palaute/get-palautteet-waiting-for-vastaajatunnus!
                db/spec {:kyselytyypit kyselytyypit
                         :hoks-id nil :palaute-id nil}))))

(defn handle-amis-palautteet-on-heratepvm!
  "Create kyselylinkki for palautteet whose herätepvm has come but
  which don't have a kyselylinkki yet."
  [_]
  (handle-palautteet-waiting-for-heratepvm!
    ["aloittaneet" "valmistuneet" "osia_suorittaneet"]))

(defn handle-tep-palautteet-on-heratepvm!
  "Creates vastaajatunnus for all herates that are waiting for processing,
  do not have vastaajatunnus and have heratepvm today or in the past."
  [_]
  (handle-palautteet-waiting-for-heratepvm! ["tyopaikkajakson_suorittaneet"]))
