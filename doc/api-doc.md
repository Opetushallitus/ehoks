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
| 200 |  | [Response178716](#response178716) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response178717](#response178717) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response178718](#response178718) |

##### ***OPTIONS***
**Responses**

| Code | Description |
| ---- | ----------- |
| default |  |

##### ***DELETE***
**Summary:** Uloskirjautuminen. Palauttaa uudelleenohjauksen Opintopolun
                uloskirjautumiseen.

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
| 200 |  | [Response178719](#response178719) |

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
| 200 |  | [Response178720](#response178720) |

### /ehoks-backend/api/v1/hoks/{id}/todennetut-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin todennettu osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| TodennettuOsaaminen | body |  | Yes | [TodennettuOsaaminen](#todennettuosaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response178721](#response178721) |

### /ehoks-backend/api/v1/hoks/{id}/todentamattomat-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin todentamaton osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| TodentamatonOsaaminen | body |  | Yes | [TodentamatonOsaaminen](#todentamatonosaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response178722](#response178722) |

### /ehoks-backend/api/v1/hoks/{id}/tukevat-opinnot/
---
##### ***POST***
**Summary:** Lisää HOKSiin opiskeluvalmiuksia tukeva opinto

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| TukevaOpinto | body |  | Yes | [TukevaOpinto](#tukevaopinto) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response178723](#response178723) |

### /ehoks-backend/api/v1/hoks/{id}/puuttuvat-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin puuttuvan osaamisen hankkiminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| PuuttuvaOsaaminen | body |  | Yes | [PuuttuvaOsaaminen](#puuttuvaosaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response178724](#response178724) |

### /ehoks-backend/api/v1/lokalisointi
---
##### ***GET***
**Summary:** Localizations for ehoks

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| category | query |  | No | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response178725](#response178725) |

### Models
---

### HOKS  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| todennetut-osaamiset | [ [TodennettuOsaaminen](#todennettuosaaminen) ] |  | Yes |
| puuttuvat-osaamiset | [ [PuuttuvaOsaaminen](#puuttuvaosaaminen) ] |  | Yes |
| paivittajan-oid | string |  | Yes |
| tukevat-opinnot | [ [TukevaOpinto](#tukevaopinto) ] |  | Yes |
| luotu | dateTime |  | Yes |
| hyvaksytty | dateTime |  | Yes |
| luonnin-hyvaksyjan-oid | string |  | Yes |
| opiskeluoikeus-oid | string |  | Yes |
| id | long |  | Yes |
| versio | long |  | Yes |
| paivityksen-hyvaksyjan-oid | string |  | Yes |
| paivitetty | dateTime |  | Yes |
| todentamattomat-osaamiset | [ [TodentamatonOsaaminen](#todentamatonosaaminen) ] |  | Yes |
| luojan-oid | string |  | Yes |
| urasuunnitelma | string |  | Yes |

### HOKSArvot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opiskeluoikeus-oid | string |  | Yes |
| urasuunnitelma | string |  | Yes |

### HealthcheckStatus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| HealthcheckStatus | object |  |  |

### KoodistoKoodi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koodisto-koodi | string |  | Yes |
| koodisto-uri | string |  | Yes |
| versio | long |  | Yes |

### POSTResponse  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| uri | string |  | Yes |

### PuuttuvaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tutkinnon-osan-koodi | string |  | Yes |
| tutkinnon-koodi | string |  | Yes |
| osaamisen-hankkimistavat | [ [PuuttuvaOsaaminenOsaamisenHankkimistavat](#puuttuvaosaaminenosaamisenhankkimistavat) ] |  | Yes |
| keskeiset-tehtavat | [ string ] |  | Yes |
| erityinen-tuki | boolean |  | Yes |
| vastaava-ohjaaja | string |  | Yes |
| sisalto | string |  | Yes |
| organisaatio | [PuuttuvaOsaaminenOrganisaatio](#puuttuvaosaaminenorganisaatio) |  | Yes |
| ohjaus-ja-tuki | boolean |  | Yes |

### PuuttuvaOsaaminenOrganisaatio  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| y-tunnus | string |  | Yes |

### PuuttuvaOsaaminenOsaamisenHankkimistavat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date |  | Yes |
| loppu | date |  | Yes |
| osaamisen-hankkimistapa | [PuuttuvaOsaaminenOsaamisenHankkimistavatOsaamisenHankkimistapa](#puuttuvaosaaminenosaamisenhankkimistavatosaamisenhankkimistapa) |  | Yes |

### PuuttuvaOsaaminenOsaamisenHankkimistavatOsaamisenHankkimistapa  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response178716  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178716Meta](#response178716meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response178716DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response178716DataContactValuesGroupContact](#response178716datacontactvaluesgroupcontact) ] |  | Yes |

### Response178716DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response178716Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178716Meta | object |  |  |

### Response178717  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178717Meta](#response178717meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response178717Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178717Meta | object |  |  |

### Response178718  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178718Meta](#response178718meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response178718Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response178719  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178719Meta](#response178719meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response178719DataPuuttuvatOsaamisetOrganisaatio  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| y-tunnus | string |  | Yes |

### Response178719DataPuuttuvatOsaamisetOsaamisenHankkimistavat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date |  | Yes |
| loppu | date |  | Yes |
| osaamisen-hankkimistapa | [Response178719DataPuuttuvatOsaamisetOsaamisenHankkimistavatOsaamisenHankkimistapa](#response178719datapuuttuvatosaamisetosaamisenhankkimistavatosaamisenhankkimistapa) |  | Yes |

### Response178719DataPuuttuvatOsaamisetOsaamisenHankkimistavatOsaamisenHankkimistapa  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response178719Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178719Meta | object |  |  |

### Response178720  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178720Meta](#response178720meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response178720Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178720Meta | object |  |  |

### Response178721  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178721Meta](#response178721meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response178721Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178721Meta | object |  |  |

### Response178722  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178722Meta](#response178722meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response178722Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178722Meta | object |  |  |

### Response178723  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178723Meta](#response178723meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response178723Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178723Meta | object |  |  |

### Response178724  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178724Meta](#response178724meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response178724Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178724Meta | object |  |  |

### Response178725  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response178725Meta](#response178725meta) |  | Yes |
| data | [ [Response178725Data](#response178725data) ] |  | Yes |

### Response178725Data  

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

### Response178725Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response178725Meta | object |  |  |

### TodennettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| liitteet | [ string ] |  | No |

### TodentamatonOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| kuvaus | string |  | Yes |
| suorituspvm | date |  | Yes |
| yto-koodi | string |  | No |
| nimi | string |  | Yes |
| laajuus | string |  | Yes |
| kesto | string |  | Yes |
| liitteet | [ string ] |  | No |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | No |
| tyyppi | string |  | Yes |

### TukevaOpinto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| kuvaus | string |  | Yes |
| kesto-paivina | long |  | Yes |
| alku | date |  | Yes |
| loppu | date |  | Yes |

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
| contact-values-group | [ [Response178716DataContactValuesGroup](#response178716datacontactvaluesgroup) ] |  | No |