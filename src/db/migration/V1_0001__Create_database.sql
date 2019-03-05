CREATE TABLE hoksit(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  luotu TIMESTAMP WITH TIME ZONE,
  sahkoposti TEXT,
  ensikertainen_hyvaksyminen TIMESTAMP WITH TIME ZONE,
  hyvaksytty TIMESTAMP WITH TIME ZONE,
  urasuunnitelma_koodi_uri VARCHAR(256),
  opiskeluoikeus_oid VARCHAR(26),
  laatija_nimi TEXT,
  versio INTEGER,
  paivitetty TIMESTAMP WITH TIME ZONE,
  paivittaja TEXT,
  oppija_oid VARCHAR(26),
  hyvaksyja TEXT
);

CREATE TABLE koulutuksenjarjestaja_arvioijat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  nimi TEXT,
  oppilaitos_oid VARCHAR(26)
);

CREATE TABLE todennettu_arviointi_lisatiedot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  lahetetty_arvioitavaksi TIMESTAMP WITH TIME ZONE
);

CREATE TABLE todennettu_arviointi_arvioijat(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  todennettu_arviointi_lisatiedot_id INTEGER REFERENCES todennettu_arviointi_lisatiedot(id),
  koulutuksenjarjestaja_arvioija_id INTEGER REFERENCES koulutuksenjarjestaja_arvioijat(id),
  PRIMARY KEY(todennettu_arviointi_lisatiedot_id, koulutuksenjarjestaja_arvioija_id)
);

CREATE TABLE nayttoymparistot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  nimi TEXT,
  y_tunnus TEXT,
  kuvaus TEXT
);

CREATE TABLE tyoelama_arvioijat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  nimi TEXT,
  organisaatio_nimi TEXT,
  organisaatio_y_tunnus TEXT
);

CREATE TABLE hankitun_osaamisen_naytot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  jarjestaja_oppilaitos_oid VARCHAR(26),
  yto_osa_alue_koodi_uri VARCHAR(26),
  nayttoymparisto_id INTEGER REFERENCES nayttoymparistot(id),
  alku TIMESTAMP WITH TIME ZONE,
  loppu TIMESTAMP WITH TIME ZONE
);

CREATE TABLE hankitun_osaamisen_nayton_tyoelama_arvioija (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  tyoelama_arvioija_id INTEGER REFERENCES tyoelama_arvioijat(id),
  PRIMARY KEY(hankitun_osaamisen_naytto_id, tyoelama_arvioija_id)
);

CREATE TABLE hankitun_osaamisen_nayton_koulutuksen_jarjestaja_arvioija (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  koulutuksen_jarjestaja_arviojat_id INTEGER REFERENCES koulutuksenjarjestaja_arvioijat(id),
  PRIMARY KEY(hankitun_osaamisen_naytto_id, koulutuksen_jarjestaja_arviojat_id)
);

CREATE TABLE olemassa_olevat_ammatilliset_tutkinnon_osat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  hoks_id INTEGER REFERENCES hoksit(id),
  tutkinnon_osa_koodi_uri VARCHAR(256),
  koulutuksen_jarjestaja_oid VARCHAR(26),
  valittu_todentamisen_prosessi_koodi_uri VARCHAR(256),
  tarkentavat_tiedot_arvioija INTEGER REFERENCES todennettu_arviointi_lisatiedot(id)
);

CREATE TABLE olemassa_olevan_ammatillisen_tutkinnon_osan_hankitun_osaamisen_naytto(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  olemassa_oleva_ammatillinen_tutkinnon_osa_id INTEGER REFERENCES olemassa_olevat_ammatilliset_tutkinnon_osat(id),
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  PRIMARY KEY(olemassa_oleva_ammatillinen_tutkinnon_osa_id, hankitun_osaamisen_naytto_id)
);

