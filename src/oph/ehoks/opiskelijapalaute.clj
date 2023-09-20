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

(defn get-kysely-type
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

(defn send-aloituskysely!
  "Lähettää AMIS aloituskyselyn herätepalveluun."
  [hoks]
  {:pre [(:id hoks)]}
  (try
    (sqs/send-amis-palaute-message (sqs/build-hoks-hyvaksytty-msg hoks))
    (catch Exception e
      (log/warn e)
      (log/warnf "Error in sending aloituskysely for hoks id %s." (:id hoks)))))

(defn send-paattokysely!
  "Lähettää AMIS-päättöpalautekyselyn herätepalveluun."
  [hoks]
  {:pre [(:id hoks) (:osaamisen-saavuttamisen-pvm hoks)]}
  (try (let [opiskeluoikeus (k/get-opiskeluoikeus-info
                              (:opiskeluoikeus-oid hoks))
             kyselytyyppi (get-kysely-type opiskeluoikeus)]
         (when (and (some? opiskeluoikeus) (some? kyselytyyppi))
           (sqs/send-amis-palaute-message
             (sqs/build-hoks-osaaminen-saavutettu-msg hoks kyselytyyppi))))
       (catch Exception e
         (log/warn e)
         (log/warnf (str "Error in sending päättökysely for hoks id %s. "
                         "osaamisen-saavuttamisen-pvm %s. "
                         "opiskeluoikeus-oid %s.")
                    (:id hoks)
                    (:osaamisen-saavuttamisen-pvm hoks)
                    (:opiskeluoikeus-oid hoks)))))

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
