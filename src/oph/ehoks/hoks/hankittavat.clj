(ns oph.ehoks.hoks.hankittavat
  (:require [oph.ehoks.db.postgresql.hankittavat :as db]
            [oph.ehoks.hoks.common :as c]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [clj-time.core :as t]
            [clojure.tools.logging :as log])
  (:import (java.time LocalDate)))

(defn extract-and-set-osaamisen-hankkimistapa-values [oht rows]
  (let [this-oht-rows (filterv #(= (:oh__id %) (:id oht)) rows)
        kjs (mapv db-hoks/keskeytymisajanjakso-from-sql
                  (c/extract-from-joined-rows :kj__id
                                              {:kj__alku  :alku
                                               :kj__loppu :loppu}
                                              this-oht-rows))
        moys (c/extract-from-joined-rows
               :moy__id
               {:moy__alku                       :alku
                :moy__loppu                      :loppu
                :moy__oppimisymparisto_koodi_uri :oppimisymparisto-koodi-uri
                :moy__oppimisymparisto_koodi_versio
                :oppimisymparisto-koodi-versio}
               this-oht-rows)
        oht-final (dissoc (assoc oht
                                 :keskeytymisajanjaksot  kjs
                                 :muut-oppimisymparistot moys)
                          :tyopaikalla_jarjestettava_koulutus_id)]
    (if (some? (:tyopaikalla_jarjestettava_koulutus_id oht))
      (assoc oht-final
             :tyopaikalla-jarjestettava-koulutus
             (assoc (db-hoks/tyopaikalla-jarjestettava-koulutus-from-sql
                      (first
                        (c/extract-from-joined-rows
                          :tjk__id
                          {:tjk__tyopaikan_nimi     :tyopaikan_nimi
                           :tjk__tyopaikan_y_tunnus :tyopaikan_y_tunnus
                           :tjk__vastuullinen_tyopaikka_ohjaaja_nimi
                           :vastuullinen_tyopaikka_ohjaaja_nimi
                           :tjk__vastuullinen_tyopaikka_ohjaaja_sahkoposti
                           :vastuullinen_tyopaikka_ohjaaja_sahkoposti
                           :tjk__vastuullinen_tyopaikka_ohjaaja_puhelinnumero
                           :vastuullinen_tyopaikka_ohjaaja_puhelinnumero}
                          this-oht-rows)))
                    :keskeiset-tyotehtavat
                    (mapv :tyotehtava
                          (c/extract-from-joined-rows
                            :tjkt__id
                            {:tjkt__tyotehtava :tyotehtava}
                            this-oht-rows))))
      oht-final)))

(def oht-fields
  {:osa__id                                 :osa-id
   :oh__id                                  :id
   :oh__jarjestajan_edustaja_nimi           :jarjestajan_edustaja_nimi
   :oh__jarjestajan_edustaja_rooli          :jarjestajan_edustaja_rooli
   :oh__jarjestajan_edustaja_oppilaitos_oid :jarjestajan_edustaja_oppilaitos_oid
   :oh__ajanjakson_tarkenne                 :ajanjakson_tarkenne
   :oh__hankkijan_edustaja_nimi             :hankkijan_edustaja_nimi
   :oh__hankkijan_edustaja_rooli            :hankkijan_edustaja_rooli
   :oh__hankkijan_edustaja_oppilaitos_oid   :hankkijan_edustaja_oppilaitos_oid
   :oh__alku                                :alku
   :oh__loppu                               :loppu
   :oh__module_id                           :module_id
   :oh__osa_aikaisuustieto                  :osa_aikaisuustieto
   :oh__oppisopimuksen_perusta_koodi_uri    :oppisopimuksen_perusta_koodi_uri
   :oh__oppisopimuksen_perusta_koodi_versio :oppisopimuksen_perusta_koodi_versio
   :oh__yksiloiva_tunniste                  :yksiloiva_tunniste
   :oh__osaamisen_hankkimistapa_koodi_uri   :osaamisen_hankkimistapa_koodi_uri
   :oh__osaamisen_hankkimistapa_koodi_versio
   :osaamisen_hankkimistapa_koodi_versio
   :oh__tyopaikalla_jarjestettava_koulutus_id
   :tyopaikalla_jarjestettava_koulutus_id})

(defn extract-osaamisen-hankkimistavat [rows]
  (mapv #(db-hoks/osaamisen-hankkimistapa-from-sql
           (extract-and-set-osaamisen-hankkimistapa-values % rows))
        (c/extract-from-joined-rows :oh__id oht-fields rows)))

