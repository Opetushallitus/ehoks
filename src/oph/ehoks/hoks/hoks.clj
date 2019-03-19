(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]))

(defn set-olemassa-olevat-ammatilliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-ammatilliset-tutkinnon-osat
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn set-hankitun-osaamisen-naytto [o]
  (let [naytot (db/select-hankitun-osaamisen-naytot-by-ppto-id (:id o))]
    (assoc
      o
      :hankitun-osaamisen-naytto
      (mapv
        #(-> %
             (assoc
               :koulutuksen-jarjestaja-arvioijat
               (db/select-koulutuksen-jarjestaja-arvioijat-by-hon-id (:id o))
               :tyoelama-arvioijat
               (db/select-tyoelama-arvioijat-by-hon-id (:id o))
               :nayttoymparisto
               (db/select-nayttoymparisto-by-id (:nayttoymparisto-id %))
               :keskeiset-tyotehtavat-naytto
               (db/select-tyotehtavat-by-hankitun-osaamisen-naytto-id (:id o)))
             (dissoc :nayttoymparisto-id))
        naytot))))

(defn set-tyopaikalla-hankittava-osaaminen [t]
  (let [o (db/select-tyopaikalla-hankittava-osaaminen-by-id
             (:tyopaikalla-hankittava-osaaminen-id t))]
    (dissoc
      (assoc t
             :tyopaikalla-hankittava-osaaminen
             (-> o
                 (dissoc :id)
                 (assoc :muut-osallistujat
                        (db/select-henkilot-by-tho-id (:id o)))
                 (assoc :keskeiset-tyotehtavat
                        (db/select-tyotehtavat-by-tho-id (:id o)))))
     :tyopaikalla-hankittava-osaaminen-id)))

(defn set-muut-oppimisymparistot [t]
  (assoc
    t
    :muut-oppimisymparisto
    (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
      (:id t))))

(defn set-osaamisen-hankkimistavat [o]
  (let [hankkimistavat (db/select-osaamisen-hankkimistavat-by-ppto-id (:id o))]
    (assoc
      o
      :osaamisen-hankkimistavat
      (->> hankkimistavat
        (map set-tyopaikalla-hankittava-osaaminen)
        (map set-muut-oppimisymparistot)
        (map #(dissoc % :id))))))

(defn set-puuttuvat-paikalliset-tutkinnon-osat [h]
  (let [c (db/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id (:id h))]
    (assoc
      h
      :puuttuvat-paikalliset-tutkinnon-osat

      (mapv
        #(-> %
           set-hankitun-osaamisen-naytto
           set-osaamisen-hankkimistavat)
        c))))

(defn set-olemassa-olevat-paikalliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-paikalliset-tutkinnon-osat
    (db/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn set-olemassa-olevat-yhteiset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-yhteiset-tutkinnon-osat
    (db/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id (:id h))))

(defn get-hokses-by-oppija [oid]
  (map
    #(-> %
         set-olemassa-olevat-ammatilliset-tutkinnon-osat
         set-puuttuvat-paikalliset-tutkinnon-osat
         set-olemassa-olevat-yhteiset-tutkinnon-osat)
    (db/select-hoks-by-oppija-oid oid)))

(defn save-ppto-osaamisen-hankkimistavat! [ppto c]
  (db/insert-ppto-osaamisen-hankkimistavat! ppto c))

(defn save-ppto-hankitun-osaamisen-naytto! [ppto n]
  (let [nayttoymparisto
        (db/insert-nayttoymparisto! (:nayttoymparisto n))]
    (let [naytto
          (db/insert-ppto-hankitun-osaamisen-naytto!
            ppto
            (assoc n :nayttoymparisto-id (:id nayttoymparisto)))]
      (db/insert-hankitun-osaamisen-nayton-koulutuksen-jarjestaja-arvioijat!
        naytto (:koulutuksen-jarjestaja-arvioijat n))
      (db/insert-hankitun-osaamisen-nayton-tyoelama-arvioijat!
        naytto (:tyoelama-arvioijat n))
      (db/insert-hankitun-osaamisen-nayton-tyotehtavat!
        naytto (:keskeiset-tyotehtavat-naytto n)))))

(defn save-ppto-hankitun-osaamisen-naytot! [ppto c]
  (mapv
    #(save-ppto-hankitun-osaamisen-naytto! ppto %)
    c))

(defn save-puuttuva-paikallinen-tutkinnon-osa! [h ppto]
  (let [ppto-db (db/insert-puuttuva-paikallinen-tutkinnon-osa! ppto)]
    (assoc
      ppto-db
      :osaamisen-hankkimistavat
      (save-ppto-osaamisen-hankkimistavat!
        ppto-db (:osaamisen-hankkimistavat ppto))
      :hankitun-osaamisen-naytto
      (save-ppto-hankitun-osaamisen-naytot!
        ppto-db (:hankitun-osaamisen-naytto ppto)))))

(defn save-puuttuvat-paikalliset-tutkinnon-osat! [h c]
  (mapv #(save-puuttuva-paikallinen-tutkinnon-osa! h %) c))

(defn save-hoks! [h]
  (let [saved-hoks (first (db/insert-hoks! h))]
    (db/insert-puuttuvat-paikalliset-tutkinnon-osat!
      (map
        #(assoc % :hoks-id (:id saved-hoks))
        (:puuttuvat-paikalliset-tutkinnon-osat h)))))
