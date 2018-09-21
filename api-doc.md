eHOKS backend
=============
Backend for eHOKS

**Version:** 0.0.1

### /ehoks/api/v1/healthcheck
---
##### ***GET***
**Summary:** Service healthcheck status

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [HealthcheckStatus](#healthcheckstatus) |

### /ehoks/api/v1/education/info/
---
##### ***GET***
**Summary:** System information for education provider

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response116503](#response116503) |

### /ehoks/api/v1/work/info/
---
##### ***GET***
**Summary:** System information for workplace provider

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response116504](#response116504) |

### /ehoks/api/v1/student/info/
---
##### ***GET***
**Summary:** System information for student

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response116505](#response116505) |

### /ehoks/api/v1/session/user-info
---
##### ***GET***
**Summary:** Get current user info

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response116506](#response116506) |

### /ehoks/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Updates session user info from Oppijanumerorekisteri

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response116507](#response116507) |

### /ehoks/api/v1/session/opintopolku/
---
##### ***GET***
**Summary:** Get current Opintopolku session

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response116508](#response116508) |

##### ***OPTIONS***
**Summary:** Options for session DELETE (logout)

**Responses**

| Code | Description |
| ---- | ----------- |
| default |  |

##### ***DELETE***
**Summary:** Delete Opintopolku session (logout)

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response116509](#response116509) |

##### ***POST***
**Summary:** Creates new Opintopolku session and redirects to frontend

**Description:** Creates new Opintopolku session. After storing session
                    http status 'See Other' (303) will be returned with url of
                    frontend in configuration.

**Responses**

| Code | Description |
| ---- | ----------- |
| default |  |

### /ehoks/api/v1/localization
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
| 200 |  | [Response116510](#response116510) |

### /ehoks/api/v1/hoks/{id}
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
| 200 |  | [Response116511](#response116511) |

### /ehoks/api/v1/hoks
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
| 200 |  | [Response116512](#response116512) |

### /ehoks/api/v1/hoks/{id}/osaamiset/
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
| 200 |  | [Response116513](#response116513) |

### /ehoks/api/v1/hoks/{id}/koulutukset/
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
| 200 |  | [Response116514](#response116514) |

### /ehoks/api/v1/hoks/{id}/suunnitellut-osaamiset/
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
| 200 |  | [Response116515](#response116515) |

### Models
---

### HOKS  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| suunnitellut-osaamiset | [ [SuunniteltuOsaaminen](#suunniteltuosaaminen) ] |  | Yes |
| paivittajan-oid | string |  | Yes |
| koulutukset | [ [Osaaminen](#osaaminen) ] |  | Yes |
| oppijan-oid | string |  | Yes |
| luotu | dateTime |  | Yes |
| osaamiset | [ [Osaaminen](#osaaminen) ] |  | Yes |
| osaamisala | [Osaamisala](#osaamisala) |  | Yes |
| tutkintotavoite | string |  | Yes |
| hyvaksytty | dateTime |  | Yes |
| luonnin-hyvaksyjan-oid | string |  | Yes |
| opiskeluoikeus-paattymispvm | dateTime |  | Yes |
| id | long |  | Yes |
| versio | long |  | Yes |
| paivityksen-hyvaksyjan-oid | string |  | Yes |
| paivitetty | dateTime |  | Yes |
| tutkinnon-perusteet-diaarinumero | string |  | Yes |
| luojan-oid | string |  | Yes |
| urasuunnitelma | string |  | Yes |
| opiskeluoikeus-alkupvm | dateTime |  | Yes |

### HOKSArvot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| oppijan-oid | string |  | Yes |
| urasuunnitelma | string |  | Yes |
| tutkintotavoite | string |  | Yes |
| tutkinnon-perusteet-diaarinumero | string |  | Yes |
| osaamisala | [Osaamisala](#osaamisala) |  | Yes |
| opiskeluoikeus-alkupvm | dateTime |  | Yes |
| opiskeluoikeus-paattymispvm | dateTime |  | Yes |

### HealthcheckStatus  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| HealthcheckStatus | object |  |  |

### Information  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| basic-information | [Translated](#translated) |  | Yes |
| hoks-process | [Translated](#translated) |  | Yes |

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

### Response116503  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116503Meta](#response116503meta) |  | Yes |
| data | [ [Information](#information) ] |  | Yes |

### Response116503Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116503Meta | object |  |  |

### Response116504  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116504Meta](#response116504meta) |  | Yes |
| data | [ [Information](#information) ] |  | Yes |

### Response116504Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116504Meta | object |  |  |

### Response116505  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116505Meta](#response116505meta) |  | Yes |
| data | [ [Information](#information) ] |  | Yes |

### Response116505Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116505Meta | object |  |  |

### Response116506  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116506Meta](#response116506meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response116506DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response116506DataContactValuesGroupContact](#response116506datacontactvaluesgroupcontact) ] |  | Yes |

### Response116506DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response116506Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116506Meta | object |  |  |

### Response116507  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116507Meta](#response116507meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response116507Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116507Meta | object |  |  |

### Response116508  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116508Meta](#response116508meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response116508Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response116509  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116509Meta](#response116509meta) |  | Yes |
| data | [  ] |  | Yes |

### Response116509Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116509Meta | object |  |  |

### Response116510  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116510Meta](#response116510meta) |  | Yes |
| data | [ [Response116510Data](#response116510data) ] |  | Yes |

### Response116510Data  

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

### Response116510Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116510Meta | object |  |  |

### Response116511  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116511Meta](#response116511meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response116511Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116511Meta | object |  |  |

### Response116512  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116512Meta](#response116512meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response116512Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116512Meta | object |  |  |

### Response116513  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116513Meta](#response116513meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response116513Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116513Meta | object |  |  |

### Response116514  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116514Meta](#response116514meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response116514Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116514Meta | object |  |  |

### Response116515  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response116515Meta](#response116515meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response116515Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response116515Meta | object |  |  |

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

### Translated  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fi | string |  | Yes |
| en | string |  | No |
| sv | string |  | No |

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
| contact-values-group | [ [Response116506DataContactValuesGroup](#response116506datacontactvaluesgroup) ] |  | No |