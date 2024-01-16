(ns oph.ehoks.db.postgresql.hankittavat
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]))

(defn select-hankittava-paikallinen-tutkinnon-osa-by-id
  "Hankittava paikallisen tutkinnon osa"
  [id]
  (first
    (db-ops/query
      [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-id id]
      {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql})))

(defn select-osaamisen-hankkimistapa-by-id
  "Osaamisen hankkimistapa"
  [id]
  (db-ops/query [queries/select-osaamisen-hankkimistapa-by-id id]))

(defn select-osaamisen-hankkimistavat-by-module-id
  "Osaamisen hankkimistavat"
  [uuid]
  (db-ops/query
    [queries/select-osaamisen-hankkimistavat-by-module-id uuid]))

(defn select-osaamisen-hankkimistavat-by-hoks-id-and-tunniste
  "Hoksin osaamistapa tunnisteella"
  [oh-type hoks-id tunniste conn]
  (db-ops/query-in-tx
    [(case oh-type
       :hato
       queries/select-hato-osaamisen-hankkimistavat-by-hoks-id-and-tunniste
       :hpto
       queries/select-hpto-osaamisen-hankkimistavat-by-hoks-id-and-tunniste
       :hyto-osa-alue
       queries/select-hyto-osaamisen-hankkimistavat-by-hoks-id-and-tunniste)
     hoks-id
     tunniste]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}
    conn))

(defn select-osaamisen-osoittamiset-by-module-id
  "Hankittavan ammatillisen tutkinnon osan osaamisen osoittamiset"
  [uuid]
  (db-ops/query [queries/select-osaamisen-osoittamiset-by-module-id uuid]))

(defn select-hankittava-yhteinen-tutkinnon-osa-by-id
  "Hankittava yhteisen tutkinnon osa"
  [hyto-id]
  (->
    (db-ops/query [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-id
                   hyto-id])
    first
    h/hankittava-yhteinen-tutkinnon-osa-from-sql))

(defn select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id
  "Hankittavat yhteisen tutkinnon osat"
  [id]
  (db-ops/query
    [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-yhteinen-tutkinnon-osa-from-sql}))

(defn select-hankittavat-koulutuksen-osat-by-hoks-id
  "Hankittavat koulutuksen osat"
  [id]
  (db-ops/query
    [queries/select-hankittavat-koulutuksen-osat-by-hoks-id id]
    {:row-fn h/hankittava-koulutuksen-osa-from-sql}))

(defn select-all-hatos-for-hoks
  "Hankittavat ammatillisen tutkinnon osat"
  [id]
  (db-ops/query [queries/select-all-hatos-for-hoks id]))

(defn select-one-hato
  "Hankittava ammatillinen tutkinnon osa"
  [id]
  (db-ops/query [queries/select-one-hato id]))

(defn select-all-hptos-for-hoks
  "Hankittavat paikallisen tutkinnon osat"
  [id]
  (db-ops/query [queries/select-all-hptos-for-hoks id]))

(defn select-one-hpto
  "hankittava paikallinen tutkinnon osa"
  [id]
  (db-ops/query [queries/select-one-hpto id]))

(defn select-all-osa-alueet-for-yto
  "Hankittavat yhteisen tutkinnon osan osa-alueet"
  [id]
  (db-ops/query [queries/select-all-osa-alueet-for-yto id]))

(defn insert-tho-tyotehtavat!
  "Lisää työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  ([tho c]
    (db-ops/insert-multi!
      :tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
      (map
        #(hash-map
           :tyopaikalla_jarjestettava_koulutus_id (:id tho)
           :tyotehtava %)
        c)))
  ([tho c db-conn]
    (db-ops/insert-multi!
      :tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
      (map
        #(hash-map
           :tyopaikalla_jarjestettava_koulutus_id (:id tho)
           :tyotehtava %)
        c)
      db-conn)))

(defn insert-tyopaikalla-jarjestettava-koulutus!
  "Lisää työpaikalla järjestettävä koulutus"
  ([o]
    (insert-tyopaikalla-jarjestettava-koulutus! o (db-ops/get-db-connection)))
  ([o db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (when (some? o)
        (let [o-db (db-ops/insert-one!
                     :tyopaikalla_jarjestettavat_koulutukset
                     (h/tyopaikalla-jarjestettava-koulutus-to-sql o)
                     conn)]
          (insert-tho-tyotehtavat! o-db (:keskeiset-tyotehtavat o) conn)
          o-db)))))

