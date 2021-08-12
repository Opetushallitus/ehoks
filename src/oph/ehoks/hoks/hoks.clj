(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql.aiemmin-hankitut :as db-ah]
            [oph.ehoks.db.postgresql.hankittavat :as db-ha]
            [oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat :as db-ot]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [clojure.tools.logging :as log])
  (:import (java.time LocalDate)))

(defn get-hoks-values [h]
  (let [id (:id h)]
    (assoc
      h
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (ah/get-aiemmin-hankitut-ammat-tutkinnon-osat id)
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-paikalliset-tutkinnon-osat id)
      :hankittavat-paikalliset-tutkinnon-osat
      (ha/get-hankittavat-paikalliset-tutkinnon-osat id)
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-yhteiset-tutkinnon-osat id)
      :hankittavat-ammat-tutkinnon-osat
      (ha/get-hankittavat-ammat-tutkinnon-osat id)
      :opiskeluvalmiuksia-tukevat-opinnot
      (ot/get-opiskeluvalmiuksia-tukevat-opinnot id)
      :hankittavat-yhteiset-tutkinnon-osat
      (ha/get-hankittavat-yhteiset-tutkinnon-osat id))))

(defn get-hokses-by-oppija [oid]
  (mapv
    get-hoks-values
    (db-hoks/select-hoks-by-oppija-oid oid)))

(defn get-hoks-by-id [id]
  (get-hoks-values (db-hoks/select-hoks-by-id id)))

(defn save-hoks! [h]
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (let [saved-hoks (db-hoks/insert-hoks! h conn)]
      (when (:osaamisen-hankkimisen-tarve h)
        (sqs/send-amis-palaute-message
          (sqs/build-hoks-hyvaksytty-msg
            (:id saved-hoks) h)))
      (assoc
        saved-hoks
        :aiemmin-hankitut-ammat-tutkinnon-osat
        (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
          (:id saved-hoks)
          (:aiemmin-hankitut-ammat-tutkinnon-osat h)
          conn)
        :aiemmin-hankitut-paikalliset-tutkinnon-osat
        (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
          (:id saved-hoks)
          (:aiemmin-hankitut-paikalliset-tutkinnon-osat h)
          conn)
        :hankittavat-paikalliset-tutkinnon-osat
        (ha/save-hankittavat-paikalliset-tutkinnon-osat!
          (:id saved-hoks)
          (:hankittavat-paikalliset-tutkinnon-osat h)
          conn)
        :aiemmin-hankitut-yhteiset-tutkinnon-osat
        (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
          (:id saved-hoks)
          (:aiemmin-hankitut-yhteiset-tutkinnon-osat h)
          conn)
        :hankittavat-ammat-tutkinnon-osat
        (ha/save-hankittavat-ammat-tutkinnon-osat!
          (:id saved-hoks)
          (:hankittavat-ammat-tutkinnon-osat h)
          conn)
        :opiskeluvalmiuksia-tukevat-opinnot
        (ot/save-opiskeluvalmiuksia-tukevat-opinnot!
          (:id saved-hoks)
          (:opiskeluvalmiuksia-tukevat-opinnot h)
          conn)
        :hankittavat-yhteiset-tutkinnon-osat
        (ha/save-hankittavat-yhteiset-tutkinnon-osat!
          (:id saved-hoks)
          (:hankittavat-yhteiset-tutkinnon-osat h)
          conn)))))

(defn- merge-not-given-hoks-values [new-hoks-values]
  (let [empty-top-level-hoks {:versio nil
                              :sahkoposti nil
                              :urasuunnitelma-koodi-uri nil
                              :osaamisen-hankkimisen-tarve nil
                              :hyvaksytty nil
                              :urasuunnitelma-koodi-versio nil
                              :osaamisen-saavuttamisen-pvm nil
                              :paivitetty nil}]
    (merge empty-top-level-hoks new-hoks-values)))

(defn- replace-main-hoks! [hoks-id new-values db-conn]
  (db-hoks/update-hoks-by-id!
    hoks-id (merge-not-given-hoks-values new-values) db-conn))

(defn- replace-oto! [hoks-id new-oto-values db-conn]
  (db-ot/delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id
    hoks-id db-conn)
  (when
   new-oto-values
    (ot/save-opiskeluvalmiuksia-tukevat-opinnot!
      hoks-id new-oto-values db-conn)))

