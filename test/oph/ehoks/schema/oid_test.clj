(ns oph.ehoks.schema.oid-test
  (:require [clojure.test :refer [are deftest testing]]
            [oph.ehoks.schema.oid :refer [organisaatio-oid-nodes
                                          OpiskeluoikeusOID
                                          OppijaOID
                                          OrganisaatioOID]]
            [schema.core :as s])
  (:import [clojure.lang ExceptionInfo]))

(deftest test-opiskeluoikeus-oid-validation
  (testing "Valid opiskeluoikeus OIDs pass the validation."
    (are [oid] (s/validate OpiskeluoikeusOID oid)
      "1.2.246.562.15.41024330544"
      "1.2.246.562.15.73222573930"
      "1.2.246.562.15.68533489990"
      "1.2.246.562.15.80624098275"
      "1.2.246.562.15.97241700166"
      "1.2.246.562.15.78573431000"
      "1.2.246.562.15.298574987110"   ; checksum == 0
      "1.2.246.562.15.983769802510")) ; checksum == 0
  (testing "Invalid opiskeluoikeus OIDs won't pass the validation."
    (are [oid] (thrown? ExceptionInfo (s/validate OpiskeluoikeusOID oid))
      "asd"
      "1.2.3"
      "1.2.246.562.15.09873698279"    ; correct checksum but starts with zero
      "1.2.246.562.15.1068968963"     ; correct checksum but too few digits
      "1.2.246.562.15.987369457211"   ; correct checksum but too many digits
      "1.2.246.562.15.897639829610"   ; checksum != 0
      "1.2.246.562.24.41024330544"    ; oppija oid node
      "1.2.246.562.10.73222573930"    ; organisaatio oid node
      "1.2.246.562.15.41014330544"    ; checksum mismatch
      "1.2.246.562.15.68533489991"))) ; checksum mismatch

(deftest test-oppija-oid-validation
  (testing "Valid oppija OIDs (node 24) pass the validation."
    (are [oid] (s/validate OppijaOID (format "1.2.246.562.24.%s" oid))
      "54450598189"
      "37998958910"
      "92170778843"
      "64297803263"
      "89826171930"
      "16068378700"
      "898261719310"   ; checksum == 0
      "160683787010")) ; checksum == 0
  (testing
   "Valid test oppija OIDs (nodes 98, 198, and 298) pass the validation."
    (doseq [node [98 198 298]]
      (are [oid] (s/validate OppijaOID (format "1.2.246.562.%s.%s" node oid))
        "54450598187"
        "37998958914"
        "92170778846"
        "64297803260"
        "89826171939"
        "16068378708"
        "160683787410"   ; checksum == 0
        "160683787410"))) ; checksum == 0
  (testing "Invalid oppija OIDs won't pass the validation."
    (are [oid] (thrown? ExceptionInfo (s/validate OppijaOID oid))
      "asd"
      "1.2.3"
      "1.2.246.562.10.02589689322"    ; correct checksum but starts with zero
      "1.2.246.562.24.7189698747"     ; correct checksum but too few digits
      "1.2.246.562.24.639871578679"   ; correct checksum but too many digits
      "1.2.246.562.15.54450598189"    ; opiskeluoikeus oid node
      "1.2.246.562.10.37998958910"    ; organisaatio oid node
      "1.2.246.562.24.642978032610"   ; checksum != 0
      "1.2.246.562.24.54440598189"    ; checksum mismatch, 6 would be correct
      "1.2.246.562.98.54440598186"    ; checksum mismatch, 0 would be correct
      "1.2.246.562.198.54440598186"   ; checksum mismatch, 0 would be correct
      "1.2.246.562.298.54440598186"   ; checksum mismatch, 0 would be correct
      "1.2.246.562.24.37998957910"))) ; checksum mismatch, 1 would be correct

(deftest test-organisaatio-oid-validation
  (testing "Valid organisaatio OIDs pass the validation."
    (doseq [node organisaatio-oid-nodes]
      (are [oid-part] (s/validate OrganisaatioOID (format "1.2.246.562.%s.%s"
                                                          node oid-part))
        "92214483247"
        "18950669244"
        "77831291537"
        "38262856784"
        "67924833642"
        "2013110716590316970385" ; These types OIDs also exist
        "2014081110425906984827"
        "587342913610"
        "54440598189"
        "37998957910"
        "778312915310"
        "143886286710")))
  (testing "Invalid organisaatio OIDs won't pass the validation."
    (are [oid-part] (thrown? ExceptionInfo (s/validate OrganisaatioOID
                                                       oid-part))
      "asd"
      "1.2.3"
      "1.2.246.562.10.5286980957"
      "1.2.246.562.10.2897630745971"
      "1.2.246.562.99.2897630745971"
      "1.2.246.562.199.2897630745971"
      "1.2.246.562.299.2897630745971"
      "1.2.246.562.10.20131107165903169703852"
      "1.2.246.562.15.92214483247"    ; opiskeluoikeus oid node
      "1.2.246.562.24.18950669244"))) ; oppija oid node

;;;; The following tests can be used to test OID validation with data stored in
;;;; files. They're commented because it's not necessary to run them everytime.

; (deftest test-opiskeluoikeus-oid-validation-with-hoks-opiskeluoikeus-oids
;   (let [opiskeluoikeus-oids (str/split-lines (slurp "./opiskeluoikeudet"))]
;     (doseq [oid opiskeluoikeus-oids]
;       (is (s/validate OpiskeluoikeusOID oid)))))
;
; (deftest test-opiskeluoikeus-oid-validation-with-hoks-oppija-oids
;   (let [oppija-oids (str/split-lines (slurp "./oppijat"))]
;     (doseq [oid oppija-oids]
;       (is (s/validate OppijaOID oid)))))

; (deftest test-opiskeluoikeus-oid-validation-with-hato-koulutuksenjarjestajat
;   (let [organisaatio-oids (str/split-lines
;                             (slurp "./organisaatio_oidit.csv"))]
;     (doseq [oid organisaatio-oids]
;       (is (s/validate OrganisaatioOID oid)))))
