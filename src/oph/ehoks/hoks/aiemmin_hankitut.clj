(ns oph.ehoks.hoks.aiemmin-hankitut
  (:require [oph.ehoks.db.postgresql.aiemmin-hankitut :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.common :as c]
            [clojure.java.jdbc :as jdbc]))

; Tätä funktiota käytetään vielä testeissä, eikä pidä poistaa vielä
(defn get-tarkentavat-tiedot-osaamisen-arvioija
  "Hakee tarkentavien tietojen osaamisen arvioijan tietokannasta ID:n
  perusteella."
  [ttoa-id]
  (let [tta (db/select-todennettu-arviointi-lisatiedot-by-id ttoa-id)]
    (dissoc
      (assoc
        tta
        :aiemmin-hankitun-osaamisen-arvioijat
        (db/select-arvioijat-by-todennettu-arviointi-id ttoa-id))
      :id)))

(defn extract-tarkentavat-tiedot-osaamisen-arvioija
  "Irrottaa tarkentavien tietojen osaamisen arvioijan tietokannasta haetuista
  riveistä."
  [rows]
  (let [tta (mapv db-hoks/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql
                  (c/extract-from-joined-rows
                    :talkjoa__id
                    {:talkjoa__nimi           :nimi
                     :talkjoa__oppilaitos_oid :oppilaitos_oid}
                    rows))]
    (assoc
      (db-hoks/todennettu-arviointi-lisatiedot-from-sql
        (first (c/extract-from-joined-rows :tal__id
                                           {:tal__lahetetty_arvioitavaksi
                                            :lahetetty_arvioitavaksi}
                                           rows)))
      :aiemmin-hankitun-osaamisen-arvioijat
      tta)))

(defn extract-arvioijat-and-osoittamiset
  "Irrottaa arvioijat ja osaamisen osoittamiset tietokannasta haetuista
  riveistä."
  [rows from-sql-func fields]
  (let [oos (c/extract-osaamisen-osoittamiset rows)
        osa-objs (c/extract-from-joined-rows :osa__id fields rows)]
    (mapv (fn [osa]
            (assoc osa
                   :tarkentavat-tiedot-naytto
                   (c/process-subitems osa oos)
                   :tarkentavat-tiedot-osaamisen-arvioija
                   (extract-tarkentavat-tiedot-osaamisen-arvioija
                     (filterv #(= (:osa__id %) (:id osa)) rows))))
          (mapv from-sql-func osa-objs))))

(def ahato-fields
  "Kentät, jotka irrotetaan aiemmin hankituksi ammatilliseksi tutkinnon osaksi
  haetuista riveistä AHATO:n ydintiedoiksi."
  {:osa__id                         :id
   :osa__tutkinnon_osa_koodi_uri    :tutkinnon_osa_koodi_uri
   :osa__tutkinnon_osa_koodi_versio :tutkinnon_osa_koodi_versio
   :osa__koulutuksen_jarjestaja_oid :koulutuksen_jarjestaja_oid
   :osa__olennainen_seikka          :olennainen_seikka
   :osa__module_id                  :module_id
   :osa__tarkentavat_tiedot_osaamisen_arvioija_id
   :tarkentavat_tiedot_osaamisen_arvioija_id
   :osa__valittu_todentamisen_prosessi_koodi_uri
   :valittu_todentamisen_prosessi_koodi_uri
   :osa__valittu_todentamisen_prosessi_koodi_versio
   :valittu_todentamisen_prosessi_koodi_versio})

(defn get-aiemmin-hankitut-ammat-tutkinnon-osat
  "Hakee aiemmin hankitut ammatilliset tutkinnon osat tietokannasta HOKSin
  perusteella."
  [hoks-id]
  (mapv #(dissoc % :id)
        (extract-arvioijat-and-osoittamiset
          (db/select-all-ahatos-for-hoks hoks-id)
          db-hoks/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql
          ahato-fields)))

(defn get-aiemmin-hankittu-ammat-tutkinnon-osa
  "Hakee yhden aiemmin hankitun ammatillisen tutkinnon osan tietokannasta."
  [id]
  (first (extract-arvioijat-and-osoittamiset
           (db/select-one-ahato id)
           db-hoks/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql
           ahato-fields)))

(def ahpto-fields
  "Kentät, jotka irrotetaan aiemmin hankituksi paikalliseksi tutkinnon osaksi
  haetuista riveistä AHPTO:n ydintiedoiksi."
  {:osa__id                         :id
   :osa__koulutuksen_jarjestaja_oid :koulutuksen_jarjestaja_oid
   :osa__olennainen_seikka          :olennainen_seikka
   :osa__module_id                  :module_id
   :osa__laajuus                    :laajuus
   :osa__nimi                       :nimi
   :osa__tavoitteet_ja_sisallot     :tavoitteet_ja_sisallot
   :osa__amosaa_tunniste            :amosaa_tunniste
   :osa__lahetetty_arvioitavaksi    :lahetetty_arvioitavaksi
   :osa__tarkentavat_tiedot_osaamisen_arvioija_id
   :tarkentavat_tiedot_osaamisen_arvioija_id
   :osa__vaatimuksista_tai_tavoitteista_poikkeaminen
   :vaatimuksista_tai_tavoitteista_poikkeaminen
   :osa__valittu_todentamisen_prosessi_koodi_uri
   :valittu_todentamisen_prosessi_koodi_uri
   :osa__valittu_todentamisen_prosessi_koodi_versio
   :valittu_todentamisen_prosessi_koodi_versio})

(defn get-aiemmin-hankitut-paikalliset-tutkinnon-osat
  "Hakee aiemmin hankitut paikalliset tutkinnon osat tietokannasta HOKSin
  perusteella."
  [hoks-id]
  (mapv #(dissoc % :id)
        (extract-arvioijat-and-osoittamiset
          (db/select-all-ahptos-for-hoks hoks-id)
          db-hoks/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql
          ahpto-fields)))

(defn get-aiemmin-hankittu-paikallinen-tutkinnon-osa
  "Hakee yhden aiemmin hankitun paikallisen tutkinnon osan tietokannasta."
  [id]
  (first (extract-arvioijat-and-osoittamiset
           (db/select-one-ahpto id)
           db-hoks/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql
           ahpto-fields)))

(def ahyto-osa-alue-fields
  "Kentät, jotka irrotetaan aiemmin hankitun yhteisen tutkinnon osan
  osa-alueeksi haetuista riveistä AHYTO:n osa-alueen ydintiedoiksi."
  {:osa__id                         :id
   :osa__osa_alue_koodi_uri         :osa_alue_koodi_uri
   :osa__osa_alue_koodi_versio      :osa_alue_koodi_versio
   :osa__koulutuksen_jarjestaja_oid :koulutuksen_jarjestaja_oid
   :osa__olennainen_seikka          :olennainen_seikka
   :osa__module_id                  :module_id
   :osa__tarkentavat_tiedot_osaamisen_arvioija_id
   :tarkentavat_tiedot_osaamisen_arvioija_id
   :osa__vaatimuksista_tai_tavoitteista_poikkeaminen
   :vaatimuksista_tai_tavoitteista_poikkeaminen
   :osa__valittu_todentamisen_prosessi_koodi_uri
   :valittu_todentamisen_prosessi_koodi_uri
   :osa__valittu_todentamisen_prosessi_koodi_versio
   :valittu_todentamisen_prosessi_koodi_versio})

(defn get-ahyto-osa-alueet
  "Hakee aiemmin hankitun yhteisen tutkinnon osan osa-alueet tietokannasta."
  [ahyto-id]
  (mapv #(dissoc % :id)
        (extract-arvioijat-and-osoittamiset
          (db/select-all-osa-alueet-for-ahyto ahyto-id)
          db-hoks/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql
          ahyto-osa-alue-fields)))

(def ahyto-fields
  "Kentät, jotka irrotetaan aiemmin hankituksi yhteiseksi tutkinnon osaksi
  haetuista riveistä AHYTO:n ydintiedoiksi."
  {:osa__id                         :id
   :osa__tutkinnon_osa_koodi_uri    :tutkinnon_osa_koodi_uri
   :osa__tutkinnon_osa_koodi_versio :tutkinnon_osa_koodi_versio
   :osa__koulutuksen_jarjestaja_oid :koulutuksen_jarjestaja_oid
   :osa__lahetetty_arvioitavaksi    :lahetetty_arvioitavaksi
   :osa__module_id                  :module_id
   :osa__tarkentavat_tiedot_osaamisen_arvioija_id
   :tarkentavat_tiedot_osaamisen_arvioija_id
   :osa__valittu_todentamisen_prosessi_koodi_uri
   :valittu_todentamisen_prosessi_koodi_uri
   :osa__valittu_todentamisen_prosessi_koodi_versio
   :valittu_todentamisen_prosessi_koodi_versio})

(defn get-aiemmin-hankitut-yhteiset-tutkinnon-osat
  "Hakee aiemmin hankitut yhteiset tutkinnon osat tietokannasta HOKSin
  perusteella."
  [hoks-id]
  (mapv #(dissoc (assoc % :osa-alueet (get-ahyto-osa-alueet (:id %))) :id)
        (extract-arvioijat-and-osoittamiset
          (db/select-all-ahytos-for-hoks hoks-id)
          db-hoks/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql
          ahyto-fields)))

(defn get-aiemmin-hankittu-yhteinen-tutkinnon-osa
  "Hakee yhden aiemmin hankitun yhteisen tutkinnon osan tietokannasta."
  [id]
  (assoc (first (extract-arvioijat-and-osoittamiset
                  (db/select-one-ahyto id)
                  db-hoks/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql
                  ahyto-fields))
         :osa-alueet
         (get-ahyto-osa-alueet id)))

(defn save-ahpto-tarkentavat-tiedot-naytto!
  "Tallentaa aiemmin hankitun paikallisen tutkinnon osan tarkentavat tiedot
  tietokantaan."
  ([ahpto-id c]
    (save-ahpto-tarkentavat-tiedot-naytto!
      ahpto-id c (db-ops/get-db-connection)))
  ([ahpto-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [n (c/save-osaamisen-osoittaminen! % conn)]
           (db/insert-ahpto-osaamisen-osoittaminen! ahpto-id (:id n) conn)
           n)
        c))))

(defn save-tta-aiemmin-hankitun-osaamisen-arvioijat!
  "Tallentaa aiemmin hankitun tutkinnon osan osaamisen arvioijat tietokantaan."
  ([tta-id new-arvioijat]
    (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
      tta-id new-arvioijat (db-ops/get-db-connection)))
  ([tta-id new-arvioijat db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(db/insert-todennettu-arviointi-arvioijat! tta-id (:id %) conn)
        (db/insert-koulutuksen-jarjestaja-osaamisen-arvioijat!
          new-arvioijat conn)))))

(defn save-ahyto-tarkentavat-tiedot-naytto!
  "Tallentaa aiemmin hankitun yhteisen tutkinnon osan tarkentavat tiedot
  tietokantaan."
  ([ahyto-id new-values]
    (save-ahyto-tarkentavat-tiedot-naytto!
      ahyto-id new-values (db-ops/get-db-connection)))
  ([ahyto-id new-values db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [n (c/save-osaamisen-osoittaminen! % conn)]
           (db/insert-ahyto-osaamisen-osoittaminen! ahyto-id n conn)
           n)
        new-values))))

(defn save-tarkentavat-tiedot-osaamisen-arvioija!
  "Tallentaa tarkentavien tietojen osaamisen arvioijan tietokantaan."
  ([new-tta]
    (save-tarkentavat-tiedot-osaamisen-arvioija!
      new-tta (db-ops/get-db-connection)))
  ([new-tta db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tta-db (db/insert-todennettu-arviointi-lisatiedot! new-tta conn)]
        (save-tta-aiemmin-hankitun-osaamisen-arvioijat!
          (:id tta-db) (:aiemmin-hankitun-osaamisen-arvioijat new-tta) conn)
        tta-db))))

(defn- save-ahyto-osa-alue!
  "Tallentaa yhden aiemmin hankitun yhteisen tutkinnon osan osa-alueen
  tietokantaan."
  ([ahyto-id osa-alue]
    (save-ahyto-osa-alue! ahyto-id osa-alue (db-ops/get-db-connection)))
  ([ahyto-id osa-alue db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [arvioija (:tarkentavat-tiedot-osaamisen-arvioija osa-alue)
            stored-osa-alue
            (db/insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue!
              (assoc
                osa-alue
                :aiemmin-hankittu-yhteinen-tutkinnon-osa-id ahyto-id
                :tarkentavat-tiedot-osaamisen-arvioija-id
                (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                       arvioija conn)))
              conn)]
        (mapv
          (fn [naytto]
            (let [stored-naytto (c/save-osaamisen-osoittaminen! naytto conn)]
              (db/insert-ahyto-osa-alue-osaamisen-osoittaminen!
                (:id stored-osa-alue) (:id stored-naytto) conn)))
          (:tarkentavat-tiedot-naytto osa-alue))))))

