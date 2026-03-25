(ns oph.ehoks.palaute.vastaajatunnus
  (:require [clojure.set]
            [clojure.tools.logging :as log]
            [medley.core :refer [map-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.handling :as handling]
            [oph.ehoks.palaute.lahetys :as lahetys]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.utils :as utils])
  (:import (clojure.lang ExceptionInfo)))

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
   {:keys [arvo-builder arvo-caller]}]
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
   {:keys [heratepalvelu-builder heratepalvelu-caller extra-handlers]}]
  (log/info "Replicating palaute" (:id existing-palaute) "to herätepalvelu")
  (try
    (heratepalvelu-caller (heratepalvelu-builder ctx) :after-arvo-call)
    (doseq [handler extra-handlers] (handler ctx :after-arvo-call))
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
        (check-palaute ctx (make-kysely-type existing-palaute))
        lisatiedot (map-vals str (select-keys (merge jakso hoks) [field]))]
    (log/info "Requested state for palaute" (:id existing-palaute)
              "of HOKS" (:id hoks) "is" (or state :ei-kasitella)
              "because of" reason "in" field)
    (if state
      (if (= :odottaa-kasittelya state)
        (-> ctx
            (create-and-save-tunnus! handlers)
            (sync-to-heratepalvelu! handlers))
        (palaute/update-tila! ctx state reason lisatiedot))
      (tapahtuma/build-and-insert! ctx reason lisatiedot))))

(defn create-vastaajatunnus!
  "Check that palaute is part of kohderyhmä and create and save
  vastaajatunnus if so."
  [palaute]
  (log/info "Creating vastaajatunnus for" (:kyselytyyppi palaute)
            "palaute" (:id palaute))
  (handling/call-with-context-and-error-handling
    :arvo-luonti palaute-check-call-arvo-save-and-sync! palaute))

(defn handle-palaute-waiting-for-heratepvm!
  "Do all actions that need to be done to palaute when its heratepvm comes."
  [palaute]
  (let [vastaajatunnus (create-vastaajatunnus! palaute)
        ;; this needs to have hankkimistapa_id too when työelämäpalaute
        ;; support is added
        updated-palaute (palaute/get-by-id! db/spec {:id (:id palaute)})]
    ;; if creation succeeded, also send messages immediately (with fresh caches)
    (if (contains? #{"kysely_muodostettu" "vastaajatunnus_muodostettu"}
                   (:tila updated-palaute))
      (lahetys/handle-unsent-palaute! updated-palaute)
      (log/info "Palaute" (:id updated-palaute) "is in state"
                (:tila updated-palaute) "so not proceeding to sending"))
    vastaajatunnus))

(defn handle-palautteet-waiting-for-heratepvm!
  "Fetch all unhandled palautteet whose heratepvm has come, check that
  they are part of kohderyhmä now (on their handling date) and create
  and save vastaajatunnus if so."
  [kyselytyypit]
  (log/info "Creating vastaajatunnukset for kyselytyypit" kyselytyypit)
  (doall (map handle-palaute-waiting-for-heratepvm!
              (palaute/get-palautteet-waiting-for-vastaajatunnus!
                db/spec {:kyselytyypit kyselytyypit}))))

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
