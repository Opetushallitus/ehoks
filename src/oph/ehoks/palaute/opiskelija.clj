(ns oph.ehoks.palaute.opiskelija
  "A namespace for everything related to opiskelijapalaute"
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [medley.core :refer [find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]))

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

(defn- added?
  [key* current-hoks updated-hoks]
  (and (some? (get updated-hoks key*)) (nil? (get current-hoks key*))))

(defn- reason-for-not-initiating
  "Runs several checks against HOKS to determine if opiskelijapalautekysely
  should be initiated and if any of the checks fail, returns a reason
  (string) that describes why kysely cannot be initiated. Returns `nil`
  if there is no reason preventing kysely initiation."
  [kysely prev-hoks hoks opiskeluoikeus]
  (let [herate-date (get hoks (herate-date-basis kysely))]
    (cond
      (not herate-date)
      (format "`%s` has not been set (is `nil`)." (herate-date-basis kysely))

      (not (:osaamisen-hankkimisen-tarve hoks))
      "`osaamisen-hankkimisen-tarve` not set to `true` for given HOKS."

      (not-any? suoritus/ammatillinen? (:suoritukset opiskeluoikeus))
      (format "No ammatillinen suoritus in opiskeluoikeus `%s`."
              (:opiskeluoikeus-oid hoks))

      (c/tuva-related-hoks? hoks)
      (str "HOKS is either TUVA-HOKS or \"ammatillisen koulutuksen HOKS\" "
           "related to TUVA-HOKS.")

      (and (= kysely :paattokysely)
           (not (added? :osaamisen-saavuttamisen-pvm prev-hoks hoks)))
      "`osaamisen-saavuttamisen-pvm` has not yet been set for given HOKS."

      (not (palaute/valid-herate-date? herate-date))
      (format "Herate date `%s` is invalid." herate-date)

      (opiskeluoikeus/linked-to-another? opiskeluoikeus)
      (format "Opiskeluoikeus `%s` is linked to another opiskeluoikeus"
              (:opiskeluoikeus-oid hoks)))))

