(ns oph.ehoks.palaute.opiskelija
  "A namespace for everything related to opiskelijapalaute"
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first greatest]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as dynamodb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as palautetapahtuma]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date])
  (:import (clojure.lang ExceptionInfo)
           (java.util UUID)))

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

(defn initial-palaute-state-and-reason
  "Runs several checks against HOKS and opiskeluoikeus to determine if
  opiskelijapalautekysely should be initiated.  Returns the initial state
  of the palaute (or nil if it cannot be formed at all), the field the
  decision was based on, and the reason for picking that state."
  [{:keys [hoks opiskeluoikeus] :as ctx} kysely-type existing-heratteet]
  (let [herate-basis (herate-date-basis kysely-type)]
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
  [tx {:keys [hoks koulutustoimija] :as ctx} kysely-type]
  (let [rahoituskausi (palaute/rahoituskausi
                        (get hoks (herate-date-basis kysely-type)))
        kyselytyypit  (case kysely-type
                        :aloituskysely ["aloittaneet"]
                        :paattokysely  (vec paattokyselyt))
        params        {:kyselytyypit     kyselytyypit
                       :oppija-oid       (:oppija-oid hoks)
                       :koulutustoimija  koulutustoimija}]
    (->>
      (palaute/get-by-kyselytyyppi-oppija-and-koulutustoimija! tx params)
      (vec)
      (log/spyf :info "existing-heratteet!: before rk filtering: %s")
      (filterv #(= rahoituskausi (palaute/rahoituskausi (:heratepvm %))))
      (log/spyf :info "existing-heratteet!: after rk filtering: %s"))))

(defn build!
  "Builds opiskelijapalaute to be inserted to DB. Uses `palaute/build!` to build
  an initial `palaute` map, then `assoc`s opiskelijapalaute specific values to
  that."
  [{:keys [hoks opiskeluoikeus] :as ctx} tyyppi tila existing-palaute]
  {:pre [(some? tila)
         (or (nil? existing-palaute) (palaute/unhandled? existing-palaute))]}
  (let [heratepvm (get hoks (herate-date-basis tyyppi))
        alkupvm   (greatest heratepvm (date/now))]
    (assoc (palaute/build! ctx tila existing-palaute)
           :kyselytyyppi      (palaute/kyselytyyppi tyyppi opiskeluoikeus)
           :heratepvm         heratepvm
           :voimassa-alkupvm  alkupvm
           :voimassa-loppupvm (palaute/vastaamisajan-loppupvm
                                heratepvm alkupvm))))

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
  ([ctx kysely-type] (initiate-if-needed! ctx kysely-type nil))
  ([{:keys [hoks opiskeluoikeus] :as ctx} kysely-type opts]
    (jdbc/with-db-transaction
      [tx db/spec]
      (let [ctx (assoc ctx :koulutustoimija (palaute/koulutustoimija-oid!
                                              opiskeluoikeus))
            existing-heratteet (when-not (:resend? opts)
                                 (existing-heratteet! tx ctx kysely-type))
            [state field reason] (initial-palaute-state-and-reason
                                   ctx kysely-type existing-heratteet)]
        (log/info "Initial state for" kysely-type "for HOKS" (:id hoks)
                  "will be" (or state :ei-luoda-ollenkaan)
                  "because of" reason "in" field)
        (when state
          (let [heratepvm       (get hoks (herate-date-basis kysely-type))
                existing-herate (find-first palaute/unhandled?
                                            existing-heratteet)
                target-kasittelytila (not= state :odottaa-kasittelya)
                amisherate-kasittelytila
                (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id!
                  (:id hoks))]
            (db-hoks/update-amisherate-kasittelytilat!
              tx {:id (:id amisherate-kasittelytila)
                  (kysely-kasittely-field-mapping kysely-type)
                  target-kasittelytila})
            (->> (build! ctx kysely-type state existing-herate)
                 (palaute/upsert! tx)
                 (palautetapahtuma/build ctx state field reason existing-herate)
                 (palautetapahtuma/insert! tx))
            (when (= :odottaa-kasittelya state)
              (log/info "Making" type "heräte for HOKS" (:id hoks))
              (sqs/send-amis-palaute-message
                {:ehoks-id           (:id hoks)
                 :kyselytyyppi       (translate-kyselytyyppi
                                       (palaute/kyselytyyppi
                                         kysely-type opiskeluoikeus))
                 :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
                 :oppija-oid         (:oppija-oid hoks)
                 :sahkoposti         (:sahkoposti hoks)
                 :puhelinnumero      (:puhelinnumero hoks)
                 :alkupvm            (str heratepvm)}))))
        state))))

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
  ([kysely-type hoksit] (initiate-every-needed! kysely-type hoksit nil))
  ([kysely-type hoksit opts]
    (count (filter #(= :odottaa-kasittelya
                       (initiate-if-needed!
                         {:hoks           %
                          :opiskeluoikeus (koski/get-opiskeluoikeus!
                                            (:opiskeluoikeus-oid %))}
                         kysely-type
                         opts))
                   hoksit))))

