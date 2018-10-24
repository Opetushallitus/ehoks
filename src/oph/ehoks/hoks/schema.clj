(ns oph.ehoks.hoks.schema
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
              :laajuus s/Int
              :eperusteet-diaarinumero s/Str
              :kuvaus s/Str
              :koulutustyyppi KoodistoKoodi})

(s/defschema YTOTutkinnonOsa
             "YTO tutkinnon osa"
             (assoc TutkinnonOsa :osa-alue-tunniste KoodistoKoodi))

(s/defschema MuuTutkinnonOsa
             "Muu tutkinnon osa"
             {:nimi s/Str
              :kuvaus s/Str
              :laajuus s/Int
              :kesto s/Int
              :suorituspvm KoskiDate})

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

(s/defschema OpiskeluvalmiuksiaTukevatOpinnot
             "Opiskeluvalmiuksia tukevat opinnot"
             MuuTutkinnonOsa)

(s/defschema OlemassaOlevaOsaaminen
             "Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi
              opiskelijan tutkintoa"
             {:tunnustettu-osaaminen {:ammatilliset-opinnot [TutkinnonOsa]
                                      :yhteiset-tutkinnon-osat [YTOTutkinnonOsa]
                                      :muut-osaamiset [MuuTutkinnonOsa]}
              :aiempi-tunnustettava-osaaminen
              {:ammatilliset-opinnot [TutkinnonOsa]
               :yhteiset-tutkinnon-osat [YTOTutkinnonOsa]
               :muut-osaamiset [MuuTutkinnonOsa]}
              :tunnustettavana-olevat
              {:ammatilliset-opinnot [TutkinnonOsa]
               :yhteiset-tutkinnon-osat [YTOTutkinnonOsa]
               :muut-osaamiset [MuuTutkinnonOsa]}
              :muut-opinnot {:ammatilliset-opinnot [TutkinnonOsa]
                             :yhteiset-tutkinnon-osat [YTOTutkinnonOsa]
                             :muut-osaamiset [MuuTutkinnonOsa]}})

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

(s/defschema PuuttuvaOsaaminen
             "Puuttuva osaaminen"
             {:ammatilliset-opinnot [TutkinnonOsa]
              :yhteiset-tutkinnon-osat [YTOTutkinnonOsa]
              :muut [TutkinnonOsa]
              :poikkeama {:alkuperainen-tutkinnon-osa TutkinnonOsa
                          :kuvaus s/Str}
              :osaamisen-hankkimistavat
              [{:ajankohta DateRange
                :osaamisen-hankkimistavan-tunniste KoodistoKoodi}]
              :ajankohta DateRange
              :koulutuksen-jarjestaja-oid s/Str
              :tarvittava-opetus s/Str
              :tyopaikalla-hankittava-osaaminen TyopaikallaHankittavaOsaaminen})



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

(s/defschema HOKS
             "HOKS"
             {:id s/Int
              :opiskeluoikeus-oid s/Str
              :urasuunnitelma KoodistoKoodi
              :versio s/Int
              :luojan-oid s/Str
              :paivittajan-oid s/Str
              :luonnin-hyvaksyjan-oid s/Str
              :paivityksen-hyvaksyjan-oid s/Str
              :luotu s/Inst
              :hyvaksytty s/Inst
              :paivitetty s/Inst
              :olemassa-oleva-osaaminen OlemassaOlevaOsaaminen
              ; OSAAMISEN TUNNISTAMIS- JA TUNNUSTAMISPROSESSIN LOPPUTULOS
              :opiskeluvalmiuksia-tukevat-opinnot
              OpiskeluvalmiuksiaTukevatOpinnot
              :puuttuva-osaaminen PuuttuvaOsaaminen
              :hankitun-osaamisen-naytto HankitunOsaamisenNaytto})

(s/defschema HOKSArvot
             "HOKS arvot uuden HOKSin luomiseen"
             (dissoc HOKS :id))
