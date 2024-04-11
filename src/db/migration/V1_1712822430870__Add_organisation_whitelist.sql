CREATE TABLE IF NOT EXISTS organisaatio_whitelist (
  oid TEXT PRIMARY KEY,
  kayttoonottopvm DATE NOT NULL,
  nimi TEXT NOT NULL
);
