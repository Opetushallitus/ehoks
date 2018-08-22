# eHOKS

## Technologies

### Frontend

[Github Repository](https://github.com/Opetushallitus/ehoks-ui)

### Backend

+ [Clojure 1.9.0](https://clojure.org/)
+ [Compojure-api 2](https://github.com/metosin/compojure-api/)
+ [Leiningen](https://leiningen.org/)
+ [PostgreSQL 10.4](https://www.postgresql.org/) database
+ [HugSQL](https://www.hugsql.org/)
+ [Flyway](https://flywaydb.org/) database migrations
+ [clj-http](https://github.com/dakrone/clj-http) http requests with
integrations
+ [cheshire](https://github.com/dakrone/cheshire) JSON decoding/encoding
+ [environ](https://github.com/weavejester/environ) environment variables

## Quality assurance

[The Clojure Style Guidea](https://github.com/bbatsov/clojure-style-guide).

Repository has `.editorconfig` file for configuring your editor.

Static linters for backend can be run with command:

``` shell
lein checkall
```

It runs Kibit, Bikeshed, Eastwood, and cljfmt all at once. Every tool can also
be run individually:

``` shell
lein kibit
lein bikeshed
lein eastwood
lein cljfmt check
```

### More info

+ [kibit](https://github.com/jonase/kibit)
+ [lein-bikeshed](https://github.com/dakrone/lein-bikeshed)
+ [eastwood](https://github.com/jonase/eastwood)
+ [cljfmt](https://github.com/weavejester/cljfmt)

## Running application

``` shell
lein ring server-headless
```

Or in development mode (for example development CORS)

``` shell
lein with-profile dev ring server-headless
```

Or inside repl with file reload:

``` repl
user> (use 'oph.ehoks.dev-server)
user> (def server (start-server))
```

And shutting down:

``` repl
user> (.stop server)
```

### Configure

Default configure is located in `config/defaults.edn` file. Values, and only
those values, in defaults file can be overriden with environment variables.
Variables are imported with [environ](https://github.com/weavejester/environ)
so keys with underscore can is allowed.

## Running tests

``` shell
lein test
```

## Creating runnable JAR

```
lein do clean, ring uberjar
java -jar target/ehoks-backend.jar
```

## Integrations

Service | Documentation
--------|--------------
Opintohallintojärjestelmät |
AMOSAA |
ePerusteet | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/ePerusteet)
KOSKI | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Koski-palvelukortti)

## Links

+ [eHOKS Confluence](https://confluence.csc.fi/display/OPHPALV/eHOKS+-+hanke)
