(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]))

(defmacro query [name filename]
  `(def ~name (slurp (io/file (io/resource ~filename)))))

(query select-hoks-by-oppija-oid "hoksit/select_by_oppija_oid.sql")
(query select-hoks-by-id "hoksit/select_by_id.sql")
(query select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-ammatilliset-tutkinnon-osat/select_by_hoks_id.sql")
(query select-hankitun-osaamisen-naytot-by-ooato-id
  (str "olemassa-olevat-ammatilliset-tutkinnon-osat/"
       "select_hankitun_osaamisen_naytot.sql"))
(query select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(query select-puuttuva-paikallinen-tutkinnon-osa-by-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_by_id.sql")
(query select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(query select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-yhteiset-tutkinnon-osat/select_by_hoks_id.sql")
(query select-hankitun-osaamisen-naytot-by-ppto-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_hankitun_osaamisen_naytot.sql")
(query select-koulutuksen-jarjestaja-arvioijat-by-hon-id
  "hankitun-osaamisen-naytot/select_koulutuksen_jarjestaja_arvioijat.sql")
(query select-tyoelama-arvioijat-by-hon-id
  "hankitun-osaamisen-naytot/select_tyoelama_arvioijat.sql")
(query select-nayttoymparisto-by-id "nayttoymparistot/select_by_id.sql")
(query select-tyotehtavat-by-hankitun-osaamisen-naytto-id
  "hankitun-osaamisen-naytot/select_tyotehtavat.sql")
(query select-osaamisen-hankkmistavat-by-ppto-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_osaamisen_hankkimistavat.sql")
(query select-tyopaikalla-hankittava-osaaminen-by-id
  "tyopaikalla-hankittavat-osaamiset/select_by_id.sql")
(query select-henkilot-by-tho-id
  "tyopaikalla-hankittavat-osaamiset/select_henkilot.sql")
(query select-tyotehtavat-by-tho-id
  "tyopaikalla-hankittavat-osaamiset/select_tyotehtavat.sql")
(query select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
  "muut-oppimisymparistot/select_by_osaamisen_hankkimistapa_id.sql")
(query select-todennettu-arviointi-lisatiedot-by-id
  "todennettu-arviointi-lisatiedot/select_by_id.sql")
(query select-arvioijat-by-todennettu-arviointi-id
  "todennettu-arviointi-lisatiedot/select_arvioijat.sql")
