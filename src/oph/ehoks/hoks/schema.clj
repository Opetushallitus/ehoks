(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]
            [schema-tools.core :as st]
            [oph.ehoks.schema-tools :refer [describe]])
  (:import (java.time LocalDate)))

(s/defschema
  Organisaatio
  (describe
    "Organisaatio"
    :nimi s/Str "Organisaation nimi"
    (s/optional-key :y-tunnus) s/Str "Organisaation y-tunnus"))

(s/defschema
  KoodiMetadata
  (describe
    "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"
    (s/optional-key :nimi) (s/maybe s/Str) "Koodisto-koodin nimi"
    (s/optional-key :lyhyt-nimi) (s/maybe s/Str) "Koodisto-koodin lyhyt nimi"
    (s/optional-key :kuvaus) (s/maybe s/Str) "Koodisto-koodin kuvaus"
    :kieli s/Str "Koodisto-koodin kieli"))

(s/defschema
  KoodistoKoodi
  (describe
    "Koodisto-koodi"
    :koodi-arvo s/Str "Koodisto-koodin arvo"
    :koodi-uri s/Str "Koodiston URI"
    :versio s/Int "Koodisto-koodin versio"
    (s/optional-key :metadata) [KoodiMetadata]
    "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"))

(s/defschema
  TutkinnonOsa
  (describe
    "Tutkinnon osa"
    :tunniste KoodistoKoodi "Koodisto-koodi"
    :laajuus s/Int "Tutkinnon laajuus"
    :eperusteet-diaarinumero s/Str "Diaarinumero ePerusteet-palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :koulutustyyppi KoodistoKoodi
    "Tutkinnon osan koulutustyypin Koodisto-koodi"))

(s/defschema
  YhteinenTutkinnonOsa
  (st/merge
    (describe
      "Yhteinen tutkinnon osa (YTO)"
      :osa-alue-tunniste KoodistoKoodi
      "Tutkinnon osan osa-alueen tunnisteen Koodisto-koodi")
    TutkinnonOsa))

(s/defschema
  MuuTutkinnonOsa
  (describe
    "Muu tutkinnon osa (ei ePerusteet-palvelussa)"
    :nimi s/Str "Tutkinnon osan nimi"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :laajuus s/Int "Tutkinnon osan laajuus osaamispisteissä"
    :kesto s/Int "Tutkinnon osan kesto päivinä"
    :suorituspvm LocalDate "Tutkinnon suorituspäivä muodossa YYYY-MM-DD"))

(s/defschema
  Henkilo
  (describe
    "Henkilö"
    :organisaatio Organisaatio "Henkilön organisaatio"
    :nimi s/Str "Henkilön nimi"
    :rooli s/Str "Henkilön rooli"))

(s/defschema
  DateRange
  (describe
    "Aikaväli"
    :alku LocalDate "Alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Loppupäivämäärä muodossa YYYY-MM-DD"))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnot
  (describe
    "Opiskeluvalmiuksia tukevat opinnot"
    :nimi s/Str "Opintojen nimi"
    :kuvaus s/Str "Opintojen kuvaus"
    :kesto s/Int "Opintojen kesto päivinä"
    :ajankohta DateRange "Opintojen ajoittuminen"))

(s/defschema
  Opinnot
  (describe
    "Opinnot"
    (s/optional-key :ammatilliset-opinnot) [TutkinnonOsa]
    "Osaamisen ammattilliset opinnot"
    (s/optional-key :yhteiset-tutkinnon-osat) [YhteinenTutkinnonOsa]
    "Osaamisen yhteiset tutkinnon osat (YTO)"
    (s/optional-key :muut-osaamiset) [MuuTutkinnonOsa]
    "Muut osaamisen opinnot"))

(s/defschema
  TunnustettavanaOlevaOsaaminen
  (st/merge
    (describe
     "Osaaminen, joka on toimitettu arvioijille osaamisen tunnustamista varten"
     :todentajan-nimi s/Str
     "Osaamisen todentaneen toimivaltaisen viranomaisen nimi")
    Opinnot))

