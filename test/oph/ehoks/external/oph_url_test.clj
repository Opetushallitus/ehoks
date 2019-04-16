(ns oph.ehoks.external.oph-url-test
  (:require [oph.ehoks.external.oph-url :as u]
            [clojure.test :as t]))

(t/deftest valid-line-test
  (t/testing "Testing valid line"
    (t/is (u/valid-line? "hello=world"))
    (t/is (u/valid-line? " hello=world "))
    (t/is (not (u/valid-line? "#hello=world")))
    (t/is (not (u/valid-line? "")))))

(t/deftest replace-vars-test
  (t/testing "Replacing vars"
    (t/is (= (u/replace-vars "${var1}/path" {"var1" "replaced"})
             "replaced/path"))
    (t/is (= (u/replace-vars "some/path" {"var1" "replaced"})
             "some/path"))
    (t/is (= (u/replace-vars "/path" {})
             "/path"))))

(t/deftest parse-line-test
  (t/testing "Parsing line"
    (t/is (= (u/parse-line "hello=world")
             ["hello" "world"]))
    (t/is (= (u/parse-line "")
             [""]))))

(t/deftest parse-urls-test
  (t/testing "Parsing urls"
    (t/is (= (u/parse-urls ["some=path"
                            "other=path2"
                            "third=${some}/else"]
                           {})
             {"some" "path"
              "other" "path2"
              "third" "path/else"}))
    (t/is (= (u/parse-urls nil {})
             {}))
    (t/is (= (u/parse-urls [] {})
             {}))))

(t/deftest replace-args-test
  (t/testing "Replacing args"
    (t/is (= (u/replace-args "test/$1" [1])
             "test/1"))
    (t/is (= (u/replace-args
               "host/path/$1/$2/$3/$4/$5/$6/$7/$8/$9/$10/$11"
               ["param1" "param2" "param3" "param4" "param5" "param6"
                "param7" "param8" "param9" "param10" "param11"])
             (str "host/path/param1/param2/param3/param4/param5/param6/param7"
                  "/param8/param9/param10/param11")))))
