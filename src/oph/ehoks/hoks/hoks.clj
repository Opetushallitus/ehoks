(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]))

(defn set-hankitun-osaamisen-naytto-values [naytto]
  (dissoc
    (assoc
      naytto
      :koulutuksen-jarjestaja-arvioijat
      (db/select-koulutuksen-jarjestaja-arvioijat-by-hon-id (:id naytto))
      :tyoelama-arvioijat
      (db/select-tyoelama-arvioijat-by-hon-id (:id naytto))
      :nayttoymparisto
      (db/select-nayttoymparisto-by-id (:nayttoymparisto-id naytto))
      :keskeiset-tyotehtavat-naytto
      (db/select-tyotehtavat-by-hankitun-osaamisen-naytto-id (:id naytto)))
    :nayttoymparisto-id))

(defn get-ooato-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-hankitun-osaamisen-naytto-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooato-id id)))

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

(defn set-osaamisen-hankkimistapa-values [m]
  (dissoc
    (assoc
      m
      :tyopaikalla-hankittava-osaaminen
      (get-tyopaikalla-hankittava-osaaminen
        (:tyopaikalla-hankittava-osaaminen-id m))
      :muut-oppimisymparisto
      (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
        (:id m)))
    :id :tyopaikalla-hankittava-osaaminen-id))

(defn get-osaamisen-hankkimistavat [id]
  (let [hankkimistavat (db/select-osaamisen-hankkimistavat-by-ppto-id id)]
    (mapv
      set-osaamisen-hankkimistapa-values
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

(defn get-ooyto-osa-alue-tarkentavat-tiedot [id]
  (mapv
    #(dissoc (set-hankitun-osaamisen-naytto-values %) :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooyto-osa-alue-id id)))

(defn get-ooyto-osa-alueet [id]
  (mapv
    #(dissoc
       (assoc
         %
         :tarkentavat-tiedot
         (get-ooyto-osa-alue-tarkentavat-tiedot (:id %)))
       :id)
    (db/select-osa-alueet-by-ooyto-id id)))

(defn get-ooyto-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-hankitun-osaamisen-naytto-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooyto-id id)))

(defn get-olemassa-olevat-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(-> %
         (assoc
           :tarkentavat-tiedot-naytto
           (get-ooyto-tarkentavat-tiedot-naytto (:id %))
           :osa-alueet
           (get-ooyto-osa-alueet (:id %)))
         (assoc-in
           [:tarkentavat-tiedot-arvioija :aiemmin-hankitun-osaamisen-arvioijat]
           (db/select-arvioija-by-ooyto-id (:id %)))
         (dissoc :id))
    (db/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-pato-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-pato-id id)))

(defn get-pato-hankitun-osaamisen-naytto [id]
  (mapv
    #(dissoc
       (set-hankitun-osaamisen-naytto-values %)
       :id)
    (db/select-hankitun-osaamisen-naytot-by-pato-id id)))

