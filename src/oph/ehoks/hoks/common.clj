(ns oph.ehoks.hoks.common
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

(defn save-yto-osa-alueen-osaamisen-osoittaminen! [yto n]
  (let [naytto (save-osaamisen-osoittaminen! n)
        yto-naytto (db/insert-yto-osa-alueen-osaamisen-osoittaminen!
                     (:id yto) (:id naytto))]
    yto-naytto))

(defn save-osaamisen-hankkimistapa! [oh]
  (let [tho (db/insert-tyopaikalla-jarjestettava-koulutus!
              (:tyopaikalla-jarjestettava-koulutus oh))
        o-db (db/insert-osaamisen-hankkimistapa!
               (assoc oh :tyopaikalla-jarjestettava-koulutus-id
                      (:id tho)))]
    (db/insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
      o-db (:muut-oppimisymparistot oh))
    o-db))

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

(defn get-osaamisen-osoittaminen [id]
  (let [naytot (db/select-osaamisen-osoittamiset-by-ppto-id id)]
    (mapv
      #(dissoc (set-osaamisen-osoittaminen-values %) :id)
      naytot)))

(defn get-osaamisen-hankkimistavat [id]
  (let [hankkimistavat (db/select-osaamisen-hankkimistavat-by-hpto-id id)]
    (mapv
      set-osaamisen-hankkimistapa-values
      hankkimistavat)))
