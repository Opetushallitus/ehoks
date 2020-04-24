(ns oph.ehoks.hoks.aiemmin-hankitut
  (:require [oph.ehoks.db.postgresql.aiemmin-hankitut :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.hoks.common :as c]
            [clojure.java.jdbc :as jdbc]))

(defn get-ahato-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
       :id :share-id)
    (db/select-tarkentavat-tiedot-naytto-by-ahato-id id)))

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
    (dissoc (set-ahato-values ahato-from-db) :share-id)))

(defn get-aiemmin-hankitut-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ahato-values %) :id :share-id)
    (db/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id hoks-id)))

(defn- get-ahpto-tarkentavat-tiedot-naytto [ahpto-id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
       :id :share-id)
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
    (dissoc (set-ahpto-values ahpto-from-db) :share-id)))

(defn get-aiemmin-hankitut-paikalliset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ahpto-values %) :id :share-id)
    (db/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-ahyto-osa-alue-tarkentavat-tiedot [id]
  (mapv
    #(dissoc (c/set-osaamisen-osoittaminen-values %) :id :share-id)
    (db/select-tarkentavat-tiedot-naytto-by-ahyto-osa-alue-id id)))

(defn get-ahyto-osa-alueet [ahyto-id]
  (mapv
    #(dissoc
       (assoc
         %
         :tarkentavat-tiedot-naytto
         (get-ahyto-osa-alue-tarkentavat-tiedot (:id %))
         :tarkentavat-tiedot-osaamisen-arvioija
         (get-tarkentavat-tiedot-osaamisen-arvioija
           (:tarkentavat-tiedot-osaamisen-arvioija-id %)))
       :id
       :share-id
       :tarkentavat-tiedot-osaamisen-arvioija-id)
    (db/select-osa-alueet-by-ahyto-id ahyto-id)))

(defn get-ahyto-tarkentavat-tiedot-naytto [ahyto-id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
       :id :share-id)
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
    (dissoc (set-ahyto-values ahyto-from-db) :share-id)))

(defn get-aiemmin-hankitut-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ahyto-values %) :id :share-id)
    (db/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn save-ahpto-tarkentavat-tiedot-naytto!
  ([ahpto-id c]
    (save-ahpto-tarkentavat-tiedot-naytto!
      ahpto-id c (db-ops/get-db-connection)))
  ([ahpto-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [n (c/save-osaamisen-osoittaminen! % conn)]
           (db/insert-ahpto-osaamisen-osoittaminen! ahpto-id (:id n) conn)
           n)
        c))))

(defn save-tta-aiemmin-hankitun-osaamisen-arvioijat!
  ([tta-id new-arvioijat]
    (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
      tta-id new-arvioijat (db-ops/get-db-connection)))
  ([tta-id new-arvioijat db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(db/insert-todennettu-arviointi-arvioijat! tta-id (:id %) conn)
        (db/insert-koulutuksen-jarjestaja-osaamisen-arvioijat!
          new-arvioijat conn)))))

(defn save-ahyto-tarkentavat-tiedot-naytto!
  ([ahyto-id new-values]
    (save-ahyto-tarkentavat-tiedot-naytto!
      ahyto-id new-values (db-ops/get-db-connection)))
  ([ahyto-id new-values db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [n (c/save-osaamisen-osoittaminen! % conn)]
           (db/insert-ahyto-osaamisen-osoittaminen! ahyto-id n conn)
           n)
        new-values))))

(defn save-tarkentavat-tiedot-osaamisen-arvioija!
  ([new-tta]
    (save-tarkentavat-tiedot-osaamisen-arvioija!
      new-tta (db-ops/get-db-connection)))
  ([new-tta db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tta-db (db/insert-todennettu-arviointi-lisatiedot! new-tta conn)]
        (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
          (:id tta-db) (:aiemmin-hankitun-osaamisen-arvioijat new-tta) conn)
        tta-db))))

