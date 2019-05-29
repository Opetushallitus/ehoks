(ns oph.ehoks.db.postgresql
  (:require [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.hoks :as h]
            [clj-time.coerce :as c]
            [oph.ehoks.db.queries :as queries]))

(extend-protocol jdbc/ISQLValue
  java.time.LocalDate
  (sql-value [value] (java.sql.Date/valueOf value))
  java.util.Date
  (sql-value [value] (c/to-sql-time value)))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [o _ _]
    (.toLocalDate o)))

(defn insert-empty! [t]
  (jdbc/execute!
    {:connection-uri (:database-url config)}
    (format
      "INSERT INTO %s DEFAULT VALUES" (name t))))

(defn query
  ([queries opts]
    (jdbc/query {:connection-uri (:database-url config)} queries opts))
  ([queries arg & opts]
    (query queries (apply hash-map arg opts))))

(defn insert! [t v]
  (if (seq v)
    (jdbc/insert! {:connection-uri (:database-url config)} t v)
    (insert-empty! t)))

(defn insert-one! [t v] (first (insert! t v)))

(defn update! [t v w]
  (jdbc/update! {:connection-uri (:database-url config)}
                t v w))

(defn shallow-delete! [t w]
  (update! t {:deleted_at (java.util.Date.)} w))

(defn insert-multi! [t v]
  (jdbc/insert-multi! {:connection-uri (:database-url config)} t v))

(defn select-hoksit []
  (query
    [queries/select-hoksit]
    :row-fn h/from-sql))

(defn select-hoks-by-oppija-oid [oid]
  (query
    [queries/select-hoksit-by-oppija-oid oid]
    :row-fn h/from-sql))

(defn select-hoks-by-id [id]
  (first
    (query
      [queries/select-hoksit-by-id id]
      {:row-fn h/from-sql})))

(defn select-hoks-by-eid [eid]
  (first
    (query
      [queries/select-hoksit-by-eid eid]
      {:row-fn h/from-sql})))

(defn select-hoksit-eid-by-eid [eid]
  (query
    [queries/select-hoksit-eid-by-eid eid]
    {}))

(defn select-hoksit-by-opiskeluoikeus-oid [oid]
  (query
    [queries/select-hoksit-by-opiskeluoikeus-oid oid]
    {:row-fn h/from-sql}))

(defn generate-unique-eid []
  (loop [eid nil]
    (if (or (nil? eid) (seq (select-hoksit-eid-by-eid eid)))
      (recur (str (java.util.UUID/randomUUID)))
      eid)))

(defn insert-hoks! [hoks]
  (let [eid (generate-unique-eid)]
    (insert-one! :hoksit (h/hoks-to-sql (assoc hoks :eid eid)))))

(defn update-hoks-by-id! [id hoks]
  (update! :hoksit (h/hoks-to-sql hoks) ["id = ? AND deleted_at IS NULL" id]))

(defn select-todennettu-arviointi-lisatiedot-by-id [id]
  (first
    (query
      [queries/select-todennettu-arviointi-lisatiedot-by-id id]
      {:row-fn h/todennettu-arviointi-lisatiedot-from-sql})))

(defn insert-todennettu-arviointi-lisatiedot! [m]
  (insert-one!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql m)))

(defn select-arvioijat-by-todennettu-arviointi-id [id]
  (query
    [queries/select-arvioijat-by-todennettu-arviointi-id id]
    {:row-fn h/koulutuksen-jarjestaja-arvioija-from-sql}))

(defn insert-todennettu-arviointi-arvioija! [tta m]
  (insert-one!
    :todennettu_arviointi_arvioijat
    {:todennettu_arviointi_lisatiedot_id (:id tta)
     :koulutuksen_jarjestaja_arvioija_id (:id m)}))

(defn insert-koulutuksen-jarjestaja-arvioijat! [c]
  (insert-multi!
    :koulutuksen_jarjestaja_arvioijat
    (map h/koulutuksen-jarjestaja-arvioija-to-sql c)))

(defn insert-koulutuksen-jarjestaja-arvioija! [m]
  (insert-one!
    :koulutuksen_jarjestaja_arvioijat
    (h/koulutuksen-jarjestaja-arvioija-to-sql m)))

