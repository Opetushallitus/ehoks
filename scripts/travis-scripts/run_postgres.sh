#!/usr/bin/env bash
cd scripts/postgres-docker
docker build -t ehoks-postgres .
docker run --rm --name ehoks-postgres -p 5432:5432 -d ehoks-postgres
