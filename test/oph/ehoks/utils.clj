(ns oph.ehoks.utils
  (:require [cheshire.core :as cheshire]
            [ring.mock.request :as mock]
            [clojure.test :refer [is]]
            [clojure.data :as d]
            [clojure.pprint :as p]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.db.migrations :as m]))

(def base-url "/ehoks-oppija-backend/api/v1/oppija/session")

(defn authenticate
  ([app oppija-oid]
    (client/with-mock-responses
      [(fn [url options]
         (cond
           (.endsWith url "/serviceValidate")
           {:status 200
            :body
            (str "<cas:serviceResponse"
                 "  xmlns:cas='http://www.yale.edu/tp/cas'>"
                 "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
                 "<cas:attributes>"
                 "<cas:longTermAuthenticationRequestTokenUsed>false"
                 "</cas:longTermAuthenticationRequestTokenUsed>"
                 "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                 "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                 "</cas:authenticationDate></cas:attributes>"
                 "</cas:authenticationSuccess></cas:serviceResponse>")}
           (.contains url "oppijanumerorekisteri-service")
           {:status 200
            :body {:results
                   [{:oidHenkilo (or oppija-oid "1.2.246.562.24.44651722625")
                     :hetu "250103-5360"
                     :etunimet "Aarto Maurits"
                     :kutsumanimi "Aarto"
                     :sukunimi "Väisänen-perftest"}]}}))
       (fn [url options]
         (cond
           (.endsWith url "/v1/tickets")
           {:status 201
            :headers {"location" "http://test.ticket/1234"}}
           (= url "http://test.ticket/1234")
           {:status 200
            :body "ST-1234-testi"}))]
      (app (-> (mock/request :get (str base-url "/opintopolku/"))
               (mock/header "FirstName" "Teuvo Testi")
               (mock/header "cn" "Teuvo")
               (mock/header "givenname" "Teuvo")
               (mock/header "hetu" "190384-9245")
               (mock/header "sn" "Testaaja")))))
  ([app] (authenticate app nil)))

(defn get-auth-cookie
  ([app oppija-oid]
    (-> (authenticate app oppija-oid)
        (get-in [:headers "Set-Cookie"])
        (first)))
  ([app] (get-auth-cookie app nil)))

(defn with-authentication [app request]
  (let [cookie (get-auth-cookie app)]
    (app (mock/header request :cookie cookie))))

(defn with-authenticated-oid-multi [store oid app & requests]
  (let [cookie (get-auth-cookie app oid)]
    (swap! store assoc-in [(-> @store keys first) :user :oid] oid)
    (mapv
      (fn [request]
        (app (mock/header request :cookie cookie)))
      requests)))

(defn with-authenticated-oid [store oid app request]
  (first (with-authenticated-oid-multi store oid app request)))

(defn set-auth-functions! [organisaatio-oid unmatched-fn]
  (client/set-post!
    (fn [url options]
      (cond
        (.endsWith url "/v1/tickets")
        {:status 201
         :headers {"location" "http://test.ticket/1234"}}
        (= url "http://test.ticket/1234")
        {:status 200
         :body "ST-1234-testi"}
        :else (unmatched-fn :post url options))))
  (client/set-get!
    (fn [url options]
      (cond (.endsWith url "/serviceValidate")
            {:status 200
             :body
             (str "<cas:serviceResponse"
                  "  xmlns:cas='http://www.yale.edu/tp/cas'>"
                  "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
                  "<cas:attributes>"
                  "<cas:longTermAuthenticationRequestTokenUsed>false"
                  "</cas:longTermAuthenticationRequestTokenUsed>"
                  "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                  "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                  "</cas:authenticationDate></cas:attributes>"
                  "</cas:authenticationSuccess></cas:serviceResponse>")}
            (.endsWith url "/kayttooikeus-service/kayttooikeus/kayttaja")
            {:status 200
             :body [{:oidHenkilo "1.2.246.562.24.11474338834"
                     :username "ehoks-test"
                     :kayttajaTyyppi "PALVELU"
                     :organisaatiot
                     [{:organisaatioOid organisaatio-oid
                       :kayttooikeudet [{:palvelu "EHOKS"
                                         :oikeus "CRUD"}]}]}]}
            (.endsWith
              url (str "/rest/organisaatio/v4/" organisaatio-oid))
            {:status 200
             :body {:parentOidPath
                    "|"}}
            :else (unmatched-fn :get url options)))))

