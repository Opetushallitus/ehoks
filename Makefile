
stamps/db-image: $(wildcard scripts/postgres-docker/*)
	(cd scripts/postgres-docker && docker build -t ehoks-postgres .)
	touch $@

stamps/db-running: stamps/db-image
	docker ps --format '{{.Names}}' | grep -qx 'ehoks-postgres' \
	|| docker run -d --rm --name ehoks-postgres \
		-p 5432:5432 ehoks-postgres > $@ || (rm $@ && false)
	until echo pingping | nc localhost 5432; do \
		echo "Waiting for database to come up..."; \
		sleep 1; \
	done
	touch $@

stamps/db-schema: stamps/db-running
	lein dbmigrate
	touch $@

stamps/local-ddb-running:
	docker ps --format '{{.Names}}' | grep -qx 'ehoks-dynamodb' \
	|| docker run -d --rm --name ehoks-dynamodb -p 18000:8000 \
		docker.io/amazon/dynamodb-local > $@ || (rm $@ && false)
	touch $@

DDB_TABLES = amisherate jaksotunnus tep-nippu tpk-nippu

myenv:
	python3 -m venv myenv

stamps/install-awscli: myenv
	./myenv/bin/pip install awscli
	touch $@

stamps/local-ddb-schema: $(DDB_TABLES:%=stamps/local-ddb-schema-%)
	touch $@

stamps/local-ddb-schema-%: resources/dev/demo-data/%-schema.json \
		stamps/local-ddb-running stamps/install-awscli
	AWS_ACCESS_KEY_ID=foo AWS_SECRET_ACCESS_KEY=bar \
	./myenv/bin/aws dynamodb create-table --region eu-west-1 \
	--endpoint http://localhost:18000 --cli-input-json "$$(cat $<)"
	touch $@

MAVEN_REPO = $(HOME)/.m2/repository
SS_JAR = $(MAVEN_REPO)/net/sourceforge/schemaspy/schemaspy/5.0.0/schemaspy-5.0.0.jar
PG_JAR = $(MAVEN_REPO)/org/postgresql/postgresql/42.2.12/postgresql-42.2.12.jar

schemaDoc: stamps/db-schema
	lein with-profile schemaspy deps
	java -jar $(SS_JAR) -t pgsql -host localhost -db ehoks -u postgres \
		-s public -dp $(PG_JAR) -o $@ \
	|| (rm -r $@ && false)

stamps/server-running: stamps/db-schema
	lein with-profiles +dev run ehoks-virkailija \
		> ehoks-virkailija.log 2>&1 & echo $$! > $@
	until curl localhost:3000; do \
		echo "Waiting for backend to come up..."; \
		sleep 2; \
	done
	touch $@

stamps/example-data: stamps/server-running
	sh ./scripts/create-curl-session.sh
	sh ./scripts/upload-hoks.sh resources/dev/demo-data/hoksit.json
	touch $@

tags::
	ctags -R --exclude='*.min.js' --exclude='json-schema-viewer' \
		--exclude='myenv' .

pom.xml::
	lein pom
	sed -i -e '1s/^/<!-- Snyk uses pom.xml for analysis. -->\n/' \
		-e "1s/^/<!-- I'm autogenerated with 'make pom.xml'. -->\n/" \
		pom.xml

.PHONY: check-pom-xml
check-pom-xml: pom.xml
	if git diff -I '<tag>' -I '<?xml' -I '<project' -I leiningen pom.xml \
		| grep .; then \
	echo 'pom.xml is not up to date.'; \
	echo 'Run `make pom.xml` and commit the result.'; \
	exit 1; \
	fi

.PHONY: psql
psql: stamps/db-running
	psql -h localhost -U postgres ehoks

.PHONY: test
test:
	lein test

.PHONY: stop
stop:
	-test -f stamps/db-running && docker rm -f ehoks-postgres
	-rm stamps/db-running
	-test -f stamps/server-running && kill $$(cat stamps/server-running)
	-rm stamps/server-running
	-test -f stamps/local-ddb-running && docker rm -f ehoks-dynamodb
	-rm stamps/local-ddb-running

