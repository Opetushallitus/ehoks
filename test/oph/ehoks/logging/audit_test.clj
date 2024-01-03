(ns oph.ehoks.logging.audit-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [matches with-log]]
            [oph.ehoks.hoks.hoks-parts.parts-test-data
             :refer [ahato-data multiple-ahato-values-patched]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.logging.audit :as a]
            [oph.ehoks.utils :as utils]))

(use-fixtures :once utils/migrate-database)

(def ahato-path "aiemmin-hankittu-ammat-tutkinnon-osa")

(declare expected-log-entry-on-hoks-creation
         expected-log-entry-on-ahato-creation
         expected-log-entry-on-ahato-update
         expected-log-entry-on-ahato-read)

(defn- vec-remove
  "Remove element from vector"
  [coll index]
  (into (subvec coll 0 index) (subvec coll (inc index))))

(deftest test-handle-audit-logging
  (with-log
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request ahato-path ahato-data app hoks)
      (hoks-utils/create-mock-hoks-osa-patch-request
        ahato-path app multiple-ahato-values-patched)
      (hoks-utils/create-mock-hoks-osa-get-request ahato-path app hoks)
      (let [[hoks-create _ ahato-creation ahato-update ahato-read]
            (map #(-> % :message json/read-str (dissoc "bootTime"))
                 (matches "audit" :info #""))
            logseq (get hoks-create "logSeq")]
        (are [actual expected] (= actual expected)
          hoks-create    (expected-log-entry-on-hoks-creation  logseq)
          ahato-creation (expected-log-entry-on-ahato-creation (+ 2 logseq))
          (update ahato-update "changes" vec-remove 5)
          (expected-log-entry-on-ahato-update   (+ 3 logseq))
          ahato-read     (expected-log-entry-on-ahato-read     (+ 4 logseq))))
      (utils/clear-db))))

