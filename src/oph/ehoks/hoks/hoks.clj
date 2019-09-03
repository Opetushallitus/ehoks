(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.common :as c]))

(defn get-osaamisen-osoittaminen [id]
  (let [naytot (db/select-osaamisen-osoittamiset-by-ppto-id id)]
    (mapv
      #(dissoc (c/set-osaamisen-osoittaminen-values %) :id)
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
      (ah/get-aiemmin-hankitut-ammat-tutkinnon-osat id)
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-paikalliset-tutkinnon-osat id)
      :hankittavat-paikalliset-tutkinnon-osat
      (get-hankittavat-paikalliset-tutkinnon-osat id)
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-yhteiset-tutkinnon-osat id)
      :hankittavat-ammat-tutkinnon-osat
      (get-hankittavat-ammat-tutkinnon-osat id)
      :opiskeluvalmiuksia-tukevat-opinnot
      (get-opiskeluvalmiuksia-tukevat-opinnot id)
      :hankittavat-yhteiset-tutkinnon-osat
      (get-hankittavat-yhteiset-tutkinnon-osat id))))

(defn get-hokses-by-oppija [oid]
  (mapv
    get-hoks-values
    (db-hoks/select-hoks-by-oppija-oid oid)))

(defn get-hoks-by-id [id]
  (get-hoks-values (db-hoks/select-hoks-by-id id)))

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

(defn save-opiskeluvalmiuksia-tukeva-opinto! [hoks-id new-oto-values]
  (db/insert-opiskeluvalmiuksia-tukeva-opinto!
    (assoc new-oto-values :hoks-id hoks-id)))

(defn save-opiskeluvalmiuksia-tukevat-opinnot! [hoks-id new-oto-values]
  (db/insert-opiskeluvalmiuksia-tukevat-opinnot!
    (mapv #(assoc % :hoks-id hoks-id) new-oto-values)))

(defn save-yto-osa-alueen-osaamisen-osoittaminen! [yto n]
  (let [naytto (c/save-osaamisen-osoittaminen! n)
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
  (let [saved-hoks (db-hoks/insert-hoks! h)]
    (when (:osaamisen-hankkimisen-tarve h)
      (sqs/send-message (sqs/build-hoks-hyvaksytty-msg
                          (:id saved-hoks) h)))
    (assoc
      saved-hoks
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-ammat-tutkinnon-osat h))
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
        (:id saved-hoks) (:aiemmin-hankitut-paikalliset-tutkinnon-osat h))
      :hankittavat-paikalliset-tutkinnon-osat
      (save-hankittavat-paikalliset-tutkinnon-osat!
        (:id saved-hoks) (:hankittavat-paikalliset-tutkinnon-osat h))
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
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

(defn- replace-main-hoks! [hoks-id new-values db-conn]
  (db-hoks/update-hoks-by-id!
    hoks-id (merge-not-given-hoks-values new-values) db-conn))

(defn- replace-oto! [hoks-id new-oto-values db-conn]
  (db/delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id hoks-id db-conn)
  (when
   new-oto-values
    (save-opiskeluvalmiuksia-tukevat-opinnot! hoks-id new-oto-values)))

(defn- replace-hato! [hoks-id new-hato-values db-conn]
  (db/delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id hoks-id db-conn)
  (when
   new-hato-values
    (save-hankittavat-ammat-tutkinnon-osat! hoks-id new-hato-values)))

(defn- replace-hpto! [hoks-id new-hpto-values db-conn]
  (db/delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id hoks-id db-conn)
  (when
   new-hpto-values
    (save-hankittavat-paikalliset-tutkinnon-osat! hoks-id new-hpto-values)))

(defn- replace-hyto! [hoks-id new-hyto-values db-conn]
  (db/delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id db-conn)
  (when
   new-hyto-values
    (save-hankittavat-yhteiset-tutkinnon-osat! hoks-id new-hyto-values)))

(defn- replace-ahato! [hoks-id new-ahato-values db-conn]
  (db/delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahato-values
    (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat! hoks-id new-ahato-values)))

(defn- replace-ahpto! [hoks-id new-ahpto-values db-conn]
  (db/delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahpto-values
    (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id new-ahpto-values)))

(defn- replace-ahyto! [hoks-id new-ahyto-values]
  (db/delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)
  (when
   new-ahyto-values
    (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat! hoks-id new-ahyto-values)))

(defn replace-hoks! [hoks-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (replace-main-hoks! hoks-id new-values db-conn)
    ;TODO db-conn should be also used when saving hoks parts, see EH-465
    (replace-oto! hoks-id (:opiskeluvalmiuksia-tukevat-opinnot new-values)
                  db-conn)
    (replace-hato! hoks-id (:hankittavat-ammat-tutkinnon-osat new-values)
                   db-conn)
    (replace-hpto! hoks-id (:hankittavat-paikalliset-tutkinnon-osat new-values)
                   db-conn)
    (replace-hyto! hoks-id (:hankittavat-yhteiset-tutkinnon-osat new-values)
                   db-conn)
    (replace-ahato! hoks-id (:aiemmin-hankitut-ammat-tutkinnon-osat new-values)
                    db-conn)
    (replace-ahpto! hoks-id (:aiemmin-hankitut-paikalliset-tutkinnon-osat
                              new-values)
                    db-conn)
    (replace-ahyto! hoks-id (:aiemmin-hankitut-yhteiset-tutkinnon-osat
                              new-values))))

(defn update-hoks! [hoks-id new-values]
  (db-hoks/update-hoks-by-id! hoks-id new-values))
