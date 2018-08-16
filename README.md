# eHOKS

## Teknologiat

### Käyttöliittymä

### Backend

+ [Clojure 1.9.0](https://clojure.org/)
+ [Compojure-api 1.1.1](https://github.com/metosin/compojure-api/tree/1.1.x)
+ [Leiningen](https://leiningen.org/)
+ [PostgreSQL 10.4](https://www.postgresql.org/) tietokantana
+ [Flyway](https://flywaydb.org/) tietokantamigraatioille

## Laadunvarmistus

Koodin tyylissä tavoitellaan
[The Clojure Style Guidea](https://github.com/bbatsov/clojure-style-guide).

Repossa on `.editorconfig`-tiedosto, jota kannattaa hyödyntää.

Backendin staattiset tarkistustyökalut voi ajaa kerralla seuraavalla komennolla:

``` shell
lein checkall
```

Tämä ajaa lein check, kibit, eastwood ja bikeshed -työkalut yhdellä kerralla.
Työkaluja voi ajaa myös yksittäin:

``` shell
lein check
lein kibit
lein eastwood
lein bikeshed
```

### Lisätietoja

+ [kibit](https://github.com/jonase/kibit)
+ [eastwood](https://github.com/jonase/eastwood)
+ [lein-bikeshed](https://github.com/dakrone/lein-bikeshed)

## Integraatiot

Palvelu | Dokumentaatio
--------|--------------
Opintohallintojärjestelmät |
AMOSAA |
ePerusteet | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/ePerusteet)
KOSKI | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Koski-palvelukortti)

## Linkkejä

+ [eHOKS Confluence](https://confluence.csc.fi/display/OPHPALV/eHOKS+-+hanke)