(defn extract-hankkimistavat-and-osoittamiset [rows from-sql-func fields]
  (let [ohts (extract-osaamisen-hankkimistavat rows)
        oos (c/extract-osaamisen-osoittamiset rows)
        osa-objs (c/extract-from-joined-rows :osa__id fields rows)]
    (mapv (fn [osa]
            (assoc osa
                   :osaamisen-osoittaminen (c/process-subitems osa oos)
                   :osaamisen-hankkimistavat (c/process-subitems osa ohts)))
          (mapv from-sql-func osa-objs))))

(def hato-fields
  {:osa__id                         :id
   :osa__tutkinnon_osa_koodi_uri    :tutkinnon_osa_koodi_uri
   :osa__tutkinnon_osa_koodi_versio :tutkinnon_osa_koodi_versio
   :osa__koulutuksen_jarjestaja_oid :koulutuksen_jarjestaja_oid
   :osa__olennainen_seikka          :olennainen_seikka
   :osa__module_id                  :module_id
   :osa__vaatimuksista_tai_tavoitteista_poikkeaminen
   :vaatimuksista_tai_tavoitteista_poikkeaminen})

(defn get-hankittavat-ammat-tutkinnon-osat [hoks-id]
  (mapv #(dissoc % :id)
        (extract-hankkimistavat-and-osoittamiset
          (db/select-all-hatos-for-hoks hoks-id)
          db-hoks/hankittava-ammat-tutkinnon-osa-from-sql
          hato-fields)))

(defn get-hankittava-ammat-tutkinnon-osa [id]
  (first (extract-hankkimistavat-and-osoittamiset
           (db/select-one-hato id)
           db-hoks/hankittava-ammat-tutkinnon-osa-from-sql
           hato-fields)))

(def hpto-fields
  {:osa__id                         :id
   :osa__laajuus                    :laajuus
   :osa__nimi                       :nimi
   :osa__tavoitteet_ja_sisallot     :tavoitteet_ja_sisallot
   :osa__amosaa_tunniste            :amosaa_tunniste
   :osa__koulutuksen_jarjestaja_oid :koulutuksen_jarjestaja_oid
   :osa__olennainen_seikka          :olennainen_seikka
   :osa__module_id                  :module_id
   :osa__vaatimuksista_tai_tavoitteista_poikkeaminen
   :vaatimuksista_tai_tavoitteista_poikkeaminen})

(defn get-hankittavat-paikalliset-tutkinnon-osat [hoks-id]
  (mapv #(dissoc % :id)
        (extract-hankkimistavat-and-osoittamiset
          (db/select-all-hptos-for-hoks hoks-id)
          db-hoks/hankittava-paikallinen-tutkinnon-osa-from-sql
          hpto-fields)))

(defn get-hankittava-paikallinen-tutkinnon-osa [id]
  (first (extract-hankkimistavat-and-osoittamiset
           (db/select-one-hpto id)
           db-hoks/hankittava-paikallinen-tutkinnon-osa-from-sql
           hpto-fields)))

(def yto-osa-alue-fields
  {:osa__id                         :id
   :osa__osa_alue_koodi_uri         :osa_alue_koodi_uri
   :osa__osa_alue_koodi_versio      :osa_alue_koodi_versio
   :osa__koulutuksen_jarjestaja_oid :koulutuksen_jarjestaja_oid
   :osa__olennainen_seikka          :olennainen_seikka
   :osa__module_id                  :module_id
   :osa__vaatimuksista_tai_tavoitteista_poikkeaminen
   :vaatimuksista_tai_tavoitteista_poikkeaminen})

(defn get-yto-osa-alueet [hyto-id]
  (mapv #(dissoc % :id)
        (extract-hankkimistavat-and-osoittamiset
          (db/select-all-osa-alueet-for-yto hyto-id)
          db-hoks/yhteisen-tutkinnon-osan-osa-alue-from-sql
          yto-osa-alue-fields)))

(defn get-hankittava-yhteinen-tutkinnon-osa [hyto-id]
  (when-let [hato-db
             (db/select-hankittava-yhteinen-tutkinnon-osa-by-id hyto-id)]
    (assoc hato-db :osa-alueet (get-yto-osa-alueet hyto-id))))

