(ns oph.ehoks.hoks.hoks-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.db.migrations :as m]))


(defn with-database [f]
  (f)
  (m/clean!)
  (m/migrate!))

(defn create-db [f]
  (m/migrate!)
  (f)
  (m/clean!))

(use-fixtures :each with-database)

(use-fixtures :once create-db)

(deftest set-puuttuvat-paikalliset-tutkinnon-osat-test
  (testing "Set HOKS puuttuvat paikalliset tutkinnon osat"
    (let [hoks (first (db/insert-hoks! {}))
          ppto-col (db/insert-puuttuvat-paikalliset-tutkinnon-osat!
                     [{:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
                       :tavoitteet-ja-sisallot "Testitavoite"
                       :nimi "Orientaatio alaan"
                       :hoks-id (:id hoks)}])
          nayttoymparisto (first (db/insert-nayttoymparisto!
                                   {:nimi "Testiympäristö"
                                    :y-tunnus "12345678-1"
                                    :kuvaus "Test"}))
          naytot (db/insert-ppto-hankitun-osaamisen-naytot!
                   (first ppto-col)
                   [{:jarjestaja-oppilaitos-oid "1.2.246.562.10.54453921330"
                     :osa-alue-koodi-uri "ammatillisenoppiaineet_fk"
                     :osa-alue-koodi-versio 2
                     :nayttoymparisto-id (:id nayttoymparisto)
                     :alku (java.time.LocalDate/of 2019 3 11)
                     :loppu (java.time.LocalDate/of 2019 3 13)}])]
      (db/insert-hankitun-osaamisen-nayton-koulutuksen-jarjestaja-arvioijat!
        (first naytot)
        [{:nimi "Terttu Testaaja"
          :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921332"}}])

      (db/insert-hankitun-osaamisen-nayton-tyoelama-arvioijat!
        (first naytot)
        [{:nimi "Teppo Työmies"
          :organisaatio {:nimi "Kallen Paja Ky"
                         :y-tunnus "12345679-2"}}])

      (db/insert-hankitun-osaamisen-nayton-tyotehtavat!
        (first naytot)
        ["Renkaanvaihto"
         "Tuulilasin vaihto"])
      (let [ppto-oht
            (db/insert-ppto-osaamisen-hankkimistavat!
              (first ppto-col)
              [{:jarjestajan-edustaja
                {:nimi "Erkki Edustaja"
                 :rooli "Valvoja"
                 :oppilaitos-oid "1.2.246.562.10.54453921340"}
                :osaamisen-hankkimistapa-koodi-uri
                "osaamisenhankkimistapa_oppisopimus"
                :osaamisen-hankkimistapa-koodi-versio 1
                :tyopaikalla-hankittava-osaaminen
                {:vastuullinen-ohjaaja {:nimi "Olli Ohjaaja"
                                        :sahkoposti "olli.ohjaaja@esimerkki.com"}
                 :tyopaikan-nimi "Ohjaus Oy"
                 :tyopaikan-y-tunnus "12345689-4"
                 :muut-osallistujat [{:organisaatio {:nimi "Esimerkki Oy"
                                                     :y-tunnus "12345688-5"}
                                      :nimi "Kiira Kirjaaja"
                                      :rooli "Avustaja"}]
                 :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                         "Vuoronvaihdon tarkistukset"]
                 :lisatiedot false}
                :muut-oppimisymparisto
                [{:oppimisymparisto-koodi-uri "oppimisymparistot_0001"
                  :oppimisymparisto-koodi-versio 1
                  :selite "Testioppilaitos"
                  :lisatiedot false}]
                :ajanjakson-tarkenne "Ei tarkennettavaa"
                :hankkijan-edustaja {:nimi "Harri Hankkija"
                                     :rooli "Opettaja"
                                     :oppilaitos-oid "1.2.246.562.10.54453921350"}
                :alku (java.time.LocalDate/of 2019 2 10)
                :loppu (java.time.LocalDate/of 2019 2 15)}])])
      (eq
        (dissoc
          (h/set-puuttuvat-paikalliset-tutkinnon-osat
            (first (db/select-hoks-by-id (:id hoks))))
          :updated-at :created-at :eid :version :id)
        {:puuttuvat-paikalliset-tutkinnon-osat
         [{:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
           :tavoitteet-ja-sisallot "Testitavoite"
           :nimi "Orientaatio alaan"
           :id (:id (first ppto-col))
           :osaamisen-hankkimistavat
           [{:jarjestajan-edustaja
             {:nimi "Erkki Edustaja"
              :rooli "Valvoja"
              :oppilaitos-oid "1.2.246.562.10.54453921340"}
             :osaamisen-hankkimistapa-koodi-uri
             "osaamisenhankkimistapa_oppisopimus"
             :osaamisen-hankkimistapa-koodi-versio 1
             :muut-oppimisymparisto
             [{:oppimisymparisto-koodi-uri "oppimisymparistot_0001"
               :oppimisymparisto-koodi-versio 1
               :selite "Testioppilaitos"
               :lisatiedot false}]
             :ajanjakson-tarkenne "Ei tarkennettavaa"
             :tyopaikalla-hankittava-osaaminen
             {:vastuullinen-ohjaaja {:nimi "Olli Ohjaaja"
                                     :sahkoposti "olli.ohjaaja@esimerkki.com"}
              :tyopaikan-nimi "Ohjaus Oy"
              :tyopaikan-y-tunnus "12345689-4"
              :muut-osallistujat [{:organisaatio {:nimi "Esimerkki Oy"
                                                  :y-tunnus "12345688-5"}
                                   :nimi "Kiira Kirjaaja"
                                   :rooli "Avustaja"}]
              :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                      "Vuoronvaihdon tarkistukset"]
              :lisatiedot false}
             :hankkijan-edustaja {:nimi "Harri Hankkija"
                                  :rooli "Opettaja"
                                  :oppilaitos-oid "1.2.246.562.10.54453921350"}
             :alku (java.time.LocalDate/of 2019 2 10)
             :loppu (java.time.LocalDate/of 2019 2 15)}]
           :hankitun-osaamisen-naytto
           [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921330"}
             :nayttoymparisto {:nimi "Testiympäristö"
                               :y-tunnus "12345678-1"
                               :kuvaus "Test"}
             :keskeiset-tyotehtavat-naytto ["Renkaanvaihto"
                                            "Tuulilasin vaihto"]
             :koulutuksen-jarjestaja-arvioijat
             [{:nimi "Terttu Testaaja"
               :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921332"}}]
             :tyoelama-arvioijat [{:nimi "Teppo Työmies"
                                   :organisaatio {:nimi "Kallen Paja Ky"
                                                  :y-tunnus "12345679-2"}}]
             :id (:id (first naytot))
             :osa-alue-koodi-uri "ammatillisenoppiaineet_fk"
             :osa-alue-koodi-versio 2
             :alku (java.time.LocalDate/of 2019 3 11)
             :loppu (java.time.LocalDate/of 2019 3 13)}]}]}))))
