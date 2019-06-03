CREATE TABLE oppijat (
  oid VARCHAR(26) PRIMARY KEY,
  nimi TEXT NOT NULL DEFAULT ''
);

CREATE TABLE opiskeluoikeudet (
  oid VARCHAR(26),
  oppija_oid VARCHAR(26) REFERENCES oppijat(oid),  
  oppilaitos_oid VARCHAR(26),
  koulutustoimija_oid VARCHAR(26),
  tutkinto TEXT NOT NULL DEFAULT '',
  osaamisala TEXT NOT NULL DEFAULT '',
  PRIMARY KEY(oid, oppija_oid)
);