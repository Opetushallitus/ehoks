(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]))

(defn set-osaamisen-osoittaminen-values [naytto]
  (dissoc
    (assoc
      naytto
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      (db/select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id
        (:id naytto))
      :tyoelama-osaamisen-arvioijat
      (db/select-tyoelama-osaamisen-arvioijat-by-hon-id (:id naytto))
      :nayttoymparisto
      (db/select-nayttoymparisto-by-id (:nayttoymparisto-id naytto))
      :sisallon-kuvaus
      (db/select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
        (:id naytto))
      :osa-alueet
      (db/select-osa-alueet-by-osaamisen-osoittaminen (:id naytto))
      :yksilolliset-kriteerit
      (db/select-osaamisen-osoittamisen-kriteerit-by-osaamisen-osoittaminen-id
        (:id naytto)))
    :nayttoymparisto-id))

(defn get-ahato-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooato-id id)))

(defn get-tarkentavat-tiedot-osaamisen-arvioija [ttoa-id]
  (let [tta (db/select-todennettu-arviointi-lisatiedot-by-id ttoa-id)]
    (dissoc
      (assoc
        tta
        :aiemmin-hankitun-osaamisen-arvioijat
        (db/select-arvioijat-by-todennettu-arviointi-id ttoa-id))
      :id)))

(defn- set-ahato-values [ahato]
  (dissoc
    (assoc
      ahato
      :tarkentavat-tiedot-osaamisen-arvioija
      (get-tarkentavat-tiedot-osaamisen-arvioija
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahato))
      :tarkentavat-tiedot-naytto
      (get-ahato-tarkentavat-tiedot-naytto (:id ahato)))
    :tarkentavat-tiedot-osaamisen-arvioija-id))

(defn get-aiemmin-hankittu-ammat-tutkinnon-osa [id]
  (when-let [ahato-from-db
             (db/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id id)]
    (set-ahato-values ahato-from-db)))

(defn get-aiemmin-hankitut-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ahato-values %) :id)
    (db/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-osaamisen-osoittaminen [id]
  (let [naytot (db/select-osaamisen-osoittamiset-by-ppto-id id)]
    (mapv
      #(dissoc (set-osaamisen-osoittaminen-values %) :id)
      naytot)))

(defn get-tyopaikalla-jarjestettava-koulutus [id]
  (let [o (db/select-tyopaikalla-jarjestettava-koulutus-by-id id)]
    (-> o
        (dissoc :id)
        (assoc :keskeiset-tyotehtavat
               (db/select-tyotehtavat-by-tho-id (:id o))))))

(defn set-osaamisen-hankkimistapa-values [m]
  (if  (some? (:tyopaikalla-jarjestettava-koulutus-id m))
    (dissoc
      (assoc
        m
        :tyopaikalla-jarjestettava-koulutus
        (get-tyopaikalla-jarjestettava-koulutus
          (:tyopaikalla-jarjestettava-koulutus-id m))
        :muut-oppimisymparistot
        (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
          (:id m)))
      :id :tyopaikalla-jarjestettava-koulutus-id)
    (dissoc
      (assoc
        m
        :muut-oppimisymparistot
        (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
          (:id m)))
      :id)))

(defn get-osaamisen-hankkimistavat [id]
  (let [hankkimistavat (db/select-osaamisen-hankkimistavat-by-hpto-id id)]
    (mapv
      set-osaamisen-hankkimistapa-values
      hankkimistavat)))

(defn get-hankittava-paikallinen-tutkinnon-osa [id]
  (assoc
    (db/select-hankittava-paikallinen-tutkinnon-osa-by-id id)
    :osaamisen-osoittaminen
    (get-osaamisen-osoittaminen id)
    :osaamisen-hankkimistavat
    (get-osaamisen-hankkimistavat id)))

