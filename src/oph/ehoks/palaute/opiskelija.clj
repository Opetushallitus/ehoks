(ns oph.ehoks.palaute.opiskelija
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.hoks.common :as c])
  (:import [clojure.lang ExceptionInfo]))

(defn- suoritustyyppi [suoritus] (get-in suoritus [:tyyppi :koodiarvo]))

(defn- ammatillinen-suoritus?
  "Varmistaa, että suorituksen tyyppi on joko ammatillinen tutkinto tai
  osittainen ammatillinen tutkinto."
  [suoritus]
  (some? (#{"ammatillinentutkinto" "ammatillinentutkintoosittainen"}
           (suoritustyyppi suoritus))))

(def ^:private suoritustyyppi->kyselytyyppi
  {"ammatillinentutkinto"           "tutkinnon_suorittaneet"
   "ammatillinentutkintoosittainen" "tutkinnon_osia_suorittaneet"})

(defn- telma-suoritus?
  "Tarkistaa, onko suorituksen tyyppi TELMA (työhön ja elämään valmentava)."
  [suoritus]
  (some? (#{"telma", "telmakoulutuksenosa"} (suoritustyyppi suoritus))))

(defn- first-ammatillinen-suoritus
  "Hakee opiskeluoikeudesta ensimmäisen ammatillisen suorituksen (tyyppi
  ammatillinen suoritus tai osittainen ammatillinen suoritus). Nostaa
  poikkeuksen, jos yhtään ammatillista suoritusta ei löydy."
  [opiskeluoikeus]
  (or (some #(when (ammatillinen-suoritus? %) %) (:suoritukset opiskeluoikeus))
      (throw (ex-info "No ammatillinen suoritus in opiskeluoikeus."
                      {:type :no-ammatillinen-suoritus}))))

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

(defn initiate?
  "Checks if aloituskysely or päättökysely should be initiated The function has
  two arities, one for HOKS creation and the other for HOKS update. On HOKS
  creation log printings will occur when kysely won't be initiated and on HOKS
  update the other way around."
  ([kysely hoks] ; on HOKS creation
    {:pre [(#{:aloituskysely :paattokysely} kysely)]}
    (let [msg (format "Not sending %s for HOKS `%s`. "
                      (name kysely) (:id hoks))]
      (cond
        (not (:osaamisen-hankkimisen-tarve hoks))
        (log/info msg "`osaamisen-hankkimisen-tarve` is not set to `true` "
                  "for given HOKS.")
        (c/tuva-related-hoks? hoks)
        (log/info msg "HOKS is either TUVA-HOKS or \"ammatillisen "
                  "koulutuksen HOKS\" related to TUVA-HOKS.")
        (and (= kysely :paattokysely)
             (nil? (:osaamisen-saavuttamisen-pvm hoks)))
        (log/info msg "`osaamisen-saavuttamisen-pvm` has not yet been set "
                  "for given HOKS.")
        :else true)))
  ([kysely current-hoks updated-hoks] ; on HOKS update
    {:pre [(#{:aloituskysely :paattokysely} kysely)]}
    (let [msg (format "Sending %s for HOKS `%s`. "
                      (name kysely) (:id updated-hoks))]
      (and (:osaamisen-hankkimisen-tarve updated-hoks)
           (not (c/tuva-related-hoks? updated-hoks))
           (not ; In case of log prints `nil` is returned, convert to `true`.
             (cond
               (and (= kysely :aloituskysely)
                    (not (:osaamisen-hankkimisen-tarve current-hoks)))
               (log/info msg "`osaamisen-hankkimisen-tarve` updated from "
                         "`false` to `true`.")
               (and (= kysely :aloituskysely)
                    (added? :sahkoposti current-hoks updated-hoks))
               (log/info msg "`sahkoposti` has been added.")
               (and (= kysely :aloituskysely)
                    (added? :puhelinnumero current-hoks updated-hoks))
               (log/info msg "`puhelinnumero` has been added.")
               (and (= kysely :paattokysely)
                    (added? :osaamisen-saavuttamisen-pvm
                            current-hoks
                            updated-hoks))
               (log/info msg "`osaamisen-saavuttamisen-pvm` has been added.")
               :else true)))))) ; will be converted to `false`.

(defn initiate!
  "Sends heräte data required for opiskelijapalautekysely (`:aloituskysely` or
  `:paattokysely`) to appropriate DynamoDB table of Herätepalvelu and returns
  `true` if kysely was successfully sent."
  [kysely hoks]
  {:pre [(#{:aloituskysely :paattokysely} kysely)
         (:id hoks)
         (:opiskeluoikeus-oid hoks)]}
  (try
    (case kysely
      :aloituskysely
      (sqs/send-amis-palaute-message (sqs/build-hoks-hyvaksytty-msg hoks))
      ; In the case of päättökysely, `opiskeluoikeus` is fetched from Koski in
      ; order to determine, if the kyselytyyppi is `tutkinnon_suorittaneet` or
      ; `tutkinnon_osia_suorittaneet`."
      :paattokysely (->> (:opiskeluoikeus-oid hoks)
                         k/get-existing-opiskeluoikeus!
                         first-ammatillinen-suoritus
                         suoritustyyppi
                         suoritustyyppi->kyselytyyppi
                         (sqs/build-hoks-osaaminen-saavutettu-msg hoks)
                         sqs/send-amis-palaute-message))
    (catch ExceptionInfo e
      (log/logf (case (:type (ex-data e))
                  ::k/opiskeluoikeus-not-found :warn
                  :no-ammatillinen-suoritus    :info
                  :error)
                "Not sending %s for HOKS `%s` - %s %s"
                (name kysely)
                (:id hoks)
                (.getMessage e)
                (ex-data e)))))

(defn initiate-if-needed!
  "Sends heräte data required for opiskelijapalautekysely (`:aloituskysely` or
  `:paattokysely`) to appropriate DynamoDB table of Herätepalvelu if no check is
  preventing the sending. Returns `true` if kysely was successfully sent."
  [kysely hoks]
  (when (initiate? kysely hoks) (initiate! kysely hoks)))

(defn initiate-every-needed!
  "Effectively the same as running `initiate-if-needed!` for multiple HOKSes,
  but also returns a count of the number of kyselys initiated."
  [kysely hoksit]
  (->> hoksit
       (filter #(initiate-if-needed! kysely %))
       count))
