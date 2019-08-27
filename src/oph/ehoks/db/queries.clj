(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(defn read-sql-file [f] (slurp (io/resource f)))

(def select-by-template (read-sql-file "select_by.sql"))

(def select-id-by-template (read-sql-file "hoksit/select_id_by.sql"))

(def select-join-template (read-sql-file "select_join.sql"))

(def delete-by-tutkinnon-osa-by-hoks-id-by-template
  (read-sql-file "hoksit/delete_oo_by_tutkinnonosa_by_hoks_id.sql"))

(def delete-by-yto-osa-alue-by-hoks-id-by-template
  (read-sql-file "hoksit/delete_oo_by_yto_osaalue_by_hoks_id.sql"))

(def delete-nayttoymparisto-by-oo-osa-by-hoks-id-by-template
  (read-sql-file
    "hoksit/delete_nayttoymparisto_by_oo_by_tutkinnon_osa_by_hoks.sql"))

(def delete-nayttoymparisto-by-oo-osa-alue-by-hoks-id-by-template
  (read-sql-file
    "hoksit/delete_nayttoymparisto_by_oo_by_yto_osa_alue_by_hoks.sql"))

(def delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa-template
  (read-sql-file
    "hoksit/delete_tyoelama_osaamisen_arvioijat_tutkinnon_osat.sql"))

(def delete-tyoelama-osaamisen-arvioijat-yto-osa-alueet-template
  (read-sql-file
    "hoksit/delete_tyoelama_osaamisen_arvioijat_yto_osa_alueet.sql"))

(def delete-kj-osaamisen-arvioijat-tutkinnon-osa-template
  (read-sql-file "hoksit/delete_kj_osaamisen_arvioijat_tutkinnon_osat.sql"))

(def delete-kj-osaamisen-arvioijat-yto-osa-alueet-template
  (read-sql-file "hoksit/delete_kj_osaamisen_arvioijat_yto_osa_alueet.sql"))

(def delete-osaamisen-hankkimistavat-by-tutkinnon-osa-template
  (read-sql-file
    "hoksit/delete_osaamisen_hankkimistavat_by_tutkinnon_osa_by_hoks.sql"))

(def delete-osaamisen-hankkimistavat-by-yto-osa-alue-template
  (read-sql-file "hoksit/delete_osaamisen_hankkimistavat_by_yto_osa_alue.sql"))

(def delete-kj-arvioijat-by-todennettu-arviointi-template
  (read-sql-file
    "hoksit/delete_kj_osaamisen_arvioijat_by_todennettu_arviointi.sql"))

(def delete-todennettu-arviointi-lisatiedot-template
  (read-sql-file "hoksit/delete_todennettu_arviointi_lisatiedot.sql"))

(defn populate-sql [m sql]
  (reduce
    (fn [c [k v]]
      (cstr/replace c (str k) v))
    sql
    m))

(defn generate-select-by [m]
  (populate-sql m select-by-template))

(defn generate-select-id-by [m]
  (populate-sql m select-id-by-template))

(defn generate-delete-oo-tutkinnon-osa-by [m]
  (populate-sql m delete-by-tutkinnon-osa-by-hoks-id-by-template))

(defn generate-delete-oo-yto-osa-alue-by [m]
  (populate-sql m delete-by-yto-osa-alue-by-hoks-id-by-template))

(defn generate-delete-nayttoymparisto-by-oo-by-osa-by-hoks-id [m]
  (populate-sql m delete-nayttoymparisto-by-oo-osa-by-hoks-id-by-template))

(defn generate-delete-nayttoymparisto-by-oo-by-osa-alue-by-hoks-id [m]
  (populate-sql m delete-nayttoymparisto-by-oo-osa-alue-by-hoks-id-by-template))

(defn generate-delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa [m]
  (populate-sql m
                delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa-template))

(defn generate-delete-tyoelama-osaamisen-arvioijat-osa-alueet [m]
  (populate-sql m
                delete-tyoelama-osaamisen-arvioijat-yto-osa-alueet-template))

(defn generate-delete-kj-osaamisen-arvioijat-tutkinnon-osa [m]
  (populate-sql m
                delete-kj-osaamisen-arvioijat-tutkinnon-osa-template))

(defn generate-delete-kj-osaamisen-arvioijat-osa-alueet [m]
  (populate-sql m
                delete-kj-osaamisen-arvioijat-yto-osa-alueet-template))

(defn generate-delete-kj-osaamisen-arvioijat-todennettu-arviointi [m]
  (populate-sql m
                delete-kj-arvioijat-by-todennettu-arviointi-template))

(defn generate-delete-osaamisen-hankkimistavat-tutkinnon-osa [m]
  (populate-sql m
                delete-osaamisen-hankkimistavat-by-tutkinnon-osa-template))

(defn generate-delete-osaamisen-hankkimistavat-tutkinnon-osan-osa-alue [m]
  (populate-sql m
                delete-osaamisen-hankkimistavat-by-yto-osa-alue-template))

(defn generate-delete-todennettu-arviointi-lisatiedot [m]
  (populate-sql m delete-todennettu-arviointi-lisatiedot-template))

(defn generate-select-join [m]
  (populate-sql m select-join-template))

(defn parse-sql [n]
  (let [[table column] (rest (clojure.string/split
                               (cstr/replace n #"-" "_")
                               #"(_by_)|(select_)"))]
    {:table table :column column}))

(defmacro defq [query-name & filename]
  `(def ~query-name (if (nil? (first (quote ~filename)))
                      (generate-select-by (parse-sql (str (quote ~query-name))))
                      (read-sql-file (cstr/join (quote ~filename))))))

(defq select-hoksit "hoksit/select.sql")
(defq delete-hoksit-by-id "hoksit/delete_by_id.sql")
(defq select-hoksit-by-oppija-oid)
(defq select-hoksit-by-id)
(defq select-hoksit-by-opiskeluoikeus-oid)
(defq select-hoksit-by-eid)
(defq select-hoksit-eid-by-eid "hoksit/select_eid.sql")
(defq select-hoks-oppijat-without-index
      "hoksit/select_oppija_oids_without_info.sql")
(defq select-hoks-oppijat-without-index-count
      "hoksit/select_oppija_oids_without_info_count.sql")
(defq select-hoks-opiskeluoikeudet-without-index
      "hoksit/select_opiskeluoikeus_oids_without_info.sql")
(defq select-hoks-opiskeluoikeudet-without-index-count
      "hoksit/select_opiskeluoikeus_oids_without_info_count.sql")
(defq select-hoks-opiskeluoikeudet-without-tutkinto
      "opiskeluoikeudet/select_without_tutkinto.sql")
(defq select-hoks-opiskeluoikeudet-without-tutkinto-count
      "opiskeluoikeudet/select_without_tutkinto_count.sql")
(defq select-oppilaitos-oppijat
      "oppijat/select_oppilaitos_oppijat.sql")
(defq select-oppilaitos-oppijat-search-count
      "oppijat/select_oppilaitos_oppijat_search_count.sql")
(defq select-opiskeluoikeudet-by-oppija-oid
      "oppijat/select_opiskeluoikeudet_by_oppija_oid.sql")
(defq select-oppijat-by-oid "oppijat/select_oppijat_by_oid.sql")
(defq select-opiskeluoikeudet-by-oid
      "oppijat/select_opiskeluoikeudet_by_oid" .sql)
(defq select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id)
(defq select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id)
(def select-osaamisen-osoittamiset-by-ooato-id
  (generate-select-join
    {:table "osaamisen_osoittamiset"
     :join "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
     :secondary-column "osaamisen_osoittaminen_id"
     :primary-column "id"
     :column "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))
(def select-osa-alueet-by-osaamisen-osoittaminen
  (generate-select-join
    {:table "koodisto_koodit"
     :join "osaamisen_osoittamisen_osa_alueet"
     :secondary-column "koodisto_koodi_id"
     :primary-column "id"
     :column "osaamisen_osoittaminen_id"}))
(defq select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id)
(defq select-hankittavat-paikalliset-tutkinnon-osat-by-id)
(defq select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id)
(defq select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id)
(def select-osaamisen-osoittamiset-by-oopto-id
  (generate-select-join
    {:table "osaamisen_osoittamiset"
     :join "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :secondary-column "osaamisen_osoittaminen_id"
     :primary-column "id"
     :column "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(defq select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-id)
(defq select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id)
(def select-osaamisen-osoittamiset-by-ppto-id
  (generate-select-join
    {:table "osaamisen_osoittamiset"
     :join "hankittavan_paikallisen_tutkinnon_osan_naytto"
     :secondary-column "osaamisen_osoittaminen_id"
     :primary-column "id"
     :column "hankittava_paikallinen_tutkinnon_osa_id"}))
(def select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_osaamisen_arvioijat"
     :join "osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija"
     :secondary-column "koulutuksen_jarjestaja_osaamisen_arvioija_id"
     :primary-column "id"
     :column "osaamisen_osoittaminen_id"}))
(def select-tyoelama-osaamisen-arvioijat-by-hon-id
  (generate-select-join
    {:table "tyoelama_osaamisen_arvioijat"
     :join "osaamisen_osoittamisen_tyoelama_arvioija"
     :secondary-column "tyoelama_arvioija_id"
     :primary-column "id"
     :column "osaamisen_osoittaminen_id"}))
(defq select-nayttoymparistot-by-id)
(def select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
  (generate-select-by {:table "osaamisen_osoittamisen_sisallot"
                       :column "osaamisen_osoittaminen_id"}))
(def select-osaamisen-osoittamisen-kriteeri-by-osaamisen-osoittaminen-id
  (generate-select-by {:table "osaamisen_osoittamisen_yksilolliset_kriteerit"
                       :column "osaamisen_osoittaminen_id"}))
(def select-osaamisen-hankkmistavat-by-ppto-id
  (generate-select-join
    {:table "osaamisen_hankkimistavat"
     :join "hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat"
     :secondary-column "osaamisen_hankkimistapa_id"
     :primary-column "id"
     :column "hankittava_paikallinen_tutkinnon_osa_id"}))
(defq select-tyopaikalla-jarjestettavat-koulutukset-by-id)
(def select-tyotehtavat-by-tho-id
  (generate-select-by
    {:table "tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat"
     :column "tyopaikalla_jarjestettava_koulutus_id"}))
(defq select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id)
(defq select-todennettu-arviointi-lisatiedot-by-id)
(def select-arvioijat-by-todennettu-arviointi-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_osaamisen_arvioijat"
     :join "todennettu_arviointi_arvioijat"
     :secondary-column "koulutuksen_jarjestaja_osaamisen_arvioija_id"
     :primary-column "id"
     :column "todennettu_arviointi_lisatiedot_id"}))
(def select-osaamisen-osoittamiset-by-ooyto-id
  (generate-select-join
    {:table "osaamisen_osoittamiset"
     :join "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
     :secondary-column "osaamisen_osoittaminen_id"
     :primary-column "id"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def select-osaamisen-osoittamiset-by-ahyto-osa-alue-id
  (generate-select-join
    {:table "osaamisen_osoittamiset"
     :join "aiemmin_hankitun_yto_osa_alueen_naytto"
     :secondary-column "osaamisen_osoittaminen_id"
     :primary-column "id"
     :column "aiemmin_hankittu_yto_osa_alue_id"}))
(def select-arvioijat-by-ooyto-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_osaamisen_arvioijat"
     :join "aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat"
     :secondary-column "koulutuksen_jarjestaja_osaamisen_arvioija_id"
     :primary-column "id"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def select-osa-alueet-by-ooyto-id
  (generate-select-by
    {:table "aiemmin_hankitut_yto_osa_alueet"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(defq select-hankittavat-ammat-tutkinnon-osat-by-hoks-id)
(defq select-hankittavat-ammat-tutkinnon-osat-by-id)
(def select-osaamisen-osoittamiset-by-pato-id
  (generate-select-join
    {:table "osaamisen_osoittamiset"
     :join "hankittavan_ammat_tutkinnon_osan_naytto"
     :secondary-column "osaamisen_osoittaminen_id"
     :primary-column "id"
     :column "hankittava_ammat_tutkinnon_osa_id"}))
(def select-osaamisen-hankkmistavat-by-pato-id
  (generate-select-join
    {:table "osaamisen_hankkimistavat"
     :join "hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat"
     :secondary-column "osaamisen_hankkimistapa_id"
     :primary-column "id"
     :column "hankittava_ammat_tutkinnon_osa_id"}))
(defq select-opiskeluvalmiuksia-tukevat-opinnot-by-id)
(defq select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id)
(defq select-hankittavat-yhteiset-tutkinnon-osat-by-id)
(defq select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id)
(def select-osaamisen-hankkimistavat-by-yto-osa-alue-id
  (generate-select-join
    {:table "osaamisen_hankkimistavat"
     :join "yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat"
     :secondary-column "osaamisen_hankkimistapa_id"
     :primary-column "id"
     :column "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def select-yto-osa-alueet-by-yto-id
  (generate-select-by
    {:table "yhteisen_tutkinnon_osan_osa_alueet"
     :column "yhteinen_tutkinnon_osa_id"}))
(def select-osaamisen-osoittamiset-by-yto-osa-alue-id
  (generate-select-join
    {:table "osaamisen_osoittamiset"
     :join "yhteisen_tutkinnon_osan_osa_alueen_naytot"
     :secondary-column "osaamisen_osoittaminen_id"
     :primary-column "id"
     :column "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def delete-ahato-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id
  (generate-delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "aiemmin_hankitut_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))
(def delete-ahato-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa
  (generate-delete-kj-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "aiemmin_hankitut_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))
(def delete-ahato-kj-arvioijat-by-todennettu-arviointi
  (generate-delete-kj-osaamisen-arvioijat-todennettu-arviointi
    {:tutkinnon-osa-table "aiemmin_hankitut_ammat_tutkinnon_osat"}))
(def delete-ahato-todennettu-arviointi-lisatiedot-by-hoks-id
  (generate-delete-todennettu-arviointi-lisatiedot
    {:tutkinnon-osa-table "aiemmin_hankitut_ammat_tutkinnon_osat"}))
(def delete-oo-by-ahato-by-hoks-id
  (generate-delete-oo-tutkinnon-osa-by
    {:tutkinnon-osa-table "aiemmin_hankitut_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))
(def delete-ahato-nayttoymparisto-tutkinnon-osa-by-hoks-id
  (generate-delete-nayttoymparisto-by-oo-by-osa-by-hoks-id
    {:tutkinnon-osa-table "aiemmin_hankitut_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))
(def delete-oo-by-ahyto-by-hoks-id
  (generate-delete-oo-tutkinnon-osa-by
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def delete-oo-by-ahyto-yto-osa-alue-by-hoks-id
  (generate-delete-oo-yto-osa-alue-by
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "aiemmin_hankitut_yto_osa_alueet"
     :yto-osa-alue-naytto-table "aiemmin_hankitun_yto_osa_alueen_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "aiemmin_hankittu_yto_osa_alue_id"}))
(def delete-ahyto-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id
  (generate-delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def delete-ahyto-tyoelama-arvioijat-by-yto-osa-alue-by-hoks-id
  (generate-delete-tyoelama-osaamisen-arvioijat-osa-alueet
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "aiemmin_hankitut_yto_osa_alueet"
     :yto-osa-alue-naytto-table "aiemmin_hankitun_yto_osa_alueen_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "aiemmin_hankittu_yto_osa_alue_id"}))
(def delete-ahyto-koulutuksen-jarjestaja-arvioijat-by-yto-osa-alue
  (generate-delete-kj-osaamisen-arvioijat-osa-alueet
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "aiemmin_hankitut_yto_osa_alueet"
     :yto-osa-alue-naytto-table "aiemmin_hankitun_yto_osa_alueen_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "aiemmin_hankittu_yto_osa_alue_id"}))
(def delete-ahyto-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa
  (generate-delete-kj-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def delete-ahyto-kj-arvioijat-by-todennettu-arviointi
  (generate-delete-kj-osaamisen-arvioijat-todennettu-arviointi
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"}))
(def delete-ahyto-todennettu-arviointi-lisatiedot-by-hoks-id
  (generate-delete-todennettu-arviointi-lisatiedot
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"}))
(def delete-ahyto-nayttoymparisto-osa-alueet-by-hoks-id
  (generate-delete-nayttoymparisto-by-oo-by-osa-alue-by-hoks-id
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "aiemmin_hankitut_yto_osa_alueet"
     :yto-osa-alue-naytto-table "aiemmin_hankitun_yto_osa_alueen_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "aiemmin_hankittu_yto_osa_alue_id"}))
(def delete-ahyto-nayttoymparisto-tutkinnon-osa-by-hoks-id
  (generate-delete-nayttoymparisto-by-oo-by-osa-by-hoks-id
    {:tutkinnon-osa-table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def delete-hato-osaamisen-hankkimistavat-tutkinnon-osa-by-hoks-id
  (generate-delete-osaamisen-hankkimistavat-tutkinnon-osa
    {:tutkinnon-osa-table "hankittavat_ammat_tutkinnon_osat"
     :tutkinnon-osa-hankkimistapa-table
     "hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat"
     :tutkinnon-osa-id "hankittava_ammat_tutkinnon_osa_id"}))
(def delete-hato-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id
  (generate-delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "hankittavat_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_ammat_tutkinnon_osa_id"}))
(def delete-hato-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa
  (generate-delete-kj-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "hankittavat_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_ammat_tutkinnon_osa_id"}))
(def delete-oo-by-hato-by-hoks-id
  (generate-delete-oo-tutkinnon-osa-by
    {:tutkinnon-osa-table "hankittavat_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_ammat_tutkinnon_osa_id"}))
(def delete-hato-nayttoymparisto-tutkinnon-osa-by-hoks-id
  (generate-delete-nayttoymparisto-by-oo-by-osa-by-hoks-id
    {:tutkinnon-osa-table "hankittavat_ammat_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_ammat_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_ammat_tutkinnon_osa_id"}))
(def delete-hpto-osaamisen-hankkimistavat-tutkinnon-osa-by-hoks-id
  (generate-delete-osaamisen-hankkimistavat-tutkinnon-osa
    {:tutkinnon-osa-table "hankittavat_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-hankkimistapa-table
     "hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat"
     :tutkinnon-osa-id "hankittava_paikallinen_tutkinnon_osa_id"}))
(def delete-hpto-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa
  (generate-delete-kj-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "hankittavat_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_paikallinen_tutkinnon_osa_id"}))
(def delete-hpto-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id
  (generate-delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "hankittavat_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_paikallinen_tutkinnon_osa_id"}))
(def delete-oo-by-hpto-by-hoks-id
  (generate-delete-oo-tutkinnon-osa-by
    {:tutkinnon-osa-table "hankittavat_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_paikallinen_tutkinnon_osa_id"}))
(def delete-hpto-nayttoymparisto-tutkinnon-osa-by-hoks-id
  (generate-delete-nayttoymparisto-by-oo-by-osa-by-hoks-id
    {:tutkinnon-osa-table "hankittavat_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table "hankittavan_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "hankittava_paikallinen_tutkinnon_osa_id"}))
(def delete-ahpto-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id
  (generate-delete-tyoelama-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "aiemmin_hankitut_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(def delete-ahpto-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa
  (generate-delete-kj-osaamisen-arvioijat-tutkinnon-osa
    {:tutkinnon-osa-table "aiemmin_hankitut_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(def delete-ahpto-kj-arvioijat-by-todennettu-arviointi
  (generate-delete-kj-osaamisen-arvioijat-todennettu-arviointi
    {:tutkinnon-osa-table "aiemmin_hankitut_paikalliset_tutkinnon_osat"}))
(def delete-ahpto-todennettu-arviointi-lisatiedot-by-hoks-id
  (generate-delete-todennettu-arviointi-lisatiedot
    {:tutkinnon-osa-table "aiemmin_hankitut_paikalliset_tutkinnon_osat"}))
(def delete-oo-by-ahpto-by-hoks-id
  (generate-delete-oo-tutkinnon-osa-by
    {:tutkinnon-osa-table "aiemmin_hankitut_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(def delete-ahpto-nayttoymparisto-tutkinnon-osa-by-hoks-id
  (generate-delete-nayttoymparisto-by-oo-by-osa-by-hoks-id
    {:tutkinnon-osa-table "aiemmin_hankitut_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(def delete-ahpto-osaamisen-hankkimistavat-tutkinnon-osa-by-hoks-id
  (generate-delete-osaamisen-hankkimistavat-tutkinnon-osa
    {:tutkinnon-osa-table "aiemmin_hankitut_paikalliset_tutkinnon_osat"
     :tutkinnon-osa-naytto-table
     "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :tutkinnon-osa-id "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(def delete-hyto-tyoelama-arvioijat-by-yto-osa-alue-by-hoks-id
  (generate-delete-tyoelama-osaamisen-arvioijat-osa-alueet
    {:tutkinnon-osa-table "hankittavat_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "yhteisen_tutkinnon_osan_osa_alueet"
     :yto-osa-alue-naytto-table "yhteisen_tutkinnon_osan_osa_alueen_naytot"
     :tutkinnon-osa-id "yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def delete-hyto-koulutuksen-jarjestaja-arvioijat-by-yto-osa-alue
  (generate-delete-kj-osaamisen-arvioijat-osa-alueet
    {:tutkinnon-osa-table "hankittavat_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "yhteisen_tutkinnon_osan_osa_alueet"
     :yto-osa-alue-naytto-table "yhteisen_tutkinnon_osan_osa_alueen_naytot"
     :tutkinnon-osa-id "yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def delete-hyto-nayttoymparisto-osa-alueet-by-hoks-id
  (generate-delete-nayttoymparisto-by-oo-by-osa-alue-by-hoks-id
    {:tutkinnon-osa-table "hankittavat_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "yhteisen_tutkinnon_osan_osa_alueet"
     :yto-osa-alue-naytto-table "yhteisen_tutkinnon_osan_osa_alueen_naytot"
     :tutkinnon-osa-id "yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def delete-oo-by-hyto-yto-osa-alue-by-hoks-id
  (generate-delete-oo-yto-osa-alue-by
    {:tutkinnon-osa-table "hankittavat_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "yhteisen_tutkinnon_osan_osa_alueet"
     :yto-osa-alue-naytto-table "yhteisen_tutkinnon_osan_osa_alueen_naytot"
     :tutkinnon-osa-id "yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def delete-hyto-osaamisen-hankkimistavat-yto-osa-alue-by-hoks-id
  (generate-delete-osaamisen-hankkimistavat-tutkinnon-osan-osa-alue
    {:tutkinnon-osa-table "hankittavat_yhteiset_tutkinnon_osat"
     :yto-osa-alueet-table "yhteisen_tutkinnon_osan_osa_alueet"
     :yto-osa-alue-hankkimistapa-table
     "yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat"
     :tutkinnon-osa-id "yhteinen_tutkinnon_osa_id"
     :yto-osa-alue-id "yhteisen_tutkinnon_osan_osa_alue_id"}))
(defq select-oppilaitos-oids
      "oppijat/select_oppilaitos_oids.sql")
(defq select-oppilaitos-oids-by-koulutustoimija-oid
      "oppijat/select_oppilaitos_oids_by_koulutustoimija_oid.sql")
(defq select-sessions-by-session-key
      "sessions/select_by_session_key.sql")
