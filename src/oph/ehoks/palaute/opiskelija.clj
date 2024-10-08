(ns oph.ehoks.palaute.opiskelija
  "A namespace for everything related to opiskelijapalaute"
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first greatest]]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils.date :as date]))

(def kyselytyypit #{"aloittaneet" "valmistuneet" "osia_suorittaneet"})
(def paattokyselyt #{"valmistuneet" "osia_suorittaneet"})
(def herate-date-basis {:aloituskysely :ensikertainen-hyvaksyminen
                        :paattokysely  :osaamisen-saavuttamisen-pvm})

(def ^:private translate-kyselytyyppi
  "Translate kyselytyyppi name to the equivalent one used in Herätepalvelu,
  i.e., `lhs` is the one used in eHOKS and `rhs` is the one used Herätepalvelu.
  This should not be needed when eHOKS-Herätepalvelu integration is done."
  {"aloittaneet"       "aloittaneet"
   "valmistuneet"      "tutkinnon_suorittaneet"
   "osia_suorittaneet" "tutkinnon_osia_suorittaneet"})

(defn kuuluu-palautteen-kohderyhmaan?
  "Kuuluuko opiskeluoikeus palautteen kohderyhmään?  Tällä hetkellä
  vain katsoo, onko kyseessä TELMA-opiskeluoikeus, joka ei ole tutkintoon
  tähtäävä koulutus (ks. OY-4433).  Muita mahdollisia kriteereitä
  ovat tulevaisuudessa koulutuksen rahoitus ja muut kriteerit, joista
  voidaan katsoa, onko koulutus tutkintoon tähtäävä."
  [opiskeluoikeus]
  (every? (complement suoritus/telma?) (:suoritukset opiskeluoikeus)))

(defn initial-palaute-state-and-reason
  "Runs several checks against HOKS and opiskeluoikeus to determine if
  opiskelijapalautekysely should be initiated.  Returns the initial state
  of the palaute (or nil if it cannot be formed at all), the field the
  decision was based on, and the reason for picking that state."
  [{:keys [hoks opiskeluoikeus]} kysely existing-heratteet]
  (let [herate-basis (herate-date-basis kysely)]
    (or
      (palaute/initial-palaute-state-and-reason-if-not-kohderyhma
        herate-basis hoks opiskeluoikeus)
      (cond
        (palaute/already-initiated? existing-heratteet)
        [nil :id :jo-lahetetty]

        (not (:osaamisen-hankkimisen-tarve hoks))
        [:ei-laheteta :osaamisen-hankkimisen-tarve :ei-ole]

        (c/tuva-related-hoks? hoks)
        [:ei-laheteta :tuva-opiskeluoikeus-oid :tuva-opiskeluoikeus]

        :else
        [:odottaa-kasittelya herate-basis :hoks-tallennettu]))))

(def kysely-kasittely-field-mapping
  {:aloituskysely :aloitusherate_kasitelty
   :paattokysely :paattoherate_kasitelty})

(defn existing-heratteet!
  [{:keys [tx hoks koulutustoimija]} kysely]
  (let [rahoituskausi
        (palaute/rahoituskausi (get hoks (herate-date-basis kysely)))
        kyselytyypit (case kysely
                       :aloituskysely ["aloittaneet"]
                       :paattokysely  (vec paattokyselyt))
        params {:kyselytyypit     kyselytyypit
                :oppija-oid       (:oppija-oid hoks)
                :koulutustoimija  koulutustoimija}]
    (filter
      #(= rahoituskausi (palaute/rahoituskausi (:heratepvm %)))
      (palaute/get-by-kyselytyyppi-oppija-and-koulutustoimija! tx params))))

(defn initiate!
  "Initiates opiskelijapalautekysely (`:aloituskysely` or `:paattokysely`).
  Currently, stores kysely data to eHOKS DB `palautteet` table and also sends
  the herate to AWS SQS for Herätepalvelu to process. Returns `true` if kysely
  was successfully initiated, `nil` or `false` otherwise.

  Supported options in `opts`:
  `:resend?`  If set to true, don't check if kysely is already
              intiated, i.e., resend herate straight to Herätepalvelu. Also skip
              the insertion to eHOKS `palautteet` table. Without this flag, the
              functionality to resend heratteet to Herätepalvelu wouldn't work.
              This should be removed once Herätepalvelu functionality has been
              fully migrated to eHOKS."
  [{:keys [tx hoks opiskeluoikeus] :as ctx}
   kysely
   {:keys [initial-state] :or {initial-state :odottaa-kasittelya} :as options}]
  {:pre [(#{:aloituskysely :paattokysely} kysely)]}
  (let [target-kasittelytila (not= initial-state :odottaa-kasittelya)
        amisherate-kasittelytila
        (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id! (:id hoks))]
    (db-hoks/update-amisherate-kasittelytilat!
      tx {:id (:id amisherate-kasittelytila)
          (kysely-kasittely-field-mapping kysely) target-kasittelytila}))

  (let [heratepvm (get hoks (herate-date-basis kysely))]
    (palaute/upsert-from-data!
      ctx (merge options {:kysely kysely
                          :heratepvm heratepvm
                          :alkupvm (greatest heratepvm (date/now))}))

    (when (= :odottaa-kasittelya initial-state)
      (log/info "Making" kysely "heräte for HOKS" (:id hoks))
      (sqs/send-amis-palaute-message
        {:ehoks-id           (:id hoks)
         :kyselytyyppi       (translate-kyselytyyppi
                               (palaute/kyselytyyppi kysely opiskeluoikeus))
         :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
         :oppija-oid         (:oppija-oid hoks)
         :sahkoposti         (:sahkoposti hoks)
         :puhelinnumero      (:puhelinnumero hoks)
         :alkupvm            (str heratepvm)}))))

(defn initiate-if-needed!
  "Sends heräte data required for opiskelijapalautekysely (`:aloituskysely` or
  `:paattokysely`) to appropriate DynamoDB table of Herätepalvelu if no check is
  preventing the sending. Returns `true` if kysely was successfully sent.

  Supported options in `opts`:
  `:resend?`  If set to true, don't check if kysely is already
              intiated, i.e., resend herate straight to Herätepalvelu. Also skip
              the insertion to eHOKS `palautteet` table. Without this flag, the
              functionality to resend heratteet to Herätepalvelu wouldn't work.
              This should be removed once Herätepalvelu functionality has been
              fully migrated to eHOKS."
  ([ctx kysely] (initiate-if-needed! ctx kysely nil))
  ([{:keys [hoks opiskeluoikeus] :as ctx} kysely opts]
    (jdbc/with-db-transaction
      [tx db/spec]
      (let [ctx (assoc ctx
                       :tx              tx
                       :koulutustoimija (palaute/koulutustoimija-oid!
                                          opiskeluoikeus))
            existing-heratteet (existing-heratteet! ctx kysely)
            [init-state field reason]
            (initial-palaute-state-and-reason
              ctx kysely (when-not (:resend? opts) existing-heratteet))]
        (log/info "Initial state for" kysely "for HOKS" (:id hoks)
                  "will be" (or init-state :ei-luoda-ollenkaan)
                  "because of" reason "in" field)
        (when init-state
          (initiate! ctx
                     kysely
                     (assoc opts
                            :initial-state init-state
                            :reason reason
                            :other-info (select-keys hoks [field])
                            :existing-heratteet existing-heratteet)))
        init-state))))

(defn initiate-every-needed!
  "Effectively the same as running `initiate-if-needed!` for multiple HOKSes,
  but also returns a count of the number of kyselys initiated.

  Supported options in `opts`:
  `:resend?`  If set to true, don't check if kysely is already
              intiated, i.e., resend herate straight to Herätepalvelu. Also skip
              the insertion to eHOKS `palautteet` table. Without this flag, the
              functionality to resend heratteet to Herätepalvelu wouldn't work.
              This should be removed once Herätepalvelu functionality has been
              fully migrated to eHOKS."
  ([kysely hoksit] (initiate-every-needed! kysely hoksit nil))
  ([kysely hoksit opts]
    (count (filter #(= :odottaa-kasittelya
                       (initiate-if-needed!
                         {:hoks           %
                          :opiskeluoikeus (koski/get-opiskeluoikeus!
                                            (:opiskeluoikeus-oid %))}
                         kysely
                         opts))
                   hoksit))))
