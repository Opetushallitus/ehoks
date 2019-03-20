#!/usr/bin/env bash

psql -c "create database ehoks_test;" -U postgres
psql -d ehoks_test -f scripts/travis-scripts/init_travis_postgres.sql