CREATE TABLE olemassa_olevat_paikalliset_tutkinnon_osat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  hoks_id INTEGER REFERENCES hoksit(id),
  laajuus INTEGER,
  nimi TEXT,
  tavoitteet_ja_sisallot TEXT,
  amosaa_tunniste TEXT,
  koulutuksen_jarjestaja_oid VARCHAR(26),
  vaatimuksista_tai_tavoitteista_poikkeaminen TEXT
);

CREATE TABLE puuttuvat_paikalliset_tutkinnon_osat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  hoks_id INTEGER REFERENCES hoksit(id),
  laajuus INTEGER,
  nimi TEXT,
  tavoitteet_ja_sisallot TEXT,
  amosaa_tunniste TEXT,
  hankitun_osaamisen_naytto INTEGER REFERENCES hankitun_osaamisen_naytto_hankitun_paikallisen_osaamisen_naytto(id),
  osaamisen_hankkimistavat INTEGER REFERENCES osaamisen_hankkimistavat_osaamisen_hankkimistapa(id),
  koulutuksen_jarjestaja_oid VARCHAR(26),
  vaatimuksista_tai_tavoitteista_poikkeaminen TEXT
);

CREATE TABLE puuttuvan_paikallisen_tutkinnon_osan_hankitun_osaamisen_naytto(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  puuttuva_paikallinen_tutkinnon_osa_id INTEGER REFERENCES puuttuvat_paikalliset_tutkinnon_osat(id)
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  PRIMARY KEY(puuttuva_paikallinen_tutkinnon_osa_id, hankitun_osaamisen_naytto_id)
);

CREATE TABLE tyopaikalla_hankittavat_osaamiset(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  vastuullinen_ohjaaja_nimi TEXT,
  vastuullinen_ohjaaja_sahkoposti TEXT,
  tyopaikan_nimi TEXT,
  tyopaikan_y_tunnus TEXT,
  lisatiedot BOOLEAN
);

CREATE TABLE tyopaikalla_hankittavat_osaamisen_henkilot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  tyopaikalla_hankittava_osaaminen_id INTEGER REFERENCES tyopaikalla_hankittavat_osaamiset(id),
  organisaatio_nimi TEXT,
  organisaatio_y_tunnus TEXT,
  nimi TEXT,
  rooli TEXT
);

CREATE TABLE tyopaikalla_hankittavat_osaamisen_tyotehtavat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  tyopaikalla_hankittava_osaaminen_id INTEGER REFERENCES tyopaikalla_hankittavat_osaamiset(id),
  tyotehtava TEXT
);

CREATE TABLE osaamisen_hankkimistavat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  jarjestajan_edustaja_nimi TEXT,
  jarjestajan_edustaja_rooli TEXT,
  jarjestajan_edustaja_oppilaitos_oid VARCHAR(26),
  ajanjakson_tarkenne TEXT,
  osamisen_hankkimistapa_koodi_uri VARCHAR(256),
  hankkijan_edustaja_nimi TEXT,
  hankkijan_edustaja_rooli TEXT,
  hankkijan_edustaja_oppilaitos_oid VARCHAR(26),
  alku TIMESTAMP WITH TIME ZONE,
  loppu TIMESTAMP WITH TIME ZONE
);

CREATE TABLE muut_oppimisymparistot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  version INTEGER DEFAULT 0,
  oppimisymparisto_koodi_uri TEXT,
  selite TEXT,
  lisatiedot BOOLEAN,
  osaamisen_hankkimistapa_id INTEGER REFERENCES osaamisen_hankkimistavat(id)
);

CREATE TABLE puuttuvan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  puuttuva_paikallinen_tutkinnon_osa_id INTEGER REFERENCES puuttuvat_paikalliset_tutkinnon_osat(id)
  osaamisen_hankkimistapa_id INTEGER REFERENCES osaamisen_hankkimistavat(id),
  PRIMARY KEY(puuttuva_paikallinen_tutkinnon_osa_id, osaamisen_hankkimistapa_id)
);