(defmacro with-ticket-auth
  [[organisaatio-oid unmatched-fn] & body]
  `(do
     (cache/clear-cache!)
     (set-auth-functions! ~organisaatio-oid ~unmatched-fn)
     (do ~@body)
     (client/reset-functions!)))

(defn with-service-ticket
  ([app request oppilaitos-oid]
    (client/set-post!
      (fn [url options]
        (cond
          (.endsWith url "/v1/tickets")
          {:status 201
           :headers {"location" "http://test.ticket/1234"}}
          (= url "http://test.ticket/1234")
          {:status 200
           :body "ST-1234-testi"}
          (.endsWith
            url "/koski/api/sure/oids")
          {:status 200
           :body [{:henkilö {:oid "1.2.246.562.24.44000000001"}
                   :opiskeluoikeudet
                   [{:oid "1.2.246.562.15.76000000001"
                     :oppilaitos {:oid "1.2.246.562.10.12000000000"
                                  :nimi {:fi "TestiFi"
                                         :sv "TestiSv"
                                         :en "TestiEn"}}
                     :alkamispäivä "2020-03-12"}]}]})))
    (client/set-get!
      (fn [url options]
        (cond (.endsWith url "/serviceValidate")
              {:status 200
               :body
               (str "<cas:serviceResponse"
                    "  xmlns:cas='http://www.yale.edu/tp/cas'>"
                    "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
                    "<cas:attributes>"
                    "<cas:longTermAuthenticationRequestTokenUsed>false"
                    "</cas:longTermAuthenticationRequestTokenUsed>"
                    "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                    "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                    "</cas:authenticationDate></cas:attributes>"
                    "</cas:authenticationSuccess></cas:serviceResponse>")}
              (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.00000000001")
              {:status 200
               :body {:oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.10.12944436166")}}}
              (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.00000000002")
              {:status 200
               :body {:oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.24.47861388608")}}}
              (.endsWith url "/kayttooikeus-service/kayttooikeus/kayttaja")
              {:status 200
               :body [{:oidHenkilo "1.2.246.562.24.11474338834"
                       :username "ehoks-test"
                       :kayttajaTyyppi "PALVELU"
                       :organisaatiot
                       [{:organisaatioOid (or oppilaitos-oid
                                              "1.2.246.562.10.12944436166")
                         :kayttooikeudet [{:palvelu "EHOKS"
                                           :oikeus "CRUD"}]}]}]}
              (.endsWith
                url "/rest/organisaatio/v4/1.2.246.562.24.47861388608")
              {:status 200
               :body {:parentOidPath
                      "|"}}
              (.endsWith
                url "/rest/organisaatio/v4/1.2.246.562.10.12944436166")
              {:status 200
               :body {:parentOidPath
                      "|1.2.246.562.10.00000000001|"}}
              (> (.indexOf url "oppijanumerorekisteri-service/henkilo") -1)
              (let [oid (last (.split url "/"))]
                (if (= oid "1.2.246.562.24.40404040404")
                  (throw (ex-info "Not found" {:status 404}))
                  {:status 200
                   :body {:oidHenkilo oid
                          :hetu "250103-5360"
                          :etunimet "Tero Teuvo"
                          :kutsumanimi "Tero"
                          :sukunimi "Testaaja"}})))))
    (let [result (app (-> request
                          (mock/header "Caller-Id" "test")
                          (mock/header "ticket" "ST-testitiketti")))]
      (client/reset-functions!)
      result))
  ([app request]
    (with-service-ticket app request nil)))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn eq-check [value expect]
  (when (not= value expect)
    (let [diff (d/diff value expect)]
      (when (seq (first diff))
        (println "Not expected:")
        (p/pprint (first diff)))
      (when (seq (second diff))
        (println "Missing:")
        (p/pprint (second diff))))))

(defmacro eq
  ([value expect msg]
    `(do (let [v# ~value
               e# ~expect]
           (eq-check v# e#)
           (is (= v# e# ~msg)))))
  ([value expect]
    `(do (let [v# ~value
               e# ~expect]
           (eq-check v# e#)
           (is (= v# e#))))))

(defn with-database [f]
  (m/clean!)
  (m/migrate!)
  (f)
  (m/clean!))

(defmacro with-db [& body]
  `(do (m/clean!)
       (m/migrate!)
       (do ~@body)
       (m/clean!)))

(defn dissoc-uuids [data]
  (if (coll? data)
    (if (map? data)
      (reduce (fn [res val]
                (conj res [(first val) (dissoc-uuids (second val))]))
              {}
              (dissoc data :uuid))
      (map #(dissoc-uuids %) data))
    data))
