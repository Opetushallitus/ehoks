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
  ([queries]
    (query queries {}))
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
    :row-fn h/hoks-from-sql))

(defn select-hoks-by-oppija-oid [oid]
  (query
    [queries/select-hoksit-by-oppija-oid oid]
    :row-fn h/hoks-from-sql))

(defn select-hoks-by-id [id]
  (first
    (query
      [queries/select-hoksit-by-id id]
      {:row-fn h/hoks-from-sql})))

(defn select-hoks-by-eid [eid]
  (first
    (query
      [queries/select-hoksit-by-eid eid]
      {:row-fn h/hoks-from-sql})))

(defn select-hoksit-eid-by-eid [eid]
  (query
    [queries/select-hoksit-eid-by-eid eid]
    {}))

(defn select-hoksit-by-opiskeluoikeus-oid [oid]
  (query
    [queries/select-hoksit-by-opiskeluoikeus-oid oid]
    {:row-fn h/hoks-from-sql}))

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

(defn select-hoks-oppijat-without-index []
  (query
    [queries/select-hoks-oppijat-without-index]))

(defn select-hoks-opiskeluoikeudet-without-index []
  (query
    [queries/select-hoks-opiskeluoikeudet-without-index]))

(defn select-opiskeluoikeudet-by-oppija-oid [oppija-oid]
  (query
    [queries/select-opiskeluoikeudet-by-oppija-oid oppija-oid]
    {:row-fn h/from-sql}))

(defn insert-oppija [oppija]
  (insert-one! :oppijat (h/to-sql oppija)))

(defn insert-opiskeluoikeus [opiskeluoikeus]
  (insert-one! :opiskeluoikeudet (h/to-sql opiskeluoikeus)))

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

(defn insert-todennettu-arviointi-arvioijat! [tta-id arvioija-id]
  (insert-one!
    :todennettu_arviointi_arvioijat
    {:todennettu_arviointi_lisatiedot_id tta-id
     :koulutuksen_jarjestaja_arvioija_id arvioija-id}))

(defn insert-koulutuksen-jarjestaja-arvioijat! [c]
  (insert-multi!
    :koulutuksen_jarjestaja_arvioijat
    (map h/koulutuksen-jarjestaja-arvioija-to-sql c)))

(defn insert-koulutuksen-jarjestaja-arvioija! [m]
  (insert-one!
    :koulutuksen_jarjestaja_arvioijat
    (h/koulutuksen-jarjestaja-arvioija-to-sql m)))

(defn select-tarkentavat-tiedot-naytto-by-ooato-id
  "Olemassa olevan ammatillisen tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (query
    [queries/select-hankitun-osaamisen-naytot-by-ooato-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

(defn insert-olemassa-olevan-ammatillisen-tutkinnon-osan-naytto! [ooato-id n]
  (insert-one!
    :olemassa_olevan_ammatillisen_tutkinnon_osan_naytto
    {:olemassa_oleva_ammatillinen_tutkinnon_osa_id ooato-id
     :hankitun_osaamisen_naytto_id (:id n)}))

(defn insert-ooyto-hankitun-osaamisen-naytto! [ooyto n]
  (insert-one!
    :olemassa_olevan_yhteisen_tutkinnon_osan_naytto
    {:olemassa_oleva_yhteinen_tutkinnon_osa_id (:id ooyto)
     :hankitun_osaamisen_naytto_id (:id n)}))

(defn insert-koodisto-koodi! [m]
  (insert-one!
    :koodisto_koodit
    (h/to-sql m)))

(defn insert-hankitun-osaamisen-nayton-osa-alue! [naytto-id koodi-id]
  (insert-one!
    :hankitun_osaamisen_nayton_osa_alueet
    {:hankitun_osaamisen_naytto_id naytto-id
     :koodisto_koodi_id koodi-id}))

(defn select-osa-alueet-by-hankitun-osaamisen-naytto [naytto-id]
  (query
    [queries/select-osa-alueet-by-hankitun-osaamisen-naytto naytto-id]
    {:row-fn h/koodi-uri-from-sql}))

(defn select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-id [id]
  (->
    (query [queries/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-id
            id])
    first
    h/olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql))

(defn select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql}))

(defn insert-olemassa-oleva-ammatillinen-tutkinnon-osa! [m]
  (insert-one!
    :olemassa_olevat_ammatilliset_tutkinnon_osat
    (h/olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql m)))

(defn insert-olemassa-olevat-ammatilliset-tutkinnon-osat! [c]
  (insert-multi!
    :olemassa_olevat_ammatilliset_tutkinnon_osat
    (map h/olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql c)))

(defn select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/puuttuva-paikallinen-tutkinnon-osa-from-sql}))

