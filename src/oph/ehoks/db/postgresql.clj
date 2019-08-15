(ns oph.ehoks.db.postgresql
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn insert-oppija [oppija]
  (db-ops/insert-one! :oppijat (db-ops/to-sql oppija)))

(defn update-oppija! [oid oppija]
  (db-ops/update!
    :oppijat
    (db-ops/to-sql oppija)
    ["oid = ?" oid]))

(defn select-todennettu-arviointi-lisatiedot-by-id [id]
  (first
    (db-ops/query
      [queries/select-todennettu-arviointi-lisatiedot-by-id id]
      {:row-fn h/todennettu-arviointi-lisatiedot-from-sql})))

(defn insert-todennettu-arviointi-lisatiedot! [m]
  (db-ops/insert-one!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql m)))

(defn select-arvioijat-by-todennettu-arviointi-id [id]
  (db-ops/query
    [queries/select-arvioijat-by-todennettu-arviointi-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn insert-todennettu-arviointi-arvioijat! [tta-id arvioija-id]
  (db-ops/insert-one!
    :todennettu_arviointi_arvioijat
    {:todennettu_arviointi_lisatiedot_id tta-id
     :koulutuksen_jarjestaja_osaamisen_arvioija_id arvioija-id}))

(defn insert-koulutuksen-jarjestaja-osaamisen-arvioija! [m]
  (db-ops/insert-one!
    :koulutuksen_jarjestaja_osaamisen_arvioijat
    (h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql m)))

(defn insert-koulutuksen-jarjestaja-osaamisen-arvioijat! [c]
  (db-ops/insert-multi!
    :koulutuksen_jarjestaja_osaamisen_arvioijat
    (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c)))

(defn select-tarkentavat-tiedot-naytto-by-ahpto-id [oopto-id]
  (db-ops/query [queries/select-osaamisen-osoittamiset-by-oopto-id oopto-id]
                {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ooato-id
  "Aiemmin hankitun ammat tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ooato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto! [ooato-id n]
  (db-ops/insert-one!
    :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    {:aiemmin_hankittu_ammat_tutkinnon_osa_id ooato-id
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-ahyto-osaamisen-osoittaminen! [ahyto-id n]
  (db-ops/insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id ahyto-id
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-koodisto-koodi! [m]
  (db-ops/insert-one!
    :koodisto_koodit
    (db-ops/to-sql m)))

(defn insert-osaamisen-osoittamisen-osa-alue! [naytto-id koodi-id]
  (db-ops/insert-one!
    :osaamisen_osoittamisen_osa_alueet
    {:osaamisen_osoittaminen_id naytto-id
     :koodisto_koodi_id koodi-id}))

(defn select-osa-alueet-by-osaamisen-osoittaminen [naytto-id]
  (db-ops/query
    [queries/select-osa-alueet-by-osaamisen-osoittaminen naytto-id]
    {:row-fn h/koodi-uri-from-sql}))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id [id]
  (->
    (db-ops/query [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id
                   id])
    first
    h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id [id]
  (db-ops/query
    [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql}))

(defn insert-aiemmin-hankittu-ammat-tutkinnon-osa! [m]
  (db-ops/insert-one!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql m)))

(defn insert-aiemmin-hankitut-ammat-tutkinnon-osat! [c]
  (db-ops/insert-multi!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (map h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql c)))

(defn select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (db-ops/query
    [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql}))

(defn select-hankittava-paikallinen-tutkinnon-osa-by-id [id]
  (first
    (db-ops/query
      [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-id id]
      {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql})))

(defn insert-hankittavat-paikalliset-tutkinnon-osat! [c]
  (db-ops/insert-multi!
    :hankittavat_paikalliset_tutkinnon_osat
    (map h/hankittava-paikallinen-tutkinnon-osa-to-sql c)))

(defn insert-hankittava-paikallinen-tutkinnon-osa! [m]
  (db-ops/insert-one!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)))

(defn update-hankittava-paikallinen-tutkinnon-osa-by-id! [id m]
  (db-ops/update!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id [hoks-id db-conn]
  (db-ops/shallow-delete!
    :hankittavat_paikalliset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn select-osaamisen-osoittamiset-by-ppto-id
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ppto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn delete-osaamisen-hankkimistavat-by-hpto-id!
  "hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn delete-osaamisen-osoittamiset-by-ppto-id!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn insert-tho-tyotehtavat!
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [tho c]
  (db-ops/insert-multi!
    :tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
    (map
      #(hash-map
         :tyopaikalla_jarjestettava_koulutus_id (:id tho)
         :tyotehtava %)
      c)))

(defn select-tyotehtavat-by-tho-id
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [id]
  (db-ops/query
    [queries/select-tyotehtavat-by-tho-id id]
    {:row-fn h/tyotehtava-from-sql}))

(defn insert-tyopaikalla-jarjestettava-koulutus! [o]
  (when (some? o)
    (let [o-db (db-ops/insert-one!
                 :tyopaikalla_jarjestettavat_koulutukset
                 (h/tyopaikalla-jarjestettava-koulutus-to-sql o))]
      (insert-tho-tyotehtavat! o-db (:keskeiset-tyotehtavat o))
      o-db)))

(defn select-tyopaikalla-jarjestettava-koulutus-by-id [id]
  (first
    (db-ops/query
      [queries/select-tyopaikalla-jarjestettavat-koulutukset-by-id id]
      {:row-fn h/tyopaikalla-jarjestettava-koulutus-from-sql})))

(defn insert-osaamisen-hankkimistavan-muut-oppimisymparistot! [oh c]
  (db-ops/insert-multi!
    :muut_oppimisymparistot
    (map
      #(db-ops/to-sql
         (assoc % :osaamisen-hankkimistapa-id (:id oh)))
      c)))

(defn select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id [id]
  (db-ops/query
    [queries/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id id]
    {:row-fn h/muu-oppimisymparisto-from-sql}))

(defn insert-osaamisen-hankkimistapa! [oh]
  (db-ops/insert-one!
    :osaamisen_hankkimistavat
    (h/osaamisen-hankkimistapa-to-sql oh)))

(defn insert-hankittavan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
  [ppto oh]
  (db-ops/insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_hankkimistapa_id (:id oh)}))

(defn select-osaamisen-hankkimistavat-by-hpto-id
  "hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/query
    [queries/select-osaamisen-hankkmistavat-by-ppto-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-osaamisen-osoittaminen! [m]
  (db-ops/insert-one!
    :osaamisen_osoittamiset
    (h/osaamisen-osoittaminen-to-sql m)))

(defn insert-hpto-osaamisen-osoittaminen!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näyttö"
  [ppto h]
  (db-ops/insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_osoittaminen_id (:id h)}))

(defn insert-ppto-osaamisen-osoittamiset!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [ppto c]
  (let [h-col (db-ops/insert-multi!
                :osaamisen_osoittamiset
                (map h/osaamisen-osoittaminen-to-sql c))]
    (db-ops/insert-multi!
      :hankittavan_paikallisen_tutkinnon_osan_naytto
      (map #(hash-map
              :hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
              :osaamisen_osoittaminen_id (:id %))
           h-col))
    h-col))

(defn insert-osaamisen-osoittamisen-koulutuksen-jarjestaja-osaamisen-arvioija!
  [hon c]
  (let [kja-col (db-ops/insert-multi!
                  :koulutuksen_jarjestaja_osaamisen_arvioijat
                  (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c))]
    (db-ops/insert-multi!
      :osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
      (map #(hash-map
              :osaamisen_osoittaminen_id (:id hon)
              :koulutuksen_jarjestaja_osaamisen_arvioija_id (:id %))
           kja-col))
    kja-col))

(defn select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id
  "Hankitun osaamisen näytön koulutuksen järjestäjän arvioijat"
  [id]
  (db-ops/query
    [queries/select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn insert-tyoelama-arvioija! [arvioija]
  (db-ops/insert-one!
    :tyoelama_osaamisen_arvioijat
    (h/tyoelama-arvioija-to-sql arvioija)))

(defn insert-osaamisen-osoittamisen-tyoelama-arvioija! [hon arvioija]
  (db-ops/insert-one!
    :osaamisen_osoittamisen_tyoelama_arvioija
    {:osaamisen_osoittaminen_id (:id hon)
     :tyoelama_arvioija_id (:id arvioija)}))

(defn select-tyoelama-osaamisen-arvioijat-by-hon-id
  "Hankitun osaamisen näytön työelemän arvioijat"
  [id]
  (db-ops/query
    [queries/select-tyoelama-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/tyoelama-arvioija-from-sql}))

(defn insert-osaamisen-osoittamisen-sisallot! [hon c]
  (db-ops/insert-multi!
    :osaamisen_osoittamisen_sisallot
    (map #(hash-map :osaamisen_osoittaminen_id (:id hon) :sisallon_kuvaus %)
         c)))

(defn select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/sisallon-kuvaus-from-sql}))

(defn insert-osaamisen-osoittamisen-yksilolliset-kriteerit! [hon c]
  (db-ops/insert-multi!
    :osaamisen_osoittamisen_yksilolliset_kriteerit
    (map #(hash-map :osaamisen_osoittaminen_id (:id hon)
                    :yksilollinen_kriteeri %) c)))

(defn select-osaamisen-osoittamisen-kriteerit-by-osaamisen-osoittaminen-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamisen-kriteeri-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/yksilolliset-kriteerit-from-sql}))

