(ns oph.ehoks.db.postgresql.aiemmin-hankitut
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-all-ahatos-for-hoks
  "Aiemmin hankittujen ammatillisten tutkintojen osat"
  [id]
  (db-ops/query [queries/select-all-ahatos-for-hoks id]))

(defn select-all-ahptos-for-hoks
  "Aiemmin hankittujen paikallisten tutkintojen osat"
  [id]
  (db-ops/query [queries/select-all-ahptos-for-hoks id]))

(defn select-all-osa-alueet-for-ahyto
  "Aiemmin hankitun yhteisen tutkinnon osan osa-alueet"
  [id]
  (db-ops/query [queries/select-all-osa-alueet-for-ahyto id]))

(defn select-all-ahytos-for-hoks
  "Aiemmin hankittujen yhteisten tutkintojen osat"
  [id]
  (db-ops/query [queries/select-all-ahytos-for-hoks id]))

(defn select-todennettu-arviointi-lisatiedot-by-id
  "Todennetun arvioinnin lisätiedot"
  [id]
  (first
    (db-ops/query
      [queries/select-todennettu-arviointi-lisatiedot-by-id id]
      {:row-fn h/todennettu-arviointi-lisatiedot-from-sql})))

(defn select-arvioijat-by-todennettu-arviointi-id
  "Todennetun arvioinnin arvioijat"
  [id]
  (db-ops/query
    [queries/select-arvioijat-by-todennettu-arviointi-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id
  "Aiemmin hankittujen ammatillisten tutkintojen osat"
  [id]
  (->
    (db-ops/query [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id
                   id])
    first
    h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id
  "Aiemmin hankitut paikalliset tutkinnon osat"
  [id]
  (->
    (db-ops/query
      [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id id])
    first
    h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id
  "Aiemmin hankitun yhteisen tutkinnon osa"
  [id]
  (->
    (db-ops/query [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-id
                   id])
    first
    h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql))

(defn insert-ahpto-osaamisen-osoittaminen!
  "Lisää aiemmin hankitun paikallisen tutkinnon osan osaamisen osoittaminen"
  ([oopto-id naytto-id]
    (db-ops/insert-one!
      :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
      {:aiemmin_hankittu_paikallinen_tutkinnon_osa_id oopto-id
       :osaamisen_osoittaminen_id naytto-id}))
  ([oopto-id naytto-id db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
      {:aiemmin_hankittu_paikallinen_tutkinnon_osa_id oopto-id
       :osaamisen_osoittaminen_id naytto-id}
      db-conn)))

(defn insert-todennettu-arviointi-arvioijat!
  "Lisää arvioijat todennetulle arvioinnille"
  ([tta-id arvioija-id]
    (db-ops/insert-one!
      :todennettu_arviointi_arvioijat
      {:todennettu_arviointi_lisatiedot_id tta-id
       :koulutuksen_jarjestaja_osaamisen_arvioija_id arvioija-id}))
  ([tta-id arvioija-id db-conn]
    (db-ops/insert-one!
      :todennettu_arviointi_arvioijat
      {:todennettu_arviointi_lisatiedot_id tta-id
       :koulutuksen_jarjestaja_osaamisen_arvioija_id arvioija-id}
      db-conn)))

(defn insert-koulutuksen-jarjestaja-osaamisen-arvioijat!
  "Lisää osaamisen arvioijat koulutuksen järjestäjälle"
  ([c]
    (db-ops/insert-multi!
      :koulutuksen_jarjestaja_osaamisen_arvioijat
      (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c)))
  ([c db-conn]
    (db-ops/insert-multi!
      :koulutuksen_jarjestaja_osaamisen_arvioijat
      (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c)
      db-conn)))

(defn insert-ahyto-osaamisen-osoittaminen!
  "Lisää aiemmin hankitun yhteisen tutkinnon osan osaamisen osoittaminen"
  ([ahyto-id n]
    (db-ops/insert-one!
      :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
      {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id ahyto-id
       :osaamisen_osoittaminen_id (:id n)}))
  ([ahyto-id n db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
      {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id ahyto-id
       :osaamisen_osoittaminen_id (:id n)}
      db-conn)))

(defn insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue!
  "Lisää aiemmin hankitun yhteisen tutkinnon osan osa-alue"
  ([m]
    (db-ops/insert-one!
      :aiemmin_hankitut_yto_osa_alueet
      (h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql m)))
  ([m db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitut_yto_osa_alueet
      (h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql m)
      db-conn)))

(defn insert-ahyto-osa-alue-osaamisen-osoittaminen!
  "Lisää aiemmin hankitun yhteisen tutkinnon osan osa-alueen osaamisen
  osoittaminen"
  ([osa-alue-id naytto-id]
    (db-ops/insert-one!
      :aiemmin_hankitun_yto_osa_alueen_naytto
      {:aiemmin_hankittu_yto_osa_alue_id osa-alue-id
       :osaamisen_osoittaminen_id naytto-id}))
  ([osa-alue-id naytto-id db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitun_yto_osa_alueen_naytto
      {:aiemmin_hankittu_yto_osa_alue_id osa-alue-id
       :osaamisen_osoittaminen_id naytto-id}
      db-conn)))

(defn- ensure-lahetetty-arvioitavaksi-exists
  "Varmistaa, että :lahetetty-arvioitavaksi kenttä on olemassa, vaikka se on
  nil"
  [tarkentavat-tiedot-arvioija]
  (if (:lahetetty-arvioitavaksi tarkentavat-tiedot-arvioija)
    tarkentavat-tiedot-arvioija
    (assoc tarkentavat-tiedot-arvioija :lahetetty-arvioitavaksi nil)))

(defn insert-todennettu-arviointi-lisatiedot!
  "Lisää todennetun arvioinnin lisätiedot"
  ([tarkentavat-tiedot-arvioija]
    (let [refined-arvioija
          (ensure-lahetetty-arvioitavaksi-exists tarkentavat-tiedot-arvioija)]
      (db-ops/insert-one! :todennettu_arviointi_lisatiedot
                          (h/todennettu-arviointi-lisatiedot-to-sql
                            refined-arvioija))))
  ([tarkentavat-tiedot-arvioija db-conn]
    (let [refined-arvioija
          (ensure-lahetetty-arvioitavaksi-exists tarkentavat-tiedot-arvioija)]
      (db-ops/insert-one! :todennettu_arviointi_lisatiedot
                          (h/todennettu-arviointi-lisatiedot-to-sql
                            refined-arvioija)
                          db-conn))))

(defn insert-aiemmin-hankittu-yhteinen-tutkinnon-osa!
  "Lisää aiemmin hankitun yhteisen tutkinnon osa"
  ([m]
    (db-ops/insert-one!
      :aiemmin_hankitut_yhteiset_tutkinnon_osat
      (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql m)))
  ([m db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitut_yhteiset_tutkinnon_osat
      (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql m)
      db-conn)))

(defn insert-aiemmin-hankittu-paikallinen-tutkinnon-osa!
  "Lisää aiemmin hankitun paikallisen tutkinnon osa"
  ([m]
    (db-ops/insert-one!
      :aiemmin_hankitut_paikalliset_tutkinnon_osat
      (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql m)))
  ([m db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitut_paikalliset_tutkinnon_osat
      (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql m)
      db-conn)))

(defn insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto!
  "Lisää aiemmin hankitun ammatillisen tutkinnon osan näyttö"
  ([ooato-id n]
    (db-ops/insert-one!
      :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
      {:aiemmin_hankittu_ammat_tutkinnon_osa_id ooato-id
       :osaamisen_osoittaminen_id (:id n)}))
  ([ooato-id n db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
      {:aiemmin_hankittu_ammat_tutkinnon_osa_id ooato-id
       :osaamisen_osoittaminen_id (:id n)}
      db-conn)))

(defn insert-aiemmin-hankittu-ammat-tutkinnon-osa!
  "Lisää aiemmin hankitun ammatillisen tutkinnon osa"
  ([m]
    (db-ops/insert-one!
      :aiemmin_hankitut_ammat_tutkinnon_osat
      (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql m)))
  ([m db-conn]
    (db-ops/insert-one!
      :aiemmin_hankitut_ammat_tutkinnon_osat
      (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql m)
      db-conn)))

(defn delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id!
  "Poista aiemmin hankitun ammatillisen tutkinnon osan näyttö"
  ([id]
    (db-ops/soft-delete!
      :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
      ["aiemmin_hankittu_ammat_tutkinnon_osa_id = ? AND deleted_at IS NULL"
       id]))
  ([id db-conn]
    (db-ops/soft-delete!
      :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
      ["aiemmin_hankittu_ammat_tutkinnon_osa_id = ? AND deleted_at IS NULL" id]
      db-conn)))

(defn delete-todennettu-arviointi-arvioijat-by-tta-id!
  "Poista todennetun arvioinnin arvioija"
  ([id]
    (db-ops/soft-delete!
      :todennettu_arviointi_arvioijat
      ["todennettu_arviointi_lisatiedot_id = ? AND deleted_at IS NULL" id]))
  ([id db-conn]
    (db-ops/soft-delete!
      :todennettu_arviointi_arvioijat
      ["todennettu_arviointi_lisatiedot_id = ? AND deleted_at IS NULL" id]
      db-conn)))

(defn update-todennettu-arviointi-lisatiedot-by-id!
  "Päivitä todennetun arvioinnin lisätiedot"
  ([id new-values]
    (db-ops/update!
      :todennettu_arviointi_lisatiedot
      (h/todennettu-arviointi-lisatiedot-to-sql new-values)
      ["id = ? AND deleted_at IS NULL" id]))
  ([id new-values db-conn]
    (db-ops/update!
      :todennettu_arviointi_lisatiedot
      (h/todennettu-arviointi-lisatiedot-to-sql new-values)
      ["id = ? AND deleted_at IS NULL" id] db-conn)))

(defn delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id!
  "Poista aiemmin hankitun paikallisen tutkinnon osan näyttö"
  ([id]
    (db-ops/soft-delete!
      :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
      [(str "aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ? "
            "AND deleted_at IS NULL")
       id]))
  ([id db-conn]
    (db-ops/soft-delete!
      :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
      [(str "aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ? "
            "AND deleted_at IS NULL")
       id]
      db-conn)))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id!
  "Päivitä aiemmin hankitun paikallisen tutkinnon osa"
  ([id new-values]
    (when-let
     [new-ahpt (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql
                 new-values)]
      (db-ops/update!
        :aiemmin_hankitut_paikalliset_tutkinnon_osat
        new-ahpt
        ["id = ? AND deleted_at IS NULL" id])))
  ([id new-values db-conn]
    (when-let
     [new-ahpt (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql
                 new-values)]
      (db-ops/update!
        :aiemmin_hankitut_paikalliset_tutkinnon_osat
        new-ahpt
        ["id = ? AND deleted_at IS NULL" id] db-conn))))

(defn delete-aiemmin-hankitun-yhteisen-tutkinnon-osan-naytto-by-id!
  "Poista aiemmin hankitun yhteisen tutkinnon osan näyttö"
  ([id]
    (db-ops/soft-delete!
      :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
      ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ? AND deleted_at IS NULL"
       id]))
  ([id db-conn]
    (db-ops/soft-delete!
      :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
      ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ? AND deleted_at IS NULL"
       id]
      db-conn)))

(defn delete-aiemmin-hankitut-yto-osa-alueet-by-id!
  "Poista aiemmin hankitun yhteisen tutkinnon osan osa-alue"
  ([id]
    (db-ops/soft-delete!
      :aiemmin_hankitut_yto_osa_alueet
      ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ? AND deleted_at IS NULL"
       id]))
  ([id db-conn]
    (db-ops/soft-delete!
      :aiemmin_hankitut_yto_osa_alueet
      ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ? AND deleted_at IS NULL"
       id]
      db-conn)))