(defn get-hankittavat-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-osoittaminen
         (get-osaamisen-osoittaminen (:id %))
         :osaamisen-hankkimistavat
         (get-osaamisen-hankkimistavat (:id %)))
       :id)
    (db/select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn- get-ahpto-tarkentavat-tiedot-naytto [ahpto-id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ahpto-id ahpto-id)))

(defn- set-ahpto-values [ahpto]
  (dissoc
    (assoc
      ahpto
      :tarkentavat-tiedot-osaamisen-arvioija
      (get-tarkentavat-tiedot-osaamisen-arvioija
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahpto))
      :tarkentavat-tiedot-naytto
      (get-ahpto-tarkentavat-tiedot-naytto (:id ahpto)))
    :tarkentavat-tiedot-osaamisen-arvioija-id))

(defn get-aiemmin-hankittu-paikallinen-tutkinnon-osa [id]
  (when-let [ahpto-from-db
             (db/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id id)]
    (set-ahpto-values ahpto-from-db)))

(defn get-aiemmin-hankitut-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ahpto-values %) :id)
    (db/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-ahyto-osa-alue-tarkentavat-tiedot [id]
  (mapv
    #(dissoc (set-osaamisen-osoittaminen-values %) :id)
    (db/select-tarkentavat-tiedot-naytto-by-ahyto-osa-alue-id id)))

(defn get-ahyto-osa-alueet [id]
  (mapv
    #(dissoc
       (assoc
         %
         :tarkentavat-tiedot-naytto
         (get-ahyto-osa-alue-tarkentavat-tiedot (:id %)))
       :id)
    (db/select-osa-alueet-by-ahyto-id id)))

(defn get-ahyto-tarkentavat-tiedot-naytto [ahyto-id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ahyto-id ahyto-id)))

(defn- set-ahyto-values [ahyto]
  (dissoc
    (assoc
      ahyto
      :osa-alueet
      (get-ahyto-osa-alueet (:id ahyto))
      :tarkentavat-tiedot-osaamisen-arvioija
      (get-tarkentavat-tiedot-osaamisen-arvioija
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahyto))
      :tarkentavat-tiedot-naytto
      (get-ahyto-tarkentavat-tiedot-naytto (:id ahyto)))
    :tarkentavat-tiedot-osaamisen-arvioija-id))

(defn get-aiemmin-hankittu-yhteinen-tutkinnon-osa [id]
  (when-let [ahyto-from-db
             (db/select-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id id)]
    (set-ahyto-values ahyto-from-db)))

(defn get-aiemmin-hankitut-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ahyto-values %) :id)
    (db/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-hato-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-hato-id id)))

(defn get-hato-osaamisen-osoittaminen [id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-osaamisen-osoittamiset-by-hato-id id)))

(defn get-hankittava-ammat-tutkinnon-osa [id]
  (when-let [hato-db (db/select-hankittava-ammat-tutkinnon-osa-by-id id)]
    (assoc
      hato-db
      :osaamisen-osoittaminen
      (get-hato-osaamisen-osoittaminen id)
      :osaamisen-hankkimistavat
      (get-hato-osaamisen-hankkimistavat id))))

(defn get-hankittavat-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-osoittaminen
         (get-hato-osaamisen-osoittaminen (:id %))
         :osaamisen-hankkimistavat
         (get-hato-osaamisen-hankkimistavat (:id %)))
       :id)
    (db/select-hankittavat-ammat-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-opiskeluvalmiuksia-tukeva-opinto [oto-id]
  (db/select-opiskeluvalmiuksia-tukevat-opinnot-by-id oto-id))

(defn get-opiskeluvalmiuksia-tukevat-opinnot [hoks-id]
  (mapv
    #(dissoc % :id)
    (db/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id hoks-id)))

(defn get-yto-osa-alue-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-hyto-osa-alue-id id)))

(defn get-yto-osa-alueen-osaamisen-osoittamiset [id]
  (mapv
    #(dissoc (set-osaamisen-osoittaminen-values %) :id)
    (db/select-osaamisen-osoittamiset-by-yto-osa-alue-id id)))