(defn select-tarkentavat-tiedot-naytto-by-ooato-id
  "Aiemmin hankitun ammat tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ooato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-ooato-osaamisen-osoittaminen! [ooato n]
  (insert-one!
    :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    {:aiemmin_hankittu_ammat_tutkinnon_osa_id (:id ooato)
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-ooyto-osaamisen-osoittaminen! [ooyto n]
  (insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id (:id ooyto)
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-koodisto-koodi! [m]
  (insert-one!
    :koodisto_koodit
    (h/to-sql m)))

(defn insert-osaamisen-osoittamisen-osa-alue! [naytto-id koodi-id]
  (insert-one!
    :osaamisen_osoittamisen_osa_alueet
    {:osaamisen_osoittaminen_id naytto-id
     :koodisto_koodi_id koodi-id}))

(defn select-osa-alueet-by-osaamisen-osoittaminen [naytto-id]
  (query
    [queries/select-osa-alueet-by-osaamisen-osoittaminen naytto-id]
    {:row-fn h/koodi-uri-from-sql}))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql}))

(defn insert-aiemmin-hankittu-ammat-tutkinnon-osa! [m]
  (insert-one!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql m)))

(defn insert-aiemmin-hankitut-ammat-tutkinnon-osat! [c]
  (insert-multi!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (map h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql c)))

(defn select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql}))

(defn select-hankittava-paikallinen-tutkinnon-osa-by-id [id]
  (first
    (query
      [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-id id]
      {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql})))

(defn insert-hankittavat-paikalliset-tutkinnon-osat! [c]
  (insert-multi!
    :hankittavat_paikalliset_tutkinnon_osat
    (map h/hankittava-paikallinen-tutkinnon-osa-to-sql c)))

(defn insert-hankittava-paikallinen-tutkinnon-osa! [m]
  (insert-one!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)))

(defn update-hankittava-paikallinen-tutkinnon-osa-by-id! [id m]
  (update!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn select-osaamisen-osoittamiset-by-ppto-id
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ppto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn delete-osaamisen-hankkimistavat-by-ppto-id!
  "hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn delete-osaamisen-osoittamiset-by-ppto-id!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn insert-tho-henkilot!
  "Työpaikalla hankittavan osaamisen muut osallistujat"
  [o c]
  (insert-multi!
    :tyopaikalla_jarjestettavan_koulutuksen_henkilot
    (map
      #(assoc (h/henkilo-to-sql %) :tyopaikalla_jarjestettava_koulutus_id
              (:id o))
      c)))

(defn select-henkilot-by-tho-id
  "Työpaikalla hankittavan osaamisen muut osallistujat"
  [id]
  (query
    [queries/select-henkilot-by-tho-id id]
    {:row-fn h/henkilo-from-sql}))

(defn insert-tho-tyotehtavat!
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [tho c]
  (insert-multi!
    :tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
    (map
      #(hash-map
         :tyopaikalla_jarjestettava_koulutus_id (:id tho)
         :tyotehtava %)
      c)))

(defn select-tyotehtavat-by-tho-id
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [id]
  (query
    [queries/select-tyotehtavat-by-tho-id id]
    {:row-fn h/tyotehtava-from-sql}))

(defn insert-tyopaikalla-jarjestettava-koulutus! [o]
  (when (some? o)
    (let [o-db (insert-one!
                 :tyopaikalla_jarjestettavat_koulutukset
                 (h/tyopaikalla-jarjestettava-koulutus-to-sql o))]
      (insert-tho-henkilot! o-db (:muut-osallistujat o))
      (insert-tho-tyotehtavat! o-db (:keskeiset-tyotehtavat o))
      o-db)))

(defn select-tyopaikalla-jarjestettava-koulutus-by-id [id]
  (first
    (query
      [queries/select-tyopaikalla-jarjestettavat-koulutukset-by-id id]
      {:row-fn h/tyopaikalla-jarjestettava-koulutus-from-sql})))

(defn insert-osaamisen-hankkimistavan-muut-oppimisymparistot! [oh c]
  (insert-multi!
    :muut_oppimisymparistot
    (map
      #(h/to-sql
         (assoc % :osaamisen-hankkimistapa-id (:id oh)))
      c)))

(defn select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id [id]
  (query
    [queries/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id id]
    {:row-fn h/muu-oppimisymparisto-from-sql}))

(defn insert-osaamisen-hankkimistapa! [oh]
  (insert-one!
    :osaamisen_hankkimistavat
    (h/osaamisen-hankkimistapa-to-sql oh)))

