(ns oph.ehoks.oppija.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [create-app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.session-store :refer [test-session-store]]
            [clj-time.core :as t]))

(def url "/ehoks-backend/api/v1/oppijat")

(def hoks
  {:urasuunnitelma {:koodi-arvo "jatkokoulutus"
                    :koodi-uri "urasuunnitelma"
                    :versio 0}
   :opiskeluoikeus
   {:oid "1.2.444.333.55.76666666666"
    :tutkinto {:laajuus 35
               :nimi "Audiovisuaalisen sisällön tuottamisen perustutkinto"}}
   :oppijan-oid "1.2.333.444.55.76666666666"
   :luotu (java.util.Date.)
   :luonut "Olli Opettaja"
   :hyvaksytty (java.util.Date.)
   :hyvaksynyt "Heikki Hyväksyjä"
   :paivitetty (java.util.Date.)
   :paivittanyt "Päivi Päivittäjä"
   :versio 2
   :eid 1})

(deftest get-oppijan-hoks
  (testing "GET oppijan HOKSit"
    (reset! db/hoks-store [hoks])
    (let [store (atom {})
          app (create-app (test-session-store store))
          response
          (utils/with-authenticated-oid
            store
            (:oppijan-oid hoks)
            app
            (mock/request
              :get
              (format "%s/%s/hoks" url (:oppijan-oid hoks))))]
      (is (= (:status response) 200))
      (let [body (utils/parse-body (:body response))]
        (eq
          (update-in
            body
            [:data 0]
            dissoc :luotu :paivitetty :hyvaksytty)
          {:data [(dissoc hoks :luotu :paivitetty :hyvaksytty)]
           :meta {}})))))