(s/defschema
  OlemassaOlevaOsaaminen
  (describe
    (str "Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi "
         "opiskelijan tutkintoa")
    (s/optional-key :tunnustettu-osaaminen) Opinnot "Tunnustettu osaaminen"
    (s/optional-key :aiempi-tunnustettava-osaaminen) Opinnot
    "Aiempi tunnustettava osaaminen"
    (s/optional-key :tunnustettavana-olevat) TunnustettavanaOlevaOsaaminen
    "Tunnustettavana oleva osaaminen"
    (s/optional-key :muut-opinnot) Opinnot "Muu olemassa oleva osaaminen"
    (s/optional-key :muut-arvioidut-ja-todennetut-tunnustettavat-opinnot)
    MuuTutkinnonOsa
    "Muut arvioidut ja todennetut tunnustettavat opinnot"))

(s/defschema
  Oppimisymparisto
  (describe
    "Oppimisympäristö"
    :paikka s/Str "Oppimisympäristön paikan nimi"
    :ajankohta DateRange "Ajankohta kyseisessä oppimisympäristössä"))

(s/defschema
  TyopaikallaHankittavaOsaaminen
  (describe
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    :ajankohta DateRange "Työpaikalla järjestettävän koulutuksen ajoittuminen"
    (s/optional-key :muut-oppimisymparistot) [Oppimisymparisto]
    "Muissa oppimisympäristöissä tapahtuvat osaamisen hankkimiset"
    :hankkijan-edustaja Henkilo
    "Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja"
    :vastuullinen-ohjaaja Henkilo "Vastuullinen työpaikkaohjaaja"
    :jarjestajan-edustaja Henkilo "Koulutuksen järjestäjän edustaja"
    (s/optional-key :muut-osallistujat) [Henkilo]
    "Muut ohjaukseen osallistuvat henkilöt"
    :keskeiset-tyotehtavat [s/Str] "Keskeiset työtehtävät"
    :ohjaus-ja-tuki s/Bool
    "Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta"
    :erityinen-tuki s/Bool
    (str "Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä "
         "erityisen tuen päätös")
    (s/optional-key :erityisen-tuen-aika) DateRange
    (str "Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon tai"
         "koulutuksen osassa")))

(s/defschema
  OsaamisenHankkimistapa
  (describe
    "Osaamisen hankkimisen tapa"
    :ajankohta DateRange "Hankkimisen ajankohta"
    :osaamisen-hankkimistavan-tunniste KoodistoKoodi
    "Osaamisen hankkimisen Koodisto-koodi (URI: osaamisenhankkimistapa)"))

(s/defschema
  PuuttuvanOsaamisenTiedot
  (describe
     "Puuttuvan osaamisen hankkimisen suunnitelman tiedot"
     :osaamisen-hankkimistapa OsaamisenHankkimistapa
     "Osaamisen hankkimistavat"
     :koulutuksen-jarjestaja-oid s/Str
     (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
          "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
          "koulutuksen järjestäjän oid.")
     :tarvittava-opetus s/Str "Tarvittava opetus"
     (s/optional-key :tyopaikalla-hankittava-osaaminen)
     TyopaikallaHankittavaOsaaminen
     "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"))

(s/defschema
  PuuttuvaAmmatillinenOsaaminen
  (st/merge
    (describe
     "Puuttuvan ammatillisen osaamisen tiedot"
     :tutkinnon-osa TutkinnonOsa "Tutkinnon osa"
     (s/optional-key :tutkinnon-osa-josta-poiketaan) TutkinnonOsa
     "Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen")
    PuuttuvanOsaamisenTiedot))

(s/defschema
  PuuttuvaYTO
  (st/merge
    (describe
     "Puuttuvan yhteinen tutkinnon osan tiedot"
     :tutkinnon-osa YhteinenTutkinnonOsa "Tutkinnon osa"
     (s/optional-key :tutkinnon-osa-josta-poiketaan) YhteinenTutkinnonOsa
     "Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen")
    PuuttuvanOsaamisenTiedot))

