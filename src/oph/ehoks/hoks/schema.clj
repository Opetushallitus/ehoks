(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s])
  (:import (java.time LocalDate)))

(def KoskiDate LocalDate)

(s/defschema KoodistoKoodi
             "Koodisto-koodi"
             {:koodisto-koodi s/Str
              :koodisto-uri s/Str
              :versio s/Int})

(s/defschema TodennettuOsaaminen
             "Todennettu osaaminen"
             {:tunniste KoodistoKoodi
              (s/optional-key :liitteet) [s/Str]})

(s/defschema TodentamatonOsaaminen
             "Todentamaton osaaminen"
             {:nimi s/Str
              :kuvaus s/Str
              :laajuus s/Str
              :kesto s/Str
              :suorituspvm KoskiDate
              :lahde (s/enum :arvioija :naytto)
              :koulutusmoduuli KoulutusModuuli
              (s/optional-key :liitteet) [s/Str]})

(s/defschema TukevaOpinto
             "Opiskeluvalmiuksia tukevat opinnot"
             {:nimi s/Str
              :kuvaus s/Str
              :kesto-paivina s/Int
              :alku KoskiDate
              :loppu KoskiDate})

(s/defschema KoulutusModuuli
             "Koulutusmoduuli"
             {:tunniste KoodistoKoodi
              :kieli KoodistoKoodi
              :pakollinen s/Bool
              :laajuus s/Int})

(s/defschema PuuttuvaOsaaminen
             "Puuttuva osaaminen"
             {:koulutusmoduuli KoulutusModuuli
              :vastaava-ohjaaja s/Str
              :osaamisen-hankkimistavat
              [{:alku KoskiDate
                :loppu KoskiDate
                :osaamisen-hankkimistapa
                {:tunniste KoodistoKoodi}}]

              :sisalto s/Str
              :organisaatio {:nimi s/Str
                             :y-tunnus s/Str}
              :keskeiset-tehtavat [s/Str]

              :tyyppi KoodistoKoodi

              :ohjaus-ja-tuki s/Bool
              :erityinen-tuki s/Bool})

(s/defschema HOKSArvot
             "HOKS arvot uuden HOKSin luomiseen"
             {:opiskeluoikeus-oid s/Str
              :urasuunnitelma s/Str})

(s/defschema HOKS
             "HOKS"
             (merge
               HOKSArvot
               {:id s/Int
                :versio s/Int
                :luojan-oid s/Str
                :paivittajan-oid s/Str
                :luonnin-hyvaksyjan-oid s/Str
                :paivityksen-hyvaksyjan-oid s/Str
                :luotu s/Inst
                :hyvaksytty s/Inst
                :paivitetty s/Inst
                :todennetut-osaamiset [TodennettuOsaaminen]
                :todentamattomat-osaamiset [TodentamatonOsaaminen]
                :tukevat-opinnot [TukevaOpinto]
                :puuttuvat-osaamiset [PuuttuvaOsaaminen]}))
