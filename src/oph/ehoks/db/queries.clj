(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]))

(defmacro defquery [name filename]
  `(def ~name (slurp (io/file (io/resource ~filename)))))

(defquery select-hoks-by-oppija-oid "hoksit/select_by_oppija_oid.sql")
(defquery select-hoks-by-id "hoksit/select_by_id.sql")
(defquery select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-ammatilliset-tutkinnon-osat/select_by_hoks_id.sql")
(defquery select-hankitun-osaamisen-naytot-by-ooato-id
  (str "olemassa-olevat-ammatilliset-tutkinnon-osat/"
       "select_hankitun_osaamisen_naytot.sql"))
(defquery select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(defquery select-puuttuva-paikallinen-tutkinnon-osa-by-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_by_id.sql")
(defquery select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(defquery select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-yhteiset-tutkinnon-osat/select_by_hoks_id.sql")
(defquery select-hankitun-osaamisen-naytot-by-ppto-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_hankitun_osaamisen_naytot.sql")
(defquery select-koulutuksen-jarjestaja-arvioijat-by-hon-id
  "hankitun-osaamisen-naytot/select_koulutuksen_jarjestaja_arvioijat.sql")
(defquery select-tyoelama-arvioijat-by-hon-id
  "hankitun-osaamisen-naytot/select_tyoelama_arvioijat.sql")
(defquery select-nayttoymparisto-by-id "nayttoymparistot/select_by_id.sql")
(defquery select-tyotehtavat-by-hankitun-osaamisen-naytto-id
  "hankitun-osaamisen-naytot/select_tyotehtavat.sql")
(defquery select-osaamisen-hankkmistavat-by-ppto-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_osaamisen_hankkimistavat.sql")
(defquery select-tyopaikalla-hankittava-osaaminen-by-id
  "tyopaikalla-hankittavat-osaamiset/select_by_id.sql")
(defquery select-henkilot-by-tho-id
  "tyopaikalla-hankittavat-osaamiset/select_henkilot.sql")
(defquery select-tyotehtavat-by-tho-id
  "tyopaikalla-hankittavat-osaamiset/select_tyotehtavat.sql")
(defquery select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
  "muut-oppimisymparistot/select_by_osaamisen_hankkimistapa_id.sql")
(defquery select-todennettu-arviointi-lisatiedot-by-id
  "todennettu-arviointi-lisatiedot/select_by_id.sql")
(defquery select-arvioijat-by-todennettu-arviointi-id
  "todennettu-arviointi-lisatiedot/select_arvioijat.sql")
