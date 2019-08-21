CREATE TABLE tutkinnon_osa_shares (
  uuid UUID PRIMARY KEY,
  voimassaolo_alku DATE NOT NULL,
  voimassaolo_loppu DATE NOT NULL,
  hoks_id INTEGER REFERENCES hoksit(id),
  koodisto_koodi VARCHAR(256) NOT NULL,
  tyyppi VARCHAR(32) NOT NULL
)