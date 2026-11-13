# Järjestelmätunnuksella testaaminen

Kun tulee tarve tehdä uusia hokseja/muokata aikaisempia rajapinnan kautta testiympäristössä:
1.	Luo järjestelmätunnus testiympäristössä
        Opintopolku > Käytönhallinta > Järjestelmätunnukset > Järjestelmätunnuksen luonti
        Järjestelmätunnulle tulee antaa käyttöoikeudet Koskeen ja luoda opiskeluoikeuksia. Opetushallituksen organisaation
        alla olevat PROD KOSKI-tallentaja ja KOSKI-oph-eHOKS pitäisi riittää.
        Myös halutulle oppilaitokselle tulee asettaa oikeudet, PROD KOSKI-tallentaja, KOSKI-oppilaitos-tallentaja


2.	Luodun järjestelmätunnuksen käyttäjätunnuksella ja salasanalla voit kutsua tgt-tiketin create-cas-tgt.sh -skriptillä

``` shell
sh ./scripts/create-cas-tgt.sh username password
```

Tämä tallentaa tgt-ticketin sisältämän urlin cas-tgt-url.txt -tiedostoon.

Ennen kuin voidaan lähettää kutsuja rajapintaan, pitää luoda opiskeluoikeus saman organisaation alle, jolle järjestelmätunnuksella on käyttöoiekudet asetettu aikaisemmin.

Luodaan uusi opinto-oikeus Koskessa käyttöliittymän kautta
    (Onnistuu myös komentoriviltä)
1.	Opintopolku > Rekisterit ja tiedonsiirrot > Koski
      Jos et näe Koski -vaihtoehtoa Rekisterit ja tiedonsiirrot alla, sinulle tulee lisätä oikeudet Koskeen mainittu ylempänä. Kun Koski -oikeudet ovat laitettu, voi tgt-ticketin joutua hakemaan uudelleen.
2.	Valitse saman organisaation alla oleva opiskelija ja ”Lisää opiskeluoikeus”
3.	Tämän opiskelijan oppijanumerolla ja opiskeluoikeuden oidilla saat tehtyä nyt hoksin rajapintakutsulla

Rajapintakutsuja voi tehdä curl-with-cas.sh -scriptillä esimerkiksi:

``` shell
sh ./scripts/curl-with-cas.sh -i \
-X POST \
-H 'Content-Type: application/json' \
-d ' {
"osaamisen-hankkimisen-tarve": true,
"ensikertainen-hyvaksyminen": "2026-05-21",
"sahkoposti": "oppija@emal.fi",
"opiskeluoikeus-oid": "1.2.246.562.15.32707173343",
"oppija-oid": "1.2.246.562.24.231940740260"} ' \
'https://virkailija.testiopintopolku.fi/ehoks-virkailija-backend/api/v1/hoks'
```
Huom. Hoksin voi luoda myös käyttöliittymästä, jolloin tarvitsee virkalilijatunnuksen.
Tämä ohje on intergaatiotunnusten rajapinnan testaamista varten.