(defn initiate?
  "Returns `true` when opiskelijapalautekysely (`kysely` being `:aloituskysely`
  or `:paattokysely`) should be initiated. The function has two arities, one for
  HOKS creation and the other for HOKS update."
  [kysely prev-hoks hoks opiskeluoikeus] ; on HOKS creation, prev-hoks is nil
  {:pre [(#{:aloituskysely :paattokysely} kysely)]}
  (if-let [reason (reason-for-not-initiating
                    kysely prev-hoks hoks opiskeluoikeus)]
    (log/infof "Not sending %s for HOKS `%d`. %s"
               (name kysely) (:id hoks) reason)
    (not ; In case of log prints `nil` is returned, convert to `true`.
      (when (and (some? prev-hoks) (= kysely :aloituskysely))
        ; On following cases we log print when aloituskysely is initiated.
        (let [msg (format "Sending aloituskysely for HOKS `%s`. " hoks)]
          (cond
            (not (:osaamisen-hankkimisen-tarve prev-hoks))
            (log/info msg (str "`osaamisen-hankkimisen-tarve` updated from "
                               "`false` to `true`."))
            (added? :sahkoposti prev-hoks hoks)
            (log/info msg "`sahkoposti` has been added.")

            (added? :puhelinnumero prev-hoks hoks)
            (log/info msg "`puhelinnumero` has been added.")

            :else true)))))) ; will be converted to `false`.

(defn kyselytyyppi
  [kysely opiskeluoikeus]
  (case kysely
    :aloituskysely "aloittaneet"
    :paattokysely  (->> opiskeluoikeus
                        :suoritukset
                        (find-first suoritus/ammatillinen?)
                        suoritus/tyyppi
                        koski-suoritustyyppi->kyselytyyppi)))

(defn already-initiated?!
  "Returns `true` if aloituskysely or paattokysely with same oppija and
  koulutustoimija has already been initiated within the same rahoituskausi."
  [kysely hoks koulutustoimija tx]
  (let [rahoituskausi (->> (herate-date-basis kysely)
                           (get hoks)
                           palaute/rahoituskausi)]
    (->> (get-by-kyselytyyppi-oppija-and-koulutustoimija!
           tx
           {:kyselytyypit    (case kysely
                               :aloituskysely ["aloittaneet"]
                               :paattokysely  (vec paattokyselyt))
            :oppija-oid       (:oppija-oid hoks)
            :koulutustoimija  koulutustoimija})
         (map (comp palaute/rahoituskausi :heratepvm))
         (some #(= % rahoituskausi)))))

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
  ([kysely hoks opiskeluoikeus] (initiate! kysely hoks opiskeluoikeus nil))
  ([kysely hoks opiskeluoikeus opts]
    {:pre [(#{:aloituskysely :paattokysely} kysely)]}
    (let [koulutustoimija (palaute/koulutustoimija-oid! opiskeluoikeus)]
      (jdbc/with-db-transaction
        [tx db/spec]
        (if (and (not (:resend? opts))
                 (already-initiated?! kysely hoks koulutustoimija tx))
          (log/warnf
            "%s already exists for HOKS `%d`." (name kysely) (:id hoks))
          (let [kyselytyyppi (kyselytyyppi kysely opiskeluoikeus)
                suoritus     (find-first suoritus/ammatillinen?
                                         (:suoritukset opiskeluoikeus))
                heratepvm    (get hoks (herate-date-basis kysely))
                alkupvm      (palaute/vastaamisajan-alkupvm heratepvm)]
            (assert (some? kyselytyyppi))
            (log/info "Making" kysely "heräte for HOKS" (:id hoks))
            (if (:resend? opts)
              (log/info "Not putting in DB because :resend? is set")
              (insert! db/spec
                       {:hoks-id          (:id hoks)
                        :heratepvm         heratepvm
                        :kyselytyyppi      kyselytyyppi
                        :koulutustoimija   koulutustoimija
                        :voimassa-alkupvm  alkupvm
                        :voimassa-loppupvm (palaute/vastaamisajan-loppupvm
                                             heratepvm alkupvm)
                        :suorituskieli     (suoritus/kieli suoritus)
                        :toimipiste-oid    (palaute/toimipiste-oid! suoritus)
                        :tutkintonimike    (suoritus/tutkintonimike suoritus)
                        :hankintakoulutuksen-toteuttaja
                        (palaute/hankintakoulutuksen-toteuttaja! hoks)
                        :tutkintotunnus    (suoritus/tutkintotunnus suoritus)
                        :herate-source     "ehoks_update"}))
            ; Sending herate to AWS SQS (will be removed when Herätepalvelu
            ; migration is complete).
            (sqs/send-amis-palaute-message
              {:ehoks-id           (:id hoks)
               :kyselytyyppi       (translate-kyselytyyppi kyselytyyppi)
               :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
               :oppija-oid         (:oppija-oid hoks)
               :sahkoposti         (:sahkoposti hoks)
               :puhelinnumero      (:puhelinnumero hoks)
               :alkupvm            (str
                                     (get hoks
                                          (herate-date-basis kysely)))})))))))

(def kysely-kasittely-field-mapping
  {:aloituskysely :aloitusherate_kasitelty
   :paattokysely :paattoherate_kasitelty})

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
    (let [opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                           (:opiskeluoikeus-oid hoks))]
      (when (initiate? kysely prev-hoks hoks opiskeluoikeus)
        (let [amisherate-kasittelytila
              (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id!
                (:id hoks))]
          (db-hoks/update-amisherate-kasittelytilat!
            {:id (:id amisherate-kasittelytila)
             (kysely-kasittely-field-mapping kysely) false}))
        (initiate! kysely hoks opiskeluoikeus opts)))))

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
    (->> hoksit
         (filter #(initiate-if-needed! kysely nil % opts))
         count)))
