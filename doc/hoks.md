# HOKS API doc
Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin esittämiseen.

Generoitu 22.06.2022 14.00

###   



### HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus  

Hankittavan ammatillisen osaamisen tiedot (POST, PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| olennainen-seikka | Totuusarvo | Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai osoittamisessa. | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| opetus-ja-ohjaus-maara | (constrained Num "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.") | Tutkinnon osaan suunnitellun opetuksen ja ohjauksen määrä tunteina. | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapaLuontiJaMuokkaus](#OsaamisenHankkimistapaLuontiJaMuokkaus)] | Osaamisen hankkimistavat | Ei |
| osaamisen-osoittaminen | [[OsaamisenOsoittaminenLuontiJaMuokkaus](#OsaamisenOsoittaminenLuontiJaMuokkaus)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### MuuOppimisymparisto  

Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppimisymparisto-koodi-uri | Koodistokoodin uri, merkkijono, esim. oppimisymparistot_0001 | Oppimisympäristön tarkenne, eHOKS Koodisto-koodi-URI, koodisto<br>    oppimisympäristöt eli muotoa oppimisymparistot_xxxx, esim.<br>    oppimisymparistot_0001 | Kyllä |
| oppimisymparisto-koodi-versio | Kokonaisluku | Koodisto-koodin versio, koodistolle oppimisympäristöt | Kyllä |
| alku | Päivämäärä | Muussa oppimisympäristössä tapahtuvan osaamisen hankkimisen<br>    aloituspäivämäärä. | Kyllä |
| loppu | Päivämäärä | Muussa oppimisympäristössä tapahtuvan osaamisen hankkimisen<br>    päättymispäivämäärä. | Kyllä |

###   



### YhteinenTutkinnonOsa  

Yhteinen Tutkinnon osa (YTO)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| module-id | Uuid | Tietorakenteen yksilöivä tunniste esimerkiksi tiedon jakamista varten | Kyllä |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAlue](#YhteisenTutkinnonOsanOsaAlue)] | YTO osa-alueet | Kyllä |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |

###   



### OsaamisenOsoittaminen  

Hankittavaan tutkinnon osaan tai yhteisen tutkinnon osan osa-alueeseen
    sisältyvä osaamisen osoittaminen: näyttö tai muu osaamisen osoittaminen.

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| nayttoymparisto | [Nayttoymparisto](#Nayttoymparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| koulutuksen-jarjestaja-osaamisen-arvioijat | [[KoulutuksenJarjestajaArvioija](#KoulutuksenJarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| sisallon-kuvaus | [Merkkijono] | Tiivis kuvaus (esim. lista) työtilanteista ja työprosesseista, joiden<br>    avulla ammattitaitovaatimusten tai osaamistavoitteiden mukainen osaaminen<br>    osoitetaan. Vastaavat tiedot muusta osaamisen osoittamisesta siten, että<br>    tieto kuvaa sovittuja tehtäviä ja toimia, joiden avulla osaaminen<br>    osoitetaan. | Kyllä |
| module-id | Uuid | Tietorakenteen yksilöivä tunniste esimerkiksi tiedon jakamista varten | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tutkinnon osan tai osa-alueen perusteisiin sisältyvät<br>    ammattitaitovaatimukset tai osaamistavoitteet, joista opiskelijan kohdalla<br>    poiketaan. | Ei |
| osa-alueet | [[KoodistoKoodi](#KoodistoKoodi)] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueiden Koodisto-koodi-URIt<br>         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxxesim. ammatillisenoppiaineet_etk | Ei |
| tyoelama-osaamisen-arvioijat | [[TyoelamaOsaamisenArvioija](#TyoelamaOsaamisenArvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| yksilolliset-kriteerit | [Merkkijono] | Ammattitaitovaatimus tai osaamistavoite, johon yksilölliset<br>    arviointikriteerit kohdistuvat ja yksilölliset arviointikriteerit kyseiseen<br>    ammattitaitovaatimukseen tai osaamistavoitteeseen. | Ei |

### OsaamisenOsoittaminenLuontiJaMuokkaus  

Osaamisen hankkimisen tavan luonti ja muokkaus (POST, PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| nayttoymparisto | [Nayttoymparisto](#Nayttoymparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| koulutuksen-jarjestaja-osaamisen-arvioijat | [[KoulutuksenJarjestajaArvioija](#KoulutuksenJarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| sisallon-kuvaus | [Merkkijono] | Tiivis kuvaus (esim. lista) työtilanteista ja työprosesseista, joiden<br>    avulla ammattitaitovaatimusten tai osaamistavoitteiden mukainen osaaminen<br>    osoitetaan. Vastaavat tiedot muusta osaamisen osoittamisesta siten, että<br>    tieto kuvaa sovittuja tehtäviä ja toimia, joiden avulla osaaminen<br>    osoitetaan. | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tutkinnon osan tai osa-alueen perusteisiin sisältyvät<br>    ammattitaitovaatimukset tai osaamistavoitteet, joista opiskelijan kohdalla<br>    poiketaan. | Ei |
| osa-alueet | [[KoodistoKoodi](#KoodistoKoodi)] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueiden Koodisto-koodi-URIt<br>         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxxesim. ammatillisenoppiaineet_etk | Ei |
| tyoelama-osaamisen-arvioijat | [[TyoelamaOsaamisenArvioija](#TyoelamaOsaamisenArvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| yksilolliset-kriteerit | [Merkkijono] | Ammattitaitovaatimus tai osaamistavoite, johon yksilölliset<br>    arviointikriteerit kohdistuvat ja yksilölliset arviointikriteerit kyseiseen<br>    ammattitaitovaatimukseen tai osaamistavoitteeseen. | Ei |

### HOKSPaivitys  

HOKS-dokumentin ylikirjoitus (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d+$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001'. | Ei |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| osaamisen-hankkimisen-tarve | Totuusarvo | Tutkintokoulutuksen ja muun tarvittavan<br>                               ammattitaidon hankkimisen tarve; osaamisen<br>                               tunnistamis- ja tunnustamisprosessin<br>                               lopputulos. | Ei |
| puhelinnumero | Merkkijono | Oppijan puhelinnumero, merkkijono. | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| osaamisen-saavuttamisen-pvm | Päivämäärä | HOKSin osaamisen saavuttamisen ajankohta muodossa YYYY-MM-DD | Ei |
| oppija-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Oppijan tunniste Opintopolku-ympäristössä | Ei |

###   



### Henkilo  

Henkilö

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| organisaatio | [Organisaatio](#Organisaatio) | Henkilön organisaatio | Kyllä |
| nimi | Merkkijono | Henkilön nimi | Kyllä |
| rooli | Merkkijono | Henkilön rooli | Ei |

### shallow-delete-hoks  



| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppilaitos-oid | Merkkijono |  | Kyllä |

###   



### HOKSKorvaus  

HOKS-dokumentin ylikirjoitus (PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Kyllä |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d+$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001'. | Ei |
| aiemmin-hankitut-ammat-tutkinnon-osat | [[AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus](#AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus)] | Aiemmin hankittu ammatillinen osaaminen | Ei |
| osaamisen-hankkimisen-tarve | Totuusarvo | Tutkintokoulutuksen ja muun tarvittavan<br>                               ammattitaidon hankkimisen tarve; osaamisen<br>                               tunnistamis- ja tunnustamisprosessin<br>                               lopputulos. | Kyllä |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| hankittavat-ammat-tutkinnon-osat | [[HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus](#HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus)] | Hankittavan ammatillisen osaamisen hankkimisen tiedot | Ei |
| aiemmin-hankitut-paikalliset-tutkinnon-osat | [[AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus](#AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus)] | Aiemmin hankittu paikallinen tutkinnon osa | Ei |
| aiemmin-hankitut-yhteiset-tutkinnon-osat | [[AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus](#AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus)] | Aiemmin hankitut yhteiset tutkinnon osat (YTO) | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| puhelinnumero | Merkkijono | Oppijan puhelinnumero, merkkijono. | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| hankittavat-yhteiset-tutkinnon-osat | [[HankittavaYTOLuontiJaMuokkaus](#HankittavaYTOLuontiJaMuokkaus)] | Hankittavan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| hankittavat-paikalliset-tutkinnon-osat | [[HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus](#HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus)] | Hankittavat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| osaamisen-saavuttamisen-pvm | Päivämäärä | HOKSin osaamisen saavuttamisen ajankohta muodossa YYYY-MM-DD | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| oppija-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Oppijan tunniste Opintopolku-ympäristössä | Ei |

### AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus  

Aiemmin hankitun paikallisen osaamisen tiedot (POST, PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Kyllä |
| olennainen-seikka | Totuusarvo | Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai osoittamisessa. | Ei |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Kyllä |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| opetus-ja-ohjaus-maara | (constrained Num "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.") | Tutkinnon osaan suunnitellun opetuksen ja ohjauksen määrä tunteina. | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| tarkentavat-tiedot-osaamisen-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| tarkentavat-tiedot-naytto | [[OsaamisenOsoittaminenLuontiJaMuokkaus](#OsaamisenOsoittaminenLuontiJaMuokkaus)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### OsaamisenOsoittaminenPatch  

Osaamisen hankkimisen tavan luonti muokkaus (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| jarjestaja | [NaytonJarjestaja](#NaytonJarjestaja) | Näytön tai osaamisen osoittamisen järjestäjä | Ei |
| nayttoymparisto | [Nayttoymparisto](#Nayttoymparisto) | Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan | Kyllä |
| koulutuksen-jarjestaja-osaamisen-arvioijat | [[KoulutuksenJarjestajaArvioija](#KoulutuksenJarjestajaArvioija)] | Näytön tai osaamisen osoittamisen<br>    arvioijat | Ei |
| sisallon-kuvaus | [Merkkijono] | Tiivis kuvaus (esim. lista) työtilanteista ja työprosesseista, joiden<br>    avulla ammattitaitovaatimusten tai osaamistavoitteiden mukainen osaaminen<br>    osoitetaan. Vastaavat tiedot muusta osaamisen osoittamisesta siten, että<br>    tieto kuvaa sovittuja tehtäviä ja toimia, joiden avulla osaaminen<br>    osoitetaan. | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tutkinnon osan tai osa-alueen perusteisiin sisältyvät<br>    ammattitaitovaatimukset tai osaamistavoitteet, joista opiskelijan kohdalla<br>    poiketaan. | Ei |
| osa-alueet | [[KoodistoKoodi](#KoodistoKoodi)] | Suoritettavan tutkinnon osan näyttöön sisältyvänyton osa-alueiden Koodisto-koodi-URIt<br>         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxxesim. ammatillisenoppiaineet_etk | Ei |
| tyoelama-osaamisen-arvioijat | [[TyoelamaOsaamisenArvioija](#TyoelamaOsaamisenArvioija)] | Näytön tai osaamisen osoittamisen arvioijat | Ei |
| alku | Päivämäärä | Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| loppu | Päivämäärä | Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa<br>    YYYY-MM-DD | Kyllä |
| yksilolliset-kriteerit | [Merkkijono] | Ammattitaitovaatimus tai osaamistavoite, johon yksilölliset<br>    arviointikriteerit kohdistuvat ja yksilölliset arviointikriteerit kyseiseen<br>    ammattitaitovaatimukseen tai osaamistavoitteeseen. | Ei |

### HOKSLuonti  

HOKS-dokumentin arvot uutta merkintää luotaessa (POST)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Kyllä |
| aiemmin-hankitut-ammat-tutkinnon-osat | [[AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus](#AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus)] | Aiemmin hankittu ammatillinen osaaminen | Ei |
| osaamisen-hankkimisen-tarve | Totuusarvo | Tutkintokoulutuksen ja muun tarvittavan<br>                               ammattitaidon hankkimisen tarve; osaamisen<br>                               tunnistamis- ja tunnustamisprosessin<br>                               lopputulos. | Kyllä |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| hankittavat-ammat-tutkinnon-osat | [[HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus](#HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus)] | Hankittavan ammatillisen osaamisen hankkimisen tiedot | Ei |
| aiemmin-hankitut-paikalliset-tutkinnon-osat | [[AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus](#AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus)] | Aiemmin hankittu paikallinen tutkinnon osa | Ei |
| aiemmin-hankitut-yhteiset-tutkinnon-osat | [[AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus](#AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus)] | Aiemmin hankitut yhteiset tutkinnon osat (YTO) | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| puhelinnumero | Merkkijono | Oppijan puhelinnumero, merkkijono. | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d+$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001'. | Kyllä |
| hankittavat-yhteiset-tutkinnon-osat | [[HankittavaYTOLuontiJaMuokkaus](#HankittavaYTOLuontiJaMuokkaus)] | Hankittavan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| hankittavat-paikalliset-tutkinnon-osat | [[HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus](#HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus)] | Hankittavat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| osaamisen-saavuttamisen-pvm | Päivämäärä | HOKSin osaamisen saavuttamisen ajankohta muodossa YYYY-MM-DD | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| oppija-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Oppijan tunniste Opintopolku-ympäristössä | Kyllä |

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
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Ei |
| osa-alueet | [[AiemminHankitunYTOOsaAlue](#AiemminHankitunYTOOsaAlue)] | YTO osa-alueet | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| module-id | Uuid | Tietorakenteen yksilöivä tunniste esimerkiksi tiedon jakamista varten | Kyllä |
| tarkentavat-tiedot-osaamisen-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| tarkentavat-tiedot-naytto | [[OsaamisenOsoittaminen](#OsaamisenOsoittaminen)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### YhteisenTutkinnonOsanOsaAluePatch  

Hankittavan yhteinen tutkinnon osan (YTO) osa-alueen tiedot (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| olennainen-seikka | Totuusarvo | Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai osoittamisessa. | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapaPatch](#OsaamisenHankkimistapaPatch)] | Osaamisen hankkimistavat | Ei |
| osaamisen-osoittaminen | [[OsaamisenOsoittaminenPatch](#OsaamisenOsoittaminenPatch)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| opetus-ja-ohjaus-maara | (constrained Num "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.") | Tutkinnon osan osa-alueeseen suunnitellun opetuksen ja ohjauksen määrä tunteina. | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| osa-alue-koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| osa-alue-koodi-versio | Kokonaisluku | Osa-alueen Koodisto-koodi-URIn versio (ammatillisenoppiaineet) | Kyllä |

### HankittavaYTOPatch  

Hankittavan yhteisen tutkinnnon osan (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| osa-alueet | [[YhteisenTutkinnonOsanOsaAluePatch](#YhteisenTutkinnonOsanOsaAluePatch)] | YTO osa-alueet | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapaPatch](#OsaamisenHankkimistapaPatch)] | Osaamisen hankkimistavat | Ei |
| osaamisen-osoittaminen | [[OsaamisenOsoittaminenPatch](#OsaamisenOsoittaminenPatch)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus  

Aiemmin hankitun yhteisen osaamisen tiedot (POST, PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| tarkentavat-tiedot-osaamisen-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| tarkentavat-tiedot-naytto | [[OsaamisenOsoittaminenLuontiJaMuokkaus](#OsaamisenOsoittaminenLuontiJaMuokkaus)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| osa-alueet | [[AiemminHankitunYTOOsaAlueLuontiJaMuokkaus](#AiemminHankitunYTOOsaAlueLuontiJaMuokkaus)] | YTO osa-alueet | Kyllä |

### HankittavaAmmatillinenTutkinnonOsaPatch  

Hankittavan ammatillisen osaamisen tiedot (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| olennainen-seikka | Totuusarvo | Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai osoittamisessa. | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat) | Kyllä |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapaPatch](#OsaamisenHankkimistapaPatch)] | Osaamisen hankkimistavat | Ei |
| osaamisen-osoittaminen | [[OsaamisenOsoittaminenPatch](#OsaamisenOsoittaminenPatch)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| opetus-ja-ohjaus-maara | (constrained Num "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.") | Tutkinnon osaan suunnitellun opetuksen ja ohjauksen määrä tunteina. | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | Tekstimuotoinen selite ammattitaitovaatimuksista tai osaamistavoitteista poikkeamiseen | Ei |

###   



### AiemminHankittuYhteinenTutkinnonOsaPatch  

Aiemmin hankitun yhteisen osaamisen tiedot (PATCH)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Ei |
| osa-alueet | [[AiemminHankitunYTOOsaAluePatch](#AiemminHankitunYTOOsaAluePatch)] | YTO osa-alueet | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| tarkentavat-tiedot-osaamisen-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| tarkentavat-tiedot-naytto | [[OsaamisenOsoittaminenPatch](#OsaamisenOsoittaminenPatch)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus  

Aiemmin hankitun ammatillisen osaamisen tiedot (POST, PUT)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| tutkinnon-osa-koodi-versio | Kokonaisluku | Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa<br>     (tutkinnonosat) | Kyllä |
| olennainen-seikka | Totuusarvo | Tieto sellaisen seikan olemassaolosta, jonka koulutuksen<br>    järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen<br>    liittyvän osaamisen hankkimisessa tai osoittamisessa. | Ei |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>       (Osaamisen todentamisen prosessi) | Ei |
| tutkinnon-osa-koodi-uri | Merkkijono, esim. tutkinnonosat_123456 | Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa<br>    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.<br>    tutkinnonosat_100002 | Kyllä |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö) | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| tarkentavat-tiedot-osaamisen-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu,<br>       annetaan myös arvioijan lisätiedot | Ei |
| tarkentavat-tiedot-naytto | [[OsaamisenOsoittaminenLuontiJaMuokkaus](#OsaamisenOsoittaminenLuontiJaMuokkaus)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |

### kyselylinkki  



| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| kyselylinkki | Merkkijono |  | Kyllä |
| alkupvm | Päivämäärä |  | Kyllä |
| tyyppi | Merkkijono |  | Kyllä |
| lahetyspvm | Päivämäärä |  | Ei |
| sahkoposti | Merkkijono |  | Ei |
| lahetystila | Merkkijono |  | Ei |

### KoulutuksenJarjestajaOrganisaatio  

Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| oppilaitos-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Mikäli kyseessä oppilaitos, oppilaitoksen oid-tunniste<br>       Opintopolku-palvelussa. | Ei |

###   



### kyselylinkki-lahetys  



| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| kyselylinkki | Merkkijono |  | Kyllä |
| lahetyspvm | Päivämäärä |  | Kyllä |
| sahkoposti | Merkkijono |  | Kyllä |
| lahetystila | Merkkijono |  | Ei |

### HOKS  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| ensikertainen-hyvaksyminen | Päivämäärä | HOKS-dokumentin ensimmäinen hyväksymisaika<br>                                muodossa YYYY-MM-DD | Kyllä |
| opiskeluoikeus-oid | #"^1\.2\.246\.562\.15\.\d+$" | Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa<br>                  '1.2.246.562.15.00000000001'. | Ei |
| aiemmin-hankitut-ammat-tutkinnon-osat | [[AiemminHankittuAmmatillinenTutkinnonOsa](#AiemminHankittuAmmatillinenTutkinnonOsa)] | Aiemmin hankittu ammatillinen osaaminen | Ei |
| versio | Kokonaisluku | HOKS-dokumentin versio | Ei |
| sahkoposti | Merkkijono | Oppijan sähköposti, merkkijono. | Ei |
| hankittavat-ammat-tutkinnon-osat | [[HankittavaAmmatillinenTutkinnonOsa](#HankittavaAmmatillinenTutkinnonOsa)] | Hankittavan ammatillisen osaamisen hankkimisen tiedot | Ei |
| manuaalisyotto | Totuusarvo | Tieto, onko HOKS tuotu manuaalisyötön kautta | Ei |
| aiemmin-hankitut-paikalliset-tutkinnon-osat | [[AiemminHankittuPaikallinenTutkinnonOsa](#AiemminHankittuPaikallinenTutkinnonOsa)] | Aiemmin hankittu paikallinen tutkinnon osa | Ei |
| aiemmin-hankitut-yhteiset-tutkinnon-osat | [[AiemminHankittuYhteinenTutkinnonOsa](#AiemminHankittuYhteinenTutkinnonOsa)] | Aiemmin hankitut yhteiset tutkinnon osat (YTO) | Ei |
| urasuunnitelma-koodi-uri | Merkkijono, urasuunnitelman koodistokoodi uri, esim. urasuunnitelma_0001 | Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto<br>    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.<br>    urasuunnitelma_0001 | Ei |
| osaamisen-hankkimisen-tarve | Totuusarvo | Tutkintokoulutuksen ja muun tarvittavan<br>                               ammattitaidon hankkimisen tarve; osaamisen<br>                               tunnistamis- ja tunnustamisprosessin<br>                               lopputulos. | Ei |
| puhelinnumero | Merkkijono | Oppijan puhelinnumero, merkkijono. | Ei |
| hyvaksytty | Aikaleima | HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| hankittavat-yhteiset-tutkinnon-osat | [[HankittavaYTO](#HankittavaYTO)] | Hankittavan yhteisen tutkinnon osan hankkimisen tiedot | Ei |
| hankittavat-paikalliset-tutkinnon-osat | [[HankittavaPaikallinenTutkinnonOsa](#HankittavaPaikallinenTutkinnonOsa)] | Hankittavat paikallisen tutkinnon osat | Ei |
| urasuunnitelma-koodi-versio | Kokonaisluku | Opiskelijan tavoitteen Koodisto-koodin versio | Ei |
| paivitetty | Aikaleima | HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ | Ei |
| osaamisen-saavuttamisen-pvm | Päivämäärä | HOKSin osaamisen saavuttamisen ajankohta muodossa YYYY-MM-DD | Ei |
| opiskeluvalmiuksia-tukevat-opinnot | [[OpiskeluvalmiuksiaTukevatOpinnot](#OpiskeluvalmiuksiaTukevatOpinnot)] | Opiskeluvalmiuksia tukevat opinnot | Ei |
| eid | Merkkijono | HOKSin generoitu ulkoinen tunniste eHOKS-järjestelmässä | Kyllä |
| oppija-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Oppijan tunniste Opintopolku-ympäristössä | Ei |

### HankittavaPaikallinenTutkinnonOsa  

Hankittava paikallinen tutkinnon osa

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| olennainen-seikka | Totuusarvo | Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai osoittamisessa. | Ei |
| laajuus | Kokonaisluku | Tutkinnon osan laajuus | Ei |
| nimi | Merkkijono | Tutkinnon osan nimi | Ei |
| tavoitteet-ja-sisallot | Merkkijono | Paikallisen tutkinnon osan ammattitaitovaatimukset taiosaamistavoitteet | Ei |
| amosaa-tunniste | Merkkijono | Tunniste ePerusteet AMOSAA -palvelussa | Ei |
| osaamisen-hankkimistavat | [[OsaamisenHankkimistapa](#OsaamisenHankkimistapa)] | Osaamisen hankkimistavat | Ei |
| osaamisen-osoittaminen | [[OsaamisenOsoittaminen](#OsaamisenOsoittaminen)] | Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen | Ei |
| opetus-ja-ohjaus-maara | (constrained Num "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.") | Tutkinnon osaan suunnitellun opetuksen ja ohjauksen määrä tunteina. | Ei |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| module-id | Uuid | Tietorakenteen yksilöivä tunniste esimerkiksi tiedon jakamista varten | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |

###   



### Keskeytymisajanjakso  

Ajanjakso, jolloin tutkinnon osan osaamisen hankkiminen kyseisellä työpaikalla on ollut keskeytyneenä.

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| alku | Päivämäärä | Keskeytymisajanjakson aloituspäivämäärä. | Kyllä |
| loppu | Päivämäärä | Keskeytymisajanjakson päättymispäivämäärä. | Ei |

### AiemminHankitunYTOOsaAlue  

AiemminHankitun YTOn osa-alueen tiedot

| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
| valittu-todentamisen-prosessi-koodi-versio | Kokonaisluku | Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio<br>    (Osaamisen todentamisen prosessi) | Kyllä |
| olennainen-seikka | Totuusarvo | Tieto sellaisen seikan<br>    olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon<br>    osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai<br>    osoittamisessa. | Ei |
| valittu-todentamisen-prosessi-koodi-uri | Merkkijono, muotoa osaamisentodentamisenprosessi_0003 | Todentamisen prosessin kuvauksen (suoraan/arvioijien kautta/näyttö)<br>    koodi-uri. Koodisto Osaamisen todentamisen prosessi, eli muotoa<br>    osaamisentodentamisenprosessi_xxxx | Kyllä |
| koulutuksen-jarjestaja-oid | #"^1\.2\.246\.562\.[0-3]\d\.\d+$" | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Ei |
| osa-alue-koodi-uri | Merkkijono, esim. ammatillisenoppiaineet_aa | Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet) | Kyllä |
| module-id | Uuid | Tietorakenteen yksilöivä tunniste esimerkiksi tiedon jakamista varten | Kyllä |
| vaatimuksista-tai-tavoitteista-poikkeaminen | Merkkijono | vaatimuksista tai osaamistavoitteista poikkeaminen | Ei |
| osa-alue-koodi-versio | Kokonaisluku | Osa-alueen Koodisto-koodi-URIn versio (ammatillisenoppiaineet) | Kyllä |
| tarkentavat-tiedot-osaamisen-arvioija | [TodennettuArviointiLisatiedot](#TodennettuArviointiLisatiedot) | Mikäli arvioijan kautta todennettu, annetaan myös arvioijan lisätiedot | Ei |
| tarkentavat-tiedot-naytto | [[OsaamisenOsoittaminen](#OsaamisenOsoittaminen)] | Mikäli valittu näytön kautta, tuodaan myös näytön tiedot. | Ei |

### OsaamisenHankkimistapaLuontiJaMuokkaus  



| Nimi | Tyyppi | Selite | Vaaditaan |
| ---- | ------ | ------ | --------- |
