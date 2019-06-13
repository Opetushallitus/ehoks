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

(defn get-ooato-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooato-id id)))

(defn get-tarkentavat-tiedot-osaamisen-arvioija [tta-id]
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
      :tarkentavat-tiedot-osaamisen-arvioija
      (get-tarkentavat-tiedot-osaamisen-arvioija
        (:tarkentavat-tiedot-osaamisen-arvioija-id ooato))
      :tarkentavat-tiedot-naytto
      (get-ooato-tarkentavat-tiedot-naytto (:id ooato)))
    :tarkentavat-tiedot-osaamisen-arvioija-id))

(defn get-aiemmin-hankittu-ammat-tutkinnon-osa [id]
  (when-let [ooato-from-db
             (db/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id id)]
    (set-ooato-values ooato-from-db)))

(defn get-aiemmin-hankitut-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ooato-values %) :id)
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
  (let [hankkimistavat (db/select-osaamisen-hankkimistavat-by-ppto-id id)]
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

(defn- get-oopto-tarkentavat-tiedot-naytto [oopto-id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-oopto-id oopto-id)))

(defn- set-oopto-values [oopto]
  (dissoc
    (assoc
      oopto
      :tarkentavat-tiedot-osaamisen-arvioija
      (get-tarkentavat-tiedot-osaamisen-arvioija
        (:tarkentavat-tiedot-osaamisen-arvioija-id oopto))
      :tarkentavat-tiedot-naytto
      (get-oopto-tarkentavat-tiedot-naytto (:id oopto)))
    :tarkentavat-tiedot-osaamisen-arvioija-id))

(defn get-aiemmin-hankittu-paikallinen-tutkinnon-osa [id]
  (when-let [oopto-from-db
             (db/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id id)]
    (set-oopto-values oopto-from-db)))

(defn get-aiemmin-hankitut-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-oopto-values %) :id)
    (db/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-ooyto-osa-alue-tarkentavat-tiedot [id]
  (mapv
    #(dissoc (set-osaamisen-osoittaminen-values %) :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooyto-osa-alue-id id)))

(defn get-ooyto-osa-alueet [id]
  (mapv
    #(dissoc
       (assoc
         %
         :tarkentavat-tiedot-naytto
         (get-ooyto-osa-alue-tarkentavat-tiedot (:id %)))
       :id)
    (db/select-osa-alueet-by-ooyto-id id)))

(defn get-ooyto-tarkentavat-tiedot-naytto [ooyto-id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooyto-id ooyto-id)))

(defn- set-ooyto-values [ooyto]
  (dissoc
    (assoc
      ooyto
      :osa-alueet
      (get-ooyto-osa-alueet (:id ooyto))
      :tarkentavat-tiedot-osaamisen-arvioija
      (get-tarkentavat-tiedot-osaamisen-arvioija
        (:tarkentavat-tiedot-osaamisen-arvioija-id ooyto))
      :tarkentavat-tiedot-naytto
      (get-ooyto-tarkentavat-tiedot-naytto (:id ooyto)))
    :tarkentavat-tiedot-osaamisen-arvioija-id))

(defn get-aiemmin-hankittu-yhteinen-tutkinnon-osa [id]
  (when-let [ooyto-from-db
             (db/select-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id id)]
    (set-ooyto-values ooyto-from-db)))

(defn get-aiemmin-hankitut-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ooyto-values %) :id)
    (db/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-pato-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-pato-id id)))

(defn get-pato-osaamisen-osoittaminen [id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-osaamisen-osoittamiset-by-pato-id id)))

(defn get-hankittava-ammat-tutkinnon-osa [id]
  (when-let [pato-db (db/select-hankittava-ammat-tutkinnon-osa-by-id id)]
    (assoc
      pato-db
      :osaamisen-osoittaminen
      (get-pato-osaamisen-osoittaminen id)
      :osaamisen-hankkimistavat
      (get-pato-osaamisen-hankkimistavat id))))

(defn get-hankittavat-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-osoittaminen
         (get-pato-osaamisen-osoittaminen (:id %))
         :osaamisen-hankkimistavat
         (get-pato-osaamisen-hankkimistavat (:id %)))
       :id)
    (db/select-hankittavat-ammat-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-opiskeluvalmiuksia-tukevat-opinnot [hoks-id]
  (db/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id hoks-id))

(defn get-yto-osa-alue-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-pyto-osa-alue-id id)))

(defn get-yto-osa-alueen-osaamisen-osoittamiset [id]
  (mapv
    #(dissoc (set-osaamisen-osoittaminen-values %) :id)
    (db/select-osaamisen-osoittamiset-by-yto-osa-alue-id id)))

