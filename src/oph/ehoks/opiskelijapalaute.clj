(ns oph.ehoks.opiskelijapalaute
  (:require
    [clojure.tools.logging :as log]
    [oph.ehoks.external.aws-sqs :as sqs]
    [oph.ehoks.external.koski :as k]))

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
