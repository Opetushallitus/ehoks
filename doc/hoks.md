# HOKS API doc
Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin esittämiseen.

Generoitu 28.02.2019 15.46

Katso myös [HOKS doc](https://testiopintopolku.fi/ehoks-backend/hoks-doc/index.html)

### MuuOppimisymparisto  

Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppimisymparisto-koodi-uri | Merkkijono | Oppimisympäristön tarkenne, eHOKS Koodisto-koodi-URI | Kyllä |
| selite | Merkkijono | Oppimisympäristön nimi | Kyllä |
| lisatiedot | Totuusarvo | Lisätiedoisssa, onko tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

### PuuttuvaAmmatillinenOsaaminenLuonti  

Puuttuvan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### NayttoYmparisto  

Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Mikäli organisaatiolla on y-tunnus,<br>    organisaation y-tunnus | Ei |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Ei |

### PuuttuvaYTOLuonti  

Puuttuvan yhteinen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

###   



### YhteinenTutkinnonOsa  

Yhteinen Tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

###   



### OpiskeluvalmiuksiaTukevatOpinnotPaivitys  

Opiskeluvalmiuksia tukevien opintojen tiedot merkintää ylikirjoittaessa
     (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### HOKSPaivitys  

HOKS-dokumentin ylikirjoitus (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| ensikertainen-hyvaksyminen | Aikaleima | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| urasuunnitelma-koodi-uri | #"^urasuunnitelma_\d{4}$" | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osat | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| puuttuva-ammatillinen-tutkinnon-osat | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d{11}$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| paivittaja | [HoksToimija](#HoksToimija) | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksyja | [HoksToimija](#HoksToimija) | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |

### TyopaikallaHankittavaOsaaminen  

Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| vastuullinen-ohjaaja | [VastuullinenOhjaaja](#VastuullinenOhjaaja) | Vastuullinen työpaikkaohjaaja | Kyllä |
| tyopaikan-nimi | Merkkijono | Työpaikan nimi | Kyllä |
| tyopaikan-y-tunnus | Merkkijono | Työpaikan y-tunnus | Ei |
| muut-osallistujat | [[Henkilo](#Henkilo)] | Muut ohjaukseen osallistuvat henkilöt | Ei |
| keskeiset-tyotehtavat | [Merkkijono] | Keskeiset työtehtävät | Kyllä |
| lisatiedot | Totuusarvo | Lisätietoja, esim. opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys  

Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Ei |
| kuvaus | Merkkijono | Opintojen kuvaus | Ei |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Ei |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Ei |

###   



### Henkilo  

Henkilö

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| organisaatio | [Organisaatio](#Organisaatio) | Henkilön organisaatio | Kyllä |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Ei |

### KoulutuksenjarjestajaArvioija  

Työelämän arvioija

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Arvioijan nimi | Kyllä |
| organisaatio | [KoulutuksenJarjestajaOrganisaatio](#KoulutuksenJarjestajaOrganisaatio) | Koulutuksenjarjestajan arvioijan organisaatio | Kyllä |

### PuuttuvaYTOPaivitys  

Puuttuvan yhteinen tutkinnon osa tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### OppijaOlemassaOlevaAmmatillinenTutkinnonOsa  

Oppijan olemassa oleava ammatillinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| valittu-todentamisen-prosessi-koodi-uri | #"^valittuprosessi_\d+$" | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tarkentavat-tiedot-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |
| tarkentavat-tiedot-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| tutkinnon-osa-koodisto-koodi | [KoodistoKoodi](#KoodistoKoodi) |  | Kyllä |

### VastuullinenOhjaaja  

Vastuullinen ohjaaja

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| sahkoposti | Merkkijono | Vastuullisen ohjaajan sähköpostiosoite | Ei |

### OppijaPuuttuvaAmmatillinenOsaaminen  



| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodisto-koodi | [KoodistoKoodi](#KoodistoKoodi) |  | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono |  | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono |  | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] |  | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] |  | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" |  | Ei |

### HankitunPaikallisenOsaamisenNaytto  

Hankitun paikallisen osaamisen osoittaminen: Näyttö tai muu osaamisen
     osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| yto-osa-alue-koodi-uri | [Merkkijono, esim. ammatillisenoppiaineet_aa] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueen Koodisto-koodi-URI eperusteet-järjestelmässä | Ei |
| nayttoymparisto | [NayttoYmparisto](#NayttoYmparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| koulutuksenjarjestaja-arvioijat | [[KoulutuksenjarjestajaArvioija](#KoulutuksenjarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| tyoelama-arvioijat | [[TyoelamaArvioija](#TyoelamaArvioija)] | Näytön tai<br>    osaamisen osoittamisen arvioijat | Ei |

### HOKSLuonti  

HOKS-dokumentin arvot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| ensikertainen-hyvaksyminen | Aikaleima | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-uri | #"^urasuunnitelma_\d{4}$" | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osat | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| puuttuva-ammatillinen-tutkinnon-osat | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d{11}$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Kyllä |
| laatija | [HoksToimija](#HoksToimija) | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| paivittaja | [HoksToimija](#HoksToimija) | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| hyvaksyja | [HoksToimija](#HoksToimija) | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |

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
| organisaatio | [Organisaatio](#Organisaatio) | Arvioijan organisaatio | Kyllä |

###   



### Arviointikriteeri  

Arviointikriteeri

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osaamistaso | Kokonaisluku | Osaamistaso | Kyllä |
| kuvaus | Merkkijono | Arviointikriteerin kuvaus | Kyllä |

### KoulutuksenJarjestajaOrganisaatio  

Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppilaitos-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Mikäli kyseessä oppilaitos, oppilaitoksen oid-tunniste<br>       Opintopolku-palvelussa. | Ei |

###   



### HOKS  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| ensikertainen-hyvaksyminen | Aikaleima | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| urasuunnitelma-koodi-uri | #"^urasuunnitelma_\d{4}$" | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osat | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| puuttuva-ammatillinen-tutkinnon-osat | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d{11}$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Kyllä |
| laatija | [HoksToimija](#HoksToimija) | HOKS-dokumentin luoneen henkilön nimi | Kyllä |
| versio | Kokonaisluku | HOKS-dokumentin versio | Kyllä |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Kyllä |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| paivittaja | [HoksToimija](#HoksToimija) | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Kyllä |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| tutkinto | [Tutkinto](#Tutkinto) | Tutkinnon tiedot ePerusteet palvelussa | Ei |
| hyvaksyja | [HoksToimija](#HoksToimija) | Luodun HOKS-dokumentn hyväksyjän nimi | Kyllä |

###   



### HankitunOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| yto-osa-alue-koodi-uri | [Merkkijono, esim. ammatillisenoppiaineet_aa] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueen Koodisto-koodi-URI eperusteet-järjestelmässä | Ei |
| nayttoymparisto | [NayttoYmparisto](#NayttoYmparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| koulutuksenjarjestaja-arvioijat | [[KoulutuksenjarjestajaArvioija](#KoulutuksenjarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| tyoelama-arvioijat | [[TyoelamaArvioija](#TyoelamaArvioija)] | Näytön tai<br>    osaamisen osoittamisen arvioijat | Ei |

### OpiskeluvalmiuksiaTukevatOpinnotLuonti  

Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### Organisaatio  

Organisaatio

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Mikäli organisaatiolla on y-tunnus,<br>    organisaation y-tunnus | Ei |

### TodennettuArviointiLisatiedot  

Mikäli arvioijan kautta todennettu, annetaan myös arvioijan lisätiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| lahetetty-arvioitavaksi | Päivämäärä | Päivämäärä, jona<br>    lähetetty arvioitavaksi, muodossa YYYY-MM-DD | Ei |
| aiemmin-hankitun-osaamisen-arvioija | [[KoulutuksenjarjestajaArvioija](#KoulutuksenjarjestajaArvioija)] | Mikäli todennettu arvioijan kautta, annetaan arvioijien tiedot. | Ei |

### HOKSKentanPaivitys  

HOKS-dokumentin ylikirjoitus (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| paivittaja | [HoksToimija](#HoksToimija) | HOKS-dokumenttia viimeksi päivittäneen henkilön nimi | Ei |
| olemassa-olevat-ammatilliset-tutkinnon-osat | [[OlemassaOlevaAmmatillinenTutkinnonOsa](#OlemassaOlevaAmmatillinenTutkinnonOsa)] | Olemassa oleva ammatillinen osaaminen | Ei |
| hyvaksyja | [HoksToimija](#HoksToimija) | Luodun HOKS-dokumentn hyväksyjän nimi | Ei |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d{11}$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Ei |
| laatija | [HoksToimija](#HoksToimija) | HOKS-dokumentin luoneen henkilön nimi | Ei |
| puuttuva-yhteisen-tutkinnon-osat | [[PuuttuvaYTO](#PuuttuvaYTO)] | Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| ensikertainen-hyvaksyminen | Aikaleima | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| puuttuva-paikallinen-tutkinnon-osat | [[PuuttuvaPaikallinenTutkinnonOsa](#PuuttuvaPaikallinenTutkinnonOsa)] | Puuttuvat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-uri | #"^urasuunnitelma_\d{4}$" | Opiskelijan tavoitteen Koodisto-koodi-URI | Ei |
| olemassa-olevat-yhteiset-tutkinnon-osat | [[OlemassaOlevaYhteinenTutkinnonOsa](#OlemassaOlevaYhteinenTutkinnonOsa)] | Olemassa olevat yhteiset tutkinnon osat (YTO) | Ei |
| olemassa-oleva-paikallinen-tutkinnon-osat | [[OlemassaOlevaPaikallinenTutkinnonOsa](#OlemassaOlevaPaikallinenTutkinnonOsa)] | Olemassa oleva paikallinen tutkinnon osa | Ei |
| puuttuva-ammatillinen-tutkinnon-osat | [[PuuttuvaAmmatillinenOsaaminen](#PuuttuvaAmmatillinenOsaaminen)] | Puuttuvan ammatillisen osaamisen hankkimisen tiedot | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| oppija-oid | Merkkijono | Oppijan tunniste Opintopolku-ympäristössä | Ei |

### OsaamisenHankkimistapa  

Osaamisen hankkimisen tapa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestajan-edustaja | [Oppilaitoshenkilo](#Oppilaitoshenkilo) | Koulutuksen järjestäjän edustaja | Ei |
| muut-oppimisymparisto | [[MuuOppimisymparisto](#MuuOppimisymparisto)] | Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |
| ajanjakson-tarkenne | Merkkijono | Tarkentava teksti ajanjaksolle, jos useita aikavälillä. | Ei |
| osamisen-hankkimistapa-koodi-uri | Merkkijono, esim. osaamisenhankkimistapa_oppisopimus | Osaamisen hankkimisen Koodisto-koodi-URI (osaamisenhankkimistapa) | Kyllä |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#TyopaikallaHankittavaOsaaminen) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot. Tämä tieto tuodaan, jos hankkimistapa on oppisopimuskoulutus tai koulutussopimus. | Ei |
| hankkijan-edustaja | [Oppilaitoshenkilo](#Oppilaitoshenkilo) | Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja | Ei |
| alku | Päivämäärä | Alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### OlemassaOlevanYTOOsaAlue  

Olemassaolevan YTOn osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alue-koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| valittu-todentamisen-prosessi-koodi-uri | #"^valittuprosessi_\d+$" | Todentamisen prosessin kuvauksen (suoraan/arvioijien kautta/näyttö)<br>    koodi-uri | Kyllä |
| tarkentavat-tiedot | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### TyoelamaOrganisaatio  

Työelämän toimijan organisaatio, jossa näyttö tai osaamisen osoittaminen
     annetaan

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Mikäli organisaatiolla on y-tunnus,<br>    organisaation y-tunnus | Ei |

### PuuttuvaAmmatillinenOsaaminen  

Puuttuvan ammatillisen osaamisen tiedot (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### PuuttuvaAmmatillinenOsaaminenKentanPaivitys  

Puuttuvan ammatillisen osaamisen tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### Oppilaitoshenkilo  

Oppilaitoksen edustaja

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Ei |
| oppilaitos-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Oppilaitoksen oid-tunniste Opintopolku-palvelussa. | Kyllä |

### OlemassaOlevaYhteinenTutkinnonOsa  

Olemassa oleva yhteinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| osa-alueet | [[OlemassaOlevanYTOOsaAlue](#OlemassaOlevanYTOOsaAlue)] | YTO osa-alueet | Kyllä |
| valittu-todentamisen-prosessi-koodi-uri | #"^valittuprosessi_\d+$" | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tarkentavat-tiedot-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |
| tarkentavat-tiedot-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |

### OpiskeluvalmiuksiaTukevatOpinnot  

Opiskeluvalmiuksia tukevat opinnot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### PuuttuvaPaikallinenTutkinnonOsaPaivitys  

Puuttuvan paikallisen tutkinnon osan tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

### YhteisenTutkinnonOsanOsaAlue  

Puuttuvan yhteinen tutkinnon osan (YTO) osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alue-koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [[HankitunYTOOsaamisenNaytto](#HankitunYTOOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### PuuttuvaAmmatillinenOsaaminenPaivitys  

Puuttuvan ammatillisen osaamisen tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### NaytonJarjestaja  

Näytön tai osaamisen osoittamisen järjestäjä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppilaitos-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### PuuttuvaYTOKentanPaivitys  

Puuttuvan yhteinen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### HankitunYTOOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| osaamistavoitteet | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan.Tunnisteen tyyppi voi vielä päivittyä ja tähän saattaa tulla vielä Yksilölliset arvioinnin kriteerit | Ei |
| nayttoymparisto | [NayttoYmparisto](#NayttoYmparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| tyoelama-arvioijat | [[TyoelamaArvioija](#TyoelamaArvioija)] | Näytön tai<br>    osaamisen osoittamisen arvioijat | Ei |
| koulutuksenjarjestaja-arvioijat | [[KoulutuksenjarjestajaArvioija](#KoulutuksenjarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| yto-osa-alue-koodi-uri | [Merkkijono, esim. ammatillisenoppiaineet_aa] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueen Koodisto-koodi-URI eperusteet-järjestelmässä | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### PuuttuvaPaikallinenTutkinnonOsaKentanPaivitys  

Puuttuvan paikallisen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

### OlemassaOlevaPaikallinenTutkinnonOsa  

Olemassa oleva paikallinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

###   



### TyoelamaArvioija  

Työelämän arvioija

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Arvioijan nimi | Kyllä |
| organisaatio | [TyoelamaOrganisaatio](#TyoelamaOrganisaatio) | Työelämän arvioijan organisaatio | Kyllä |

### PuuttuvaPaikallinenTutkinnonOsaLuonti  

Puuttuvan paikallisen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

### OlemassaOlevaAmmatillinenTutkinnonOsa  

Olemassa oleva yhteinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| valittu-todentamisen-prosessi-koodi-uri | #"^valittuprosessi_\d+$" | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tarkentavat-tiedot-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |
| tarkentavat-tiedot-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |

### HoksToimija  

Hoksin hyväksyjä tai päivittäjä koulutusjärjestäjän organisaatiossa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |

### PuuttuvaYTO  

Puuttuvan yhteinen tutkinnon osan (YTO) tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### PuuttuvaPaikallinenTutkinnonOsa  

Puuttuva paikallinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

