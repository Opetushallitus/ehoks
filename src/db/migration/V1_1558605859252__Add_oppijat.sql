CREATE TABLE oppijat (
  oid VARCHAR(26) PRIMARY KEY,
  etunimi TEXT,
  sukunimi TEXT
);

CREATE TABLE opiskeluoikeudet (
  oid VARCHAR(26),
  oppija_oid VARCHAR(26) REFERENCES oppijat(oid),  
  oppilaitos_oid VARCHAR(26),
  koulutustoimija_oid VARCHAR(26),
  tutkinto TEXT,
  osaamisala TEXT,
  alku DATE,
  loppu DATE,
  PRIMARY KEY(oid, oppija_oid)
);