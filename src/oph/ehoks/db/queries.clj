(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(defn read-sql-file
  "Read SQL file from resource (default 'resources/db')"
  [f] (slurp (io/resource f)))

(def select-by-template
  (read-sql-file "select_by.sql"))

(def select-join-template
  (read-sql-file "select_join.sql"))

(defn populate-sql
  "Populates given template with values of keys in given map"
  [m sql]
  (reduce
    (fn [c [k v]]
      (cstr/replace c (str k) v))
    sql
    m))

(defn generate-select-by
  "Generates select query of given values"
  [m]
  (populate-sql m select-by-template))

(defn generate-select-join
  "Generates select join query of given values"
  [m]
  (populate-sql m select-join-template))

(defn parse-sql
  "Parse SQL query from symbol name"
  [n]
  (let [[table column] (rest (clojure.string/split
                               (cstr/replace n #"-" "_")
                               #"(_by_)|(select_)"))]
    {:table table :column column}))

(defmacro defq
  "Automatically create SQL query symbol. If filename is not given query will
   be created of symbol name."
  [query-name & filename]
  `(def ~query-name (if (nil? (first (quote ~filename)))
                      (generate-select-by (parse-sql (str (quote ~query-name))))
                      (read-sql-file (cstr/join (quote ~filename))))))

(defq select-count-all-hoks "hoksit/select_count_all_hoksit.sql")
(defq select-hoksit "hoksit/select.sql")
(defq select-hoksit-by-oppija-oid)
(defq select-hoksit-by-id)
(defq select-hoksit-by-id-paged "hoksit/select_hoksit_by_id_paged.sql")
(defq select-hoksit-by-opiskeluoikeus-oid-deleted-at-included
      "hoksit/select_hoksit_by_opiskeluoikeus_oid_deleted_at_included.sql")
(defq select-hoksit-by-eid)
(defq select-hoksit-by-opiskeluoikeus-oid)
(defq select-hoksit-eid-by-eid "hoksit/select_eid.sql")
(defq select-hoksit-created-between "hoksit/select_hoksit_created_between.sql")
(defq select-non-tuva-hoksit-started-between
      "hoksit/select_non_tuva_hoksit_started_between.sql")
(defq select-non-tuva-hoksit-finished-between
      "hoksit/select_non_tuva_hoksit_finished_between.sql")
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
(defq select-oppija-with-opiskeluoikeus-oid-by-oid
      "oppijat/select_oppija_with_opiskeluoikeus_oid_by_oid.sql")
(defq select-opiskeluoikeudet-by-oid
      "oppijat/select_opiskeluoikeudet_by_oid.sql")
(defq select-count-by-koulutustoimija
      "opiskeluoikeudet/select_count_by_koulutustoimija.sql")
(defq select-keskeytymisajanjaksot-by-osaamisen-hankkimistapa-id
      "hoksit/select_keskeytymisajanjaksot_by_osaamisen_hankkimistapa_id.sql")
(defq select-todennettu-arviointi-lisatiedot-by-id)
(def select-arvioijat-by-todennettu-arviointi-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_osaamisen_arvioijat"
     :join "todennettu_arviointi_arvioijat"
     :secondary-column "koulutuksen_jarjestaja_osaamisen_arvioija_id"
     :primary-column "id"
     :column "todennettu_arviointi_lisatiedot_id"}))
(defq select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id)
(defq select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id)
(defq select-hankittavat-koulutuksen-osat-by-hoks-id)
(defq select-oppilaitos-oids
      "oppijat/select_oppilaitos_oids.sql")
(defq select-oppilaitos-oids-by-koulutustoimija-oid
      "oppijat/select_oppilaitos_oids_by_koulutustoimija_oid.sql")
(defq select-sessions-by-session-key
      "sessions/select_by_session_key.sql")
(defq select-user-settings-by-user-oid
      "settings/select_by_user_oid.sql")
(defq select-shared-link-by-uuid
      "shared_modules/select_by_share_id.sql")
(defq select-shared-module-links-by-module-uuid
      "shared_modules/select_by_module_id.sql")
(defq select-oppija-opiskeluoikeus-for-shared-link
      "shared_modules/select-oppija-opiskeluoikeus-for-shared-link.sql")
(defq select-hankittavat-paikalliset-tutkinnon-osat-by-module-id)
(defq select-hankittavat-ammat-tutkinnon-osat-by-module-id)
(defq select-hankittavat-yhteiset-tutkinnon-osat-by-module-id)
(defq select-kyselylinkki
      "hoksit/select_kyselylinkki.sql")
(defq select-kyselylinkit-by-oppija-oid
      "hoksit/select_kyselylinkit_by_oppija_oid.sql")
(defq select-kyselylinkit-by-fuzzy-linkki
      "hoksit/select_kyselylinkit_by_fuzzy_linkki.sql")
(defq select-hoksit-by-ensikert-hyvaks-and-saavutettu-tiedot
      "hoksit/select_hoksit_by_ensikert_hyvaks_and_saavutettu_tiedot.sql")
(defq select-paattyneet-tyoelamajaksot-hato
      "heratepalvelu/select_paattyneet_tyoelamajaksot_hato.sql")
(defq select-paattyneet-tyoelamajaksot-hpto
      "heratepalvelu/select_paattyneet_tyoelamajaksot_hpto.sql")
(defq select-paattyneet-tyoelamajaksot-hyto
      "heratepalvelu/select_paattyneet_tyoelamajaksot_hyto.sql")
(defq select-paattyneet-tyoelamajaksot-3kk
      "hoksit/select_paattyneet_tyoelamajaksot_3kk.sql")
;; TODO: tätä voisi optimoida hajottamalla eri kyselyihin päättyneet
;; HOKSit ja "unohtuneet" HOKSit jotka etsitään
;; osaamisenhankkimisjaksojen vanhenemisen perusteella
(defq select-vanhat-hoksit-having-yhteystiedot
      "hoksit/select_vanhat_hoksit_having_yhteystiedot.sql")
(defq select-amisherate-kasittelytilat-by-hoks-id
      "heratepalvelu/select_amisherate_kasittelytilat_by_hoks_id.sql")
(defq select-hoksit-with-kasittelemattomat-aloitusheratteet
      "heratepalvelu/select_hoksit_with_kasittelemattomat_aloitusheratteet.sql")
(defq select-hoksit-with-kasittelemattomat-paattoheratteet
      "heratepalvelu/select_hoksit_with_kasittelemattomat_paattoheratteet.sql")
(defq select-hato-tyoelamajaksot-active-between
      "heratepalvelu/select_hato_tyoelamajaksot_active_between.sql")
(defq select-hpto-tyoelamajaksot-active-between
      "heratepalvelu/select_hpto_tyoelamajaksot_active_between.sql")
(defq select-hyto-tyoelamajaksot-active-between
      "heratepalvelu/select_hyto_tyoelamajaksot_active_between.sql")
(defq select-hankintakoulutus-oids-by-master-oid
      "opiskeluoikeudet/select-hankintakoulutus-oids-by-master-oid.sql")
(defq select-paattyneet-kyselylinkit-by-date-and-type-temp
      "heratepalvelu/select_paattyneet_kyselylinkit_by_date_and_type_temp.sql")
(defq select-all-hatos-for-hoks "hoksit/select_all_hatos_for_hoks.sql")
(defq select-all-hptos-for-hoks "hoksit/select_all_hptos_for_hoks.sql")
(defq select-all-osa-alueet-for-yto "hoksit/select_all_osa_alueet_for_yto.sql")
(defq select-osaamisen-hankkimistapa-by-id
      "hoksit/select_osaamisen_hankkimistapa_by_id.sql")
(defq select-osaamisen-hankkimistavat-by-module-id
      "hoksit/select_osaamisen_hankkimistavat_by_module_id.sql")
(defq select-osaamisen-osoittamiset-by-module-id
      "hoksit/select_osaamisen_osoittamiset_by_module_id.sql")
(defq select-all-ahatos-for-hoks "hoksit/select_all_ahatos_for_hoks.sql")
(defq select-all-ahptos-for-hoks "hoksit/select_all_ahptos_for_hoks.sql")
(defq select-all-osa-alueet-for-ahyto
      "hoksit/select_all_osa_alueet_for_ahyto.sql")
(defq select-all-ahytos-for-hoks "hoksit/select_all_ahytos_for_hoks.sql")
(defq select-hoksit-by-oo-oppilaitos-and-koski404
      "hoksit/select_hoksit_by_oo_oppilaitos_and_koski404.sql")
(defq select-oht-by-tutkinto-and-oppilaitos-between
      "hoksit/select_oht_by_tutkinto_and_oppilaitos_between.sql")
(defq select-oht-by-tutkinto-between
      "hoksit/select_oht_by_tutkinto_between.sql")
