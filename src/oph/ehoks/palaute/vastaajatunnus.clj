(ns oph.ehoks.palaute.vastaajatunnus
  (:require [medley.core :refer [find-first]]
            [clojure.walk :refer [walk]]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.db :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute.opiskelija :as amis]
            [oph.ehoks.palaute.tyoelama :as tep])
  (:import (clojure.lang ExceptionInfo)))

(defn- enrich-ctx!
  "Lisää muista palveluista saatavia tietoja kontekstiin myöhempää
  käsittelyä varten."
  [{:keys [hoks] :as ctx}]
  (let [opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                         (:opiskeluoikeus-oid hoks))
        suoritus (find-first suoritus/ammatillinen?
                             (:suoritukset opiskeluoikeus))]
    (assoc ctx
           :opiskeluoikeus        opiskeluoikeus
           :suoritus              suoritus
           :koulutustoimija       (palaute/koulutustoimija-oid! opiskeluoikeus)
           :toimipiste            (palaute/toimipiste-oid! suoritus))))

(defn- handle-exception
  "Handles an ExceptionInfo `ex` based on `:type` in `ex-data`. If `ex` doesn't
  match any of the cases below, re-throws `ex`."
  [{:keys [existing-palaute] :as ctx} ex]
  (let [ex-data (ex-data ex)
        ex-type (:type ex-data)
        tunnus  (:arvo-tunnus ex-data)]
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
                  (select-keys ex-data [:keskeytymisajanjaksot]))))

      ::arvo/no-jaksotunnus-created
      (log/logf (if (:configured-to-call-arvo? ex-data) :error :warn)
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

(defn handle-palaute-waiting-for-heratepvm!
  "Check that palaute is part of kohderyhmä and create and save
  vastaajatunnus if so."
  [palaute]
  (jdbc/with-db-transaction
    [tx db/spec]
    (try
      (log/info "Creating vastaajatunnus for" (:kyselytyyppi palaute)
                "palaute" (:id palaute))
      (let [handler (if (:jakson-yksiloiva-tunniste palaute)
                      tep/handle-palaute-waiting-for-vastaajatunnus!
                      amis/create-and-save-arvo-kyselylinkki!)
            ctx (assoc (build-ctx palaute) :tx tx)]
        (handler ctx))
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