(defn get-yto-osa-alueet [hyto-id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-hankkimistavat
         (get-yto-osa-alue-osaamisen-hankkimistavat (:id %))
         :osaamisen-osoittaminen
         (get-yto-osa-alueen-osaamisen-osoittamiset (:id %)))
       :id :yhteinen-tutkinnon-osa-id)
    (db/select-yto-osa-alueet-by-yto-id hyto-id)))

(defn get-hankittava-yhteinen-tutkinnon-osa [hyto-id]
  (when-let [hato-db
             (db/select-hankittava-yhteinen-tutkinnon-osa-by-id hyto-id)]
    (assoc hato-db :osa-alueet (get-yto-osa-alueet hyto-id))))

(defn get-hankittavat-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc % :osa-alueet (get-yto-osa-alueet (:id %)))
       :id)
    (db/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-hoks-values [h]
  (let [id (:id h)]
    (assoc
      h
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (get-aiemmin-hankitut-ammat-tutkinnon-osat id)
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (get-aiemmin-hankitut-paikalliset-tutkinnon-osat id)
      :hankittavat-paikalliset-tutkinnon-osat
      (get-hankittavat-paikalliset-tutkinnon-osat id)
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (get-aiemmin-hankitut-yhteiset-tutkinnon-osat id)
      :hankittavat-ammat-tutkinnon-osat
      (get-hankittavat-ammat-tutkinnon-osat id)
      :opiskeluvalmiuksia-tukevat-opinnot
      (get-opiskeluvalmiuksia-tukevat-opinnot id)
      :hankittavat-yhteiset-tutkinnon-osat
      (get-hankittavat-yhteiset-tutkinnon-osat id))))

(defn get-hokses-by-oppija [oid]
  (mapv
    get-hoks-values
    (db/select-hoks-by-oppija-oid oid)))

(defn get-hoks-by-id [id]
  (get-hoks-values (db/select-hoks-by-id id)))

(defn save-osaamisen-hankkimistapa! [oh]
  (let [tho (db/insert-tyopaikalla-jarjestettava-koulutus!
              (:tyopaikalla-jarjestettava-koulutus oh))
        o-db (db/insert-osaamisen-hankkimistapa!
               (assoc oh :tyopaikalla-jarjestettava-koulutus-id
                      (:id tho)))]
    (db/insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
      o-db (:muut-oppimisymparistot oh))
    o-db))

(defn save-hpto-osaamisen-hankkimistapa! [hpto oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-hankittavan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
      hpto o-db)
    o-db))

(defn save-hpto-osaamisen-hankkimistavat! [hpto c]
  (mapv #(save-hpto-osaamisen-hankkimistapa! hpto %) c))

(defn replace-hpto-osaamisen-hankkimistavat! [hpto c]
  (db/delete-osaamisen-hankkimistavat-by-hpto-id! (:id hpto))
  (save-hpto-osaamisen-hankkimistavat! hpto c))

(defn save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
  [naytto arvioijat]
  (mapv
    #(let [arvioija (db/insert-tyoelama-arvioija! %)]
       (db/insert-osaamisen-osoittamisen-tyoelama-arvioija!
         naytto arvioija)
       arvioija)
    arvioijat))

(defn save-osaamisen-osoittamisen-osa-alueet! [n c]
  (mapv
    #(let [k (db/insert-koodisto-koodi! %)]
       (db/insert-osaamisen-osoittamisen-osa-alue! (:id n) (:id k))
       k)
    c))