(defn- save-ahyto-osa-alue!
  ([ahyto-id osa-alue]
    (save-ahyto-osa-alue! ahyto-id osa-alue (db-ops/get-db-connection)))
  ([ahyto-id osa-alue db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [arvioija (:tarkentavat-tiedot-osaamisen-arvioija osa-alue)
            stored-osa-alue
            (db/insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue!
              (assoc
                osa-alue
                :aiemmin-hankittu-yhteinen-tutkinnon-osa-id ahyto-id
                :tarkentavat-tiedot-osaamisen-arvioija-id
                (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                       arvioija conn)))
              conn)]
        (mapv
          (fn [naytto]
            (let [stored-naytto (c/save-osaamisen-osoittaminen! naytto conn)]
              (db/insert-ahyto-osa-alue-osaamisen-osoittaminen!
                (:id stored-osa-alue) (:id stored-naytto) conn)))
          (:tarkentavat-tiedot-naytto osa-alue))))))

(defn save-ahyto-osa-alueet!
  ([ahyto-id osa-alueet]
    (save-ahyto-osa-alueet! ahyto-id osa-alueet (db-ops/get-db-connection)))
  ([ahyto-id osa-alueet db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-ahyto-osa-alue! ahyto-id % conn)
        osa-alueet))))

(defn save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
  ([hoks-id ahyto]
    (save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
      hoks-id ahyto (db-ops/get-db-connection)))
  ([hoks-id ahyto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tta (:tarkentavat-tiedot-osaamisen-arvioija ahyto)
            yto (db/insert-aiemmin-hankittu-yhteinen-tutkinnon-osa!
                  (assoc
                    ahyto
                    :hoks-id hoks-id
                    :tarkentavat-tiedot-osaamisen-arvioija-id
                    (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                           tta conn)))
                  conn)]
        (save-ahyto-tarkentavat-tiedot-naytto!
          (:id yto)
          (:tarkentavat-tiedot-naytto ahyto)
          conn)
        (save-ahyto-osa-alueet! (:id yto) (:osa-alueet ahyto) conn)
        yto))))

(defn save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
  ([hoks-id ahpto]
    (save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
      hoks-id ahpto (db-ops/get-db-connection)))
  ([hoks-id ahpto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tta (:tarkentavat-tiedot-osaamisen-arvioija ahpto)
            ahpto-db (db/insert-aiemmin-hankittu-paikallinen-tutkinnon-osa!
                       (assoc
                         ahpto
                         :hoks-id hoks-id
                         :tarkentavat-tiedot-osaamisen-arvioija-id
                         (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                tta conn)))
                       conn)]
        (assoc
          ahpto-db
          :tarkentavat-tiedot-naytto
          (save-ahpto-tarkentavat-tiedot-naytto!
            (:id ahpto-db) (:tarkentavat-tiedot-naytto ahpto) conn))))))

(defn save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
  ([hoks-id c]
    (save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-aiemmin-hankittu-paikallinen-tutkinnon-osa! hoks-id % conn) c))))

(defn save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
  ([hoks-id c]
    (save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-aiemmin-hankittu-yhteinen-tutkinnon-osa! hoks-id % conn)
        c))))

(defn save-ahato-tarkentavat-tiedot-naytto!
  ([ahato-id new-values]
    (save-ahato-tarkentavat-tiedot-naytto!
      ahato-id new-values (db-ops/get-db-connection)))
  ([ahato-id new-values db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [n (c/save-osaamisen-osoittaminen! %)]
           (db/insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto!
             ahato-id n conn)
           n)
        new-values))))

(defn save-aiemmin-hankittu-ammat-tutkinnon-osa!
  ([hoks-id ahato]
    (save-aiemmin-hankittu-ammat-tutkinnon-osa!
      hoks-id ahato (db-ops/get-db-connection)))
  ([hoks-id ahato db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [ahato-db (db/insert-aiemmin-hankittu-ammat-tutkinnon-osa!
                       (assoc
                         ahato
                         :hoks-id hoks-id
                         :tarkentavat-tiedot-osaamisen-arvioija-id
                         (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                (:tarkentavat-tiedot-osaamisen-arvioija
                                  ahato))))
                       conn)]
        (assoc
          ahato-db
          :tarkentavat-tiedot-naytto
          (save-ahato-tarkentavat-tiedot-naytto!
            (:id ahato-db) (:tarkentavat-tiedot-naytto ahato) conn))))))