(defn save-ahyto-osa-alueet!
  "Tallentaa aiemmin hankitun yhteisen tutkinnon osan osa-alueet tietokantaan."
  ([ahyto-id osa-alueet]
    (save-ahyto-osa-alueet! ahyto-id osa-alueet (db-ops/get-db-connection)))
  ([ahyto-id osa-alueet db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-ahyto-osa-alue! ahyto-id % conn)
        osa-alueet))))

(defn save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
  "Tallentaa yhden aiemmin hankitun yhteisen tutkinnon osan tietokantaan."
  ([hoks-id ahyto]
    (save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
      hoks-id ahyto (db-ops/get-db-connection)))
  ([hoks-id ahyto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tta (:tarkentavat-tiedot-osaamisen-arvioija ahyto)
            yto (db/insert-aiemmin-hankittu-yhteinen-tutkinnon-osa!
                  (assoc
                    ahyto
                    :hoks-id hoks-id
                    :tarkentavat-tiedot-osaamisen-arvioija-id
                    (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                           tta conn)))
                  conn)]
        (save-ahyto-tarkentavat-tiedot-naytto!
          (:id yto)
          (:tarkentavat-tiedot-naytto ahyto)
          conn)
        (save-ahyto-osa-alueet! (:id yto) (:osa-alueet ahyto) conn)
        yto))))

(defn save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
  "Tallentaa yhden aiemmin hankitun paikallisen tutkinnon osan tietokantaan."
  ([hoks-id ahpto]
    (save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
      hoks-id ahpto (db-ops/get-db-connection)))
  ([hoks-id ahpto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tta (:tarkentavat-tiedot-osaamisen-arvioija ahpto)
            ahpto-db (db/insert-aiemmin-hankittu-paikallinen-tutkinnon-osa!
                       (assoc
                         ahpto
                         :hoks-id hoks-id
                         :tarkentavat-tiedot-osaamisen-arvioija-id
                         (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                tta conn)))
                       conn)]
        (assoc
          ahpto-db
          :tarkentavat-tiedot-naytto
          (save-ahpto-tarkentavat-tiedot-naytto!
            (:id ahpto-db) (:tarkentavat-tiedot-naytto ahpto) conn))))))

(defn save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
  "Tallentaa aiemmin hankitut paikalliset tutkinnon osat tietokantaan."
  ([hoks-id c]
    (save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-aiemmin-hankittu-paikallinen-tutkinnon-osa! hoks-id % conn) c))))

(defn save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
  "Tallentaa aiemmin hankitut yhteiset tutkinnon osat tietokantaan."
  ([hoks-id c]
    (save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-aiemmin-hankittu-yhteinen-tutkinnon-osa! hoks-id % conn)
        c))))

(defn save-ahato-tarkentavat-tiedot-naytto!
  "Tallentaa aiemmin hankitun ammatillisen tutkinnon osan tarkentavat tiedot
  tietokantaan."
  ([ahato-id new-values]
    (save-ahato-tarkentavat-tiedot-naytto!
      ahato-id new-values (db-ops/get-db-connection)))
  ([ahato-id new-values db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [n (c/save-osaamisen-osoittaminen! %)]
           (db/insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto!
             ahato-id n conn)
           n)
        new-values))))

(defn save-aiemmin-hankittu-ammat-tutkinnon-osa!
  "Tallentaa yhden aiemmin hankitun ammatillisen tutkinnon osan tietokantaan."
  ([hoks-id ahato]
    (save-aiemmin-hankittu-ammat-tutkinnon-osa!
      hoks-id ahato (db-ops/get-db-connection)))
  ([hoks-id ahato db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [ahato-db (db/insert-aiemmin-hankittu-ammat-tutkinnon-osa!
                       (assoc
                         ahato
                         :hoks-id hoks-id
                         :tarkentavat-tiedot-osaamisen-arvioija-id
                         (:id (save-tarkentavat-tiedot-osaamisen-arvioija!
                                (:tarkentavat-tiedot-osaamisen-arvioija
                                  ahato))))
                       conn)]
        (assoc
          ahato-db
          :tarkentavat-tiedot-naytto
          (save-ahato-tarkentavat-tiedot-naytto!
            (:id ahato-db) (:tarkentavat-tiedot-naytto ahato) conn))))))

(defn save-aiemmin-hankitut-ammat-tutkinnon-osat!
  "Tallentaa aiemmin hankitut ammatilliset tutkinnon osat tietokantaan."
  ([hoks-id c]
    (save-aiemmin-hankitut-ammat-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-aiemmin-hankittu-ammat-tutkinnon-osa! hoks-id % conn) c))))

