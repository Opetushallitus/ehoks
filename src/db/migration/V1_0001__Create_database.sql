CREATE TABLE hoksit (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  oppija_oid VARCHAR(64) NOT NULL,
  opiskeluoikeus_oid VARCHAR(64) NOT NULL,
  urasuunnitelma_koodi_arvo VARCHAR(256),
  urasuunnitelma_koodi_uri VARCHAR(256),
  urasuunnitelma_koodi_versio BIGINT,
  luonut TEXT,
  luotu TIMESTAMP WITH TIME ZONE,
  paivittanyt TEXT,
  paivitetty TIMESTAMP WITH TIME ZONE,
  hyvaksynyt TEXT,
  hyvaksytty TIMESTAMP WITH TIME ZONE);

CREATE TABLE yhteiset_tutkinnon_osat (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  koulutuksen_jarjestaja_oid VARCHAR(64),
  tunniste_koodi_arvo VARCHAR(256) NOT NULL,
  tunniste_koodi_uri VARCHAR(256) NOT NULL,
  tunniste_koodi_versio VARCHAR(256) NOT NULL);

CREATE TABLE yhteisen_tutkinnon_osan_osa_alueet (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  yhteinen_tutkinnon_osa_id
    INTEGER NOT NULL REFERENCES yhteiset_tutkinnon_osat(id),
  tunniste_koodi_arvo VARCHAR(256) NOT NULL,
  tunniste_koodi_uri VARCHAR(256) NOT NULL,
  tunniste_koodi_versio VARCHAR(256) NOT NULL
  poikkeaminen TEXT,
  tarvittava_opetus TEXT NOT NULL);

CREATE TABLE puuttuvat_yhteiset_tutkinnon_osat (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  hoks_id INTEGER NOT NULL REFERENCES hoksit(id));

CREATE TABLE olemassa_olevat_yhteiset_tutkinnon_osat (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  hoks_id INTEGER NOT NULL REFERENCES hoksit(id));

CREATE TABLE osaamisen_hankkimistavat (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  alku TIMESTAMP WITH TIME ZONE NOT NULL,
  loppu TIMESTAMP WITH TIME ZONE NOT NULL,
  tunniste_koodi_arvo VARCHAR(256) NOT NULL,
  tunniste_koodi_uri VARCHAR(256) NOT NULL,
  tunniste_koodi_versio VARCHAR(256) NOT NULL);

CREATE TABLE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  yhteisen_tutkinnon_osan_osa_alue_id
    INTEGER NOT NULL REFERENCES yhteisen_tutkinnon_osan_osa_alueet(id),
  osaamisen_hankkimistapa_id
    INTEGER NOT NULL REFERENCES osaamisen_hankkimistavat(id),
  primary key
    (yhteisen_tutkinnon_osan_osa_alue_id, osaamisen_hankkimistapa_id));

CREATE TABLE henkilot (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  nimi TEXT,
  organisaatio_nimi TEXT,
  organisaatio_y_tunnus VARCHAR(9),
  rooli TEXT);

CREATE TABLE tyopaikalla_hankittavat_osaamiset (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  erityisen_tuen_alku TIMESTAMP WITH TIME ZONE NOT NULL,
  erityisen_tuen_loppu TIMESTAMP WITH TIME ZONE NOT NULL,
  erityinen_tuki BOOLEAN NOT NULL,
  ohjaus_ja_tuki BOOLEAN NOT NULL);

CREATE TABLE osaamisen_hankkimistapojen_tyopaikalla_hankittavat_osaamiset (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  osaamisen_hankkimistapa_id
    INTEGER NOT NULL REFERENCES osaamisen_hankkimistavat(id),
  tyopaikalla_hankittava_osaaminen_id
    INTEGER NOT NULL REFERENCES tyopaikalla_hankittavat_osaamiset(id),
  primary key
    (osaamisen_hankkimistapa_id, tyopaikalla_hankittava_osaaminen_id));

CREATE TABLE tyopaikalla_hankittavan_osaamisen_henkilot (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  tyopaikalla_hankittava_osaaminen_id
    INTEGER NOT NULL REFERENCES tyopaikalla_hankittavat_osaamiset(id),
  henkilo_id
    INTEGER NOT NULL REFERENCES henkilot(id),
  rooli VARCHAR(24) NOT NULL,
  primary key (
    tyopaikalla_hankittava_osaaminen_id,
    henkilo_id,
    rooli));