(defn select-puuttuva-paikallinen-tutkinnon-osa-by-id [id]
  (first
    (query
      [queries/select-puuttuvat-paikalliset-tutkinnon-osat-by-id id]
      {:row-fn h/puuttuva-paikallinen-tutkinnon-osa-from-sql})))

(defn insert-puuttuvat-paikalliset-tutkinnon-osat! [c]
  (insert-multi!
    :puuttuvat_paikalliset_tutkinnon_osat
    (map h/puuttuva-paikallinen-tutkinnon-osa-to-sql c)))

(defn insert-puuttuva-paikallinen-tutkinnon-osa! [m]
  (insert-one!
    :puuttuvat_paikalliset_tutkinnon_osat
    (h/puuttuva-paikallinen-tutkinnon-osa-to-sql m)))

(defn update-puuttuva-paikallinen-tutkinnon-osa-by-id! [id m]
  (update!
    :puuttuvat_paikalliset_tutkinnon_osat
    (h/puuttuva-paikallinen-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn select-hankitun-osaamisen-naytot-by-ppto-id
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (query
    [queries/select-hankitun-osaamisen-naytot-by-ppto-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

(defn delete-osaamisen-hankkimistavat-by-ppto-id!
  "Puuttuvan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (shallow-delete!
    :puuttuvan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    ["puuttuva_paikallinen_tutkinnon_osa_id = ?" id]))

(defn delete-hankitun-osaamisen-naytot-by-ppto-id!
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (shallow-delete!
    :puuttuvan_paikallisen_tutkinnon_osan_naytto
    ["puuttuva_paikallinen_tutkinnon_osa_id = ?" id]))

(defn insert-tho-henkilot!
  "Työpaikalla hankittavan osaamisen muut osallistujat"
  [o c]
  (insert-multi!
    :tyopaikalla_hankittavat_osaamisen_henkilot
    (map
      #(assoc (h/henkilo-to-sql %) :tyopaikalla_hankittava_osaaminen_id (:id o))
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
    :tyopaikalla_hankittavat_osaamisen_tyotehtavat
    (map
      #(hash-map
         :tyopaikalla_hankittava_osaaminen_id (:id tho)
         :tyotehtava %)
      c)))

(defn select-tyotehtavat-by-tho-id
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [id]
  (query
    [queries/select-tyotehtavat-by-tho-id id]
    {:row-fn h/tyotehtava-from-sql}))

(defn insert-tyopaikalla-hankittava-osaaminen! [o]
  (when (some? o)
    (let [o-db (insert-one!
                 :tyopaikalla_hankittavat_osaamiset
                 (h/tyopaikalla-hankittava-osaaminen-to-sql o))]
      (insert-tho-henkilot! o-db (:muut-osallistujat o))
      (insert-tho-tyotehtavat! o-db (:keskeiset-tyotehtavat o))
      o-db)))

(defn select-tyopaikalla-hankittava-osaaminen-by-id [id]
  (first
    (query
      [queries/select-tyopaikalla-hankittavat-osaamiset-by-id id]
      {:row-fn h/tyopaikalla-hankittava-osaaminen-from-sql})))

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

(defn insert-puuttuvan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
  [ppto oh]
  (insert-one!
    :puuttuvan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    {:puuttuva_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_hankkimistapa_id (:id oh)}))

(defn select-osaamisen-hankkimistavat-by-ppto-id
  "Puuttuvan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (query
    [queries/select-osaamisen-hankkmistavat-by-ppto-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-hankitun-osaamisen-naytto! [m]
  (insert-one!
    :hankitun_osaamisen_naytot
    (h/hankitun-osaamisen-naytto-to-sql m)))

(defn insert-ppto-hankitun-osaamisen-naytto!
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näyttö"
  [ppto h]
  (insert-one!
    :puuttuvan_paikallisen_tutkinnon_osan_naytto
    {:puuttuva_paikallinen_tutkinnon_osa_id (:id ppto)
     :hankitun_osaamisen_naytto_id (:id h)}))

(defn insert-ppto-hankitun-osaamisen-naytot!
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [ppto c]
  (let [h-col (insert-multi!
                :hankitun_osaamisen_naytot
                (map h/hankitun-osaamisen-naytto-to-sql c))]
    (insert-multi!
      :puuttuvan_paikallisen_tutkinnon_osan_naytto
      (map #(hash-map
              :puuttuva_paikallinen_tutkinnon_osa_id (:id ppto)
              :hankitun_osaamisen_naytto_id (:id %))
           h-col))
    h-col))

(defn insert-hankitun-osaamisen-nayton-koulutuksen-jarjestaja-arvioijat! [hon c]
  (let [kja-col (insert-multi!
                  :koulutuksen_jarjestaja_arvioijat
                  (map h/koulutuksen-jarjestaja-arvioija-to-sql c))]
    (insert-multi!
      :hankitun_osaamisen_nayton_koulutuksen_jarjestaja_arvioija
      (map #(hash-map
              :hankitun_osaamisen_naytto_id (:id hon)
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

(defn insert-hankitun-osaamisen-nayton-tyoelama-arvioija! [hon arvioija]
  (insert-one!
    :hankitun_osaamisen_nayton_tyoelama_arvioija
    {:hankitun_osaamisen_naytto_id (:id hon)
     :tyoelama_arvioija_id (:id arvioija)}))

(defn select-tyoelama-arvioijat-by-hon-id
  "Hankitun osaamisen näytön työelemän arvioijat"
  [id]
  (query
    [queries/select-tyoelama-arvioijat-by-hon-id id]
    {:row-fn h/tyoelama-arvioija-from-sql}))

(defn insert-hankitun-osaamisen-nayton-tyotehtavat! [hon c]
  (insert-multi!
    :hankitun_osaamisen_tyotehtavat
    (map #(hash-map :hankitun_osaamisen_naytto_id (:id hon) :tyotehtava %) c)))

(defn select-tyotehtavat-by-hankitun-osaamisen-naytto-id [id]
  (query
    [queries/select-tyotehtavat-by-hankitun-osaamisen-naytto-id id]
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

(defn select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-paikallinen-tutkinnon-osa-from-sql}))

(defn insert-olemassa-oleva-paikallinen-tutkinnon-osa! [m]
  (insert-one!
    :olemassa_olevat_paikalliset_tutkinnon_osat
    (h/olemassa-oleva-paikallinen-tutkinnon-osa-to-sql m)))

(defn select-hankitun-osaamisen-naytto-by-oopto-id [id]
  (query
    [queries/select-tarkentavat-tiedot-naytto-by-oopto-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

(defn insert-oopto-arvioija! [oopto-id arvioija-id]
  (insert-one!
    :olemassa_olevan_paikallisen_tutkinnon_osan_arvioijat
    {:olemassa_oleva_paikallinen_tutkinnon_osa_id oopto-id
     :koulutuksen_jarjestaja_arvioija_id arvioija-id}))

(defn select-arvioijat-by-oopto-id [id]
  (query
    [queries/select-arvioijat-by-oopto-id id]
    {:row-fn h/koulutuksen-jarjestaja-arvioija-from-sql}))

(defn insert-oopto-hankitun-osaamisen-naytto! [oopto-id naytto-id]
  (insert-one!
    :olemassa_olevan_paikallisen_tutkinnon_osan_naytto
    {:olemassa_oleva_paikallinen_tutkinnon_osa_id oopto-id
     :hankitun_osaamisen_naytto_id naytto-id}))

(defn insert-ooyto-arvioija! [yto-id a-id]
  (insert-one!
    :olemassa_olevan_yhteisen_tutkinnon_osan_arvioijat
    {:olemassa_oleva_yhteinen_tutkinnon_osa_id yto-id
     :koulutuksen_jarjestaja_arvioija_id a-id}))

(defn select-arvioija-by-ooyto-id [id]
  (query
    [queries/select-arvioijat-by-ooyto-id id]
    {:row-fn h/koulutuksen-jarjestaja-arvioija-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ooyto-id
  "Olemassa olevan yhteisen tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (query
    [queries/select-hankitun-osaamisen-naytot-by-ooyto-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ooyto-osa-alue-id [id]
  (query
    [queries/select-hankitun-osaamisen-naytot-by-ooyto-osa-alue-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

(defn insert-ooyto-osa-alue-hankitun-osaamisen-naytto! [osa-alue-id naytto-id]
  (insert-one!
    :olemassa_olevan_yto_osa_alueen_naytto
    {:olemassa_oleva_yto_osa_alue_id osa-alue-id
     :hankitun_osaamisen_naytto_id naytto-id}))

(defn select-osa-alueet-by-ooyto-id [id]
  (query
    [queries/select-osa-alueet-by-ooyto-id id]
    {:row-fn h/olemassa-olevan-yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn insert-olemassa-olevan-yhteisen-tutkinnon-osan-osa-alue! [m]
  (insert-one!
    :olemassa_olevat_yto_osa_alueet
    (h/olemassa-olevan-yhteisen-tutkinnon-osan-osa-alue-to-sql m)))

(defn insert-olemassa-oleva-yhteinen-tutkinnon-osa! [m]
  (insert-one!
    :olemassa_olevat_yhteiset_tutkinnon_osat
    (h/olemassa-oleva-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-yhteinen-tutkinnon-osa-from-sql}))

(defn insert-puuttuva-ammatillinen-tutkinnon-osa! [m]
  (insert-one!
    :puuttuvat_ammatilliset_tutkinnon_osat
    (h/puuttuva-ammatillinen-tutkinnon-osa-to-sql m)))

(defn select-puuttuva-ammatillinen-tutkinnon-osa-by-id [id]
  (->
    (query
      [queries/select-puuttuvat-ammatilliset-tutkinnon-osat-by-id id])
    first
    h/puuttuva-ammatillinen-tutkinnon-osa-from-sql))

(defn select-puuttuvat-ammatilliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-puuttuvat-ammatilliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/puuttuva-ammatillinen-tutkinnon-osa-from-sql}))

(defn insert-pato-hankitun-osaamisen-naytto! [pato-id naytto-id]
  (insert-one!
    :puuttuvan_ammatillisen_tutkinnon_osan_naytto
    {:puuttuva_ammatillinen_tutkinnon_osa_id pato-id
     :hankitun_osaamisen_naytto_id naytto-id}))

(defn select-hankitun-osaamisen-naytot-by-pato-id [id]
  (query
    [queries/select-hankitun-osaamisen-naytot-by-pato-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

(defn delete-osaamisen-hankkimistavat-by-pato-id!
  "Puuttuvan ammatillisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (shallow-delete!
    :puuttuvan_ammatillisen_tutkinnon_osan_osaamisen_hankkimistavat
    ["puuttuva_ammatillinen_tutkinnon_osa_id = ?" id]))

(defn delete-hankitun-osaamisen-naytot-by-pato-id!
  "Puuttuvan ammatillisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (shallow-delete!
    :puuttuvan_ammatillisen_tutkinnon_osan_naytto
    ["puuttuva_ammatillinen_tutkinnon_osa_id = ?" id]))

(defn update-puuttuva-ammatillinen-tutkinnon-osa-by-id! [id m]
  (update!
    :puuttuvat_ammatilliset_tutkinnon_osat
    (h/puuttuva-ammatillinen-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-olemassa-oleva-ammatillinen-tutkinnon-osat-by-id! [id new-values]
  (update!
    :olemassa_olevat_ammatilliset_tutkinnon_osat
    (h/olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-todennettu-arviointi-lisatiedot-by-id! [id new-values]
  (update!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn delete-todennettu-arviointi-arvioijat-by-tta-id! [id]
  (shallow-delete!
    :todennettu_arviointi_arvioijat
    ["todennettu_arviointi_lisatiedot_id = ?" id]))

(defn delete-olemassa-olevan-ammatillisen-tutkinnon-osan-naytto-by-id! [id]
  (shallow-delete!
    :olemassa_olevan_ammatillisen_tutkinnon_osan_naytto
    ["olemassa_oleva_ammatillinen_tutkinnon_osa_id = ?" id]))

(defn insert-puuttuvan-ammatillisen-tutkinnon-osan-osaamisen-hankkimistapa!
  [pato-id oh-id]
  (insert-one!
    :puuttuvan_ammatillisen_tutkinnon_osan_osaamisen_hankkimistavat
    {:puuttuva_ammatillinen_tutkinnon_osa_id pato-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn select-osaamisen-hankkimistavat-by-pato-id
  "Puuttuvan ammatillisen tutkinnon osan osaamisen hankkimistavat"
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

(defn insert-puuttuva-yhteinen-tutkinnon-osa! [m]
  (insert-one!
    :puuttuvat_yhteiset_tutkinnon_osat
    (h/puuttuva-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-puuttuvat-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-puuttuvat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/puuttuva-yhteinen-tutkinnon-osa-from-sql}))

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

(defn insert-yto-osa-alueen-hankitun-osaamisen-naytto! [yto-id naytto-id]
  (insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_naytot
    {:yhteisen_tutkinnon_osan_osa_alue_id yto-id
     :hankitun_osaamisen_naytto_id naytto-id}))

(defn insert-hankitun-yto-osaamisen-nayton-osaamistavoitteet! [yto-id hon-id c]
  (insert-multi!
    :hankitun_yto_osaamisen_nayton_osaamistavoitteet
    (mapv
      #(hash-map
         :hankitun_osaamisen_naytto_id hon-id
         :osaamistavoite %)
      c)))

(defn select-hankitun-yto-osaamisen-nayton-osaamistavoitteet
  [naytto-id]
  (query
    [queries/select-hankitun-yto-osaamisen-nayton-osaamistavoitteet naytto-id]
    {:row-fn h/osaamistavoite-from-sql}))

(defn select-hankitun-osaamisen-naytot-by-yto-osa-alue-id [id]
  (query
    [queries/select-hankitun-osaamisen-naytot-by-yto-osa-alue-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))
