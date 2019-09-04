(ns oph.ehoks.db.postgresql.aiemmin-hankitut
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-tarkentavat-tiedot-naytto-by-ahato-id
  "Aiemmin hankitun ammatillisen tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ahato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-todennettu-arviointi-lisatiedot-by-id [id]
  "Todennetun arvioinnin lisätiedot"
  (first
    (db-ops/query
      [queries/select-todennettu-arviointi-lisatiedot-by-id id]
      {:row-fn h/todennettu-arviointi-lisatiedot-from-sql})))

(defn select-arvioijat-by-todennettu-arviointi-id [id]
  "Todennetun arvioinnin arvioijat"
  (db-ops/query
    [queries/select-arvioijat-by-todennettu-arviointi-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id [id]
  "Aiemmin hankittujen ammatillisten tutkintojen osat"
  (->
    (db-ops/query [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id
                   id])
    first
    h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id [id]
  "Aiemmin hankittujen ammatillisten tutkintojen osat"
  (db-ops/query
    [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ahpto-id [oopto-id]
  "Aiemmin hankitun paikallisen tutkinnon osan tarkentavien tietojen näyttö"
  (db-ops/query [queries/select-osaamisen-osoittamiset-by-oopto-id oopto-id]
                {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id [id]
  "Aiemmin hankitut paikalliset tutkinnon osat"
  (->
    (db-ops/query
      [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id id])
    first
    h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id [id]
  "Aiemmin hankitut paikalliset tutkinnon osat"
  (db-ops/query
    [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ahyto-osa-alue-id [id]
  "Aiemmin hankitun yhteisen tutkinnon osan tarkentavien tietojen näyttö"
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ahyto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-osa-alueet-by-ahyto-id [id]
  "Aiemmin hankitun yhteisen tutkinnon osan osa-alueet"
  (db-ops/query
    [queries/select-osa-alueet-by-ooyto-id id]
    {:row-fn h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ahyto-id
  "Aiemmin hankitun yhteisen tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ooyto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id [id]
  "Aiemmin hankitun yhteisen tutkinnon osa"
  (->
    (db-ops/query [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-id
                   id])
    first
    h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id [id]
  "Aiemmin hankitun yhteisen tutkinnon osat"
  (db-ops/query
    [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql}))

(defn insert-ahpto-osaamisen-osoittaminen! [oopto-id naytto-id]
  "Lisää aiemmin hankitun paikallisen tutkinnon osan osaamisen osoittaminen"
  (db-ops/insert-one!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_paikallinen_tutkinnon_osa_id oopto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-todennettu-arviointi-arvioijat! [tta-id arvioija-id]
  "Lisää arvioijat todennetulle arvioinnille"
  (db-ops/insert-one!
    :todennettu_arviointi_arvioijat
    {:todennettu_arviointi_lisatiedot_id tta-id
     :koulutuksen_jarjestaja_osaamisen_arvioija_id arvioija-id}))

(defn insert-koulutuksen-jarjestaja-osaamisen-arvioijat! [c]
  "Lisää osaamisen arvioijat koulutuksen järjestäjälle"
  (db-ops/insert-multi!
    :koulutuksen_jarjestaja_osaamisen_arvioijat
    (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c)))

(defn insert-ahyto-osaamisen-osoittaminen! [ahyto-id n]
  "Lisää aiemmin hankitun yhteisen tutkinnon osan osaamisen osoittaminen"
  (db-ops/insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id ahyto-id
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue! [m]
  "Lisää aiemmin hankitun yhteisen tutkinnon osan osa-alue"
  (db-ops/insert-one!
    :aiemmin_hankitut_yto_osa_alueet
    (h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql m)))

(defn insert-ahyto-osa-alue-osaamisen-osoittaminen! [osa-alue-id naytto-id]
  "Lisää aiemmin hankitun yhteisen tutkinnon osan osa-alueen osaamisen osoittaminen"
  (db-ops/insert-one!
    :aiemmin_hankitun_yto_osa_alueen_naytto
    {:aiemmin_hankittu_yto_osa_alue_id osa-alue-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-todennettu-arviointi-lisatiedot! [m]
  "Lisää todennetun arvioinnin lisätiedot"
  (db-ops/insert-one!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql m)))

(defn insert-aiemmin-hankittu-yhteinen-tutkinnon-osa! [m]
  "Lisää aiemmin hankitun yhteisen tutkinnon osa"
  (db-ops/insert-one!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql m)))

(defn insert-aiemmin-hankittu-paikallinen-tutkinnon-osa! [m]
  "Lisää aiemmin hankitun paikallisen tutkinnon osa"
  (db-ops/insert-one!
    :aiemmin_hankitut_paikalliset_tutkinnon_osat
    (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql m)))

(defn insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto! [ooato-id n]
  "Lisää aiemmin hankitun ammatillisen tutkinnon osan näyttö"
  (db-ops/insert-one!
    :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    {:aiemmin_hankittu_ammat_tutkinnon_osa_id ooato-id
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-aiemmin-hankittu-ammat-tutkinnon-osa! [m]
  "Lisää aiemmin hankitun ammatillisen tutkinnon osa"
  (db-ops/insert-one!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql m)))

(defn delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id! [id]
  "Poista aiemmin hankitun ammatillisen tutkinnon osan näyttö"
  (db-ops/shallow-delete!
    :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    ["aiemmin_hankittu_ammat_tutkinnon_osa_id = ?" id]))

(defn delete-todennettu-arviointi-arvioijat-by-tta-id! [id]
  "Poista todennetun arvioinnin arvioija"
  (db-ops/shallow-delete!
    :todennettu_arviointi_arvioijat
    ["todennettu_arviointi_lisatiedot_id = ?" id]))

(defn update-todennettu-arviointi-lisatiedot-by-id! [id new-values]
  "Päivitä todennetun arvioinnin lisätiedot"
  (db-ops/update!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id! [id]
  "Poista aiemmin hankitun paikallisen tutkinnon osan näyttö"
  (db-ops/shallow-delete!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    ["aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ?" id]))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id! [id new-values]
  "Päivitä aiemmin hankitun paikallisen tutkinnon osa"
  (when-let
    [new-ahpt (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql new-values)]
    (db-ops/update!
      :aiemmin_hankitut_paikalliset_tutkinnon_osat
      new-ahpt
      ["id = ? AND deleted_at IS NULL" id])))

(defn delete-aiemmin-hankitun-yhteisen-tutkinnon-osan-naytto-by-id! [id]
  "Poista aiemmin hankitun yhteisen tutkinnon osan näyttö"
  (db-ops/shallow-delete!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ?" id]))

(defn delete-aiemmin-hankitut-yto-osa-alueet-by-id! [id]
  "Poista aiemmin hankitun yhteisen tutkinnon osan osa-alue"
  (db-ops/shallow-delete!
    :aiemmin_hankitut_yto_osa_alueet
    ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ?" id]))

(defn update-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id! [id new-values]
  "Päivitä aiemmin hankitun yhteisen tutkinnon osa"
  (db-ops/update!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id! [id new-values]
  "Päivitä aiemmin hankitun ammatillisen tutkinnon osa"
  (db-ops/update!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))
