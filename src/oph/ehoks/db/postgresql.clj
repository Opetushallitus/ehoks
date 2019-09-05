(ns oph.ehoks.db.postgresql
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn insert-koulutuksen-jarjestaja-osaamisen-arvioija! [m]
  (db-ops/insert-one!
    :koulutuksen_jarjestaja_osaamisen_arvioijat
    (h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql m)))

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

(defn insert-aiemmin-hankitut-ammat-tutkinnon-osat! [c]
  (db-ops/insert-multi!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (map h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql c)))

(defn insert-hankittavat-paikalliset-tutkinnon-osat! [c]
  (db-ops/insert-multi!
    :hankittavat_paikalliset_tutkinnon_osat
    (map h/hankittava-paikallinen-tutkinnon-osa-to-sql c)))

(defn insert-osaamisen-osoittaminen! [m]
  (db-ops/insert-one!
    :osaamisen_osoittamiset
    (h/osaamisen-osoittaminen-to-sql m)))

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

(defn select-osaamisen-osoittaminen-by-oopto-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-oopto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-ooyto-arvioija! [yto-id a-id]
  (db-ops/insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id yto-id
     :koulutuksen_jarjestaja_osaamisen_arvioija_id a-id}))

(defn select-arvioija-by-ooyto-id [id]
  (db-ops/query
    [queries/select-arvioijat-by-ooyto-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn select-oppilaitos-oids []
  (db-ops/query
    [queries/select-oppilaitos-oids]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-oppilaitos-oids-by-koulutustoimija-oid [oid]
  (db-ops/query
    [queries/select-oppilaitos-oids-by-koulutustoimija-oid oid]
    {:row-fn h/oppilaitos-oid-from-sql}))
