(ns oph.ehoks.heratepalvelu.heratepalvelu
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.hoks.hoks :as h]
            [clojure.string :as str]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.oppijaindex :as op]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-oo]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops])
  (:import (java.time LocalDate)))

(defn find-finished-workplace-periods-teprah
  "Queries for all finished workplace periods between start and end"
  [start end limit]
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot
                "hyto" start end limit "AND NOT teprah_kasitelty")
        hptos (db-hoks/select-paattyneet-tyoelamajaksot
                "hpto" start end limit "AND NOT teprah_kasitelty")
        hatos (db-hoks/select-paattyneet-tyoelamajaksot
                "hato" start end limit "AND NOT teprah_kasitelty")]
    (concat hytos hptos hatos)))

(defn find-finished-workplace-periods
  "Queries for all finished workplace periods between start and end"
  [start end limit]
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot
                "hyto" start end limit "AND NOT tep_kasitelty")
        hptos (db-hoks/select-paattyneet-tyoelamajaksot
                "hpto" start end limit "AND NOT tep_kasitelty")
        hatos (db-hoks/select-paattyneet-tyoelamajaksot
                "hato" start end limit "AND NOT tep_kasitelty")]
    (concat hytos hptos hatos)))

(defn send-workplace-periods-rahoituslaskenta
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (doseq [period periods]
    (sqs/send-teprah-message (sqs/build-tyoelamapalaute-msg period))))

(defn send-workplace-periods
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (doseq [period periods]
    (sqs/send-tyoelamapalaute-message (sqs/build-tyoelamapalaute-msg period))))

(defn set-teprah-kasitelty
  "Marks an osaamisen hankkimistapa as handled (käsitelty)."
  [hankkimistapa-ids]
  (db-hoks/update-osaamisen-hankkimistapa-teprah-kasitelty hankkimistapa-ids))

(defn process-periods-for-teprah
  "Finds all finished workplace periods (teprah_kasitelty == false)
  between dates start and end and sends them to a SQS queue,
  marks teprah_kasitelty = true after queuing."
  [start end limit]
  (log/info (str "Processing finished periods for rahoituslaskenta: start "
                 start ", end " end ", limit " limit))
  (let [periods (find-finished-workplace-periods-teprah start end limit)]
    (log/infof
      "Sending teprah %d  (limit %d) finished workplace periods between %s - %s"
      (count periods) limit start end)
    (send-workplace-periods-rahoituslaskenta periods)
    (log/infof "Total of %d periods sent to queue, marking to db"
               (count periods))
    (set-teprah-kasitelty (map :hankkimistapa_id periods))
    periods))

(defn process-finished-workplace-periods
  "Finds all finished workplace periods between dates start and
  end and sends them to a SQS queue"
  [start end limit]
  (let [periods (find-finished-workplace-periods start end limit)]
    (log/infof
      "Sending %d  (limit %d) finished workplace periods between %s - %s"
      (count periods) limit start end)
    (send-workplace-periods periods)
    periods))