(defn get-hankittavat-yhteiset-tutkinnon-osat [hoks-id]
  (mapv
    #(dissoc
       (assoc % :osa-alueet (get-yto-osa-alueet (:id %)))
       :id
       :hoks-id)
    (db/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id hoks-id)))

(defn get-osaamisen-hankkimistapa-by-id [id]
  (first (extract-osaamisen-hankkimistavat
           (db/select-osaamisen-hankkimistapa-by-id id))))

(defn get-osaamisenosoittaminen-or-hankkimistapa-of-jakolinkki [jakolinkki]
  (cond
    (= (:shared-module-tyyppi jakolinkki) "osaamisenhankkiminen")
    (extract-osaamisen-hankkimistavat
      (db/select-osaamisen-hankkimistavat-by-module-id
        (:shared-module-uuid jakolinkki)))
    (= (:shared-module-tyyppi jakolinkki) "osaamisenosoittaminen")
    (c/extract-osaamisen-osoittamiset
      (db/select-osaamisen-osoittamiset-by-module-id
        (:shared-module-uuid jakolinkki)))))

(defn save-osaamisen-hankkimistapa!
  ([oh hoks-id]
    (save-osaamisen-hankkimistapa! oh hoks-id (db-ops/get-db-connection)))
  ([oh hoks-id db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [tho (db/insert-tyopaikalla-jarjestettava-koulutus!
                  (:tyopaikalla-jarjestettava-koulutus oh) conn)
            existing (db/select-osaamisen-hankkimistavat-by-hoks-id-and-tunniste
                       hoks-id
                       (:yksiloiva-tunniste oh))
            to-upsert (assoc (if (.isBefore (LocalDate/now) (:loppu oh))
                               oh
                               (assoc oh :tep_kasitelty true))
                             :tyopaikalla-jarjestettava-koulutus-id
                             (:id tho))
            existing-id (:id (first existing))
            o-db (if (empty? existing)
                   (db/insert-osaamisen-hankkimistapa! to-upsert conn)
                   (do
                     (db/update-osaamisen-hankkimistapa! existing-id
                                                         to-upsert
                                                         conn)
                     {:id existing-id}))]
        (when (seq existing)
          (db/delete-osaamisen-hankkimistavan-muut-oppimisymparistot o-db conn)
          (db/delete-osaamisen-hankkimistavan-keskeytymisajanjaksot o-db conn))
        (db/insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
          o-db (:muut-oppimisymparistot oh) conn)
        (db/insert-osaamisen-hankkimistavan-keskeytymisajanjaksot!
          o-db (:keskeytymisajanjaksot oh) conn)
        o-db))))

(defn save-hpto-osaamisen-hankkimistapa!
  ([hpto oh]
    (save-hpto-osaamisen-hankkimistapa! hpto oh (db-ops/get-db-connection)))
  ([hpto oh db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [o-db (save-osaamisen-hankkimistapa! oh (:hoks_id hpto) conn)]
        (db/insert-hpto-osaamisen-hankkimistapa!
          hpto o-db conn)
        o-db))))

(defn save-hpto-osaamisen-hankkimistavat!
  ([hpto c]
    (save-hpto-osaamisen-hankkimistavat! hpto c (db-ops/get-db-connection)))
  ([hpto c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-hpto-osaamisen-hankkimistapa! hpto % conn) c))))

(defn- replace-hpto-osaamisen-hankkimistavat! [hpto c db-conn]
  (db/delete-osaamisen-hankkimistavat-by-hpto-id! (:id hpto) db-conn)
  (save-hpto-osaamisen-hankkimistavat! hpto c db-conn))

(defn save-hpto-osaamisen-osoittaminen!
  ([hpto n]
    (save-hpto-osaamisen-osoittaminen! hpto n (db-ops/get-db-connection)))
  ([hpto n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [naytto (c/save-osaamisen-osoittaminen! n conn)]
        (db/insert-hpto-osaamisen-osoittaminen! hpto naytto conn)
        naytto))))

(defn save-hpto-osaamisen-osoittamiset!
  ([ppto c]
    (save-hpto-osaamisen-osoittamiset! ppto c (db-ops/get-db-connection)))
  ([ppto c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-hpto-osaamisen-osoittaminen! ppto % conn)
        c))))

(defn- replace-hpto-osaamisen-osoittamiset! [hpto c db-conn]
  (db/delete-osaamisen-osoittamiset-by-ppto-id! (:id hpto) db-conn)
  (save-hpto-osaamisen-osoittamiset! hpto c db-conn))

