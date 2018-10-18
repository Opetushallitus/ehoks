(ns oph.ehoks.external.oppijanumerorekisteri-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]))

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
