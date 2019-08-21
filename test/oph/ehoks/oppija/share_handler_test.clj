(ns oph.ehoks.oppija.share-handler-test
  (:require [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.hoks.hoks :as h]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]]))

(t/use-fixtures :each utils/with-database)

(t/use-fixtures :once utils/clean-db)

(def url "/ehoks-oppija-backend/api/v1/oppija/hoksit")

(def hoks-data
  {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
   :oppija-oid "1.2.246.562.24.12312312312"
   :ensikertainen-hyvaksyminen
   (java.time.LocalDate/of 2019 3 18)
   :osaamisen-hankkimisen-tarve false
   :sahkoposti "erkki.esimerkki@esimerkki.com"
   :hankittavat-yhteiset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_121123"
     :tutkinnon-osa-koodi-versio 3
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ke"
       :osa-alue-koodi-versio 4
       :osaamisen-hankkimistavat
       [{:osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
         :osaamisen-hankkimistapa-koodi-versio 2
         :alku (java.time.LocalDate/of 2019 1 13)
         :loppu (java.time.LocalDate/of 2019 2 19)
         :muut-oppimisymparistot [{:oppimisymparisto-koodi-uri
                                   "oppimisymparistot_0001"
                                   :oppimisymparisto-koodi-versio 1
                                   :alku (java.time.LocalDate/of 2019 1 13)
                                   :loppu (java.time.LocalDate/of 2019 2 19)}]}]
       :osaamisen-osoittaminen
       [{:nayttoymparisto {:nimi "Testiympäristö"}
         :sisallon-kuvaus ["Testikuvaus"]
         :alku (java.time.LocalDate/of 2019 1 13)
         :loppu (java.time.LocalDate/of 2019 2 19)
         :osa-alueet []
         :tyoelama-osaamisen-arvioijat []
         :koulutuksen-jarjestaja-osaamisen-arvioijat []
         :yksilolliset-kriteerit []}]}]}]})

(t/deftest get-shared-link
  (t/testing "GET shared link"
    (let [share-url (format
                      "%s/%s/share/%s"
                      url
                      (:eid (h/save-hoks! hoks-data))
                      "tutkinnonosat_121123")
          store (atom {})
          responses
          (utils/with-authenticated-oid
            store
            (:oppija-oid hoks-data)
            (common-api/create-app
              handler/app-routes (test-session-store store))
            (mock/json-body
              (mock/request
                :post
                share-url)
              {:voimassaolo-alku (str (java.time.LocalDate/now))
               :voimassaolo-loppu (str (str (java.time.LocalDate/now)))
               :tyyppi ""})
            (mock/request
              :get
              share-url))
          body (utils/parse-body (:body (first responses)))]
      (t/is (= (:status (first responses)) 200))
      (t/is (= (:status (second responses)) 200))
      (t/is (-> (:body (second responses))
                utils/parse-body
                :data
                first
                :uuid)
            (get-in body [:meta :uuid])))))