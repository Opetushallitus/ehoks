(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]))

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
              :suorituspvm s/Inst
              :tyyppi (s/enum :arvioijan-kautta :nayton-kautta)
              (s/optional-key :tunniste) (s/maybe KoodistoKoodi)
              (s/optional-key :yto-koodi) (s/maybe s/Str)
              (s/optional-key :liitteet) [s/Str]})

(s/defschema TukevaOpinto
             "Opiskeluvalmiuksia tukevat opinnot"
             {:nimi s/Str
              :kuvaus s/Str
              :kesto-paivina s/Int
              :alku s/Inst
              :loppu s/Inst})



(s/defschema PuuttuvaOsaaminen
             "Puuttuva osaaminen"
             {:tutkinnon-osan-koodi s/Str
              :tutkinnon-koodi s/Str
              :vastaava-ohjaaja s/Str
              :osaamisen-hankkimistavat
              [{:alku s/Inst
                :loppu s/Inst
                :osaamisen-hankkimistapa
                {:tunniste KoodistoKoodi}}]

              :sisalto s/Str
              :organisaatio {:nimi s/Str
                             :y-tunnus s/Str}
              :keskeiset-tehtavat [s/Str]

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