(defn insert-hankittavan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
  [ppto oh]
  (insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_hankkimistapa_id (:id oh)}))

(defn select-osaamisen-hankkimistavat-by-ppto-id
  "hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (query
    [queries/select-osaamisen-hankkmistavat-by-ppto-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-osaamisen-osoittaminen! [m]
  (insert-one!
    :osaamisen_osoittamiset
    (h/osaamisen-osoittaminen-to-sql m)))

(defn insert-ppto-osaamisen-osoittaminen!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näyttö"
  [ppto h]
  (insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_osoittaminen_id (:id h)}))

(defn insert-ppto-osaamisen-osoittamiset!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [ppto c]
  (let [h-col (insert-multi!
                :osaamisen_osoittamiset
                (map h/osaamisen-osoittaminen-to-sql c))]
    (insert-multi!
      :hankittavan_paikallisen_tutkinnon_osan_naytto
      (map #(hash-map
              :hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
              :osaamisen_osoittaminen_id (:id %))
           h-col))
    h-col))

(defn insert-osaamisen-osoittamisen-koulutuksen-jarjestaja-arvioijat! [hon c]
  (let [kja-col (insert-multi!
                  :koulutuksen_jarjestaja_arvioijat
                  (map h/koulutuksen-jarjestaja-arvioija-to-sql c))]
    (insert-multi!
      :osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
      (map #(hash-map
              :osaamisen_osoittaminen_id (:id hon)
              :koulutuksen_jarjestaja_arvioija_id (:id %))
           kja-col))
    kja-col))

(defn select-koulutuksen-jarjestaja-arvioijat-by-hon-id
  "Hankitun osaamisen näytön koulutuksen järjestäjän arvioijat"
  [id]
  (query
    [queries/select-koulutuksen-jarjestaja-arvioijat-by-hon-id id]
    {:row-fn h/koulutuksen-jarjestaja-arvioija-from-sql}))

(defn insert-tyoelama-arvioija! [arvioija]
  (insert-one!
    :tyoelama_arvioijat
    (h/tyoelama-arvioija-to-sql arvioija)))

(defn insert-osaamisen-osoittamisen-tyoelama-arvioija! [hon arvioija]
  (insert-one!
    :osaamisen_osoittamisen_tyoelama_arvioija
    {:osaamisen_osoittaminen_id (:id hon)
     :tyoelama_arvioija_id (:id arvioija)}))

(defn select-tyoelama-arvioijat-by-hon-id
  "Hankitun osaamisen näytön työelemän arvioijat"
  [id]
  (query
    [queries/select-tyoelama-arvioijat-by-hon-id id]
    {:row-fn h/tyoelama-arvioija-from-sql}))

(defn insert-osaamisen-osoittamisen-tyotehtavat! [hon c]
  (insert-multi!
    :osaamisen_osoittamisen_tyotehtavat
    (map #(hash-map :osaamisen_osoittaminen_id (:id hon) :tyotehtava %) c)))

(defn select-tyotehtavat-by-osaamisen-osoittaminen-id [id]
  (query
    [queries/select-tyotehtavat-by-osaamisen-osoittaminen-id id]
    {:row-fn h/tyotehtava-from-sql}))

(defn insert-nayttoymparisto! [m]
  (insert-one!
    :nayttoymparistot
    (h/to-sql m)))

(defn insert-nayttoymparistot! [c]
  (insert-multi!
    :nayttoymparistot
    (map h/to-sql c)))

(defn select-nayttoymparisto-by-id [id]
  (first
    (query
      [queries/select-nayttoymparistot-by-id id]
      {:row-fn h/nayttoymparisto-from-sql})))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql}))

(defn insert-aiemmin-hankittu-paikallinen-tutkinnon-osa! [m]
  (insert-one!
    :aiemmin_hankitut_paikalliset_tutkinnon_osat
    (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql m)))

