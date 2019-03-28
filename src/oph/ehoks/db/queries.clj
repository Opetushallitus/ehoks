(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(defn read-sql-file [f] (slurp (io/file (io/resource f))))

(def select-by-template (read-sql-file "select_by.sql"))

(def select-join-template (read-sql-file "select_join.sql"))

(defn populate-sql [m sql]
  (reduce
    (fn [c [k v]]
      (cstr/replace c (str k) v))
    sql
    m))

(defn generate-select-by [m]
  (populate-sql m select-by-template))

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

(defq select-hoks-by-oppija-oid "hoksit/select_by_oppija_oid.sql")
(defq select-hoksit-by-id)
(defq select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id)
(defq select-hankitun-osaamisen-naytot-by-ooato-id
      "olemassa-olevat-ammatilliset-tutkinnon-osat/"
      "select_hankitun_osaamisen_naytot.sql")
(defq select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id)
(defq select-puuttuvat-paikalliset-tutkinnon-osat-by-id)
(defq select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id)
(defq select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id)
(defq select-hankitun-osaamisen-naytot-by-ppto-id
      "puuttuvat-paikalliset-tutkinnon-osat/"
      "select_hankitun_osaamisen_naytot.sql")
(defq select-koulutuksen-jarjestaja-arvioijat-by-hon-id
      "hankitun-osaamisen-naytot/select_koulutuksen_jarjestaja_arvioijat.sql")
(defq select-tyoelama-arvioijat-by-hon-id
      "hankitun-osaamisen-naytot/select_tyoelama_arvioijat.sql")
(defq select-nayttoymparistot-by-id)
(def select-tyotehtavat-by-hankitun-osaamisen-naytto-id
  (generate-select-by {:table "hankitun_osaamisen_tyotehtavat"
                       :column "hankitun_osaamisen_naytto_id"}))
(defq select-osaamisen-hankkmistavat-by-ppto-id
      "puuttuvat-paikalliset-tutkinnon-osat/"
      "select_osaamisen_hankkimistavat.sql")
(defq select-tyopaikalla-hankittavat-osaamiset-by-id)
(def select-henkilot-by-tho-id
  (generate-select-by
    {:table "tyopaikalla_hankittavat_osaamisen_henkilot"
     :column "tyopaikalla_hankittava_osaaminen_id"}))
(def select-tyotehtavat-by-tho-id
    (generate-select-by
      {:table "tyopaikalla_hankittavat_osaamisen_tyotehtavat"
       :column "tyopaikalla_hankittava_osaaminen_id"}))
(defq select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
      "muut-oppimisymparistot/select_by_osaamisen_hankkimistapa_id.sql")
(defq select-todennettu-arviointi-lisatiedot-by-id)
(defq select-arvioijat-by-todennettu-arviointi-id
      "todennettu-arviointi-lisatiedot/select_arvioijat.sql")
(defq select-hankitun-osaamisen-naytot-by-ooyto-id
      "olemassa-olevat-yhteiset-tutkinnon-osat/"
      "select_hankitun_osaamisen_naytot.sql")
(defq select-hankitun-osaamisen-naytot-by-ooyto-osa-alue-id
      "olemassa-olevat-yhteiset-tutkinnon-osat/"
      "select_osa_alue_hankitun_osaamisen_naytot.sql")
(defq select-arvioijat-by-ooyto-id
      "olemassa-olevat-yhteiset-tutkinnon-osat/"
      "select_koulutuksen_jarjestaja_arvioijat.sql")
(defq select-osa-alueet-by-ooyto-id
      "olemassa-olevat-yhteiset-tutkinnon-osat/select_osa_alueet.sql")
(defq select-puuttuvat-ammatilliset-tutkinnon-osat-by-hoks-id)
(defq select-hankitun-osaamisen-naytot-by-pato-id
      "puuttuvat-ammatilliset-tutkinnon-osat/"
      "select_hankitun_osaamisen_naytot.sql")
(defq select-osaamisen-hankkmistavat-by-pato-id
      "puuttuvat-ammatilliset-tutkinnon-osat/"
      "select_osaamisen_hankkimistavat.sql")
(defq select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id)
