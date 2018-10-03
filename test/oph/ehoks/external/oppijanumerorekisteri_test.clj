(ns oph.ehoks.external.oppijanumerorekisteri-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [clj-time.core :as t]))

(defn with-service-ticket [f & args]
  (with-redefs [clj-http.client/post (fn [_ __] {:body "test-ticket"})
                clj-http.client/get (fn [_ __]
                                      {:body "{\"data\": [{\"cn\": \"Testi\"}]}"
                                       :status 200})]
    (reset! c/service-ticket {:url "http://ticket.url"
                              :expires (t/plus (t/now) (t/hours 2))})
    (apply f args)))

(deftest test-find-student-by-nat-id
  (testing "Finding student by national ID returns unmangled data"
    (let [response (with-service-ticket
                     onr/find-student-by-nat-id "010101-1100")]
      (is (= (:body response) {:data [{:cn "Testi"}]}))
      (is (= (:status response) 200)))))

(deftest test-find-student-by-oid
  (testing "Finding student by oid returns unmangled data"
    (let [response (with-service-ticket
                     onr/find-student-by-nat-id "1234.4567.89")]
      (is (= (:body response) {:data [{:cn "Testi"}]}))
      (is (= (:status response) 200)))))

(deftest test-convert-student-info
  (testing "Converting Oppijanumerorekisteri student info"
    (is (= (onr/convert-student-info
             {})
           {}))
    (is (= (onr/convert-student-info
             {:oidHenkilo "1.2.246.562.24.78058065184"
              :hetu "190384-9245"
              :etunimet "Teuvo Taavetti"
              :kutsumanimi "Teuvo"
              :sukunimi "Testaaja"
              :yhteystiedotRyhma
              '({:id 0,
                 :readOnly true,
                 :ryhmaAlkuperaTieto "testiservice",
                 :ryhmaKuvaus "testiryhmä",
                 :yhteystieto
                 [{:yhteystietoArvo "kayttaja@domain.local",
                   :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})})
           {:oid "1.2.246.562.24.78058065184"
            :first-name "Teuvo Taavetti"
            :surname "Testaaja"
            :common-name "Teuvo"
            :contact-values-group
            '({:id 0
               :contact [{:value "kayttaja@domain.local"
                          :type "YHTEYSTIETO_SAHKOPOSTI"}]})}))))
