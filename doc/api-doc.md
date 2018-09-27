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
| 200 |  | [Response245153](#response245153) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response245154](#response245154) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response245155](#response245155) |

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
| 200 |  | [Response245156](#response245156) |

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
| 200 |  | [Response245157](#response245157) |

### /ehoks-backend/api/v1/hoks/{id}/todennetut-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin todennettu osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| Osaaminen | body |  | Yes | [Osaaminen](#osaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response245158](#response245158) |

### /ehoks-backend/api/v1/hoks/{id}/todentamattomat-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin todentamaton osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| Osaaminen | body |  | Yes | [Osaaminen](#osaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response245159](#response245159) |

### /ehoks-backend/api/v1/hoks/{id}/tukevat-opinnot/
---
##### ***POST***
**Summary:** Lisää HOKSiin opiskeluvalmiuksia tukeva opinto

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| Osaaminen | body |  | Yes | [Osaaminen](#osaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response245160](#response245160) |

### /ehoks-backend/api/v1/hoks/{id}/puuttuvat-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin puuttuvan osaamisen hankkiminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| Osaaminen | body |  | Yes | [Osaaminen](#osaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response245161](#response245161) |

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
| 200 |  | [Response245162](#response245162) |

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

### Osaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| todentaja | string |  | Yes |
| osaamisala | [Osaamisala](#osaamisala) |  | Yes |
| liitteet | [ string ] |  | No |
| suorituspvm | dateTime |  | Yes |
| perusteet-diaarinumero | string |  | Yes |
| hoks-id | long |  | Yes |
| tyyppi | string |  | Yes |

### Osaamisala  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| versio | long |  | Yes |
| uri | string |  | Yes |

### POSTResponse  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| uri | string |  | Yes |

### PuuttuvaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tutkinnon-osan-koodi | string |  | Yes |
| tutkinnon-koodi | string |  | Yes |
| osaamisen-hankkimistavat | [ [Response245156DataPuuttuvatOsaamisetOsaamisenHankkimistavat](#response245156datapuuttuvatosaamisetosaamisenhankkimistavat) ] |  | Yes |
| keskeiset-tehtavat | [ string ] |  | Yes |
| erityinen-tuki | boolean |  | Yes |
| ohjaaja | string |  | Yes |
| sisalto | string |  | Yes |
| organisaatio | string |  | Yes |
| ohjaus-ja-tuki | boolean |  | Yes |

### Response245153  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245153Meta](#response245153meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response245153DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response245153DataContactValuesGroupContact](#response245153datacontactvaluesgroupcontact) ] |  | Yes |

### Response245153DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response245153Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245153Meta | object |  |  |

### Response245154  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245154Meta](#response245154meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response245154Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245154Meta | object |  |  |

### Response245155  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245155Meta](#response245155meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response245155Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response245156  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245156Meta](#response245156meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response245156DataPuuttuvatOsaamisetOsaamisenHankkimistavat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | dateTime |  | Yes |
| loppu | dateTime |  | Yes |
| osaamisen-hankkimistapa | [Response245156DataPuuttuvatOsaamisetOsaamisenHankkimistavatOsaamisenHankkimistapa](#response245156datapuuttuvatosaamisetosaamisenhankkimistavatosaamisenhankkimistapa) |  | Yes |

### Response245156DataPuuttuvatOsaamisetOsaamisenHankkimistavatOsaamisenHankkimistapa  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [Response245156DataPuuttuvatOsaamisetOsaamisenHankkimistavatOsaamisenHankkimistapaTunniste](#response245156datapuuttuvatosaamisetosaamisenhankkimistavatosaamisenhankkimistapatunniste) |  | Yes |

### Response245156DataPuuttuvatOsaamisetOsaamisenHankkimistavatOsaamisenHankkimistapaTunniste  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| koodisto-koodi | string |  | Yes |
| koodisto-uri | string |  | Yes |
| versio | long |  | Yes |

### Response245156Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245156Meta | object |  |  |

### Response245157  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245157Meta](#response245157meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response245157Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245157Meta | object |  |  |

### Response245158  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245158Meta](#response245158meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response245158Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245158Meta | object |  |  |

### Response245159  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245159Meta](#response245159meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response245159Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245159Meta | object |  |  |

### Response245160  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245160Meta](#response245160meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response245160Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245160Meta | object |  |  |

### Response245161  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245161Meta](#response245161meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response245161Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245161Meta | object |  |  |

### Response245162  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response245162Meta](#response245162meta) |  | Yes |
| data | [ [Response245162Data](#response245162data) ] |  | Yes |

### Response245162Data  

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

### Response245162Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response245162Meta | object |  |  |

### TodennettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tutkinnon-osan-koodi | string |  | Yes |
| liitteet | [ string ] |  | No |

### TodentamatonOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| kuvaus | string |  | Yes |
| laajuus | string |  | Yes |
| kesto | string |  | Yes |
| suorituspvm | dateTime |  | Yes |
| liitteet | [ string ] |  | No |

### TukevaOpinto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| kuvaus | string |  | Yes |
| kesto-paivina | long |  | Yes |
| alku | dateTime |  | Yes |
| loppu | dateTime |  | Yes |

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
| contact-values-group | [ [Response245153DataContactValuesGroup](#response245153datacontactvaluesgroup) ] |  | No |