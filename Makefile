
stamps/db-image: $(wildcard scripts/postgres-docker/*)
	(cd scripts/postgres-docker && docker build -t ehoks-postgres .)
	touch $@

stamps/db-running: stamps/db-image
	docker run -d --rm --name ehoks-postgres -p 5432:5432 ehoks-postgres \
		> $@ || (rm $@ && false)

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

tags::
	ctags -R --exclude='*.min.js' .

.PHONY: psql
psql: stamps/db-running
	psql -h localhost -U postgres ehoks

.PHONY: test
test: stamps/db-schema
	lein test

.PHONY: stop
stop:
	docker rm -f ehoks-postgres
	-rm stamps/db-running