(defn insert-osaamisen-hankkimistapa!
  "Lisää osaamisen hankkimistapa"
  ([oh]
    (db-ops/insert-one!
      :osaamisen_hankkimistavat
      (h/osaamisen-hankkimistapa-to-sql oh)))
  ([oh db-conn]
    (db-ops/insert-one!
      :osaamisen_hankkimistavat
      (h/osaamisen-hankkimistapa-to-sql oh)
      db-conn)))

(defn insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
  "Lisää osaamisen hankkimistavan muut oppimisympäristöt"
  ([oh c]
    (db-ops/insert-multi!
      :muut_oppimisymparistot
      (map
        #(db-ops/to-sql
           (assoc % :osaamisen-hankkimistapa-id (:id oh)))
        c)))
  ([oh c db-conn]
    (db-ops/insert-multi!
      :muut_oppimisymparistot
      (map
        #(db-ops/to-sql
           (assoc % :osaamisen-hankkimistapa-id (:id oh)))
        c)
      db-conn)))

(defn insert-osaamisen-hankkimistavan-keskeytymisajanjaksot!
  "Lisää osaamisen hankkimistavan keskeytymisajanjaksot"
  ([oh c]
    (db-ops/insert-multi!
      :keskeytymisajanjaksot
      (map
        #(db-ops/to-sql
           (assoc % :osaamisen-hankkimistapa-id (:id oh)))
        c)))
  ([oh c db-conn]
    (db-ops/insert-multi!
      :keskeytymisajanjaksot
      (map
        #(db-ops/to-sql
           (assoc % :osaamisen-hankkimistapa-id (:id oh)))
        c)
      db-conn)))

(defn insert-hpto-osaamisen-hankkimistapa!
  "Lisää hankittavan paikallisen tutkinnon osan osaamisen hankkimistapa"
  ([ppto oh]
    (db-ops/insert-one!
      :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
      {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
       :osaamisen_hankkimistapa_id (:id oh)}))
  ([ppto oh db-conn]
    (db-ops/insert-one!
      :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
      {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
       :osaamisen_hankkimistapa_id (:id oh)}
      db-conn)))

(defn delete-osaamisen-hankkimistavat-by-hpto-id!
  "Poista hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id db-conn]
  (db-ops/soft-delete!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id] db-conn))

(defn insert-hpto-osaamisen-osoittaminen!
  "Lisää hankittavan paikallisen tutkinnon osan hankitun osaamisen näyttö"
  ([ppto h]
    (db-ops/insert-one!
      :hankittavan_paikallisen_tutkinnon_osan_naytto
      {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
       :osaamisen_osoittaminen_id (:id h)}))
  ([ppto h db-conn]
    (db-ops/insert-one!
      :hankittavan_paikallisen_tutkinnon_osan_naytto
      {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
       :osaamisen_osoittaminen_id (:id h)}
      db-conn)))

(defn delete-osaamisen-osoittamiset-by-ppto-id!
  "Poista hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id db-conn]
  (db-ops/soft-delete!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id] db-conn))

(defn update-osaamisen-hankkimistapa!
  "Muokkaa osaamisen hankkimistapa"
  ([id oh]
    (db-ops/update!
      :osaamisen_hankkimistavat
      (h/osaamisen-hankkimistapa-to-sql (assoc oh
                                               :updated_at
                                               (java.util.Date.)))
      ["id = ?" id]))
  ([id oh db-conn]
    (db-ops/update!
      :osaamisen_hankkimistavat
      (h/osaamisen-hankkimistapa-to-sql (assoc oh
                                               :updated_at
                                               (java.util.Date.)))
      ["id = ?" id]
      db-conn)))

(defn update-hankittava-paikallinen-tutkinnon-osa-by-id!
  "Päivitä hankittavan paikallisen tutkinnon osa"
  [id m db-conn]
  (db-ops/update!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id] db-conn))