(defn insert-nayttoymparisto! [m]
  (db-ops/insert-one!
    :nayttoymparistot
    (db-ops/to-sql m)))

(defn insert-nayttoymparistot! [c]
  (db-ops/insert-multi!
    :nayttoymparistot
    (map db-ops/to-sql c)))

(defn select-nayttoymparisto-by-id [id]
  (first
    (db-ops/query
      [queries/select-nayttoymparistot-by-id id]
      {:row-fn h/nayttoymparisto-from-sql})))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id [id]
  (->
    (db-ops/query
      [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id id])
    first
    h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (db-ops/query
    [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql}))

(defn insert-aiemmin-hankittu-paikallinen-tutkinnon-osa! [m]
  (db-ops/insert-one!
    :aiemmin_hankitut_paikalliset_tutkinnon_osat
    (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql m)))

(defn select-osaamisen-osoittaminen-by-oopto-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-oopto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-ahpto-osaamisen-osoittaminen! [oopto-id naytto-id]
  (db-ops/insert-one!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_paikallinen_tutkinnon_osa_id oopto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-ooyto-arvioija! [yto-id a-id]
  (db-ops/insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id yto-id
     :koulutuksen_jarjestaja_osaamisen_arvioija_id a-id}))

(defn select-arvioija-by-ooyto-id [id]
  (db-ops/query
    [queries/select-arvioijat-by-ooyto-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ahyto-id
  "Aiemmin hankitun yhteisen tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ooyto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ahyto-osa-alue-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ahyto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-ahyto-osa-alue-osaamisen-osoittaminen! [osa-alue-id naytto-id]
  (db-ops/insert-one!
    :aiemmin_hankitun_yto_osa_alueen_naytto
    {:aiemmin_hankittu_yto_osa_alue_id osa-alue-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osa-alueet-by-ahyto-id [id]
  (db-ops/query
    [queries/select-osa-alueet-by-ooyto-id id]
    {:row-fn h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue! [m]
  (db-ops/insert-one!
    :aiemmin_hankitut_yto_osa_alueet
    (h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql m)))

(defn insert-aiemmin-hankittu-yhteinen-tutkinnon-osa! [m]
  (db-ops/insert-one!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id [id]
  (->
    (db-ops/query [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-id
                   id])
    first
    h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (db-ops/query
    [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql}))

(defn insert-hankittava-ammat-tutkinnon-osa! [m]
  (db-ops/insert-one!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)))

(defn select-hankittava-ammat-tutkinnon-osa-by-id [id]
  (->
    (db-ops/query
      [queries/select-hankittavat-ammat-tutkinnon-osat-by-id id])
    first
    h/hankittava-ammat-tutkinnon-osa-from-sql))

(defn select-hankittavat-ammat-tutkinnon-osat-by-hoks-id [id]
  (db-ops/query
    [queries/select-hankittavat-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-ammat-tutkinnon-osa-from-sql}))

(defn insert-hato-osaamisen-osoittaminen! [hato-id naytto-id]
  (db-ops/insert-one!
    :hankittavan_ammat_tutkinnon_osan_naytto
    {:hankittava_ammat_tutkinnon_osa_id hato-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osaamisen-osoittamiset-by-hato-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-pato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn delete-osaamisen-hankkimistavat-by-hato-id!
  "Hankittavan ammatillisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_ammat_tutkinnon_osa_id = ?" id]))

(defn delete-osaamisen-osoittamiset-by-pato-id!
  "Hankittavan ammatillisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_ammat_tutkinnon_osan_naytto
    ["hankittava_ammat_tutkinnon_osa_id = ?" id]))

(defn update-hankittava-ammat-tutkinnon-osa-by-id! [id m]
  (db-ops/update!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-hankittava-yhteinen-tutkinnon-osa-by-id! [hyto-id new-values]
  (db-ops/update!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" hyto-id]))

(defn delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id
  [hoks-id db-conn]
  (db-ops/shallow-delete!
    :hankittavat_ammat_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-hyto-osa-alueet! [hyto-id]
  (db-ops/shallow-delete!
    :yhteisen_tutkinnon_osan_osa_alueet
    ["yhteinen_tutkinnon_osa_id = ?" hyto-id]))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id! [id new-values]
  (db-ops/update!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id! [id new-values]
  (when-let
   [new-ahpt (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql new-values)]
    (db-ops/update!
      :aiemmin_hankitut_paikalliset_tutkinnon_osat
      new-ahpt
      ["id = ? AND deleted_at IS NULL" id])))

(defn update-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id! [id new-values]
  (db-ops/update!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-todennettu-arviointi-lisatiedot-by-id! [id new-values]
  (db-ops/update!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn delete-todennettu-arviointi-arvioijat-by-tta-id! [id]
  (db-ops/shallow-delete!
    :todennettu_arviointi_arvioijat
    ["todennettu_arviointi_lisatiedot_id = ?" id]))

(defn delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id! [id]
  (db-ops/shallow-delete!
    :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    ["aiemmin_hankittu_ammat_tutkinnon_osa_id = ?" id]))

(defn delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
  [hoks-id db-conn]
  (db-ops/shallow-delete!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id! [id]
  (db-ops/shallow-delete!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    ["aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ?" id]))

(defn delete-aiemmin-hankitun-yhteisen-tutkinnon-osan-naytto-by-id! [id]
  (db-ops/shallow-delete!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ?" id]))

(defn delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
  [hoks-id db-conn]
  (db-ops/shallow-delete!
    :aiemmin_hankitut_paikalliset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id [hoks-id]
  (db-ops/shallow-delete!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    ["hoks_id = ?" hoks-id]))

(defn delete-aiemmin-hankitut-yto-osa-alueet-by-id! [id]
  (db-ops/shallow-delete!
    :aiemmin_hankitut_yto_osa_alueet
    ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ?" id]))

(defn insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
  [pato-id oh-id]
  (db-ops/insert-one!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_ammat_tutkinnon_osa_id pato-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn select-osaamisen-hankkimistavat-by-hato-id
  "hankittavan ammat tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/query
    [queries/select-osaamisen-hankkmistavat-by-pato-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-opiskeluvalmiuksia-tukeva-opinto! [new-value]
  (db-ops/insert-one!
    :opiskeluvalmiuksia_tukevat_opinnot
    (db-ops/to-sql new-value)))

(defn insert-opiskeluvalmiuksia-tukevat-opinnot! [c]
  (db-ops/insert-multi!
    :opiskeluvalmiuksia_tukevat_opinnot
    (mapv db-ops/to-sql c)))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-id [oto-id]
  (->
    (db-ops/query [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-id
                   oto-id])
    first
    h/opiskeluvalmiuksia-tukevat-opinnot-from-sql))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id [id]
  (db-ops/query
    [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id id]
    {:row-fn h/opiskeluvalmiuksia-tukevat-opinnot-from-sql}))

(defn delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id [hoks-id db-conn]
  (db-ops/shallow-delete!
    :opiskeluvalmiuksia_tukevat_opinnot
    ["hoks_id = ?" hoks-id] db-conn))

(defn update-opiskeluvalmiuksia-tukevat-opinnot-by-id! [oto-id new-values]
  (db-ops/update!
    :opiskeluvalmiuksia_tukevat_opinnot
    (db-ops/to-sql new-values)
    ["id = ? AND deleted_at IS NULL" oto-id]))

(defn insert-hankittava-yhteinen-tutkinnon-osa! [m]
  (db-ops/insert-one!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-hankittava-yhteinen-tutkinnon-osa-by-id [hyto-id]
  (->
    (db-ops/query [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-id
                   hyto-id])
    first
    h/hankittava-yhteinen-tutkinnon-osa-from-sql))

(defn select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (db-ops/query
    [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-yhteinen-tutkinnon-osa-from-sql}))

(defn select-osaamisen-hankkimistavat-by-hyto-osa-alue-id [id]
  (db-ops/query
    [queries/select-osaamisen-hankkimistavat-by-yto-osa-alue-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-hyto-osa-alueen-osaamisen-hankkimistapa! [hyto-osa-alue-id oh-id]
  (db-ops/insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    {:yhteisen_tutkinnon_osan_osa_alue_id hyto-osa-alue-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn insert-yhteisen-tutkinnon-osan-osa-alue! [osa-alue]
  (db-ops/insert-one!
    :yhteisen_tutkinnon_osan_osa_alueet
    (h/yhteisen-tutkinnon-osan-osa-alue-to-sql osa-alue)))

(defn delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id [hoks-id db-conn]
  (db-ops/shallow-delete!
    :hankittavat_yhteiset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn select-yto-osa-alueet-by-yto-id [id]
  (db-ops/query
    [queries/select-yto-osa-alueet-by-yto-id id]
    {:row-fn h/yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn insert-yto-osa-alueen-osaamisen-osoittaminen! [yto-id naytto-id]
  (db-ops/insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_naytot
    {:yhteisen_tutkinnon_osan_osa_alue_id yto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osaamisen-osoittamiset-by-yto-osa-alue-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-yto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-oppilaitos-oids []
  (db-ops/query
    [queries/select-oppilaitos-oids]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-oppilaitos-oids-by-koulutustoimija-oid [oid]
  (db-ops/query
    [queries/select-oppilaitos-oids-by-koulutustoimija-oid oid]
    {:row-fn h/oppilaitos-oid-from-sql}))
