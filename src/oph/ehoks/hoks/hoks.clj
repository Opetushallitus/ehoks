(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]))

(defn get-ooato-tarkentavat-tiedot-naytto [ooato]
  [])

(defn get-tarkentavat-tiedot-arvioija [id]
  (let [tta (first (db/select-todennettu-arviointi-lisatiedot-by-id id))]
    (dissoc
    (assoc
      tta
      :aiemmin-hankitun-osaamisen-arvioijat
      (db/select-arvioijat-by-todennettu-arviointi-id id))
    :id)))

(defn get-olemassa-olevat-ammatilliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :tarkentavat-tiedot-arvioija
         (get-tarkentavat-tiedot-arvioija (:tarkentavat-tiedot-arvioija-id %))
         :tarkentavat-tiedot-naytto
         (get-ooato-tarkentavat-tiedot-naytto (:id %)))
       :tarkentavat-tiedot-arvioija-id)
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id
      hoks-id)))

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
    #(-> (assoc % :olemassa-olevat-ammatilliset-tutkinnon-osat
                (get-olemassa-olevat-ammatilliset-tutkinnon-osat (:id %)))
         set-puuttuvat-paikalliset-tutkinnon-osat
         set-olemassa-olevat-yhteiset-tutkinnon-osat)
    (db/select-hoks-by-oppija-oid oid)))

(defn save-ppto-osaamisen-hankkimistapa! [ppto oh]
  (let [tho (db/insert-tyopaikalla-hankittava-osaaminen!
              (:tyopaikalla-hankittava-osaaminen oh))
        o-db (db/insert-ppto-osaamisen-hankkimistapa!
               ppto
               (assoc oh :tyopaikalla-hankittava-osaaminen-id
                      (:id tho)))]
    (db/insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
      o-db (:muut-oppimisymparisto oh))
    (db/insert-puuttuvan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
      ppto o-db)
    o-db))

(defn save-ppto-osaamisen-hankkimistavat! [ppto c]
  (mapv #(save-ppto-osaamisen-hankkimistapa! ppto %) c))

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
  (let [ppto-db (db/insert-puuttuva-paikallinen-tutkinnon-osa!
                  (assoc ppto :hoks-id (:id h)))]
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

(defn save-ooato-tarkentavat-tiedot-naytto! [ttn]
  [])

(defn save-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta c]
  (mapv
    #(db/insert-todennettu-arviointi-arvioija! tta %)
    (db/insert-koulutuksen-jarjestaja-arvioijat! c)))

(defn save-ooato-tarkentavat-tiedot-arvioija! [m]
  (let [tta (db/insert-todennettu-arviointi-lisatiedot! m)]
    (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
      tta (:aiemmin-hankitun-osaamisen-arvioijat m))
    tta))

(defn save-olemassa-oleva-ammatillinen-tutkinnon-osa! [h ooato]
  (assoc
    (db/insert-olemassa-oleva-ammatillinen-tutkinnon-osa!
      (assoc ooato
             :hoks-id (:id h)
             :tarkentavat-tiedot-arvioija-id
             (:id (save-ooato-tarkentavat-tiedot-arvioija!
                    (:tarkentavat-tiedot-arvioija ooato)))))
    :tarkentavat-tiedot-naytto
    (save-ooato-tarkentavat-tiedot-naytto!
      (:tarkentavat-tiedot-naytto ooato))))

(defn save-olemassa-olevat-ammatilliset-tutkinnon-osat [h c]
  (mapv #(save-olemassa-oleva-ammatillinen-tutkinnon-osa! h %) c))

(defn save-hoks! [h]
  (let [saved-hoks (first (db/insert-hoks! h))]
    (db/insert-puuttuvat-paikalliset-tutkinnon-osat!
      (map
        #(assoc % :hoks-id (:id saved-hoks))
        (:puuttuvat-paikalliset-tutkinnon-osat h)))))
