# Skriptit

Skriptit on lähtökohtaisesti suunniteltu helpottamaan QA-ympäristössä
testaamista sekä helpottamaan ajoittaista selvitystyötä. Hokseja poistavien tai
hoksin tietoja muuttavien skriptien suorittamiselle tuotantoympäristöstä ei
lähtökohtaisesti pitäisi olla tarvetta. Kuitenkin, jos skriptejä on joskus syytä
ajaa tuotantoympäristössä, skriptit tulisi laatia siten, että ennen
muokkaavan/poistavan operaation suorittamista kysytään käyttäjältä aina
varmistus. Tähän tarkoitukseen voidaan käyttää valmista skriptiä
`scripts/ask-confirmation`. Myös, koska skriptien toiminnallisuus nojautuu
asetettuihin ympäristömuuttujiin, on mahdollista että skriptejä ajetaan
vahingossa väärää ympäristöä vasten. Varmistuksen kysyminen käyttäjältä on
tarpeellista tästäkin syystä.

Asetusten
```bash
set -euo pipefail
```
asettaminen skriptin alussa on suositeltavaa, jotta skriptin suoritus lopetetaan
heti kun jonkin komennon exit-koodi on nollasta poikkeava.

## Skriptien suorittamista varten vaadittavat työkalut

Vaadittavat työkalut skriptien suorittamiseksi ovat
- [bash-komentotulkki](https://www.gnu.org/software/bash/)
- [POSIX-työkalut](https://pubs.opengroup.org/onlinepubs/9699919799/idx/utilities.html) (cat, grep, xargs, etc.)
- [jq](https://jqlang.github.io/jq/) (JSON-datan poimintaan, validointiin ja manipulointiin)

## Ympäristömuuttujien asettamien aktivaatioskriptillä

Aktivaatioskripti pyytää valitsemaan halutun ympäristön (esim. `dev`) ja
syöttämään kehittäjän ympäristöä vastaavan käyttäjätunnuksen ja salasanan.
Skripti asettaa joukon ympäristömuuttujia (joita monet muut skriptit
hyödyntävät), hakee valmiiksi CAS Ticket Granting Ticketin (TGT) sekä luo
Session Cookien virkailija-rajapinnan endpointtien käyttämistä varten.

Aktivaatioskriptiä ei ole tarkoitus suorittaa erillisessä aliprosessissa, vaan
skriptin komennot suoritetaan ("sourcetaan") käynnissä olevasta shellistä
komennolla:
```bash
$ . scripts/env/activate
```
Jos skripti suoritetaan onnistuneesti, konsoliin pitäisi tulostua kaikkine
vaiheineen jotakuinkin seuraavanlaiset rivit:
```bash
$ . scripts/env/activate
Which environment (prod/dev)? dev
Please provide your CAS credentials.
Username: ehoks_kehittaja
Password:
Fetching CAS Ticket Granting Ticket (TGT)... Success.
Refreshing session cookie... Success.
(dev) $
```
Samaan tapaan kuin Python `venv`-virtuaaliympäristön aktivaation yhteydessä,
skripti asettaa käyttäjän bash-promptiin (`PS1`) prefiksin, joka indikoi mikä
ympäristö on aktivoitu. eHOKSin aktivaatiskriptin tapauksessa lisätty prefiksi
on `(dev)` tai `(prod)`, riippuen mikä ympäristö on aktivoitu. On mahdollista,
että prefiksi ei näy, jos käyttäjän `.bashrc`-konfiguraatiossa on asetettu
`PROMPT_COMMAND`, joka muokkaa `PS1`-promptia ennen tämän näyttämistä
konsolissa.

## Ympäristömuuttujien siivoaminen `deactivate`-funktiolla

Aktivaatioskriptissa määritellään funktio `deactivate`, jota voidaan käyttää
siivoamaan skriptin asettamat ympäristömuuttujat. Komentorivillä riittää siis
ajaa
```bash
deactivate
```
jolloin bash-komentotulkin pitäisi olla ympäristömuuttujien osalta samassa
tilassa kuin ennen aktivaatioskriptin ajamista.

## eHOKS

### Hoksin hakeminen skriptillä `virkailija/get-hoks`

Hoksin koko tietorakenteen hakemiseksi virkailija-rajapintaa käyttäen tarvitaan
sekä `oppija_oid` että `hoks_id`.
```bash
hoks_id=44875; oppija_oid=$(scripts/virkailija/get-hoks-oppija $hoks_id); scripts/virkailija/get-hoks
$oppija_oid $hoks_id > "hoks_$hoks_id.json"
```
Em. komento tallentaisi siis hoksin JSON-tietorakenteen tiedostoon
`hoks_44875.json`. Molemmat kyselyt `scripts/virkailija/get-hoks-oppija` ja
`scripts/virkailija/get-hoks` voi halutessaan yhdistää yhteen skriptiin, jolloin
hoksin tietorakenteen hakemiseksi riittää pelkkä `hoks_id`:

### Uuden hoksin luominen skriptillä `virkailija/create-hoks`

Jos hoks on tallennettu tiedostoon `path/to/hoks.json`, onnistuu hoksin luominen
valittuun ympäristöön skriptillä:
```bash
scripts/virkailija/create-hoks path/to/hoks.json
```
Hoksin JSON-tietorakenne voidaan halutessa lukea myös standardisyötevirrasta (`stdin`). Esim.
```bash
cat path/to/hoks.json | scripts/virkailija/create-hoks
```
tai
```bash
scripts/virkailija/create-hoks <<EOL
{
  "ensikertainen-hyvaksyminen": "2023-12-05",
  "sahkoposti": "testikayttaja@testi.fi",
  "osaamisen-hankkimisen-tarve": true,
  "opiskeluoikeus-oid": "1.2.246.562.15.71095074820",
  "oppija-oid": "1.2.246.562.24.54450598189"
}
EOL
```

### Hoksin korvaaminen skriptillä `virkailija/replace-hoks`

`scripts/virkailija/replace-hoks` skripti toimii samalla periaatteella kuin
`scripts/virkailija/create-hoks` (kts. tämän toimintaperiaate yltä), mutta
hoksin luomisen sijaan korvaa olemassa olevan hoksin.

**Skriptille riittää antaa korvaavan hoksin tietorakenne**, sillä sekä
`hoks_id`:n että `oppija_oid`:n on löydyttävä tietorakenteesta, ja skripti
käyttää näitä tietoja hyväksi. Esim.
```bash
scripts/virkailija/replace-hoks <<EOL
{
  "id": 44875,
  "ensikertainen-hyvaksyminen": "2023-12-05",
  "sahkoposti": "testikayttaja@testi.fi",
  "osaamisen-hankkimisen-tarve": true,
  "opiskeluoikeus-oid": "1.2.246.562.15.71095074820",
  "oppija-oid": "1.2.246.562.24.54450598189"
}
EOL
```

### Hoksin päivittäminen skriptillä `virkailija/update-hoks`

Skripti hoksin päätason tietojen päivittämiseen (PATCH-metodilla). Toimii
samalla periaatteella kuin `scripts/virkailija/create-hoks` tai
`scripts/virkailija/replace-hoks`, eli hoksin päivitys-JSON-tietorakenteen voi
tarjota tiedostona tai standardisyötevirrasta (`stdin`). Hoksin ID:n on
löydyttävä skriptille tarjottavasta, päivitystä kuvaavasta
JSON-tietorakenteesta. Skripti hyödyntää tätä tietoa, eikä `hoks_id`:tä tarvitse
tarjota skriptille erikseen.
```bash
scripts/virkailija/update-hoks 1.2.246.562.24.54450598189 <<EOL
{
  "id": 44875,
  "osaamisen-saavuttamisen-pvm": "2023-12-07"
}
EOL
```



### Hoksin poistaminen pysyvästi skriptillä `virkailija/delete-hoks`

Hoksin voi poistaa `hoks_id`:n skriptille:
```bash
scripts/virkailija/delete-hoks 44875
```
Tämä poistaa hoksin **pysyvästi**, joten **skriptiä kannattaa käyttää varoen**.
Aktivoidusta ympäristöstä riippumatta, skripti pyytää käyttäjää varmistamaan
poistotoimenpiteen ennen se suoritetaan

## Koski

### Opiskeluoikeuden tietojen hakeminen
Olettaen, että kehittäjällä Koski-lukuoikudet, voidaan
`scripts/koski/get-opiskeluoikeus`-skriptin avulla hakea opiskeluoikeuden tiedot
Koskesta. Esimerkiksi, jos halutaan selvittää minkä tyyppiisä suorituksia
opiskeluoikeuteen kuuluu, voidaan skriptin ulostulo putkittaa `jq`-työkalulle:
```bash
scripts/koski/get-opiskeluoikeus 1.2.246.562.15.71095074820 | jq -r '.suoritukset[].tyyppi.koodiarvo'
```

## Organisaatiopalvelu

### Organisaation nimen hakemin OID:in perusteella

Organisaation suomenkielisen nimen voi hakea komennolla:
```bash
scripts/organisaatio/get-name 1.2.246.562.10.54425555 | jq -r '.[0].nimi.fi'
```