(defn- replace-ahato-tarkentavat-tiedot-naytto!
  "Korvaa aiemmin hankitun ammatillisen tutkinnon osan tarkentavat tiedot
  tietokannassa."
  [ahato-id new-values db-conn]
  (db/delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id!
    ahato-id db-conn)
  (save-ahato-tarkentavat-tiedot-naytto! ahato-id new-values db-conn))

(defn- replace-tta-aiemmin-hankitun-osaamisen-arvioijat!
  "Korvaa tarkentavien tietojen arvioinnin osaamisen arvioijat tietokannassa."
  [tta-id new-values db-conn]
  (db/delete-todennettu-arviointi-arvioijat-by-tta-id! tta-id db-conn)
  (save-tta-aiemmin-hankitun-osaamisen-arvioijat! tta-id new-values db-conn))

(defn- update-tarkentavat-tiedot-osaamisen-arvioija!
  "Päivittää tarkentavien tietojen osaamisen arvioijat tietokantaan."
  [tta-id new-tta-values db-conn]
  (db/update-todennettu-arviointi-lisatiedot-by-id!
    tta-id new-tta-values db-conn)
  (when-let [new-arvioijat
             (:aiemmin-hankitun-osaamisen-arvioijat new-tta-values)]
    (replace-tta-aiemmin-hankitun-osaamisen-arvioijat!
      tta-id new-arvioijat db-conn)))

(defn- replace-ahpto-tarkentavat-tiedot-naytto!
  "Korvaa aiemmin hankitun paikallisen tutkinnon osan tarkentavat tiedot
  tietokannassa."
  [ahpto-id new-values db-conn]
  (db/delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id!
    ahpto-id db-conn)
  (save-ahpto-tarkentavat-tiedot-naytto!
    ahpto-id new-values db-conn))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa!
  "Päivittää yhden aiemmin hankitun paikallisen tutkinnon osan tietokantaan."
  [ahpto-from-db new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id!
      (:id ahpto-from-db) new-values db-conn)
    (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
      (update-tarkentavat-tiedot-osaamisen-arvioija!
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahpto-from-db)
        new-tta db-conn))
    (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
      (replace-ahpto-tarkentavat-tiedot-naytto!
        (:id ahpto-from-db) new-ttn db-conn))))

(defn- replace-ahyto-tarkentavat-tiedot-naytto!
  "Korvaa aiemmin hankitun yhteisen tutkinnon osan tarkentavat tiedot
  tietokannassa."
  [ahyto-id new-values db-conn]
  (db/delete-aiemmin-hankitun-yhteisen-tutkinnon-osan-naytto-by-id!
    ahyto-id db-conn)
  (save-ahyto-tarkentavat-tiedot-naytto! ahyto-id new-values db-conn))

(defn- replace-ahyto-osa-alueet!
  "Korvaa aiemmin hankitun yhteisen tutkinnon osan osa-alueet tietokannassa."
  [ahyto-id new-values db-conn]
  (db/delete-aiemmin-hankitut-yto-osa-alueet-by-id! ahyto-id db-conn)
  (save-ahyto-osa-alueet! ahyto-id new-values db-conn))

(defn update-aiemmin-hankittu-yhteinen-tutkinnon-osa!
  "Päivittää yhden aiemmin hankitun yhteisen tutkinnon osan tietokantaan."
  [ahyto-from-db new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id!
      (:id ahyto-from-db) new-values db-conn)
    (when-let [new-ttoa (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
      (update-tarkentavat-tiedot-osaamisen-arvioija!
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahyto-from-db)
        new-ttoa db-conn))
    (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
      (replace-ahyto-tarkentavat-tiedot-naytto!
        (:id ahyto-from-db) new-ttn db-conn))
    (when-let [new-oa (:osa-alueet new-values)]
      (replace-ahyto-osa-alueet! (:id ahyto-from-db) new-oa db-conn))))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa!
  "Päivittää yhden aiemmin hankitun ammatillisen tutkinnon osan tietokantaan."
  [ahato-from-db new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id!
      (:id ahato-from-db) new-values db-conn)
    (when-let [new-tta (:tarkentavat-tiedot-osaamisen-arvioija new-values)]
      (update-tarkentavat-tiedot-osaamisen-arvioija!
        (:tarkentavat-tiedot-osaamisen-arvioija-id ahato-from-db)
        new-tta db-conn))
    (when-let [new-ttn (:tarkentavat-tiedot-naytto new-values)]
      (replace-ahato-tarkentavat-tiedot-naytto!
        (:id ahato-from-db) new-ttn db-conn))))