(defn save-osaamisen-osoittaminen! [n]
  (let [nayttoymparisto (db/insert-nayttoymparisto! (:nayttoymparisto n))
        naytto (db/insert-osaamisen-osoittaminen!
                 (assoc n :nayttoymparisto-id (:id nayttoymparisto)))]
    (db/insert-osaamisen-osoittamisen-koulutuksen-jarjestaja-osaamisen-arvioija!
      naytto (:koulutuksen-jarjestaja-osaamisen-arvioijat n))
    (save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
      naytto (:tyoelama-osaamisen-arvioijat n))
    (db/insert-osaamisen-osoittamisen-sisallot!
      naytto (:sisallon-kuvaus n))
    (db/insert-osaamisen-osoittamisen-yksilolliset-kriteerit!
      naytto (:yksilolliset-kriteerit n))
    (save-osaamisen-osoittamisen-osa-alueet!
      naytto (:osa-alueet n))
    naytto))

(defn save-hpto-osaamisen-osoittaminen! [hpto n]
  (let [naytto (save-osaamisen-osoittaminen! n)]
    (db/insert-hpto-osaamisen-osoittaminen! hpto naytto)
    naytto))

(defn save-hpto-osaamisen-osoittamiset! [ppto c]
  (mapv
    #(save-hpto-osaamisen-osoittaminen! ppto %)
    c))

(defn replace-hpto-osaamisen-osoittamiset! [hpto c]
  (db/delete-osaamisen-osoittamiset-by-ppto-id! (:id hpto))
  (save-hpto-osaamisen-osoittamiset! hpto c))

(defn update-hankittava-paikallinen-tutkinnon-osa! [hpto-db values]
  (db/update-hankittava-paikallinen-tutkinnon-osa-by-id! (:id hpto-db) values)
  (cond-> hpto-db
    (:osaamisen-hankkimistavat values)
    (assoc :osaamisen-hankkimistavat
           (replace-hpto-osaamisen-hankkimistavat!
             hpto-db (:osaamisen-hankkimistavat values)))
    (:osaamisen-osoittaminen values)
    (assoc :osaamisen-osoittaminen
           (replace-hpto-osaamisen-osoittamiset!
             hpto-db (:osaamisen-osoittaminen values)))))

(defn save-hankittava-paikallinen-tutkinnon-osa! [hoks-id hpto]
  (let [hpto-db (db/insert-hankittava-paikallinen-tutkinnon-osa!
                  (assoc hpto :hoks-id hoks-id))]
    (assoc
      hpto-db
      :osaamisen-hankkimistavat
      (save-hpto-osaamisen-hankkimistavat!
        hpto-db (:osaamisen-hankkimistavat hpto))
      :osaamisen-osoittaminen
      (save-hpto-osaamisen-osoittamiset!
        hpto-db (:osaamisen-osoittaminen hpto)))))

(defn save-hankittavat-paikalliset-tutkinnon-osat! [hoks-id c]
  (mapv #(save-hankittava-paikallinen-tutkinnon-osa! hoks-id %) c))

(defn save-ahpto-tarkentavat-tiedot-naytto! [ahpto-id c]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-ahpto-osaamisen-osoittaminen! ahpto-id (:id n))
       n)
    c))

(defn save-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta-id new-arvioijat]
  (mapv
    #(db/insert-todennettu-arviointi-arvioijat! tta-id (:id %))
    (db/insert-koulutuksen-jarjestaja-osaamisen-arvioijat! new-arvioijat)))

(defn save-tarkentavat-tiedot-osaamisen-arvioija! [new-tta]
  (let [tta-db (db/insert-todennettu-arviointi-lisatiedot! new-tta)]
    (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
      (:id tta-db) (:aiemmin-hankitun-osaamisen-arvioijat new-tta))
    tta-db))

(defn save-aiemmin-hankittu-paikallinen-tutkinnon-osa! [hoks-id ahpto]
  (let [tta (:tarkentavat-tiedot-osaamisen-arvioija ahpto)
        ahpto-db (db/insert-aiemmin-hankittu-paikallinen-tutkinnon-osa!
                   (assoc ahpto
                          :hoks-id hoks-id
                          :tarkentavat-tiedot-osaamisen-arvioija-id
                          (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                 tta))))]
    (assoc
      ahpto-db
      :tarkentavat-tiedot-naytto
      (save-ahpto-tarkentavat-tiedot-naytto!
        (:id ahpto-db) (:tarkentavat-tiedot-naytto ahpto)))))

