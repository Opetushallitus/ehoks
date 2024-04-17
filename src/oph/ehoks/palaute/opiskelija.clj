(ns oph.ehoks.palaute.opiskelija
  "A namespace for everything related to opiskelijapalaute"
  (:require [clojure.tools.logging :as log]
            [hugsql.core :as hugsql]
            [medley.core :refer [find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks.common :as c]))

(hugsql/def-db-fns "oph/ehoks/db/sql/opiskelijapalaute.sql")

(def paattokyselyt #{"valmistuneet" "osia_suorittaneet"})

(defn- suoritustyyppi [suoritus] (get-in suoritus [:tyyppi :koodiarvo]))

(defn- ammatillinen-suoritus?
  "Varmistaa, että suorituksen tyyppi on joko ammatillinen tutkinto tai
  osittainen ammatillinen tutkinto."
  [suoritus]
  (some? (#{"ammatillinentutkinto" "ammatillinentutkintoosittainen"}
           (suoritustyyppi suoritus))))

(def ^:private koski-suoritustyyppi->kyselytyyppi
  {"ammatillinentutkinto"           "valmistuneet"
   "ammatillinentutkintoosittainen" "osia_suorittaneet"})

(def ^:private translate-kyselytyyppi
  "Translate kyselytyyppi name to the equivalent one used in Herätepalvelu,
  i.e., `lhs` is the one used in eHOKS and `rhs` is the one used Herätepalvelu.
  This should not be needed when eHOKS-Herätepalvelu integration is done."
  {"valmistuneet"      "tutkinnon_suorittaneet"
   "osia_suorittaneet" "tutkinnon_osia_suorittaneet"})

(defn- telma-suoritus?
  "Tarkistaa, onko suorituksen tyyppi TELMA (työhön ja elämään valmentava)."
  [suoritus]
  (some? (#{"telma", "telmakoulutuksenosa"} (suoritustyyppi suoritus))))

(defn kuuluu-palautteen-kohderyhmaan?
  "Kuuluuko opiskeluoikeus palautteen kohderyhmään?  Tällä hetkellä
  vain katsoo, onko kyseessä TELMA-opiskeluoikeus, joka ei ole tutkintoon
  tähtäävä koulutus (ks. OY-4433).  Muita mahdollisia kriteereitä
  ovat tulevaisuudessa koulutuksen rahoitus ja muut kriteerit, joista
  voidaan katsoa, onko koulutus tutkintoon tähtäävä."
  [opiskeluoikeus]
  (every? (complement telma-suoritus?) (:suoritukset opiskeluoikeus)))

(defn- added?
  [key* current-hoks updated-hoks]
  (and (some? (get updated-hoks key*)) (nil? (get current-hoks key*))))

(defn- reason-for-not-initiating
  "Runs several checks against HOKS to determine if opiskelijapalautekysely
  should be initiated and if any of the checks fail, returns a reason
  (string) that describes why kysely cannot be initiated. Returns `nil`
  if there is no reason preventing kysely initiation."
  [kysely prev-hoks hoks opiskeluoikeus]
  (cond
    (not (:osaamisen-hankkimisen-tarve hoks))
    "`osaamisen-hankkimisen-tarve` not set to `true` for given HOKS."

    (not-any? ammatillinen-suoritus? (:suoritukset opiskeluoikeus))
    (format "No ammatillinen suoritus in opiskeluoikeus `%s`."
            (:opiskeluoikeus-oid hoks))

    (c/tuva-related-hoks? hoks)
    (str "HOKS is either TUVA-HOKS or \"ammatillisen koulutuksen HOKS\" "
         "related to TUVA-HOKS.")

    (and (= kysely :paattokysely)
         (not (added? :osaamisen-saavuttamisen-pvm prev-hoks hoks)))
    "`osaamisen-saavuttamisen-pvm` has not yet been set for given HOKS."))

(defn initiate?
  "Returns `true` when opiskelijapalautekysely (`kysely` being `:aloituskysely`
  or `:paattokysely`) should be initiated. The function has two arities, one for
  HOKS creation and the other for HOKS update."
  ([kysely hoks opiskeluoikeus] ; on HOKS creation
    {:pre [(#{:aloituskysely :paattokysely} kysely)]}
    (initiate? kysely nil hoks opiskeluoikeus))
  ([kysely prev-hoks hoks opiskeluoikeus] ; on HOKS update
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

              :else true))))))) ; will be converted to `false`.

(defn kysely-data
  [hoks kyselytyyppi]
  {:hoks-id       (:id hoks)
   :heratepvm     (if (= kyselytyyppi "aloittaneet")
                    (:ensikertainen-hyvaksyminen hoks)
                    (:osaamisen-saavuttamisen-pvm hoks))
   :kyselytyyppi  kyselytyyppi
   :herate-source "ehoks_update"})

(defn kyselytyyppi
  [kysely opiskeluoikeus]
  (case kysely
    :aloituskysely "aloittaneet"
    :paattokysely  (->> opiskeluoikeus
                        :suoritukset
                        (find-first ammatillinen-suoritus?)
                        suoritustyyppi
                        koski-suoritustyyppi->kyselytyyppi)))

(defn kysely-already-exists?!
  [kysely hoks]
  (some? (first (get-by-hoks-id-and-kyselytyypit!
                  db/spec
                  {:hoks-id      (:id hoks)
                   :kyselytyypit (case kysely
                                   :aloituskysely ["aloittaneet"]
                                   :paattokysely  (vec paattokyselyt))}))))

(defn initiate!
  "Initiates opiskelijapalautekysely (`:aloituskysely` or `:paattokysely`).
  Currently, stores kysely data to eHOKS DB `palautteet` table and also sends
  the herate to AWS SQS for Herätepalvelu to process. Returns `true` if kysely
  was successfully initiated, `nil` or `false` otherwise."
  [kysely hoks opiskeluoikeus]
  {:pre [(#{:aloituskysely :paattokysely} kysely)]}
  (if (kysely-already-exists?! kysely hoks)
    (log/warnf "%s already exists for HOKS `%d`." (name kysely) (:id hoks))
    (let [kyselytyyppi (kyselytyyppi kysely opiskeluoikeus)]
      (assert (some? kyselytyyppi))
      (insert! db/spec (kysely-data hoks kyselytyyppi))
      ; Seding herate to AWS SQS (will be removed when Herätepalvelu migration
      ; is complete).
      (sqs/send-amis-palaute-message
        (case kysely
          :aloituskysely (sqs/build-hoks-hyvaksytty-msg hoks)
          :paattokysely  (sqs/build-hoks-osaaminen-saavutettu-msg
                           hoks (translate-kyselytyyppi kyselytyyppi)))))))

(defn initiate-if-needed!
  "Sends heräte data required for opiskelijapalautekysely (`:aloituskysely` or
  `:paattokysely`) to appropriate DynamoDB table of Herätepalvelu if no check is
  preventing the sending. Returns `true` if kysely was successfully sent."
  [kysely hoks]
  (let [opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                         (:opiskeluoikeus-oid hoks))]
    (when (initiate? kysely hoks opiskeluoikeus)
      (initiate! kysely hoks opiskeluoikeus))))

(defn initiate-every-needed!
  "Effectively the same as running `initiate-if-needed!` for multiple HOKSes,
  but also returns a count of the number of kyselys initiated."
  [kysely hoksit]
  (->> hoksit
       (filter #(initiate-if-needed! kysely %))
       count))
