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

(defn get-tarkentavat-tiedot-arvioija [id]
  (let [tta (db/select-todennettu-arviointi-lisatiedot-by-id id)]
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

(defn get-oopto-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-hankitun-osaamisen-naytto-values %)
       :id)
    (db/select-hankitun-osaamisen-naytto-by-oopto-id id)))

(defn get-olemassa-olevat-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc-in
         (assoc
           %
           :tarkentavat-tiedot-naytto
           (get-oopto-tarkentavat-tiedot-naytto (:id %)))
         [:tarkentavat-tiedot-arvioija :aiemmin-hankitun-osaamisen-arvioijat]
         (db/select-arvioijat-by-oopto-id (:id %)))
       :id)
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

(defn delete-hoks-by-id! [hoks-id]
  (db/delete-hoks! hoks-id))

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

(defn save-oopto-arvioijat! [oopto-id arvioijat]
  (mapv
    #(let [a (db/insert-koulutuksen-jarjestaja-arvioija! %)]
       (db/insert-oopto-arvioija! oopto-id (:id a)))
    arvioijat))

(defn save-olemassa-oleva-paikallinen-tutkinnon-osa! [oopto]
  (let [oopto-db (db/insert-olemassa-oleva-paikallinen-tutkinnon-osa! oopto)]
    (assoc
      oopto-db
      :tarkentavat-tiedot-arvioija
      {:aiemmin-hankitun-osaamisen-arvioijat
       (save-oopto-arvioijat!
         (:id oopto-db)
         (get-in
           oopto
           [:tarkentavat-tiedot-arvioija
            :aiemmin-hankitun-osaamisen-arvioijat]))}
      :tarkentavat-tiedot-naytto
      (save-oopto-tarkentavat-tiedot-naytto!
        (:id oopto-db) (:tarkentavat-tiedot-naytto oopto)))))

(defn save-olemassa-olevat-paikalliset-tutkinnon-osat! [h c]
  (mapv
    #(save-olemassa-oleva-paikallinen-tutkinnon-osa!
       (assoc % :hoks-id (:id h)))
    c))

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

(defn save-olemassa-olevat-ammatilliset-tutkinnon-osat! [h c]
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
