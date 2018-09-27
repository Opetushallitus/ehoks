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
| 200 |  | [Response97672](#response97672) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response97673](#response97673) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response97674](#response97674) |

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
| 200 |  | [Response97675](#response97675) |

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
| 200 |  | [Response97676](#response97676) |

### /ehoks-backend/api/v1/hoks/{id}/osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin olemassa oleva osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| Osaaminen | body |  | Yes | [Osaaminen](#osaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response97677](#response97677) |

### /ehoks-backend/api/v1/hoks/{id}/koulutukset/
---
##### ***POST***
**Summary:** Lisää HOKSiin koulutus

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| Osaaminen | body |  | Yes | [Osaaminen](#osaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response97678](#response97678) |

### /ehoks-backend/api/v1/hoks/{id}/suunnitellut-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin suunniteltu osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| SuunniteltuOsaaminen | body |  | Yes | [SuunniteltuOsaaminen](#suunniteltuosaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response97679](#response97679) |

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
| 200 |  | [Response97680](#response97680) |

### Models
---

### HOKS  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| suunnitellut-osaamiset | [ [SuunniteltuOsaaminen](#suunniteltuosaaminen) ] |  | Yes |
| paivittajan-oid | string |  | Yes |
| koulutukset | [ [Osaaminen](#osaaminen) ] |  | Yes |
| luotu | dateTime |  | Yes |
| osaamiset | [ [Osaaminen](#osaaminen) ] |  | Yes |
| hyvaksytty | dateTime |  | Yes |
| luonnin-hyvaksyjan-oid | string |  | Yes |
| opiskeluoikeus-oid | string |  | Yes |
| id | long |  | Yes |
| versio | long |  | Yes |
| paivityksen-hyvaksyjan-oid | string |  | Yes |
| paivitetty | dateTime |  | Yes |
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
| tyyppi | string |  | Yes |
| hoks-id | long |  | Yes |
| perusteet-diaarinumero | string |  | Yes |
| osaamisala | [Osaamisala](#osaamisala) |  | Yes |
| suorituspvm | dateTime |  | Yes |
| todentaja | string |  | Yes |
| liitteet | [ string ] |  | No |

### Osaamisala  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| versio | long |  | Yes |
| uri | string |  | Yes |

### POSTResponse  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| uri | string |  | Yes |

### Response97672  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97672Meta](#response97672meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response97672DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response97672DataContactValuesGroupContact](#response97672datacontactvaluesgroupcontact) ] |  | Yes |

### Response97672DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response97672Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97672Meta | object |  |  |

### Response97673  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97673Meta](#response97673meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response97673Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97673Meta | object |  |  |

### Response97674  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97674Meta](#response97674meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response97674Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response97675  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97675Meta](#response97675meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response97675Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97675Meta | object |  |  |

### Response97676  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97676Meta](#response97676meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response97676Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97676Meta | object |  |  |

### Response97677  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97677Meta](#response97677meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response97677Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97677Meta | object |  |  |

### Response97678  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97678Meta](#response97678meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response97678Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97678Meta | object |  |  |

### Response97679  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97679Meta](#response97679meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response97679Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97679Meta | object |  |  |

### Response97680  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response97680Meta](#response97680meta) |  | Yes |
| data | [ [Response97680Data](#response97680data) ] |  | Yes |

### Response97680Data  

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

### Response97680Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response97680Meta | object |  |  |

### SuunniteltuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| osaamisala | [Osaamisala](#osaamisala) |  | Yes |
| keskeiset-tehtavat | [ string ] |  | Yes |
| erityinen-tuki | boolean |  | Yes |
| sisalto | string |  | Yes |
| perusteet-diaarinumero | string |  | Yes |
| hoks-id | long |  | Yes |
| alku | dateTime |  | Yes |
| tyyppi | string |  | Yes |
| loppu | dateTime |  | Yes |
| suoritustapa | string |  | Yes |
| organisaatio | string |  | Yes |
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
| contact-values-group | [ [Response97672DataContactValuesGroup](#response97672datacontactvaluesgroup) ] |  | No |