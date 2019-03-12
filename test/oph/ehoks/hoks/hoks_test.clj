(ns oph.ehoks.hoks.hoks-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.db.migrations :as m]))


(defn with-database [f]
  (f)
  ;(m/clean!)
  ;(m/migrate!)
  )

(use-fixtures :each with-database)

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
                     :loppu (java.time.LocalDate/of 2019 3 13)}
                    ])]

      (comment          {:jarjestaja-oppilaitos-oid "1.2.246.562.10.54453921331"
                         :osa-alue-koodi-uri "ammatillisenoppiaineet_ai"
                         :osa-alue-koodi-versio 3
                         :nayttoymparisto-id (:id nayttoymparisto)
                         :alku (java.time.LocalDate/of 2019 3 19)
                         :loppu (java.time.LocalDate/of 2019 3 19)})
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
           ;:osaamisen-hankkimistavat []
           :hankitun-osaamisen-naytto
           [{:jarjestaja-oppilaitos-oid "1.2.246.562.10.54453921330"
             :id (:id (first naytot))
             :osa-alue-koodi-uri "ammatillisenoppiaineet_fk"
             :osa-alue-koodi-versio 2
             :nayttoymparisto-id (:id nayttoymparisto)
             :alku (java.time.LocalDate/of 2019 3 11)
             :loppu (java.time.LocalDate/of 2019 3 13)}]}]}))))
