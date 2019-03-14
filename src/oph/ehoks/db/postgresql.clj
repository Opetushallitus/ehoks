(ns oph.ehoks.db.postgresql
  (:require [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.hoks :as h]
            [clj-time.coerce :as c]
            [oph.ehoks.db.queries :as queries]))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [value] (c/to-sql-date value)))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [o _ _]
    (.toLocalDate o)))

(defn query
  ([queries opts]
   (jdbc/query {:connection-uri (:database-url config)} queries opts))
  ([queries arg & opts]
    (query queries (apply hash-map arg opts))))

(defn insert! [t v]
  (jdbc/insert! {:connection-uri (:database-url config)} t v))

(defn insert-one! [t v] (first (insert! t v)))

(defn insert-multi! [t v]
  (jdbc/insert-multi! {:connection-uri (:database-url config)} t v))

(defn select-hoks-by-oppija-oid [oid]
  (query
    [queries/select-hoks-by-oppija-oid oid]
    :row-fn h/hoks-from-sql))

(defn select-hoks-by-id [id]
  (query
    [queries/select-hoks-by-id id]
    {:row-fn h/hoks-from-sql}))

(defn insert-hoks! [hoks]
  (insert! :hoksit (h/hoks-to-sql hoks)))

(defn select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql}))

(defn insert-olemassa-oleva-ammatillinen-tutkinnon-osa! [m]
  (insert! :hoksit (h/olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql m)))

(defn select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/puuttuva-paikallinen-tutkinnon-osa-from-sql}))

(defn insert-puuttuvat-paikalliset-tutkinnon-osat! [c]
  (insert-multi!
    :puuttuvat_paikalliset_tutkinnon_osat
    (map h/puuttuva-paikallinen-tutkinnon-osa-to-sql c)))

(defn select-hankitun-osaamisen-naytot-by-ppto-id
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (query
    [queries/select-hankitun-osaamisen-naytot-by-ppto-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

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
      [queries/select-tyopaikalla-hankittava-osaaminen-by-id id]
      {:row-fn h/tyopaikalla-hankittava-osaaminen-from-sql})))

(defn insert-osaamisen-hankkimistavan-muut-oppimisymparistot! [oh c]
  (insert-multi!
    :muut_oppimisymparistot
    (map
      #(h/muu-oppimisymparisto-to-sql
         (assoc % :osaamisen-hankkimistapa-id (:id oh)))
      c)))

(defn select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id [id]
  (query
    [queries/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id id]
    {:row-fn h/muu-oppimisymparisto-from-sql}))

(defn insert-ppto-osaamisen-hankkimistavat!
  "Puuttuvan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [ppto c]
  (let [oht-col
        (mapv
          (fn [o]
            (let [tho (insert-tyopaikalla-hankittava-osaaminen!
                        (:tyopaikalla-hankittava-osaaminen o))
                  o-db (insert-one!
                         :osaamisen_hankkimistavat
                         (-> o
                             (assoc :tyopaikalla-hankittava-osaaminen-id
                                    (:id tho))
                             (dissoc :tyopaikalla-hankittava-osaaminen)
                             h/osaamisen-hankkimistavat-to-sql))]
              (insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
                o-db (:muut-oppimisymparisto o))
              o-db))
          c)]
    (insert-multi!
      :puuttuvan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
      (map
        #(hash-map
           :puuttuva_paikallinen_tutkinnon_osa_id (:id ppto)
           :osaamisen_hankkimistapa_id (:id %))
        oht-col))
    oht-col))

(defn select-osaamisen-hankkimistavat-by-ppto-id
  "Puuttuvan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (query
    [queries/select-osaamisen-hankkmistavat-by-ppto-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-ppto-hankitun-osaamisen-naytot!
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [ppto c]
  (let [h-col (insert-multi!
                :hankitun_osaamisen_naytot
                (map h/hankitun-osaamisen-naytto-to-sql c))]
    (insert-multi!
      :puuttuvan_paikallisen_tutkinnon_osan_hankitun_osaamisen_naytto
      (map #(hash-map
              :puuttuva_paikallinen_tutkinnon_osa_id (:id ppto)
              :hankitun_osaamisen_naytto_id (:id %))
           h-col))
    h-col))

(defn insert-hankitun-osaamisen-nayton-koulutuksen-jarjestaja-arvioijat [hon c]
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

(defn insert-hankitun-osaamisen-nayton-tyoelama-arvioijat [hon c]
  (let [kja-col (insert-multi!
                :tyoelama_arvioijat
                (map h/tyoelama-arvioija-to-sql c))]
    (insert-multi!
      :hankitun_osaamisen_nayton_tyoelama_arvioija
      (map #(hash-map
              :hankitun_osaamisen_naytto_id (:id hon)
              :tyoelama_arvioija_id (:id %))
           kja-col))
    kja-col))

(defn select-tyoelama-arvioijat-by-hon-id
  "Hankitun osaamisen näytön työelemän arvioijat"
  [id]
  (query
    [queries/select-tyoelama-arvioijat-by-hon-id id]
    {:row-fn h/tyoelama-arvioija-from-sql}))

(defn insert-hankitun-osaamisen-nayton-tyotehtavat [hon c]
  (insert-multi!
    :hankitun_osaamisen_tyotehtavat
    (map #(hash-map :hankitun_osaamisen_naytto_id (:id hon) :tyotehtava %) c)))

(defn select-tyotehtavat-by-hankitun-osaamisen-naytto-id [id]
  (query
    [queries/select-tyotehtavat-by-hankitun-osaamisen-naytto-id id]
    {:row-fn h/tyotehtava-from-sql}))

(defn insert-nayttoymparisto! [m]
  (insert!
    :nayttoymparistot
    (h/nayttoymparisto-to-sql m)))

(defn select-nayttoymparisto-by-id [id]
  (first
    (query
      [queries/select-nayttoymparisto-by-id id]
      {:row-fn h/nayttoymparisto-from-sql})))

(defn select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-paikallinen-tutkinnon-osa-from-sql}))

(defn select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-yhteinen-tutkinnon-osa-from-sql}))
