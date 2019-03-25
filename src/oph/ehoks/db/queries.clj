(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]))

(defmacro defq [query-name filename]
  `(def ~query-name (slurp (io/file (io/resource ~filename)))))

(defq select-hoks-by-oppija-oid "hoksit/select_by_oppija_oid.sql")
(defq select-hoks-by-id "hoksit/select_by_id.sql")
(defq select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id
      "olemassa-olevat-ammatilliset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-hankitun-osaamisen-naytot-by-ooato-id
      (str "olemassa-olevat-ammatilliset-tutkinnon-osat/"
           "select_hankitun_osaamisen_naytot.sql"))
(defq select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id
      "puuttuvat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-puuttuva-paikallinen-tutkinnon-osa-by-id
      "puuttuvat-paikalliset-tutkinnon-osat/select_by_id.sql")
(defq select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id
      "olemassa-olevat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id
      "olemassa-olevat-yhteiset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-hankitun-osaamisen-naytot-by-ppto-id
      (str "puuttuvat-paikalliset-tutkinnon-osat/"
           "select_hankitun_osaamisen_naytot.sql"))
(defq select-koulutuksen-jarjestaja-arvioijat-by-hon-id
      "hankitun-osaamisen-naytot/select_koulutuksen_jarjestaja_arvioijat.sql")
(defq select-tyoelama-arvioijat-by-hon-id
      "hankitun-osaamisen-naytot/select_tyoelama_arvioijat.sql")
(defq select-nayttoymparisto-by-id "nayttoymparistot/select_by_id.sql")
(defq select-tyotehtavat-by-hankitun-osaamisen-naytto-id
      "hankitun-osaamisen-naytot/select_tyotehtavat.sql")
(defq select-osaamisen-hankkmistavat-by-ppto-id
      (str "puuttuvat-paikalliset-tutkinnon-osat/"
           "select_osaamisen_hankkimistavat.sql"))
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
      (str "olemassa-olevat-yhteiset-tutkinnon-osat/"
           "select_hankitun_osaamisen_naytot.sql"))
(defq select-hankitun-osaamisen-naytot-by-ooyto-osa-alue-id
      (str "olemassa-olevat-yhteiset-tutkinnon-osat/"
           "select_osa_alue_hankitun_osaamisen_naytot.sql"))
(defq select-arvioijat-by-ooyto-id
      (str "olemassa-olevat-yhteiset-tutkinnon-osat/"
           "select_koulutuksen_jarjestaja_arvioijat.sql"))
(defq select-osa-alueet-by-ooyto-id
      "olemassa-olevat-yhteiset-tutkinnon-osat/select_osa_alueet.sql")
