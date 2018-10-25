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
| 200 |  | [Response4030683](#response4030683) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030684](#response4030684) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030685](#response4030685) |

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
| 200 |  | [Response4030686](#response4030686) |

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
| 200 |  | [Response4030687](#response4030687) |

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
| 200 |  | [Response4030688](#response4030688) |

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
| 200 |  | [Response4030689](#response4030689) |

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
| 200 |  | [Response4030690](#response4030690) |

### /ehoks-backend/api/v1/external/koski/oppija
---
##### ***GET***
**Summary:** Hakee oppijan tietoja Koski-palvelusta

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030691](#response4030691) |

### /ehoks-backend/api/v1/misc/environment
---
##### ***GET***
**Summary:** Palauttaa ympäristön tiedot ja asetukset

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response4030692](#response4030692) |

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
| arvioitsijat | [ [Response4030691DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat](#response4030691dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarvioitsijat) ] |  | No |
| arviointikohteet | [ [Response4030691DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet](#response4030691dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarviointikohteet) ] |  | No |
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
| suoritusaika | [Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika](#response4030691dataopiskeluoikeudetsuorituksetosasuorituksetnäyttösuoritusaika) |  | Yes |
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
| lähdejärjestelmänId | [Response4030691DataOpiskeluoikeudetLähdejärjestelmänId](#response4030691dataopiskeluoikeudetlähdejärjestelmänid) |  | Yes |
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
| vahvistus | [Response4030691DataOpiskeluoikeudetSuorituksetVahvistus](#response4030691dataopiskeluoikeudetsuorituksetvahvistus) |  | Yes |
| suorituskieli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| osaamisala | [ [Response4030691DataOpiskeluoikeudetSuorituksetOsaamisala](#response4030691dataopiskeluoikeudetsuorituksetosaamisala) ] |  | No |
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
| järjestämismuodot | [ [Response4030691DataOpiskeluoikeudetSuorituksetJärjestämismuodot](#response4030691dataopiskeluoikeudetsuorituksetjärjestämismuodot) ] |  | No |
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
| osaaminen | [Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen](#response4030691dataopiskeluoikeudetsuorituksetosasuorituksettunnustettuosaaminen) |  | Yes |
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
| osaamisalat | [ [Response4030690DataOsaamisalat](#response4030690dataosaamisalat) ] |  | No |
| tutkintonimikkeet | [ [Response4030690DataTutkintonimikkeet](#response4030690datatutkintonimikkeet) ] |  | No |

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

### Response4030683  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030683Meta](#response4030683meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response4030683DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response4030683DataContactValuesGroupContact](#response4030683datacontactvaluesgroupcontact) ] |  | Yes |

### Response4030683DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response4030683Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030683Meta | object |  |  |

### Response4030684  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030684Meta](#response4030684meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response4030684Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030684Meta | object |  |  |

### Response4030685  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030685Meta](#response4030685meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response4030685Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response4030686  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030686Meta](#response4030686meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response4030686Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030686Meta | object |  |  |

### Response4030687  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030687Meta](#response4030687meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response4030687Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030687Meta | object |  |  |

### Response4030688  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030688Meta](#response4030688meta) |  | Yes |
| data | [ [Response4030688Data](#response4030688data) ] |  | Yes |

### Response4030688Data  

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

### Response4030688Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030688Meta | object |  |  |

### Response4030689  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030689Meta](#response4030689meta) |  | Yes |
| data | [ExtendedKoodistoKoodi](#extendedkoodistokoodi) |  | Yes |

### Response4030689Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030689Meta | object |  |  |

### Response4030690  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030690Meta](#response4030690meta) |  | Yes |
| data | [ [Peruste](#peruste) ] |  | Yes |

### Response4030690DataOsaamisalat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response4030690DataTutkintonimikkeet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response4030690Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030690Meta | object |  |  |

### Response4030691  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030691Meta](#response4030691meta) |  | Yes |
| data | [KoskiOppija](#koskioppija) |  | Yes |

### Response4030691DataOpiskeluoikeudetLähdejärjestelmänId  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string |  | Yes |
| lähdejärjestelmä | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetJärjestämismuodot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| järjestämismuoto | [Response4030691DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto](#response4030691dataopiskeluoikeudetsuorituksetjärjestämismuodotjärjestämismuoto) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030691DataOpiskeluoikeudetSuorituksetOsaamisala  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| osaamisala | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response4030691DataOpiskeluoikeudetSuorituksetVahvistus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| päivä | string |  | Yes |
| paikkakunta | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| myöntäjäOrganisaatio | [KoskiOrganisaatio](#koskiorganisaatio) |  | Yes |
| myöntäjäHenkilöt | [ [KoskiMyontajaHenkilo](#koskimyontajahenkilo) ] |  | Yes |

### Response4030691Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030691Meta | object |  |  |

### Response4030692  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response4030692Meta](#response4030692meta) |  | Yes |
| data | [Environment](#environment) |  | Yes |

### Response4030692Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response4030692Meta | object |  |  |

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
| contact-values-group | [ [Response4030683DataContactValuesGroup](#response4030683datacontactvaluesgroup) ] |  | No |

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