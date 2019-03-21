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
       :tarkentavat-tiedot-arvioija-id :id)
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id
      hoks-id)))

(defn set-hankitun-osaamisen-naytto-values [naytto]
  (-> naytto
      (assoc
        :koulutuksen-jarjestaja-arvioijat
        (db/select-koulutuksen-jarjestaja-arvioijat-by-hon-id (:id naytto))
        :tyoelama-arvioijat
        (db/select-tyoelama-arvioijat-by-hon-id (:id naytto))
        :nayttoymparisto
        (db/select-nayttoymparisto-by-id (:nayttoymparisto-id naytto))
        :keskeiset-tyotehtavat-naytto
        (db/select-tyotehtavat-by-hankitun-osaamisen-naytto-id (:id naytto)))
      (dissoc :nayttoymparisto-id)))

(defn get-hankitun-osaamisen-naytto [id]
  (let [naytot (db/select-hankitun-osaamisen-naytot-by-ppto-id id)]
    (mapv
      set-hankitun-osaamisen-naytto-values
      naytot)))

(defn get-tyopaikalla-hankittava-osaaminen [id]
  (let [o (db/select-tyopaikalla-hankittava-osaaminen-by-id id)]
    (-> o
        (dissoc :id)
        (assoc :muut-osallistujat
               (db/select-henkilot-by-tho-id (:id o)))
        (assoc :keskeiset-tyotehtavat
               (db/select-tyotehtavat-by-tho-id (:id o))))))

(defn get-osaamisen-hankkimistavat [id]
  (let [hankkimistavat (db/select-osaamisen-hankkimistavat-by-ppto-id id)]
    (mapv
      #(dissoc
         (assoc
          %
          :tyopaikalla-hankittava-osaaminen
          (get-tyopaikalla-hankittava-osaaminen
            (:tyopaikalla-hankittava-osaaminen-id %))
          :muut-oppimisymparisto
          (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
            (:id %)))
         :id :tyopaikalla-hankittava-osaaminen-id)
      hankkimistavat)))

(defn get-puuttuvat-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(assoc
       %
       :hankitun-osaamisen-naytto
       (get-hankitun-osaamisen-naytto (:id %))
       :osaamisen-hankkimistavat
       (get-osaamisen-hankkimistavat (:id %)))
    (db/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-olemassa-olevat-paikalliset-tutkinnon-osat [hoks-id]
  (db/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id))

(defn get-olemassa-olevat-yhteiset-tutkinnon-osat [hoks-id]
  (db/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id))

(defn get-hokses-by-oppija [oid]
  (map
    #(assoc
       %
       :olemassa-olevat-ammatilliset-tutkinnon-osat
       (get-olemassa-olevat-ammatilliset-tutkinnon-osat (:id %))
       :puuttuvat-paikalliset-tutkinnon-osat
       (get-puuttuvat-paikalliset-tutkinnon-osat (:id %))
       :olemassa-olevat-yhteiset-tutkinnon-osat
       (get-olemassa-olevat-yhteiset-tutkinnon-osat (:id %))
       :olemassa-olevat-paikalliset-tutkinnon-osat
       (get-olemassa-olevat-paikalliset-tutkinnon-osat (:id %)))
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

(defn save-hankitun-osaamisen-naytto! [n]
  (let [nayttoymparisto (db/insert-nayttoymparisto! (:nayttoymparisto n))
        naytto (db/insert-hankitun-osaamisen-naytto!
                 (assoc n :nayttoymparisto-id (:id nayttoymparisto)))]
    (db/insert-hankitun-osaamisen-nayton-koulutuksen-jarjestaja-arvioijat!
      naytto (:koulutuksen-jarjestaja-arvioijat n))
    (db/insert-hankitun-osaamisen-nayton-tyoelama-arvioijat!
      naytto (:tyoelama-arvioijat n))
    (db/insert-hankitun-osaamisen-nayton-tyotehtavat!
      naytto (:keskeiset-tyotehtavat-naytto n))
    naytto))

(defn save-ppto-hankitun-osaamisen-naytto! [ppto n]
  (let [naytto (save-hankitun-osaamisen-naytto! n)]
    (db/insert-ppto-hankitun-osaamisen-naytto! ppto naytto)
    naytto))

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
