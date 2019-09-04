(ns oph.ehoks.hoks.hankittavat
  (:require [oph.ehoks.db.postgresql.hankittavat :as db]
            [oph.ehoks.hoks.common :as c]))

(defn- get-tyopaikalla-jarjestettava-koulutus [id]
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

(defn get-osaamisen-osoittaminen [id]
  (let [naytot (db/select-osaamisen-osoittamiset-by-ppto-id id)]
    (mapv
      #(dissoc (c/set-osaamisen-osoittaminen-values %) :id)
      naytot)))

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

(defn get-hato-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-hato-id id)))

(defn get-hato-osaamisen-osoittaminen [id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
       :id)
    (db/select-osaamisen-osoittamiset-by-hato-id id)))

(defn get-yto-osa-alue-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-hyto-osa-alue-id id)))

(defn get-yto-osa-alueen-osaamisen-osoittamiset [id]
  (mapv
    #(dissoc (c/set-osaamisen-osoittaminen-values %) :id)
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

(defn save-hpto-osaamisen-osoittaminen! [hpto n]
  (let [naytto (c/save-osaamisen-osoittaminen! n)]
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

(defn save-yto-osa-alueen-osaamisen-osoittaminen! [yto n]
  (let [naytto (c/save-osaamisen-osoittaminen! n)
        yto-naytto (db/insert-yto-osa-alueen-osaamisen-osoittaminen!
                     (:id yto) (:id naytto))]
    yto-naytto))

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
  (let [naytto (c/save-osaamisen-osoittaminen! n)]
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
