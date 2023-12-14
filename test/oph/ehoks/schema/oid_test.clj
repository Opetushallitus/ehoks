(ns oph.ehoks.schema.oid-test
  (:require [clojure.test :refer [are deftest testing]]
            [oph.ehoks.schema.oid :refer [OpiskeluoikeusOID
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
  (testing "Valid oppija OIDs pass the validation."
    (are [oid] (s/validate OppijaOID oid)
      "1.2.246.562.24.54450598189"
      "1.2.246.562.24.37998958910"
      "1.2.246.562.24.92170778843"
      "1.2.246.562.24.64297803263"
      "1.2.246.562.24.89826171930"
      "1.2.246.562.24.16068378700"
      "1.2.246.562.24.898261719310"   ; checksum == 0
      "1.2.246.562.24.160683787010")) ; checksum == 0
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
      "1.2.246.562.24.54440598189"    ; checksum mismatch
      "1.2.246.562.24.37998957910"))) ; checksum mismatch

(deftest test-organisaatio-oid-validation
  (testing "Valid organisaatio OIDs pass the validation."
    (are [oid] (s/validate OrganisaatioOID oid)
      "1.2.246.562.10.92214483247"
      "1.2.246.562.10.18950669244"
      "1.2.246.562.10.77831291537"
      "1.2.246.562.10.38262856784"
      "1.2.246.562.10.67924833642"
      "1.2.246.562.10.587342913610"   ; checksum == 0
      "1.2.246.562.10.143886286710")) ; checksum == 0
  (testing "Invalid organisaatio OIDs won't pass the validation."
    (are [oid] (thrown? ExceptionInfo (s/validate OrganisaatioOID oid))
      "asd"
      "1.2.3"
      "1.2.246.562.10.09873698279"    ; correct checksum but starts with zero
      "1.2.246.562.10.5286980957"     ; correct checksum but too few digits
      "1.2.246.562.10.289763074597"   ; correct checksum but too many digits
      "1.2.246.562.15.92214483247"    ; opiskeluoikeus oid node
      "1.2.246.562.24.18950669244"    ; oppija oid node
      "1.2.246.562.10.778312915310"   ; checksum != 0
      "1.2.246.562.10.54440598189"    ; checksum mismatch
      "1.2.246.562.10.37998957910"))) ; checksum mismatch
