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
      (db/select-tyotehtavat-by-hankitun-osaamisen-naytto-id (:id naytto))
      :osa-alueet
      (db/select-osa-alueet-by-hankitun-osaamisen-naytto (:id naytto)))
    :nayttoymparisto-id))

(defn get-ooato-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-hankitun-osaamisen-naytto-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooato-id id)))

(defn get-tarkentavat-tiedot-arvioija [tta-id]
  (let [tta (db/select-todennettu-arviointi-lisatiedot-by-id tta-id)]
    (dissoc
      (assoc
        tta
        :aiemmin-hankitun-osaamisen-arvioijat
        (db/select-arvioijat-by-todennettu-arviointi-id tta-id))
      :id)))

(defn- set-ooato-values [ooato]
  (dissoc
    (assoc
      ooato
      :tarkentavat-tiedot-arvioija
      (get-tarkentavat-tiedot-arvioija (:tarkentavat-tiedot-arvioija-id ooato))
      :tarkentavat-tiedot-naytto
      (get-ooato-tarkentavat-tiedot-naytto (:id ooato)))
    :tarkentavat-tiedot-arvioija-id))

(defn get-olemassa-oleva-ammatillinen-tutkinnon-osa [id]
  (when-let [ooato-from-db
             (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-id id)]
    (set-ooato-values ooato-from-db)))

(defn get-olemassa-olevat-ammatilliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ooato-values %) :id)
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-hankitun-osaamisen-naytto [id]
  (let [naytot (db/select-hankitun-osaamisen-naytot-by-ppto-id id)]
    (mapv
      #(dissoc (set-hankitun-osaamisen-naytto-values %) :id)
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

(defn get-puuttuva-paikallinen-tutkinnon-osa [id]
  (assoc
    (db/select-puuttuva-paikallinen-tutkinnon-osa-by-id id)
    :hankitun-osaamisen-naytto
    (get-hankitun-osaamisen-naytto id)
    :osaamisen-hankkimistavat
    (get-osaamisen-hankkimistavat id)))

(defn get-puuttuvat-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :hankitun-osaamisen-naytto
         (get-hankitun-osaamisen-naytto (:id %))
         :osaamisen-hankkimistavat
         (get-osaamisen-hankkimistavat (:id %)))
       :id)
    (db/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn- get-oopto-tarkentavat-tiedot-naytto [oopto-id]
  (mapv
    #(dissoc
       (set-hankitun-osaamisen-naytto-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-oopto-id oopto-id)))

(defn- set-oopto-values [oopto]
  (dissoc
    (assoc
      oopto
      :tarkentavat-tiedot-arvioija
      (get-tarkentavat-tiedot-arvioija (:tarkentavat-tiedot-arvioija-id oopto))
      :tarkentavat-tiedot-naytto
      (get-oopto-tarkentavat-tiedot-naytto (:id oopto)))
    :tarkentavat-tiedot-arvioija-id))

(defn get-olemassa-olevat-paikallinen-tutkinnon-osa [id]
  (when-let [oopto-from-db
             (db/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-id id)]
    (set-oopto-values oopto-from-db)))

(defn get-olemassa-olevat-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-oopto-values %) :id)
    (db/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

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

(defn get-ooyto-tarkentavat-tiedot-naytto [ooyto-id]
  (mapv
    #(dissoc
       (set-hankitun-osaamisen-naytto-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooyto-id ooyto-id)))

(defn- set-ooyto-values [ooyto]
  (dissoc
    (assoc
      ooyto
      :osa-alueet
      (get-ooyto-osa-alueet (:id ooyto))
      :tarkentavat-tiedot-arvioija
      (get-tarkentavat-tiedot-arvioija (:tarkentavat-tiedot-arvioija-id ooyto))
      :tarkentavat-tiedot-naytto
      (get-ooyto-tarkentavat-tiedot-naytto (:id ooyto)))
    :tarkentavat-tiedot-arvioija-id))

(defn get-olemassa-olevat-yhteinen-tutkinnon-osa [id]
  (when-let [ooyto-from-db
             (db/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-id id)]
    (set-ooyto-values ooyto-from-db)))

(defn get-olemassa-olevat-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ooyto-values %) :id)
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