(defn insert-yto-osa-alueen-osaamisen-osoittaminen!
  "Lisää yhteisen tutkinnon osan osa-alueen osaamisen näytöt"
  ([yto-id naytto-id]
    (db-ops/insert-one!
      :yhteisen_tutkinnon_osan_osa_alueen_naytot
      {:yhteisen_tutkinnon_osan_osa_alue_id yto-id
       :osaamisen_osoittaminen_id naytto-id}))
  ([yto-id naytto-id db-conn]
    (db-ops/insert-one!
      :yhteisen_tutkinnon_osan_osa_alueen_naytot
      {:yhteisen_tutkinnon_osan_osa_alue_id yto-id
       :osaamisen_osoittaminen_id naytto-id}
      db-conn)))

(defn insert-hankittava-paikallinen-tutkinnon-osa!
  "Lisää hankittavan paikallisen tutkinnon osa"
  ([m]
    (db-ops/insert-one!
      :hankittavat_paikalliset_tutkinnon_osat
      (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)))
  ([m db-conn]
    (db-ops/insert-one!
      :hankittavat_paikalliset_tutkinnon_osat
      (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)
      db-conn)))

(defn insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
  "Lisää hankittavan ammatillisen tutkinnon osan osaamisen hankkimistapa"
  ([pato-id oh-id]
    (db-ops/insert-one!
      :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
      {:hankittava_ammat_tutkinnon_osa_id pato-id
       :osaamisen_hankkimistapa_id oh-id}))
  ([pato-id oh-id db-conn]
    (db-ops/insert-one!
      :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
      {:hankittava_ammat_tutkinnon_osa_id pato-id
       :osaamisen_hankkimistapa_id oh-id}
      db-conn)))

(defn insert-hato-osaamisen-osoittaminen!
  "Lisää hankittavan ammatillisen tutkinnon osan osaamisen näyttö"
  ([hato-id naytto-id]
    (db-ops/insert-one!
      :hankittavan_ammat_tutkinnon_osan_naytto
      {:hankittava_ammat_tutkinnon_osa_id hato-id
       :osaamisen_osoittaminen_id naytto-id}))
  ([hato-id naytto-id db-conn]
    (db-ops/insert-one!
      :hankittavan_ammat_tutkinnon_osan_naytto
      {:hankittava_ammat_tutkinnon_osa_id hato-id
       :osaamisen_osoittaminen_id naytto-id}
      db-conn)))

(defn insert-hankittava-ammat-tutkinnon-osa!
  "Lisää hankittavan ammatillisen tutkinnon osa"
  ([m]
    (db-ops/insert-one!
      :hankittavat_ammat_tutkinnon_osat
      (h/hankittava-ammat-tutkinnon-osa-to-sql m)))
  ([m db-conn]
    (db-ops/insert-one!
      :hankittavat_ammat_tutkinnon_osat
      (h/hankittava-ammat-tutkinnon-osa-to-sql m)
      db-conn)))

