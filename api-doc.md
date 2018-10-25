eHOKS backend
=============
Backend for eHOKS

**Version:** 0.0.1

### /ehoks-backend/api/v1/healthcheck
---
##### ***GET***
**Summary:** Service healthcheck status

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [HealthcheckStatus](#healthcheckstatus) |

### /ehoks-backend/api/v1/session/user-info
---
##### ***GET***
**Summary:** Palauttaa istunnon käyttäjän tiedot

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030673](#response4030673) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030674](#response4030674) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030675](#response4030675) |

##### ***OPTIONS***
**Responses**

| Code | Description |
| ---- | ----------- |
| default |  |

##### ***DELETE***
**Summary:** Uloskirjautuminen.

**Responses**

| Code | Description |
| ---- | ----------- |
| default |  |

### /ehoks-backend/api/v1/session/opintopolku/
---
##### ***GET***
**Summary:** Opintopolkutunnistautumisen päätepiste

**Description:** Opintopolkutunnistautumisen jälkeen päädytään tänne.
                    Sovellus ottaa käyttäjän tunnistetiedot headereista ja
                    huolimatta metodin tyypistä luodaan uusi istunto. Tämä
                    ulkoisen järjestelmän vuoksi.
                    Lopuksi käyttäjä ohjataan käyttöliittymän urliin.

**Responses**

| Code | Description |
| ---- | ----------- |
| default |  |

### /ehoks-backend/api/v1/hoks/{id}
---
##### ***GET***
**Summary:** Palauttaa HOKSin

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030676](#response4030676) |

##### ***PUT***
**Summary:** Päivittää olemassa olevaa HOKSia

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

**Responses**

| Code | Description |
| ---- | ----------- |
| default |  |

### /ehoks-backend/api/v1/hoks
---
##### ***POST***
**Summary:** Luo uuden HOKSin

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| HOKSArvot | body | Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti | Yes | [HOKSArvot](#hoksarvot) |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030677](#response4030677) |

### /ehoks-backend/api/v1/lokalisointi
---
##### ***GET***
**Summary:** Hakee lokalisoinnin tulokset lokalisointipalvelusta

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| category | query |  | No | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030678](#response4030678) |

### /ehoks-backend/api/v1/external/koodistokoodi/{uri}/{versio}
---
##### ***GET***
**Summary:** Hakee koodisto koodin tietoja Koodisto-palvelusta

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| uri | path |  | Yes | string |
| versio | path |  | Yes | long |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030679](#response4030679) |

### /ehoks-backend/api/v1/external/eperusteet/
---
##### ***GET***
**Summary:** Hakee perusteiden tietoja ePerusteet-palvelusta

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| nimi | query |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030680](#response4030680) |

### /ehoks-backend/api/v1/external/koski/oppija
---
##### ***GET***
**Summary:** Hakee oppijan tietoja Koski-palvelusta

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030681](#response4030681) |

### /ehoks-backend/api/v1/misc/environment
---
##### ***GET***
**Summary:** Palauttaa ympäristön tiedot ja asetukset

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030682](#response4030682) |

### Models
---

### Arvioija  

Arvioija

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string | Arvioijan nimi | Yes |
| rooli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| organisaatio | [Organisaatio](#organisaatio) |  | Yes |

### Arviointikriteeri  

Arviointikriteeri

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvosana | long | Arvosana | Yes |
| kuvaus | string | Arviointikriteerin kuvaus | Yes |

### DateRange  

Näytön tai osaamisen osoittamisen ajankohta

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date | Alkupäivämäärä muodossa YYYY-MM-DD | Yes |
| loppu | date | Loppupäivämäärä muodoss YYYY-MM-DD | Yes |

### Environment  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |
| eperusteet-peruste-url | string |  | Yes |
| opintopolku-logout-url | string |  | Yes |

### ExtendedKoodistoKoodi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tila | string |  | Yes |
| resourceUri | string |  | Yes |
| koodiArvo | string |  | Yes |
| voimassaLoppuPvm | string |  | Yes |
| koodisto | [KoodistoItem](#koodistoitem) |  | Yes |
| voimassaAlkuPvm | string |  | Yes |
| versio | long |  | Yes |
| koodiUri | string |  | Yes |
| paivitysPvm | long |  | Yes |
| version | long |  | Yes |
| metadata | [ [KoodiMetadata](#koodimetadata) ] |  | Yes |

### HOKS  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paivittajan-oid | string | HOKS-dokumenttia viimeksi päivittäneen henkilön yksilöivä tunniste Koski-järjestelmässä | Yes |
| luotu | dateTime | HOKS-dokumentin luontiaika | Yes |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) |  | Yes |
| hyvaksytty | dateTime | HOKS-dokumentin hyväksymisaika | Yes |
| luonnin-hyvaksyjan-oid | string | Luodun HOKS-dokumentin hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Yes |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#puuttuvaosaaminen) |  | Yes |
| opiskeluoikeus-oid | string | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Yes |
| versio | long | HOKS-dokumentin versio | Yes |
| paivityksen-hyvaksyjan-oid | string | HOKS-dokumentin viimeisen päivityksen hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Yes |
| paivitetty | dateTime | HOKS-dokumentin viimeisin päivitysaika | Yes |
| eid | long | eHOKS-id eli tunniste eHOKS-järjestelmässä | Yes |
| luojan-oid | string | HOKS-dokumentin luoneen henkilön yksilöivä tunniste Koski-järjestelmässä | Yes |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#olemassaolevaosaaminen) |  | Yes |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#opiskeluvalmiuksiatukevatopinnot) |  | Yes |
| urasuunnitelma | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### HOKSArvot  

Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paivittajan-oid | string | HOKS-dokumenttia viimeksi päivittäneen henkilön yksilöivä tunniste Koski-järjestelmässä | Yes |
| luotu | dateTime | HOKS-dokumentin luontiaika | Yes |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) |  | Yes |
| hyvaksytty | dateTime | HOKS-dokumentin hyväksymisaika | Yes |
| luonnin-hyvaksyjan-oid | string | Luodun HOKS-dokumentin hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Yes |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#puuttuvaosaaminen) |  | Yes |
| opiskeluoikeus-oid | string | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Yes |
| versio | long | HOKS-dokumentin versio | Yes |
| paivityksen-hyvaksyjan-oid | string | HOKS-dokumentin viimeisen päivityksen hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Yes |
| paivitetty | dateTime | HOKS-dokumentin viimeisin päivitysaika | Yes |
| eid | long | eHOKS-id eli tunniste eHOKS-järjestelmässä | Yes |
| luojan-oid | string | HOKS-dokumentin luoneen henkilön yksilöivä tunniste Koski-järjestelmässä | Yes |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#olemassaolevaosaaminen) |  | Yes |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#opiskeluvalmiuksiatukevatopinnot) |  | Yes |
| urasuunnitelma | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### HankitunOsaamisenNaytto  

Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvioijat | [ [Arvioija](#arvioija) ] | Näytön tai osaamisen osoittamisen arvioijat | Yes |
| kuvaus | string | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, simulaattori | Yes |
| osaamistavoitteet | [ [KoodistoKoodi](#koodistokoodi) ] | Osaamistavoitteet, jonka arvioinnin kriteereitä mukautetaan | Yes |
| ammattitaitovaatimukset | [ [KoodistoKoodi](#koodistokoodi) ] | Ammattitaitovaatimukset, jonka arvioinnin kriteereitä mukautetaan | Yes |
| nayttoymparisto | [Organisaatio](#organisaatio) |  | Yes |
| sisalto | string | Näytön tai osaamisen osoittamisen sisältö tai työtehtävät | Yes |
| arviointikriteerit | [ [Arviointikriteeri](#arviointikriteeri) ] | Yksilölliset arvioinnin kriteerit | Yes |
| jarjestaja | [NaytonJarjestaja](#naytonjarjestaja) |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### HealthcheckStatus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| HealthcheckStatus | object |  |  |

### Henkilo  

Vastuullinen työpaikkaohjaaja

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| organisaatio | [Organisaatio](#organisaatio) |  | Yes |
| oid | string | Oppijanumero 'oid' on oppijan yksilöivä tunniste Opintopolku-palvelussa ja Koskessa. | Yes |
| nimi | string | Henkilön nimi | Yes |
| rooli | string | Henkilön rooli | Yes |

### KoodiMetadata  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| kuvaus | string |  | Yes |
| kasite | string |  | Yes |
| lyhytNimi | string |  | Yes |
| eiSisallaMerkitysta | string |  | Yes |
| kieli | string |  | Yes |
| nimi | string |  | Yes |
| sisaltaaMerkityksen | string |  | Yes |
| huomioitavaKoodi | string |  | Yes |
| kayttoohje | string |  | Yes |
| sisaltaaKoodiston | string |  | Yes |

### KoodistoItem  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koodistoUri | string |  | Yes |
| organisaatioOid | string |  | Yes |
| koodistoVersios | [ long ] |  | Yes |

### KoodistoKoodi  

Arvioijan roolin Koodisto-koodi

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koodisto-koodi | string | Koodiston koodi | Yes |
| koodisto-uri | string | Koodiston URI | Yes |
| versio | long | Koodisto-koodin versio | Yes |

### KoskiArviointi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| hyväksytty | boolean |  | Yes |
| päivä | string |  | No |
| kuvaus | [TranslatedValue](#translatedvalue) |  | No |
| arvioitsijat | [ [Response4030681DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat](#response4030681dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarvioitsijat) ] |  | No |
| arviointikohteet | [ [Response4030681DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet](#response4030681dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarviointikohteet) ] |  | No |
| arvioinnistaPäättäneet | [ [KoodistoKoodi](#koodistokoodi) ] |  | No |
| arviointikeskusteluunOsallistuneet | [ [KoodistoKoodi](#koodistokoodi) ] |  | No |

### KoskiHenkilo  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| oid | string |  | Yes |
| hetu | string |  | Yes |
| syntymäaika | string |  | Yes |
| etunimet | string |  | Yes |
| kutsumanimi | string |  | Yes |
| sukunimi | string |  | Yes |

### KoskiKoulutusmoduuli  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| kieli | [KoodistoKoodi](#koodistokoodi) |  | No |
| pakollinen | boolean |  | No |
| perusteenDiaarinumero | string |  | No |
| laajuus | [KoskiLaajuus](#koskilaajuus) |  | No |
| kuvaus | [TranslatedValue](#translatedvalue) |  | No |
| koulutustyyppi | [KoodistoKoodi](#koodistokoodi) |  | No |
| perusteenNimi | [TranslatedValue](#translatedvalue) |  | No |

### KoskiKoulutustoimija  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| oid | string |  | Yes |
| nimi | [TranslatedValue](#translatedvalue) |  | Yes |
| yTunnus | string |  | Yes |
| kotipaikka | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### KoskiLaajuus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvo | double |  | Yes |
| yksikkö | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### KoskiMyontajaHenkilo  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| titteli | [TranslatedValue](#translatedvalue) |  | Yes |
| organisaatio | [KoskiOrganisaatio](#koskiorganisaatio) |  | Yes |

### KoskiNaytto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| kuvaus | [TranslatedValue](#translatedvalue) |  | Yes |
| suorituspaikka | [KoskiTunnisteKuvaus](#koskitunnistekuvaus) |  | Yes |
| suoritusaika | [Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika](#response4030681dataopiskeluoikeudetsuorituksetosasuorituksetnäyttösuoritusaika) |  | Yes |
| työssäoppimisenYhteydessä | boolean |  | Yes |
| arviointi | [KoskiArviointi](#koskiarviointi) |  | Yes |

### KoskiOpiskeluoikeus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tila | [KoskiOpiskeluoikeusTila](#koskiopiskeluoikeustila) |  | Yes |
| aikaleima | string |  | Yes |
| päättymispäivä | string |  | Yes |
| oppilaitos | [KoskiOppilaitos](#koskioppilaitos) |  | Yes |
| oid | string |  | Yes |
| alkamispäivä | string |  | Yes |
| arvioituPäättymispäivä | string |  | No |
| koulutustoimija | [KoskiKoulutustoimija](#koskikoulutustoimija) |  | Yes |
| versionumero | long |  | Yes |
| suoritukset | [ [KoskiSuoritus](#koskisuoritus) ] |  | Yes |
| lähdejärjestelmänId | [Response4030681DataOpiskeluoikeudetLähdejärjestelmänId](#response4030681dataopiskeluoikeudetlähdejärjestelmänid) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### KoskiOpiskeluoikeusTila  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opiskeluoikeusjaksot | [ [KoskiOpiskeluoikeusjakso](#koskiopiskeluoikeusjakso) ] |  | Yes |

### KoskiOpiskeluoikeusjakso  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| tila | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| opintojenRahoitus | [KoodistoKoodi](#koodistokoodi) |  | No |

### KoskiOppija  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| henkilö | [KoskiHenkilo](#koskihenkilo) |  | Yes |
| opiskeluoikeudet | [ [KoskiOpiskeluoikeus](#koskiopiskeluoikeus) ] |  | Yes |

### KoskiOppilaitos  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| oid | string |  | Yes |
| oppilaitosnumero | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| nimi | [TranslatedValue](#translatedvalue) |  | Yes |
| kotipaikka | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### KoskiOrganisaatio  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| oid | string |  | Yes |
| oppilaitosnumero | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| nimi | [TranslatedValue](#translatedvalue) |  | No |
| kotipaikka | [KoodistoKoodi](#koodistokoodi) |  | No |

### KoskiOsasuoritus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| painotettuOpetus | boolean |  | No |
| yksilöllistettyOppimäärä | boolean |  | No |
| arviointi | [ [KoskiArviointi](#koskiarviointi) ] |  | Yes |
| tutkinnonOsanRyhmä | [KoodistoKoodi](#koodistokoodi) |  | No |
| lisätiedot | [ [KoskiTunnisteKuvaus](#koskitunnistekuvaus) ] |  | No |
| tunnustettu | [KoskiTunnustettu](#koskitunnustettu) |  | No |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| alkamispäivä | string |  | No |
| osasuoritukset | [ [KoskiOsasuoritusOsasuoritus](#koskiosasuoritusosasuoritus) ] |  | No |
| toimipiste | [KoskiOppilaitos](#koskioppilaitos) |  | No |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| näyttö | [KoskiNaytto](#koskinaytto) |  | No |

### KoskiOsasuoritusOsasuoritus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| painotettuOpetus | boolean |  | No |
| yksilöllistettyOppimäärä | boolean |  | No |
| arviointi | [ [KoskiArviointi](#koskiarviointi) ] |  | Yes |
| tutkinnonOsanRyhmä | [KoodistoKoodi](#koodistokoodi) |  | No |
| lisätiedot | [ [KoskiTunnisteKuvaus](#koskitunnistekuvaus) ] |  | No |
| tunnustettu | [KoskiTunnustettu](#koskitunnustettu) |  | No |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| alkamispäivä | string |  | No |
| toimipiste | [KoskiOppilaitos](#koskioppilaitos) |  | No |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| näyttö | [KoskiNaytto](#koskinaytto) |  | No |

### KoskiSuoritus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| muutSuorituskielet | [ [KoodistoKoodi](#koodistokoodi) ] |  | No |
| vahvistus | [Response4030681DataOpiskeluoikeudetSuorituksetVahvistus](#response4030681dataopiskeluoikeudetsuorituksetvahvistus) |  | Yes |
| suorituskieli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| osaamisala | [ [Response4030681DataOpiskeluoikeudetSuorituksetOsaamisala](#response4030681dataopiskeluoikeudetsuorituksetosaamisala) ] |  | No |
| kielikylpykieli | [KoodistoKoodi](#koodistokoodi) |  | No |
| luokka | string |  | No |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| alkamispäivä | string |  | No |
| osasuoritukset | [ [KoskiOsasuoritus](#koskiosasuoritus) ] |  | No |
| ryhmä | string |  | No |
| toimipiste | [KoskiOrganisaatio](#koskiorganisaatio) |  | Yes |
| käyttäytymisenArvio | [KoskiArviointi](#koskiarviointi) |  | No |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| suoritustapa | [KoodistoKoodi](#koodistokoodi) |  | No |
| tutkintonimike | [ [KoodistoKoodi](#koodistokoodi) ] |  | No |
| järjestämismuodot | [ [Response4030681DataOpiskeluoikeudetSuorituksetJärjestämismuodot](#response4030681dataopiskeluoikeudetsuorituksetjärjestämismuodot) ] |  | No |
| työssäoppimisjaksot | [ [KoskiTyossaoppimisjakso](#koskityossaoppimisjakso) ] |  | No |
| jääLuokalle | boolean |  | No |

### KoskiTunnisteKuvaus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| kuvaus | [TranslatedValue](#translatedvalue) |  | Yes |

### KoskiTunnustettu  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| osaaminen | [Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen](#response4030681dataopiskeluoikeudetsuorituksetosasuorituksettunnustettuosaaminen) |  | Yes |
| selite | [TranslatedValue](#translatedvalue) |  | Yes |
| rahoituksenPiirissä | boolean |  | Yes |

### KoskiTyossaoppimisjakso  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |
| työssäoppimispaikka | [TranslatedValue](#translatedvalue) |  | Yes |
| paikkakunta | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| maa | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| työtehtävät | [TranslatedValue](#translatedvalue) |  | Yes |
| laajuus | [KoskiLaajuus](#koskilaajuus) |  | Yes |

### MuuTutkinnonOsa  

Muu tutkinnon osa

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string | Tutkinnon osan nimi | Yes |
| kuvaus | string | Tutkinnon osan kuvaus | Yes |
| laajuus | long | Tutkinnon osan laajuus osaamispisteiss' | Yes |
| kesto | long | Tutkinnon osan kesto päivinä | Yes |
| suorituspvm | date | Tutkinnon suorituspäivä muodossa YYYY-MM-DD | Yes |

### NaytonJarjestaja  

Näytön tai osaamisen osoittamisen järjestäjä

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string | Näytön tai osaamisen osoittamisen järjestäjän nimi | Yes |
| oid | string | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Yes |

### Nimi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fi | string |  | No |
| sv | string |  | No |
| en | string |  | No |

### OlemassaOlevaOsaaminen  

Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan tutkintoa

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunnustettu-osaaminen | [Opinnot](#opinnot) |  | Yes |
| aiempi-tunnustettava-osaaminen | [Opinnot](#opinnot) |  | Yes |
| tunnustettavana-olevat | [Opinnot](#opinnot) |  | Yes |
| muut-opinnot | [Opinnot](#opinnot) |  | Yes |

### Opinnot  

Tunnustettu osaaminen

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] | Osaamisen ammattilliset opinnot | Yes |
| yhteiset-tutkinnon-osat | [ [YhteinenTutkinnonOsa](#yhteinentutkinnonosa) ] | Osaamisen yhteiset tutkinnon osat (YTO) | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] | Muut osaamisen opinnot | Yes |

### OpiskeluvalmiuksiaTukevatOpinnot  

Opiskeluvalmiuksia tukevat opinnot

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string | Tutkinnon osan nimi | Yes |
| kuvaus | string | Tutkinnon osan kuvaus | Yes |
| laajuus | long | Tutkinnon osan laajuus osaamispisteiss' | Yes |
| kesto | long | Tutkinnon osan kesto päivinä | Yes |
| suorituspvm | date | Tutkinnon suorituspäivä muodossa YYYY-MM-DD | Yes |

### Oppimisymparisto  

Oppimisympäristö

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paikka | string | Oppimisympäristön paikan nimi | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### Organisaatio  

Arvioijan organisaatio

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string | Organisaation nimi | Yes |
| y-tunnus | string | Organisaation y-tunnus | Yes |

### OsaamisenHankkimistapa  

Osaamisen hankkimisen tapa

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ajankohta | [DateRange](#daterange) |  | Yes |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### POSTResponse  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| uri | string |  | Yes |

### Peruste  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| nimi | [Nimi](#nimi) |  | No |
| osaamisalat | [ [Response4030680DataOsaamisalat](#response4030680dataosaamisalat) ] |  | No |
| tutkintonimikkeet | [ [Response4030680DataTutkintonimikkeet](#response4030680datatutkintonimikkeet) ] |  | No |

### PuuttuvaOsaaminen  

Puuttuvan osaamisen hankkimisen suunnitelma

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| poikkeama | [PuuttuvanSaamisenPoikkeama](#puuttuvansaamisenpoikkeama) |  | Yes |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] | Ammatilliset opinnot | Yes |
| tarvittava-opetus | string | Tarvittava opetus | Yes |
| osaamisen-hankkimistavat | [ [OsaamisenHankkimistapa](#osaamisenhankkimistapa) ] | Osaamisen hankkimistavat | Yes |
| yhteiset-tutkinnon-osat | [ [YhteinenTutkinnonOsa](#yhteinentutkinnonosa) ] | Yhteiset tutkinnon osat | Yes |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#tyopaikallahankittavaosaaminen) |  | Yes |
| koulutuksen-jarjestaja-oid | string | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid. | Yes |
| muut | [ [MuuTutkinnonOsa](#muututkinnonosa) ] | Muut tutkinnon osaa pienemmät osaamiskokonaisuudet | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### PuuttuvanSaamisenPoikkeama  

Puutuvan osaamisen poikkeama

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alkuperainen-tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| kuvaus | string | Poikkeaman kuvaus | Yes |

### Response4030673  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030673Meta](#response4030673meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response4030673DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response4030673DataContactValuesGroupContact](#response4030673datacontactvaluesgroupcontact) ] |  | Yes |

### Response4030673DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response4030673Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030673Meta | object |  |  |

### Response4030674  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030674Meta](#response4030674meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response4030674Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030674Meta | object |  |  |

### Response4030675  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030675Meta](#response4030675meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response4030675Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response4030676  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030676Meta](#response4030676meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response4030676Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030676Meta | object |  |  |

### Response4030677  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030677Meta](#response4030677meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response4030677Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030677Meta | object |  |  |

### Response4030678  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030678Meta](#response4030678meta) |  | Yes |
| data | [ [Response4030678Data](#response4030678data) ] |  | Yes |

### Response4030678Data  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| category | string |  | Yes |
| createdBy | string |  | Yes |
| key | string |  | Yes |
| force | boolean |  | Yes |
| locale | string |  | Yes |
| value | string |  | Yes |
| created | double |  | Yes |
| modified |  |  | Yes |
| accessed |  |  | Yes |
| accesscount | long |  | Yes |
| id | long |  | Yes |
| modifiedBy | string |  | Yes |

### Response4030678Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030678Meta | object |  |  |

### Response4030679  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030679Meta](#response4030679meta) |  | Yes |
| data | [ExtendedKoodistoKoodi](#extendedkoodistokoodi) |  | Yes |

### Response4030679Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030679Meta | object |  |  |

### Response4030680  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030680Meta](#response4030680meta) |  | Yes |
| data | [ [Peruste](#peruste) ] |  | Yes |

### Response4030680DataOsaamisalat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response4030680DataTutkintonimikkeet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response4030680Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030680Meta | object |  |  |

### Response4030681  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030681Meta](#response4030681meta) |  | Yes |
| data | [KoskiOppija](#koskioppija) |  | Yes |

### Response4030681DataOpiskeluoikeudetLähdejärjestelmänId  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string |  | Yes |
| lähdejärjestelmä | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetJärjestämismuodot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| järjestämismuoto | [Response4030681DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto](#response4030681dataopiskeluoikeudetsuorituksetjärjestämismuodotjärjestämismuoto) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030681DataOpiskeluoikeudetSuorituksetOsaamisala  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| osaamisala | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030681DataOpiskeluoikeudetSuorituksetVahvistus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| päivä | string |  | Yes |
| paikkakunta | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| myöntäjäOrganisaatio | [KoskiOrganisaatio](#koskiorganisaatio) |  | Yes |
| myöntäjäHenkilöt | [ [KoskiMyontajaHenkilo](#koskimyontajahenkilo) ] |  | Yes |

### Response4030681Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030681Meta | object |  |  |

### Response4030682  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030682Meta](#response4030682meta) |  | Yes |
| data | [Environment](#environment) |  | Yes |

### Response4030682Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030682Meta | object |  |  |

### TranslatedValue  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fi | string |  | No |
| sv | string |  | No |
| en | string |  | No |

### TutkinnonOsa  

Tutkinnon osa, johon poikkeus pohjautuu

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| laajuus | long | Tutkinnon laajuus | Yes |
| eperusteet-diaarinumero | string | Diaarinumero ePerusteet-palvelussa | Yes |
| kuvaus | string | Tutkinnon osan kuvaus | Yes |
| koulutustyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### TyopaikallaHankittavaOsaaminen  

Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| vastuullinen-ohjaaja | [Henkilo](#henkilo) |  | Yes |
| keskeiset-tyotehtavat | [ string ] | Keskeiset työtehtävät | Yes |
| hankkijan-edustaja | [Henkilo](#henkilo) |  | Yes |
| erityinen-tuki | boolean | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen tuen päätös | Yes |
| jarjestajan-edustaja | [Henkilo](#henkilo) |  | Yes |
| erityisen-tuen-aika | [DateRange](#daterange) |  | Yes |
| muut-osallistujat | [ [Henkilo](#henkilo) ] | Muut ohjaukseen osallistuvat henkilöt | Yes |
| muut-oppimisymparistot | [ [Oppimisymparisto](#oppimisymparisto) ] | Muissa oppimisympäristöissä tapahtuvat osaamisen hankkimiset | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |
| ohjaus-ja-tuki | boolean | Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta | Yes |

### User  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| oid | string |  | No |
| first-name | string |  | Yes |
| common-name | string |  | Yes |
| surname | string |  | Yes |

### UserInfo  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| oid | string |  | No |
| first-name | string |  | Yes |
| common-name | string |  | Yes |
| surname | string |  | Yes |
| contact-values-group | [ [Response4030673DataContactValuesGroup](#response4030673datacontactvaluesgroup) ] |  | No |

### YhteinenTutkinnonOsa  

YTO tutkinnon osa

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| osa-alue-tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| laajuus | long | Tutkinnon laajuus | Yes |
| eperusteet-diaarinumero | string | Diaarinumero ePerusteet-palvelussa | Yes |
| kuvaus | string | Tutkinnon osan kuvaus | Yes |
| koulutustyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |