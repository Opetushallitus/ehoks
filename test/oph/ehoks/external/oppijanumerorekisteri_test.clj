(ns oph.ehoks.external.oppijanumerorekisteri-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.oppijanumerorekisteri
             :refer [convert-student-info]]))

(deftest test-convert-student-info
  (testing "Converting Oppijanumerorekisteri student info"
    (is (= (convert-student-info
             {})
           {}))
    (is (= (convert-student-info
             {:oidHenkilo "1.2.246.562.24.78058065184"
              :hetu "190384-9245"
              :etunimet "Vapautettu"
              :kutsumanimi "Testi"
              :sukunimi "Maksullinen"
              :yhteystiedotRyhma
              '({:id 0,
                 :readOnly true,
                 :ryhmaAlkuperaTieto "testiservice",
                 :ryhmaKuvaus "testiryhmä",
                 :yhteystieto
                 [{:yhteystietoArvo "kayttaja@domain.local",
                   :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})})
           {:oid "1.2.246.562.24.78058065184"
            :first-names "Vapautettu"
            :surname "Maksullinen"
            :common-name "Testi"
            :contact-values-group
            '({:id 0
               :contact [{:value "kayttaja@domain.local"
                          :type "YHTEYSTIETO_SAHKOPOSTI"}]})}))))
