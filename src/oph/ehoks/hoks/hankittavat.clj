(ns oph.ehoks.hoks.hankittavat
  (:require [oph.ehoks.db.postgresql.hankittavat :as db]
            [oph.ehoks.hoks.common :as c]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn- get-tyopaikalla-jarjestettava-koulutus [id]
  (let [o (db/select-tyopaikalla-jarjestettava-koulutus-by-id id)]
    (-> o
        (dissoc :id)
        (assoc :keskeiset-tyotehtavat
               (db/select-tyotehtavat-by-tho-id (:id o))))))

(defn set-osaamisen-hankkimistapa-values [m]
  (if (some? (:tyopaikalla-jarjestettava-koulutus-id m))
    (dissoc
      (assoc
        m
        :tyopaikalla-jarjestettava-koulutus
        (get-tyopaikalla-jarjestettava-koulutus
          (:tyopaikalla-jarjestettava-koulutus-id m))
        :muut-oppimisymparistot
        (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
          (:id m)))
      :id :uuid :tyopaikalla-jarjestettava-koulutus-id)
    (dissoc
      (assoc
        m
        :muut-oppimisymparistot
        (db/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
          (:id m)))
      :id :uuid)))

(defn get-osaamisen-osoittaminen [id]
  (let [naytot (db/select-osaamisen-osoittamiset-by-ppto-id id)]
    (mapv
      #(dissoc (c/set-osaamisen-osoittaminen-values %) :id :uuid)
      naytot)))

(defn get-osaamisen-hankkimistavat [id]
  (let [hankkimistavat (db/select-osaamisen-hankkimistavat-by-hpto-id id)]
    (mapv
      set-osaamisen-hankkimistapa-values
      hankkimistavat)))

(defn get-hankittava-paikallinen-tutkinnon-osa [id]
  (dissoc (assoc
            (db/select-hankittava-paikallinen-tutkinnon-osa-by-id id)
            :osaamisen-osoittaminen
            (get-osaamisen-osoittaminen id)
            :osaamisen-hankkimistavat
            (get-osaamisen-hankkimistavat id))
          :uuid))

(defn get-hankittavat-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-osoittaminen
         (get-osaamisen-osoittaminen (:id %))
         :osaamisen-hankkimistavat
         (get-osaamisen-hankkimistavat (:id %)))
       :id :uuid)
    (db/select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-hato-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-hato-id id)))

(defn get-hato-osaamisen-osoittaminen [id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
       :id :uuid)
    (db/select-osaamisen-osoittamiset-by-hato-id id)))

(defn get-yto-osa-alue-osaamisen-hankkimistavat [id]
  (mapv
    set-osaamisen-hankkimistapa-values
    (db/select-osaamisen-hankkimistavat-by-hyto-osa-alue-id id)))

(defn get-yto-osa-alueen-osaamisen-osoittamiset [id]
  (mapv
    #(dissoc (c/set-osaamisen-osoittaminen-values %) :id :uuid)
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
       :id :uuid :yhteinen-tutkinnon-osa-id)
    (db/select-yto-osa-alueet-by-yto-id hyto-id)))

(defn get-hankittava-ammat-tutkinnon-osa [id]
  (when-let [hato-db (db/select-hankittava-ammat-tutkinnon-osa-by-id id)]
    (dissoc (assoc
              hato-db
              :osaamisen-osoittaminen
              (get-hato-osaamisen-osoittaminen id)
              :osaamisen-hankkimistavat
              (get-hato-osaamisen-hankkimistavat id))
            :uuid)))

(defn get-hankittavat-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc
         %
         :osaamisen-osoittaminen
         (get-hato-osaamisen-osoittaminen (:id %))
         :osaamisen-hankkimistavat
         (get-hato-osaamisen-hankkimistavat (:id %)))
       :id :uuid)
    (db/select-hankittavat-ammat-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-hankittava-yhteinen-tutkinnon-osa [hyto-id]
  (when-let [hato-db
             (db/select-hankittava-yhteinen-tutkinnon-osa-by-id hyto-id)]
    (dissoc (assoc hato-db :osa-alueet (get-yto-osa-alueet hyto-id))
            :uuid)))

(defn get-hankittavat-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc % :osa-alueet (get-yto-osa-alueet (:id %)))
       :id :uuid)
    (db/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn save-osaamisen-hankkimistapa!
  ([oh]
    (save-osaamisen-hankkimistapa! oh (db-ops/get-db-connection)))
  ([oh db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tho (db/insert-tyopaikalla-jarjestettava-koulutus!
                  (:tyopaikalla-jarjestettava-koulutus oh) conn)
            o-db (db/insert-osaamisen-hankkimistapa!
                   (assoc
                     oh
                     :tyopaikalla-jarjestettava-koulutus-id
                     (:id tho)) conn)]
        (db/insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
          o-db (:muut-oppimisymparistot oh) conn)
        o-db))))

