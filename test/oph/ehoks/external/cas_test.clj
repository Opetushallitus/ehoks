(ns oph.ehoks.external.cas-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.string :as s]
            [oph.ehoks.external.cas :as c]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.http-client :as client]
            [clj-time.core :as t]
            [clojure.data.xml :as xml]))

(defn with-reset-cas [f]
  (f)
  (client/reset-functions!)
  (reset! c/grant-ticket {:url nil :expires nil}))

(use-fixtures :each with-reset-cas)

(def example-responses
  {"https://some.url/"
   {:status 200
    :body {}
    :timestamp (t/now)}
   "https://someother.url/"
   {:status 200
    :body {}
    :timestamp
    (t/minus
      (t/now)
      (t/minutes
        (inc (:ext-cache-lifetime-minutes config))))}})

(deftest test-refresh-grant-ticket
  (testing "Refresh grant ticket successfully"
    (reset! c/grant-ticket {:url nil :expires nil})
    (is (= (deref c/grant-ticket) {:url nil :expires nil}))
    (client/set-post!
      (fn [_ options]
        (is (= (get-in options [:form-params :username])
               (:cas-username config)))
        (is (= (get-in options [:form-params :password])
               (:cas-password config)))
        {:status 201
         :headers {"location" "test-url"}}))
    (c/refresh-grant-ticket!)
    (is (= (:url (deref c/grant-ticket)) "test-url"))
    (is (some? (:expires (deref c/grant-ticket)))))

  (testing "Refresh grant ticket unsuccessfully (404)"
    (reset! c/grant-ticket {:url nil :expires nil})
    (client/set-post! (fn [_ options]
                        (throw (ex-info "HTTP Exception" {:status 404}))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"HTTP Exception"
                          (c/refresh-grant-ticket!))))
  (testing "Refresh grant ticket unsuccessfully (missing location header)"
    (reset! c/grant-ticket {:url nil :expires nil})
    (client/set-post! (fn [_ options]
                        {:status 201}))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Failed to refresh CAS Service Ticket"
                          (c/refresh-grant-ticket!)))))

(deftest test-get-service-ticket
  (testing "Get service ticket"
    (client/set-post!
      (fn [_ options]
        (is (= (get-in options [:form-params :service])
               "http://test-service/j_spring_cas_security_check"))
        {:body "test-ticket"}))
    (is (= (c/get-service-ticket "http://url" "http://test-service")
           "test-ticket"))))

(deftest test-add-cas-ticket
  (testing "Add service ticket successfully"
    (client/set-post! (fn [_ options] {:body "test-ticket"}))

    (reset! c/grant-ticket {:url "http://ticket.url"
                            :expires (t/plus (t/now) (t/hours 2))})
    (let [data (c/add-cas-ticket {} "http://test-service")]
      (is (= (get-in data [:headers "accept"]) "*/*"))
      (is (= (get-in data [:query-params :ticket]) "test-ticket"))))
  (testing "Add service ticket retries when get ST returns 404 once"
    (let [get-st-returns-404 (atom true)
          get-st-call-count (atom 0)]
      (client/set-post!
        (fn [^String url _]
          (cond
            ; refresh ticket granting ticket
            (.endsWith url "/v1/tickets")
            {:status 201 :headers {"location" "http://ticket.url"}}
            ; get service ticket returns 404 once
            (= url "http://ticket.url")
            (do
              (swap! get-st-call-count inc)
              (if @get-st-returns-404
                (do
                  (reset! get-st-returns-404 false)
                  (throw
                    (ex-info
                      "Test HTTP Exception"
                      {:body
                       "TGT-nnn could not be found or is considered invalid"
                       :status 404})))
                {:status 201 :body "test-ticket"})))))
      (reset! c/grant-ticket {:url "http://ticket.url"
                              :expires (t/plus (t/now) (t/hours 2))})
      (let [data (c/add-cas-ticket {} "http://test-service")]
        (is (= (get-in data [:headers "accept"]) "*/*"))
        (is (= (get-in data [:query-params :ticket]) "test-ticket")))
      (is (= @get-st-call-count 2))))
  (testing "Add service ticket eventually throws"
    (let [get-st-call-count (atom 0)]
      (client/set-post!
        (fn [^String url _]
          (cond
            ; refresh ticket granting ticket
            (.endsWith url "/v1/tickets")
            {:status 201 :headers {"location" "http://ticket.url"}}
            ; get service ticket returns 404 continuously
            (= url "http://ticket.url")
            (do
              (swap! get-st-call-count inc)
              (throw
                (ex-info
                  "Test HTTP Exception"
                  {:status 404
                   :body
                   "TGT-nnn could not be found or is considered invalid"}))))))
      (reset! c/grant-ticket {:url "http://ticket.url"
                              :expires (t/plus (t/now) (t/hours 2))})
      (let [result (try
                     (c/add-cas-ticket {} "http://test-service")
                     nil
                     (catch Exception e
                       e))]
        (is (= :unauthorized (:type (ex-data result)))))
      (is (= @get-st-call-count 2))))
  (testing "Add service ticket immediately throws when get ST returns 500"
    (let [get-st-call-count (atom 0)]
      (client/set-post! (fn [_ __]
                          (swap! get-st-call-count inc)
                          (throw (ex-info "Test HTTP Exception"
                                          {:status 500
                                           :body "Infernal server error"}))))
      (reset! c/grant-ticket {:url "http://ticket.url"
                              :expires (t/plus (t/now) (t/hours 2))})
      (let [result (try
                     (c/add-cas-ticket {} "http://test-service")
                     nil
                     (catch Exception e
                       e))]
        (is (= :unauthorized (:type (ex-data result)))))
      ; not trying to retry in this case
      (is (= @get-st-call-count 1)))))

(deftest test-with-service-ticket
  (testing "Request with API headers"
    (client/set-get! (fn [_ __] {:body {:value true}
                                 :status 200}))
    (client/set-post! (fn [_ __] {:body "test-ticket"}))
    (reset! c/grant-ticket {:url "http://ticket.url"
                            :expires (t/plus (t/now) (t/hours 2))})
    (let [response (c/with-service-ticket
                     {:method :get
                      :service "http://test-service"
                      :path "/"
                      :options {}})]
      (is (= (:body response) {:value true})))))

(def xml-map '{:v ({:k ("something")} {:o ("other")})})

(deftest test-xml->map
  (testing "Conversion of XML response to map"
    (is (= (c/xml->map
             (xml/sexp-as-element
               [:v
                [:k "something"]
                [:o "other"]]))
           xml-map))))

(deftest test-find-value
  (testing "Finding value in XML map"
    (is (= (first (c/find-value xml-map [:v :o]))
           "other"))
    (is (= (first (c/find-value xml-map [:v :k]))
           "something"))))