(defn update-hankittava-paikallinen-tutkinnon-osa! [hpto-db values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-hankittava-paikallinen-tutkinnon-osa-by-id!
      (:id hpto-db) values db-conn)
    (cond-> hpto-db
      (:osaamisen-hankkimistavat values)
      (assoc :osaamisen-hankkimistavat
             (replace-hpto-osaamisen-hankkimistavat!
               hpto-db (:osaamisen-hankkimistavat values) db-conn))
      (:osaamisen-osoittaminen values)
      (assoc :osaamisen-osoittaminen
             (replace-hpto-osaamisen-osoittamiset!
               hpto-db (:osaamisen-osoittaminen values) db-conn)))))

(defn save-yto-osa-alueen-osaamisen-osoittaminen!
  ([yto n]
    (save-yto-osa-alueen-osaamisen-osoittaminen!
      yto n (db-ops/get-db-connection)))
  ([yto n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [naytto (c/save-osaamisen-osoittaminen! n conn)
            yto-naytto (db/insert-yto-osa-alueen-osaamisen-osoittaminen!
                         (:id yto) (:id naytto) conn)]
        yto-naytto))))

(defn save-hankittava-paikallinen-tutkinnon-osa!
  ([hoks-id hpto]
    (save-hankittava-paikallinen-tutkinnon-osa!
      hoks-id hpto (db-ops/get-db-connection)))
  ([hoks-id hpto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [hpto-db (db/insert-hankittava-paikallinen-tutkinnon-osa!
                      (assoc hpto :hoks-id hoks-id) conn)]
        (assoc
          hpto-db
          :osaamisen-hankkimistavat
          (save-hpto-osaamisen-hankkimistavat!
            hpto-db (:osaamisen-hankkimistavat hpto) conn)
          :osaamisen-osoittaminen
          (save-hpto-osaamisen-osoittamiset!
            hpto-db (:osaamisen-osoittaminen hpto) conn))))))

(defn save-hankittavat-paikalliset-tutkinnon-osat!
  ([hoks-id c]
    (save-hankittavat-paikalliset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-hankittava-paikallinen-tutkinnon-osa! hoks-id % conn) c))))

(defn save-hato-osaamisen-hankkimistapa!
  ([hato oh]
    (save-hato-osaamisen-hankkimistapa! hato oh (db-ops/get-db-connection)))
  ([hato oh db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [o-db (save-osaamisen-hankkimistapa! oh (:hoks_id hato) conn)]
        (db/insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
          (:id hato) (:id o-db) conn)
        o-db))))

(defn save-hato-osaamisen-hankkimistavat! [hato-db c db-conn]
  (mapv
    #(save-hato-osaamisen-hankkimistapa! hato-db % db-conn)
    c))

(defn save-hato-osaamisen-osoittaminen!
  ([hato n]
    (save-hato-osaamisen-osoittaminen!
      hato n (db-ops/get-db-connection)))
  ([hato n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [naytto (c/save-osaamisen-osoittaminen! n conn)]
        (db/insert-hato-osaamisen-osoittaminen! (:id hato) (:id naytto) conn)
        naytto))))

(defn- save-hato-osaamisen-osoittamiset! [hato-db c db-conn]
  (mapv
    #(save-hato-osaamisen-osoittaminen! hato-db % db-conn)
    c))

(defn save-hankittava-ammat-tutkinnon-osa!
  ([hoks-id hato]
    (save-hankittava-ammat-tutkinnon-osa!
      hoks-id hato (db-ops/get-db-connection)))
  ([hoks-id hato db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [hato-db (db/insert-hankittava-ammat-tutkinnon-osa!
                      (assoc hato :hoks-id hoks-id) conn)]
        (assoc
          hato-db
          :osaamisen-osoittaminen
          (mapv
            #(save-hato-osaamisen-osoittaminen! hato-db % conn)
            (:osaamisen-osoittaminen hato))
          :osaamisen-hankkimistavat
          (mapv
            #(save-hato-osaamisen-hankkimistapa! hato-db % conn)
            (:osaamisen-hankkimistavat hato)))))))