CREATE TABLE tyopaikalla_hankittavan_osaamisen_keskeiset_tyotehtavat (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  tyopaikalla_hankittava_osaaminen_id
    INTEGER NOT NULL REFERENCES tyopaikalla_hankittavat_osaamiset(id)
  tehtava TEXT));

CREATE TABLE oppimisymparistot (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  tarkenne_koodi_arvo VARCHAR(256) NOT NULL,
  tarkenne_koodi_uri VARCHAR(256) NOT NULL,
  tarkenne_koodi_versio VARCHAR(256) NOT NULL,
  selite TEXT NOT NULL,
  ohjaus_ja_tuki BOOLEAN NOT NULL,
  erityinen_tuki BOOLEAN NOT NULL,
  erityinen_tuki_alku TIMESTAMP WITH TIME ZONE NOT NULL,
  erityinen_tuki_loppu TIMESTAMP WITH TIME ZONE NOT NULL);

CREATE TABLE osaamisen_hankkimistavan_oppimisymparistot (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  osaamisen_hankkimistavat_id
    INTEGER NOT NULL REFERENCES osaamisen_hankkimistavat(id),
  oppimisymparisto_id
    INTEGER NOT NULL REFERENCES oppimisymparistot(id),
  primary key (osaamisen_hankkimistavat_id, oppimisymparisto_id));

CREATE TABLE hankitun_osaamisen_naytot (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  jarjestaja_nimi TEXT,
  jarjestaja_oid VARCHAR(64),
  nayttoymparisto_nimi TEXT,
  nayttoymparisto_y_tunnus VARCHAR(9),
  kuvaus TEXT,
  alku TIMESTAMP WITH TIME ZONE DEFAULT NOT NULL,
  loppu TIMESTAMP WITH TIME ZONE DEFAULT NOT NULL,
  sisalto TEXT);

CREATE TABLE hankitun_yhteisen_tutkinnon_osan_osaamisen_naytot (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  hankitun_osaamisen_naytto_id
    INTEGER NOT NULL REFERENCES hankitun_osaamisen_naytot(id));

CREATE TABLE hankitun_yhteisen_tutkinnon_osan_osaamisen_nayton_osaamistavoitteet (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  hankitun_yhteisen_tutkinnon_osan_osaamisen_naytto_id
    INTEGER NOT NULL REFERENCES
      hankitun_yhteisen_tutkinnon_osan_osaamisen_naytot(id));

CREATE TABLE paikallisen_tutkinnon_osat (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  kuvaus TEXT NOT NULL,
  amosaa_id INTEGER,
  tarvittava_opetus TEXT NOT NULL,
  nimi TEXT NOT NULL,
  laajuus INTEGER NOT NULL,
  koulutuksen_jarjestajan_oid VARCHAR(64));

CREATE TABLE paikallisen_tutkinnon_osan_osaamisen_hankkimistavat (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  paikallinen_tutkinnon_osa_id
    INTEGER NOT NULL REFERENCES paikallisen_tutkinnon_osat(id),
  osaamisen_hankkimistapa_id
    INTEGER NOT NULL REFERENCES osaamisen_hankkimistavat(id),
  primary key
    (paikallinen_tutkinnon_osa_id, osaamisen_hankkimistapa_id));

CREATE TABLE puuttuvat_paikallisen_tutkinnon_osat (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  hoks_id INTEGER NOT NULL REFERENCES hoksit(id),
  paikallinen_tutkinnon_osa_id
    INTEGER NOT NULL REFERENCES paikallisen_tutkinnon_osat(id));

CREATE TABLE paikallisen_tutkinnon_osan_osaamisen_naytot (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  hankitun_osaamisen_naytto_id
    INTEGER NOT NULL REFERENCES hankitun_osaamisen_naytot(id));

CREATE TABLE paikallisen_tutkinnon_osan_osaamisen_nayton_ammattitaitovaatimukset (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER NOT NULL DEFAULT 0,
  vaatimus TEXT,
  paikallisen_tutkinnon_osan_osaamisen_naytto_id
  INTEGER NOT NULL REFERENCES
    paikallisen_tutkinnon_osan_osaamisen_naytot(id));

CREATE TABLE paikallisen_tutkinnon_osan_osaamisen_nayton_arvioijat (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  paikallisen_tutkinnon_osan_osaamisen_naytto_id
    INTEGER NOT NULL REFERENCES paikallisen_tutkinnon_osan_osaamisen_naytot(id),
  arvioija_id
  INTEGER NOT NULL REFERENCES arvioijat(id),
  PRIMARY KEY (paikallisen_tutkinnon_osan_osaamisen_naytto_id, arvioija_id));
