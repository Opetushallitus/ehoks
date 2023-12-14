(ns oph.ehoks.utils
  (:require [cheshire.core :as cheshire]
            [ring.mock.request :as mock]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [clojure.test :refer [is]]
            [clojure.data :as d]
            [clojure.pprint :as p]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.migrations :as m]
            [clojure.java.jdbc :as jdbc]))

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
            :body {:oidHenkilo (or oppija-oid "1.2.246.562.24.44651722625")
                   :hetu "250103-5360"
                   :etunimet "Aarto Maurits"
                   :kutsumanimi "Aarto"
                   :sukunimi "Väisänen-perftest"}}))
       (fn [url options]
         (cond
           (.endsWith url "/v1/tickets")
           {:status 201
            :headers {"location" "http://test.ticket/1234"}}
           (= url "http://test.ticket/1234")
           {:status 200
            :body "ST-1234-testi"}))]
      (app (-> (mock/request :get (str base-url "/opintopolku/"))
               (mock/query-string {:ticket "ST-1234-testi"})))))
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
        (app (-> request
                 (mock/header :cookie cookie)
                 (mock/header "Caller-Id" "test"))))
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
           :body [{:henkilö {:oid "1.2.246.562.24.44000000008"}
                   :opiskeluoikeudet
                   [{:oid "1.2.246.562.15.76000000000"
                     :oppilaitos {:oid "1.2.246.562.10.12000000005"
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
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.10000000009")
              {:status 200
               :body {:oid "1.2.246.562.15.10000000009"
                      :tila {:opiskeluoikeusjaksot
                             [{:alku "2010-01-01"
                               :tila {:koodiarvo "lasna"
                                      :nimi {:fi "Läsnä"}
                                      :koodistoUri "koskiopiskeluoikeudentila"
                                      :koodistoVersio 1}}]}
                      :oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.10.12944436166")}
                      :suoritukset
                      [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
                      :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
              (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.20000000008")
              {:status 200
               :body {:oid "1.2.246.562.15.20000000008"
                      :oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.10.47861388602")}
                      :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
              (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.30000000007")
              {:status 200
               :body {:oid "1.2.246.562.15.30000000007"
                      :oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.10.12944436166")}
                      :suoritukset
                      [{:tyyppi {:koodiarvo "tuvaperusopetus"}}]
                      :tyyppi {:koodiarvo "tuva"}}}
              (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.40000000006")
              {:status 200
               :body {:oid "1.2.246.562.15.40000000006"
                      :tila {:opiskeluoikeusjaksot
                             [{:alku "2023-10-01"
                               :tila {:koodiarvo "lasna"
                                      :nimi {:fi "Läsnä"}
                                      :koodistoUri "koskiopiskeluoikeudentila"
                                      :koodistoVersio 1}}]}
                      :oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.10.12944436166")}
                      :alkamispäivä "2023-10-01"
                      :suoritukset
                      [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
                      :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
              (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.50000000005")
              {:status 200
               :body {:oid "1.2.246.562.15.50000000005"
                      :tila {:opiskeluoikeusjaksot
                             [{:alku "2010-10-01"
                               :tila {:koodiarvo "lasna"
                                      :nimi {:fi "Läsnä"}
                                      :koodistoUri "koskiopiskeluoikeudentila"
                                      :koodistoVersio 1}}]}
                      :oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.10.12944436166")}
                      :alkamispäivä "2010-10-01"
                      :arvioituPäättymispäivä "2010-12-01"
                      :suoritukset
                      [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
                      :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
              (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.60000000004")
              {:status 200
               :body {:oid "1.2.246.562.15.60000000004"
                      :tila {:opiskeluoikeusjaksot
                             [{:alku "2010-12-01"
                               :tila {:koodiarvo "eronnut"
                                      :nimi {:fi "Eronnut"}
                                      :koodistoUri "koskiopiskeluoikeudentila"
                                      :koodistoVersio 1}}]}
                      :oppilaitos {:oid (or oppilaitos-oid
                                            "1.2.246.562.10.12944436166")}
                      :alkamispäivä "2010-10-01"
                      :arvioituPäättymispäivä "2010-12-01"
                      :suoritukset
                      [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
                      :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
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
                url "/rest/organisaatio/v4/1.2.246.562.10.47861388602")
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
                (if (= oid "1.2.246.562.24.40404040406")
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

(defn to-string [body]
  (cheshire/generate-string body))

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

(defn clear-db []
  (jdbc/execute!
    (db-ops/get-db-connection)
    (slurp (clojure.java.io/resource "oph/ehoks/empty_database.sql"))))

(defn migrate-database [f]
  (m/clean!)
  (m/migrate!)
  (f))

(defn empty-database-after-test [f]
  (f)
  (clear-db))

(defmacro with-db [& body]
  `(do (do ~@body)
       (clear-db)))

(defn dissoc-module-ids [data]
  (if (coll? data)
    (if (map? data)
      (reduce (fn [res val]
                (conj res [(first val) (dissoc-module-ids (second val))]))
              {}
              (dissoc data :module-id :yksiloiva-tunniste))
      (map #(dissoc-module-ids %) data))
    data))

(defn wait-for
  [predicate timeout-ms]
  (let [wait-until (+ (tc/to-long (time/now)) timeout-ms)
        result (atom false)]
    (while (and (false? @result)
                (< (tc/to-long (time/now)) wait-until))
      (swap! result predicate))
    @result))

(defn mock-get-opiskeluoikeus-info
  [_]
  {:tyyppi {:koodiarvo "ammatillinenkoulutus"}})

(defn assoc-if-some
  "Like `assoc`, but doesn't add key-value-pair to map if value is `nil`."
  [m & kvs]
  (->> (partition 2 kvs)
       (filter (comp some? second))
       (map vec)
       (into m)))