(defn update-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id!
  "Päivitä aiemmin hankitun yhteisen tutkinnon osa"
  ([id new-values]
    (db-ops/update!
      :aiemmin_hankitut_yhteiset_tutkinnon_osat
      (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql new-values)
      ["id = ? AND deleted_at IS NULL" id]))
  ([id new-values db-conn]
    (db-ops/update!
      :aiemmin_hankitut_yhteiset_tutkinnon_osat
      (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql new-values)
      ["id = ? AND deleted_at IS NULL" id] db-conn)))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id!
  "Päivitä aiemmin hankitun ammatillisen tutkinnon osa"
  ([id new-values]
    (db-ops/update!
      :aiemmin_hankitut_ammat_tutkinnon_osat
      (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql new-values)
      ["id = ? AND deleted_at IS NULL" id]))
  ([id new-values db-conn]
    (db-ops/update!
      :aiemmin_hankitut_ammat_tutkinnon_osat
      (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql new-values)
      ["id = ? AND deleted_at IS NULL" id] db-conn)))

(defn delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
  "Poista aiemmin hankitut ammatilliset tutkinnon osat"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))

(defn delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
  "Poista aiemmin hankitut paikalliset tutkinnon osat"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :aiemmin_hankitut_paikalliset_tutkinnon_osat
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))

(defn delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id
  "Poista aiemmin hankitut yhteiset tutkinnon osat"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))
