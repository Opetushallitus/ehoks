-(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s])
  (:import (java.time LocalDate)))

(def KoskiDate LocalDate)

(s/defschema Organisaatio
             "Organisaatio"
             {:nimi s/Str
              :y-tunnus s/Str})

(s/defschema KoodistoKoodi
             "Koodisto-koodi"
             {:koodisto-koodi s/Str
              :koodisto-uri s/Str
              :versio s/Int})

(s/defschema TutkinnonOsa
             "Tutkinnon osa"
             {:tunniste KoodistoKoodi
              :laajuus s/Int})

(s/defschema Henkilo
             "Henkilö"
             {:organisaatio Organisaatio
              :oid s/Str
              :nimi s/Str
              :rooli s/Str})

(s/defschema DateRange
             "Aikaväli"
             {:alku KoskiDate
              :loppu KoskiDate})

(s/defschema OlemassaOlevaOsaaminen
             "Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi
              opiskelijan tutkintoa"
             {:tutkinnon-osa TutkinnonOsa
              :tutkinnon-diaarinumero s/Str})

(s/defschema MuuTodennettuOsaaminen
             "Muu opiskelijan aiemmin hankkima ja osoittama osaaminen, joka
              liittyy suoritettavaan tutkintoon tai valmentavaan koulutukseen"
             {:tutkinnon-osa TutkinnonOsa
              :kuvaus s/Str
              :liitteet [s/Str]})

(s/defschema TukevaOpinto
             "Opiskeluvalmiuksia tukevat opinnot"
             {:nimi s/Str
              :kuvaus s/Str
              :kesto-paivina s/Int
              :ajankohta DateRange})

(s/defschema PuuttuvaOsaaminen
             "Puuttuva osaaminen"
             {:tutkinnon-osa TutkinnonOsa
              :poikkeama {:alkuperainen-tutkinnon-osa TutkinnonOsa
                          :kuvaus s/Str}
              :osaamisen-hankkimistavat
              [{:ajankohta DateRange
                :osaamisen-hankkimistavan-tunniste KoodistoKoodi}]
              :ajankohta DateRange
              :koulutuksen-jarjestaja-oid s/Str
              :tarvittava-opetus s/Str})

(s/defschema TyopaikallaHankittavaOsaaminen
             "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
             {:ajankohta DateRange
              :muut-oppimisymparistot [{:paikka s/Str
                                        :ajankohta DateRange}]
              :hankkijan-edustaja Henkilo
              :vastuullinen-ohjaaja Henkilo
              :jarjestajan-edustaja Henkilo
              :muut-osallistujat [Henkilo]
              :keskeiset-tehtavat [s/Str]
              :ohjaus-ja-tuki s/Bool
              :erityinen-tuki s/Bool
              :erityisen-tuen-aika {:alku KoskiDate
                                    :loppu KoskiDate}})

(s/defschema HankitunOsaamisenNaytto
             "Hankitun osaamisen osoittaminen"
             {:jarjestaja {:nimi s/Str
                           :oid s/Str}
              :nayttoymparisto Organisaatio
              :kuvaus s/Str
              :ajankohta DateRange
              :sisalto s/Str
              :ammattitaitovaatimukset [KoodistoKoodi]
              :osaamistavoitteet [KoodistoKoodi]
              :arvioijat [{:nimi s/Str
                           :rooli KoodistoKoodi
                           :organisaatio Organisaatio}]
              :arviointikriteerit [{:arvosana s/Int
                                    :kuvaus s/Str}]})

(s/defschema HOKSArvot
             "HOKS arvot uuden HOKSin luomiseen"
             {:opiskeluoikeus-oid s/Str
              :urasuunnitelma KoodistoKoodi})

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
                :olemassa-olevat-osaamiset [OlemassaOlevaOsaaminen]
                :muut-todennetut-osaamiset [MuuTodennettuOsaaminen]
                :tukevat-opinnot [TukevaOpinto]
                :puuttuvat-osaamiset [PuuttuvaOsaaminen]
                :tyopaikalla-hankittavat-osaamiset
                [TyopaikallaHankittavaOsaaminen]
                :osaamisen-osoittamiset [HankitunOsaamisenNaytto]}))