(defn get-puuttuva-ammatillinen-tutkinnon-osa [id]
  (when-let [pato-db (db/select-puuttuva-ammatillinen-tutkinnon-osa-by-id id)]
    (assoc
      pato-db
      :hankitun-osaamisen-naytto
      (get-pato-hankitun-osaamisen-naytto id)
      :osaamisen-hankkimistavat
      (get-pato-osaamisen-hankkimistavat id))))

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

(defn get-opiskeluvalmiuksia-tukevat-opinnot [hoks-id]
  (db/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id hoks-id))

(defn get-yto-osa-alue-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-pyto-osa-alue-id id)))

(defn get-yto-osa-alueen-hankitun-osaamisen-naytot [id]
  (mapv
    #(dissoc
       (assoc
         (set-hankitun-osaamisen-naytto-values %)
         :osaamistavoitteet
         (db/select-hankitun-yto-osaamisen-nayton-osaamistavoitteet (:id %)))
       :id)
    (db/select-hankitun-osaamisen-naytot-by-yto-osa-alue-id id)))

(defn get-yto-osa-alueet [id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-hankkimistavat
         (get-yto-osa-alue-osaamisen-hankkimistavat (:id %))
         :hankitun-osaamisen-naytto
         (get-yto-osa-alueen-hankitun-osaamisen-naytot (:id %)))
       :id :yhteinen-tutkinnon-osa-id)
    (db/select-yto-osa-alueet-by-yto-id id)))

(defn get-puuttuvat-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc % :osa-alueet (get-yto-osa-alueet (:id %)))
       :id)
    (db/select-puuttuvat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-hoks-values [h]
  (let [id (:id h)]
    (assoc
      h
      :olemassa-olevat-ammatilliset-tutkinnon-osat
      (get-olemassa-olevat-ammatilliset-tutkinnon-osat id)
      :olemassa-olevat-paikalliset-tutkinnon-osat
      (get-olemassa-olevat-paikalliset-tutkinnon-osat id)
      :puuttuvat-paikalliset-tutkinnon-osat
      (get-puuttuvat-paikalliset-tutkinnon-osat id)
      :olemassa-olevat-yhteiset-tutkinnon-osat
      (get-olemassa-olevat-yhteiset-tutkinnon-osat id)
      :puuttuvat-ammatilliset-tutkinnon-osat
      (get-puuttuvat-ammatilliset-tutkinnon-osat id)
      :opiskeluvalmiuksia-tukevat-opinnot
      (get-opiskeluvalmiuksia-tukevat-opinnot id)
      :puuttuvat-yhteiset-tutkinnon-osat
      (get-puuttuvat-yhteiset-tutkinnon-osat id))))

(defn get-hokses-by-oppija [oid]
  (mapv
    get-hoks-values
    (db/select-hoks-by-oppija-oid oid)))

(defn get-hoks-by-id [id]
  (get-hoks-values (db/select-hoks-by-id id)))

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

(defn replace-ppto-osaamisen-hankkimistavat! [ppto c]
  (db/delete-osaamisen-hankkimistavat-by-ppto-id! (:id ppto))
  (save-ppto-osaamisen-hankkimistavat! ppto c))

(defn save-hankitun-osaamisen-nayton-tyoelama-arvioijat! [naytto arvioijat]
  (mapv
    #(let [arvioija (db/insert-tyoelama-arvioija! %)]
       (db/insert-hankitun-osaamisen-nayton-tyoelama-arvioija!
         naytto arvioija)
       arvioija)
    arvioijat))

(defn save-hankitun-osaamisen-nayton-osa-alueet! [n c]
  (mapv
    #(let [k (db/insert-koodisto-koodi! %)]
       (db/insert-hankitun-osaamisen-nayton-osa-alue! (:id n) (:id k))
       k)
    c))

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
    (save-hankitun-osaamisen-nayton-osa-alueet!
      naytto (:osa-alueet n))
    naytto))

(defn save-ppto-hankitun-osaamisen-naytto! [ppto n]
  (let [naytto (save-hankitun-osaamisen-naytto! n)]
    (db/insert-ppto-hankitun-osaamisen-naytto! ppto naytto)
    naytto))

(defn save-ppto-hankitun-osaamisen-naytot! [ppto c]
  (mapv
    #(save-ppto-hankitun-osaamisen-naytto! ppto %)
    c))

(defn replace-ppto-hankitun-osaamisen-naytot! [ppto c]
  (db/delete-hankitun-osaamisen-naytot-by-ppto-id! (:id ppto))
  (save-ppto-hankitun-osaamisen-naytot! ppto c))

