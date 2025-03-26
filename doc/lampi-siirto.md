# eHOKSin tietojen vienti Lampeen

eHOKSin tiedot viedään Lampeen kerran vuorokaudessa. Siirto ajetaan ajastettuna
ECS Taskina, jonka image muodostetaan aina tarvittaessa manuaalisella
GitHub-ajolla (Create Lampi export runner image). Imagen muodostamisessa
käytetään `scripts/lampi-export`-hakemistossa olevia tiedostoja. GitHub-ajo
muodostaa imagen utility-tilin ECR:ään nimellä `ehoks-lampi-export`. ECS Taskin
infrakoodi on Opintopolun cloud infrassa `ehoks-export` stackissa.

Siirtoa varten eHOKSin tietokantaskeemaa muunnetaan hieman, jotta tietojen
jatkokäyttö Lammessa on yksinkertaisempaa. Skeeman muunnos tehdään
tietokantafunktioilla, jotka on määritetty eHOKSin tietokantamigraatioina.
Lampeen siirrettävä eHOKSin tietomalli on kuvattu
[Kehittäjien Wikissä](https://wiki.eduuni.fi/x/LPaAHg) (vaatii käyttöoikeudet).

Muulta osin tietojen vienti Lampeen noudattaa Lammen standarditapaa siirtää
tietoa (csv-tiedostoina). Lampeen liittyviä tietovirtoja on kuvattu yleisellä
tasolla [tietoalustaselvityksen yhteydessä](https://wiki.eduuni.fi/spaces/OPHPALV/pages/285257658/Tietoalustaselvitys#Tietoalustaselvitys-Tietovirrat).

## Monitorointi

`TODO` Siirron monitorointi on vielä työn alla. Minimitoteutuksena on tarkoitus
luoda automaattihälytys, joka tarkistaa kerran vuorokaudessa onko manifest.json
S3:ssa yli vuorokauden vanha.
