# HOKS API doc
Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin esittämiseen.

Generoitu 07.02.2019 16.58

Katso myös [HOKS doc](https://testiopintopolku.fi/ehoks-backend/hoks-doc/index.html)

### MuuOppimisymparisto  

Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tarkenne | [KoodistoKoodi](#KoodistoKoodi) | Oppimisympäristön tarkenne, eHOS Koodisto-koodi | Kyllä |
| selite | Merkkijono | Oppimisympäristön nimi | Kyllä |
| ohjaus-ja-tuki | Totuusarvo | Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |
| erityinen-tuki | Totuusarvo | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen tuen päätös | Kyllä |
| erityisen-tuen-aika | [Aikavali](#Aikavali) | Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon taikoulutuksen osassa | Ei |

### PuuttuvaAmmatillinenOsaaminenLuonti  

Puuttuvan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### PuuttuvaYTOLuonti  

Puuttuvan yhteinen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### YhteinenTutkinnonOsa  

Yhteinen Tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi (tutkinnonosat) | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon laajuus ePerusteet palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi ePerusteet-palvelussa | Ei |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus ePerusteet-palvelussa | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### KoodistoKoodiLuonti  

Koodisto-koodin lisäys tai päivitys

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodi-arvo | Merkkijono | Koodisto-koodin arvo | Kyllä |
| koodi-uri | Merkkijono | Koodiston URI | Kyllä |
| versio | Kokonaisluku | Koodisto-koodin versio | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnotPaivitys  

Opiskeluvalmiuksia tukevien opintojen tiedot merkintää ylikirjoittaessa
     (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Kyllä |
| ajankohta | [Aikavali](#Aikavali) | Opintojen ajoittuminen | Kyllä |

### HOKSPaivitys  

HOKS-dokumentin ylikirjoitus (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | [KoodistoKoodiLuonti](#KoodistoKoodiLuonti) | Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi | Ei |
| opiskeluoikeus | [Opiskeluoikeus](#Opiskeluoikeus) | Opiskeluoikeuden tiedot Koski-järjestelmässä | Kyllä |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| olemassa-oleva-ammatillinen-osaaminen | [[OlemassaOlevaAmmatillinenOsaaminen](#OlemassaOlevaAmmatillinenOsaaminen)] | Olemassa oleva ammatillinen osaaminen | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |
| puuttuva-ammatillinen-osaaminen | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |

### TyopaikallaHankittavaOsaaminen  

Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| vastuullinen-ohjaaja | [Henkilo](#Henkilo) | Vastuullinen työpaikkaohjaaja | Kyllä |
| erityisen-tuen-aika | [Aikavali](#Aikavali) | Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon taikoulutuksen osassa | Ei |
| keskeiset-tyotehtavat | [Merkkijono] | Keskeiset työtehtävät | Kyllä |
| hankkijan-edustaja | [Henkilo](#Henkilo) | Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja | Kyllä |
| erityinen-tuki | Totuusarvo | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen tuen päätös | Kyllä |
| jarjestajan-edustaja | [Henkilo](#Henkilo) | Koulutuksen järjestäjän edustaja | Kyllä |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| muut-osallistujat | [[Henkilo](#Henkilo)] | Muut ohjaukseen osallistuvat henkilöt | Ei |
| ohjaus-ja-tuki | Totuusarvo | Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys  

Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| nimi | Merkkijono | Opintojen nimi | Ei |
| kuvaus | Merkkijono | Opintojen kuvaus | Ei |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Ei |
| ajankohta | [Aikavali](#Aikavali) | Opintojen ajoittuminen | Ei |

### Henkilo  

Henkilö

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| organisaatio | [Organisaatio](#Organisaatio) | Henkilön organisaatio | Kyllä |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Kyllä |

### PuuttuvaYTOPaivitys  

Puuttuvan yhteinen tutkinnon osa tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### PaikallinenTutkinnonOsaKentanPaivitys  

Puuttuvan paikallisen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| amosaa-tunniste | Kokonaisluku | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### Aikavali  

Aikaväli

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| alku | Päivämäärä | Alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### TunnustettavanaOlevaOsaaminen  

Osaaminen, joka on toimitettu arvioijille osaamisen tunnustamista varten

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| todentajan-nimi | Merkkijono | Osaamisen todentaneen toimivaltaisen viranomaisen nimi | Kyllä |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| ammatilliset-opinnot | [[TutkinnonOsa](#TutkinnonOsa)] | Osaamisen ammattilliset opinnot | Ei |
| yhteiset-tutkinnon-osat | [[YhteinenTutkinnonOsa](#YhteinenTutkinnonOsa)] | Osaamisen yhteiset tutkinnon osat (YTO) | Ei |
| paikalliset-osaamiset | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Osaamisen paikallisen tutkinnon osat | Ei |

### HankitunPaikallisenOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Kyllä |
| nayttoymparisto | [Organisaatio](#Organisaatio) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Kyllä |
| ajankohta | [Aikavali](#Aikavali) | Näytön tai osaamisen osoittamisen ajankohta | Kyllä |
| sisalto | Merkkijono | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Kyllä |
| ammattitaitovaatimukset | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. Saattaa sisältää tulevaisuudessa yksilölliset arviointikriteerit. | Ei |
| arvioijat | [[Arvioija](#Arvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Kyllä |

### HOKSLuonti  

HOKS-dokumentin arvot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | [KoodistoKoodiLuonti](#KoodistoKoodiLuonti) | Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi | Ei |
| opiskeluoikeus | [Opiskeluoikeus](#Opiskeluoikeus) | Opiskeluoikeuden tiedot Koski-järjestelmässä | Kyllä |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| olemassa-oleva-ammatillinen-osaaminen | [[OlemassaOlevaAmmatillinenOsaaminen](#OlemassaOlevaAmmatillinenOsaaminen)] | Olemassa oleva ammatillinen osaaminen | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |
| puuttuva-ammatillinen-osaaminen | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |

### MuuTutkinnonOsa  

Muu tutkinnon osa (ei ePerusteet-palvelussa)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus osaamispisteissä | Kyllä |
| kesto | Kokonaisluku | Tutkinnon osan kesto päivinä | Kyllä |
| suorituspvm | Päivämäärä | Tutkinnon suorituspäivä muodossa YYYY-MM-DD | Kyllä |

### Arvioija  

Arvioija

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| nimi | Merkkijono | Arvioijan nimi | Kyllä |
| rooli | [KoodistoKoodi](#KoodistoKoodi) | Arvioijan roolin Koodisto-koodi | Kyllä |
| organisaatio | [Organisaatio](#Organisaatio) | Arvioijan organisaatio | Kyllä |

### Opiskeluoikeus  

Opiskeluoikeuden tiedot Koski-järjestelmässä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oid | Merkkijono | Opinto-oikeuden tunniste Opintopolku-ympäristössä | Kyllä |
| tutkinto | [Tutkinto](#Tutkinto) | Opinto-oikeuden tutkinto | Kyllä |

### Arviointikriteeri  

Arviointikriteeri

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| osaamistaso | Kokonaisluku | Osaamistaso | Kyllä |
| kuvaus | Merkkijono | Arviointikriteerin kuvaus | Kyllä |

### PaikallinenTutkinnonOsaLuonti  

Puuttuvan paikallisen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| amosaa-tunniste | Kokonaisluku | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |

### HOKS  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | [KoodistoKoodi](#KoodistoKoodi) | Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi | Ei |
| opiskeluoikeus | [Opiskeluoikeus](#Opiskeluoikeus) | Opiskeluoikeuden tiedot Koski-järjestelmässä | Kyllä |
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| versio | Kokonaisluku | HOKS-dokumentin versio | Kyllä |
| olemassa-oleva-ammatillinen-osaaminen | [[OlemassaOlevaAmmatillinenOsaaminen](#OlemassaOlevaAmmatillinenOsaaminen)] | Olemassa oleva ammatillinen osaaminen | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |
| puuttuva-ammatillinen-osaaminen | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |

### PaikallinenTutkinnonOsaPaivitys  

Puuttuvan paikallisen tutkinnon osan tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| amosaa-tunniste | Kokonaisluku | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |

### KoodiMetadata  

Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Valinnainen merkkijono | Koodisto-koodin nimi | Ei |
| lyhyt-nimi | Valinnainen merkkijono | Koodisto-koodin lyhyt nimi | Ei |
| kuvaus | Valinnainen merkkijono | Koodisto-koodin kuvaus | Ei |
| kieli | Merkkijono | Koodisto-koodin kieli | Kyllä |

### HankitunOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Kyllä |
| nayttoymparisto | [Organisaatio](#Organisaatio) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Kyllä |
| ajankohta | [Aikavali](#Aikavali) | Näytön tai osaamisen osoittamisen ajankohta | Kyllä |
| sisalto | Merkkijono | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Kyllä |
| ammattitaitovaatimukset | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. Tunnisteen tyyppi voi vielä päivittyä. Tulevaisuudessa, jos tarvitaan, tähän voidaan lisätä yksilölliset arviointikriteerit | Ei |
| arvioijat | [[Arvioija](#Arvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnotLuonti  

Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Kyllä |
| ajankohta | [Aikavali](#Aikavali) | Opintojen ajoittuminen | Kyllä |

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

### HOKSKentanPaivitys  

HOKS-dokumentin ylikirjoitus (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | [KoodistoKoodiLuonti](#KoodistoKoodiLuonti) | Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi | Ei |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Ei |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Ei |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| opiskeluoikeus | [Opiskeluoikeus](#Opiskeluoikeus) | Opiskeluoikeuden tiedot Koski-järjestelmässä | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| olemassa-oleva-ammatillinen-osaaminen | [[OlemassaOlevaAmmatillinenOsaaminen](#OlemassaOlevaAmmatillinenOsaaminen)] | Olemassa oleva ammatillinen osaaminen | Ei |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[PaikallinenTutkinnonOsa](#PaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Ei |
| puuttuva-ammatillinen-osaaminen | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |

### OsaamisenHankkimistapa  

Osaamisen hankkimisen tapa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| ajankohta | [Aikavali](#Aikavali) | Hankkimisen ajankohta | Kyllä |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#KoodistoKoodi) | Osaamisen hankkimisen Koodisto-koodi (URI: osaamisenhankkimistapa) | Kyllä |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#TyopaikallaHankittavaOsaaminen) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |
| muut-oppimisymparisto | [MuuOppimisymparisto](#MuuOppimisymparisto) | Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |

### PuuttuvaAmmatillinenOsaaminen  

Puuttuvan ammatillisen osaamisen tiedot (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### TutkinnonOsa  

Tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon laajuus ePerusteet palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi ePerusteet-palvelussa | Ei |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus ePerusteet-palvelussa | Ei |

### PuuttuvaAmmatillinenOsaaminenKentanPaivitys  

Puuttuvan ammatillisen osaamisen tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### OlemassaOlevaYhteinenTutkinnonOsa  

Yhteinen Tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon laajuus ePerusteet palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi ePerusteet-palvelussa | Ei |
| tarkentavat-tiedot | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus ePerusteet-palvelussa | Ei |
| valittu-todentamisen-prosessi | Joukon alkio (valittu-todentaminen-naytto, valittu-todentaminen-arvioijat, valittu-todentaminen-suoraan) | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi (tutkinnonosat) | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnot  

Opiskeluvalmiuksia tukevat opinnot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Kyllä |
| ajankohta | [Aikavali](#Aikavali) | Opintojen ajoittuminen | Kyllä |

### YhteisenTutkinnonOsanOsaAlue  

Puuttuvan yhteinen tutkinnon osan (YTO) osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon laajuus ePerusteet palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi ePerusteet-palvelussa | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [HankitunYTOOsaamisenNaytto](#HankitunYTOOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### PuuttuvaAmmatillinenOsaaminenPaivitys  

Puuttuvan ammatillisen osaamisen tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### NaytonJarjestaja  

Näytön tai osaamisen osoittamisen järjestäjä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| nimi | Merkkijono | Näytön tai osaamisen osoittamisen järjestäjän nimi | Kyllä |
| oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### PuuttuvaYTOKentanPaivitys  

Puuttuvan yhteinen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Ei |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi (tutkinnonosat) | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### HankitunYTOOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Kyllä |
| nayttoymparisto | [Organisaatio](#Organisaatio) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Kyllä |
| ajankohta | [Aikavali](#Aikavali) | Näytön tai osaamisen osoittamisen ajankohta | Kyllä |
| sisalto | Merkkijono | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Kyllä |
| arvioijat | [[Arvioija](#Arvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Kyllä |
| osaamistavoitteet | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan.Tunnisteen tyyppi voi vielä päivittyä ja tähän saattaa tulla vielä Yksilölliset arvioinnin kriteerit | Ei |

### PaikallinenTutkinnonOsa  

Puuttuva paikallinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Kyllä |
| amosaa-tunniste | Kokonaisluku | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |

### Tutkinto  

Tutkinnon perustiedot ePerusteet järjestelmässä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon laajuus | Kyllä |
| nimi | Merkkijono | Tutkinnon nimi | Kyllä |

### OlemassaOlevaAmmatillinenOsaaminen  

Ammatillinen osaaminen, joka osaamisen tunnustamisen perusteella
    sisällytetty suoraan osaksi opiskelijan tutkintoa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-tunniste | [KoodistoKoodi](#KoodistoKoodi) | Tutkinnon osan, johon tunnistettava olemassa oleva osaaminen liittyy, Koodisto-koodi | Ei |
| valittu-todentamisen-prosessi | Joukon alkio (valittu-todentaminen-naytto, valittu-todentaminen-arvioijat, valittu-todentaminen-suoraan) | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tarkentavat-tiedot | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### PuuttuvaYTO  

Puuttuvan yhteinen tutkinnon osan (YTO) tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| id | Kokonaisluku | Tunniste eHOKS-järjestelmässä | Ei |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tunniste | [KoodistoKoodi](#KoodistoKoodi) | Koodisto-koodi (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