(defn create-arvo-kyselylinkki!
  "For the given palaute, make Arvo call for creating its kyselylinkki
  and return the Arvo reply."
  [palaute]
  (-> palaute
      (select-keys [:hoks-id :kyselytyyppi])
      (->> (palaute/get-for-arvo-by-hoks-id-and-kyselytyyppi! db/spec))
      (or (throw (ex-info "No Arvo-processable palaute found"
                          {:palaute palaute})))
      (utils/to-underscore-keys)
      ;; FIXME: add up-to-date values from Koski here
      (assoc :metatiedot {:tila "ei_lahetetty"}
             :request_id (str (UUID/randomUUID)))
      (#(assoc % :tutkinnonosat_hankkimistavoittain
               {:oppisopimus (:tutkinnonosat_oppisopimus %)
                :koulutussopimus (:tutkinnonosat_koulutussopimus %)
                :oppilaitosmuotoinenkoulutus
                (:tutkinnonosat_oppilaitosmuotoinenkoulutus %)}))
      (update :tutkinnon_suorituskieli #(or % "fi"))
      (update :koulutustoimija_oid #(or % ""))
      (update :tutkintotunnus str)
      (update :kyselyn_tyyppi translate-kyselytyyppi)
      (dissoc :tila :tutkinnonosat_koulutussopimus :tutkinnonosat_oppisopimus
              :tutkinnonosat_oppilaitosmuotoinenkoulutus)
      (arvo/create-kyselytunnus!)))

(defn create-and-save-arvo-kyselylinkki!
  "Update given palaute with a kyselylinkki from Arvo."
  [palaute]
  (try
    (jdbc/with-db-transaction
      [tx db/spec]
      (let [response (create-arvo-kyselylinkki! palaute)
            for-db (rename-keys response {:kysely_linkki :url})]
        (try
          (palaute/save-arvo-tunniste! tx palaute for-db
                                       {:arvo_response response})
          (catch Exception e
            (log/error "error while saving arvo tunniste" (:tunnus response)
                       "; trying to delete kyselylinkki")
            ;; FIXME: create chained exception if this throws
            (arvo/delete-kyselytunnus (:tunnus response))
            (log/info "successfully deleted kyselylinkki" (:tunnus response))
            (throw e)))))
    (catch Exception e
      (log/error e "while processing palaute" palaute)
      (palautetapahtuma/insert!
        db/spec
        {:palaute-id      (:id palaute)
         :vanha-tila      (:tila palaute)
         :uusi-tila       (:tila palaute)
         :tapahtumatyyppi "arvo_luonti"
         :syy             "arvo_kutsu_epaonnistui"
         :lisatiedot      {:errormsg (.getMessage e)
                           :body (:body (ex-data e))}})
      (throw e)))
  (dynamodb/sync-amis-herate! (:hoks-id palaute) (:kyselytyyppi palaute)))

(defn create-and-save-arvo-kyselylinkki-for-all-needed!
  "Create kyselylinkki for palautteet whose herätepvm has come but
  which don't have a kyselylinkki yet."
  [_]
  (if-not (contains? (set (:arvo-responsibilities config)) :create-kyselytunnus)
    (log/warn "`create-and-save-arvo-kyselylinkki-for-all-needed!` configured"
              "not to do anything")
    (do (log/info "Creating kyselylinkki for unprocessed amispalaute.")
        (doseq [palaute (palaute/get-amis-palautteet-waiting-for-kyselylinkki!
                          db/spec {:heratepvm (date/now)})]
          (try (log/infof "Creating kyselylinkki for %d" (:id palaute))
               (create-and-save-arvo-kyselylinkki! palaute)
               (catch ExceptionInfo e
                 (log/errorf e "Error processing amispalaute %s" palaute)))))))