(defn update-puuttuva-paikallinen-tutkinnon-osa! [ppto-db values]
  (db/update-puuttuva-paikallinen-tutkinnon-osa-by-id! (:id ppto-db) values)
  (cond-> ppto-db
    (:osaamisen-hankkimistavat values)
    (assoc :osaamisen-hankkimistavat
           (replace-ppto-osaamisen-hankkimistavat!
             ppto-db (:osaamisen-hankkimistavat values)))
    (:hankitun-osaamisen-naytto values)
    (assoc :hankitun-osaamisen-naytto
           (replace-ppto-hankitun-osaamisen-naytot!
             ppto-db (:hankitun-osaamisen-naytto values)))))

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

(defn save-oopto-tarkentavat-tiedot-naytto! [oopto-id c]
  (mapv
    #(let [n (save-hankitun-osaamisen-naytto! %)]
       (db/insert-oopto-hankitun-osaamisen-naytto! oopto-id (:id n))
       n)
    c))

(defn save-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta-id new-arvioijat]
  (mapv
    #(db/insert-todennettu-arviointi-arvioijat! tta-id (:id %))
    (db/insert-koulutuksen-jarjestaja-arvioijat! new-arvioijat)))

(defn save-tarkentavat-tiedot-arvioija! [new-tta]
  (let [tta-db (db/insert-todennettu-arviointi-lisatiedot! new-tta)]
    (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
      (:id tta-db) (:aiemmin-hankitun-osaamisen-arvioijat new-tta))
    tta-db))

(defn save-olemassa-oleva-paikallinen-tutkinnon-osa! [hoks-id oopto]
  (let [tta (:tarkentavat-tiedot-arvioija oopto)
        oopto-db (db/insert-olemassa-oleva-paikallinen-tutkinnon-osa!
                   (assoc oopto
                          :hoks-id hoks-id
                          :tarkentavat-tiedot-arvioija-id
                          (:id (save-tarkentavat-tiedot-arvioija! tta))))]
    (assoc
      oopto-db
      :tarkentavat-tiedot-naytto
      (save-oopto-tarkentavat-tiedot-naytto!
        (:id oopto-db) (:tarkentavat-tiedot-naytto oopto)))))

(defn save-olemassa-olevat-paikalliset-tutkinnon-osat! [hoks c]
  (mapv
    #(save-olemassa-oleva-paikallinen-tutkinnon-osa! (:id hoks) %)
    c))

(defn save-ooyto-tarkentavat-tiedot-naytto! [ooyto c]
  (mapv
    #(let [n (save-hankitun-osaamisen-naytto! %)]
       (db/insert-ooyto-hankitun-osaamisen-naytto! ooyto n)
       n)
    c))

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

(defn save-olemassa-oleva-yhteinen-tutkinnon-osa! [hoks-id ooyto]
  (let [tta (:tarkentavat-tiedot-arvioija ooyto)
        yto (db/insert-olemassa-oleva-yhteinen-tutkinnon-osa!
              (assoc ooyto
                :hoks-id hoks-id
                :tarkentavat-tiedot-arvioija-id
                (:id (save-tarkentavat-tiedot-arvioija! tta))))]
    (save-ooyto-tarkentavat-tiedot-naytto! yto
                                           (:tarkentavat-tiedot-naytto ooyto))
    (save-ooyto-osa-alueet! (:id yto) (:osa-alueet ooyto))
    yto))

(defn save-olemassa-olevat-yhteiset-tutkinnon-osat! [hoks c]
  (mapv
    #(save-olemassa-oleva-yhteinen-tutkinnon-osa! (:id hoks) %)
    c))

(defn save-ooato-tarkentavat-tiedot-naytto! [ooato-id new-values]
  (mapv
    #(let [n (save-hankitun-osaamisen-naytto! %)]
       (db/insert-olemassa-olevan-ammatillisen-tutkinnon-osan-naytto!
         ooato-id n)
       n)
    new-values))

(defn save-olemassa-oleva-ammatillinen-tutkinnon-osa! [hoks-id ooato]
  (let [ooato-db (db/insert-olemassa-oleva-ammatillinen-tutkinnon-osa!
                   (assoc ooato
                          :hoks-id hoks-id
                          :tarkentavat-tiedot-arvioija-id
                          (:id (save-tarkentavat-tiedot-arvioija!
                                 (:tarkentavat-tiedot-arvioija ooato)))))]
    (assoc
      ooato-db
      :tarkentavat-tiedot-naytto
      (save-ooato-tarkentavat-tiedot-naytto!
        (:id ooato-db) (:tarkentavat-tiedot-naytto ooato)))))

