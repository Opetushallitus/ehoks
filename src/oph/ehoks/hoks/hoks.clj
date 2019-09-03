(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as h]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]))

(defn get-hoks-values [h]
  (let [id (:id h)]
    (assoc
      h
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (ah/get-aiemmin-hankitut-ammat-tutkinnon-osat id)
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-paikalliset-tutkinnon-osat id)
      :hankittavat-paikalliset-tutkinnon-osat
      (h/get-hankittavat-paikalliset-tutkinnon-osat id)
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-yhteiset-tutkinnon-osat id)
      :hankittavat-ammat-tutkinnon-osat
      (h/get-hankittavat-ammat-tutkinnon-osat id)
      :opiskeluvalmiuksia-tukevat-opinnot
      (ot/get-opiskeluvalmiuksia-tukevat-opinnot id)
      :hankittavat-yhteiset-tutkinnon-osat
      (h/get-hankittavat-yhteiset-tutkinnon-osat id))))

(defn get-hokses-by-oppija [oid]
  (mapv
    get-hoks-values
    (db-hoks/select-hoks-by-oppija-oid oid)))

(defn get-hoks-by-id [id]
  (get-hoks-values (db-hoks/select-hoks-by-id id)))

(defn save-hoks! [h]
  (let [saved-hoks (db-hoks/insert-hoks! h)]
    (when (:osaamisen-hankkimisen-tarve h)
      (sqs/send-message (sqs/build-hoks-hyvaksytty-msg
                          (:id saved-hoks) h)))
    (assoc
      saved-hoks
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-ammat-tutkinnon-osat h))
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-paikalliset-tutkinnon-osat h))
      :hankittavat-paikalliset-tutkinnon-osat
      (h/save-hankittavat-paikalliset-tutkinnon-osat!
        (:id saved-hoks) (:hankittavat-paikalliset-tutkinnon-osat h))
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-yhteiset-tutkinnon-osat h))
      :hankittavat-ammat-tutkinnon-osat
      (h/save-hankittavat-ammat-tutkinnon-osat!
        (:id saved-hoks) (:hankittavat-ammat-tutkinnon-osat h))
      :opiskeluvalmiuksia-tukevat-opinnot
      (ot/save-opiskeluvalmiuksia-tukevat-opinnot!
        (:id saved-hoks) (:opiskeluvalmiuksia-tukevat-opinnot h))
      :hankittavat-yhteiset-tutkinnon-osat
      (h/save-hankittavat-yhteiset-tutkinnon-osat!
        (:id saved-hoks) (:hankittavat-yhteiset-tutkinnon-osat h)))))

(defn- merge-not-given-hoks-values [new-hoks-values]
  (let [empty-top-level-hoks {:versio nil
                              :sahkoposti nil
                              :urasuunnitelma-koodi-uri nil
                              :osaamisen-hankkimisen-tarve nil
                              :hyvaksytty nil
                              :urasuunnitelma-koodi-versio nil
                              :paivitetty nil}]
    (merge empty-top-level-hoks new-hoks-values)))

(defn- replace-main-hoks! [hoks-id new-values db-conn]
  (db-hoks/update-hoks-by-id!
    hoks-id (merge-not-given-hoks-values new-values) db-conn))

(defn- replace-oto! [hoks-id new-oto-values db-conn]
  (db/delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id hoks-id db-conn)
  (when
   new-oto-values
    (ot/save-opiskeluvalmiuksia-tukevat-opinnot! hoks-id new-oto-values)))

(defn- replace-hato! [hoks-id new-hato-values db-conn]
  (db/delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id hoks-id db-conn)
  (when
   new-hato-values
    (h/save-hankittavat-ammat-tutkinnon-osat! hoks-id new-hato-values)))

(defn- replace-hpto! [hoks-id new-hpto-values db-conn]
  (db/delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id db-conn)
  (when
   new-hpto-values
    (h/save-hankittavat-paikalliset-tutkinnon-osat! hoks-id new-hpto-values)))

(defn- replace-hyto! [hoks-id new-hyto-values db-conn]
  (db/delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id db-conn)
  (when
   new-hyto-values
    (h/save-hankittavat-yhteiset-tutkinnon-osat! hoks-id new-hyto-values)))

(defn- replace-ahato! [hoks-id new-ahato-values db-conn]
  (db/delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahato-values
    (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat! hoks-id new-ahato-values)))

(defn- replace-ahpto! [hoks-id new-ahpto-values db-conn]
  (db/delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahpto-values
    (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id new-ahpto-values)))

(defn- replace-ahyto! [hoks-id new-ahyto-values]
  (db/delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-ahyto-values
    (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat! hoks-id new-ahyto-values)))

(defn replace-hoks! [hoks-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (replace-main-hoks! hoks-id new-values db-conn)
    ;TODO db-conn should be also used when saving hoks parts, see EH-465
    (replace-oto! hoks-id (:opiskeluvalmiuksia-tukevat-opinnot new-values)
                  db-conn)
    (replace-hato! hoks-id (:hankittavat-ammat-tutkinnon-osat new-values)
                   db-conn)
    (replace-hpto! hoks-id (:hankittavat-paikalliset-tutkinnon-osat new-values)
                   db-conn)
    (replace-hyto! hoks-id (:hankittavat-yhteiset-tutkinnon-osat new-values)
                   db-conn)
    (replace-ahato! hoks-id (:aiemmin-hankitut-ammat-tutkinnon-osat new-values)
                    db-conn)
    (replace-ahpto! hoks-id (:aiemmin-hankitut-paikalliset-tutkinnon-osat
                              new-values)
                    db-conn)
    (replace-ahyto! hoks-id (:aiemmin-hankitut-yhteiset-tutkinnon-osat
                              new-values))))

(defn update-hoks! [hoks-id new-values]
  (db-hoks/update-hoks-by-id! hoks-id new-values))
