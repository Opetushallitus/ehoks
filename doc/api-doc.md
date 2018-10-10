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
| 200 |  | [Response37111](#response37111) |

### /ehoks-backend/api/v1/session/update-user-info
---
##### ***POST***
**Summary:** Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37112](#response37112) |

### /ehoks-backend/api/v1/session
---
##### ***GET***
**Summary:** Käyttäjän istunto

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37113](#response37113) |

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
| 200 |  | [Response37114](#response37114) |

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
| 200 |  | [Response37115](#response37115) |

### /ehoks-backend/api/v1/hoks/{id}/olemassa-olevat-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin olemassa oleva osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| OlemassaOlevaOsaaminen | body |  | Yes | [OlemassaOlevaOsaaminen](#olemassaolevaosaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37116](#response37116) |

### /ehoks-backend/api/v1/hoks/{id}/muut-todennetut-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin muu todennettu osaaminen

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| MuuTodennettuOsaaminen | body |  | Yes | [MuuTodennettuOsaaminen](#muutodennettuosaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37117](#response37117) |

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
| 200 |  | [Response37118](#response37118) |

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
| 200 |  | [Response37119](#response37119) |

### /ehoks-backend/api/v1/hoks/{id}/tyopaikalla-hankittavat-osaamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin työpaikalla tapahtuvan osaamisen tiedot

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| TyopaikallaHankittavaOsaaminen | body |  | Yes | [TyopaikallaHankittavaOsaaminen](#tyopaikallahankittavaosaaminen) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37120](#response37120) |

### /ehoks-backend/api/v1/hoks/{id}/osaamisen-osoittamiset/
---
##### ***POST***
**Summary:** Lisää HOKSiin hankitun osaamisen osoittaminen/näyttö

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| HankitunOsaamisenNaytto | body |  | Yes | [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) |
| id | path |  | Yes | string |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37121](#response37121) |

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
| 200 |  | [Response37122](#response37122) |

### /ehoks-backend/api/v1/external/koodistokoodi/{uri}/{versio}
---
##### ***GET***
**Summary:** Hakee koodisto koodin tietoja Kooidsto-palvelusta

**Parameters**

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| uri | path |  | Yes | string |
| versio | path |  | Yes | long |

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37123](#response37123) |

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
| 200 |  | [Response37124](#response37124) |

### /ehoks-backend/api/v1/misc/environment
---
##### ***GET***
**Summary:** Palauttaa ympäristön tiedot ja asetukset

**Responses**

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 |  | [Response37125](#response37125) |

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
| puuttuvat-osaamiset | [ [PuuttuvaOsaaminen](#puuttuvaosaaminen) ] |  | Yes |
| paivittajan-oid | string |  | Yes |
| tukevat-opinnot | [ [TukevaOpinto](#tukevaopinto) ] |  | Yes |
| luotu | dateTime |  | Yes |
| hyvaksytty | dateTime |  | Yes |
| luonnin-hyvaksyjan-oid | string |  | Yes |
| muut-todennetut-osaamiset | [ [MuuTodennettuOsaaminen](#muutodennettuosaaminen) ] |  | Yes |
| opiskeluoikeus-oid | string |  | Yes |
| id | long |  | Yes |
| versio | long |  | Yes |
| paivityksen-hyvaksyjan-oid | string |  | Yes |
| olemassa-olevat-osaamiset | [ [OlemassaOlevaOsaaminen](#olemassaolevaosaaminen) ] |  | Yes |
| paivitetty | dateTime |  | Yes |
| osaamisen-osoittamiset | [ [HankitunOsaamisenNaytto](#hankitunosaamisennaytto) ] |  | Yes |
| luojan-oid | string |  | Yes |
| tyopaikalla-hankittavat-osaamiset | [ [TyopaikallaHankittavaOsaaminen](#tyopaikallahankittavaosaaminen) ] |  | Yes |
| urasuunnitelma | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### HOKSArvot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opiskeluoikeus-oid | string |  | Yes |
| urasuunnitelma | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### HankitunOsaamisenNaytto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvioijat | [ [HankitunOsaamisenNayttoArvioijat](#hankitunosaamisennayttoarvioijat) ] |  | Yes |
| kuvaus | string |  | Yes |
| osaamistavoitteet | [ [KoodistoKoodi](#koodistokoodi) ] |  | Yes |
| ammattitaitovaatimukset | [ [KoodistoKoodi](#koodistokoodi) ] |  | Yes |
| nayttoymparisto | [Organisaatio](#organisaatio) |  | Yes |
| sisalto | string |  | Yes |
| arviointikriteerit | [ [HankitunOsaamisenNayttoArviointikriteerit](#hankitunosaamisennayttoarviointikriteerit) ] |  | Yes |
| jarjestaja | [HankitunOsaamisenNayttoJarjestaja](#hankitunosaamisennayttojarjestaja) |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### HankitunOsaamisenNayttoArvioijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| rooli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| organisaatio | [Organisaatio](#organisaatio) |  | Yes |

### HankitunOsaamisenNayttoArviointikriteerit  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvosana | long |  | Yes |
| kuvaus | string |  | Yes |

### HankitunOsaamisenNayttoJarjestaja  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| oid | string |  | Yes |

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

### MuuTodennettuOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| kuvaus | string |  | Yes |
| liitteet | [ string ] |  | Yes |

### Nimi  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fi | string |  | No |
| sv | string |  | No |
| en | string |  | No |

### OlemassaOlevaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| tutkinnon-diaarinumero | string |  | Yes |

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
| osaamisalat | [ [Response37124DataOsaamisalat](#response37124dataosaamisalat) ] |  | No |
| tutkintonimikkeet | [ [Response37124DataTutkintonimikkeet](#response37124datatutkintonimikkeet) ] |  | No |

### PuuttuvaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| poikkeama | [PuuttuvaOsaaminenPoikkeama](#puuttuvaosaaminenpoikkeama) |  | Yes |
| osaamisen-hankkimistavat | [ [PuuttuvaOsaaminenOsaamisenHankkimistavat](#puuttuvaosaaminenosaamisenhankkimistavat) ] |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |
| koulutuksen-jarjestaja-oid | string |  | Yes |
| tarvittava-opetus | string |  | Yes |

### PuuttuvaOsaaminenOsaamisenHankkimistavat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ajankohta | [DateRange](#daterange) |  | Yes |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### PuuttuvaOsaaminenPoikkeama  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alkuperainen-tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| kuvaus | string |  | Yes |

### Response37111  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37111Meta](#response37111meta) |  | Yes |
| data | [ [UserInfo](#userinfo) ] |  | Yes |

### Response37111DataContactValuesGroup  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | Yes |
| contact | [ [Response37111DataContactValuesGroupContact](#response37111datacontactvaluesgroupcontact) ] |  | Yes |

### Response37111DataContactValuesGroupContact  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| value | string |  | Yes |
| type | string |  | Yes |

### Response37111Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37111Meta | object |  |  |

### Response37112  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37112Meta](#response37112meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response37112Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37112Meta | object |  |  |

### Response37113  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37113Meta](#response37113meta) |  | Yes |
| data | [ [User](#user) ] |  | Yes |

### Response37113Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| opintopolku-login-url | string |  | Yes |

### Response37114  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37114Meta](#response37114meta) |  | Yes |
| data | [HOKS](#hoks) |  | Yes |

### Response37114DataOsaamisenOsoittamisetArvioijat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| rooli | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| organisaatio | [Organisaatio](#organisaatio) |  | Yes |

### Response37114DataOsaamisenOsoittamisetArviointikriteerit  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| arvosana | long |  | Yes |
| kuvaus | string |  | Yes |

### Response37114DataOsaamisenOsoittamisetJarjestaja  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| oid | string |  | Yes |

### Response37114DataPuuttuvatOsaamisetOsaamisenHankkimistavat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| ajankohta | [DateRange](#daterange) |  | Yes |
| osaamisen-hankkimistavan-tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |

### Response37114DataPuuttuvatOsaamisetPoikkeama  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alkuperainen-tutkinnon-osa | [TutkinnonOsa](#tutkinnonosa) |  | Yes |
| kuvaus | string |  | Yes |

### Response37114DataTyopaikallaHankittavatOsaamisetErityisenTuenAika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date |  | Yes |
| loppu | date |  | Yes |

### Response37114DataTyopaikallaHankittavatOsaamisetMuutOppimisymparistot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paikka | string |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### Response37114Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37114Meta | object |  |  |

### Response37115  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37115Meta](#response37115meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37115Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37115Meta | object |  |  |

### Response37116  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37116Meta](#response37116meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37116Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37116Meta | object |  |  |

### Response37117  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37117Meta](#response37117meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37117Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37117Meta | object |  |  |

### Response37118  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37118Meta](#response37118meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37118Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37118Meta | object |  |  |

### Response37119  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37119Meta](#response37119meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37119Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37119Meta | object |  |  |

### Response37120  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37120Meta](#response37120meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37120Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37120Meta | object |  |  |

### Response37121  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37121Meta](#response37121meta) |  | Yes |
| data | [POSTResponse](#postresponse) |  | Yes |

### Response37121Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37121Meta | object |  |  |

### Response37122  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37122Meta](#response37122meta) |  | Yes |
| data | [ [Response37122Data](#response37122data) ] |  | Yes |

### Response37122Data  

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

### Response37122Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37122Meta | object |  |  |

### Response37123  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37123Meta](#response37123meta) |  | Yes |
| data | [ExtendedKoodistoKoodi](#extendedkoodistokoodi) |  | Yes |

### Response37123Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37123Meta | object |  |  |

### Response37124  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37124Meta](#response37124meta) |  | Yes |
| data | [ [Peruste](#peruste) ] |  | Yes |

### Response37124DataOsaamisalat  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response37124DataTutkintonimikkeet  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | [Nimi](#nimi) |  | Yes |

### Response37124Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37124Meta | object |  |  |

### Response37125  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| meta | [Response37125Meta](#response37125meta) |  | Yes |
| data | [Environment](#environment) |  | Yes |

### Response37125Meta  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| Response37125Meta | object |  |  |

### TukevaOpinto  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| nimi | string |  | Yes |
| kuvaus | string |  | Yes |
| kesto-paivina | long |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

### TutkinnonOsa  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tunniste | [KoodistoKoodi](#koodistokoodi) |  | Yes |
| laajuus | long |  | Yes |

### TyopaikallaHankittavaOsaaminen  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| vastuullinen-ohjaaja | [Henkilo](#henkilo) |  | Yes |
| keskeiset-tehtavat | [ string ] |  | Yes |
| hankkijan-edustaja | [Henkilo](#henkilo) |  | Yes |
| erityinen-tuki | boolean |  | Yes |
| jarjestajan-edustaja | [Henkilo](#henkilo) |  | Yes |
| erityisen-tuen-aika | [TyopaikallaHankittavaOsaaminenErityisenTuenAika](#tyopaikallahankittavaosaaminenerityisentuenaika) |  | Yes |
| muut-osallistujat | [ [Henkilo](#henkilo) ] |  | Yes |
| muut-oppimisymparistot | [ [TyopaikallaHankittavaOsaaminenMuutOppimisymparistot](#tyopaikallahankittavaosaaminenmuutoppimisymparistot) ] |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |
| ohjaus-ja-tuki | boolean |  | Yes |

### TyopaikallaHankittavaOsaaminenErityisenTuenAika  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| alku | date |  | Yes |
| loppu | date |  | Yes |

### TyopaikallaHankittavaOsaaminenMuutOppimisymparistot  

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| paikka | string |  | Yes |
| ajankohta | [DateRange](#daterange) |  | Yes |

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
| contact-values-group | [ [Response37111DataContactValuesGroup](#response37111datacontactvaluesgroup) ] |  | No |