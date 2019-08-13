(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(defn read-sql-file [f] (slurp (io/resource f)))

(def select-by-template (read-sql-file "select_by.sql"))

(def select-id-by-template (read-sql-file "hoksit/select_id_by.sql"))

(def select-join-template (read-sql-file "select_join.sql"))

(def delete-osaamisen-osoittamiset-template
  (read-sql-file
    "hoksit/delete_osaamisen_osoittaminen_and_nayttoymparisto_by_oo_id.sql"))

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
(defq delete-osaamisen-osoittaminen-and-nayttoymparisto-by-oo-id
      "hoksit/delete_osaamisen_osoittaminen_and_nayttoymparisto_by_oo_id.sql")
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
(def select-ahyto-ids-by-hoks-id
  (generate-select-id-by
    {:id "id"
     :table "aiemmin_hankitut_yhteiset_tutkinnon_osat"
     :column "hoks_id"}))
(def select-oo-ids-by-ahyto-id
  (generate-select-id-by
    {:id "osaamisen_osoittaminen_id"
     :table "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def select-ahyto-osa-alue-ids-by-ahyto-id
  (generate-select-id-by
    {:id "id"
     :table "aiemmin_hankitut_yto_osa_alueet"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def select-oo-ids-by-ahyto-osa-alue-id
  (generate-select-id-by
    {:id "osaamisen_osoittaminen_id"
     :table "aiemmin_hankitun_yto_osa_alueen_naytto"
     :column "aiemmin_hankittu_yto_osa_alue_id"}))
(def select-ahato-ids-by-hoks-id
  (generate-select-id-by
    {:id "id"
     :table "aiemmin_hankitut_ammat_tutkinnon_osat"
     :column "hoks_id"}))
(def select-oo-ids-by-ahato-id
  (generate-select-id-by
    {:id "osaamisen_osoittaminen_id"
     :table "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
     :column "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))
(def select-ahpto-ids-by-hoks-id
  (generate-select-id-by
    {:id "id"
     :table "aiemmin_hankitut_paikalliset_tutkinnon_osat"
     :column "hoks_id"}))
(def select-oo-ids-by-ahpto-id
  (generate-select-id-by
    {:id "osaamisen_osoittaminen_id"
     :table "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :column "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(def select-hato-ids-by-hoks-id
  (generate-select-id-by
    {:id "id"
     :table "hankittavat_ammat_tutkinnon_osat"
     :column "hoks_id"}))
(def select-oo-ids-by-hato-id
  (generate-select-id-by
    {:id "osaamisen_osoittaminen_id"
     :table "hankittavan_ammat_tutkinnon_osan_naytto"
     :column "hankittava_ammat_tutkinnon_osa_id"}))
(def select-hpto-ids-by-hoks-id
  (generate-select-id-by
    {:id "id"
     :table "hankittavat_paikalliset_tutkinnon_osat"
     :column "hoks_id"}))
(def select-oo-ids-by-hpto-id
  (generate-select-id-by
    {:id "osaamisen_osoittaminen_id"
     :table "hankittavan_paikallisen_tutkinnon_osan_naytto"
     :column "hankittava_paikallinen_tutkinnon_osa_id"}))
(def select-hyto-ids-by-hoks-id
  (generate-select-id-by
    {:id "id"
     :table "hankittavat_yhteiset_tutkinnon_osat"
     :column "hoks_id"}))
(def select-hyto-osa-alue-ids-by-hyto-id
  (generate-select-id-by
    {:id "id"
     :table "yhteisen_tutkinnon_osan_osa_alueet"
     :column "yhteinen_tutkinnon_osa_id"}))
(def select-oo-ids-by-hyto-osa-alue-id
  (generate-select-id-by
    {:id "osaamisen_osoittaminen_id"
     :table "yhteisen_tutkinnon_osan_osa_alueen_naytot"
     :column "yhteisen_tutkinnon_osan_osa_alue_id"}))
(defq select-oppilaitos-oids
      "oppijat/select_oppilaitos_oids.sql")
(defq select-oppilaitos-oids-by-koulutustoimija-oid
      "oppijat/select_oppilaitos_oids_by_koulutustoimija_oid.sql")
(defq select-sessions-by-session-key
      "sessions/select_by_session_key.sql")
