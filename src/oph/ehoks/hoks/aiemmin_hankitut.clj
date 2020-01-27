(ns oph.ehoks.hoks.aiemmin-hankitut
  (:require [oph.ehoks.db.postgresql.aiemmin-hankitut :as db]
            [oph.ehoks.hoks.common :as c]))

(defn get-ahato-tarkentavat-tiedot-naytto [id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
       :id)
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
    (set-ahato-values ahato-from-db)))

(defn get-aiemmin-hankitut-ammat-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc (set-ahato-values %) :id)
    (db/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id hoks-id)))

(defn- get-ahpto-tarkentavat-tiedot-naytto [ahpto-id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
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
    #(dissoc (c/set-osaamisen-osoittaminen-values %) :id)
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
       :tarkentavat-tiedot-osaamisen-arvioija-id)
    (db/select-osa-alueet-by-ahyto-id ahyto-id)))

(defn get-ahyto-tarkentavat-tiedot-naytto [ahyto-id]
  (mapv
    #(dissoc
       (c/set-osaamisen-osoittaminen-values %)
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

(defn save-ahpto-tarkentavat-tiedot-naytto! [ahpto-id c]
  (mapv
    #(let [n (c/save-osaamisen-osoittaminen! %)]
       (db/insert-ahpto-osaamisen-osoittaminen! ahpto-id (:id n))
       n)
    c))

(defn save-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta-id new-arvioijat]
  (mapv
    #(db/insert-todennettu-arviointi-arvioijat! tta-id (:id %))
    (db/insert-koulutuksen-jarjestaja-osaamisen-arvioijat! new-arvioijat)))

(defn save-ahyto-tarkentavat-tiedot-naytto! [ahyto-id new-values]
  (mapv
    #(let [n (c/save-osaamisen-osoittaminen! %)]
       (db/insert-ahyto-osaamisen-osoittaminen! ahyto-id n)
       n)
    new-values))

(defn save-tarkentavat-tiedot-osaamisen-arvioija! [new-tta]
  (let [tta-db (db/insert-todennettu-arviointi-lisatiedot! new-tta)]
    (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
      (:id tta-db) (:aiemmin-hankitun-osaamisen-arvioijat new-tta))
    tta-db))

(defn- save-ahyto-osa-alue! [ahyto-id osa-alue]
  (let [arvioija (:tarkentavat-tiedot-osaamisen-arvioija osa-alue)
        stored-osa-alue
        (db/insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue!
          (assoc osa-alue
                :aiemmin-hankittu-yhteinen-tutkinnon-osa-id ahyto-id
                :tarkentavat-tiedot-osaamisen-arvioija-id
                (:id (save-tarkentavat-tiedot-osaamisen-arvioija! arvioija))))]
    (mapv
      (fn [naytto]
        (let [stored-naytto (c/save-osaamisen-osoittaminen! naytto)]
          (db/insert-ahyto-osa-alue-osaamisen-osoittaminen!
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

(defn save-aiemmin-hankitut-yhteiset-tutkinnon-osat! [hoks-id c]
  (mapv
    #(save-aiemmin-hankittu-yhteinen-tutkinnon-osa! hoks-id %)
    c))

(defn save-ahato-tarkentavat-tiedot-naytto! [ahato-id new-values]
  (mapv
    #(let [n (c/save-osaamisen-osoittaminen! %)]
       (db/insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto!
         ahato-id n)
       n)
    new-values))

(defn save-aiemmin-hankittu-ammat-tutkinnon-osa! [hoks-id ahato]
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

(defn replace-ahato-tarkentavat-tiedot-naytto! [ahato-id new-values]
  (db/delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id! ahato-id)
  (save-ahato-tarkentavat-tiedot-naytto! ahato-id new-values))

(defn- replace-tta-aiemmin-hankitun-osaamisen-arvioijat! [tta-id new-values]
  (db/delete-todennettu-arviointi-arvioijat-by-tta-id! tta-id)
  (save-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-values))

(defn update-tarkentavat-tiedot-osaamisen-arvioija! [tta-id new-tta-values]
  (db/update-todennettu-arviointi-lisatiedot-by-id! tta-id new-tta-values)
  (when-let [new-arvioijat
             (:aiemmin-hankitun-osaamisen-arvioijat new-tta-values)]
    (replace-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-arvioijat)))

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

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa!
  [ahato-from-db new-values]
  (db/update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id!
    (:id ahato-from-db) new-values)
  (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
    (update-tarkentavat-tiedot-osaamisen-arvioija!
      (:tarkentavat-tiedot-osaamisen-arvioija-id ahato-from-db) new-tta))
  (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
    (replace-ahato-tarkentavat-tiedot-naytto! (:id ahato-from-db) new-ttn)))
