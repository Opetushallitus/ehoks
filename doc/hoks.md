# HOKS API doc
Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin esittämiseen.

Generoitu 28.05.2019 12.08

Katso myös [HOKS doc](https://github.com/Opetushallitus/ehoks/blob/master/doc/hoks.md)

### MuuOppimisymparisto  

Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppimisymparisto-koodi-uri | Koodistokoodin uri, merkkijono, esim. oppimisymparistot_0001 | Oppimisympäristön tarkenne, eHOKS Koodisto-koodi-URI, koodisto<br>    oppimisympäristöt eli muotoa oppimisymparistot_xxxx, esim.<br>    oppimisymparistot_0001 | Kyllä |
| oppimisymparisto-koodi-versio | Kokonaisluku | Koodisto-koodin versio, koodistolle oppimisympäristöt | Kyllä |
| selite | Merkkijono | Oppimisympäristön nimi | Kyllä |
| lisatiedot | Totuusarvo | Lisätiedoisssa, onko tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

### HankittavaAmmatillinenOsaaminen  

Hankittavan ammatillisen osaamisen tiedot (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

###   



### YhteinenTutkinnonOsa  

Yhteinen Tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### HankittavaYTOLuonti  

Hankittavan yhteinen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

###   



### HankittavaYTOKentanPaivitys  

Hankittavan yhteinen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Ei |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Ei |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

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
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Kyllä |
| aiemmin-hankitut-ammat-tutkinnon-osat | [[AiemminHankittuAmmatillinenTutkinnonOsa](#AiemminHankittuAmmatillinenTutkinnonOsa)] | Aiemmin hankittu ammatillinen osaaminen | Ei |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| hankittavat-ammat-tutkinnon-osat | [[HankittavaAmmatillinenOsaaminen](#HankittavaAmmatillinenOsaaminen)] | Hankittavan ammatillisen osaamisen hankkimisen tiedot | Ei |
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| aiemmin-hankitut-paikalliset-tutkinnon-osat | [[AiemminHankittuPaikallinenTutkinnonOsa](#AiemminHankittuPaikallinenTutkinnonOsa)] | Aiemmin hankittu paikallinen tutkinnon osa | Ei |
| aiemmin-hankitut-yhteiset-tutkinnon-osat | [[AiemminHankittuYhteinenTutkinnonOsa](#AiemminHankittuYhteinenTutkinnonOsa)] | Aiemmin hankitut yhteiset tutkinnon osat (YTO) | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluoikeus-oid | Opiskeluoikeuden oid, muotoa 1.2.246.562.x.y | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Kyllä |
| hankittavat-yhteiset-tutkinnon-osat | [[HankittavaYTO](#HankittavaYTO)] | Hankittavan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| hankittavat-paikalliset-tutkinnon-osat | [[HankittavaPaikallinenTutkinnonOsa](#HankittavaPaikallinenTutkinnonOsa)] | Hankittavat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| oppija-oid | OID-tunniste muotoa 1.2.246.562.x.y | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |

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

### HankittavaAmmatillinenOsaaminenKentanPaivitys  

Hankittavan ammatillisen osaamisen tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Ei |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

###   



### VastuullinenOhjaaja  

Vastuullinen ohjaaja

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| sahkoposti | Merkkijono | Vastuullisen ohjaajan sähköpostiosoite | Ei |

### HankittavaPaikallinenTutkinnonOsaKentanPaivitys  

Hankittavan paikallisen tutkinnon osan tiedot kenttää tai kenttiä päivittäessä (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

### HankitunPaikallisenOsaamisenNaytto  

Hankitun paikallisen osaamisen osoittaminen: Näyttö tai muu osaamisen
     osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koulutuksen-jarjestaja-arvioijat | [[KoulutuksenJarjestajaArvioija](#KoulutuksenJarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| nayttoymparisto | [Nayttoymparisto](#Nayttoymparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| tyoelama-arvioijat | [[TyoelamaArvioija](#TyoelamaArvioija)] | Näytön tai<br>    osaamisen osoittamisen arvioijat | Ei |
| osa-alueet | [[KoodistoKoodi](#KoodistoKoodi)] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueiden Koodisto-koodi-URIt<br>         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxxesim. ammatillisenoppiaineet_etk | Ei |
| keskeiset-tyotehtavat-naytto | [Merkkijono] | Keskeiset<br>    työtehtävät | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |

### HOKSLuonti  

HOKS-dokumentin arvot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Kyllä |
| aiemmin-hankitut-ammat-tutkinnon-osat | [[AiemminHankittuAmmatillinenTutkinnonOsa](#AiemminHankittuAmmatillinenTutkinnonOsa)] | Aiemmin hankittu ammatillinen osaaminen | Ei |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| hankittavat-ammat-tutkinnon-osat | [[HankittavaAmmatillinenOsaaminen](#HankittavaAmmatillinenOsaaminen)] | Hankittavan ammatillisen osaamisen hankkimisen tiedot | Ei |
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| aiemmin-hankitut-paikalliset-tutkinnon-osat | [[AiemminHankittuPaikallinenTutkinnonOsa](#AiemminHankittuPaikallinenTutkinnonOsa)] | Aiemmin hankittu paikallinen tutkinnon osa | Ei |
| aiemmin-hankitut-yhteiset-tutkinnon-osat | [[AiemminHankittuYhteinenTutkinnonOsa](#AiemminHankittuYhteinenTutkinnonOsa)] | Aiemmin hankitut yhteiset tutkinnon osat (YTO) | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluoikeus-oid | Opiskeluoikeuden oid, muotoa 1.2.246.562.x.y | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Kyllä |
| hankittavat-yhteiset-tutkinnon-osat | [[HankittavaYTO](#HankittavaYTO)] | Hankittavan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| hankittavat-paikalliset-tutkinnon-osat | [[HankittavaPaikallinenTutkinnonOsa](#HankittavaPaikallinenTutkinnonOsa)] | Hankittavat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| oppija-oid | OID-tunniste muotoa 1.2.246.562.x.y | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |

### Arvioija  

Arvioija

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Arvioijan nimi | Kyllä |
| organisaatio | [Organisaatio](#Organisaatio) | Arvioijan organisaatio | Kyllä |

### AiemminHankittuYhteinenTutkinnonOsa  

Aiemmin hankittu yhteinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tarkentavat-tiedot-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| osa-alueet | [[AiemminHankitunYTOOsaAlue](#AiemminHankitunYTOOsaAlue)] | YTO osa-alueet | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| tarkentavat-tiedot-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

###   



### HankittavaPaikallinenTutkinnonOsaLuonti  

Hankittavan paikallisen tutkinnon osan tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

### KoulutuksenJarjestajaOrganisaatio  

Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppilaitos-oid | OID-tunniste muotoa 1.2.246.562.x.y | Mikäli kyseessä oppilaitos, oppilaitoksen oid-tunniste<br>       Opintopolku-palvelussa. | Ei |

###   



### HOKS  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Kyllä |
| aiemmin-hankitut-ammat-tutkinnon-osat | [[AiemminHankittuAmmatillinenTutkinnonOsa](#AiemminHankittuAmmatillinenTutkinnonOsa)] | Aiemmin hankittu ammatillinen osaaminen | Ei |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| hankittavat-ammat-tutkinnon-osat | [[HankittavaAmmatillinenOsaaminen](#HankittavaAmmatillinenOsaaminen)] | Hankittavan ammatillisen osaamisen hankkimisen tiedot | Ei |
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| aiemmin-hankitut-paikalliset-tutkinnon-osat | [[AiemminHankittuPaikallinenTutkinnonOsa](#AiemminHankittuPaikallinenTutkinnonOsa)] | Aiemmin hankittu paikallinen tutkinnon osa | Ei |
| aiemmin-hankitut-yhteiset-tutkinnon-osat | [[AiemminHankittuYhteinenTutkinnonOsa](#AiemminHankittuYhteinenTutkinnonOsa)] | Aiemmin hankitut yhteiset tutkinnon osat (YTO) | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluoikeus-oid | Opiskeluoikeuden oid, muotoa 1.2.246.562.x.y | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Kyllä |
| hankittavat-yhteiset-tutkinnon-osat | [[HankittavaYTO](#HankittavaYTO)] | Hankittavan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| hankittavat-paikalliset-tutkinnon-osat | [[HankittavaPaikallinenTutkinnonOsa](#HankittavaPaikallinenTutkinnonOsa)] | Hankittavat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| eid | Merkkijono | HOKSin generoitu ulkoinen tunniste eHOKS-järjestelmässä | Kyllä |
| oppija-oid | OID-tunniste muotoa 1.2.246.562.x.y | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |
| tutkinto | [Tutkinto](#Tutkinto) | Tutkinnon tiedot ePerusteet palvelussa | Ei |

### HankittavaPaikallinenTutkinnonOsa  

Hankittava paikallinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

###   



### AiemminHankitunYTOOsaAlue  

AiemminHankitun YTOn osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alue-koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| osa-alue-koodi-versio | Kokonaisluku | Osa-alueen Koodisto-koodi-URIn versio (ammatillisenoppiaineet) | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvauksen (suoraan/arvioijien kautta/näyttö)<br>    koodi-uri. Koodisto Osaamisen todentamisen prosessi, eli muotoa<br>    osaamisentodentamisenprosessi_xxxx | Kyllä |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>    (Osaamisen todentamisen prosessi) | Kyllä |
| tarkentavat-tiedot | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### HankitunOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen
    osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koulutuksen-jarjestaja-arvioijat | [[KoulutuksenJarjestajaArvioija](#KoulutuksenJarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| nayttoymparisto | [Nayttoymparisto](#Nayttoymparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| tyoelama-arvioijat | [[TyoelamaArvioija](#TyoelamaArvioija)] | Näytön tai<br>    osaamisen osoittamisen arvioijat | Ei |
| osa-alueet | [[KoodistoKoodi](#KoodistoKoodi)] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueiden Koodisto-koodi-URIt<br>         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxxesim. ammatillisenoppiaineet_etk | Ei |
| keskeiset-tyotehtavat-naytto | [Merkkijono] | Keskeiset<br>    työtehtävät | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnotLuonti  

Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### KoodistoKoodi  

Koodisto Koodi

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koodi-uri | Merkkijono | Koodisto-koodi URI | Kyllä |
| koodi-versio | Kokonaisluku | Koodisto-koodin versio | Kyllä |

### Organisaatio  

Organisaatio

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Mikäli organisaatiolla on y-tunnus,<br>    organisaation y-tunnus | Ei |

### AiemminHankittuPaikallinenTutkinnonOsa  

Aiemmin hankittu yhteinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Kyllä |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| tarkentavat-tiedot-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| tarkentavat-tiedot-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### TodennettuArviointiLisatiedot  

Mikäli arvioijan kautta todennettu, annetaan myös arvioijan lisätiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| lahetetty-arvioitavaksi | Päivämäärä | Päivämäärä, jona<br>    lähetetty arvioitavaksi, muodossa YYYY-MM-DD | Ei |
| aiemmin-hankitun-osaamisen-arvioijat | [[KoulutuksenJarjestajaArvioija](#KoulutuksenJarjestajaArvioija)] | Mikäli todennettu arvioijan kautta, annetaan arvioijien tiedot. | Ei |

### HOKSKentanPaivitys  

HOKS-dokumentin ylikirjoitus (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| opiskeluoikeus-oid | Opiskeluoikeuden oid, muotoa 1.2.246.562.x.y | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001' | Ei |
| aiemmin-hankitut-ammat-tutkinnon-osat | [[AiemminHankittuAmmatillinenTutkinnonOsa](#AiemminHankittuAmmatillinenTutkinnonOsa)] | Aiemmin hankittu ammatillinen osaaminen | Ei |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Ei |
| hankittavat-ammat-tutkinnon-osat | [[HankittavaAmmatillinenOsaaminen](#HankittavaAmmatillinenOsaaminen)] | Hankittavan ammatillisen osaamisen hankkimisen tiedot | Ei |
| luotu | Aikaleima | HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| aiemmin-hankitut-paikalliset-tutkinnon-osat | [[AiemminHankittuPaikallinenTutkinnonOsa](#AiemminHankittuPaikallinenTutkinnonOsa)] | Aiemmin hankittu paikallinen tutkinnon osa | Ei |
| aiemmin-hankitut-yhteiset-tutkinnon-osat | [[AiemminHankittuYhteinenTutkinnonOsa](#AiemminHankittuYhteinenTutkinnonOsa)] | Aiemmin hankitut yhteiset tutkinnon osat (YTO) | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| hankittavat-yhteiset-tutkinnon-osat | [[HankittavaYTO](#HankittavaYTO)] | Hankittavan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| hankittavat-paikalliset-tutkinnon-osat | [[HankittavaPaikallinenTutkinnonOsa](#HankittavaPaikallinenTutkinnonOsa)] | Hankittavat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| oppija-oid | OID-tunniste muotoa 1.2.246.562.x.y | Oppijan tunniste Opintopolku-ympäristössä | Ei |

### HankittavaPaikallinenTutkinnonOsaPaivitys  

Hankittavan paikallisen tutkinnon osan tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| hankitun-osaamisen-naytto | [[HankitunPaikallisenOsaamisenNaytto](#HankitunPaikallisenOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

### OsaamisenHankkimistapa  

Osaamisen hankkimisen tapa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestajan-edustaja | [Oppilaitoshenkilo](#Oppilaitoshenkilo) | Koulutuksen järjestäjän edustaja | Ei |
| osaamisen-hankkimistapa-koodi-uri | Merkkijono, esim. osaamisenhankkimistapa_oppisopimus | Osaamisen hankkimisen Koodisto-koodi-URI (osaamisenhankkimistapa)<br>    eli muotoa osaamisenhankkimistapa_xxx eli esim.<br>    osaamisenhankkimistapa_koulutussopimus | Kyllä |
| muut-oppimisymparisto | [[MuuOppimisymparisto](#MuuOppimisymparisto)] | Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen liittyvät tiedot | Ei |
| ajanjakson-tarkenne | Merkkijono | Tarkentava teksti ajanjaksolle, jos useita aikavälillä. | Ei |
| osaamisen-hankkimistapa-koodi-versio | Kokonaisluku | Koodisto-koodin versio, koodistolle osaamisenhankkimistapa | Kyllä |
| hankkijan-edustaja | [Oppilaitoshenkilo](#Oppilaitoshenkilo) | Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja | Ei |
| tyopaikalla-jarjestettava-koulutus | [TyopaikallaJarjestettavaKoulutus](#TyopaikallaJarjestettavaKoulutus) | Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot. Tämä tieto tuodaan, jos hankkimistapa on oppisopimuskoulutus tai koulutussopimus. | Ei |
| alku | Päivämäärä | Alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### HankittavaAmmatillinenOsaaminenLuonti  

Hankittavan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### Nayttoymparisto  

Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Mikäli organisaatiolla on y-tunnus,<br>    organisaation y-tunnus | Ei |
| kuvaus | Merkkijono | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Ei |

### HankittavaAmmatillinenOsaaminenPaivitys  

Hankittavan ammatillisen osaamisen tiedot merkintää ylikirjoittaessa
    (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| hankitun-osaamisen-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### TyoelamaOrganisaatio  

Työelämän toimijan organisaatio, jossa näyttö tai osaamisen osoittaminen
     annetaan

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Organisaation nimi | Kyllä |
| y-tunnus | Merkkijono | Mikäli organisaatiolla on y-tunnus,<br>    organisaation y-tunnus | Ei |

### HankittavaYTO  

Hankittavan yhteinen tutkinnon osan (YTO) tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### Oppilaitoshenkilo  

Oppilaitoksen edustaja

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Ei |
| oppilaitos-oid | OID-tunniste muotoa 1.2.246.562.x.y | Oppilaitoksen oid-tunniste Opintopolku-palvelussa. | Kyllä |

### OpiskeluvalmiuksiaTukevatOpinnot  

Opiskeluvalmiuksia tukevat opinnot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Opintojen nimi | Kyllä |
| kuvaus | Merkkijono | Opintojen kuvaus | Kyllä |
| alku | Päivämäärä | Opintojen alkupäivämäärä muodossa YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Opintojen loppupäivämäärä muodossa YYYY-MM-DD | Kyllä |

### YhteisenTutkinnonOsanOsaAlue  

Hankittavan yhteinen tutkinnon osan (YTO) osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alue-koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| osa-alue-koodi-versio | Kokonaisluku | Osa-alueen Koodisto-koodi-URIn versio (ammatillisenoppiaineet) | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| hankitun-osaamisen-naytto | [[HankitunYTOOsaamisenNaytto](#HankitunYTOOsaamisenNaytto)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### NaytonJarjestaja  

Näytön tai osaamisen osoittamisen järjestäjä

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppilaitos-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### HankitunYTOOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen
    osoittaminen

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| koulutuksen-jarjestaja-arvioijat | [[KoulutuksenJarjestajaArvioija](#KoulutuksenJarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| osaamistavoitteet | [Merkkijono] | Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan.Tunnisteen tyyppi voi vielä päivittyä ja tähän saattaa tulla vielä Yksilölliset arvioinnin kriteerit | Ei |
| nayttoymparisto | [Nayttoymparisto](#Nayttoymparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| tyoelama-arvioijat | [[TyoelamaArvioija](#TyoelamaArvioija)] | Näytön tai<br>    osaamisen osoittamisen arvioijat | Ei |
| osa-alueet | [[KoodistoKoodi](#KoodistoKoodi)] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueiden Koodisto-koodi-URIt<br>         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxxesim. ammatillisenoppiaineet_etk | Ei |
| keskeiset-tyotehtavat-naytto | [Merkkijono] | Keskeiset<br>    työtehtävät | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |

### AiemminHankittuAmmatillinenTutkinnonOsa  

Aiemmin hankittu yhteinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tarkentavat-tiedot-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| tarkentavat-tiedot-naytto | [[HankitunOsaamisenNaytto](#HankitunOsaamisenNaytto)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### TyopaikallaJarjestettavaKoulutus  

Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| vastuullinen-ohjaaja | [VastuullinenOhjaaja](#VastuullinenOhjaaja) | Vastuullinen työpaikkaohjaaja | Kyllä |
| tyopaikan-nimi | Merkkijono | Työpaikan nimi | Kyllä |
| tyopaikan-y-tunnus | Merkkijono | Työpaikan y-tunnus | Ei |
| muut-osallistujat | [[Henkilo](#Henkilo)] | Muut ohjaukseen osallistuvat henkilöt | Ei |
| keskeiset-tyotehtavat | [Merkkijono] | Keskeiset työtehtävät | Kyllä |
| lisatiedot | Totuusarvo | Lisätietoja, esim. opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Kyllä |

###   



### KoulutuksenJarjestajaArvioija  

Työelämän arvioija

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Arvioijan nimi | Kyllä |
| organisaatio | [KoulutuksenJarjestajaOrganisaatio](#KoulutuksenJarjestajaOrganisaatio) | KoulutuksenJarjestajan arvioijan organisaatio | Kyllä |

### HankittavaYTOPaivitys  

Hankittavan yhteinen tutkinnon osa tiedot merkintää ylikirjoittaessa (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | OID-tunniste muotoa 1.2.246.562.x.y | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

### TyoelamaArvioija  

Työelämän arvioija

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Arvioijan nimi | Kyllä |
| organisaatio | [TyoelamaOrganisaatio](#TyoelamaOrganisaatio) | Työelämän arvioijan organisaatio | Kyllä |

### HoksToimija  

Hoksin hyväksyjä tai päivittäjä koulutusjärjestäjän organisaatiossa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| nimi | Merkkijono | Henkilön nimi | Kyllä |