(defn save-hpto-osaamisen-hankkimistapa!
  ([hpto oh]
    (save-hpto-osaamisen-hankkimistapa! hpto oh (db-ops/get-db-connection)))
  ([hpto oh db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [o-db (save-osaamisen-hankkimistapa! oh conn)]
        (db/insert-hpto-osaamisen-hankkimistapa!
          hpto o-db conn)
        o-db))))

(defn save-hpto-osaamisen-hankkimistavat!
  ([hpto c]
    (save-hpto-osaamisen-hankkimistavat! hpto c (db-ops/get-db-connection)))
  ([hpto c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-hpto-osaamisen-hankkimistapa! hpto % conn) c))))

(defn- replace-hpto-osaamisen-hankkimistavat! [hpto c db-conn]
  (db/delete-osaamisen-hankkimistavat-by-hpto-id! (:id hpto) db-conn)
  (save-hpto-osaamisen-hankkimistavat! hpto c db-conn))

(defn save-hpto-osaamisen-osoittaminen!
  ([hpto n]
    (save-hpto-osaamisen-osoittaminen! hpto n (db-ops/get-db-connection)))
  ([hpto n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [naytto (c/save-osaamisen-osoittaminen! n conn)]
        (db/insert-hpto-osaamisen-osoittaminen! hpto naytto conn)
        naytto))))

(defn save-hpto-osaamisen-osoittamiset!
  ([ppto c]
    (save-hpto-osaamisen-osoittamiset! ppto c (db-ops/get-db-connection)))
  ([ppto c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-hpto-osaamisen-osoittaminen! ppto % conn)
        c))))

(defn- replace-hpto-osaamisen-osoittamiset! [hpto c db-conn]
  (db/delete-osaamisen-osoittamiset-by-ppto-id! (:id hpto) db-conn)
  (save-hpto-osaamisen-osoittamiset! hpto c db-conn))

(defn update-hankittava-paikallinen-tutkinnon-osa! [hpto-db values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-hankittava-paikallinen-tutkinnon-osa-by-id!
      (:id hpto-db) values db-conn)
    (cond-> hpto-db
      (:osaamisen-hankkimistavat values)
      (assoc :osaamisen-hankkimistavat
             (replace-hpto-osaamisen-hankkimistavat!
               hpto-db (:osaamisen-hankkimistavat values) db-conn))
      (:osaamisen-osoittaminen values)
      (assoc :osaamisen-osoittaminen
             (replace-hpto-osaamisen-osoittamiset!
               hpto-db (:osaamisen-osoittaminen values) db-conn)))))

(defn save-yto-osa-alueen-osaamisen-osoittaminen!
  ([yto n]
    (save-yto-osa-alueen-osaamisen-osoittaminen!
      yto n (db-ops/get-db-connection)))
  ([yto n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [naytto (c/save-osaamisen-osoittaminen! n conn)
            yto-naytto (db/insert-yto-osa-alueen-osaamisen-osoittaminen!
                         (:id yto) (:id naytto) conn)]
        yto-naytto))))

(defn save-hankittava-paikallinen-tutkinnon-osa!
  ([hoks-id hpto]
    (save-hankittava-paikallinen-tutkinnon-osa!
      hoks-id hpto (db-ops/get-db-connection)))
  ([hoks-id hpto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [hpto-db (db/insert-hankittava-paikallinen-tutkinnon-osa!
                      (assoc hpto :hoks-id hoks-id) conn)]
        (assoc
          hpto-db
          :osaamisen-hankkimistavat
          (save-hpto-osaamisen-hankkimistavat!
            hpto-db (:osaamisen-hankkimistavat hpto) conn)
          :osaamisen-osoittaminen
          (save-hpto-osaamisen-osoittamiset!
            hpto-db (:osaamisen-osoittaminen hpto) conn))))))

(defn save-hankittavat-paikalliset-tutkinnon-osat!
  ([hoks-id c]
    (save-hankittavat-paikalliset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-hankittava-paikallinen-tutkinnon-osa! hoks-id % conn) c))))

(defn save-hato-osaamisen-hankkimistapa!
  ([hato oh]
    (save-hato-osaamisen-hankkimistapa! hato oh (db-ops/get-db-connection)))
  ([hato oh db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [o-db (save-osaamisen-hankkimistapa! oh conn)]
        (db/insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
          (:id hato) (:id o-db) conn)
        o-db))))

(defn save-hato-osaamisen-hankkimistavat! [hato-db c db-conn]
  (mapv
    #(save-hato-osaamisen-hankkimistapa! hato-db % db-conn)
    c))

(defn save-hato-osaamisen-osoittaminen!
  ([hato n]
    (save-hato-osaamisen-osoittaminen!
      hato n (db-ops/get-db-connection)))
  ([hato n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [naytto (c/save-osaamisen-osoittaminen! n conn)]
        (db/insert-hato-osaamisen-osoittaminen! (:id hato) (:id naytto) conn)
        naytto))))

(defn- save-hato-osaamisen-osoittamiset! [hato-db c db-conn]
  (mapv
    #(save-hato-osaamisen-osoittaminen! hato-db % db-conn)
    c))

