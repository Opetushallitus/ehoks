;; This buffer is for text that is not saved, and for Lisp evaluation.
;; To create a file, visit it with C-x C-f and enter text in its buffer.

# Schemojen luonti
- /src/oph/ehoks/hoks/schema.clj
- Opiskele ensimmäiseksi GET, PUT, POST ja PATCH sekä niiden erot, jos ei ole
  RESTful tuttu
- Etsi käytettävä schema, esim. PuuttuvaPaikallinenTutkinnonOsa
- `describe` on funktio, jolla voi määrittää schemalle helposti descriptionin
  ja jokaiselle avain-arvo-parille oman descriptionin.
  Eli sille annetaan merkkijono ja sen jälkeen 0..n määrä
  avain-tyyppi-selite-kolmikkoja.
- `schemat-tools/modify`-funktiolla voi muokata schemaa turvallisesti.
  Esimerkiksi avaimien poistaminen tai pakollisuuden poistaminen onnistuu
  helposti (kts. esimerkki)
- Luo POST, PUT ja PATCH schemat (kts. esimerkki)
  - Olen nimennyt schemat xLuonti, xPaivitys ja xKentanPaivitys
  - POST:ssa ei saa olla eid:tä
  - PATCH eli kentän päivityksessä pitää yleensä kaikki, paitsi eid, olla
    valinnaisia
  - PUT:ssa pitäisi olla samat, kuin POST:ssa, mutta lisäksi eid pakollisena
  - Kannattaa tarkistaa muutenkin, mitkä arvot on järkeviä olla missäkin
    metodissa mukana (metodi näissä on siis GET, PUT, POST, PATCH jne.)
- Huomaa tarkistaa, että perusschemassa (GET, ja muiden pohja) on eid
  pakollisena.

# Handler
- /src/oph/ehoks/hoks/handler.clj
- Lisää routet
  - Olen käyttänyt tässä private arvoa ja käyttänyt sitä hoks-root routen
    perässä
- PUT ja PATCH ei yleensä palauta mitään muuta, kuin OK (poikkeuksia toki on)
  - no-content
- POST palauttaa urin, joka osoittaa luotuun arvoon. Tämä täytyy toteuttaa
  myöhemmin. Väliaikaisesti palautetaan tyhjä merkkijono.

# Huomioita
- Helpottamaan devausta voi käyttää Leiningening auto plugaria generoimaan
  dokumentaatio automaattisesti
  - `lein auto gendoc`
- Itsellä on selaimessa Markdown-renderöintiplugari, niin md-filu näyttää
  suoraan GH:n tyyliseltä
- Jos käytät copy-pastea, niin tee triplacheck jokaiselle koodiriville.
  Varsinkin descriptioneihin saattaa helposti jäädä vanhaa tekstiä. Ja myös
  väärien schemojen muokkaus tekee varmasti hämmentäviä lopputuloksia.
- Kannattaa käyttää `git add filu.clj -p` ja käydä jokainen lisättävä rivi läpi
- Tarkista aina, että kaikki schemat näyttää myös dokumentaatiossa oikein
  - gendocissa on jonkin verran validointia, joten sielläkin jää virheitä kiinni
  - Varsinkin kenttien vaillinaisuus on tärkeää tarkistaa
  - Urliin voi antaa otsikon, johon automaattisesti hypätään (#otsikko)
- Tarkista aina, että swaggeriin tulee oikeat schemat ja descriptionin joka
  routelle
- Tee itse PR review. Eli käy PR vielä rivi riviltä läpi, ennen kuin mergeät.
- Resursseille täytyy lisätä relaatiot myöhemmin
  - [RESTful HATEOAS](https://en.wikipedia.org/wiki/HATEOAS)