(defn get-yto-osa-alueet [id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-hankkimistavat
         (get-yto-osa-alue-osaamisen-hankkimistavat (:id %))
         :osaamisen-osoittaminen
         (get-yto-osa-alueen-osaamisen-osoittamiset (:id %)))
       :id :yhteinen-tutkinnon-osa-id)
    (db/select-yto-osa-alueet-by-yto-id id)))

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

(defn save-ppto-osaamisen-hankkimistapa! [ppto oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-hankittavan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
      ppto o-db)
    o-db))

(defn save-ppto-osaamisen-hankkimistavat! [ppto c]
  (mapv #(save-ppto-osaamisen-hankkimistapa! ppto %) c))

(defn replace-ppto-osaamisen-hankkimistavat! [ppto c]
  (db/delete-osaamisen-hankkimistavat-by-ppto-id! (:id ppto))
  (save-ppto-osaamisen-hankkimistavat! ppto c))

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

(defn save-ppto-osaamisen-osoittaminen! [ppto n]
  (let [naytto (save-osaamisen-osoittaminen! n)]
    (db/insert-ppto-osaamisen-osoittaminen! ppto naytto)
    naytto))

(defn save-ppto-osaamisen-osoittamiset! [ppto c]
  (mapv
    #(save-ppto-osaamisen-osoittaminen! ppto %)
    c))

(defn replace-ppto-osaamisen-osoittamiset! [ppto c]
  (db/delete-osaamisen-osoittamiset-by-ppto-id! (:id ppto))
  (save-ppto-osaamisen-osoittamiset! ppto c))

(defn update-hankittava-paikallinen-tutkinnon-osa! [ppto-db values]
  (db/update-hankittava-paikallinen-tutkinnon-osa-by-id! (:id ppto-db) values)
  (cond-> ppto-db
    (:osaamisen-hankkimistavat values)
    (assoc :osaamisen-hankkimistavat
           (replace-ppto-osaamisen-hankkimistavat!
             ppto-db (:osaamisen-hankkimistavat values)))
    (:osaamisen-osoittaminen values)
    (assoc :osaamisen-osoittaminen
           (replace-ppto-osaamisen-osoittamiset!
             ppto-db (:osaamisen-osoittaminen values)))))

(defn save-hankittava-paikallinen-tutkinnon-osa! [h ppto]
  (let [ppto-db (db/insert-hankittava-paikallinen-tutkinnon-osa!
                  (assoc ppto :hoks-id (:id h)))]
    (assoc
      ppto-db
      :osaamisen-hankkimistavat
      (save-ppto-osaamisen-hankkimistavat!
        ppto-db (:osaamisen-hankkimistavat ppto))
      :osaamisen-osoittaminen
      (save-ppto-osaamisen-osoittamiset!
        ppto-db (:osaamisen-osoittaminen ppto)))))

(defn save-hankittavat-paikalliset-tutkinnon-osat! [h c]
  (mapv #(save-hankittava-paikallinen-tutkinnon-osa! h %) c))

(defn save-oopto-tarkentavat-tiedot-naytto! [oopto-id c]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-oopto-osaamisen-osoittaminen! oopto-id (:id n))
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

(defn save-aiemmin-hankittu-paikallinen-tutkinnon-osa! [hoks-id oopto]
  (let [tta (:tarkentavat-tiedot-osaamisen-arvioija oopto)
        oopto-db (db/insert-aiemmin-hankittu-paikallinen-tutkinnon-osa!
                   (assoc oopto
                          :hoks-id hoks-id
                          :tarkentavat-tiedot-osaamisen-arvioija-id
                          (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                 tta))))]
    (assoc
      oopto-db
      :tarkentavat-tiedot-naytto
      (save-oopto-tarkentavat-tiedot-naytto!
        (:id oopto-db) (:tarkentavat-tiedot-naytto oopto)))))

(defn save-aiemmin-hankitut-paikalliset-tutkinnon-osat! [hoks c]
  (mapv
    #(save-aiemmin-hankittu-paikallinen-tutkinnon-osa! (:id hoks) %)
    c))

(defn save-ahyto-tarkentavat-tiedot-naytto! [ahyto-id new-values]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-ahyto-osaamisen-osoittaminen! ahyto-id n)
       n)
    new-values))

(defn save-ahyto-osa-alueet! [yto-id osa-alueet]
  (mapv
    #(let [o (db/insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue!
               (assoc % :aiemmin-hankittu-yhteinen-tutkinnon-osa-id yto-id))]
       (mapv
         (fn [naytto]
           (let [n (save-osaamisen-osoittaminen! naytto)]
             (db/insert-ooyto-osa-alue-osaamisen-osoittaminen!
               (:id o) (:id n))))
         (:tarkentavat-tiedot-naytto %)))
    osa-alueet))

(defn save-aiemmin-hankittu-yhteinen-tutkinnon-osa! [hoks-id ooyto]
  (let [tta (:tarkentavat-tiedot-osaamisen-arvioija ooyto)
        yto (db/insert-aiemmin-hankittu-yhteinen-tutkinnon-osa!
              (assoc ooyto
                     :hoks-id hoks-id
                     :tarkentavat-tiedot-osaamisen-arvioija-id
                     (:id (save-tarkentavat-tiedot-osaamisen-arvioija! tta))))]
    (save-ahyto-tarkentavat-tiedot-naytto! (:id yto)
                                           (:tarkentavat-tiedot-naytto ooyto))
    (save-ahyto-osa-alueet! (:id yto) (:osa-alueet ooyto))
    yto))

(defn save-aiemmin-hankitut-yhteiset-tutkinnon-osat! [hoks c]
  (mapv
    #(save-aiemmin-hankittu-yhteinen-tutkinnon-osa! (:id hoks) %)
    c))

(defn save-ooato-tarkentavat-tiedot-naytto! [ooato-id new-values]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto!
         ooato-id n)
       n)
    new-values))

(defn  save-aiemmin-hankittu-ammat-tutkinnon-osa! [hoks-id ooato]
  (let [ooato-db (db/insert-aiemmin-hankittu-ammat-tutkinnon-osa!
                   (assoc ooato
                          :hoks-id hoks-id
                          :tarkentavat-tiedot-osaamisen-arvioija-id
                          (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                 (:tarkentavat-tiedot-osaamisen-arvioija
                                   ooato)))))]
    (assoc
      ooato-db
      :tarkentavat-tiedot-naytto
      (save-ooato-tarkentavat-tiedot-naytto!
        (:id ooato-db) (:tarkentavat-tiedot-naytto ooato)))))

(defn save-aiemmin-hankitut-ammat-tutkinnon-osat! [h c]
  (mapv #(save-aiemmin-hankittu-ammat-tutkinnon-osa! (:id h) %) c))

(defn save-pato-osaamisen-hankkimistapa! [pato oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
      (:id pato) (:id o-db))
    o-db))

(defn save-pato-osaamisen-hankkimistavat! [pato-db c]
  (mapv
    #(save-pato-osaamisen-hankkimistapa! pato-db %)
    c))

(defn save-pato-osaamisen-osoittaminen! [pato n]
  (let [naytto (save-osaamisen-osoittaminen! n)]
    (db/insert-pato-osaamisen-osoittaminen! (:id pato) (:id naytto))
    naytto))

(defn save-pato-osaamisen-osoittamiset! [pato-db c]
  (mapv
    #(save-pato-osaamisen-osoittaminen! pato-db %)
    c))

(defn save-hankittava-ammat-tutkinnon-osa! [h pato]
  (let [pato-db (db/insert-hankittava-ammat-tutkinnon-osa!
                  (assoc pato :hoks-id (:id h)))]
    (assoc
      pato-db
      :osaamisen-osoittaminen
      (mapv
        #(save-pato-osaamisen-osoittaminen! pato-db %)
        (:osaamisen-osoittaminen pato))
      :osaamisen-hankkimistavat
      (mapv
        #(save-pato-osaamisen-hankkimistapa! pato-db %)
        (:osaamisen-hankkimistavat pato)))))

(defn save-hankittavat-ammat-tutkinnon-osat! [h c]
  (mapv #(save-hankittava-ammat-tutkinnon-osa! h %) c))

(defn replace-pato-osaamisen-hankkimistavat! [pato c]
  (db/delete-osaamisen-hankkimistavat-by-pato-id! (:id pato))
  (save-pato-osaamisen-hankkimistavat! pato c))

(defn replace-pato-osaamisen-osoittamiset! [pato c]
  (db/delete-osaamisen-osoittamiset-by-pato-id! (:id pato))
  (save-pato-osaamisen-osoittamiset! pato c))

(defn update-hankittava-ammat-tutkinnon-osa! [pato-db values]
  (db/update-hankittava-ammat-tutkinnon-osa-by-id! (:id pato-db) values)
  (cond-> pato-db
    (:osaamisen-hankkimistavat values)
    (assoc :osaamisen-hankkimistavat
           (replace-pato-osaamisen-hankkimistavat!
             pato-db (:osaamisen-hankkimistavat values)))
    (:osaamisen-osoittaminen values)
    (assoc :osaamisen-osoittaminen
           (replace-pato-osaamisen-osoittamiset!
             pato-db (:osaamisen-osoittaminen values)))))

(defn- replace-ooato-tarkentavat-tiedot-naytto! [ooato-id new-values]
  (db/delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id! ooato-id)
  (save-ooato-tarkentavat-tiedot-naytto! ooato-id new-values))

(defn- replace-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta-id new-values]
  (db/delete-todennettu-arviointi-arvioijat-by-tta-id! tta-id)
  (save-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-values))

(defn- update-tarkentavat-tiedot-osaamisen-arvioija! [tta-id new-tta-values]
  (db/update-todennettu-arviointi-lisatiedot-by-id! tta-id new-tta-values)
  (when-let [new-arvioijat
             (:aiemmin-hankitun-osaamisen-arvioijat new-tta-values)]
    (replace-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-arvioijat)))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa!
  [ooato-from-db new-values]
  (db/update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id!
    (:id ooato-from-db) new-values)
  (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
    (update-tarkentavat-tiedot-osaamisen-arvioija!
      (:tarkentavat-tiedot-osaamisen-arvioija-id ooato-from-db) new-tta))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-ooato-tarkentavat-tiedot-naytto! (:id ooato-from-db) new-ttn)))

