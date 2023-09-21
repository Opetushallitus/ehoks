(ns oph.ehoks.opiskelijapalaute
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.hoks.common :as c]))

(defn- ammatillinen-suoritus?
  "Tarkistaa, onko suorituksen tyyppi ammatillinen suoritus tai osittainen
  ammatillinen suoritus."
  [suoritus]
  (or (= (:koodiarvo (:tyyppi suoritus)) "ammatillinentutkinto")
      (= (:koodiarvo (:tyyppi suoritus)) "ammatillinentutkintoosittainen")))

(defn- get-suoritus
  "Hakee opiskeluoikeudesta ensimmäisen suorituksen, jonka tyyppi on
  ammatillinen suoritus tai osittainen ammatillinen suoritus."
  [opiskeluoikeus]
  (some #(when (ammatillinen-suoritus? %) %) (:suoritukset opiskeluoikeus)))

(defn- get-kysely-type
  "Muuttaa opiskeluoikeuden suorituksen tyypin sellaiseksi, minkä herätepalvelu
  voi hyväksyä."
  [opiskeluoikeus]
  (let [tyyppi (get-in
                 (get-suoritus opiskeluoikeus)
                 [:tyyppi :koodiarvo])]
    (cond
      (= tyyppi "ammatillinentutkinto")
      "tutkinnon_suorittaneet"
      (= tyyppi "ammatillinentutkintoosittainen")
      "tutkinnon_osia_suorittaneet")))

(defn- added?
  [key* current-hoks updated-hoks]
  (and (some? (get updated-hoks key*)) (nil? (get current-hoks key*))))

(defn send?
  "Checks if aloituskysely or päättökysely should be sent. The function has two
  arities, one for HOKS creation and the other for HOKS update. On HOKS creation
  log printings will occur when kysely won't be sent and on HOKS update the
  other way around."
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

(defn send!
  "Sends heräte data required for opiskelijapalautekysely (`:aloituskysely` or
  `:paattokysely`) to appropriate DynamoDB table of Herätepalvelu and returns
  `true` if kysely was successfully sent."
  [kysely hoks]
  {:pre [(#{:aloituskysely :paattokysely} kysely)
         (:id hoks)]}
  (try
    (case kysely
      :aloituskysely
      (sqs/send-amis-palaute-message (sqs/build-hoks-hyvaksytty-msg hoks))
      ; In the case of päättökysely, `opiskeluoikeus` is fetched from Koski in
      ; order to determine, if the kyselytyyppi is `tutkinnon_suorittaneet` or
      ; `tutkinnon_osia_suorittaneet`."
      :paattokysely
      (or (some->> (:opiskeluoikeus-oid hoks)
                   k/get-opiskeluoikeus-info
                   get-kysely-type
                   (sqs/build-hoks-osaaminen-saavutettu-msg hoks)
                   sqs/send-amis-palaute-message)
          (log/warnf (str "Not sending paattokysely for HOKS `%s`. Couldn't "
                          "get opiskeluoikeus `%s` from Koski.`")
                     (:id hoks)
                     (:opiskeluoikeus-oid hoks))))
    (catch Exception e
      (log/warn e)
      (log/warnf (str "Error in sending %s for HOKS `%s`. "
                      "osaamisen-saavuttamisen-pvm %s."
                      "opiskeluoikeus-oid %s.")
                 (name kysely)
                 (:id hoks)
                 (:osaamisen-saavuttamisen-pvm hoks)
                 (:opiskeluoikeus-oid hoks)))))

(defn send-if-needed!
  "Sends heräte data required for opiskelijapalautekysely (`:aloituskysely` or
  `:paattokysely`) to appropriate DynamoDB table of Herätepalvelu if no check is
  preventing the sending. Returns `true` if kysely was successfully sent."
  [kysely hoks]
  (when (send? kysely hoks) (send! kysely hoks)))

(defn send-every-needed!
  "Effectively the same as running `send-if-needed!` for multiple HOKSes, but
  also returns a count of the number of kyselys sent."
  [kysely hoksit]
  (->> hoksit
       (filter #(send-if-needed! kysely %))
       count))
