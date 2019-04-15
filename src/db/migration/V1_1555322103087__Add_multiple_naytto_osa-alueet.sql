CREATE TABLE koodisto_koodit(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  koodi_uri VARCHAR(256),
  koodi_versio INTEGER,
  naytto_id INTEGER
);

CREATE TABLE hankitun_osaamisen_nayton_osa_alueet(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  koodisto_koodi_id INTEGER REFERENCES koodisto_koodit(id),
  PRIMARY KEY(hankitun_osaamisen_naytto_id, koodisto_koodi_id)
);

INSERT INTO koodisto_koodit (naytto_id, koodi_uri, koodi_versio)
(SELECT id, osa_alue_koodi_uri, osa_alue_koodi_versio
   FROM hankitun_osaamisen_naytot);

INSERT INTO hankitun_osaamisen_nayton_osa_alueet
  (hankitun_osaamisen_naytto_id, koodisto_koodi_id)
  (SELECT naytto_id, id FROM koodisto_koodit);

-- Remove temporary column
ALTER TABLE koodisto_koodit
  DROP COLUMN naytto_id;

ALTER TABLE hankitun_osaamisen_naytot
  DROP COLUMN osa_alue_koodi_uri,
  DROP COLUMN osa_alue_koodi_versio;