(defn save-olemassa-olevat-ammatilliset-tutkinnon-osat! [h c]
  (mapv #(save-olemassa-oleva-ammatillinen-tutkinnon-osa! (:id h) %) c))

(defn save-pato-osaamisen-hankkimistapa! [pato oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-puuttuvan-ammatillisen-tutkinnon-osan-osaamisen-hankkimistapa!
      (:id pato) (:id o-db))
    o-db))

(defn save-pato-osaamisen-hankkimistavat! [pato-db c]
  (mapv
    #(save-pato-osaamisen-hankkimistapa! pato-db %)
    c))

(defn save-pato-hankitun-osaamisen-naytto! [pato n]
  (let [naytto (save-hankitun-osaamisen-naytto! n)]
    (db/insert-pato-hankitun-osaamisen-naytto! (:id pato) (:id naytto))
    naytto))

(defn save-pato-hankitun-osaamisen-naytot! [pato-db c]
  (mapv
    #(save-pato-hankitun-osaamisen-naytto! pato-db %)
    c))

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

(defn replace-pato-osaamisen-hankkimistavat! [pato c]
  (db/delete-osaamisen-hankkimistavat-by-pato-id! (:id pato))
  (save-pato-osaamisen-hankkimistavat! pato c))

(defn replace-pato-hankitun-osaamisen-naytot! [pato c]
  (db/delete-hankitun-osaamisen-naytot-by-pato-id! (:id pato))
  (save-pato-hankitun-osaamisen-naytot! pato c))

(defn update-puuttuva-ammatillinen-tutkinnon-osa! [pato-db values]
  (db/update-puuttuva-ammatillinen-tutkinnon-osa-by-id! (:id pato-db) values)
  (cond-> pato-db
    (:osaamisen-hankkimistavat values)
    (assoc :osaamisen-hankkimistavat
           (replace-pato-osaamisen-hankkimistavat!
             pato-db (:osaamisen-hankkimistavat values)))
    (:hankitun-osaamisen-naytto values)
    (assoc :hankitun-osaamisen-naytto
           (replace-pato-hankitun-osaamisen-naytot!
             pato-db (:hankitun-osaamisen-naytto values)))))

(defn- replace-ooato-tarkentavat-tiedot-naytto! [ooato-id new-values]
  (db/delete-olemassa-olevan-ammatillisen-tutkinnon-osan-naytto-by-id! ooato-id)
  (save-ooato-tarkentavat-tiedot-naytto! ooato-id new-values))

(defn- replace-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta-id new-values]
  (db/delete-todennettu-arviointi-arvioijat-by-tta-id! tta-id)
  (save-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-values))

(defn- update-tarkentavat-tiedot-arvioija! [tta-id new-tta-values]
  (db/update-todennettu-arviointi-lisatiedot-by-id! tta-id new-tta-values)
  (when-let [new-arvioijat
             (:aiemmin-hankitun-osaamisen-arvioijat new-tta-values)]
    (replace-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-arvioijat)))

(defn update-olemassa-oleva-ammatillinen-tutkinnon-osa!
  [ooato-from-db new-values]
  (db/update-olemassa-oleva-ammatillinen-tutkinnon-osat-by-id!
    (:id ooato-from-db) new-values)
  (when-let [new-tta (:tarkentavat-tiedot-arvioija new-values)]
    (update-tarkentavat-tiedot-arvioija!
      (:tarkentavat-tiedot-arvioija-id ooato-from-db) new-tta))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-ooato-tarkentavat-tiedot-naytto! (:id ooato-from-db) new-ttn)))

(defn- replace-oopto-tarkentavat-tiedot-naytto! [oopto-id new-values]
  (db/delete-olemassa-olevan-paikallisen-tutkinnon-osan-naytto-by-id! oopto-id)
  (save-oopto-tarkentavat-tiedot-naytto! oopto-id new-values))

