(ns oph.ehoks.hoks.common
  (:require [oph.ehoks.db.postgresql.common :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [clojure.java.jdbc :as jdbc]))

(defn- save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
  "Tallentaa osaamisen osoittamisen työelämän osaamisen arvioijat tietokantaan."
  ([naytto arvioijat]
    (save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
      naytto arvioijat (db-ops/get-db-connection)))
  ([naytto arvioijat db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [arvioija (db/insert-tyoelama-arvioija! % conn)]
           (db/insert-osaamisen-osoittamisen-tyoelama-arvioija!
             naytto arvioija conn)
           arvioija)
        arvioijat))))

(defn- save-osaamisen-osoittamisen-osa-alueet!
  "Tallentaa osaamisen osoittamisen osa-alueet tietokantaan."
  ([n c]
    (save-osaamisen-osoittamisen-osa-alueet! n c (db-ops/get-db-connection)))
  ([n c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [k (db/insert-koodisto-koodi! % conn)]
           (db/insert-osaamisen-osoittamisen-osa-alue! (:id n) (:id k) conn)
           k)
        c))))

(defn save-osaamisen-osoittaminen!
  "Tallentaa yhden osaamisen osoittamisen tietokantaan."
  ([n]
    (save-osaamisen-osoittaminen! n (db-ops/get-db-connection)))
  ([n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [nayttoymparisto (db/insert-nayttoymparisto!
                              (:nayttoymparisto n) conn)
            naytto (db/insert-osaamisen-osoittaminen!
                     (assoc n :nayttoymparisto-id (:id nayttoymparisto))
                     conn)]
        (db/insert-oo-koulutuksen-jarjestaja-osaamisen-arvioija!
          naytto (:koulutuksen-jarjestaja-osaamisen-arvioijat n) conn)
        (save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
          naytto (:tyoelama-osaamisen-arvioijat n) conn)
        (db/insert-osaamisen-osoittamisen-sisallot!
          naytto (:sisallon-kuvaus n) conn)
        (db/insert-osaamisen-osoittamisen-yksilolliset-kriteerit!
          naytto (:yksilolliset-kriteerit n) conn)
        (save-osaamisen-osoittamisen-osa-alueet!
          naytto (:osa-alueet n) conn)
        naytto))))

(defn get-map
  "Get-funktion erikoisversio. Jos avain on sekvenssi, hakee sen jokaisen
  jäsenen coll:ista ja laittaa ne uuteen vectoriin vastaavassa järjestyksessä.
  Muuten hakee avaimen arvon coll:ista normaalilla tavalla."
  [coll k]
  (if (sequential? k)
    (vec (map #(get coll %) k))
    (get coll k)))

(defn extract-from-joined-rows
  "Hakee sekvenssistä yhdeistetyistä riveistä ne tietueet, jotka ovat uniikkeja
  unique-on -argumentin perusteella, ottaen mukaan vain ne kentät, jotka on
  annettu fields -argumentissa. Kenttien nimet on muokattu fields -argumentissa
  olevien korrespondenssien perusteella, eli jos alkuperäisessä rivissä olevan
  avain on fields -argumentissa avaimena, se korvataan vastaavalla arvolla;
  muuten avain/arvo -paria ei oteta mukaan."
  [unique-on fields rows]
  (mapv
    ; Hakee rivistä fieldsissä designoidut kentät ja lisää ne uuteen objektiin
    ; uusilla nimillä. Jos fieldsissä on kv-pari `x: y` ja x on rowssa avaimena,
    ; x:in arvo rowsta lisätään uuteen objektiin avaimella y:llä.
    (fn [row] (reduce-kv #(assoc %1 %3 (get row %2)) {} fields))
    (sort #(compare (get-map %1 unique-on) (get-map %2 unique-on))
          ; Deduplikoi rivit, ja siivoa pois ne, joista unique-on -arvo ei löydy
          (vals
            (dissoc (reduce #(assoc %1 (get-map %2 unique-on) %2) {} rows)
                    nil
                    [nil nil])))))

(defn extract-and-set-osaamisen-osoittaminen-values
  "Irrottaa tietyn osaamisen osoittamisen arvot tietokannasta haetuista
  riveistä."
  [oo rows]
  (let [this-oo-rows (filterv #(= (:oo__id %) (:id oo)) rows)
        kj-arvioijat
        (mapv db-hoks/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql
              (extract-from-joined-rows
                :kjoa__id
                {:kjoa__id             :id
                 :kjoa__nimi           :nimi
                 :kjoa__oppilaitos_oid :oppilaitos_oid}
                this-oo-rows))
        te-arvioijat (mapv
                       db-hoks/tyoelama-arvioija-from-sql
                       (extract-from-joined-rows
                         :toa__id
                         {:toa__id                    :id
                          :toa__nimi                  :nimi
                          :toa__organisaatio_nimi     :organisaatio_nimi
                          :toa__organisaatio_y_tunnus :organisaatio_y_tunnus}
                         this-oo-rows))
        nayttoymparisto (db-hoks/nayttoymparisto-from-sql
                          (first (extract-from-joined-rows
                                   :ny__id
                                   {:ny__nimi     :nimi
                                    :ny__y_tunnus :y_tunnus
                                    :ny__kuvaus   :kuvaus}
                                   this-oo-rows)))
        sisallon-kuvaus (mapv :sisallon_kuvaus
                              (extract-from-joined-rows
                                :oos__id
                                {:oos__sisallon_kuvaus :sisallon_kuvaus}
                                this-oo-rows))
        osa-alueet (extract-from-joined-rows
                     [:oooa__osaamisen_osoittaminen_id :oooa__kooisto_koodi_id]
                     {:kk__koodi_uri    :koodi-uri
                      :kk__koodi_versio :koodi-versio}
                     this-oo-rows)
        kriteerit (mapv :kriteeri
                        (extract-from-joined-rows
                          :ooyk__id
                          {:ooyk__yksilollinen_kriteeri :kriteeri}
                          this-oo-rows))]
    (assoc oo
           :koulutuksen-jarjestaja-osaamisen-arvioijat kj-arvioijat
           :tyoelama-osaamisen-arvioijat               te-arvioijat
           :nayttoymparisto                            nayttoymparisto
           :sisallon-kuvaus                            sisallon-kuvaus
           :osa-alueet                                 osa-alueet
           :yksilolliset-kriteerit                     kriteerit)))

(def oo-fields
  "Kentät, jotka irrotetaan tietokannasta haetuista riveistä osaamisen
  osoittamisen perustiedoiksi."
  {:osa__id                       :osa-id
   :oo__id                        :id
   :oo__jarjestaja_oppilaitos_oid :jarjestaja_oppilaitos_oid
   :oo__alku                      :alku
   :oo__loppu                     :loppu
   :oo__module_id                 :module_id
   :oo__vaatimuksista_tai_tavoitteista_poikkeaminen
   :vaatimuksista_tai_tavoitteista_poikkeaminen})

(defn extract-osaamisen-osoittamiset
  "Hakee kaikki osaamisen osoittamiset yhdistetyistä riveistä."
  [rows]
  (mapv #(db-hoks/osaamisen-osoittaminen-from-sql
           (extract-and-set-osaamisen-osoittaminen-values % rows))
        (extract-from-joined-rows :oo__id oo-fields rows)))

(defn process-subitems
  "Hakee ne osaamisen hankkimistavat tai osaamisen osoittamiset, jotka kuuluvat
  annettuun tutkinnon osaan."
  [osa rows]
  (mapv #(dissoc % :osa-id :id) (filterv #(= (:osa-id %) (:id osa)) rows)))