(defn get-oppija-kyselylinkit
  "Returns all feedback links for oppija"
  [oppija-oid]
  (filter
    some?
    (map
      #(try
         (if-not (:vastattu %1)
           (when-let [status (arvo/get-kyselylinkki-status-catch-404
                               (:kyselylinkki %1))]
             (let [loppupvm (LocalDate/parse
                              (first
                                (str/split (:voimassa_loppupvm status) #"T")))]
               (h/update-kyselylinkki!
                 {:kyselylinkki (:kyselylinkki %1)
                  :voimassa_loppupvm loppupvm
                  :vastattu (:vastattu status)})
               (assoc
                 %1
                 :voimassa-loppupvm loppupvm
                 :vastattu (:vastattu status))))
           %1)
         (catch Exception e
           (log/error e)
           (throw e)))
      (h/get-kyselylinkit-by-oppija-oid oppija-oid))))

(defn set-tep-kasitelty
  "Marks an osaamisen hankkimistapa as handled (käsitelty)."
  [hankkimistapa-id to]
  (db-hoks/update-osaamisen-hankkimistapa-tep-kasitelty hankkimistapa-id to))

(defn- send-kyselyt-for-hoksit
  "Send questionaires for the given set of HOKSes."
  [hoksit build-msg]
  (loop [hoks (first hoksit)
         r (rest hoksit)
         c 0]
    (if (:osaamisen-hankkimisen-tarve hoks)
      (do
        (if-let [msg (build-msg hoks)]
          (sqs/send-amis-palaute-message msg))
        (recur (first r) (rest r) (inc c)))
      (if (not-empty r)
        (recur (first r) (rest r) c)
        c))))

(defn resend-aloituskyselyherate-between
  "Resend aloituskyselyt for given time period."
  [from to]
  (send-kyselyt-for-hoksit (db-hoks/select-hoksit-created-between from to)
                           #(sqs/build-hoks-hyvaksytty-msg (:id %) %)))

(defn paatto-build-msg
  "Build päättökysely message."
  [hoks]
  (if-let [opiskeluoikeus (k/get-opiskeluoikeus-info
                            (:opiskeluoikeus-oid hoks))]
    (if-let [kyselytyyppi (h/get-kysely-type opiskeluoikeus)]
      (sqs/build-hoks-osaaminen-saavutettu-msg
        (:id hoks)
        (:osaamisen-saavuttamisen-pvm hoks)
        hoks
        kyselytyyppi))))

(defn resend-paattokyselyherate-between
  "Resend päättökyselyt for given time period."
  [from to]
  (send-kyselyt-for-hoksit
    (db-hoks/select-hoksit-finished-between from to)
    paatto-build-msg))

(defn process-hoksit-without-kyselylinkit
  "Finds all HOKSit for which kyselylinkit haven't been created and sends them
  to the SQS queue"
  [start end limit]
  (let [aloittaneet
        (db-hoks/select-hoksit-with-kasittelemattomat-aloitusheratteet start
                                                                       end
                                                                       limit)
        paattyneet
        (db-hoks/select-hoksit-with-kasittelemattomat-paattoheratteet start
                                                                      end
                                                                      limit)
        hoksit (concat aloittaneet paattyneet)]
    (log/infof
      "Sending %d (limit %d) hoksit between %s and %s"
      (count hoksit) (* 2 limit) start end)
    (send-kyselyt-for-hoksit aloittaneet
                             #(sqs/build-hoks-hyvaksytty-msg (:id %) %))
    (send-kyselyt-for-hoksit paattyneet paatto-build-msg)
    hoksit))

(defn set-aloitusherate-kasitelty
  "Marks aloitusheräte handled (käsitelty) for a given HOKS."
  [hoks-id to]
  (db-hoks/update-amisherate-kasittelytilat-aloitusherate-kasitelty hoks-id to))

(defn set-paattoherate-kasitelty
  "Marks päättöheräte handled (käsitelty) for a given HOKS."
  [hoks-id to]
  (db-hoks/update-amisherate-kasittelytilat-paattoherate-kasitelty hoks-id to))

(defn handle-onrmodified
  "Handles ONR-modified call from heratepalvelu which is triggered by
  data change in ONR service."
  [oid]
  (if-let [oppija (op/get-oppija-by-oid oid)]
    ;; Jos päivitetyn oppijan oid löytyy ehoksista, niin tiedetään
    ;; että kyseessä ei ole oid-tiedon päivitys ja voidaan vain tarkastaa,
    ;; että nimi täsmää ONR:n tiedon kanssa.
    (let [onr-oppija (:body (onr/find-student-by-oid-no-cache oid))
          ehoks-oppija-nimi (:nimi oppija)
          onr-oppija-nimi (op/format-oppija-name onr-oppija)]
      (when (not= ehoks-oppija-nimi onr-oppija-nimi)
        (log/infof "Updating changed name for oppija %s" oid)
        (op/update-oppija! oid true)))
    ;; Jos oppijaa ei löydy päivitetyllä oidilla ehoksista,
    ;; niin ensin tarkastetaan, ettei kyseessä ole duplicate/slave oid.
    ;; (tämä saattaa olla turha tarkastus, mutta ainakin se estää sen,
    ;; että koskaan päivitettäisiin slave oideja ehoksin tauluihin.
    ;;
    ;; Sitten haetaan kyseisen master-oidin slavet ja niiden oideilla
    ;; oppijat ehoksin oppijat-taulusta.
    ;;
    ;; Jos oppijoita löytyy slave oideilla, niin päivitetään niiden
    ;; hokseihin, opiskeluoikeuksiin ja oppijat-taulun riviin uusi
    ;; master-oid. Lopuksi poistetaan slave-oidit oppijat taulusta.
    (let [onr-oppija (:body (onr/find-student-by-oid-no-cache oid))]
      (when-not (:duplicate onr-oppija)
        (let [slave-oppija-oids
              (map
                :oidHenkilo
                (:body (onr/get-slaves-of-master-oppija-oid oid)))
              oppijas-from-oppijaindex-by-slave-oids
              (remove nil?
                      (flatten
                        (map
                          #(:oid (op/get-oppija-by-oid %))
                          slave-oppija-oids)))]
          (when (seq oppijas-from-oppijaindex-by-slave-oids)
            (jdbc/with-db-transaction
              [db-conn (db-ops/get-db-connection)]
              (doseq [oppija-oid oppijas-from-oppijaindex-by-slave-oids]
                (log/infof (str "Changing duplicate oppija-oid %s to %s "
                                "for tables hoksit, oppijat and "
                                "opiskeluoikeudet.")
                           oppija-oid oid)
                (db-hoks/update-hoks-by-oppija-oid! oppija-oid
                                                    {:oppija-oid oid}
                                                    db-conn)
                (if (some? (op/get-oppija-by-oid oid))
                  (do
                    (db-oo/update-opiskeluoikeus-by-oppija-oid!
                      oppija-oid {:oppija-oid oid})
                    (db-ops/delete!
                      :oppijat ["oid = ?" oppija-oid]))
                  (db-oppija/update-oppija!
                    oppija-oid
                    {:oid  oid
                     :nimi (op/format-oppija-name onr-oppija)}))))))))))

(defn select-tyoelamajaksot-active-between
  "Finds all workplace periods active between start and end dates for student
  oppija."
  [oppija start end]
  (concat
    (db-hoks/select-tyoelamajaksot-active-between "hato" oppija start end)
    (db-hoks/select-tyoelamajaksot-active-between "hpto" oppija start end)
    (db-hoks/select-tyoelamajaksot-active-between "hyto" oppija start end)))
