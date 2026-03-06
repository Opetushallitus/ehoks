(ns oph.ehoks.external.viestinvalityspalvelu-test
  (:require [clojure.test :refer [deftest is testing]]
            [oph.ehoks.test-utils :as util]
            [clojure.string :as s]
            [oph.ehoks.external.viestinvalityspalvelu :as vvp]))

(deftest login-uses-cas-for-session
  (testing "call login and see that CAS session is created"
    (let [req (atom nil)]
      (util/with-ticket-auth
        ["1.2.246.562.10.22222222220"
         (fn [_ url options]
           (when (s/ends-with? url "login/j_spring_cas_security_check")
             (reset! req options)
             {:status 302 :body ""}))]
        (vvp/login!)
        (is (= "ST-1234-testi" (get-in @req [:query-params :ticket])))))))

(deftest test-send-message!
  (testing "send-message! calls viestinvalityspalvelu"
    (let [req (atom nil)]
      (util/with-ticket-auth
        ["1.2.246.562.10.22222222220"
         (fn [_ url options]
           (when (s/ends-with? url "lahetys/v1/viestit")
             (reset! req options)
             {:status 200
              :body {:viestiTunniste "019cb395-5840-70fa-96c9-918eec8a6f42"
                     :lahetysTunniste
                     "019cb395-5840-70fa-96c9-918eec8a6f42"}}))]
        (is (= (vvp/send-message! "vastaan.ottaja@oph.fi" "otsikko" "sisalto")
               "019cb395-5840-70fa-96c9-918eec8a6f42"))
        (is (s/includes?
              (:body @req)
              (str "\"vastaanottajat\":[{\"sahkopostiOsoite\":"
                   "\"vastaan.ottaja@oph.fi\"}")))))))

(deftest test-message-state!
  (testing "message-state! calls viestinvalityspalvelu"
    (util/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ url __]
         (when (s/ends-with? url "lahetys/v1/lahetykset/foo/vastaanottajat")
           {:status 200
            :body {:vastaanottajat
                   [{:tunniste "foo"
                     :sahkoposti "vastaan.ottaja@oph.fi"
                     :viestiTunniste "foo"
                     :tila "LAHETETTY"}]}}))]
      (is (= (vvp/message-state! "foo") ["LAHETETTY"])))))
