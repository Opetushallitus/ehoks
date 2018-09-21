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
| 200 |  | [Response104521](#response104521) |

### /ehoks/api/v1/work/info/
---
##### ***GET***
**Summary:** System information for workplace provider

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response104522](#response104522) |

### /ehoks/api/v1/student/info/
---
##### ***GET***
**Summary:** System information for student

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response104523](#response104523) |

### /ehoks/api/v1/session/user-info
---
##### ***GET***
**Summary:** Get current user info

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response104524](#response104524) |

### /ehoks/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Updates session user info from Oppijanumerorekisteri

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response104525](#response104525) |

### /ehoks/api/v1/session/opintopolku/
---
##### ***GET***
**Summary:** Get current Opintopolku session

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response104526](#response104526) |

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
| 200 |  | [Response104527](#response104527) |

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
| 200 |  | [Response104528](#response104528) |

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
| 200 |  | [Response104529](#response104529) |

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
| 200 |  | [Response104530](#response104530) |

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
| 200 |  | [Response104531](#response104531) |

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
| 200 |  | [Response104532](#response104532) |

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
| 200 |  | [Response104533](#response104533) |

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
| liitteet | [ string ] |  | Yes |

### Osaamisala  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| versio | long |  | Yes |
| uri | string |  | Yes |

### POSTResponse  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| uri | string |  | Yes |

### Response104521  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104521Meta](#response104521meta) |  | Yes |
| data | [ [Information](#information) ] |  | Yes |

### Response104521Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104521Meta | object |  |  |

### Response104522  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104522Meta](#response104522meta) |  | Yes |
| data | [ [Information](#information) ] |  | Yes |

### Response104522Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104522Meta | object |  |  |

### Response104523  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104523Meta](#response104523meta) |  | Yes |
| data | [ [Information](#information) ] |  | Yes |

### Response104523Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104523Meta | object |  |  |

### Response104524  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104524Meta](#response104524meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response104524DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response104524DataContactValuesGroupContact](#response104524datacontactvaluesgroupcontact) ] |  | Yes |

### Response104524DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response104524Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104524Meta | object |  |  |

### Response104525  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104525Meta](#response104525meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response104525Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104525Meta | object |  |  |

### Response104526  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104526Meta](#response104526meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response104526Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response104527  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104527Meta](#response104527meta) |  | Yes |
| data | [  ] |  | Yes |

### Response104527Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104527Meta | object |  |  |

### Response104528  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104528Meta](#response104528meta) |  | Yes |
| data | [ [Response104528Data](#response104528data) ] |  | Yes |

### Response104528Data  

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

### Response104528Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104528Meta | object |  |  |

### Response104529  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104529Meta](#response104529meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response104529Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104529Meta | object |  |  |

### Response104530  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104530Meta](#response104530meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response104530Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104530Meta | object |  |  |

### Response104531  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104531Meta](#response104531meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response104531Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104531Meta | object |  |  |

### Response104532  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104532Meta](#response104532meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response104532Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104532Meta | object |  |  |

### Response104533  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response104533Meta](#response104533meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response104533Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response104533Meta | object |  |  |

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
| contact-values-group | [ [Response104524DataContactValuesGroup](#response104524datacontactvaluesgroup) ] |  | No |