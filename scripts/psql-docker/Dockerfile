# Based on <https://hub.docker.com/_/postgres/>

FROM postgres:9.5

COPY ./init.sql /docker-entrypoint-initdb.d/
COPY ./configure.sh /docker-entrypoint-initdb/