(defn update-olemassa-oleva-paikallinen-tutkinnon-osa!
  [oopto-from-db new-values]
  (db/update-olemassa-oleva-paikallinen-tutkinnon-osat-by-id!
    (:id oopto-from-db) new-values)
  (when-let [new-tta (:tarkentavat-tiedot-arvioija new-values)]
    (update-tarkentavat-tiedot-arvioija!
      (:tarkentavat-tiedot-arvioija-id oopto-from-db) new-tta))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-oopto-tarkentavat-tiedot-naytto! (:id oopto-from-db) new-ttn)))

(defn save-opiskeluvalmiuksia-tukevat-opinnot! [h c]
  (db/insert-opiskeluvalmiuksia-tukevat-opinnot!
    (mapv #(assoc % :hoks-id (:id h)) c)))

(defn save-yto-osa-alueen-hankitun-osaamisen-naytto! [yto n]
  (let [naytto (save-hankitun-osaamisen-naytto! n)
        yto-naytto (db/insert-yto-osa-alueen-hankitun-osaamisen-naytto!
                     (:id yto) (:id naytto))]
    (db/insert-hankitun-yto-osaamisen-nayton-osaamistavoitteet!
      (:id yto) (:id naytto) (:osaamistavoitteet n))
    yto-naytto))

(defn save-pyto-osa-alue-osaamisen-hankkimistapa! [pyto-osa-alue oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-pyto-osa-alueen-osaamisen-hankkimistapa!
      (:id pyto-osa-alue) (:id o-db))
    o-db))

(defn save-pyto-osa-alueet! [pyto-id osa-alueet]
  (mapv
    #(let [o (db/insert-yhteisen-tutkinnon-osan-osa-alue!
               (assoc % :yhteinen-tutkinnon-osa-id pyto-id))]
       (assoc
         o
         :osaamisen-hankkimistavat
         (mapv
           (fn [oht]
             (save-pyto-osa-alue-osaamisen-hankkimistapa! o oht))
           (:osaamisen-hankkimistavat %))
         :hankitun-osaamisen-naytto
         (mapv
           (fn [hon]
             (save-yto-osa-alueen-hankitun-osaamisen-naytto! o hon))
           (:hankitun-osaamisen-naytto %))))
    osa-alueet))

(defn save-puuttuva-yhteinen-tutkinnon-osa! [h pyto]
  (let [p-db (db/insert-puuttuva-yhteinen-tutkinnon-osa!
               (assoc pyto :hoks-id (:id h)))]
    (assoc p-db
           :osa-alueet (save-pyto-osa-alueet! (:id p-db) (:osa-alueet pyto)))))

(defn save-puuttuvat-yhteiset-tutkinnon-osat! [h c]
  (mapv
    #(save-puuttuva-yhteinen-tutkinnon-osa! h %)
    c))

(defn save-hoks! [h]
  (let [saved-hoks (db/insert-hoks! h)]
    (assoc
      saved-hoks
      :olemassa-olevat-ammatilliset-tutkinnon-osat
      (save-olemassa-olevat-ammatilliset-tutkinnon-osat!
        saved-hoks (:olemassa-olevat-ammatilliset-tutkinnon-osat h))
      :olemassa-olevat-paikalliset-tutkinnon-osat
      (save-olemassa-olevat-paikalliset-tutkinnon-osat!
        saved-hoks (:olemassa-olevat-paikalliset-tutkinnon-osat h))
      :puuttuvat-paikalliset-tutkinnon-osat
      (save-puuttuvat-paikalliset-tutkinnon-osat!
        saved-hoks (:puuttuvat-paikalliset-tutkinnon-osat h))
      :olemassa-olevat-yhteiset-tutkinnon-osat
      (save-olemassa-olevat-yhteiset-tutkinnon-osat!
        saved-hoks (:olemassa-olevat-yhteiset-tutkinnon-osat h))
      :puuttuvat-ammatilliset-tutkinnon-osat
      (save-puuttuvat-ammatilliset-tutkinnon-osat!
        saved-hoks (:puuttuvat-ammatilliset-tutkinnon-osat h))
      :opiskeluvalmiuksia-tukevat-opinnot
      (save-opiskeluvalmiuksia-tukevat-opinnot!
        saved-hoks (:opiskeluvalmiuksia-tukevat-opinnot h))
      :puuttuvat-yhteiset-tutkinnon-osat
      (save-puuttuvat-yhteiset-tutkinnon-osat!
        saved-hoks (:puuttuvat-yhteiset-tutkinnon-osat h)))))
