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
| 200 |  | [Response2880995](#response2880995) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response2880996](#response2880996) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response2880997](#response2880997) |

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
| 200 |  | [Response2880998](#response2880998) |

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
| 200 |  | [Response2880999](#response2880999) |

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
| 200 |  | [Response2881000](#response2881000) |

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
| 200 |  | [Response2881001](#response2881001) |

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
| 200 |  | [Response2881002](#response2881002) |

### /ehoks-backend/api/v1/external/koski/oppija
---
##### ***GET***
**Summary:** Hakee oppijan tietoja Koski-palvelusta

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response2881003](#response2881003) |

### /ehoks-backend/api/v1/misc/environment
---
##### ***GET***
**Summary:** Palauttaa ympäristön tiedot ja asetukset

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response2881004](#response2881004) |

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
| paivittajan-oid | string | HOKS-dokumenttia viimeksi päivittäneen henkilön yksilöivä tunniste
     Koski-järjestelmässä | Yes |
| luotu | dateTime | HOKS-dokumentin luontiaika | Yes |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) |  | Yes |
| hyvaksytty | dateTime | HOKS-dokumentin hyväksymisaika | Yes |
| luonnin-hyvaksyjan-oid | string | Luodun HOKS-dokumentin hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Yes |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#puuttuvaosaaminen) |  | Yes |
| opiskeluoikeus-oid | string | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Yes |
| versio | long | HOKS-dokumentin versio | Yes |
| paivityksen-hyvaksyjan-oid | string | HOKS-dokumentin viimeisen päivityksen hyväksyjän yksilöivä tunniste
     Koski-järjestelmässä | Yes |
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
| paivittajan-oid | string | HOKS-dokumenttia viimeksi päivittäneen henkilön yksilöivä tunniste
     Koski-järjestelmässä | Yes |
| luotu | dateTime | HOKS-dokumentin luontiaika | Yes |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) |  | Yes |
| hyvaksytty | dateTime | HOKS-dokumentin hyväksymisaika | Yes |
| luonnin-hyvaksyjan-oid | string | Luodun HOKS-dokumentin hyväksyjän yksilöivä tunniste Koski-järjestelmässä | Yes |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#puuttuvaosaaminen) |  | Yes |
| opiskeluoikeus-oid | string | Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä. | Yes |
| versio | long | HOKS-dokumentin versio | Yes |
| paivityksen-hyvaksyjan-oid | string | HOKS-dokumentin viimeisen päivityksen hyväksyjän yksilöivä tunniste
     Koski-järjestelmässä | Yes |
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
| kuvaus | string | Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen näyttöympäristö
     on kyseessä. Kuvataan ympäristön luonne lyhyesti, esim. kukkakauppa,
     varaosaliike, ammatillinen oppilaitos, simulaattori | Yes |
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
| oid | string | Oppijanumero 'oid' on oppijan yksilöivä tunniste
                Opintopolku-palvelussa ja Koskessa. | Yes |
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
| arvioitsijat | [ [Response2881003DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat](#response2881003dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarvioitsijat) ] |  | No |
| arviointikohteet | [ [Response2881003DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet](#response2881003dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarviointikohteet) ] |  | No |
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
| suoritusaika | [Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika](#response2881003dataopiskeluoikeudetsuorituksetosasuorituksetnäyttösuoritusaika) |  | Yes |
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
| lähdejärjestelmänId | [Response2881003DataOpiskeluoikeudetLähdejärjestelmänId](#response2881003dataopiskeluoikeudetlähdejärjestelmänid) |  | Yes |
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
| vahvistus | [Response2881003DataOpiskeluoikeudetSuorituksetVahvistus](#response2881003dataopiskeluoikeudetsuorituksetvahvistus) |  | Yes |
| suorituskieli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| osaamisala | [ [Response2881003DataOpiskeluoikeudetSuorituksetOsaamisala](#response2881003dataopiskeluoikeudetsuorituksetosaamisala) ] |  | No |
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
| järjestämismuodot | [ [Response2881003DataOpiskeluoikeudetSuorituksetJärjestämismuodot](#response2881003dataopiskeluoikeudetsuorituksetjärjestämismuodot) ] |  | No |
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
| osaaminen | [Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen](#response2881003dataopiskeluoikeudetsuorituksetosasuorituksettunnustettuosaaminen) |  | Yes |
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
| oid | string | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla
     organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän
     oid. | Yes |

### Nimi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fi | string |  | No |
| sv | string |  | No |
| en | string |  | No |

### OlemassaOlevaOsaaminen  

Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi opiskelijan
     tutkintoa

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
| osaamisalat | [ [Response2881002DataOsaamisalat](#response2881002dataosaamisalat) ] |  | No |
| tutkintonimikkeet | [ [Response2881002DataTutkintonimikkeet](#response2881002datatutkintonimikkeet) ] |  | No |

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
| koulutuksen-jarjestaja-oid | string | Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla
     organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän
     oid. | Yes |
| muut | [ [MuuTutkinnonOsa](#muututkinnonosa) ] | Muut tutkinnon osaa pienemmät osaamiskokonaisuudet | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### PuuttuvanSaamisenPoikkeama  

Puutuvan osaamisen poikkeama

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alkuperainen-tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| kuvaus | string | Poikkeaman kuvaus | Yes |

### Response2880995  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2880995Meta](#response2880995meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response2880995DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response2880995DataContactValuesGroupContact](#response2880995datacontactvaluesgroupcontact) ] |  | Yes |

### Response2880995DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response2880995Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2880995Meta | object |  |  |

### Response2880996  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2880996Meta](#response2880996meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response2880996Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2880996Meta | object |  |  |

### Response2880997  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2880997Meta](#response2880997meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response2880997Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response2880998  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2880998Meta](#response2880998meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response2880998Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2880998Meta | object |  |  |

### Response2880999  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2880999Meta](#response2880999meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response2880999Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2880999Meta | object |  |  |

### Response2881000  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2881000Meta](#response2881000meta) |  | Yes |
| data | [ [Response2881000Data](#response2881000data) ] |  | Yes |

### Response2881000Data  

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

### Response2881000Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2881000Meta | object |  |  |

### Response2881001  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2881001Meta](#response2881001meta) |  | Yes |
| data | [ExtendedKoodistoKoodi](#extendedkoodistokoodi) |  | Yes |

### Response2881001Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2881001Meta | object |  |  |

### Response2881002  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2881002Meta](#response2881002meta) |  | Yes |
| data | [ [Peruste](#peruste) ] |  | Yes |

### Response2881002DataOsaamisalat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response2881002DataTutkintonimikkeet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response2881002Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2881002Meta | object |  |  |

### Response2881003  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2881003Meta](#response2881003meta) |  | Yes |
| data | [KoskiOppija](#koskioppija) |  | Yes |

### Response2881003DataOpiskeluoikeudetLähdejärjestelmänId  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string |  | Yes |
| lähdejärjestelmä | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetJärjestämismuodot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| järjestämismuoto | [Response2881003DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto](#response2881003dataopiskeluoikeudetsuorituksetjärjestämismuodotjärjestämismuoto) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response2881003DataOpiskeluoikeudetSuorituksetOsaamisala  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| osaamisala | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response2881003DataOpiskeluoikeudetSuorituksetVahvistus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| päivä | string |  | Yes |
| paikkakunta | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| myöntäjäOrganisaatio | [KoskiOrganisaatio](#koskiorganisaatio) |  | Yes |
| myöntäjäHenkilöt | [ [KoskiMyontajaHenkilo](#koskimyontajahenkilo) ] |  | Yes |

### Response2881003Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2881003Meta | object |  |  |

### Response2881004  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response2881004Meta](#response2881004meta) |  | Yes |
| data | [Environment](#environment) |  | Yes |

### Response2881004Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response2881004Meta | object |  |  |

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
| erityinen-tuki | boolean | Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä erityisen
     tuen päätös | Yes |
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
| contact-values-group | [ [Response2880995DataContactValuesGroup](#response2880995datacontactvaluesgroup) ] |  | No |

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