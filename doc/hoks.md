### DateRange  

Aikaväli

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| alku | Päivämäärä | Alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Loppupäivämäärä muodoss YYYY-MM-DD | Kyllä |

### Opinnot  

Opinnot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ammatilliset-opinnot | [[TutkinnonOsa](#TutkinnonOsa)] | Osaamisen ammattilliset opinnot | Kyllä |
| yhteiset-tutkinnon-osat | [[YhteinenTutkinnonOsa](#YhteinenTutkinnonOsa)] | Osaamisen yhteiset tutkinnon osat (YTO) | Kyllä |
| muut-osaamiset | [[MuuTutkinnonOsa](#MuuTutkinnonOsa)] | Muut osaamisen opinnot | Kyllä |

### YhteinenTutkinnonOsa  

YTO tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alue-tunniste | [KoodistoKoodi](#KoodistoKoodi) | Tutkinnon osan osa-alueen tunnisteen Koodisto-koodi | Kyllä |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon laajuus | Kyllä |
| eperusteet-diaarinumero | Merkkijono | Diaarinumero ePerusteet-palvelussa | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| koulutustyyppi | [KoodistoKoodi](#KoodistoKoodi) | Tutkinnon osan koulutustyypin Koodisto-koodi | Kyllä |

### TyopaikallaHankittavaOsaaminen  

Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| vastuullinen-ohjaaja | [Henkilo](#Henkilo) | Vastuullinen työpaikkaohjaaja | Kyllä |
| keskeiset-tyotehtavat | [Merkkijono] | Keskeiset työtehtävät | Kyllä |
| hankkijan-edustaja | [Henkilo](#Henkilo) | Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja | Kyllä |
| erityinen-tuki | Totuusarvo | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen tuen päätös | Kyllä |
| jarjestajan-edustaja | [Henkilo](#Henkilo) | Koulutuksen järjestäjän edustaja | Kyllä |
| erityisen-tuen-aika | [DateRange](#DateRange) | Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon taikoulutuksen osassa | Kyllä |
| muut-osallistujat | [[Henkilo](#Henkilo)] | Muut ohjaukseen osallistuvat henkilöt | Kyllä |
| muut-oppimisymparistot | [[Oppimisymparisto](#Oppimisymparisto)] | Muissa oppimisympäristöissä tapahtuvat osaamisen hankkimiset | Kyllä |
| ajankohta | [DateRange](#DateRange) | Työpaikalla järjestettävän koulutuksen ajoittuminen | Kyllä |
| ohjaus-ja-tuki | Totuusarvo | Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

### Henkilo  

Henkilö

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| organisaatio | [Organisaatio](#Organisaatio) | Henkilön organisaatio | Kyllä |
| oid | Merkkijono | Oppijanumero 'oid' on oppijan yksilöivä tunniste Opintopolku-palvelussa ja Koskessa. | Kyllä |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Kyllä |

### Oppimisymparisto  

Oppimisympäristö

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| paikka | Merkkijono | Oppimisympäristön paikan nimi | Kyllä |
| ajankohta | [DateRange](#DateRange) | Ajankohta kyseisessä oppimisympäristössä | Kyllä |

### MuuTutkinnonOsa  

Muu tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus osaamispisteiss' | Kyllä |
| kesto | Kokonaisluku | Tutkinnon osan kesto päivinä | Kyllä |
| suorituspvm | Päivämäärä | Tutkinnon suorituspäivä muodossa YYYY-MM-DD | Kyllä |

### Arvioija  

Arvioija

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Arvioijan nimi | Kyllä |
| rooli | [KoodistoKoodi](#KoodistoKoodi) | Arvioijan roolin Koodisto-koodi | Kyllä |
| organisaatio | [Organisaatio](#Organisaatio) | Arvioijan organisaatio | Kyllä |

### Arviointikriteeri  

Arviointikriteeri

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| arvosana | Kokonaisluku | Arvosana | Kyllä |
| kuvaus | Merkkijono | Arviointikriteerin kuvaus | Kyllä |

### HOKS  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | [KoodistoKoodi](#KoodistoKoodi) | Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi | Ei |
| paivittajan-oid | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| luotu | Aikaleima | HOKS-dokumentin luontiaika | Kyllä |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika | Kyllä |
| luonnin-hyvaksyjan-oid | Merkkijono | Luodun HOKS-dokumentin hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#PuuttuvaOsaaminen) | Puuttuvan osaamisen hankkimisen suunnitelma | Kyllä |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Kyllä |
| versio | Kokonaisluku | HOKS-dokumentin versio | Kyllä |
| paivityksen-hyvaksyjan-oid | Merkkijono | HOKS-dokumentin viimeisen päivityksen hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika | Kyllä |
| eid | Kokonaisluku | eHOKS-id eli tunniste eHOKS-järjestelmässä | Kyllä |
| luojan-oid | Merkkijono | HOKS-dokumentin luoneen henkilön yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#OlemassaOlevaOsaaminen) | Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Kyllä |

### HankitunOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Kyllä |
| nayttoymparisto | [Organisaatio](#Organisaatio) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Kyllä |
| ajankohta | [DateRange](#DateRange) | Näytön tai osaamisen osoittamisen ajankohta | Kyllä |
| sisalto | Merkkijono | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Kyllä |
| ammattitaitovaatimukset | [[KoodistoKoodi](#KoodistoKoodi)] | Ammattitaitovaatimukset, jonka arvioinnin kriteereitä mukautetaan | Kyllä |
| osaamistavoitteet | [[KoodistoKoodi](#KoodistoKoodi)] | Osaamistavoitteet, jonka arvioinnin kriteereitä mukautetaan | Kyllä |
| arvioijat | [[Arvioija](#Arvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Kyllä |
| arviointikriteerit | [[Arviointikriteeri](#Arviointikriteeri)] | Yksilölliset arvioinnin kriteerit | Kyllä |

### KoodistoKoodi  

Koodisto-koodi

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodisto-koodi | Merkkijono | Koodiston koodi | Kyllä |
| koodisto-uri | Merkkijono | Koodiston URI | Kyllä |
| versio | Kokonaisluku | Koodisto-koodin versio | Kyllä |

### Organisaatio  

Organisaatio

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Organisaation y-tunnus | Kyllä |

### OsaamisenHankkimistapa  

Osaamisen hankkimisen tapa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ajankohta | [DateRange](#DateRange) | Hankkimisen ajankohta | Kyllä |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#KoodistoKoodi) | Osaamisen hankkimisen Koodisto-koodi | Kyllä |

### PuuttuvaOsaaminen  

Puuttuvan osaamisen hankkimisen suunnitelma

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ammatilliset-opinnot | [[TutkinnonOsa](#TutkinnonOsa)] | Ammatilliset opinnot | Kyllä |
| yhteiset-tutkinnon-osat | [[YhteinenTutkinnonOsa](#YhteinenTutkinnonOsa)] | Yhteiset tutkinnon osat | Kyllä |
| muut | [[MuuTutkinnonOsa](#MuuTutkinnonOsa)] | Muut tutkinnon osaa pienemmät osaamiskokonaisuudet | Kyllä |
| poikkeama | [PuuttuvanSaamisenPoikkeama](#PuuttuvanSaamisenPoikkeama) | Puutuvan osaamisen poikkeama | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| ajankohta | [DateRange](#DateRange) | Puuttuvan osaamisen hankkimisen ajankohta | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| tarvittava-opetus | Merkkijono | Tarvittava opetus | Kyllä |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#TyopaikallaHankittavaOsaaminen) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Kyllä |

### PuuttuvanSaamisenPoikkeama  

Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| alkuperainen-tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa, johon poikkeus pohjautuu | Kyllä |
| kuvaus | Merkkijono | Poikkeaman kuvaus | Kyllä |

### HOKSArvot  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | [KoodistoKoodi](#KoodistoKoodi) | Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi | Ei |
| paivittajan-oid | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| luotu | Aikaleima | HOKS-dokumentin luontiaika | Kyllä |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika | Kyllä |
| luonnin-hyvaksyjan-oid | Merkkijono | Luodun HOKS-dokumentin hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#PuuttuvaOsaaminen) | Puuttuvan osaamisen hankkimisen suunnitelma | Kyllä |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Kyllä |
| paivityksen-hyvaksyjan-oid | Merkkijono | HOKS-dokumentin viimeisen päivityksen hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika | Kyllä |
| eid | Kokonaisluku | eHOKS-id eli tunniste eHOKS-järjestelmässä | Kyllä |
| luojan-oid | Merkkijono | HOKS-dokumentin luoneen henkilön yksilöivä tunniste Koski-järjestelmässä | Kyllä |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#OlemassaOlevaOsaaminen) | Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Kyllä |

### TutkinnonOsa  

Tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon laajuus | Kyllä |
| eperusteet-diaarinumero | Merkkijono | Diaarinumero ePerusteet-palvelussa | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| koulutustyyppi | [KoodistoKoodi](#KoodistoKoodi) | Tutkinnon osan koulutustyypin Koodisto-koodi | Kyllä |

### OlemassaOlevaOsaaminen  

Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tunnustettu-osaaminen | [Opinnot](#Opinnot) | Tunnustettu osaaminen | Kyllä |
| aiempi-tunnustettava-osaaminen | [Opinnot](#Opinnot) | Aiempi tunnustettava osaaminen | Kyllä |
| tunnustettavana-olevat | [Opinnot](#Opinnot) | Tunnustettavana oleva osaaminen | Kyllä |
| muut-opinnot | [Opinnot](#Opinnot) | Muu olemassa oleva osaaminen | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnot  

Muu tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus osaamispisteiss' | Kyllä |
| kesto | Kokonaisluku | Tutkinnon osan kesto päivinä | Kyllä |
| suorituspvm | Päivämäärä | Tutkinnon suorituspäivä muodossa YYYY-MM-DD | Kyllä |

### NaytonJarjestaja  

Näytön tai osaamisen osoittamisen järjestäjä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Näytön tai osaamisen osoittamisen järjestäjän nimi | Kyllä |
| oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