(defn- replace-oopto-tarkentavat-tiedot-naytto! [oopto-id new-values]
  (db/delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id! oopto-id)
  (save-oopto-tarkentavat-tiedot-naytto! oopto-id new-values))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa!
  [oopto-from-db new-values]
  (db/update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id!
    (:id oopto-from-db) new-values)
  (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
    (update-tarkentavat-tiedot-osaamisen-arvioija!
      (:tarkentavat-tiedot-osaamisen-arvioija-id oopto-from-db) new-tta))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-oopto-tarkentavat-tiedot-naytto! (:id oopto-from-db) new-ttn)))

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

(defn save-opiskeluvalmiuksia-tukevat-opinnot! [h c]
  (db/insert-opiskeluvalmiuksia-tukevat-opinnot!
    (mapv #(assoc % :hoks-id (:id h)) c)))

(defn save-yto-osa-alueen-osaamisen-osoittaminen! [yto n]
  (let [naytto (save-osaamisen-osoittaminen! n)
        yto-naytto (db/insert-yto-osa-alueen-osaamisen-osoittaminen!
                     (:id yto) (:id naytto))]
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
         :osaamisen-osoittaminen
         (mapv
           (fn [hon]
             (save-yto-osa-alueen-osaamisen-osoittaminen! o hon))
           (:osaamisen-osoittaminen %))))
    osa-alueet))

(defn save-hankittava-yhteinen-tutkinnon-osa! [h pyto]
  (let [p-db (db/insert-hankittava-yhteinen-tutkinnon-osa!
               (assoc pyto :hoks-id (:id h)))]
    (assoc p-db
           :osa-alueet (save-pyto-osa-alueet! (:id p-db) (:osa-alueet pyto)))))

(defn save-hankittavat-yhteiset-tutkinnon-osat! [h c]
  (mapv
    #(save-hankittava-yhteinen-tutkinnon-osa! h %)
    c))

(defn save-hoks! [h]
  (let [saved-hoks (db/insert-hoks! h)]
    (assoc
      saved-hoks
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (save-aiemmin-hankitut-ammat-tutkinnon-osat!
        saved-hoks (:aiemmin-hankitut-ammat-tutkinnon-osat h))
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
        saved-hoks (:aiemmin-hankitut-paikalliset-tutkinnon-osat h))
      :hankittavat-paikalliset-tutkinnon-osat
      (save-hankittavat-paikalliset-tutkinnon-osat!
        saved-hoks (:hankittavat-paikalliset-tutkinnon-osat h))
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
        saved-hoks (:aiemmin-hankitut-yhteiset-tutkinnon-osat h))
      :hankittavat-ammat-tutkinnon-osat
      (save-hankittavat-ammat-tutkinnon-osat!
        saved-hoks (:hankittavat-ammat-tutkinnon-osat h))
      :opiskeluvalmiuksia-tukevat-opinnot
      (save-opiskeluvalmiuksia-tukevat-opinnot!
        saved-hoks (:opiskeluvalmiuksia-tukevat-opinnot h))
      :hankittavat-yhteiset-tutkinnon-osat
      (save-hankittavat-yhteiset-tutkinnon-osat!
        saved-hoks (:hankittavat-yhteiset-tutkinnon-osat h)))))