(defn save-hankittava-ammat-tutkinnon-osa!
  ([hoks-id hato]
    (save-hankittava-ammat-tutkinnon-osa!
      hoks-id hato (db-ops/get-db-connection)))
  ([hoks-id hato db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [hato-db (db/insert-hankittava-ammat-tutkinnon-osa!
                      (assoc hato :hoks-id hoks-id) conn)]
        (assoc
          hato-db
          :osaamisen-osoittaminen
          (mapv
            #(save-hato-osaamisen-osoittaminen! hato-db % conn)
            (:osaamisen-osoittaminen hato))
          :osaamisen-hankkimistavat
          (mapv
            #(save-hato-osaamisen-hankkimistapa! hato-db % conn)
            (:osaamisen-hankkimistavat hato)))))))

(defn save-hankittavat-ammat-tutkinnon-osat!
  ([hoks-id c]
    (save-hankittavat-ammat-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-hankittava-ammat-tutkinnon-osa! hoks-id % conn) c))))

(defn- replace-hato-osaamisen-hankkimistavat! [hato c db-conn]
  (db/delete-osaamisen-hankkimistavat-by-hato-id! (:id hato) db-conn)
  (save-hato-osaamisen-hankkimistavat! hato c db-conn))

(defn- replace-hato-osaamisen-osoittamiset! [hato c db-conn]
  (db/delete-osaamisen-osoittamiset-by-pato-id! (:id hato) db-conn)
  (save-hato-osaamisen-osoittamiset! hato c db-conn))

(defn update-hankittava-ammat-tutkinnon-osa! [hato-db values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-hankittava-ammat-tutkinnon-osa-by-id!
      (:id hato-db) values db-conn)
    (cond-> hato-db
      (:osaamisen-hankkimistavat values)
      (assoc :osaamisen-hankkimistavat
             (replace-hato-osaamisen-hankkimistavat!
               hato-db (:osaamisen-hankkimistavat values) db-conn))
      (:osaamisen-osoittaminen values)
      (assoc :osaamisen-osoittaminen
             (replace-hato-osaamisen-osoittamiset!
               hato-db (:osaamisen-osoittaminen values) db-conn)))))

(defn save-hyto-osa-alue-osaamisen-hankkimistapa!
  ([hyto-osa-alue oh]
    (save-hyto-osa-alue-osaamisen-hankkimistapa!
      hyto-osa-alue oh (db-ops/get-db-connection)))
  ([hyto-osa-alue oh db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [o-db (save-osaamisen-hankkimistapa! oh conn)]
        (db/insert-hyto-osa-alueen-osaamisen-hankkimistapa!
          (:id hyto-osa-alue) (:id o-db) conn)
        o-db))))

(defn save-hyto-osa-alueet!
  ([hyto-id osa-alueet]
    (save-hyto-osa-alueet! hyto-id osa-alueet (db-ops/get-db-connection)))
  ([hyto-id osa-alueet db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [osa-alue-db (db/insert-yhteisen-tutkinnon-osan-osa-alue!
                             (assoc % :yhteinen-tutkinnon-osa-id hyto-id) conn)]
           (assoc
             osa-alue-db
             :osaamisen-hankkimistavat
             (mapv
               (fn [oht]
                 (save-hyto-osa-alue-osaamisen-hankkimistapa!
                   osa-alue-db oht conn))
               (:osaamisen-hankkimistavat %))
             :osaamisen-osoittaminen
             (mapv
               (fn [hon]
                 (save-yto-osa-alueen-osaamisen-osoittaminen!
                   osa-alue-db hon conn))
               (:osaamisen-osoittaminen %))))
        osa-alueet))))

(defn save-hankittava-yhteinen-tutkinnon-osa!
  ([hoks-id hyto]
    (save-hankittava-yhteinen-tutkinnon-osa!
      hoks-id hyto (db-ops/get-db-connection)))
  ([hoks-id hyto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [hyto-db (db/insert-hankittava-yhteinen-tutkinnon-osa!
                      (assoc hyto :hoks-id hoks-id) conn)]
        (assoc
          hyto-db
          :osa-alueet
          (save-hyto-osa-alueet!
            (:id hyto-db) (:osa-alueet hyto) conn))))))

(defn save-hankittavat-yhteiset-tutkinnon-osat!
  ([hoks-id c]
    (save-hankittavat-yhteiset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-hankittava-yhteinen-tutkinnon-osa! hoks-id % conn)
        c))))

(defn- replace-hyto-osa-alueet! [hyto-id new-oa-values db-conn]
  (db/delete-hyto-osa-alueet! hyto-id db-conn)
  (save-hyto-osa-alueet! hyto-id new-oa-values db-conn))

(defn update-hankittava-yhteinen-tutkinnon-osa! [hyto-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (let [bare-hyto (dissoc new-values :osa-alueet)]
      (when (not-empty bare-hyto)
        (db/update-hankittava-yhteinen-tutkinnon-osa-by-id!
          hyto-id new-values db-conn)))
    (when-let [oa (:osa-alueet new-values)]
      (replace-hyto-osa-alueet! hyto-id oa db-conn))))