(defn save-aiemmin-hankitut-paikalliset-tutkinnon-osat! [hoks-id c]
  (mapv
    #(save-aiemmin-hankittu-paikallinen-tutkinnon-osa! hoks-id %)
    c))

(defn save-ahyto-tarkentavat-tiedot-naytto! [ahyto-id new-values]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-ahyto-osaamisen-osoittaminen! ahyto-id n)
       n)
    new-values))

(defn- save-ahyto-osa-alue! [ahyto-id osa-alue]
  (let [stored-osa-alue
        (db/insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue!
          (assoc osa-alue :aiemmin-hankittu-yhteinen-tutkinnon-osa-id
                 ahyto-id))]
    (mapv
      (fn [naytto]
        (let [stored-naytto (save-osaamisen-osoittaminen! naytto)]
          (db/insert-ooyto-osa-alue-osaamisen-osoittaminen!
            (:id stored-osa-alue) (:id stored-naytto))))
      (:tarkentavat-tiedot-naytto osa-alue))))

(defn save-ahyto-osa-alueet! [ahyto-id osa-alueet]
  (mapv
    #(save-ahyto-osa-alue! ahyto-id %)
    osa-alueet))

(defn save-aiemmin-hankittu-yhteinen-tutkinnon-osa! [hoks-id ahyto]
  (let [tta (:tarkentavat-tiedot-osaamisen-arvioija ahyto)
        yto (db/insert-aiemmin-hankittu-yhteinen-tutkinnon-osa!
              (assoc ahyto
                     :hoks-id hoks-id
                     :tarkentavat-tiedot-osaamisen-arvioija-id
                     (:id (save-tarkentavat-tiedot-osaamisen-arvioija! tta))))]
    (save-ahyto-tarkentavat-tiedot-naytto! (:id yto)
                                           (:tarkentavat-tiedot-naytto ahyto))
    (save-ahyto-osa-alueet! (:id yto) (:osa-alueet ahyto))
    yto))

(defn save-aiemmin-hankitut-yhteiset-tutkinnon-osat! [hoks-id c]
  (mapv
    #(save-aiemmin-hankittu-yhteinen-tutkinnon-osa! hoks-id %)
    c))

(defn save-ahato-tarkentavat-tiedot-naytto! [ahato-id new-values]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto!
         ahato-id n)
       n)
    new-values))

(defn  save-aiemmin-hankittu-ammat-tutkinnon-osa! [hoks-id ahato]
  (let [ahato-db (db/insert-aiemmin-hankittu-ammat-tutkinnon-osa!
                   (assoc ahato
                          :hoks-id hoks-id
                          :tarkentavat-tiedot-osaamisen-arvioija-id
                          (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                 (:tarkentavat-tiedot-osaamisen-arvioija
                                   ahato)))))]
    (assoc
      ahato-db
      :tarkentavat-tiedot-naytto
      (save-ahato-tarkentavat-tiedot-naytto!
        (:id ahato-db) (:tarkentavat-tiedot-naytto ahato)))))

