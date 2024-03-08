# Työpaikkajaksojen kestojen laskenta

> [!NOTE]
> Tämä dokumentti ei kata vielä kaikkia työpaikkajaksojen kestojen laskentaan
> liittyviä seikkoja. Dokumenttia täydennetään tarpeen vaatiessa.

## Sisällysluettelo

* [Työpaikkajakson kestoon vaikuttavat tekijät](#työpaikkajakson-kestoon-vaikuttavat-tekijät)
  - [Jyvitys](#jyvitys)
* [Kestonlaskennan menettelytavat eri rahoituskausina](#kestonlaskennan-menettelytavat-eri-rahoituskausina)
  - [Rahoituskausi 2022-2023 (1.7.2022 - 30.6.2023)](#työpaikkajaksojen-kestojen-laskenta-rahoituskaudella-2022-2023-172022---3062023)
    - [Työpaikkajaksojen kestoille tehtyjä korjauksia](#työpaikkajaksojen-kestoille-tehtyjä-korjauksia)
    - [Työvaiheet kuvattuna](#työvaiheet-kuvattuna)

## Työpaikkajakson kestoon vaikuttavat tekijät

### Jyvitys

Jos oppijalla on useita osittain tai kokonaan päällekäisiä työpaikkajaksoja
(joilla on samoja voimassaolopäiviä), päällekäisten päivien aika jaetaan
näinä päivinä aktiivisena olevien jaksojen kesken. Tätä toimenpidettä kutsutaan
työpaikkajaksojen kestonlaskennan yhteydessä *jyvitykseksi*.

**Esimerkki**

Sanotaan, että oppija suorittaa huhtikuussa 3 työpaikkajaksoa:
* jakso A, alkupäivä 12.4. ja päättymispäivä 20.4.
* jakso B, alkupäivä 15.4. ja päättymispäivä 22.4.
* jakso C: alkupäivä 14.4. ja päättymispäivä 18.4.

Jos oletetaan, että kestonlaskennassa huomioidaan kaikki viikonpäivät
(mukaanlukien lauantait ja sunnuntait), jyvityksen seurauksena päivien 12.4. - 22.4.
aika jakaantuisi jaksoille seuraavasti:

|Jakso|12.4.|13.4.|14.4.|15.4.|16.4.|17.4.|18.4.|19.4.|20.4.|21.4.|22.4.|Kesto jyvityksen jälkeen|
|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|------------------------|
| A   | 1   | 1   | 1/2 | 1/3 | 1/3 | 1/3 | 1/3 | 1/2 | 1/2 | -   | -   | 4.833                  |
| B   | -   | -   | -   | 1/3 | 1/3 | 1/3 | 1/3 | 1/2 | 1/2 | 1   | 1   | 4.333                  |
| C   | -   | -   | 1/2 | 1/3 | 1/3 | 1/3 | 1/3 | -   | -   | -   | -   | 1.833                  |

## Kestonlaskennan menettelytavat eri rahoituskausina

### Työpaikkajaksojen kestojen laskenta rahoituskaudella 2022-2023 (1.7.2022 - 30.6.2023)

* Työpaikkajaksoille laskettiin kesto vastaajatunnuksen muodostamisen
  yhteydessä. Kestoille tehtiin [joitakin
  korjauksia](#työpaikkajaksojen-kestoille-tehtyjä-korjauksia) lokakuussa 2023.
* Keston laskennassa eHOKSiin tallennetun työpaikkajakson alku- ja
  päättymispäivän välisestä aikajaksosta vähennetään jaksolle eHOKSiin
  tallennetut keskeytymisjaksot sekä Koski-palveluun tallennettavat
  opiskeluoikeuden väliaikaisen keskeytymisen jaksot sekä lomat (opiskeluoikeuden tila
  *"väliaikaisesti keskeytynyt"* tai *"loma"*). Jos eHOKSiin tallennettu keskeytymisjakso ja
  Koski-palveluun tallennettu keskeytymisjakso osuvat kokonaisuudessaan samalle
  ajalle, huomioidaan ainoastaan Koski-palveluun tallennettu tieto.
* Jos oppijan työpaikkajakson voimassaolon päivinä on aktiivisena muita saman
  oppijan työpaikkajaksoja, päällekäisten päivien aika jaetaan näiden
  työpaikkajaksojen kesken (kts. [jyvittäminen](#jyvitys)).
* Kestonlaskennassa huomioitiin kaikki viikonpäivät (eli arkipäivien lisäksi
  myös lauantait ja sunnuntait)
* Työpaikkajakson tietoihin tallennettua osa-aikaisuutta ei otettu
  rahoituskauden 2022-2023 kestonlaskennassa huomioon.

#### Työpaikkajaksojen kestoille tehtyjä korjauksia

Rahoituskaudella 1.7.2022 - 30.6.2023 työpaikkajaksoille lasketuille kestoille
tehtiin Opetushallituksen päätöksellä seuraavat korjaukset, listan mukaisessa
järjestyksessä:

1. Työpaikkajaksoille aiemmin lasketusta kestosta mitätöitiin jakson
   osa-aikaisuuden vaikutus, mikä on aiemmassa kestonlaskennassa vähentänyt
   jakson kokonaiskestoa, kun osa-aikaisuus on ollut alle $100 \mathrm{\\%}$.
   Osa-aikaisuuden mitätöinti suoritettiin jokaiselle jaksolle laskemalla
   korjattu kesto $K_\mathrm{eoa}$ seuraavalla tavalla:

   $$K_\mathrm{eoa} = \frac{100 \mathrm{\\%}}{P} K_\mathrm{oa} \quad ,$$

   missä $K_\mathrm{oa}$ on aiemmin laskettu työpaikkajakson kesto (jossa
   osa-aikaisuus on huomioitu) ja $P$ on työpaikkajakson tietoihin merkitty
   osa-aikaisuus prosentteina.

   **Esimerkki**

   Sanotaan, että 1.2.2023 alkaneelle ja 31.5.2023 päättyneelle
   työpaikkajaksolle $A$ on aiemmin laskettu kestoksi $K_\mathrm{A,oa} = 73$ ja
   jakson tietoihin on merkitty osa-aikaisuus $P_\mathrm{A} = 85 \mathrm{\\%}$.
   Tällöin jaksolle saadaan tässä vaiheessa korjatuksi kestoksi

   $$K_\mathrm{A,eoa} = \frac{100 \mathrm{\\%}}{P_A} K_\mathrm{A,oa}
   = \frac{100 \mathrm{\\%}}{85 \mathrm{\\%}} \cdot 73 = 85.882 \quad .$$

   Tätä ei pyöristetä kokonaisluvuksi vielä ennen seuraavaa korjauksen
   välivaihetta.

2. Korjatuissa työpaikkajaksojen kestoissa oli määrä ottaa huomioon kaikki
   viikonpäivät, eli aiemmasta poiketen myös lauantait ja sunnuntait. Korjattu
   kesto $K_\mathrm{eoa,kp}$, jossa osa-aikaisuuden mitätöinnin lisäksi on
   otettu huomioon jakson ajalle sijoittuvien arkipäivien sijasta kaikki
   viikonpäivät, laskettiin seuraavalla tavalla:

   $$K_\mathrm{eoa,kp} = \frac{N_\mathrm{kp}}{N_\mathrm{ap}}
   K_\mathrm{eoa} \quad , $$

   missä $K_\mathrm{eoa}$ on edellisessä vaiheessa (kohdassa 1.) laskettu
   korjattu kesto, josta osa-aikaisuus on mitätöity, ja $N_\mathrm{ap}$ ja
   $N_\mathrm{kp}$ ovat arkipäivien ja kaikkien viikonpäivien määrät jakson alku-
   ja loppupäivämäärän välillä (pitäen sisällään alku- ja loppupäivät, paitsi
   $N_\mathrm{ap}$ tapauksessa, jos sijoittuvat viikonlopulle).

   **Jatkaen kohdan 1. esimerkkiä**

   Jos jatketaan kohdan 1. esimerkkiä, jossa korjattu kesto
   $K_\mathrm{A,eoa} = 85.822$ ja aikavälille 1.2.2023 - 31.5.2023 sijoittuvien
   arkipäivien yhteenlaskettu määrä $N_\mathrm{A,ap} = 86$ ja kaikkien
   viikonpäivien määrä $N_\mathrm{A,kp} = 120$, on lopullinen korjattu kesto

   $$K_\mathrm{A,eoa,kp} = \frac{N_\mathrm{A,kp}}{N_\mathrm{A,ap}}
   K_\mathrm{A,eoa} = \frac{120}{86} \cdot 85.822 = 119.751 \approx 120
   \quad , $$

   kun laskettu kesto pyöristetään lähimpään kokonaislukuun.

#### Työvaiheet kuvattuna

Kestonlaskenta suoritettiin normaalisti työpaikkajaksojen niputuksen yhteydessä.
[Edellä mainitut korjaukset](#työpaikkajaksojen-kestoille-tehtyjä-korjauksia)
tehtiin massa-ajona Herätepalvelun DynamoDB:n jaksotunnus-tauluun yksittäisellä
Python-skriptillä. Ennestään lasketut kestot taltioitiin `kesto_vanha` nimiseen
kenttään taulun tietueissa, siltä varalta että niitä tarvitaan myöhemmin.

<details>
<summary>Korjauksissa käytetty Python-skripti</summary>

```python
import boto3
import numpy as np

from datetime import datetime, timedelta

AWS_PROFILE_NAME = 'oph-dev'
TABLE_NAME = 'pallero-services-heratepalvelu-tep-jaksotunnusTable87D50EF4-1KUH8IGOE8ZV3'
RAHOITUSKAUSI_ALKUPVM = '2022-07-01'
RAHOITUSKAUSI_LOPPUPVM = '2023-06-30'

session = boto3.Session(profile_name=AWS_PROFILE_NAME)
client = session.client('dynamodb')
paginator = client.get_paginator('scan')

scan_parameters = {
    'TableName': TABLE_NAME,
    'FilterExpression': ('niputuspvm >= :rahoituskausi_alkupvm AND '
                         'niputuspvm <= :rahoituskausi_loppupvm AND '
                         'attribute_exists(kesto) AND '
                         'attribute_not_exists(kesto_vanha)'),
    'ExpressionAttributeValues':
    {':rahoituskausi_alkupvm': {'S': RAHOITUSKAUSI_ALKUPVM},
     ':rahoituskausi_loppupvm': {'S': RAHOITUSKAUSI_LOPPUPVM}}
}

updated = 0

for page in paginator.paginate(**scan_parameters):
    for item in page['Items']:
        old_kesto = int(item['kesto']['N'])  # Ei `None`, kts. FilterExpression
        new_kesto = old_kesto  # Päivitetään tämän muuttujan arvoa myöhemmin

        # Aiemmin mukaan lasketun osa-aikaisuusuden mitätöinti.
        # Jos osa-aikaisuustieto puuttuu tai on nolla, tulkitaan ettei
        # osa-aikaisuutta ole (ts. on 100 %).
        if 'osa_aikaisuus' in item:
            osa_aikaisuus = int(item['osa_aikaisuus']['N'])
            if osa_aikaisuus != 0:
                new_kesto /= osa_aikaisuus / 100

        jakso_alkupvm = datetime.strptime(
            item['jakso_alkupvm']['S'],
            '%Y-%m-%d'
        ).date()
        jakso_loppupvm = datetime.strptime(
            item['jakso_loppupvm']['S'],
            '%Y-%m-%d'
        ).date() + timedelta(days=1)  # Loppupäivämäärä kuuluu jaksoon

        # Huomioidaan viikonloppujen päivät seuraavasti:
        new_kesto *= (jakso_loppupvm - jakso_alkupvm).days / \
            np.busday_count(jakso_alkupvm, jakso_loppupvm)

        new_kesto = round(new_kesto) if old_kesto != 0 else 0

        client.update_item(
            TableName=TABLE_NAME,
            Key={'hankkimistapa_id': item['hankkimistapa_id']},
            UpdateExpression='SET kesto_vanha = :kesto_vanha, kesto = :kesto_uusi',
            ExpressionAttributeValues={
                ':kesto_uusi': {'N': str(new_kesto)},
                ':kesto_vanha': {'N': str(old_kesto)}
            }
        )
        updated += 1

    print(updated)
```

</details>
