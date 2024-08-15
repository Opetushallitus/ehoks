(ns oph.ehoks.external.koski-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.koski :as k])
  (:import [clojure.lang ExceptionInfo]))

(deftest test-filter-oppija
  (testing "Filtering Oppija values"
    (is (= (k/filter-oppija {}) {:henkilö {}}))
    (is (= (k/filter-oppija
             {:henkilö {:oid "1.2.246.562.24.44651722625"
                        :hetu "250103-5360"
                        :syntymäaika "1903-01-25"
                        :etunimet "Aarto Maurits"
                        :kutsumanimi "Aarto"
                        :sukunimi "Väisänen-perftest"
                        :turvakielto false}
              :opiskeluoikeudet [{}]})
           {:henkilö {:oid "1.2.246.562.24.44651722625"
                      :hetu "250103-5360"
                      :syntymäaika "1903-01-25"
                      :etunimet "Aarto Maurits"
                      :kutsumanimi "Aarto"
                      :sukunimi "Väisänen-perftest"}
            :opiskeluoikeudet [{}]}))))

(defn mock-get-opiskeluoikeus-raw
  [oid]
  (case oid
    "1.2.246.562.15.10000000009"
    {:oid "1.2.246.562.15.10000000009"
     :oppilaitos {:oid "1.2.246.562.10.12944436166"}
     :suoritukset [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
     :koulutustoimija {:oid "1.2.246.562.10.10000000009"}}
    "1.246.562.15.12345678911" {}
    "1.246.562.15.12345678910"
    (throw (ex-info
             "Bad request"
             {:status 400
              :body   (str "[{\"key\": \"badRequest.format.number\"}]")}))
    "1.2.246.562.15.12345678903" {}
    "1.2.246.562.15.23456789017"
    {:suoritukset [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
     :koulutustoimija {:oid "1.2.246.562.10.23456789017"}}
    "1.2.246.562.15.34567890123"
    {:suoritukset [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
     :koulutustoimija {:oid "1.2.246.562.10.34567890123"}}
    (->> {:status 404
          :body (str "[{\"key\": \"notFound.opiskeluoikeutta"
                     "EiLöydyTaiEiOikeuksia\"}]")}
         (ex-info "Asd")
         (throw))))

(deftest test-get-opiskeluoikeus!
  (with-redefs [k/get-opiskeluoikeus-info-raw mock-get-opiskeluoikeus-raw]
    (testing "The function returns opiskeluoikeus when found"
      (is (some? (k/get-opiskeluoikeus! "1.246.562.15.12345678911"))))
    (testing "The function returns `nil` when opiskeluoikeus is not found."
      (is (nil? (k/get-opiskeluoikeus! "1.246.562.15.40440440440"))))
    (testing "The function throws on exceptional status codes."
      (is (thrown-with-msg?
            ExceptionInfo
            (re-pattern
              (str "Error while fetching opiskeluoikeus "
                   "`1.246.562.15.12345678910` from Koski. "
                   "Koski-virhekoodi is `badRequest.format.number`."))
            (k/get-opiskeluoikeus! "1.246.562.15.12345678910"))))))

(deftest test-get-existing-opiskeluoikeus!
  (with-redefs [k/get-opiskeluoikeus-info-raw mock-get-opiskeluoikeus-raw]
    (testing "The function returns opiskeluoikeus when found"
      (is (some? (k/get-opiskeluoikeus! "1.246.562.15.12345678911"))))
    (testing "The function throws when opiskeluoikeus is not found."
      (is (thrown-with-msg? ExceptionInfo
                            #"Opiskeluoikeus `testi` not found in Koski"
                            (k/get-existing-opiskeluoikeus! "testi"))))
    (testing "The function throws on exceptional status codes."
      (is (thrown-with-msg?
            ExceptionInfo
            (re-pattern
              (str "Error while fetching opiskeluoikeus "
                   "`1.246.562.15.12345678910` from Koski. "
                   "Koski-virhekoodi is `badRequest.format.number`."))
            (k/get-opiskeluoikeus! "1.246.562.15.12345678910"))))))

(deftest test-get-oppija-opiskeluoikeudet
  (testing "Get opiskeluoikeudet for oppija"
    (client/set-post!
      (fn [^String url options]
        (cond
          (.endsWith
            url "/koski/api/sure/oids")
          (let [oids (json/parse-string (:body options))]
            {:status 200
             :body (map
                     (fn [oid]
                       {:henkilö {:oid oid}
                        :opiskeluoikeudet
                        [{:oid "1.2.246.562.15.55003456345"
                          :oppilaitos
                          {:oid "1.2.246.562.10.12944436166"}}
                         {:oid "1.2.246.562.15.55003456345"
                          :oppilaitos
                          {:oid "1.2.246.562.10.12944436166"}}]})
                     oids)}))))

    (is (= (k/get-oppija-opiskeluoikeudet "1.2.246.562.24.51659804532")
           [{:oid "1.2.246.562.15.55003456345"
             :oppilaitos
             {:oid "1.2.246.562.10.12944436166"}}
            {:oid "1.2.246.562.15.55003456345"
             :oppilaitos
             {:oid "1.2.246.562.10.12944436166"}}]))
    (client/reset-functions!)))

(deftest test-virhekoodi
  (testing "Can parse Koski-specific virhekoodi."
    (is (= (k/virhekoodi
             (ex-info
               "Something something..."
               {:body (str "[{\"key\":\"notFound.opiskeluoikeutta"
                           "EiLöydyTaiEiOikeuksia\",\"message\":"
                           "\"Opiskeluoikeutta ei löydy annetulla oid:llä tai "
                           "käyttäjällä ei ole siihen oikeuksia\"}]")}))
           "notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia")))
  (testing "`nil` is returned if virhekoodi cannot be parsed."
    (is (nil? (k/virhekoodi (ex-info "Something something..."
                                     {:body "Not valid JSON"}))))))
