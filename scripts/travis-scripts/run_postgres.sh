#!/usr/bin/env bash
cd scripts/postgres-docker
docker build -t ehoks-postgres .
docker run --rm --name ehoks-postgres -p 5433:5432 ehoks-postgres