(defn get-puuttuvat-ammatilliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :hankitun-osaamisen-naytto
         (get-pato-hankitun-osaamisen-naytto (:id %))
         :osaamisen-hankkimistavat
         (get-pato-osaamisen-hankkimistavat (:id %)))
       :id)
    (db/select-puuttuvat-ammatilliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-hokses-by-oppija [oid]
  (mapv
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

(defn save-osaamisen-hankkimistapa! [oh]
  (let [tho (db/insert-tyopaikalla-hankittava-osaaminen!
              (:tyopaikalla-hankittava-osaaminen oh))
        o-db (db/insert-osaamisen-hankkimistapa!
               (assoc oh :tyopaikalla-hankittava-osaaminen-id
                      (:id tho)))]
    (db/insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
      o-db (:muut-oppimisymparisto oh))
    o-db))

(defn save-ppto-osaamisen-hankkimistapa! [ppto oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-puuttuvan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
      ppto o-db)
    o-db))

(defn save-ppto-osaamisen-hankkimistavat! [ppto c]
  (mapv #(save-ppto-osaamisen-hankkimistapa! ppto %) c))

(defn save-hankitun-osaamisen-nayton-tyoelama-arvioijat! [naytto arvioijat]
  (mapv
    #(let [arvioija (db/insert-tyoelama-arvioija! %)]
       (db/insert-hankitun-osaamisen-nayton-tyoelama-arvioija!
         naytto arvioija)
       arvioija)
    arvioijat))

(defn save-hankitun-osaamisen-naytto! [n]
  (let [nayttoymparisto (db/insert-nayttoymparisto! (:nayttoymparisto n))
        naytto (db/insert-hankitun-osaamisen-naytto!
                 (assoc n :nayttoymparisto-id (:id nayttoymparisto)))]
    (db/insert-hankitun-osaamisen-nayton-koulutuksen-jarjestaja-arvioijat!
      naytto (:koulutuksen-jarjestaja-arvioijat n))
    (save-hankitun-osaamisen-nayton-tyoelama-arvioijat!
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

(defn save-olemassa-olevat-paikalliset-tutkinnon-osat! [h c]
  (db/insert-olemassa-olevat-paikalliset-tutkinnon-osat!
    (mapv #(assoc % :hoks-id (:id h)) c)))

(defn save-ooyto-tarkentavat-tiedot-naytto! [ooyto c]
  (mapv
    #(let [n (save-hankitun-osaamisen-naytto! %)]
       (db/insert-ooyto-hankitun-osaamisen-naytto! ooyto n)
       n)
    c))

(defn save-ooyto-arvioijat! [yto-id arvioijat]
  (mapv
    #(let [a (db/insert-koulutuksen-jarjestaja-arvioija! %)]
       (db/insert-ooyto-arvioija! yto-id (:id a)))
    arvioijat))

(defn save-ooyto-osa-alueet! [yto-id osa-alueet]
  (mapv
    #(let [o (db/insert-olemassa-olevan-yhteisen-tutkinnon-osan-osa-alue!
               (assoc % :olemassa-oleva-yhteinen-tutkinnon-osa-id yto-id))]
       (mapv
         (fn [naytto]
           (let [n (save-hankitun-osaamisen-naytto! naytto)]
             (db/insert-ooyto-osa-alue-hankitun-osaamisen-naytto!
               (:id o) (:id n))))
         (:tarkentavat-tiedot %)))
    osa-alueet))

(defn save-olemassa-oleva-yhteinen-tutkinnon-osa! [o]
  (let [yto (db/insert-olemassa-oleva-yhteinen-tutkinnon-osa! o)]
    (save-ooyto-tarkentavat-tiedot-naytto! yto (:tarkentavat-tiedot-naytto o))
    (save-ooyto-arvioijat!
      (:id yto)
      (get-in
        o [:tarkentavat-tiedot-arvioija :aiemmin-hankitun-osaamisen-arvioijat]))
    (save-ooyto-osa-alueet! (:id yto) (:osa-alueet o))
    yto))

(defn save-olemassa-olevat-yhteiset-tutkinnon-osat! [h c]
  (mapv
    #(save-olemassa-oleva-yhteinen-tutkinnon-osa! (assoc % :hoks-id (:id h)))
    c))

(defn save-ooato-tarkentavat-tiedot-naytto! [ooato c]
  (mapv
    #(let [n (save-hankitun-osaamisen-naytto! %)]
       (db/insert-ooato-hankitun-osaamisen-naytto! ooato n)
       n)
    c))

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
  (let [ooato-db (db/insert-olemassa-oleva-ammatillinen-tutkinnon-osa!
                   (assoc ooato
                          :hoks-id (:id h)
                          :tarkentavat-tiedot-arvioija-id
                          (:id (save-ooato-tarkentavat-tiedot-arvioija!
                                 (:tarkentavat-tiedot-arvioija ooato)))))]
    (assoc
      ooato-db
      :tarkentavat-tiedot-naytto
      (save-ooato-tarkentavat-tiedot-naytto!
        ooato-db (:tarkentavat-tiedot-naytto ooato)))))

(defn save-olemassa-olevat-ammatilliset-tutkinnon-osat [h c]
  (mapv #(save-olemassa-oleva-ammatillinen-tutkinnon-osa! h %) c))

(defn save-pato-osaamisen-hankkimistapa! [pato oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-puuttuvan-ammatillisen-tutkinnon-osan-osaamisen-hankkimistapa!
      (:id pato) (:id o-db))
    o-db))

(defn save-pato-hankitun-osaamisen-naytto! [pato n]
  (let [naytto (save-hankitun-osaamisen-naytto! n)]
    (db/insert-pato-hankitun-osaamisen-naytto! (:id pato) (:id naytto))
    naytto))

(defn save-puuttuva-ammatillinen-tutkinnon-osa! [h pato]
  (let [pato-db (db/insert-puuttuva-ammatillinen-tutkinnon-osa!
                  (assoc pato :hoks-id (:id h)))]
    (assoc
      pato-db
      :hankitun-osaamisen-naytto
      (mapv
        #(save-pato-hankitun-osaamisen-naytto! pato-db %)
        (:hankitun-osaamisen-naytto pato))
      :osaamisen-hankkimistavat
      (mapv
        #(save-pato-osaamisen-hankkimistapa! pato-db %)
        (:osaamisen-hankkimistavat pato)))))

(defn save-puuttuvat-ammatilliset-tutkinnon-osat! [h c]
  (mapv #(save-puuttuva-ammatillinen-tutkinnon-osa! h %) c))

(defn save-hoks! [h]
  (let [saved-hoks (first (db/insert-hoks! h))]
    (db/insert-puuttuvat-paikalliset-tutkinnon-osat!
      (mapv
        #(assoc % :hoks-id (:id saved-hoks))
        (:puuttuvat-paikalliset-tutkinnon-osat h)))))
