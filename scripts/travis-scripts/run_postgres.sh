psql -c "create database ehoks_test;" -U postgres
psql -d ehoks_test -f init_travis_postgres.sql
