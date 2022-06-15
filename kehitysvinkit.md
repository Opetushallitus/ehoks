# ehoks ja ehoks-ui kehitysvinkit

### Miten pääsen ehoksin tietokantaan?

`cd cloud-base`

`tools/ssh.sh <ympäristö>`

Sitten remote-ympäristöstä:

`connect-to-db.sh ehoks <app|readonly>`


Voit hakea salasanoja ajamalla tämän komennon cloud-base:ssa:

`aws/config.py <ympäristö> get-secrets --path=postgresqls/ehoks -p /aws/environment/<ympäristö>`


Ainakin toinen komento täytyy ajaa virtualenvin sisällä.


### Miten haen ehoks-logeja komentoriviltä?

Käynnistä virtualenv, `cd cloud-base`, ja aja:

`tools/logs/get.sh <ympäristö> <aika (esim. 30m)> app-ehoks-<virkailja|oppija|virkailija-ui|oppija-ui>`


### Miten asennan ehoks tai ehoks-ui ympäristöön?

Käynnistä virtualenv, `cd cloud-base`, ja aja:

`aws/deploy.sh <ympäristö> ehoks-<virkailija|oppija|virkailija-ui|oppija-ui> deploy`

Valitse sitten build ja kirjoita `y`.

Asennus kestää 6-7 minuuttia alusta loppuun. Jos saat viestin, että MFA token
vanhenee alle 6-7 minuutin päästä, kannattaa odottaa sitä, että se vanhenee, ja
sitten yrittää uudestaan. Jos token vanhenee asennuksen aikana, voi joskus
joutua poistamaan changesetin manuaalisesti ja aloittamaan uudestaan.

Tosi hidas asennus viittaa todennäköisesti siihen, että tietokantamigraatiot
ovat menneet sekaisin, eli siihen, että migraatiot tehdään eri järjestyksessä
QA:ssa ja tuotannossa. Jos tämä tapahtuu tuotannossa, sinun täytyy muokata sinun
koodi niin, että se kunnioittaa olemassa olevaa migraatiojärjestystä
tuotannossa. Jos se tapahtuu QA:ssa, täytyy korjata järjestys QA:n tietokannassa
(ohjeet ehoks:in README:ssa).

ÄLÄ KORJAA MIGRAATIOJÄRJESTYSTÄ TUOTANNOSSA, SILLÄ MENETÄT TIETOJA!


Jos asennat tuotantoon, kannattaa ensin ilmoittaa asennuksesta osoitteeseen
`oph_kriittiset_tiedotukset@postit.csc.fi`, ja lisätä rivi wiki-sivuun:
`https://wiki.eduuni.fi/pages/viewpage.action?spaceKey=OPHSS&title=Asennukset+ja+release+notes+2022`.


### Miten poistan changesetin epäonnistuneen asennuksen jälkeen?

Käynnistä virtualenv, `cd cloud-base`, ja aja:

`aws/cloudformation.py <ympäristö> services delete-change-set -s <palvelu>`

(`palvelu` = `ehoks-virkailija|ehoks-oppija|ehoks-virkailija-ui|ehoks-oppija-ui`


### Miten tutkinnon osien tietokantahakukoodi toimii?

Tehokkuuden takia olemme luoneet queryjä, jotka tarjoavat joitakin perinteisen
ORM-järjestelmän tuomia hyötyjä ilman oikeaa ORM-kirjastoa. Yleinen periaate on
se, että useiden SQL-queryjen sijaan tehdään isompia queryjä joineilla, joista
objektit erotetaan koodissa.
