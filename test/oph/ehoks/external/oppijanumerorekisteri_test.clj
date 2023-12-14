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

(def student-info {:oppijanumero "1.2.246.562.24.62444477771",
                   :etunimet "Pauliina Joku",
                   :asiointiKieli {:kieliKoodi "fi", :kieliTyyppi "suomi"},
                   :syntymaaika "1975-04-20",
                   :aidinkieli {:kieliKoodi "fi", :kieliTyyppi "suomi"},
                   :sukunimi "Pouta",
                   :kansalaisuus [{:kansalaisuusKoodi "246"}],
                   :hetu "200475-0000",
                   :oidHenkilo "1.2.246.562.24.62444477771",
                   :kutsumanimi "Pauliina",
                   :yhteystiedotRyhma
                   [{:id 118888872,
                     :ryhmaKuvaus "yhteystietotyyppi4",
                     :ryhmaAlkuperaTieto "alkupera1",
                     :yhteystieto
                     [{:yhteystietoTyyppi "YHTEYSTIETO_KATUOSOITE",
                       :yhteystietoArvo "Mannerheimintie 20 E 15"}
                      {:yhteystietoTyyppi "YHTEYSTIETO_KAUPUNKI",
                       :yhteystietoArvo "HELSINKI"}
                      {:yhteystietoTyyppi "YHTEYSTIETO_MAA",
                       :yhteystietoArvo "Suomi"}
                      {:yhteystietoTyyppi "YHTEYSTIETO_POSTINUMERO",
                       :yhteystietoArvo "00820"}]}
                    {:id 118888877,
                     :ryhmaKuvaus "yhteystietotyyppi8",
                     :readOnly true,
                     :yhteystieto [{:yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI",
                                    :yhteystietoArvo "testi.maili@gmail.com"}]}
                    {:id 155888715,
                     :ryhmaKuvaus "yhteystietotyyppi2",
                     :ryhmaAlkuperaTieto "alkupera6",
                     :yhteystieto
                     [{:yhteystietoTyyppi "YHTEYSTIETO_KUNTA",
                       :yhteystietoArvo nil}
                      {:yhteystietoTyyppi "YHTEYSTIETO_MATKAPUHELINNUMERO",
                       :yhteystietoArvo nil}
                      {:yhteystietoTyyppi "YHTEYSTIETO_KATUOSOITE",
                       :yhteystietoArvo nil}
                      {:yhteystietoTyyppi "YHTEYSTIETO_POSTINUMERO",
                       :yhteystietoArvo nil}
                      {:yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI",
                       :yhteystietoArvo "testi.maili@oph.fi"}
                      {:yhteystietoTyyppi "YHTEYSTIETO_PUHELINNUMERO",
                       :yhteystietoArvo nil}]}]})

(def converted-student-info {:oid "1.2.246.562.24.62444477771"
                             :first-name "Pauliina Joku"
                             :surname "Pouta"
                             :common-name "Pauliina"
                             :contact-values-group
                             '({:id 118888872
                                :contact [{:value "Mannerheimintie 20 E 15"
                                           :type "YHTEYSTIETO_KATUOSOITE"}
                                          {:value "HELSINKI"
                                           :type "YHTEYSTIETO_KAUPUNKI"}
                                          {:value "Suomi"
                                           :type "YHTEYSTIETO_MAA"}
                                          {:value "00820"
                                           :type "YHTEYSTIETO_POSTINUMERO"}]}
                                {:id 118888877
                                 :contact [{:value "testi.maili@gmail.com"
                                            :type "YHTEYSTIETO_SAHKOPOSTI"}]}
                                {:id 155888715
                                 :contact [{:value "testi.maili@oph.fi"
                                            :type "YHTEYSTIETO_SAHKOPOSTI"}]})})

(deftest convert-nil-contact-info
  (testing "nil contact values should be pruned"
    (is (= (onr/convert-student-info student-info)
           converted-student-info))))

(deftest convert-all-nil-contact-info
  (testing "contact group containg only nil contact values should be pruned"
    (is (= (onr/convert-student-info
             {:oppijanumero "1.2.246.562.24.62444477771",
              :etunimet "Pauliina Joku",
              :sukunimi "Pouta",
              :oidHenkilo "1.2.246.562.24.62444477771",
              :kutsumanimi "Pauliina",
              :yhteystiedotRyhma
              [{:id 118888872,
                :yhteystieto [{:yhteystietoTyyppi "YHTEYSTIETO_KATUOSOITE",
                               :yhteystietoArvo "Mannerheimintie 20 E 15"}
                              {:yhteystietoTyyppi "YHTEYSTIETO_POSTINUMERO",
                               :yhteystietoArvo "00820"}]}
               {:id 118888877,
                :yhteystieto [{:yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI",
                               :yhteystietoArvo "testi.maili@gmail.com"}]}
               {:id 155888715,
                :yhteystieto [{:yhteystietoTyyppi "YHTEYSTIETO_KUNTA",
                               :yhteystietoArvo nil}
                              {:yhteystietoTyyppi "YHTEYSTIETO_PUHELINNUMERO",
                               :yhteystietoArvo nil}]}]})
           {:oid "1.2.246.562.24.62444477771"
            :first-name "Pauliina Joku"
            :surname "Pouta"
            :common-name "Pauliina"
            :contact-values-group
            '({:id 118888872
               :contact [{:value "Mannerheimintie 20 E 15"
                          :type "YHTEYSTIETO_KATUOSOITE"}
                         {:value "00820"
                          :type "YHTEYSTIETO_POSTINUMERO"}]}
               {:id 118888877
                :contact [{:value "testi.maili@gmail.com"
                           :type "YHTEYSTIETO_SAHKOPOSTI"}]})}))))
