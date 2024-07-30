(ns oph.ehoks.palaute.opiskelija
  "A namespace for everything related to opiskelijapalaute"
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [hugsql.core :as hugsql]
            [medley.core :refer [find-first greatest map-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils.date :as date]))

(hugsql/def-db-fns "oph/ehoks/db/sql/opiskelijapalaute.sql")

(def paattokyselyt #{"valmistuneet" "osia_suorittaneet"})
(def herate-date-basis {:aloituskysely :ensikertainen-hyvaksyminen
                        :paattokysely  :osaamisen-saavuttamisen-pvm})

(def ^:private koski-suoritustyyppi->kyselytyyppi
  {"ammatillinentutkinto"           "valmistuneet"
   "ammatillinentutkintoosittainen" "osia_suorittaneet"})

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

(def palaute-unhandled? (comp #{"odottaa_kasittelya" "ei_laheteta"} :tila))

(defn already-initiated?
  "Returns `true` if aloituskysely or paattokysely with same oppija and
  koulutustoimija has already been initiated within the same rahoituskausi."
  [kysely hoks existing-heratteet]
  (not-every? palaute-unhandled? existing-heratteet))

(defn- added?
  [field current-hoks updated-hoks]
  (and (some? current-hoks)
       (not (get current-hoks field))
       (some? (get updated-hoks field))))

(defn initial-palaute-state-and-reason
  "Runs several checks against HOKS and opiskeluoikeus to determine if
  opiskelijapalautekysely should be initiated.  Returns the initial state
  of the palaute (or nil if it cannot be formed at all), the field the
  decision was based on, and the reason for picking that state."
  [kysely prev-hoks hoks opiskeluoikeus existing-heratteet]
  (let [herate-basis (herate-date-basis kysely)
        herate-date (get hoks herate-basis)]
    (cond
      (not herate-date)
      [nil herate-basis :ei-ole]

      (not (palaute/valid-herate-date? herate-date))
      [:ei-laheteta herate-basis :eri-rahoituskaudella]

      (not (:osaamisen-hankkimisen-tarve hoks))
      [:ei-laheteta :osaamisen-hankkimisen-tarve :ei-ole]

      (not-any? suoritus/ammatillinen? (:suoritukset opiskeluoikeus))
      [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen]

      (c/tuva-related-hoks? hoks)
      [:ei-laheteta :tuva-opiskeluoikeus-oid :tuva-opiskeluoikeus]

      (opiskeluoikeus/is-tuva? opiskeluoikeus)
      [:ei-laheteta :opiskeluoikeus-oid :tuva-opiskeluoikeus]

      (opiskeluoikeus/linked-to-another? opiskeluoikeus)
      [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus]

      (already-initiated? kysely hoks existing-heratteet)
      [nil herate-basis :jo-lahetetty-talla-rahoituskaudella]

      (and (= kysely :aloituskysely)
           (added? :osaamisen-hankkimisen-tarve prev-hoks hoks))
      [:odottaa-kasittelya :osaamisen-hankkimisen-tarve :lisatty]

      (and (= kysely :aloituskysely) (added? :sahkoposti prev-hoks hoks))
      [:odottaa-kasittelya :sahkoposti :lisatty]

      (and (= kysely :aloituskysely) (added? :puhelinnumero prev-hoks hoks))
      [:odottaa-kasittelya :puhelinnumero :lisatty]

      (and (some? prev-hoks) (= kysely :aloituskysely))
      [:ei-laheteta nil :ei-muutosta]

      :else
      [:odottaa-kasittelya nil :hoks-tallennettu])))

(defn kyselytyyppi
  [kysely suoritus]
  (case kysely
    :aloituskysely "aloittaneet"
    :paattokysely  (-> suoritus
                       suoritus/tyyppi
                       koski-suoritustyyppi->kyselytyyppi
                       (or "valmistuneet"))))

(def kysely-kasittely-field-mapping
  {:aloituskysely :aloitusherate_kasitelty
   :paattokysely :paattoherate_kasitelty})

(defn existing-heratteet!
  [kysely hoks koulutustoimija tx]
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
      (get-by-kyselytyyppi-oppija-and-koulutustoimija! tx params))))

(defn insert-or-update-palaute!
  "Add new palaute in the database, or set the values of an already
  created palaute to correspond to the current values from HOKS."
  [tx palaute existing-heratteet reason other-info]
  (let [updateable-herate (find-first palaute-unhandled? existing-heratteet)
        db-handler (if (:id updateable-herate) update-palaute! insert-palaute!)
        result (db-handler tx (assoc palaute :id (:id updateable-herate)))
        palaute-id (:id result)]
    (insert-palaute-tapahtuma!
      tx
      {:palaute-id palaute-id
       :vanha-tila (or (:tila updateable-herate) (:tila palaute))
       :uusi-tila (:tila palaute)
       :tapahtumatyyppi "hoks_tallennus"
       :syy (db-ops/to-underscore-str (or reason :hoks-tallennettu))
       :lisatiedot (map-vals str other-info)})))

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
  [kysely hoks opiskeluoikeus koulutustoimija tx
   {:keys [initial-state existing-heratteet reason other-info]
    :or {initial-state :odottaa-kasittelya}}]
  {:pre [(#{:aloituskysely :paattokysely} kysely)]}

  (let [target-kasittelytila (not= initial-state :odottaa-kasittelya)
        amisherate-kasittelytila
        (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id! (:id hoks))]
    (db-hoks/update-amisherate-kasittelytilat!
      {:id (:id amisherate-kasittelytila)
       (kysely-kasittely-field-mapping kysely) target-kasittelytila}))

  (let [; this may be nil if this is an :ei-laheteta palaute for
        ; non-ammatillinen opiskeluoikeus
        suoritus     (find-first suoritus/ammatillinen?
                                 (:suoritukset opiskeluoikeus))
        kyselytyyppi (kyselytyyppi kysely suoritus)
        heratepvm    (get hoks (herate-date-basis kysely))
        alkupvm      (greatest heratepvm (date/now))]
    (log/info "Making" kysely "heräte for HOKS" (:id hoks))
    (insert-or-update-palaute!
      tx
      {:hoks-id          (:id hoks)
       :tila             (db-ops/to-underscore-str initial-state)
       :heratepvm        heratepvm
       :kyselytyyppi     kyselytyyppi
       :koulutustoimija  koulutustoimija
       :voimassa-alkupvm alkupvm
       :voimassa-loppupvm
       (palaute/vastaamisajan-loppupvm heratepvm alkupvm)
       :suorituskieli    (suoritus/kieli suoritus)
       :toimipiste-oid   (palaute/toimipiste-oid! suoritus)
       :tutkintonimike   (suoritus/tutkintonimike suoritus)
       :hankintakoulutuksen-toteuttaja
       (palaute/hankintakoulutuksen-toteuttaja! hoks)
       :tutkintotunnus   (suoritus/tutkintotunnus suoritus)
       :herate-source    "ehoks_update"}
      existing-heratteet reason other-info)

    (when (= :odottaa-kasittelya initial-state)
      (sqs/send-amis-palaute-message
        {:ehoks-id           (:id hoks)
         :kyselytyyppi       (translate-kyselytyyppi kyselytyyppi)
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
  ([kysely prev-hoks hoks] (initiate-if-needed! kysely prev-hoks hoks nil))
  ([kysely prev-hoks hoks opts]
    (jdbc/with-db-transaction
      [tx db/spec]
      (let [opiskeluoikeus
            (koski/get-existing-opiskeluoikeus! (:opiskeluoikeus-oid hoks))
            koulutustoimija
            (palaute/koulutustoimija-oid! opiskeluoikeus)
            existing-heratteet
            (existing-heratteet! kysely hoks koulutustoimija tx)
            [init-state field reason]
            (initial-palaute-state-and-reason
              kysely prev-hoks hoks opiskeluoikeus
              (when-not (:resend? opts) existing-heratteet))]
        (log/info "Initial state for" kysely "for HOKS" (:id hoks)
                  "will be" (or init-state :ei-luoda-ollenkaan)
                  "because of" reason "in" field)
        (when init-state
          (initiate! kysely hoks opiskeluoikeus koulutustoimija tx
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
                       (initiate-if-needed! kysely nil % opts)) hoksit))))