(defn save-aiemmin-hankitut-ammat-tutkinnon-osat!
  ([hoks-id c]
    (save-aiemmin-hankitut-ammat-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-aiemmin-hankittu-ammat-tutkinnon-osa! hoks-id % conn) c))))

(defn- replace-ahato-tarkentavat-tiedot-naytto! [ahato-id new-values db-conn]
  (db/delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id!
    ahato-id db-conn)
  (save-ahato-tarkentavat-tiedot-naytto! ahato-id new-values db-conn))

(defn- replace-tta-aiemmin-hankitun-osaamisen-arvioijat!
  [tta-id new-values db-conn]
  (db/delete-todennettu-arviointi-arvioijat-by-tta-id! tta-id db-conn)
  (save-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-values db-conn))

(defn- update-tarkentavat-tiedot-osaamisen-arvioija!
  [tta-id new-tta-values db-conn]
  (db/update-todennettu-arviointi-lisatiedot-by-id!
    tta-id new-tta-values db-conn)
  (when-let [new-arvioijat
             (:aiemmin-hankitun-osaamisen-arvioijat new-tta-values)]
    (replace-tta-aiemmin-hankitun-osaamisen-arvioijat!
      tta-id new-arvioijat db-conn)))

(defn- replace-ahpto-tarkentavat-tiedot-naytto! [ahpto-id new-values db-conn]
  (db/delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id!
    ahpto-id db-conn)
  (save-ahpto-tarkentavat-tiedot-naytto!
    ahpto-id new-values db-conn))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa!
  [ahpto-from-db new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id!
      (:id ahpto-from-db) new-values db-conn)
    (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
      (update-tarkentavat-tiedot-osaamisen-arvioija!
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahpto-from-db)
        new-tta db-conn))
    (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
      (replace-ahpto-tarkentavat-tiedot-naytto!
        (:id ahpto-from-db) new-ttn db-conn))))

(defn- replace-ahyto-tarkentavat-tiedot-naytto! [ahyto-id new-values db-conn]
  (db/delete-aiemmin-hankitun-yhteisen-tutkinnon-osan-naytto-by-id!
    ahyto-id db-conn)
  (save-ahyto-tarkentavat-tiedot-naytto! ahyto-id new-values db-conn))

(defn- replace-ahyto-osa-alueet! [ahyto-id new-values db-conn]
  (db/delete-aiemmin-hankitut-yto-osa-alueet-by-id! ahyto-id db-conn)
  (save-ahyto-osa-alueet! ahyto-id new-values db-conn))

(defn update-aiemmin-hankittu-yhteinen-tutkinnon-osa! [ahyto-from-db new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id!
      (:id ahyto-from-db) new-values db-conn)
    (when-let [new-ttoa (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
      (update-tarkentavat-tiedot-osaamisen-arvioija!
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahyto-from-db)
        new-ttoa db-conn))
    (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
      (replace-ahyto-tarkentavat-tiedot-naytto!
        (:id ahyto-from-db) new-ttn db-conn))
    (when-let [new-oa (:osa-alueet new-values)]
      (replace-ahyto-osa-alueet! (:id ahyto-from-db) new-oa db-conn))))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa!
  [ahato-from-db new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id!
      (:id ahato-from-db) new-values db-conn)
    (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
      (update-tarkentavat-tiedot-osaamisen-arvioija!
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahato-from-db)
        new-tta db-conn))
    (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
      (replace-ahato-tarkentavat-tiedot-naytto!
        (:id ahato-from-db) new-ttn db-conn))))
