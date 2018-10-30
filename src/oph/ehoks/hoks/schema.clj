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
    :y-tunnus s/Str "Organisaation y-tunnus"))

(s/defschema
  KoodistoKoodi
  (describe
    "Koodisto-koodi"
    :koodi-arvo s/Str "Koodisto-koodin arvo"
    :koodisto-koodi s/Str "Koodiston koodi"
    :koodisto-uri s/Str "Koodiston URI"
    :versio s/Int "Koodisto-koodin versio"))

(s/defschema
  TutkinnonOsa
  (describe
    "Tutkinnon osa"
    :tunniste KoodistoKoodi "Koodisto-koodi"
    :laajuus s/Int "Tutkinnon laajuus"
    :eperusteet-diaarinumero s/Str "Diaarinumero ePerusteet-palvelussa"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :koulutustyyppi KoodistoKoodi
    "Tutkinnon osan koulutustyypin Koodisto-koodi"))

(s/defschema
  YhteinenTutkinnonOsa
  (st/merge
    (describe
      "YTO tutkinnon osa"
      :osa-alue-tunniste KoodistoKoodi
      "Tutkinnon osan osa-alueen tunnisteen Koodisto-koodi")
    TutkinnonOsa))

(s/defschema
  MuuTutkinnonOsa
  (describe
    "Muu tutkinnon osa (ei ePerusteet-palvelussa)"
    :nimi s/Str "Tutkinnon osan nimi"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :laajuus s/Int "Tutkinnon osan laajuus osaamispisteiss'"
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
  "Opiskeluvalmiuksia tukevat opinnot"
  MuuTutkinnonOsa)

(s/defschema
  Opinnot
  (describe
    "Opinnot"
    :ammatilliset-opinnot [TutkinnonOsa] "Osaamisen ammattilliset opinnot"
    :yhteiset-tutkinnon-osat [YhteinenTutkinnonOsa]
    "Osaamisen yhteiset tutkinnon osat (YTO)"
    :muut-osaamiset [MuuTutkinnonOsa] "Muut osaamisen opinnot"))

(s/defschema
  TunnustettavanaOlevaOsaaminen
  (describe
    "Osaaminen, joka on toimitettu arvioijille osaamisen tunnustamista varten"
    :ammatilliset-opinnot [TutkinnonOsa] "Osaamisen ammattilliset opinnot"
    :yhteiset-tutkinnon-osat [YhteinenTutkinnonOsa]
    "Osaamisen yhteiset tutkinnon osat (YTO)"
    :muut-osaamiset [MuuTutkinnonOsa] "Muut osaamisen opinnot"
    :todentajan-nimi s/Str
    "Osaamisen todentaneen toimivaltaisen viranomaisen nimi"))

(s/defschema
  OlemassaOlevaOsaaminen
  (describe
    (str "Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi "
         "opiskelijan tutkintoa")
    :tunnustettu-osaaminen Opinnot "Tunnustettu osaaminen"
    :aiempi-tunnustettava-osaaminen Opinnot "Aiempi tunnustettava osaaminen"
    :tunnustettavana-olevat TunnustettavanaOlevaOsaaminen
    "Tunnustettavana oleva osaaminen"
    :muut-opinnot Opinnot "Muu olemassa oleva osaaminen"
    :muut-arvioidut-ja-todennetut-tunnustettavat-opinnot MuuTutkinnonOsa
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
    :muut-oppimisymparistot [Oppimisymparisto]
    "Muissa oppimisympäristöissä tapahtuvat osaamisen hankkimiset"
    :hankkijan-edustaja Henkilo
    "Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja"
    :vastuullinen-ohjaaja Henkilo "Vastuullinen työpaikkaohjaaja"
    :jarjestajan-edustaja Henkilo "Koulutuksen järjestäjän edustaja"
    :muut-osallistujat [Henkilo] "Muut ohjaukseen osallistuvat henkilöt"
    :keskeiset-tyotehtavat [s/Str] "Keskeiset työtehtävät"
    :ohjaus-ja-tuki s/Bool
    "Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta"
    :erityinen-tuki s/Bool
    (str "Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä "
         "erityisen tuen päätös")

    :erityisen-tuen-aika DateRange
    (str "Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon tai"
         "koulutuksen osassa")))

(s/defschema
  PuuttuvanOsaamisenPoikkeama
  (describe
    "Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen"
    :alkuperainen-tutkinnon-osa TutkinnonOsa
    "Tutkinnon osa, johon poikkeus pohjautuu"
    :kuvaus s/Str "Poikkeaman kuvaus"))

(s/defschema
  OsaamisenHankkimistapa
  (describe
    "Osaamisen hankkimisen tapa"
    :ajankohta DateRange "Hankkimisen ajankohta"
    :osaamisen-hankkimistavan-tunniste KoodistoKoodi
    "Osaamisen hankkimisen Koodisto-koodi"))

(s/defschema
  PuuttuvaOsaaminen
  (describe
    "Puuttuvan osaamisen hankkimisen suunnitelma"
    :ammatilliset-opinnot [TutkinnonOsa] "Ammatilliset opinnot"
    :yhteiset-tutkinnon-osat [YhteinenTutkinnonOsa] "Yhteiset tutkinnon osat"
    :muut [MuuTutkinnonOsa] "Muut tutkinnon osaa pienemmät osaamiskokonaisuudet"
    :poikkeama PuuttuvanOsaamisenPoikkeama "Puutuvan osaamisen poikkeama"
    :osaamisen-hankkimistapa OsaamisenHankkimistapa
    "Osaamisen hankkimistavat"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    :tarvittava-opetus s/Str "Tarvittava opetus"
    :tyopaikalla-hankittava-osaaminen TyopaikallaHankittavaOsaaminen
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"))

(s/defschema
  NaytonJarjestaja
  (describe
    "Näytön tai osaamisen osoittamisen järjestäjä"
    :nimi s/Str "Näytön tai osaamisen osoittamisen järjestäjän nimi"
    :oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
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
    :arviointikriteerit [Arviointikriteeri]
    "Yksilölliset arvioinnin kriteerit"))

(s/defschema
  HOKS
  (describe
    "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti"
    :eid s/Int "eHOKS-id eli tunniste eHOKS-järjestelmässä"
    :opiskeluoikeus-oid s/Str
    "Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä."
    (s/optional-key :urasuunnitelma) KoodistoKoodi
    "Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi"
    :versio s/Int "HOKS-dokumentin versio"
    :virkailijan-oid s/Str
    (str "HOKS-dokumentin luoneen virkailijan yksilöivä tunniste "
         "oppijanumerorekisterissä")
    :paivittajan-oid s/Str
    (str "HOKS-dokumenttia viimeksi päivittäneen virkailijan yksilöivä tunniste "
         "oppijanumerorekisterissä")
    :hyvaksyjan-oid s/Str
    (str "Luodun HOKS-dokumentin hyväksyjän yksilöivä tunniste "
         "oppijanumerorekisterissä")
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
  "HOKS-dokumentin arvot uutta merkintää luotaessa"
  (st/dissoc HOKS :id :versio))
