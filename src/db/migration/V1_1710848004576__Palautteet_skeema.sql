CREATE TYPE heratelahteet AS ENUM (
  'ehoks_update',
  'koski_update',
  'niputus'
);

CREATE TYPE kyselytyypit AS ENUM (
  'aloittaneet',
  'valmistuneet',
  'osia_suorittaneet',
  'tyopaikkajakson_suorittaneet',
  'tpo_nippu',
  'tpk_nippu'
);

CREATE TYPE palautetilat AS ENUM (
  'ei_laheteta',
  'kysely_muodostettu',
  'lahetetty',
  'lahetys_epaonnistunut',
  'niputettu',
  'odottaa_kasittelya',
  'tpk_niputettu',
  'vastaajatunnus_muodostettu',
  'vastattu',
  'vastausaika_loppunut'
);

CREATE TABLE IF NOT EXISTS palautteet (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  herate_source heratelahteet NOT NULL,
  heratepvm DATE,
  kyselytyyppi kyselytyypit NOT NULL,
  tila palautetilat NOT NULL,
  kyselylinkki VARCHAR(64),
  arvo_tunniste TEXT,
  voimassa_alkupvm DATE,
  voimassa_loppupvm DATE,
  jakson_yksiloiva_tunniste VARCHAR(256),
  hoks_id INTEGER NOT NULL,
  nippu_id INTEGER,
  koulutustoimija TEXT,
  hankintakoulutuksen_toteuttaja TEXT,
  toimipiste_oid TEXT,
  suorituskieli CHAR(2),
  tutkintotunnus INTEGER,
  tutkintonimike TEXT,
  FOREIGN KEY (hoks_id) REFERENCES hoksit (id) ON DELETE NO ACTION,
  FOREIGN KEY (nippu_id) REFERENCES palautteet (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS palautteet_uniq_check ON palautteet (hoks_id, kyselytyyppi, jakson_yksiloiva_tunniste, deleted_at);

CREATE TYPE tapahtumatyypit AS ENUM (
  'arvo_luonti',
  'niputus',
  'tpk_niputus'
);

CREATE TABLE IF NOT EXISTS palaute_tapahtumat (
  id SERIAL PRIMARY KEY,
  palaute_id INTEGER NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  vanha_tila palautetilat NOT NULL,
  uusi_tila palautetilat NOT NULL,
  tyyppi tapahtumatyypit NOT NULL,
  syy TEXT,
  lisatiedot JSONB,
  FOREIGN KEY (palaute_id) REFERENCES palautteet (id) ON DELETE CASCADE
);

CREATE TYPE viestityypit AS ENUM (
  'email',
  'sms',
  'email_muistutus_1',
  'sms_muistutus_1',
  'email_muistutus_2',
  'sms_muistutus_2'
);

CREATE TYPE lahetystilat AS ENUM (
  'odottaa_lahetysta',
  'lahetetty',
  'lahetys_epaonnistunut'
);

CREATE TABLE IF NOT EXISTS palaute_viestit (
  id SERIAL PRIMARY KEY,
  palaute_id INTEGER NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  ulkoinen_tunniste TEXT,
  viestityyppi viestityypit NOT NULL,
  tila lahetystilat NOT NULL,
  vastaanottaja TEXT NOT NULL,
  FOREIGN KEY (palaute_id) REFERENCES palautteet (id) ON DELETE CASCADE
);