(defn save-hankittavat-ammat-tutkinnon-osat!
  ([hoks-id c]
    (save-hankittavat-ammat-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv #(save-hankittava-ammat-tutkinnon-osa! hoks-id % conn) c))))

(defn- replace-hato-osaamisen-hankkimistavat! [hato c db-conn]
  (db/delete-osaamisen-hankkimistavat-by-hato-id! (:id hato) db-conn)
  (save-hato-osaamisen-hankkimistavat! hato c db-conn))

(defn- replace-hato-osaamisen-osoittamiset! [hato c db-conn]
  (db/delete-osaamisen-osoittamiset-by-pato-id! (:id hato) db-conn)
  (save-hato-osaamisen-osoittamiset! hato c db-conn))

(defn update-hankittava-ammat-tutkinnon-osa! [hato-db values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db/update-hankittava-ammat-tutkinnon-osa-by-id!
      (:id hato-db) values db-conn)
    (cond-> hato-db
      (:osaamisen-hankkimistavat values)
      (assoc :osaamisen-hankkimistavat
             (replace-hato-osaamisen-hankkimistavat!
               hato-db (:osaamisen-hankkimistavat values) db-conn))
      (:osaamisen-osoittaminen values)
      (assoc :osaamisen-osoittaminen
             (replace-hato-osaamisen-osoittamiset!
               hato-db (:osaamisen-osoittaminen values) db-conn)))))

(defn save-hyto-osa-alue-osaamisen-hankkimistapa!
  ([hoks-id hyto-osa-alue oh]
    (save-hyto-osa-alue-osaamisen-hankkimistapa!
      hoks-id hyto-osa-alue oh (db-ops/get-db-connection)))
  ([hoks-id hyto-osa-alue oh db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [o-db (save-osaamisen-hankkimistapa! oh hoks-id conn)]
        (db/insert-hyto-osa-alueen-osaamisen-hankkimistapa!
          (:id hyto-osa-alue) (:id o-db) conn)
        o-db))))

(defn save-hyto-osa-alueet!
  ([hoks-id hyto-id osa-alueet]
    (save-hyto-osa-alueet!
      hoks-id hyto-id osa-alueet (db-ops/get-db-connection)))
  ([hoks-id hyto-id osa-alueet db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [osa-alue-db (db/insert-yhteisen-tutkinnon-osan-osa-alue!
                             (assoc % :yhteinen-tutkinnon-osa-id hyto-id) conn)]
           (assoc
             osa-alue-db
             :osaamisen-hankkimistavat
             (mapv
               (fn [oht]
                 (save-hyto-osa-alue-osaamisen-hankkimistapa!
                   hoks-id osa-alue-db oht conn))
               (:osaamisen-hankkimistavat %))
             :osaamisen-osoittaminen
             (mapv
               (fn [hon]
                 (save-yto-osa-alueen-osaamisen-osoittaminen!
                   osa-alue-db hon conn))
               (:osaamisen-osoittaminen %))))
        osa-alueet))))

(defn save-hankittava-yhteinen-tutkinnon-osa!
  ([hoks-id hyto]
    (save-hankittava-yhteinen-tutkinnon-osa!
      hoks-id hyto (db-ops/get-db-connection)))
  ([hoks-id hyto db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [hyto-db (db/insert-hankittava-yhteinen-tutkinnon-osa!
                      (assoc hyto :hoks-id hoks-id) conn)]
        (assoc
          hyto-db
          :osa-alueet
          (save-hyto-osa-alueet!
            hoks-id (:id hyto-db) (:osa-alueet hyto) conn))))))

(defn save-hankittavat-yhteiset-tutkinnon-osat!
  ([hoks-id c]
    (save-hankittavat-yhteiset-tutkinnon-osat!
      hoks-id c (db-ops/get-db-connection)))
  ([hoks-id c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(save-hankittava-yhteinen-tutkinnon-osa! hoks-id % conn)
        c))))

(defn- replace-hyto-osa-alueet! [hoks-id hyto-id new-oa-values db-conn]
  (db/delete-hyto-osa-alueet! hyto-id db-conn)
  (save-hyto-osa-alueet! hoks-id hyto-id new-oa-values db-conn))

(defn update-hankittava-yhteinen-tutkinnon-osa! [hoks-id hyto-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (let [bare-hyto (dissoc new-values :osa-alueet)]
      (when (not-empty bare-hyto)
        (db/update-hankittava-yhteinen-tutkinnon-osa-by-id!
          hyto-id new-values db-conn)))
    (when-let [oa (:osa-alueet new-values)]
      (replace-hyto-osa-alueet! hoks-id hyto-id oa db-conn))))
