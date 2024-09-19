#!/usr/bin/env bash
cd scripts/postgres-docker
docker build -t ehoks-postgres .
docker run --rm --name ehoks-postgres -p 5433:5432 -d \
  -e POSTGRES_HOST_AUTH_METHOD=trust ehoks-postgres