(s/defschema
  PuuttuvaOsaaminen
  (describe
    "Puuttuvan osaamisen hankkimisen suunnitelma"
    :ammatillinen-osaaminen [PuuttuvaAmmatillinenOsaaminen]
    "Puuttuvan ammatillisen osaamisen hankkimisen tiedot"
    :yhteinen-tutkinnon-osa [PuuttuvaYTO]
    "Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot"))

(s/defschema
  NaytonJarjestaja
  (describe
    "Näytön tai osaamisen osoittamisen järjestäjä"
    :nimi s/Str "Näytön tai osaamisen osoittamisen järjestäjän nimi"
    :oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  Arvioija
  (describe
    "Arvioija"
    :nimi s/Str "Arvioijan nimi"
    :rooli KoodistoKoodi "Arvioijan roolin Koodisto-koodi"
    :organisaatio Organisaatio "Arvioijan organisaatio"))

(s/defschema
  Arviointikriteeri
  (describe
    "Arviointikriteeri"
    :arvosana s/Int "Arvosana"
    :kuvaus s/Str "Arviointikriteerin kuvaus"))

(s/defschema
  HankitunOsaamisenNaytto
  (describe
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :jarjestaja NaytonJarjestaja "Näytön tai osaamisen osoittamisen järjestäjä"
    :nayttoymparisto Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    :kuvaus s/Str
    (str "Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen "
         "näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, "
         "esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, "
         "simulaattori")
    :ajankohta DateRange "Näytön tai osaamisen osoittamisen ajankohta"
    :sisalto s/Str "Näytön tai osaamisen osoittamisen sisältö tai työtehtävät"
    :ammattitaitovaatimukset [KoodistoKoodi]
    "Ammattitaitovaatimukset, jonka arvioinnin kriteereitä mukautetaan"
    :osaamistavoitteet [KoodistoKoodi]
    "Osaamistavoitteet, jonka arvioinnin kriteereitä mukautetaan"
    :arvioijat [Arvioija] "Näytön tai osaamisen osoittamisen arvioijat"
    :yksilölliset-arviointikriteerit [Arviointikriteeri]
    "Yksilölliset arvioinnin kriteerit"))

(s/defschema
  HOKS
  (describe
    "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti"
    :id s/Int "tunniste eHOKS-järjestelmässä"
    :opiskeluoikeus-oid s/Str
    "Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä."
    (s/optional-key :urasuunnitelma) KoodistoKoodi
    "Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi"
    :versio s/Int "HOKS-dokumentin versio"
    :luonut s/Str "HOKS-dokumentin luoneen henkilön nimi"
    :paivittanyt s/Str "HOKS-dokumenttia viimeksi päivittäneen henkilön nimi"
    :hyvaksynyt s/Str "Luodun HOKS-dokumentn hyväksyjän nimi"
    :luotu s/Inst "HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"
    :hyvaksytty s/Inst
    "HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"
    :paivitetty s/Inst
    "HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"
    :olemassa-oleva-osaaminen OlemassaOlevaOsaaminen
    (str "Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi "
         "opiskelijan tutkintoa")
    ; OSAAMISEN TUNNISTAMIS- JA TUNNUSTAMISPROSESSIN LOPPUTULOS
    :opiskeluvalmiuksia-tukevat-opinnot OpiskeluvalmiuksiaTukevatOpinnot
    "Opiskeluvalmiuksia tukevat opinnot"
    :puuttuva-osaaminen PuuttuvaOsaaminen
    "Puuttuvan osaamisen hankkimisen suunnitelma"
    :hankitun-osaamisen-naytto HankitunOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"))

(s/defschema
  HOKSArvot
  (st/merge
    (describe
      "HOKS-dokumentin arvot uutta merkintää luotaessa")
    (st/dissoc HOKS :id :versio :luotu :paivitetty)))
