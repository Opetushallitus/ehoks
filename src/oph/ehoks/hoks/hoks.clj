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

(defn get-tarkentavat-tiedot-arvioija [id]
  (let [tta (db/select-todennettu-arviointi-lisatiedot-by-id id)]
    (dissoc
      (assoc
        tta
        :aiemmin-hankitun-osaamisen-arvioijat
        (db/select-arvioijat-by-todennettu-arviointi-id id))
      :id)))

(defn get-aiemmin-hankitut-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :tarkentavat-tiedot-arvioija
         (get-tarkentavat-tiedot-arvioija (:tarkentavat-tiedot-arvioija-id %))
         :tarkentavat-tiedot-naytto
         (get-ooato-tarkentavat-tiedot-naytto (:id %)))
       :tarkentavat-tiedot-arvioija-id :id)
    (db/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id
      hoks-id)))

(defn get-osaamisen-osoittaminen [id]
  (let [naytot (db/select-osaamisen-osoittamiset-by-ppto-id id)]
    (mapv
      #(dissoc (set-osaamisen-osoittaminen-values %) :id)
      naytot)))

(defn get-tyopaikalla-jarjestettava-koulutus [id]
  (let [o (db/select-tyopaikalla-jarjestettava-koulutus-by-id id)]
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
      :tyopaikalla-jarjestettava-koulutus
      (get-tyopaikalla-jarjestettava-koulutus
        (:tyopaikalla-jarjestettava-koulutus-id m))
      :muut-oppimisymparisto
      (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
        (:id m)))
    :id :tyopaikalla-jarjestettava-koulutus-id))

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

(defn get-oopto-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-osaamisen-osoittaminen-by-oopto-id id)))

(defn get-aiemmin-hankitut-paikalliset-tutkinnon-osat [hoks-id]
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
         :tarkentavat-tiedot
         (get-ooyto-osa-alue-tarkentavat-tiedot (:id %)))
       :id)
    (db/select-osa-alueet-by-ooyto-id id)))

(defn get-ooyto-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-tarkentavat-tiedot-naytto-by-ooyto-id id)))

(defn get-aiemmin-hankitut-yhteiset-tutkinnon-osat [hoks-id]
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
    #(dissoc
       (assoc
         (set-osaamisen-osoittaminen-values %)
         :osaamistavoitteet
         (db/select-hankitun-yto-osaamisen-nayton-osaamistavoitteet (:id %)))
       :id)
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
      o-db (:muut-oppimisymparisto oh))
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

(defn save-oopto-arvioijat! [oopto-id arvioijat]
  (mapv
    #(let [a (db/insert-koulutuksen-jarjestaja-osaamisen-arvioija! %)]
       (db/insert-oopto-arvioija! oopto-id (:id a)))
    arvioijat))

(defn save-aiemmin-hankittu-paikallinen-tutkinnon-osa! [oopto]
  (let [oopto-db (db/insert-aiemmin-hankittu-paikallinen-tutkinnon-osa! oopto)]
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

(defn save-aiemmin-hankitut-paikalliset-tutkinnon-osat! [h c]
  (mapv
    #(save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
       (assoc % :hoks-id (:id h)))
    c))

(defn save-ooyto-tarkentavat-tiedot-naytto! [ooyto c]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-ooyto-osaamisen-osoittaminen! ooyto n)
       n)
    c))

(defn save-ooyto-arvioijat! [yto-id arvioijat]
  (mapv
    #(let [a (db/insert-koulutuksen-jarjestaja-osaamisen-arvioija! %)]
       (db/insert-ooyto-arvioija! yto-id (:id a)))
    arvioijat))

(defn save-ooyto-osa-alueet! [yto-id osa-alueet]
  (mapv
    #(let [o (db/insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue!
               (assoc % :aiemmin-hankittu-yhteinen-tutkinnon-osa-id yto-id))]
       (mapv
         (fn [naytto]
           (let [n (save-osaamisen-osoittaminen! naytto)]
             (db/insert-ooyto-osa-alue-osaamisen-osoittaminen!
               (:id o) (:id n))))
         (:tarkentavat-tiedot %)))
    osa-alueet))

(defn save-aiemmin-hankittu-yhteinen-tutkinnon-osa! [o]
  (let [yto (db/insert-aiemmin-hankittu-yhteinen-tutkinnon-osa! o)]
    (save-ooyto-tarkentavat-tiedot-naytto! yto (:tarkentavat-tiedot-naytto o))
    (save-ooyto-arvioijat!
      (:id yto)
      (get-in
        o [:tarkentavat-tiedot-arvioija :aiemmin-hankitun-osaamisen-arvioijat]))
    (save-ooyto-osa-alueet! (:id yto) (:osa-alueet o))
    yto))

(defn save-aiemmin-hankitut-yhteiset-tutkinnon-osat! [h c]
  (mapv
    #(save-aiemmin-hankittu-yhteinen-tutkinnon-osa! (assoc % :hoks-id (:id h)))
    c))

(defn save-ooato-tarkentavat-tiedot-naytto! [ooato c]
  (mapv
    #(let [n (save-osaamisen-osoittaminen! %)]
       (db/insert-ooato-osaamisen-osoittaminen! ooato n)
       n)
    c))

(defn save-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta c]
  (mapv
    #(db/insert-todennettu-arviointi-arvioija! tta %)
    (db/insert-koulutuksen-jarjestaja-osaamisen-arvioijat! c)))

(defn save-ooato-tarkentavat-tiedot-arvioija! [m]
  (let [tta (db/insert-todennettu-arviointi-lisatiedot! m)]
    (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
      tta (:aiemmin-hankitun-osaamisen-arvioijat m))
    tta))

(defn save-aiemmin-hankittu-ammat-tutkinnon-osa! [h ooato]
  (let [ooato-db (db/insert-aiemmin-hankittu-ammat-tutkinnon-osa!
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

(defn save-aiemmin-hankitut-ammat-tutkinnon-osat! [h c]
  (mapv #(save-aiemmin-hankittu-ammat-tutkinnon-osa! h %) c))

(defn save-pato-osaamisen-hankkimistapa! [pato oh]
  (let [o-db (save-osaamisen-hankkimistapa! oh)]
    (db/insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
      (:id pato) (:id o-db))
    o-db))

(defn save-pato-osaamisen-osoittaminen! [pato n]
  (let [naytto (save-osaamisen-osoittaminen! n)]
    (db/insert-pato-osaamisen-osoittaminen! (:id pato) (:id naytto))
    naytto))

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

(defn save-opiskeluvalmiuksia-tukevat-opinnot! [h c]
  (db/insert-opiskeluvalmiuksia-tukevat-opinnot!
    (mapv #(assoc % :hoks-id (:id h)) c)))

(defn save-yto-osa-alueen-osaamisen-osoittaminen! [yto n]
  (let [naytto (save-osaamisen-osoittaminen! n)
        yto-naytto (db/insert-yto-osa-alueen-osaamisen-osoittaminen!
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
