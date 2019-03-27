(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(def select-by-hoks-id-template
  (slurp (io/file (io/resource "select_by_hoks_id.sql"))))

(def select-by-id-template
  (slurp (io/file (io/resource "select_by_id.sql"))))

(defn remove-by [s]
  (cstr/replace
    s
    (cond (.endsWith s "-by-hoks-id")
          #"-by-hoks-id"
          (.endsWith s "-by-id")
          #"-by-id")
    ""))

(defn parse-table-name [s]
  (-> s
      (cstr/replace #"select-" "")
      remove-by
      (cstr/replace #"-" "_")))

(defn select-by-id? [query-name]
  (and
    (.startsWith query-name "select-")
    (.endsWith query-name "-by-id")))

(defn select-by-hoks-id? [query-name]
  (and
    (.startsWith query-name "select-")
    (.endsWith query-name "-by-hoks-id")))

(defn read-query [f]
  (slurp (io/file (io/resource (cstr/join f)))))

(defn generate-query [n]
  (cond
    (select-by-hoks-id? n)
    (cstr/replace
      select-by-hoks-id-template
      #"\{\{table_name\}\}"
      (parse-table-name n))
    (select-by-id? n)
    (cstr/replace
      select-by-id-template
      #"\{\{table_name\}\}"
      (parse-table-name n))))

(defmacro defq [query-name & filename]
  `(def ~query-name (if (seq (quote ~filename))
                      (read-query (quote ~filename))
                      (generate-query (str (quote ~query-name))))))

(defq select-hoks-by-oppija-oid "hoksit/select_by_oppija_oid.sql")
(defq select-hoks-by-id "hoksit/select_by_id.sql")
(defq select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id)
(defq select-hankitun-osaamisen-naytot-by-ooato-id
      "olemassa-olevat-ammatilliset-tutkinnon-osat/"
      "select_hankitun_osaamisen_naytot.sql")
(defq select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id)
(defq select-puuttuva-paikallinen-tutkinnon-osa-by-id
      "puuttuvat-paikalliset-tutkinnon-osat/select_by_id.sql")
(defq select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id)
(defq select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id)
(defq select-hankitun-osaamisen-naytot-by-ppto-id
      "puuttuvat-paikalliset-tutkinnon-osat/"
      "select_hankitun_osaamisen_naytot.sql")
(defq select-koulutuksen-jarjestaja-arvioijat-by-hon-id
      "hankitun-osaamisen-naytot/select_koulutuksen_jarjestaja_arvioijat.sql")
(defq select-tyoelama-arvioijat-by-hon-id
      "hankitun-osaamisen-naytot/select_tyoelama_arvioijat.sql")
(defq select-nayttoymparisto-by-id "nayttoymparistot/select_by_id.sql")
(defq select-tyotehtavat-by-hankitun-osaamisen-naytto-id
      "hankitun-osaamisen-naytot/select_tyotehtavat.sql")
(defq select-osaamisen-hankkmistavat-by-ppto-id
      "puuttuvat-paikalliset-tutkinnon-osat/"
      "select_osaamisen_hankkimistavat.sql")
(defq select-tyopaikalla-hankittava-osaaminen-by-id
      "tyopaikalla-hankittavat-osaamiset/select_by_id.sql")
(defq select-henkilot-by-tho-id
      "tyopaikalla-hankittavat-osaamiset/select_henkilot.sql")
(defq select-tyotehtavat-by-tho-id
      "tyopaikalla-hankittavat-osaamiset/select_tyotehtavat.sql")
(defq select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
      "muut-oppimisymparistot/select_by_osaamisen_hankkimistapa_id.sql")
(defq select-todennettu-arviointi-lisatiedot-by-id
      "todennettu-arviointi-lisatiedot/select_by_id.sql")
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
