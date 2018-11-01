### DateRange  

Aikaväli

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| alku | Päivämäärä | Alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### Opinnot  

Opinnot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ammatilliset-opinnot | [[TutkinnonOsa](#TutkinnonOsa)] | Osaamisen ammattilliset opinnot | Ei |
| yhteiset-tutkinnon-osat | [[YhteinenTutkinnonOsa](#YhteinenTutkinnonOsa)] | Osaamisen yhteiset tutkinnon osat (YTO) | Ei |
| muut-osaamiset | [[MuuTutkinnonOsa](#MuuTutkinnonOsa)] | Muut osaamisen opinnot | Ei |

### YhteinenTutkinnonOsa  

Yhteinen tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alue-tunniste | [KoodistoKoodi](#KoodistoKoodi) | Tutkinnon osan osa-alueen tunnisteen Koodisto-koodi | Kyllä |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon laajuus ePerusteet palvelussa | Ei |
| eperusteet-diaarinumero | Merkkijono | Diaarinumero ePerusteet-palvelussa | Kyllä |
| nimi | Merkkijono | Tutkinnon osan nimi ePerusteet-palvelussa | Ei |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |

### TyopaikallaHankittavaOsaaminen  

Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| muut-oppimisymparistot | [Merkkijono] | Muissa oppimisympäristöissä tapahtuvat osaamisen hankkimiset | Ei |
| vastuullinen-ohjaaja | [Henkilo](#Henkilo) | Vastuullinen työpaikkaohjaaja | Kyllä |
| erityisen-tuen-aika | [DateRange](#DateRange) | Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon taikoulutuksen osassa | Ei |
| keskeiset-tyotehtavat | [Merkkijono] | Keskeiset työtehtävät | Kyllä |
| hankkijan-edustaja | [Henkilo](#Henkilo) | Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja | Kyllä |
| erityinen-tuki | Totuusarvo | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen tuen päätös | Kyllä |
| jarjestajan-edustaja | [Henkilo](#Henkilo) | Koulutuksen järjestäjän edustaja | Kyllä |
| muut-osallistujat | [[Henkilo](#Henkilo)] | Muut ohjaukseen osallistuvat henkilöt | Ei |
| ajankohta | [DateRange](#DateRange) | Työpaikalla järjestettävän koulutuksen ajoittuminen | Kyllä |
| ohjaus-ja-tuki | Totuusarvo | Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

### Henkilo  

Henkilö

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| organisaatio | [Organisaatio](#Organisaatio) | Henkilön organisaatio | Kyllä |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Kyllä |

### TunnustettavanaOlevaOsaaminen  

Osaaminen, joka on toimitettu arvioijille osaamisen tunnustamista varten

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| todentajan-nimi | Merkkijono | Osaamisen todentaneen toimivaltaisen viranomaisen nimi | Kyllä |
| ammatilliset-opinnot | [[TutkinnonOsa](#TutkinnonOsa)] | Osaamisen ammattilliset opinnot | Ei |
| yhteiset-tutkinnon-osat | [[YhteinenTutkinnonOsa](#YhteinenTutkinnonOsa)] | Osaamisen yhteiset tutkinnon osat (YTO) | Ei |
| muut-osaamiset | [[MuuTutkinnonOsa](#MuuTutkinnonOsa)] | Muut osaamisen opinnot | Ei |

### MuuTutkinnonOsa  

Muu tutkinnon osa (ei ePerusteet-palvelussa)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus osaamispisteissä | Kyllä |
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
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#PuuttuvaOsaaminen) | Puuttuvan osaamisen hankkimisen suunnitelma | Kyllä |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Kyllä |
| id | Kokonaisluku | tunniste eHOKS-järjestelmässä | Kyllä |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| versio | Kokonaisluku | HOKS-dokumentin versio | Kyllä |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#OlemassaOlevaOsaaminen) | Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Kyllä |

### KoodiMetadata  

Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | (maybe Str) | Koodisto-koodin nimi | Ei |
| lyhyt-nimi | (maybe Str) | Koodisto-koodin lyhyt nimi | Ei |
| kuvaus | (maybe Str) | Koodisto-koodin kuvaus | Ei |
| kieli | Merkkijono | Koodisto-koodin kieli | Kyllä |

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
| yksilölliset-arviointikriteerit | [[Arviointikriteeri](#Arviointikriteeri)] | Yksilölliset arvioinnin kriteerit | Kyllä |

### KoodistoKoodi  

Koodisto-koodi

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodi-arvo | Merkkijono | Koodisto-koodin arvo | Kyllä |
| koodi-uri | Merkkijono | Koodiston URI | Kyllä |
| versio | Kokonaisluku | Koodisto-koodin versio | Kyllä |
| metadata | [[KoodiMetadata](#KoodiMetadata)] | Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta | Ei |

### Organisaatio  

Organisaatio

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Organisaation y-tunnus | Ei |

### OsaamisenHankkimistapa  

Osaamisen hankkimisen tapa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ajankohta | [DateRange](#DateRange) | Hankkimisen ajankohta | Kyllä |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#KoodistoKoodi) | Osaamisen hankkimisen Koodisto-koodi (URI: osaamisenhankkimistapa) | Kyllä |

### PuuttuvaOsaaminen  

Puuttuvan osaamisen hankkimisen suunnitelma

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ammatillinen-osaaminen | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Kyllä |
| yhteinen-tutkinnon-osa | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Kyllä |

### HOKSArvot  

HOKS-dokumentin arvot uutta merkintää luotaessa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | [KoodistoKoodi](#KoodistoKoodi) | Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi | Ei |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#PuuttuvaOsaaminen) | Puuttuvan osaamisen hankkimisen suunnitelma | Kyllä |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Kyllä |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#OlemassaOlevaOsaaminen) | Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Kyllä |

### PuuttuvaAmmatillinenOsaaminen  

Puuttuvan ammatillisen osaamisen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Kyllä |
| vaatimuksista-tai-tavoitteista | Merkkijono | Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| osaamisen-hankkimistapa | [OsaamisenHankkimistapa](#OsaamisenHankkimistapa) | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| tarvittava-opetus | Merkkijono | Tarvittava opetus | Kyllä |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#TyopaikallaHankittavaOsaaminen) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |

### TutkinnonOsa  

Tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon laajuus ePerusteet palvelussa | Ei |
| eperusteet-diaarinumero | Merkkijono | Diaarinumero ePerusteet-palvelussa | Kyllä |
| nimi | Merkkijono | Tutkinnon osan nimi ePerusteet-palvelussa | Ei |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |

### OlemassaOlevaOsaaminen  

Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tunnustettu-osaaminen | [Opinnot](#Opinnot) | Tunnustettu osaaminen | Ei |
| aiempi-tunnustettava-osaaminen | [Opinnot](#Opinnot) | Aiempi tunnustettava osaaminen | Ei |
| tunnustettavana-olevat | [TunnustettavanaOlevaOsaaminen](#TunnustettavanaOlevaOsaaminen) | Tunnustettavana oleva osaaminen | Ei |
| muut-opinnot | [Opinnot](#Opinnot) | Muu olemassa oleva osaaminen | Ei |
| muut-arvioidut-ja-todennetut-tunnustettavat-opinnot | [MuuTutkinnonOsa](#MuuTutkinnonOsa) | Muut arvioidut ja todennetut tunnustettavat opinnot | Ei |

### OpiskeluvalmiuksiaTukevatOpinnot  

Opiskeluvalmiuksia tukevat opinnot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Kyllä |
| ajankohta | [DateRange](#DateRange) | Opintojen ajoittuminen | Kyllä |

### NaytonJarjestaja  

Näytön tai osaamisen osoittamisen järjestäjä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Näytön tai osaamisen osoittamisen järjestäjän nimi | Kyllä |
| oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### PuuttuvanOsaamisenTiedot  

Puuttuvan osaamisen hankkimisen suunnitelman tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osaamisen-hankkimistapa | [OsaamisenHankkimistapa](#OsaamisenHankkimistapa) | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| tarvittava-opetus | Merkkijono | Tarvittava opetus | Kyllä |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#TyopaikallaHankittavaOsaaminen) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |

### PuuttuvaYTO  

Puuttuvan yhteinen tutkinnon osan tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa | [YhteinenTutkinnonOsa](#YhteinenTutkinnonOsa) | Tutkinnon osa | Kyllä |
| tutkinnon-osa-josta-poiketaan | [YhteinenTutkinnonOsa](#YhteinenTutkinnonOsa) | Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| osaamisen-hankkimistapa | [OsaamisenHankkimistapa](#OsaamisenHankkimistapa) | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| tarvittava-opetus | Merkkijono | Tarvittava opetus | Kyllä |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#TyopaikallaHankittavaOsaaminen) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |

