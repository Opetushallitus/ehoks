CREATE TABLE hoksit(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  luotu TIMESTAMP WITH TIME ZONE,
  eid VARCHAR(36),
  sahkoposti TEXT,
  ensikertainen_hyvaksyminen DATE,
  hyvaksytty TIMESTAMP WITH TIME ZONE,
  urasuunnitelma_koodi_uri VARCHAR(256),
  urasuunnitelma_koodi_versio INTEGER,
  opiskeluoikeus_oid VARCHAR(26),
  laatija_nimi TEXT,
  versio INTEGER,
  paivitetty TIMESTAMP WITH TIME ZONE,
  paivittaja_nimi TEXT,
  oppija_oid VARCHAR(26),
  hyvaksyja_nimi TEXT
);

CREATE TABLE koulutuksen_jarjestaja_arvioijat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  nimi TEXT,
  oppilaitos_oid VARCHAR(26)
);

CREATE TABLE todennettu_arviointi_lisatiedot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  lahetetty_arvioitavaksi DATE
);

CREATE TABLE todennettu_arviointi_arvioijat(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  todennettu_arviointi_lisatiedot_id INTEGER REFERENCES todennettu_arviointi_lisatiedot(id),
  koulutuksen_jarjestaja_arvioija_id INTEGER REFERENCES koulutuksen_jarjestaja_arvioijat(id),
  PRIMARY KEY(todennettu_arviointi_lisatiedot_id, koulutuksen_jarjestaja_arvioija_id)
);

CREATE TABLE nayttoymparistot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  nimi TEXT,
  y_tunnus TEXT,
  kuvaus TEXT
);

CREATE TABLE tyoelama_arvioijat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  nimi TEXT,
  organisaatio_nimi TEXT,
  organisaatio_y_tunnus TEXT
);

CREATE TABLE hankitun_osaamisen_naytot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  jarjestaja_oppilaitos_oid VARCHAR(26),
  osa_alue_koodi_uri VARCHAR(26),
  osa_alue_koodi_versio INTEGER,
  nayttoymparisto_id INTEGER REFERENCES nayttoymparistot(id),
  alku DATE,
  loppu DATE
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
  koulutuksen_jarjestaja_arvioija_id INTEGER REFERENCES koulutuksen_jarjestaja_arvioijat(id),
  PRIMARY KEY(hankitun_osaamisen_naytto_id, koulutuksen_jarjestaja_arvioija_id)
);

CREATE TABLE hankitun_osaamisen_tyotehtavat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  tyotehtava TEXT
);

CREATE TABLE olemassa_olevat_ammatilliset_tutkinnon_osat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hoks_id INTEGER REFERENCES hoksit(id),
  tutkinnon_osa_koodi_uri VARCHAR(256),
  tutkinnon_osa_koodi_versio INTEGER,
  koulutuksen_jarjestaja_oid VARCHAR(26),
  valittu_todentamisen_prosessi_koodi_uri VARCHAR(256),
  valittu_todentamisen_prosessi_koodi_versio INTEGER,
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
  hoks_id INTEGER REFERENCES hoksit(id),
  laajuus INTEGER,
  nimi TEXT,
  tavoitteet_ja_sisallot TEXT,
  amosaa_tunniste TEXT,
  koulutuksen_jarjestaja_oid VARCHAR(26),
  vaatimuksista_tai_tavoitteista_poikkeaminen TEXT
);

CREATE TABLE puuttuvan_paikallisen_tutkinnon_osan_hankitun_osaamisen_naytto(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  puuttuva_paikallinen_tutkinnon_osa_id INTEGER REFERENCES puuttuvat_paikalliset_tutkinnon_osat(id),
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  PRIMARY KEY(puuttuva_paikallinen_tutkinnon_osa_id, hankitun_osaamisen_naytto_id)
);

CREATE TABLE tyopaikalla_hankittavat_osaamiset(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
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
  tyopaikalla_hankittava_osaaminen_id INTEGER REFERENCES tyopaikalla_hankittavat_osaamiset(id),
  tyotehtava TEXT
);

CREATE TABLE osaamisen_hankkimistavat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  jarjestajan_edustaja_nimi TEXT,
  jarjestajan_edustaja_rooli TEXT,
  jarjestajan_edustaja_oppilaitos_oid VARCHAR(26),
  ajanjakson_tarkenne TEXT,
  osaamisen_hankkimistapa_koodi_uri VARCHAR(256),
  osaamisen_hankkimistapa_koodi_versio INTEGER,
  tyopaikalla_hankittava_osaaminen_id INTEGER REFERENCES tyopaikalla_hankittavat_osaamiset(id),
  hankkijan_edustaja_nimi TEXT,
  hankkijan_edustaja_rooli TEXT,
  hankkijan_edustaja_oppilaitos_oid VARCHAR(26),
  alku DATE,
  loppu DATE
);

CREATE TABLE muut_oppimisymparistot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  oppimisymparisto_koodi_uri TEXT,
  oppimisymparisto_koodi_versio INTEGER,
  selite TEXT,
  lisatiedot BOOLEAN,
  osaamisen_hankkimistapa_id INTEGER REFERENCES osaamisen_hankkimistavat(id)
);

CREATE TABLE puuttuvan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  puuttuva_paikallinen_tutkinnon_osa_id INTEGER REFERENCES puuttuvat_paikalliset_tutkinnon_osat(id),
  osaamisen_hankkimistapa_id INTEGER REFERENCES osaamisen_hankkimistavat(id),
  PRIMARY KEY(puuttuva_paikallinen_tutkinnon_osa_id, osaamisen_hankkimistapa_id)
);

CREATE TABLE olemassa_olevat_yhteiset_tutkinnon_osat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hoks_id INTEGER REFERENCES hoksit(id),
  tutkinnon_osa_koodi_uri VARCHAR(256),
  tutkinnon_osa_koodi_versio INTEGER,
  koulutuksen_jarjestaja_oid VARCHAR(26),
  valittu_todentamisen_prosessi_koodi_uri VARCHAR(256),
  valittu_todentamisen_prosessi_koodi_versio INTEGER,
  lahetetty_arvioitavaksi DATE
);

CREATE TABLE olemassa_olevan_yhteisen_tutkinnon_osan_hankitun_osaamisen_naytto(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  olemassa_oleva_yhteinen_tutkinnon_osa_id INTEGER REFERENCES olemassa_olevat_yhteiset_tutkinnon_osat(id),
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  PRIMARY KEY(olemassa_oleva_yhteinen_tutkinnon_osa_id, hankitun_osaamisen_naytto_id)
);

CREATE TABLE olemassa_olevan_yhteisen_tutkinnon_osan_arvioijat(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  olemassa_oleva_yhteinen_tutkinnon_osa_id INTEGER REFERENCES olemassa_olevat_yhteiset_tutkinnon_osat(id),
  koulutuksen_jarjestaja_arvioija_id INTEGER REFERENCES koulutuksen_jarjestaja_arvioijat(id),
  PRIMARY KEY(olemassa_oleva_yhteinen_tutkinnon_osa_id, koulutuksen_jarjestaja_arvioija_id)
);

CREATE TABLE olemassa_olevat_yto_osa_alueet(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  olemassa_oleava_yhteinen_tutkinnon_osa_id INTEGER REFERENCES olemassa_olevat_yhteiset_tutkinnon_osat(id),
  osa_alue_koodi_uri VARCHAR(256),
  osa_alue_koodi_versio INTEGER,
  koulutuksen_jarjestaja_oid VARCHAR(26),
  vaatimuksista_tai_tavoitteista_poikkeaminen TEXT,
  valittu_todentamisen_prosessi_koodi_uri VARCHAR(256),
  valittu_todentamisen_prosessi_koodi_versio INTEGER
);

CREATE TABLE olemassa_olevan_yto_osa_alueen_hankitun_osaamisen_naytto(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  olemassa_oleva_yto_osa_alue_id INTEGER REFERENCES olemassa_olevat_yto_osa_alueet(id),
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  PRIMARY KEY(olemassa_oleva_yto_osa_alue_id, hankitun_osaamisen_naytto_id)
);

CREATE TABLE puuttuvat_ammatilliset_tutkinnon_osat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hoks_id INTEGER REFERENCES hoksit(id),
  tutkinnon_osa_koodi_uri VARCHAR(256),
  tutkinnon_osa_koodi_versio INTEGER,
  vaatimuksista_tai_tavoitteista_poikkeaminen TEXT,
  koulutuksen_jarjestaja_oid VARCHAR(26)
);

CREATE TABLE puuttuvan_ammatillisen_tutkinnon_osan_hankitun_osaamisen_naytto(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  puuttuva_ammatillinen_tutkinnon_osa_id INTEGER REFERENCES puuttuvat_ammatilliset_tutkinnon_osat(id),
  hankitun_osaamisen_naytto_id INTEGER REFERENCES hankitun_osaamisen_naytot(id),
  PRIMARY KEY(puuttuva_ammatillinen_tutkinnon_osa_id, hankitun_osaamisen_naytto_id)
);

CREATE TABLE puuttuvan_ammatillisen_tutkinnon_osan_osaamisen_hankkimistavat(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  puuttuva_ammatillinen_tutkinnon_osa_id INTEGER REFERENCES puuttuvat_ammatilliset_tutkinnon_osat(id),
  osaamisen_hankkimistapa_id INTEGER REFERENCES osaamisen_hankkimistavat(id),
  PRIMARY KEY(puuttuva_ammatillinen_tutkinnon_osa_id, osaamisen_hankkimistapa_id)
);

CREATE TABLE opiskeluvalmiuksia_tukevat_opinnot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hoks_id INTEGER REFERENCES hoksit(id),
  nimi TEXT,
  kuvaus TEXT,
  alku DATE,
  loppu DATE
);

CREATE TABLE puuttuvat_yhteiset_tutkinnon_osat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hoks_id INTEGER REFERENCES hoksit(id),
  tutkinnon_osa_koodi_uri VARCHAR(256),
  tutkinnon_osa_koodi_versio INTEGER,
  koulutuksen_jarjestaja_oid VARCHAR(26)
);

CREATE TABLE yhteisen_tutkinnon_osan_osa_alueet(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  yhteinen_tutkinnon_osa_id INTEGER REFERENCES puuttuvat_yhteiset_tutkinnon_osat(id),
  osa_alue_koodi_uri VARCHAR(256),
  osa_alue_koodi_versio INTEGER,
  vaatimuksista_tai_tavoitteista_poikkeaminen TEXT
);

CREATE TABLE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat(
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  yhteisen_tutkinnon_osan_osa_alue_id INTEGER REFERENCES yhteisen_tutkinnon_osan_osa_alueet(id),
  osaamisen_hankkimistapa_id INTEGER REFERENCES osaamisen_hankkimistavat(id),
  PRIMARY KEY(yhteisen_tutkinnon_osan_osa_alue_id, osaamisen_hankkimistapa_id)
);

CREATE TABLE hankitun_yto_osaamisen_naytot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  yhteisen_tutkinnon_osan_osa_alue_id INTEGER REFERENCES yhteisen_tutkinnon_osan_osa_alueet(id),
  jarjestaja_oppilaitos_oid VARCHAR(26),
  nayttoymparisto_nimi TEXT,
  nayttoymparisto_y_tunnus TEXT,
  nayttoymparisto_kuvaus TEXT,
  osa_alue_koodi_uri VARCHAR(26),
  osa_alue_koodi_versio INTEGER,
  alku DATE,
  loppu DATE
);

CREATE TABLE hankitun_yto_osaamisen_nayton_osaamistavoitteet(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hankitun_yto_osaamisen_naytto_id INTEGER REFERENCES hankitun_yto_osaamisen_naytot(id),
  osaamistavoite TEXT
);

CREATE TABLE hankitun_yto_osaamisen_nayton_tyoelama_arvioija (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hankitun_yto_osaamisen_naytto_id INTEGER REFERENCES hankitun_yto_osaamisen_naytot(id),
  tyoelama_arvioija_id INTEGER REFERENCES tyoelama_arvioijat(id),
  PRIMARY KEY(hankitun_yto_osaamisen_naytto_id, tyoelama_arvioija_id)
);

CREATE TABLE hankitun_yto_osaamisen_nayton_koulutuksen_jarjestaja_arvioija (
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hankitun_yto_osaamisen_naytto_id INTEGER REFERENCES tyoelama_arvioijat(id),
  koulutuksen_jarjestaja_arvioija_id INTEGER REFERENCES koulutuksen_jarjestaja_arvioijat(id),
  PRIMARY KEY(hankitun_yto_osaamisen_naytto_id, koulutuksen_jarjestaja_arvioija_id)
);
