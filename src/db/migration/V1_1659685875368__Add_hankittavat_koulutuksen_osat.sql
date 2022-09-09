CREATE TABLE hankittavat_koulutuksen_osat
(
  id                         SERIAL PRIMARY KEY,
  created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at                 TIMESTAMP WITH TIME ZONE,
  hoks_id                    INTEGER REFERENCES hoksit (id) ON DELETE CASCADE,
  koulutuksen_osa_koodi_uri  VARCHAR(256),
  koulutuksen_osa_koodi_versio INTEGER,
  alku DATE,
  loppu DATE,
  laajuus DECIMAL
);