(defn- replace-hato! [hoks-id new-hato-values db-conn]
  (db-ha/delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hato-values
    (ha/save-hankittavat-ammat-tutkinnon-osat!
      hoks-id new-hato-values db-conn)))

(defn- replace-hpto! [hoks-id new-hpto-values db-conn]
  (db-ha/delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hpto-values
    (ha/save-hankittavat-paikalliset-tutkinnon-osat!
      hoks-id new-hpto-values db-conn)))

(defn- replace-hyto! [hoks-id new-hyto-values db-conn]
  (db-ha/delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hyto-values
    (ha/save-hankittavat-yhteiset-tutkinnon-osat!
      hoks-id new-hyto-values db-conn)))

(defn- replace-ahato! [hoks-id new-ahato-values db-conn]
  (db-ah/delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahato-values
    (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
      hoks-id new-ahato-values db-conn)))

(defn- replace-ahpto! [hoks-id new-ahpto-values db-conn]
  (db-ah/delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahpto-values
    (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id new-ahpto-values db-conn)))

(defn- replace-ahyto! [hoks-id new-ahyto-values db-conn]
  (db-ah/delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahyto-values
    (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
      hoks-id new-ahyto-values db-conn)))

(defn- new-osaamisen-saavuttamisen-pvm-added? [old-osp new-osp]
  (and (some? new-osp)
       (nil? old-osp)))

(defn- send-paattokysely [hoks-id os-saavut-pvm hoks]
  (log/infof
    (str "Sending päättökysely for hoks id %s. "
         "Triggered by hoks update including osaamisen-saavuttamisen-pvm %s")
    hoks-id os-saavut-pvm)
  (sqs/send-amis-palaute-message
    (sqs/build-hoks-osaaminen-saavutettu-msg hoks-id os-saavut-pvm hoks)))

(defn get-osaamisen-hankkimistavat [hoks]
  (concat
    (mapcat
      :osaamisen-hankkimistavat
      (:hankittavat-ammat-tutkinnon-osat hoks))
    (mapcat
      :osaamisen-hankkimistavat
      (:hankittavat-paikalliset-tutkinnon-osat hoks))
    (mapcat :osaamisen-hankkimistavat
            (mapcat :osa-alueet (:hankittavat-yhteiset-tutkinnon-osat hoks)))))

(defn- hankkimistapa-new-or-ongoing [oh]
  (try
    (or
      (.isAfter (:alku oh) (LocalDate/now))
      (and
        (or (.isBefore (:alku oh) (LocalDate/now))
            (.isEqual (:alku oh) (LocalDate/now)))
        (.isAfter (:loppu oh) (LocalDate/now))))
    (catch Exception e
      (log/info "hankkimistapa-new-or-ongoing check failed for:")
      (log/info oh)
      (log/info e))))

(defn- y-tunnus-missing? [oh]
  (when (and
          (or
            (= (:osaamisen-hankkimistapa-koodi-uri oh)
               "osaamisenhankkimistapa_koulutussopimus")
            (= (:osaamisen-hankkimistapa-koodi-uri oh)
               "osaamisenhankkimistapa_oppisopimus"))
          (hankkimistapa-new-or-ongoing oh)
          (:tyopaikalla-jarjestettava-koulutus oh))
    (when (nil? (get-in oh [:tyopaikalla-jarjestettava-koulutus
                            :tyopaikan-y-tunnus]))
      oh)))

(defn missing-tyopaikan-y-tunnus? [osaamisen-hankkimistavat]
  (when (seq osaamisen-hankkimistavat)
    (some y-tunnus-missing? osaamisen-hankkimistavat)))

