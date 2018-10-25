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
| 200 |  | [Response37264](#response37264) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37265](#response37265) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37266](#response37266) |

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
| 200 |  | [Response37267](#response37267) |

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
| HOKSArvot | body |  | Yes | [HOKSArvot](#hoksarvot) |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37268](#response37268) |

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
| 200 |  | [Response37269](#response37269) |

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
| 200 |  | [Response37270](#response37270) |

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
| 200 |  | [Response37271](#response37271) |

### /ehoks-backend/api/v1/external/koski/oppija
---
##### ***GET***
**Summary:** Hakee oppijan tietoja Koski-palvelusta

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37272](#response37272) |

### /ehoks-backend/api/v1/misc/environment
---
##### ***GET***
**Summary:** Palauttaa ympäristön tiedot ja asetukset

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37273](#response37273) |

### Models
---

### DateRange  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date |  | Yes |
| loppu | date |  | Yes |

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

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paivittajan-oid | string |  | Yes |
| luotu | dateTime |  | Yes |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) |  | Yes |
| hyvaksytty | dateTime |  | Yes |
| luonnin-hyvaksyjan-oid | string |  | Yes |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#puuttuvaosaaminen) |  | Yes |
| opiskeluoikeus-oid | string |  | Yes |
| id | long |  | Yes |
| versio | long |  | Yes |
| paivityksen-hyvaksyjan-oid | string |  | Yes |
| paivitetty | dateTime |  | Yes |
| luojan-oid | string |  | Yes |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#olemassaolevaosaaminen) |  | Yes |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#opiskeluvalmiuksiatukevatopinnot) |  | Yes |
| urasuunnitelma | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### HOKSArvot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paivittajan-oid | string |  | Yes |
| luotu | dateTime |  | Yes |
| hankitun-osaamisen-naytto | [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) |  | Yes |
| hyvaksytty | dateTime |  | Yes |
| luonnin-hyvaksyjan-oid | string |  | Yes |
| puuttuva-osaaminen | [PuuttuvaOsaaminen](#puuttuvaosaaminen) |  | Yes |
| opiskeluoikeus-oid | string |  | Yes |
| versio | long |  | Yes |
| paivityksen-hyvaksyjan-oid | string |  | Yes |
| paivitetty | dateTime |  | Yes |
| luojan-oid | string |  | Yes |
| olemassa-oleva-osaaminen | [OlemassaOlevaOsaaminen](#olemassaolevaosaaminen) |  | Yes |
| opiskeluvalmiuksia-tukevat-opinnot | [OpiskeluvalmiuksiaTukevatOpinnot](#opiskeluvalmiuksiatukevatopinnot) |  | Yes |
| urasuunnitelma | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### HankitunOsaamisenNaytto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvioijat | [ [HoksArvotHankitunOsaamisenNayttoArvioijat](#hoksarvothankitunosaamisennayttoarvioijat) ] |  | Yes |
| kuvaus | string |  | Yes |
| osaamistavoitteet | [ [KoodistoKoodi](#koodistokoodi) ] |  | Yes |
| ammattitaitovaatimukset | [ [KoodistoKoodi](#koodistokoodi) ] |  | Yes |
| nayttoymparisto | [Organisaatio](#organisaatio) |  | Yes |
| sisalto | string |  | Yes |
| arviointikriteerit | [ [HoksArvotHankitunOsaamisenNayttoArviointikriteerit](#hoksarvothankitunosaamisennayttoarviointikriteerit) ] |  | Yes |
| jarjestaja | [HoksArvotHankitunOsaamisenNayttoJarjestaja](#hoksarvothankitunosaamisennayttojarjestaja) |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### HealthcheckStatus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| HealthcheckStatus | object |  |  |

### Henkilo  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| organisaatio | [Organisaatio](#organisaatio) |  | Yes |
| oid | string |  | Yes |
| nimi | string |  | Yes |
| rooli | string |  | Yes |

### HoksArvotHankitunOsaamisenNayttoArvioijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| rooli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| organisaatio | [Organisaatio](#organisaatio) |  | Yes |

### HoksArvotHankitunOsaamisenNayttoArviointikriteerit  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvosana | long |  | Yes |
| kuvaus | string |  | Yes |

### HoksArvotHankitunOsaamisenNayttoJarjestaja  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| oid | string |  | Yes |

### HoksArvotOlemassaOlevaOsaaminenAiempiTunnustettavaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### HoksArvotOlemassaOlevaOsaaminenMuutOpinnot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### HoksArvotOlemassaOlevaOsaaminenTunnustettavanaOlevat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### HoksArvotOlemassaOlevaOsaaminenTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### HoksArvotPuuttuvaOsaaminenOsaamisenHankkimistavat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ajankohta | [DateRange](#daterange) |  | Yes |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### HoksArvotPuuttuvaOsaaminenPoikkeama  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alkuperainen-tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| kuvaus | string |  | Yes |

### HoksArvotPuuttuvaOsaaminenTyopaikallaHankittavaOsaaminenErityisenTuenAika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date |  | Yes |
| loppu | date |  | Yes |

### HoksArvotPuuttuvaOsaaminenTyopaikallaHankittavaOsaaminenMuutOppimisymparistot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paikka | string |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

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

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koodisto-koodi | string |  | Yes |
| koodisto-uri | string |  | Yes |
| versio | long |  | Yes |

### KoskiArviointi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| hyväksytty | boolean |  | Yes |
| päivä | string |  | No |
| kuvaus | [TranslatedValue](#translatedvalue) |  | No |
| arvioitsijat | [ [Response37272DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat](#response37272dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarvioitsijat) ] |  | No |
| arviointikohteet | [ [Response37272DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet](#response37272dataopiskeluoikeudetsuorituksetkäyttäytymisenarvioarviointikohteet) ] |  | No |
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
| suoritusaika | [Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika](#response37272dataopiskeluoikeudetsuorituksetosasuorituksetnäyttösuoritusaika) |  | Yes |
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
| lähdejärjestelmänId | [Response37272DataOpiskeluoikeudetLähdejärjestelmänId](#response37272dataopiskeluoikeudetlähdejärjestelmänid) |  | Yes |
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
| vahvistus | [Response37272DataOpiskeluoikeudetSuorituksetVahvistus](#response37272dataopiskeluoikeudetsuorituksetvahvistus) |  | Yes |
| suorituskieli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| osaamisala | [ [Response37272DataOpiskeluoikeudetSuorituksetOsaamisala](#response37272dataopiskeluoikeudetsuorituksetosaamisala) ] |  | No |
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
| järjestämismuodot | [ [Response37272DataOpiskeluoikeudetSuorituksetJärjestämismuodot](#response37272dataopiskeluoikeudetsuorituksetjärjestämismuodot) ] |  | No |
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
| osaaminen | [Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen](#response37272dataopiskeluoikeudetsuorituksetosasuorituksettunnustettuosaaminen) |  | Yes |
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

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| kuvaus | string |  | Yes |
| laajuus | long |  | Yes |
| kesto | long |  | Yes |
| suorituspvm | date |  | Yes |

### Nimi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fi | string |  | No |
| sv | string |  | No |
| en | string |  | No |

### OlemassaOlevaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunnustettu-osaaminen | [HoksArvotOlemassaOlevaOsaaminenTunnustettuOsaaminen](#hoksarvotolemassaolevaosaaminentunnustettuosaaminen) |  | Yes |
| aiempi-tunnustettava-osaaminen | [HoksArvotOlemassaOlevaOsaaminenAiempiTunnustettavaOsaaminen](#hoksarvotolemassaolevaosaaminenaiempitunnustettavaosaaminen) |  | Yes |
| tunnustettavana-olevat | [HoksArvotOlemassaOlevaOsaaminenTunnustettavanaOlevat](#hoksarvotolemassaolevaosaaminentunnustettavanaolevat) |  | Yes |
| muut-opinnot | [HoksArvotOlemassaOlevaOsaaminenMuutOpinnot](#hoksarvotolemassaolevaosaaminenmuutopinnot) |  | Yes |

### OpiskeluvalmiuksiaTukevatOpinnot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| kuvaus | string |  | Yes |
| laajuus | long |  | Yes |
| kesto | long |  | Yes |
| suorituspvm | date |  | Yes |

### Organisaatio  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| y-tunnus | string |  | Yes |

### POSTResponse  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| uri | string |  | Yes |

### Peruste  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| nimi | [Nimi](#nimi) |  | No |
| osaamisalat | [ [Response37271DataOsaamisalat](#response37271dataosaamisalat) ] |  | No |
| tutkintonimikkeet | [ [Response37271DataTutkintonimikkeet](#response37271datatutkintonimikkeet) ] |  | No |

### PuuttuvaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| poikkeama | [HoksArvotPuuttuvaOsaaminenPoikkeama](#hoksarvotpuuttuvaosaaminenpoikkeama) |  | Yes |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| tarvittava-opetus | string |  | Yes |
| osaamisen-hankkimistavat | [ [HoksArvotPuuttuvaOsaaminenOsaamisenHankkimistavat](#hoksarvotpuuttuvaosaaminenosaamisenhankkimistavat) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| tyopaikalla-hankittava-osaaminen | [TyopaikallaHankittavaOsaaminen](#tyopaikallahankittavaosaaminen) |  | Yes |
| koulutuksen-jarjestaja-oid | string |  | Yes |
| muut | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### Response37264  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37264Meta](#response37264meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response37264DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response37264DataContactValuesGroupContact](#response37264datacontactvaluesgroupcontact) ] |  | Yes |

### Response37264DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response37264Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37264Meta | object |  |  |

### Response37265  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37265Meta](#response37265meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response37265Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37265Meta | object |  |  |

### Response37266  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37266Meta](#response37266meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response37266Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response37267  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37267Meta](#response37267meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response37267DataHankitunOsaamisenNayttoArvioijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| rooli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| organisaatio | [Organisaatio](#organisaatio) |  | Yes |

### Response37267DataHankitunOsaamisenNayttoArviointikriteerit  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvosana | long |  | Yes |
| kuvaus | string |  | Yes |

### Response37267DataHankitunOsaamisenNayttoJarjestaja  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| oid | string |  | Yes |

### Response37267DataOlemassaOlevaOsaaminenAiempiTunnustettavaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### Response37267DataOlemassaOlevaOsaaminenMuutOpinnot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### Response37267DataOlemassaOlevaOsaaminenTunnustettavanaOlevat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### Response37267DataOlemassaOlevaOsaaminenTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ammatilliset-opinnot | [ [TutkinnonOsa](#tutkinnonosa) ] |  | Yes |
| yhteiset-tutkinnon-osat | [ [YTOTutkinnonOsa](#ytotutkinnonosa) ] |  | Yes |
| muut-osaamiset | [ [MuuTutkinnonOsa](#muututkinnonosa) ] |  | Yes |

### Response37267DataPuuttuvaOsaaminenOsaamisenHankkimistavat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ajankohta | [DateRange](#daterange) |  | Yes |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37267DataPuuttuvaOsaaminenPoikkeama  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alkuperainen-tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| kuvaus | string |  | Yes |

### Response37267DataPuuttuvaOsaaminenTyopaikallaHankittavaOsaaminenErityisenTuenAika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date |  | Yes |
| loppu | date |  | Yes |

### Response37267DataPuuttuvaOsaaminenTyopaikallaHankittavaOsaaminenMuutOppimisymparistot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paikka | string |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### Response37267Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37267Meta | object |  |  |

### Response37268  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37268Meta](#response37268meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37268Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37268Meta | object |  |  |

### Response37269  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37269Meta](#response37269meta) |  | Yes |
| data | [ [Response37269Data](#response37269data) ] |  | Yes |

### Response37269Data  

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

### Response37269Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37269Meta | object |  |  |

### Response37270  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37270Meta](#response37270meta) |  | Yes |
| data | [ExtendedKoodistoKoodi](#extendedkoodistokoodi) |  | Yes |

### Response37270Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37270Meta | object |  |  |

### Response37271  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37271Meta](#response37271meta) |  | Yes |
| data | [ [Peruste](#peruste) ] |  | Yes |

### Response37271DataOsaamisalat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response37271DataTutkintonimikkeet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response37271Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37271Meta | object |  |  |

### Response37272  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37272Meta](#response37272meta) |  | Yes |
| data | [KoskiOppija](#koskioppija) |  | Yes |

### Response37272DataOpiskeluoikeudetLähdejärjestelmänId  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string |  | Yes |
| lähdejärjestelmä | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetJärjestämismuodot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| järjestämismuoto | [Response37272DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto](#response37272dataopiskeluoikeudetsuorituksetjärjestämismuodotjärjestämismuoto) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetJärjestämismuodotJärjestämismuoto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetKäyttäytymisenArvioArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response37272DataOpiskeluoikeudetSuorituksetOsaamisala  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| osaamisala | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArviointikohteet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| arvosana | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöArviointiArvioitsijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| ntm | boolean |  | No |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetNäyttöSuoritusaika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | string |  | Yes |
| loppu | string |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetOsasuorituksetTunnustettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koulutusmoduuli | [KoskiKoulutusmoduuli](#koskikoulutusmoduuli) |  | Yes |
| tyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37272DataOpiskeluoikeudetSuorituksetVahvistus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| päivä | string |  | Yes |
| paikkakunta | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| myöntäjäOrganisaatio | [KoskiOrganisaatio](#koskiorganisaatio) |  | Yes |
| myöntäjäHenkilöt | [ [KoskiMyontajaHenkilo](#koskimyontajahenkilo) ] |  | Yes |

### Response37272Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37272Meta | object |  |  |

### Response37273  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37273Meta](#response37273meta) |  | Yes |
| data | [Environment](#environment) |  | Yes |

### Response37273Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37273Meta | object |  |  |

### TranslatedValue  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fi | string |  | No |
| sv | string |  | No |
| en | string |  | No |

### TutkinnonOsa  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| laajuus | long |  | Yes |
| eperusteet-diaarinumero | string |  | Yes |
| kuvaus | string |  | Yes |
| koulutustyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### TyopaikallaHankittavaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| vastuullinen-ohjaaja | [Henkilo](#henkilo) |  | Yes |
| keskeiset-tehtavat | [ string ] |  | Yes |
| hankkijan-edustaja | [Henkilo](#henkilo) |  | Yes |
| erityinen-tuki | boolean |  | Yes |
| jarjestajan-edustaja | [Henkilo](#henkilo) |  | Yes |
| erityisen-tuen-aika | [HoksArvotPuuttuvaOsaaminenTyopaikallaHankittavaOsaaminenErityisenTuenAika](#hoksarvotpuuttuvaosaaminentyopaikallahankittavaosaaminenerityisentuenaika) |  | Yes |
| muut-osallistujat | [ [Henkilo](#henkilo) ] |  | Yes |
| muut-oppimisymparistot | [ [HoksArvotPuuttuvaOsaaminenTyopaikallaHankittavaOsaaminenMuutOppimisymparistot](#hoksarvotpuuttuvaosaaminentyopaikallahankittavaosaaminenmuutoppimisymparistot) ] |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |
| ohjaus-ja-tuki | boolean |  | Yes |

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
| contact-values-group | [ [Response37264DataContactValuesGroup](#response37264datacontactvaluesgroup) ] |  | No |

### YTOTutkinnonOsa  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| laajuus | long |  | Yes |
| eperusteet-diaarinumero | string |  | Yes |
| kuvaus | string |  | Yes |
| koulutustyyppi | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| osa-alue-tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |