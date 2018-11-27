# eHOKS

## Technologies

### Frontend

[Github Repository](https://github.com/Opetushallitus/ehoks-ui)

### Backend

+ [Clojure 1.9.0](https://clojure.org/)
+ [Clojure.test](https://clojure.github.io/clojure/clojure.test-api.html)
+ [Compojure-api 2](https://github.com/metosin/compojure-api/)
+ [Leiningen](https://leiningen.org/)
+ [PostgreSQL 9.5](https://www.postgresql.org/docs/9.5/static/index.html) as a
database
+ [HugSQL](https://www.hugsql.org/)
+ [Flyway](https://flywaydb.org/) for database migrations
+ [clj-http](https://github.com/dakrone/clj-http) for http requests with
integrations
+ [Cheshire](https://github.com/dakrone/cheshire) for JSON decoding/encoding
+ [Environ](https://github.com/weavejester/environ) for environment variables
+ [Redis](https://redis.io/) for session storage
+ [Redis Client](https://github.com/ptaoussanis/carmine)
+ [Logback](https://logback.qos.ch/)
+ [tools.logging](https://github.com/clojure/tools.logging)

#### RESTful API
Backend does its best to follow
[RESTful](https://en.wikipedia.org/wiki/Representational_state_transfer)
guidelines. For example resources URI's of collections, create and update are
with trailing slash and items (representation) are withoute one. Every response
has `meta` and `data` keys.

Keys are following Clojure notation. Because of this all keys are with dash
instead of form of camelCase.

## Quality assurance

[The Clojure Style Guidea](https://github.com/bbatsov/clojure-style-guide).

Repository has `.editorconfig` file for configuring your editor.

### Running tests

Running tests once:

``` shell
lein test
```

Or automatically on change:

``` shell
lein auto test
```

Or with test coverage (cloverage):

``` shell
lein with-profile test cloverage
```

### Linters

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

## Development

### Running application

Run in development mode:

``` shell
lein run
```

Run in production mode:

``` shell
lein with-profile -dev run
```

Or inside REPL with file reload, starting with `lein repl`, then:

``` repl
user> (use 'oph.ehoks.dev-server)
user> (def server (start-server))
```

Or with custom config:

``` repl
user> (use 'oph.ehoks.dev-server)
user> (def server (start-server "config/custom.edn"))
```

And shutting down:

``` repl
user> (.stop server)
```

### Tests

You can mock external API calls for tests. There is http client with redefinable
get and post functions which you can override. This works only with `test`
profile.

### Database

For database there is proper Docker script in `scripts/psql-docker` folder. Use
this only in development environment.

Build Docker image:

``` shell
cd scripts/postgres-docker
docker build -t ehoks-postgres:9.5 .
```

Riun Docker image in a container:

``` shell
docker run --rm --name ehoks-postgres -p 5432:5432 --volume ~/path/to/ehoks-postgres-data:/var/lib/postgresql/data ehoks-postgres:9.5
```

### Redis

Redis is being used as a session storage.

For local development use you can use Docker script in `scripts/redis-docker`
folder.

Build Docker image:

``` shell
cd scripts/redis-docker
docker build -t ehoks-redis .
```

Run Docker image in a container:

``` shell
docker run --rm --name ehoks-redis -p 6379:6379 --volume ~/path/to/ehoks-redis-data:/data ehoks-redis
```

Or you can always skip runnign Redis with leaving `REDIS_URL` environment
variable or `:redis-url` cofigure option nil.

### Development routes

Application supports creating JSON-files for returning dummy data. Place files
in `resource/dev/dev-routes` folder. Files are matched with translating uri to
filename. For example `/hello/world` translates to `hello_world.json`. For
security reasons only files in `dev-routes` folders are read. Dummy data routes
works only when running development server.

## Configuration

Default configuration file is `config/default.edn`. You may override
these values by creating your own config file and supplying path to the
file either via environment variable `CONFIG`, JVM system property
`config` or as a development server parameter.

Config files are being merged as custom config overrides default values. So you
can use some default values and some custom values.

Merged configuration is being validated on load.

### CAS authentication

Application uses CAS authentication with external APIs. Fill out CAS credentials
and client sub system code before using external APIS. Application has mock APIs
for local development.

## Standalone jar

Create self-contained jar that includes all the dependencies:

```
lein uberjar
```

Run:

``` shell
java -jar target/ehoks-standalone.jar
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
