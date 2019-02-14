# HOKS API doc
Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin esittämiseen.

Generoitu 14.02.2019 11.29

Katso myös [HOKS doc](https://testiopintopolku.fi/ehoks-backend/hoks-doc/index.html)

### MuuOppimisymparisto  

Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tarkenne-koodi-uri | Merkkijono | Oppimisympäristön tarkenne, eHOKS Koodisto-koodi-URI | Kyllä |
| selite | Merkkijono | Oppimisympäristön nimi | Kyllä |
| ohjaus-ja-tuki | Totuusarvo | Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |
| erityinen-tuki | Totuusarvo | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen tuen päätös | Kyllä |
| erityinen-tuki-alku | Päivämäärä | Erityisen tuen alkupäivämäärä muodossa YYYY-MM-DD tutkinnon tai koulutuksen osassa | Kyllä |
| erityinen-tuki-loppu | Päivämäärä | Erityisen tuen loppupäivämäärä muodossa YYYY-MM-DD tutkinnon tai koulutuksen osassa | Kyllä |

### PuuttuvaAmmatillinenOsaaminenLuonti  

Puuttuvan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### PuuttuvaYTOLuonti  

Puuttuvan yhteinen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### YhteinenTutkinnonOsa  

Yhteinen Tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

###   



### OpiskeluvalmiuksiaTukevatOpinnotPaivitys  

Opiskeluvalmiuksia tukevien opintojen tiedot merkintää ylikirjoittaessa
     (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### HOKSPaivitys  

HOKS-dokumentin ylikirjoitus (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | Merkkijono | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| puuttuva-ammatillinen-tutkinnon-osa | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä | Kyllä |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |

### TyopaikallaHankittavaOsaaminen  

Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| vastuullinen-ohjaaja | [VastuullinenOhjaaja](#VastuullinenOhjaaja) | Vastuullinen työpaikkaohjaaja | Kyllä |
| erityinen-tuki-alku | Päivämäärä | Erityisen tuen alkupäivämäärä muodossa YYYY-MM-DD tutkinnon tai koulutuksen osassa | Kyllä |
| tyopaikan-nimi | Merkkijono | Työpaikan nimi | Kyllä |
| keskeiset-tyotehtavat | [Merkkijono] | Keskeiset työtehtävät | Kyllä |
| erityinen-tuki | Totuusarvo | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen tuen päätös | Kyllä |
| muut-osallistujat | [[Henkilo](#Henkilo)] | Muut ohjaukseen osallistuvat henkilöt | Ei |
| tyopaikan-y-tunnus | Merkkijono | Työpaikan y-tunnus | Ei |
| erityinen-tuki-loppu | Päivämäärä | Erityisen tuen loppupäivämäärä muodossa YYYY-MM-DD tutkinnon tai koulutuksen osassa | Kyllä |
| ohjaus-ja-tuki | Totuusarvo | Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys  

Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Ei |
| kuvaus | Merkkijono | Opintojen kuvaus | Ei |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Ei |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Ei |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Ei |

### Henkilo  

Henkilö

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| organisaatio | [Organisaatio](#Organisaatio) | Henkilön organisaatio | Kyllä |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Ei |

### PuuttuvaYTOPaivitys  

Puuttuvan yhteinen tutkinnon osa tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### VastuullinenOhjaaja  

Vastuullinen ohjaaja

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Ei |

### HankitunPaikallisenOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| arvioijat | [[Arvioija](#Arvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Kyllä |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Kyllä |
| nayttoymparisto | [Organisaatio](#Organisaatio) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| sisalto | Merkkijono | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Kyllä |
| ammattitaitovaatimukset | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. Saattaa sisältää tulevaisuudessa yksilölliset arviointikriteerit. | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### HOKSLuonti  

HOKS-dokumentin arvot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | Merkkijono | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| puuttuva-ammatillinen-tutkinnon-osa | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä | Kyllä |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |

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
| rooli | Merkkijono | Arvioijan roolin Koodisto-koodi-URI | Kyllä |
| organisaatio | [Organisaatio](#Organisaatio) | Arvioijan organisaatio | Kyllä |

### Arviointikriteeri  

Arviointikriteeri

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osaamistaso | Kokonaisluku | Osaamistaso | Kyllä |
| kuvaus | Merkkijono | Arviointikriteerin kuvaus | Kyllä |

###   



### HOKS  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| urasuunnitelma | Merkkijono | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| puuttuva-ammatillinen-tutkinnon-osa | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä | Kyllä |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| versio | Kokonaisluku | HOKS-dokumentin versio | Kyllä |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |
| tutkinto | [Tutkinto](#Tutkinto) | Tutkinnon tiedot ePerusteet palvelussa | Ei |

### HankitunOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| arvioijat | [[Arvioija](#Arvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Kyllä |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Kyllä |
| nayttoymparisto | [Organisaatio](#Organisaatio) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| sisalto | Merkkijono | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Kyllä |
| ammattitaitovaatimukset | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. Tunnisteen tyyppi voi vielä päivittyä. Tulevaisuudessa, jos tarvitaan, tähän voidaan lisätä yksilölliset arviointikriteerit | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnotLuonti  

Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

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
| urasuunnitelma | Merkkijono | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| luonut | Merkkijono | HOKS-dokumentin luoneen henkilön nimi | Ei |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| opiskeluoikeus-oid | Merkkijono | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä | Ei |
| puuttuva-ammatillinen-tutkinnon-osa | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| paivittanyt | Merkkijono | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Ei |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| hyvaksynyt | Merkkijono | Luodun HOKS-dokumentn hyväksyjän nimi | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot) | Opiskeluvalmiuksia tukevat opinnot | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osa | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Ei |

### OsaamisenHankkimistapa  

Osaamisen hankkimisen tapa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| alku | Päivämäärä | Alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| koodi-uri | Merkkijono, esim. osaamisenhankkimistapa_oppisopimus | Osaamisen hankkimisen Koodisto-koodi-URI (osaamisenhankkimistapa) | Kyllä |
| jarjestajan-edustaja | [Oppilaitoshenkilo](#Oppilaitoshenkilo) | Koulutuksen järjestäjän edustaja | Ei |
| hankkijan-edustaja | [Oppilaitoshenkilo](#Oppilaitoshenkilo) | Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja | Ei |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#TyopaikallaHankittavaOsaaminen) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot. Tämä tieto tuodaan, jos hankkimistapa on oppisopimuskoulutus tai koulutussopimus. | Ei |
| muut-oppimisymparisto | [MuuOppimisymparisto](#MuuOppimisymparisto) | Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |

### OlemassaOlevanYTOOsaAlue  

Olemassaolevan YTOn osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| valittu-todentamisen-prosessi | Joukon alkio (valittu-todentaminen-naytto, valittu-todentaminen-arvioijat, valittu-todentaminen-suoraan) | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tarkentavat-tiedot | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

###   



### PuuttuvaAmmatillinenOsaaminen  

Puuttuvan ammatillisen osaamisen tiedot (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### TutkinnonOsa  

Tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |

### PuuttuvaAmmatillinenOsaaminenKentanPaivitys  

Puuttuvan ammatillisen osaamisen tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### Oppilaitoshenkilo  

Oppilaitoksen edustaja

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Ei |
| oppilaitoksen-oid | Merkkijono | Oppilaitoksen oid-tunniste Opintopolku-palvelussa. | Kyllä |

### OlemassaOlevaYhteinenTutkinnonOsa  

Yhteinen Tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[OlemassaOlevanYTOOsaAlue](#OlemassaOlevanYTOOsaAlue)] | OlemassaOlevanYhteisenTutkinnonOsanOsa:n osa-alueet | Kyllä |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| valittu-todentamisen-prosessi | Joukon alkio (valittu-todentaminen-naytto, valittu-todentaminen-arvioijat, valittu-todentaminen-suoraan) | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tarkentavat-tiedot | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### OpiskeluvalmiuksiaTukevatOpinnot  

Opiskeluvalmiuksia tukevat opinnot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| kesto | Kokonaisluku | Opintojen kesto päivinä | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### PuuttuvaPaikallinenTutkinnonOsaPaivitys  

Puuttuvan paikallisen tutkinnon osan tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |

### YhteisenTutkinnonOsanOsaAlue  

Puuttuvan yhteinen tutkinnon osan (YTO) osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [HankitunYTOOsaamisenNaytto](#HankitunYTOOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### PuuttuvaAmmatillinenOsaaminenPaivitys  

Puuttuvan ammatillisen osaamisen tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa | [TutkinnonOsa](#TutkinnonOsa) | Tutkinnon osa | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### NaytonJarjestaja  

Näytön tai osaamisen osoittamisen järjestäjä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Näytön tai osaamisen osoittamisen järjestäjän nimi | Kyllä |
| oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### PuuttuvaYTOKentanPaivitys  

Puuttuvan yhteinen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Ei |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### HankitunYTOOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| arvioijat | [[Arvioija](#Arvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Kyllä |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Kyllä |
| osaamistavoitteet | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan.Tunnisteen tyyppi voi vielä päivittyä ja tähän saattaa tulla vielä Yksilölliset arvioinnin kriteerit | Ei |
| nayttoymparisto | [Organisaatio](#Organisaatio) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| sisalto | Merkkijono | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Kyllä |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### PuuttuvaPaikallinenTutkinnonOsaKentanPaivitys  

Puuttuvan paikallisen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### OlemassaOlevaPaikallinenTutkinnonOsa  

Olemassa oleva paikallinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### PuuttuvaPaikallinenTutkinnonOsaLuonti  

Puuttuvan paikallisen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |

### OlemassaOlevaAmmatillinenTutkinnonOsa  

Ammatillinen osaaminen, joka osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa.

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| valittu-todentamisen-prosessi | Joukon alkio (valittu-todentaminen-naytto, valittu-todentaminen-arvioijat, valittu-todentaminen-suoraan) | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tarkentavat-tiedot | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### PuuttuvaYTO  

Puuttuvan yhteinen tutkinnon osan (YTO) tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |

### PuuttuvaPaikallinenTutkinnonOsa  

Puuttuva paikallinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Kyllä |
| kuvaus | Merkkijono | Tutkinnon osan kuvaus | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | Merkkijono | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Kyllä |
| hankitun-osaamisen-naytto | [HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto) | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |

