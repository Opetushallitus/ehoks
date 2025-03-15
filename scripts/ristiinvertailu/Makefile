SHELL := /bin/bash
DATE_NOW := $(shell date '+%Y-%m-%d')
ALKUPVM := 2024-11-13 # TODO: Change this!
OUTPUTS_DIR := outputs

ifndef AWS_PROFILE
$(error AWS_PROFILE is not set)
endif

ifndef DDB_JAKSOTUNNUS_TABLE_NAME
$(error DDB_JAKSOTUNNUS_TABLE_NAME is not set)
endif

ifndef DDB_AMIS_TABLE_NAME
$(error DDB_AMIS_TABLE_NAME is not set)
endif


ifeq ($(AWS_PROFILE), oph-prod)
	ENV_NAME := sade
else
	ENV_NAME := pallero
endif

# POSTGRES
DB_NAME := ehoks
DB_USERNAME := app
DB_PASSWORD != aws ssm get-parameter --profile $(AWS_PROFILE) \
	--name /$(ENV_NAME)/postgresqls/ehoks/app-user-password \
	--with-decryption | jq '.Parameter.Value'
DB_HOST := localhost
DB_PORT := 55432

.PHONY: clean

all: $(OUTPUTS_DIR)/amis_diff.json $(OUTPUTS_DIR)/tep_diff.json

$(OUTPUTS_DIR)/ddb_tep_kyselyt.json:
	@mkdir -p $(OUTPUTS_DIR)
	aws dynamodb scan \
		--table $(DDB_JAKSOTUNNUS_TABLE_NAME) \
		--profile $(AWS_PROFILE) \
		--filter-expression "jakso_loppupvm >= :alkupvm and jakso_loppupvm <= :currentpvm" \
		--expression-attribute-values '{":alkupvm": {"S": "$(ALKUPVM)"}, ":currentpvm": {"S": "$(DATE_NOW)"}}' \
		> $@

$(OUTPUTS_DIR)/ddb_amis_kyselyt.json:
	@mkdir -p $(OUTPUTS_DIR)
	aws dynamodb scan \
		--table $(DDB_AMIS_TABLE_NAME) \
		--profile $(AWS_PROFILE) \
		--filter-expression "heratepvm >= :alkupvm and heratepvm <= :currentpvm" \
		--expression-attribute-values '{":alkupvm": {"S": "$(ALKUPVM)"}, ":currentpvm": {"S": "$(DATE_NOW)"}}' \
		> $@

$(OUTPUTS_DIR)/ehoks_tep_kyselyt.csv:
	@mkdir -p $(OUTPUTS_DIR)
	@PGPASSWORD=$(DB_PASSWORD) \
		psql -d $(DB_NAME) -h $(DB_HOST) -p $(DB_PORT) -U $(DB_USERNAME) \
		-f fetch_tep_heratteet.sql --csv \
		-v alkupvm="'$(ALKUPVM)'" -v loppupvm="'$(DATE_NOW)'" \
		> $@

$(OUTPUTS_DIR)/ehoks_amis_kyselyt.csv:
	@mkdir -p $(OUTPUTS_DIR)
	@PGPASSWORD=$(DB_PASSWORD) \
		psql -d $(DB_NAME) -h $(DB_HOST) -p $(DB_PORT) -U $(DB_USERNAME) \
		-f fetch_amis_kyselyt.sql --csv \
		-v alkupvm="'$(ALKUPVM)'" -v loppupvm="'$(DATE_NOW)'" \
		> $@

$(OUTPUTS_DIR)/amis_diff.json: $(OUTPUTS_DIR)/ddb_amis_kyselyt.json \
	$(OUTPUTS_DIR)/ehoks_amis_kyselyt.csv
	@mkdir -p $(OUTPUTS_DIR)
	python amis_diff.py $^

$(OUTPUTS_DIR)/tep_diff.json: $(OUTPUTS_DIR)/ddb_tep_kyselyt.json \
	$(OUTPUTS_DIR)/ehoks_tep_kyselyt.csv
	@mkdir -p $(OUTPUTS_DIR)
	python tep_diff.py $^

clean:
	rm -rf $(OUTPUTS_DIR)