(defn save-aiemmin-hankitut-ammat-tutkinnon-osat! [hoks-id c]
  (mapv #(save-aiemmin-hankittu-ammat-tutkinnon-osa! hoks-id %) c))

(defn save-hato-osaamisen-hankkimistapa! [hato oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
      (:id hato) (:id o-db))
    o-db))

(defn save-hato-osaamisen-hankkimistavat! [hato-db c]
  (mapv
    #(save-hato-osaamisen-hankkimistapa! hato-db %)
    c))

(defn save-hato-osaamisen-osoittaminen! [hato n]
  (let [naytto (save-osaamisen-osoittaminen! n)]
    (db/insert-hato-osaamisen-osoittaminen! (:id hato) (:id naytto))
    naytto))

(defn save-hato-osaamisen-osoittamiset! [hato-db c]
  (mapv
    #(save-hato-osaamisen-osoittaminen! hato-db %)
    c))

(defn save-hankittava-ammat-tutkinnon-osa! [hoks-id hato]
  (let [hato-db (db/insert-hankittava-ammat-tutkinnon-osa!
                  (assoc hato :hoks-id hoks-id))]
    (assoc
      hato-db
      :osaamisen-osoittaminen
      (mapv
        #(save-hato-osaamisen-osoittaminen! hato-db %)
        (:osaamisen-osoittaminen hato))
      :osaamisen-hankkimistavat
      (mapv
        #(save-hato-osaamisen-hankkimistapa! hato-db %)
        (:osaamisen-hankkimistavat hato)))))

(defn save-hankittavat-ammat-tutkinnon-osat! [hoks-id c]
  (mapv #(save-hankittava-ammat-tutkinnon-osa! hoks-id %) c))

(defn replace-hato-osaamisen-hankkimistavat! [hato c]
  (db/delete-osaamisen-hankkimistavat-by-hato-id! (:id hato))
  (save-hato-osaamisen-hankkimistavat! hato c))

(defn replace-hato-osaamisen-osoittamiset! [hato c]
  (db/delete-osaamisen-osoittamiset-by-pato-id! (:id hato))
  (save-hato-osaamisen-osoittamiset! hato c))

(defn update-hankittava-ammat-tutkinnon-osa! [hato-db values]
  (db/update-hankittava-ammat-tutkinnon-osa-by-id! (:id hato-db) values)
  (cond-> hato-db
    (:osaamisen-hankkimistavat values)
    (assoc :osaamisen-hankkimistavat
           (replace-hato-osaamisen-hankkimistavat!
             hato-db (:osaamisen-hankkimistavat values)))
    (:osaamisen-osoittaminen values)
    (assoc :osaamisen-osoittaminen
           (replace-hato-osaamisen-osoittamiset!
             hato-db (:osaamisen-osoittaminen values)))))

(defn- replace-ahato-tarkentavat-tiedot-naytto! [ahato-id new-values]
  (db/delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id! ahato-id)
  (save-ahato-tarkentavat-tiedot-naytto! ahato-id new-values))

(defn- replace-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta-id new-values]
  (db/delete-todennettu-arviointi-arvioijat-by-tta-id! tta-id)
  (save-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-values))

(defn- update-tarkentavat-tiedot-osaamisen-arvioija! [tta-id new-tta-values]
  (db/update-todennettu-arviointi-lisatiedot-by-id! tta-id new-tta-values)
  (when-let [new-arvioijat
             (:aiemmin-hankitun-osaamisen-arvioijat new-tta-values)]
    (replace-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-arvioijat)))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa!
  [ahato-from-db new-values]
  (db/update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id!
    (:id ahato-from-db) new-values)
  (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
    (update-tarkentavat-tiedot-osaamisen-arvioija!
      (:tarkentavat-tiedot-osaamisen-arvioija-id ahato-from-db) new-tta))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-ahato-tarkentavat-tiedot-naytto! (:id ahato-from-db) new-ttn)))

(defn- replace-ahpto-tarkentavat-tiedot-naytto! [ahpto-id new-values]
  (db/delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id! ahpto-id)
  (save-ahpto-tarkentavat-tiedot-naytto! ahpto-id new-values))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa!
  [ahpto-from-db new-values]
  (db/update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id!
    (:id ahpto-from-db) new-values)
  (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
    (update-tarkentavat-tiedot-osaamisen-arvioija!
      (:tarkentavat-tiedot-osaamisen-arvioija-id ahpto-from-db) new-tta))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-ahpto-tarkentavat-tiedot-naytto! (:id ahpto-from-db) new-ttn)))