(deftest test-changes
  (testing "Single element replaced in map"
    (is (= (#'a/changes {:a 1 :b 2 :c 3} {:a 1 :b -2 :c 3})
        '({"path" ":b" "oldValue" 2 "newValue" -2}))))

  (testing "Single element added to map"
    (is (= (#'a/changes {:a 1 :b 2} {:a 1 :b 2 :c 3})
        '({"path" ":c" "newValue" 3}))))

  (testing "Single element removed from map"
    (is (= (#'a/changes {:a 1 :b 2 :c 3} {:a 1 :c 3})
        '({"path" ":b" "oldValue" 2}))))

  (testing "Changes in nested data structures (within map) are detected"
    (is (= (#'a/changes {:a 1 :b {:c 2 :d  3} :e 4 :f [0  1 2]}
                        {:a 1 :b {:c 2 :d -3} :e 4 :f [0 -1 2]})
        '({"path" ":b.:d" "oldValue" 3 "newValue" -3}
          {"path" ":f.1"  "oldValue" 1 "newValue" -1}))))

  (testing "Single element replaced in vector"
    (is (= (#'a/changes [0 1 2] [0 3 2])
           '({"path" "1" "oldValue" 1 "newValue" 3}))))

  (testing "Single element added to the end of the vector"
    (is (= (#'a/changes [0 1 2] [0 1 2 3]) '({"path" "3" "newValue" 3}))))

  (testing "Single element removed from the end of the vector"
    (is (= (#'a/changes [0 1 2 3] [0 1 2]) '({"path" "3" "oldValue" 3}))))

  (testing "Single removal from the middle of the vector"
    (is (= (#'a/changes [0 1 2 3 4] [0 1 3 4])
           '({"path" "2" "oldValue" 2 "newValue" 3}
             {"path" "3" "oldValue" 3 "newValue" 4}
             {"path" "4" "oldValue" 4}))))

  (testing "Single addition to the middle of the vector"
    (is (= (#'a/changes [0 1 3 4] [0 1 2 3 4])
           '({"path" "2" "oldValue" 3 "newValue" 2}
             {"path" "3" "oldValue" 4 "newValue" 3}
             {"path" "4" "newValue" 4}))))

  (testing "Changes in nested data structures (within vector) are detected"
    (is (= (#'a/changes [0 {:a 1 :b 2} 3 [4 5 6] 7]
                      [0 {:a 1 :b -2} 3 [4 -5 6] 7])
           '({"path" "1.:b" "oldValue" 2 "newValue" -2}
             {"path" "3.1" "oldValue" 5 "newValue" -5}))))

  (testing
    "Value is added when old value is `nil`"
    (are [new] (get (first (#'a/changes nil new)) "newValue")
         1 "a" true {:a 1} [0 1]))
  (testing
    "Value is removed when new value is `nil`"
    (are [old] (get (first (#'a/changes old nil)) "oldValue")
         1 "a" true {:a 1} [0 1]))
  (testing (str "Value is replaced when the old and the new value are unequal
                but both are not maps or vectors.")
    (are [old new] (as-> (#'a/changes old new) c
                         (first c)
                         (and (= (get c "oldValue") old)
                              (= (get c "newValue") new)))
         "kissa"     "koira"
         1           2
         1           "1"
         true        1
         false       0
         :a          "a"
         \a          "a"
         1.0         1
         1/2         0.5
         {:a 1 :b 2} [1 2])))

(deftest test-make-json-serializable
  (testing "The function returns (expected) values that are JSON serializable"
    (are [obj processed] (= (#'a/make-json-serializable obj) processed)
      nil               nil
      true              true
      "cat"             "cat"
      23                23
      1.2               1.2
      1/5               1/5
      :key              ":key"
      'symbol           "symbol"
      {:key 'symbol}    {:key "symbol"}
      [:key 'symbol 23] [":key" "symbol" 23])))

(defn- expected-log-entry-on-hoks-creation
  [logseq]
  {"hostname" ""
   "logSeq" logseq
   "user"
   {"oid" "1.2.246.562.24.11474338834"
    "ipAddress" "127.0.0.1"
    "session" "ST-testitiketti"
    "userAgent" "no user agent"}
   "applicationType" "backend"
   "operation" "create"
   "serviceName" "both"
   "changes"
   [{"path" ""
     "newValue"
     {"ensikertainen-hyvaksyminen" "2019-03-18"
      "osaamisen-hankkimisen-tarve" false
      "opiskeluoikeus-oid" "1.2.246.562.15.10000000009"
      "oppija-oid" "1.2.246.562.24.12312312319"}}]
   "type" "log"
   "version" 1
   "target"
   {"hoksId" nil
    "oppijaOid" "1.2.246.562.24.12312312319"
    "opiskeluoikeusOid" nil}})

(defn- expected-log-entry-on-ahato-creation
  [logseq]
  {"hostname" ""
   "logSeq" logseq
   "user"
   {"oid" "1.2.246.562.24.11474338834"
    "ipAddress" "127.0.0.1"
    "session" "ST-testitiketti"
    "userAgent" "no user agent"}
   "applicationType" "backend"
   "operation" "create"
   "serviceName" "both"
   "changes"
   [{"path" ""
     "newValue"
     {"tutkinnon-osa-koodi-versio" 100022
      "valittu-todentamisen-prosessi-koodi-versio" 3
      "tutkinnon-osa-koodi-uri" "tutkinnonosat_100022"
      "valittu-todentamisen-prosessi-koodi-uri"
      "osaamisentodentamisenprosessi_3"
      "koulutuksen-jarjestaja-oid"
      "1.2.246.562.10.54453921410"
      "tarkentavat-tiedot-osaamisen-arvioija"
      {"lahetetty-arvioitavaksi" "2019-03-18"
       "aiemmin-hankitun-osaamisen-arvioijat"
       [{"nimi" "Erkki Esimerkki"
         "organisaatio"
         {"oppilaitos-oid" "1.2.246.562.10.54453921626"}}
        {"nimi" "Joku Tyyppi"
         "organisaatio"
         {"oppilaitos-oid" "1.2.246.562.10.54453921006"}}]}
      "tarkentavat-tiedot-naytto"
      [{"sisallon-kuvaus" ["Tutkimustyö" "Raportointi"]
        "yksilolliset-kriteerit" ["Ensimmäinen kriteeri"]
        "alku" "2019-02-09"
        "tyoelama-osaamisen-arvioijat"
        [{"nimi" "Teppo Työmies"
          "organisaatio"
          {"nimi" "Testiyrityksen Sisar Oy"
           "y-tunnus" "1234563-9"}}]
        "jarjestaja"
        {"oppilaitos-oid" "1.2.246.562.10.54453921683"}
        "loppu" "2019-01-12"
        "koulutuksen-jarjestaja-osaamisen-arvioijat"
        [{"nimi" "Aapo Arvioija"
          "organisaatio"
          {"oppilaitos-oid" "1.2.246.562.10.54453921675"}}]
        "nayttoymparisto"
        {"nimi" "Toinen Esimerkki Oyj"
         "y-tunnus" "1234566-3"
         "kuvaus" "Testiyrityksen testiosasostalla"}
        "osa-alueet"
        [{"koodi-uri" "ammatillisenoppiaineet_fy"
          "koodi-versio" 1}]}]}}]
   "type" "log"
   "version" 1
   "target"
   {"hoksId" "1" "oppijaOid" nil "opiskeluoikeusOid" nil}})

(defn- expected-log-entry-on-ahato-update
  [logseq]
  {"hostname" "",
   "logSeq" logseq,
   "user"
   {"oid" "1.2.246.562.24.11474338834",
    "ipAddress" "127.0.0.1",
    "session" "ST-testitiketti",
    "userAgent" "no user agent"},
   "applicationType" "backend",
   "operation" "update",
   "serviceName" "both",
   "changes"
   [{"path" ":tutkinnon-osa-koodi-versio",
     "oldValue" 100022,
     "newValue" 3000}
    {"path"
     (str ":tarkentavat-tiedot-naytto.0."
          ":koulutuksen-jarjestaja-osaamisen-arvioijat.0.:nimi"),
     "oldValue" "Aapo Arvioija",
     "newValue" "Muutettu Arvioija"}
    {"path"
     ":tarkentavat-tiedot-naytto.0.:nayttoymparisto.:nimi",
     "oldValue" "Toinen Esimerkki Oyj",
     "newValue" "Testi Oy"}
    {"path"
     ":tarkentavat-tiedot-naytto.0.:nayttoymparisto.:y-tunnus",
     "oldValue" "1234566-3",
     "newValue" "1234565-5"}
    {"path" ":tarkentavat-tiedot-naytto.0.:osa-alueet.0",
     "oldValue"
     {"koodi-uri" "ammatillisenoppiaineet_fy",
      "koodi-versio" 1}}
    {"path"
     ":tarkentavat-tiedot-naytto.0.:tyoelama-osaamisen-arvioijat.0",
     "oldValue"
     {"nimi" "Teppo Työmies",
      "organisaatio"
      {"nimi" "Testiyrityksen Sisar Oy",
       "y-tunnus" "1234563-9"}}}
    {"path"
     ":tarkentavat-tiedot-naytto.0.:yksilolliset-kriteerit.0",
     "oldValue" "Ensimmäinen kriteeri",
     "newValue" "testikriteeri"}
    {"path"
     ":tarkentavat-tiedot-osaamisen-arvioija.:lahetetty-arvioitavaksi",
     "oldValue" "2019-03-18",
     "newValue" "2020-01-01"}
    {"path"
     (str ":tarkentavat-tiedot-osaamisen-arvioija."
          ":aiemmin-hankitun-osaamisen-arvioijat.0.:nimi"),
     "oldValue" "Erkki Esimerkki",
     "newValue" "Nimi Muutettu"}
    {"path"
     (str ":tarkentavat-tiedot-osaamisen-arvioija."
          ":aiemmin-hankitun-osaamisen-arvioijat.0."
          ":organisaatio.:oppilaitos-oid"),
     "oldValue" "1.2.246.562.10.54453921626",
     "newValue" "1.2.246.562.10.54453555556"}],
   "type" "log",
   "version" 1,
   "target"
   {"hoksId" "1", "oppijaOid" nil, "opiskeluoikeusOid" nil}})


(defn- expected-log-entry-on-ahato-read
  [logseq]
  {"hostname" ""
   "logSeq" logseq
   "user"
   {"oid" "1.2.246.562.24.11474338834"
    "ipAddress" "127.0.0.1"
    "session" "ST-testitiketti"
    "userAgent" "no user agent"}
   "applicationType" "backend"
   "operation" "read"
   "serviceName" "both"
   "changes" []
   "type" "log"
   "version" 1
   "target"
   {"hoksId" "1" "oppijaOid" nil "opiskeluoikeusOid" nil}})
