
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
	ctags -R --exclude='*.min.js' .

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

