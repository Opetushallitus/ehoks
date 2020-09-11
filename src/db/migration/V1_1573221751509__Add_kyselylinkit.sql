CREATE TABLE kyselylinkit(
  kyselylinkki VARCHAR(64) PRIMARY KEY,
  hoks_id SERIAL REFERENCES hoksit(id) ON DELETE CASCADE,
  tyyppi VARCHAR(64),
  oppija_oid VARCHAR(256),
  alkupvm DATE
);