(defn- replace-ahyto-tarkentavat-tiedot-naytto! [ahyto-id new-values]
  (db/delete-aiemmin-hankitun-yhteisen-tutkinnon-osan-naytto-by-id! ahyto-id)
  (save-ahyto-tarkentavat-tiedot-naytto! ahyto-id new-values))

(defn- replace-ahyto-osa-alueet! [ahyto-id new-values]
  (db/delete-aiemmin-hankitut-yto-osa-alueet-by-id! ahyto-id)
  (save-ahyto-osa-alueet! ahyto-id new-values))

(defn update-aiemmin-hankittu-yhteinen-tutkinnon-osa! [ahyto-from-db new-values]
  (db/update-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id!
    (:id ahyto-from-db) new-values)
  (when-let [new-ttoa (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
    (update-tarkentavat-tiedot-osaamisen-arvioija!
      (:tarkentavat-tiedot-osaamisen-arvioija-id ahyto-from-db) new-ttoa))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-ahyto-tarkentavat-tiedot-naytto! (:id ahyto-from-db) new-ttn))
  (when-let [new-oa (:osa-alueet new-values)]
    (replace-ahyto-osa-alueet! (:id ahyto-from-db) new-oa)))

(defn save-opiskeluvalmiuksia-tukeva-opinto! [hoks-id new-oto-values]
  (db/insert-opiskeluvalmiuksia-tukeva-opinto!
    (assoc new-oto-values :hoks-id hoks-id)))

(defn save-opiskeluvalmiuksia-tukevat-opinnot! [hoks-id new-oto-values]
  (db/insert-opiskeluvalmiuksia-tukevat-opinnot!
    (mapv #(assoc % :hoks-id hoks-id) new-oto-values)))

(defn save-yto-osa-alueen-osaamisen-osoittaminen! [yto n]
  (let [naytto (save-osaamisen-osoittaminen! n)
        yto-naytto (db/insert-yto-osa-alueen-osaamisen-osoittaminen!
                     (:id yto) (:id naytto))]
    yto-naytto))

(defn save-hyto-osa-alue-osaamisen-hankkimistapa! [hyto-osa-alue oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-hyto-osa-alueen-osaamisen-hankkimistapa!
      (:id hyto-osa-alue) (:id o-db))
    o-db))

(defn save-hyto-osa-alueet! [hyto-id osa-alueet]
  (mapv
    #(let [osa-alue-db (db/insert-yhteisen-tutkinnon-osan-osa-alue!
                         (assoc % :yhteinen-tutkinnon-osa-id hyto-id))]
       (assoc
         osa-alue-db
         :osaamisen-hankkimistavat
         (mapv
           (fn [oht]
             (save-hyto-osa-alue-osaamisen-hankkimistapa! osa-alue-db oht))
           (:osaamisen-hankkimistavat %))
         :osaamisen-osoittaminen
         (mapv
           (fn [hon]
             (save-yto-osa-alueen-osaamisen-osoittaminen! osa-alue-db hon))
           (:osaamisen-osoittaminen %))))
    osa-alueet))

(defn save-hankittava-yhteinen-tutkinnon-osa! [hoks-id hyto]
  (let [hyto-db (db/insert-hankittava-yhteinen-tutkinnon-osa!
                  (assoc hyto :hoks-id hoks-id))]
    (assoc hyto-db :osa-alueet
           (save-hyto-osa-alueet! (:id hyto-db) (:osa-alueet hyto)))))

(defn save-hankittavat-yhteiset-tutkinnon-osat! [hoks-id c]
  (mapv
    #(save-hankittava-yhteinen-tutkinnon-osa! hoks-id %)
    c))

(defn replace-hyto-osa-alueet! [hyto-id new-oa-values]
  (db/delete-hyto-osa-alueet! hyto-id)
  (save-hyto-osa-alueet! hyto-id new-oa-values))

