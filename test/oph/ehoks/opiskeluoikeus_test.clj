(ns oph.ehoks.opiskeluoikeus-test
  (:require [clojure.test :refer [are deftest testing]]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]))

(def opiskeluoikeus-1
  "Opiskeluoikeus with ammatillinen suoritus"
  {:suoritukset     [{:tyyppi          {:koodiarvo "ammatillinentutkinto"}
                      :suorituskieli   {:koodiarvo "fi"}
                      :tutkintonimike  [{:koodiarvo "12345"}
                                        {:koodiarvo "23456"}]
                      :toimipiste      {:oid "1.2.246.562.10.12312312312"}
                      :koulutusmoduuli {:tunniste {:koodiarvo 351407}}}]
   :tyyppi          {:koodiarvo "ammatillinenkoulutus"}
   :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})

(def opiskeluoikeus-2
  "Opiskeluoikeus without ammatillinen suoritus"
  {:suoritukset [{:tyyppi {:koodiarvo "joku_muu"}}]
   :tyyppi      {:koodiarvo "ammatillinenkoulutus"}})

(def opiskeluoikeus-3
  "Opiskeluoikeus that is linked to another opiskeluoikeus"
  {:suoritukset               [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
   :tyyppi                    {:koodiarvo "ammatillinenkoulutus"}
   :sisältyyOpiskeluoikeuteen {:oid "1.2.246.562.15.10000000009"}})

(def opiskeluoikeus-4
  "Opiskeluoikeus that has opintojenRahoitus koodi preventing tyoelamapalaute
  initialization."
  {:tila {:opiskeluoikeusjaksot [{:tila {:koodiarvo "lasna"}
                                  :opintojenRahoitus {:koodiarvo "14"}
                                  :alku "2023-08-10"
                                  :loppu "2023-12-31"}
                                 {:tila {:koodiarvo "lasna"}
                                  :opintojenRahoitus {:koodiarvo "1"}
                                  :alku "2024-01-01"}]}
   :suoritukset     [{:tyyppi          {:koodiarvo "ammatillinentutkinto"}
                      :suorituskieli   {:koodiarvo "fi"}
                      :tutkintonimike  [{:koodiarvo "12345"}
                                        {:koodiarvo "23456"}]
                      :toimipiste      {:oid "1.2.246.562.10.12312312312"}
                      :koulutusmoduuli {:tunniste {:koodiarvo 351407}}}]
   :tyyppi          {:koodiarvo "ammatillinenkoulutus"}
   :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})

(def opiskeluoikeus-5
  "Opiskeluoikeus that is in terminal state."
  {:tila {:opiskeluoikeusjaksot [{:tila {:koodiarvo "lasna"}
                                  :alku "2022-08-10"
                                  :loppu "2022-12-31"}
                                 {:tila {:koodiarvo "valiaikaisestikeskeytynyt"}
                                  :alku "2023-01-01"}]}
   :suoritukset     [{:tyyppi          {:koodiarvo "ammatillinentutkinto"}
                      :suorituskieli   {:koodiarvo "fi"}
                      :tutkintonimike  [{:koodiarvo "12345"}
                                        {:koodiarvo "23456"}]
                      :toimipiste      {:oid "1.2.246.562.10.12312312312"}
                      :koulutusmoduuli {:tunniste {:koodiarvo 351407}}}]
   :tyyppi          {:koodiarvo "ammatillinenkoulutus"}
   :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})

(deftest test-in-terminal-state?
  (testing "Opiskeluoikeuden tilan tarkastus. Keskeytetty opiskeluoikeus estää
           jakson käsittelyn. Jakson päättymispäivänä keskeytetty opiskeluoikeus
           ei estä jakson käsittelyä."
    (let [loppupvm "2021-09-07"
          opiskeluoikeus-lasna {:tila
                                {:opiskeluoikeusjaksot
                                 [{:alku "2021-06-20"
                                   :tila {:koodiarvo "loma"}}
                                  {:alku "2021-05-01"
                                   :tila {:koodiarvo "lasna"}}
                                  {:alku "2021-06-25"
                                   :tila {:koodiarvo "lasna"}}]}}
          opiskeluoikeus-eronnut-samana-paivana {:tila
                                                 {:opiskeluoikeusjaksot
                                                  [{:alku "2021-06-20"
                                                    :tila {:koodiarvo "loma"}}
                                                   {:alku "2021-05-01"
                                                    :tila {:koodiarvo "lasna"}}
                                                   {:alku "2021-09-07"
                                                    :tila
                                                    {:koodiarvo "eronnut"}}]}}
          opiskeluoikeus-eronnut-tulevaisuudessa {:tila
                                                  {:opiskeluoikeusjaksot
                                                   [{:alku "2021-06-20"
                                                     :tila {:koodiarvo "loma"}}
                                                    {:alku "2021-05-01"
                                                     :tila {:koodiarvo "lasna"}}
                                                    {:alku "2021-09-08"
                                                     :tila
                                                     {:koodiarvo "eronnut"}}]}}
          opiskeluoikeus-eronnut-paivaa-aiemmin {:tila
                                                 {:opiskeluoikeusjaksot
                                                  [{:alku "2021-06-20"
                                                    :tila {:koodiarvo "loma"}}
                                                   {:alku "2021-05-01"
                                                    :tila {:koodiarvo "lasna"}}
                                                   {:alku "2021-09-06"
                                                    :tila
                                                    {:koodiarvo "eronnut"}}]}}]
      (are [oo expected] (= expected
                            (opiskeluoikeus/in-terminal-state? oo loppupvm))
        opiskeluoikeus-lasna false
        opiskeluoikeus-eronnut-samana-paivana false
        opiskeluoikeus-eronnut-tulevaisuudessa false
        opiskeluoikeus-eronnut-paivaa-aiemmin true))))