(defn delete-osaamisen-hankkimistavat-by-hato-id!
  "Poista hankittavan ammatillisen tutkinnon osan osaamisen hankkimistavat"
  [id db-conn]
  (db-ops/soft-delete!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_ammat_tutkinnon_osa_id = ?" id] db-conn))

(defn delete-osaamisen-osoittamiset-by-pato-id!
  "Poista hankittavan ammatillisen tutkinnon osan hankitun osaamisen näytöt"
  [id db-conn]
  (db-ops/soft-delete!
    :hankittavan_ammat_tutkinnon_osan_naytto
    ["hankittava_ammat_tutkinnon_osa_id = ?" id] db-conn))

(defn update-hankittava-ammat-tutkinnon-osa-by-id!
  "Päivitä hankittavan ammatillisen tutkinnon osa"
  [id m db-conn]
  (db-ops/update!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id] db-conn))

(defn insert-hyto-osa-alueen-osaamisen-hankkimistapa!
  "Lisää hankittavan yhteisen tutkinnon osan osa-alueen osaamisen hankkimistapa"
  ([hyto-osa-alue-id oh-id]
    (db-ops/insert-one!
      :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
      {:yhteisen_tutkinnon_osan_osa_alue_id hyto-osa-alue-id
       :osaamisen_hankkimistapa_id oh-id}))
  ([hyto-osa-alue-id oh-id db-conn]
    (db-ops/insert-one!
      :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
      {:yhteisen_tutkinnon_osan_osa_alue_id hyto-osa-alue-id
       :osaamisen_hankkimistapa_id oh-id}
      db-conn)))

(defn insert-yhteisen-tutkinnon-osan-osa-alue!
  "Lisää yhteisen tutkinnon osan osa-alue"
  ([osa-alue]
    (db-ops/insert-one!
      :yhteisen_tutkinnon_osan_osa_alueet
      (h/yhteisen-tutkinnon-osan-osa-alue-to-sql osa-alue)))
  ([osa-alue db-conn]
    (db-ops/insert-one!
      :yhteisen_tutkinnon_osan_osa_alueet
      (h/yhteisen-tutkinnon-osan-osa-alue-to-sql osa-alue)
      db-conn)))

(defn insert-hankittava-yhteinen-tutkinnon-osa!
  "Lisää hankittavan yhteisen tutkinnon osa"
  ([m]
    (db-ops/insert-one!
      :hankittavat_yhteiset_tutkinnon_osat
      (h/hankittava-yhteinen-tutkinnon-osa-to-sql m)))
  ([m db-conn]
    (db-ops/insert-one!
      :hankittavat_yhteiset_tutkinnon_osat
      (h/hankittava-yhteinen-tutkinnon-osa-to-sql m)
      db-conn)))

(defn insert-hankittava-koulutuksen-osa!
  "Lisää hankittavan koulutuksen osa"
  ([koulutuksen-osa]
    (db-ops/insert-one!
      :hankittavat_koulutuksen_osat
      (db-ops/to-sql koulutuksen-osa)))
  ([koulutuksen-osa db-conn]
    (db-ops/insert-one!
      :hankittavat_koulutuksen_osat
      (db-ops/to-sql koulutuksen-osa)
      db-conn)))

(defn delete-hyto-osa-alueet!
  "Poista hankittavan yhteisen tutkinnon osan osa-alueet"
  [hyto-id db-conn]
  (db-ops/soft-delete!
    :yhteisen_tutkinnon_osan_osa_alueet
    ["yhteinen_tutkinnon_osa_id = ? AND deleted_at IS NULL" hyto-id] db-conn))

(defn update-hankittava-yhteinen-tutkinnon-osa-by-id!
  "Päivitä hankittavan yhteisen tutkinnon osa"
  [hyto-id new-values db-conn]
  (db-ops/update!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" hyto-id] db-conn))

(defn delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id
  "Poista hankittavat ammatillisen tutkinnon osat"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :hankittavat_ammat_tutkinnon_osat
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))

(defn delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id
  "Poista hankittavat paikalliset tutkinnot osat"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :hankittavat_paikalliset_tutkinnon_osat
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))

(defn delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id
  "Poista hankittavat yhteiset tutkinnon osat"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :hankittavat_yhteiset_tutkinnon_osat
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))

(defn delete-hankittavat-koulutuksen-osat-by-hoks-id
  "Poista hankittavat koulutuksen osat"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :hankittavat_koulutuksen_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-osaamisen-hankkimistavan-muut-oppimisymparistot
  "Poista osaamisen hankkimistavan muut oppimisympäristöt"
  [oht-id db-conn]
  (db-ops/soft-delete!
    :muut_oppimisymparistot
    ["osaamisen_hankkimistapa_id = ? AND deleted_at IS NULL" (:id oht-id)]
    db-conn))

(defn delete-osaamisen-hankkimistavan-keskeytymisajanjaksot
  "Poista osaamisen hankkimistavan keskeytymisajanjaksot"
  [oht-id db-conn]
  (db-ops/soft-delete!
    :keskeytymisajanjaksot
    ["osaamisen_hankkimistapa_id = ? AND deleted_at IS NULL" (:id oht-id)]
    db-conn))

(defn delete-tyopaikalla-jarjestettava-koulutus
  "Poistaa työpaikalla järjestettävän koulutuksen tietokannasta."
  [tjk-id db-conn]
  (db-ops/soft-delete! :tyopaikalla_jarjestettavat_koulutukset
                       ["id = ? AND deleted_at IS NULL" tjk-id]
                       db-conn))