(defn update-hankittava-yhteinen-tutkinnon-osa! [hyto-id new-values]
  (let [bare-hyto (dissoc new-values :osa-alueet)]
    (when (not-empty bare-hyto)
      (db/update-hankittava-yhteinen-tutkinnon-osa-by-id! hyto-id new-values)))
  (when-let [oa (:osa-alueet new-values)]
    (replace-hyto-osa-alueet! hyto-id oa)))

(defn save-hoks! [h]
  (let [saved-hoks (db/insert-hoks! h)]
    (assoc
      saved-hoks
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (save-aiemmin-hankitut-ammat-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-ammat-tutkinnon-osat h))
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-paikalliset-tutkinnon-osat h))
      :hankittavat-paikalliset-tutkinnon-osat
      (save-hankittavat-paikalliset-tutkinnon-osat!
        (:id saved-hoks) (:hankittavat-paikalliset-tutkinnon-osat h))
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-yhteiset-tutkinnon-osat h))
      :hankittavat-ammat-tutkinnon-osat
      (save-hankittavat-ammat-tutkinnon-osat!
        (:id saved-hoks) (:hankittavat-ammat-tutkinnon-osat h))
      :opiskeluvalmiuksia-tukevat-opinnot
      (save-opiskeluvalmiuksia-tukevat-opinnot!
        (:id saved-hoks) (:opiskeluvalmiuksia-tukevat-opinnot h))
      :hankittavat-yhteiset-tutkinnon-osat
      (save-hankittavat-yhteiset-tutkinnon-osat!
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

(defn- replace-main-hoks! [hoks-id new-values]
  (db/update-hoks-by-id! hoks-id (merge-not-given-hoks-values new-values)))

(defn- replace-oto! [hoks-id new-oto-values]
  (db/delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id hoks-id)
  (when
   new-oto-values
    (save-opiskeluvalmiuksia-tukevat-opinnot! hoks-id new-oto-values)))

(defn- replace-hato! [hoks-id new-hato-values]
  (db/delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-hato-values
    (save-hankittavat-ammat-tutkinnon-osat! hoks-id new-hato-values)))

(defn- replace-hpto! [hoks-id new-hpto-values]
  (db/delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-hpto-values
    (save-hankittavat-paikalliset-tutkinnon-osat! hoks-id new-hpto-values)))

(defn- replace-hyto! [hoks-id new-hyto-values]
  (db/delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-hyto-values
    (save-hankittavat-yhteiset-tutkinnon-osat! hoks-id new-hyto-values)))

(defn- replace-ahato! [hoks-id new-ahato-values]
  (db/delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-ahato-values
    (save-aiemmin-hankitut-ammat-tutkinnon-osat! hoks-id new-ahato-values)))

(defn- replace-ahpto! [hoks-id new-ahpto-values]
  (db/delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-ahpto-values
    (save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id new-ahpto-values)))

(defn- replace-ahyto! [hoks-id new-ahyto-values]
  (db/delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-ahyto-values
    (save-aiemmin-hankitut-yhteiset-tutkinnon-osat! hoks-id new-ahyto-values)))

(defn replace-hoks! [hoks-id new-values]
  (replace-main-hoks! hoks-id new-values)
  (replace-oto! hoks-id (:opiskeluvalmiuksia-tukevat-opinnot new-values))
  (replace-hato! hoks-id (:hankittavat-ammat-tutkinnon-osat new-values))
  (replace-hpto! hoks-id (:hankittavat-paikalliset-tutkinnon-osat new-values))
  (replace-hyto! hoks-id (:hankittavat-yhteiset-tutkinnon-osat new-values))
  (replace-ahato! hoks-id (:aiemmin-hankitut-ammat-tutkinnon-osat new-values))
  (replace-ahpto! hoks-id
                  (:aiemmin-hankitut-paikalliset-tutkinnon-osat new-values))
  (replace-ahyto! hoks-id
                  (:aiemmin-hankitut-yhteiset-tutkinnon-osat new-values)))

(defn update-hoks! [hoks-id new-values]
  (db/update-hoks-by-id! hoks-id new-values))