(defn select-osaamisen-osoittaminen-by-oopto-id [id]
  (query
    [queries/select-tarkentavat-tiedot-naytto-by-oopto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-oopto-arvioija! [oopto-id arvioija-id]
  (insert-one!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
    {:aiemmin_hankittu_paikallinen_tutkinnon_osa_id oopto-id
     :koulutuksen_jarjestaja_arvioija_id arvioija-id}))

(defn select-arvioijat-by-oopto-id [id]
  (query
    [queries/select-arvioijat-by-oopto-id id]
    {:row-fn h/koulutuksen-jarjestaja-arvioija-from-sql}))

(defn insert-oopto-osaamisen-osoittaminen! [oopto-id naytto-id]
  (insert-one!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_paikallinen_tutkinnon_osa_id oopto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-ooyto-arvioija! [yto-id a-id]
  (insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id yto-id
     :koulutuksen_jarjestaja_arvioija_id a-id}))

(defn select-arvioija-by-ooyto-id [id]
  (query
    [queries/select-arvioijat-by-ooyto-id id]
    {:row-fn h/koulutuksen-jarjestaja-arvioija-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ooyto-id
  "Aiemmin hankitun yhteisen tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ooyto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ooyto-osa-alue-id [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ooyto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-ooyto-osa-alue-osaamisen-osoittaminen! [osa-alue-id naytto-id]
  (insert-one!
    :aiemmin_hankitun_yto_osa_alueen_naytto
    {:aiemmin_hankittu_yto_osa_alue_id osa-alue-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osa-alueet-by-ooyto-id [id]
  (query
    [queries/select-osa-alueet-by-ooyto-id id]
    {:row-fn h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue! [m]
  (insert-one!
    :aiemmin_hankitut_yto_osa_alueet
    (h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql m)))

(defn insert-aiemmin-hankittu-yhteinen-tutkinnon-osa! [m]
  (insert-one!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql}))

(defn insert-hankittava-ammat-tutkinnon-osa! [m]
  (insert-one!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)))

(defn select-hankittavat-ammat-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-hankittavat-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-ammat-tutkinnon-osa-from-sql}))

(defn insert-pato-osaamisen-osoittaminen! [pato-id naytto-id]
  (insert-one!
    :hankittavan_ammat_tutkinnon_osan_naytto
    {:hankittava_ammat_tutkinnon_osa_id pato-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osaamisen-osoittamiset-by-pato-id [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-pato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
  [pato-id oh-id]
  (insert-one!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_ammat_tutkinnon_osa_id pato-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn select-osaamisen-hankkimistavat-by-pato-id
  "hankittavan ammat tutkinnon osan osaamisen hankkimistavat"
  [id]
  (query
    [queries/select-osaamisen-hankkmistavat-by-pato-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-opiskeluvalmiuksia-tukevat-opinnot! [c]
  (insert-multi!
    :opiskeluvalmiuksia_tukevat_opinnot
    (mapv h/to-sql c)))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id [id]
  (query
    [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id id]
    {:row-fn h/opiskeluvalmiuksia-tukevat-opinnot-from-sql}))

(defn insert-hankittava-yhteinen-tutkinnon-osa! [m]
  (insert-one!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-yhteinen-tutkinnon-osa-from-sql}))

(defn select-osaamisen-hankkimistavat-by-pyto-osa-alue-id [id]
  (query
    [queries/select-osaamisen-hankkimistavat-by-pyto-osa-alue-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-pyto-osa-alueen-osaamisen-hankkimistapa! [pyto-osa-alue-id oh-id]
  (insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    {:yhteisen_tutkinnon_osan_osa_alue_id pyto-osa-alue-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn insert-yhteisen-tutkinnon-osan-osa-alue! [osa-alue]
  (insert-one!
    :yhteisen_tutkinnon_osan_osa_alueet
    (h/yhteisen-tutkinnon-osan-osa-alue-to-sql osa-alue)))

(defn select-yto-osa-alueet-by-yto-id [id]
  (query
    [queries/select-yto-osa-alueet-by-yto-id id]
    {:row-fn h/yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn insert-yto-osa-alueen-osaamisen-osoittaminen! [yto-id naytto-id]
  (insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_naytot
    {:yhteisen_tutkinnon_osan_osa_alue_id yto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-hankitun-yto-osaamisen-nayton-osaamistavoitteet! [yto-id hon-id c]
  (insert-multi!
    :yto_osaamisen_osoittamisen_osaamistavoitteet
    (mapv
      #(hash-map
         :osaamisen_osoittaminen_id hon-id
         :osaamistavoite %)
      c)))

(defn select-hankitun-yto-osaamisen-nayton-osaamistavoitteet
  [naytto-id]
  (query
    [queries/select-hankitun-yto-osaamisen-nayton-osaamistavoitteet naytto-id]
    {:row-fn h/osaamistavoite-from-sql}))

(defn select-osaamisen-osoittamiset-by-yto-osa-alue-id [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-yto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))
