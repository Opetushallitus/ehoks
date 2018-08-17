# eHOKS

## Technologies

### Frontend

[Github Repository](https://github.com/Opetushallitus/ehoks-ui)

### Backend

+ [Clojure 1.9.0](https://clojure.org/)
+ [Compojure-api 1.1.1](https://github.com/metosin/compojure-api/tree/1.1.x)
+ [Leiningen](https://leiningen.org/)
+ [PostgreSQL 10.4](https://www.postgresql.org/) database
+ [Flyway](https://flywaydb.org/) database migrations

## Quality assurance

[The Clojure Style Guidea](https://github.com/bbatsov/clojure-style-guide).

Repository has `.editorconfig` file for configuring your editor.

Static linters for backend can be run with command:

``` shell
lein checkall
```

It runs Kibit, Eastwood, Bikeshed and cljfmt all at once. Every tool can also be
run individually:

``` shell
lein kibit
lein eastwood
lein bikeshed
lein cljfmtcheck
```

## Running application

`lein ring server`

## Running tests

`lein test`

## Creating runnable JAR

```
lein do clean, ring uberjar
java -jar target/ehoks-backend.jar
```

### More info

+ [kibit](https://github.com/jonase/kibit)
+ [eastwood](https://github.com/jonase/eastwood)
+ [lein-bikeshed](https://github.com/dakrone/lein-bikeshed)
+ [cljfmt](https://github.com/weavejester/cljfmt)

## Integrations

Service | Documentation
--------|--------------
Opintohallintojärjestelmät |
AMOSAA |
ePerusteet | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/ePerusteet)
KOSKI | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Koski-palvelukortti)

## Links

+ [eHOKS Confluence](https://confluence.csc.fi/display/OPHPALV/eHOKS+-+hanke)
