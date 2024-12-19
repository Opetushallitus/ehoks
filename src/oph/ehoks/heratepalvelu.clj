(ns oph.ehoks.heratepalvelu
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.palaute.tyoelama.nippu :as nippu]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(defn send-workplace-periods!
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (doseq [period periods]
    (sqs/send-tyoelamapalaute-message! (sqs/build-tyoelamapalaute-msg period))))

(defn get-oppija-kyselylinkit
  "Returns all feedback links for oppija"
  [oppija-oid]
  (filter
    some?
    (map #(if-not (:vastattu %1)
            (when-let [status (arvo/get-kyselylinkki-status-catch-404
                                (:kyselylinkki %1))]
              (let [loppupvm (LocalDate/parse
                               (first
                                 (string/split (:voimassa_loppupvm status)
                                               #"T")))]
                (kyselylinkki/update! {:kyselylinkki (:kyselylinkki %1)
                                       :voimassa_loppupvm loppupvm
                                       :vastattu (:vastattu status)})
                (assoc %1
                       :voimassa-loppupvm loppupvm
                       :vastattu (:vastattu status))))
            %1)
         (kyselylinkki/get-by-oppija-oid! oppija-oid))))

(defn set-tep-kasitelty
  "Marks an osaamisen hankkimistapa as handled (käsitelty)."
  [hankkimistapa-id to]
  (db-hoks/update-osaamisen-hankkimistapa-tep-kasitelty hankkimistapa-id to))

(defn process-hoksit-without-kyselylinkit
  "Finds all HOKSit for which kyselylinkit haven't been created and sends them
  to the SQS queue"
  [start end limit]
  (let [aloittaneet
        (db-hoks/select-hoksit-with-kasittelemattomat-aloitusheratteet
          start end limit)
        paattyneet
        (db-hoks/select-hoksit-with-kasittelemattomat-paattoheratteet
          start end limit)]
    (log/infof
      "Sending %d (limit %d) hoksit between %s and %s"
      (+ (count aloittaneet) (count paattyneet)) (* 2 limit) start end)
    (op/initiate-every-needed! :aloituskysely aloittaneet {:resend? true})
    (op/initiate-every-needed! :paattokysely  paattyneet {:resend? true})
    (concat aloittaneet paattyneet)))

(defn set-aloitusherate-kasitelty
  "Marks aloitusheräte handled (käsitelty) for a given HOKS."
  [hoks-id to]
  (db-hoks/update-amisherate-kasittelytilat-aloitusherate-kasitelty hoks-id to))

(defn set-paattoherate-kasitelty
  "Marks päättöheräte handled (käsitelty) for a given HOKS."
  [hoks-id to]
  (db-hoks/update-amisherate-kasittelytilat-paattoherate-kasitelty hoks-id to))

(defn select-tyoelamajaksot-active-between
  "Finds all workplace periods active between start and end dates for student
  oppija."
  [oppija start end]
  (concat
    (db-hoks/select-tyoelamajaksot-active-between "hato" oppija start end)
    (db-hoks/select-tyoelamajaksot-active-between "hpto" oppija start end)
    (db-hoks/select-tyoelamajaksot-active-between "hyto" oppija start end)))

(defn- add-keys
  [palaute {:keys [opiskeluoikeus niputuspvm vastaamisajan-alkupvm] :as ctx}
   request-id tunnus]
  (let [niputuspvm      niputuspvm
        koulutustoimija (palaute/koulutustoimija-oid! opiskeluoikeus)
        oo-suoritus     (find-first suoritus/ammatillinen?
                                    (:suoritukset opiskeluoikeus))
        tutkinto        (get-in oo-suoritus
                                [:koulutusmoduuli :tunniste :koodiarvo])]
    (assoc palaute
           :tallennuspvm (date/now)
           :alkupvm vastaamisajan-alkupvm
           :koulutustoimija koulutustoimija
           :niputuspvm niputuspvm
           :ohjaaja-ytunnus-kj-tutkinto (nippu/tunniste ctx palaute)
           :oppilaitos (:oid (:oppilaitos opiskeluoikeus))
           :osaamisala (str (seq (suoritus/get-osaamisalat
                                   oo-suoritus (:oid opiskeluoikeus)
                                   (:heratepvm palaute))))
           :request-id request-id
           :toimipiste-oid (str (palaute/toimipiste-oid! oo-suoritus))
           :tpk-niputuspvm "ei_maaritelty"
           :tunnus tunnus
           :tutkinto tutkinto
           :tutkintonimike (str (seq (map :koodiarvo
                                          (:tutkintonimike oo-suoritus))))
           :tyopaikan-normalisoitu-nimi (utils/normalize-string
                                          (:tyopaikan-nimi palaute))
           :viimeinen-vastauspvm
           (str (.plusDays ^LocalDate vastaamisajan-alkupvm 60)))))

; Helper functions that can be mocked in tests
(def sync-jakso!*     (partial ddb/sync-item! :jakso))
(def sync-tpo-nippu!* (partial ddb/sync-item! :nippu))

;; FIXME: tältä puuttuu yksikkötesti.
;; test-create-and-save-arvo-vastaajatunnus-for-all-needed! sisältää
;; ylimalkaisen testin tälle funktiolle.
(defn sync-jakso!
  "Update the herätepalvelu jaksotunnustable to have the same content
  for given heräte as palaute-backend has in its own database.
  sync-jakso-herate! only updates fields it 'owns': currently that
  means that the messaging tracking fields are left intact (because
  herätepalvelu will update those)."
  [{:keys [existing-palaute tx] :as ctx} request-id tunnus]
  {:pre [(some? tunnus)]}
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-jakso-heratteet)
    (log/warn "sync-jakso!: configured to not do anything")
    (let [query (select-keys existing-palaute
                             [:jakson-yksiloiva-tunniste :hoks-id])]
      (try (-> (palaute/get-for-heratepalvelu-by-hoks-id-and-yksiloiva-tunniste!
                 tx query)
               (first)
               (not-empty)
               (or (throw (ex-info "palaute not found" query)))
               (add-keys ctx request-id tunnus)
               (dissoc :internal-kyselytyyppi :jakson-yksiloiva-tunniste)
               (utils/remove-nils)
               utils/to-underscore-keys
               ;; the only field that has dashes in its name is tpk-niputuspvm
               (rename-keys {:tpk_niputuspvm :tpk-niputuspvm})
               (sync-jakso!*))
           (catch Exception e
             (throw (ex-info (format (str "Failed to sync jakso `%s` of HOKS "
                                          "`%d` to Herätepalvelu")
                                     (:jakson-yksiloiva-tunniste query)
                                     (:hoks-id query))
                             {:type        ::jakso-sync-failed
                              :arvo-tunnus tunnus}
                             e)))))))

(defn sync-tpo-nippu!
  "Update the Herätepalvelu nipputable to have the same content for given heräte
  as palaute-backend has in its own database."
  [nippu tunnus]
  {:pre [(:koulutuksenjarjestaja nippu) (:niputuspvm nippu) (:tutkinto nippu)]}
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-jakso-heratteet)
    (log/warn "sync-tpo-nippu!: configured to not do anything")
    (let [tunnisteet (select-keys nippu [:ohjaaja_ytunnus_kj_tutkinto
                                         :niputuspvm])]
      (try
        (if (ddb/get-item! :nippu tunnisteet)
          (log/infof "Nippu `%s` already exists" tunnisteet)
          (sync-tpo-nippu!* nippu))
        (catch Exception e
          (throw (ex-info (format (str "Failed to sync TPO-nippu with "
                                       "tunnisteet %s to Herätepalvelu")
                                  tunnisteet)
                          {:type        ::tpo-nippu-sync-failed
                           :arvo-tunnus tunnus}
                          e)))))))

(defn delete-jakso-herate!
  [tep-palaute]
  (ddb/delete-item! :jakso {:hankkimistapa_id (:hankkimistapa-id tep-palaute)}))
