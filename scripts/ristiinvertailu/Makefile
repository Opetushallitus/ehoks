SHELL := /bin/bash
DATE_NOW := $(shell date '+%Y-%m-%d')
ALKUPVM := 2024-11-14 # TODO: Change this!
TILAT = 'odottaa_kasittelya','kysely_muodostettu','vastaajatunnus_muodostettu'

# POSTGRES
DB_NAME := ehoks
DB_USERNAME := app
DB_HOST := localhost
DB_PORT := 55432

ifdef AWS_PROFILE
  ifeq ($(AWS_PROFILE), oph-prod)
    ENV_NAME := sade
  else
    ENV_NAME := pallero
  endif
  DB_PASSWORD != aws ssm get-parameter --profile $(AWS_PROFILE) \
  	--name /$(ENV_NAME)/postgresqls/ehoks/app-user-password \
  	--with-decryption | jq '.Parameter.Value'
endif

.PHONY: clean plot

all: plot

outputs:
	mkdir -p outputs

outputs/ddb_tep_kyselyt.json: outputs
	test -n "$(DDB_JAKSOTUNNUS_TABLE_NAME)" \
		# Set environment variable DDB_JAKSOTUNNUS_TABLE_NAME
	aws dynamodb scan \
		--table $(DDB_JAKSOTUNNUS_TABLE_NAME) \
		--profile $(AWS_PROFILE) \
		--filter-expression "jakso_loppupvm >= :alkupvm and jakso_loppupvm <= :currentpvm" \
		--expression-attribute-values '{":alkupvm": {"S": "$(ALKUPVM)"}, ":currentpvm": {"S": "$(DATE_NOW)"}}' \
		> $@

outputs/ddb_amis_kyselyt.json: outputs
	test -n "$(DDB_AMIS_TABLE_NAME)" \
		# Set environment variable DDB_AMIS_TABLE_NAME
	aws dynamodb scan \
		--table $(DDB_AMIS_TABLE_NAME) \
		--profile $(AWS_PROFILE) \
		--filter-expression "heratepvm >= :alkupvm and heratepvm <= :currentpvm" \
		--expression-attribute-values '{":alkupvm": {"S": "$(ALKUPVM)"}, ":currentpvm": {"S": "$(DATE_NOW)"}}' \
		> $@

outputs/ehoks_tep_kyselyt.csv:
	@mkdir -p outputs
	@PGPASSWORD=$(DB_PASSWORD) \
		psql -d $(DB_NAME) -h $(DB_HOST) -p $(DB_PORT) -U $(DB_USERNAME) \
		-f fetch_tep_heratteet.sql --csv \
		-v alkupvm="'$(ALKUPVM)'" \
		-v loppupvm="'$(DATE_NOW)'" \
		-v tilat="$(TILAT)" \
		> $@

outputs/ehoks_amis_kyselyt.csv:
	@mkdir -p outputs
	@PGPASSWORD=$(DB_PASSWORD) \
		psql -d $(DB_NAME) -h $(DB_HOST) -p $(DB_PORT) -U $(DB_USERNAME) \
		-f fetch_amis_kyselyt.sql --csv \
		-v alkupvm="'$(ALKUPVM)'" \
		-v loppupvm="'$(DATE_NOW)'" \
		-v tilat="$(TILAT)" \
		> $@

myenv:
	python -m venv myenv
	./myenv/bin/pip install matplotlib

outputs/amis_diff.json: outputs/ddb_amis_kyselyt.json \
	outputs/ehoks_amis_kyselyt.csv myenv
	./myenv/bin/python amis_diff.py $^

outputs/tep_diff.json: outputs/ddb_tep_kyselyt.json \
	outputs/ehoks_tep_kyselyt.csv myenv
	./myenv/bin/python tep_diff.py $^

plot: outputs/amis_diff.json outputs/tep_diff.json myenv
	./myenv/bin/python plot.py amis $(ALKUPVM) $(DATE_NOW)
	./myenv/bin/python plot.py tep $(ALKUPVM) $(DATE_NOW)

.PHONY: clean
clean:
	rm -rf outputs
