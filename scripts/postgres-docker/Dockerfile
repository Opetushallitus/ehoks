# Based on <https://hub.docker.com/_/postgres/>

FROM docker.io/library/postgres:12.19

COPY ./configure-database.sh /docker-entrypoint-initdb.d/
COPY ./configure-postgres.sh /docker-entrypoint-initdb.d/