(defn replace-hoks! [hoks-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (let [hoks (get-hoks-by-id hoks-id)
          old-opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
          old-oppija-oid (:oppija-oid hoks)
          old-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                            hoks)
          new-opiskeluoikeus-oid (:opiskeluoikeus-oid new-values)
          new-oppija-oid (:oppija-oid new-values)
          new-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                            new-values)
          osaamisen-hankkimistavat (get-osaamisen-hankkimistavat new-values)
          oh-missing-tyopaikan-y-tunnus (missing-tyopaikan-y-tunnus?
                                          osaamisen-hankkimistavat)]
      (cond
        (and (some? new-opiskeluoikeus-oid)
             (not= new-opiskeluoikeus-oid old-opiskeluoikeus-oid))
        (throw (ex-info
                 "Opiskeluoikeus update not allowed!"
                 {:error :disallowed-update}))
        (and (some? new-oppija-oid) (not= new-oppija-oid old-oppija-oid))
        (throw (ex-info
                 "Oppija-oid update not allowed!"
                 {:error :disallowed-update}))
        (some? oh-missing-tyopaikan-y-tunnus)
        (throw (ex-info
                 (str "tyopaikan-y-tunnus missing for new or active "
                      "osaamisen hankkimistapa: " oh-missing-tyopaikan-y-tunnus)
                 {:error :disallowed-update}))
        :else
        (do
          (replace-main-hoks! hoks-id new-values db-conn)
          (replace-oto! hoks-id (:opiskeluvalmiuksia-tukevat-opinnot new-values)
                        db-conn)
          (replace-hato! hoks-id (:hankittavat-ammat-tutkinnon-osat new-values)
                         db-conn)
          (replace-hpto! hoks-id
                         (:hankittavat-paikalliset-tutkinnon-osat new-values)
                         db-conn)
          (replace-hyto! hoks-id
                         (:hankittavat-yhteiset-tutkinnon-osat new-values)
                         db-conn)
          (replace-ahato! hoks-id
                          (:aiemmin-hankitut-ammat-tutkinnon-osat new-values)
                          db-conn)
          (replace-ahpto! hoks-id (:aiemmin-hankitut-paikalliset-tutkinnon-osat
                                    new-values)
                          db-conn)
          (replace-ahyto! hoks-id (:aiemmin-hankitut-yhteiset-tutkinnon-osat
                                    new-values)
                          db-conn)
          (when (new-osaamisen-saavuttamisen-pvm-added?
                  old-osaamisen-saavuttamisen-pvm
                  new-osaamisen-saavuttamisen-pvm)
            (send-paattokysely hoks-id
                               new-osaamisen-saavuttamisen-pvm hoks)))))))

(defn update-hoks! [hoks-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (let [hoks (get-hoks-by-id hoks-id)
          old-opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
          old-oppija-oid (:oppija-oid hoks)
          old-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                            hoks)
          new-opiskeluoikeus-oid (:opiskeluoikeus-oid new-values)
          new-oppija-oid (:oppija-oid new-values)
          new-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                            new-values)
          osaamisen-hankkimistavat (get-osaamisen-hankkimistavat new-values)
          oh-missing-tyopaikan-y-tunnus (missing-tyopaikan-y-tunnus?
                                          osaamisen-hankkimistavat)]
      (cond
        (and (some? new-opiskeluoikeus-oid)
             (not= new-opiskeluoikeus-oid old-opiskeluoikeus-oid))
        (throw (ex-info
                 "Opiskeluoikeus update not allowed!"
                 {:error :disallowed-update}))
        (and (some? new-oppija-oid) (not= new-oppija-oid old-oppija-oid))
        (throw (ex-info
                 "Oppija-oid update not allowed!"
                 {:error :disallowed-update}))
        (some? oh-missing-tyopaikan-y-tunnus)
        (throw (ex-info
                 (str "tyopaikan-y-tunnus missing for new or active "
                      "osaamisen hankkimistapa: " oh-missing-tyopaikan-y-tunnus)
                 {:error :disallowed-update}))
        :else
        (let [h (db-hoks/update-hoks-by-id! hoks-id new-values db-conn)]
          (when (new-osaamisen-saavuttamisen-pvm-added?
                  old-osaamisen-saavuttamisen-pvm
                  new-osaamisen-saavuttamisen-pvm)
            (send-paattokysely hoks-id new-osaamisen-saavuttamisen-pvm hoks))
          h)))))

(defn insert-kyselylinkki! [m]
  (db-ops/insert-one!
    :kyselylinkit
    (db-ops/to-sql m)))

(defn update-kyselylinkki! [m]
  (db-ops/update!
    :kyselylinkit m
    ["kyselylinkki = ?" (:kyselylinkki m)]))

(defn get-kyselylinkit-by-oppija-oid [oid]
  (db-hoks/select-kyselylinkit-by-oppija-oid oid))

(defn delete-kyselylinkki! [kyselylinkki]
  (db-ops/delete!
    :kyselylinkit
    ["kyselylinkki = ?" kyselylinkki]))
