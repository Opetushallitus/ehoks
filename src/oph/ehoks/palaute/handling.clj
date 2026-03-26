(ns oph.ehoks.palaute.handling
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as amis]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date])
  (:import (clojure.lang ExceptionInfo)
           (java.util UUID)))

(defn palaute-ddb-record
  "Hakee DynamoDB:stä palautetta vastaavan tietueen."
  [palaute hoks koulutustoimija]
  (if (:jakson-yksiloiva-tunniste palaute)
    (ddb/get-jakso-by-hoks-id-and-yksiloiva-tunniste!
      (:id hoks) (:jakson-yksiloiva-tunniste palaute))
    (ddb/get-item!
      :amis {:toimija_oppija (str koulutustoimija "/" (:oppija-oid hoks))
             :tyyppi_kausi
             (str (amis/translate-kyselytyyppi (:kyselytyyppi palaute))
                  "/" (palaute/rahoituskausi (:heratepvm palaute)))})))

(defn- enrich-ctx!
  "Lisää muista palveluista saatavia tietoja kontekstiin myöhempää
  käsittelyä varten."
  [{:keys [hoks existing-palaute] :as ctx}]
  (let [opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                         (:opiskeluoikeus-oid hoks))
        suoritus (find-first suoritus/ammatillinen?
                             (:suoritukset opiskeluoikeus))
        koulutustoimija (palaute/koulutustoimija-oid! opiskeluoikeus)]
    (assoc ctx
           :existing-ddb-herate
           (delay (palaute-ddb-record existing-palaute hoks koulutustoimija))
           :arvo-status
           ;; needs more logic when tep-palaute may also be queried
           (delay (some-> (:kyselylinkki existing-palaute)
                          (arvo/get-kyselylinkki-status!)))
           :niputuspvm            (tep/next-niputus-date (date/now))
           :vastaamisajan-alkupvm (tep/next-niputus-date
                                    (:heratepvm existing-palaute))
           :opiskeluoikeus opiskeluoikeus
           :suoritus suoritus
           :hk-toteuttaja
           (delay (palaute/hankintakoulutuksen-toteuttaja! hoks))
           :koulutustoimija koulutustoimija
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
    (log/info ex "Handling exception in tunnus handling")
    (when (and tunnus (not (.startsWith ^String tunnus "<dummy-")))
      (try
        (log/infof "Trying to delete vastaajatunnus `%s` from Arvo" tunnus)
        (tunnus-cleanup-handler tunnus)
        (catch Exception e
          (log/error e "While trying to clean up tunnus from Arvo"))))
    (case ex-type
      (::koski/opiskeluoikeus-not-found
        ::hoks/invalid-data
        :oph.ehoks.palaute.vastaajatunnus/arvossa-ei-kyselya)
      (do (log/warnf "%s. Setting `tila` to `ei_laheteta` for palaute `%d`."
                     (ex-message ex) (:id existing-palaute))
          (palaute/update-tila!
            ctx "ei_laheteta" ex-type
            {:opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
             :heratepvm (str (:heratepvm existing-palaute))}))

      (:oph.ehoks.palaute.vastaajatunnus/arvo-kutsu-epaonnistui
        :oph.ehoks.palaute.vastaajatunnus/tunnus-tallennus-epaonnistui)
      (let [cause (ex-cause ex)]
        (log/error "Error" ex-type "because of" (ex-message cause)
                   "with error body" (:body (ex-data cause))
                   "for palaute" (:id existing-palaute))
        (tapahtuma/build-and-insert!
          ctx ex-type {:errormsg (ex-message cause)
                       :body     (:body (ex-data cause))}))

      :oph.ehoks.palaute.vastaajatunnus/heratepalvelu-sync-epaonnistui
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

(def hoks-cache-amount
  "How many HOKSes we cache.  Palautteet from
  palaute/get-palautteet-waiting-for-vastaajatunnus! and
  palaute/get-unsent-palautteet! are ordered by hoks-id, so 1 should
  suffice."
  2)

(def hoks-cache-time
  "Handling a palaute should not take longer than 15 seconds"
  15000)

(def get-hoks-by-id!
  (utils/with-fifo-ttl-cache
    hoks/get-by-id hoks-cache-time hoks-cache-amount {}))

(defn build-ctx!
  "Creates a full information context (i.e. background information)
  for a given palaute."
  [palaute]
  (let [hoks (get-hoks-by-id! (:hoks-id palaute))
        jakso (some->>
                (:jakson-yksiloiva-tunniste palaute)
                (oht/osaamisen-hankkimistapa-by-yksiloiva-tunniste hoks))]
    (enrich-ctx! {:hoks hoks :jakso jakso :existing-palaute palaute
                  :tapahtumatyyppi :arvo-luonti
                  :request-id (str (UUID/randomUUID))})))

(defn call-with-context-and-error-handling
  "Process one palaute with given handler, giving full context to the
  handler and processing any errors"
  [tapahtumatyyppi handler palaute]
  (try
    (jdbc/with-db-transaction
      [tx db/spec]
      (handler
        (assoc (build-ctx! palaute) :tapahtumatyyppi tapahtumatyyppi :tx tx)
        (if (:jakson-yksiloiva-tunniste palaute) tep/handlers amis/handlers)))
    (catch ExceptionInfo e
      (jdbc/with-db-transaction
        [tx db/spec]
        (-> (:ctx (ex-data e))
            (or {:existing-palaute palaute})  ; poor person's context
            (assoc :tapahtumatyyppi tapahtumatyyppi :tx tx)
            (handle-exception e)))
      nil) ; handler failed, nothing created
    (catch Exception e
      (log/error e "Unknown error processing palaute" palaute))))
