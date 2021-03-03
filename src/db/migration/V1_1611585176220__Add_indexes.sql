-- Table kyselylinkit
CREATE INDEX kyselylinkit_oppija_oid_alkupvm_idx
    ON kyselylinkit(oppija_oid, alkupvm);

-- Table hoksit
CREATE INDEX hoksit_deleted_at_eid_idx
    ON hoksit(deleted_at, eid);

CREATE INDEX hoksit_opiskeluoikeus_oid_idx
    ON hoksit(opiskeluoikeus_oid);

CREATE INDEX hoksit_oppija_oid_eid_opiskeluoikeus_oid_idx
    ON hoksit(oppija_oid, eid, opiskeluoikeus_oid);

-- Table opiskeluoikeudet
CREATE INDEX opiskeluoikeudet_oid_hankintakoulutus_oo_oid_idx
    ON opiskeluoikeudet(oid, hankintakoulutus_opiskeluoikeus_oid);

CREATE INDEX opiskeluoikeudet_kt_oid_oppilaitos_oid_oid_oppija_oid_idx
    ON opiskeluoikeudet(koulutustoimija_oid, oppilaitos_oid, oid, oppija_oid);

CREATE INDEX opiskeluoikeudet_tutkinto_nimi_idx
    ON opiskeluoikeudet(tutkinto_nimi);

-- Table oppijat
CREATE INDEX oppijat_oid_nimi_idx
    ON oppijat(oid, nimi);

-- Table sessions
CREATE INDEX sessions_session_key_idx
    ON sessions(session_key);

-- Table user_settings
CREATE INDEX user_settings_user_oid_idx
    ON user_settings(user_oid);

-- Table shared_modules
CREATE INDEX shared_modules_idx
    ON shared_modules(share_id, hoks_eid);

CREATE INDEX shared_modules_shared_module_uuid_idx
    ON shared_modules(shared_module_uuid);

-- Table osaamisen_osoittamisen_sisallot
CREATE INDEX osaamisen_osoittamisen_sisallot_osaamisen_osoittaminen_id_idx
    ON osaamisen_osoittamisen_sisallot(osaamisen_osoittaminen_id);

-- Table osaamisen_osoittamisen_yksilolliset_kriteerit
CREATE INDEX osaamisen_osoittamisen_yksilolliset_kriteerit_idx
    ON osaamisen_osoittamisen_yksilolliset_kriteerit(osaamisen_osoittaminen_id);

-- Table tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
CREATE INDEX tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat_idx
    ON tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat(tyopaikalla_jarjestettava_koulutus_id);

-- Table aiemmin_hankitut_yto_osa_alueet
CREATE INDEX aiemmin_hankitut_yto_osa_alueet_idx
    ON aiemmin_hankitut_yto_osa_alueet(aiemmin_hankittu_yhteinen_tutkinnon_osa_id);

-- Table yhteisen_tutkinnon_osan_osa_alueet
CREATE INDEX yhteisen_tutkinnon_osan_osa_alueet_idx
    ON yhteisen_tutkinnon_osan_osa_alueet(yhteinen_tutkinnon_osa_id);

-- Table aiemmin_hankitun_ammat_tutkinnon_osan_naytto
CREATE INDEX aiemmin_hankitun_ammat_tutkinnon_osan_naytto_idx
    ON aiemmin_hankitun_ammat_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_ammat_tutkinnon_osa_id);

-- Table koodisto_koodit
CREATE INDEX koodisto_koodit_deleted_at_idx
    ON koodisto_koodit(deleted_at);

-- Table osaamisen_osoittamisen_osa_alueet
CREATE INDEX osaamisen_osoittamisen_osa_alueet_idx
    ON osaamisen_osoittamisen_osa_alueet(osaamisen_osoittaminen_id, koodisto_koodi_id);

-- Table aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
CREATE INDEX aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto_idx
    ON aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_paikallinen_tutkinnon_osa_id);

-- Table hankittavan_paikallisen_tutkinnon_osan_naytto
CREATE INDEX hankittavan_paikallisen_tutkinnon_osan_naytto_idx
    ON hankittavan_paikallisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, hankittava_paikallinen_tutkinnon_osa_id);

-- Table osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
CREATE INDEX osaam_osoitt_koul_jarj_arv_koul_jarj_osaamisen_arvioija_idx
    ON osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija(koulutuksen_jarjestaja_osaamisen_arvioija_id, osaamisen_osoittaminen_id);

-- Table todennettu_arviointi_arvioijat
CREATE INDEX todennettu_arviointi_arvioijat_idx
    ON todennettu_arviointi_arvioijat(koulutuksen_jarjestaja_osaamisen_arvioija_id, todennettu_arviointi_lisatiedot_id);

-- Table tyoelama_osaamisen_arvioijat
CREATE INDEX tyoelama_osaamisen_arvioijat_deleted_at_idx
    ON tyoelama_osaamisen_arvioijat(deleted_at);

-- Table osaamisen_osoittamisen_tyoelama_arvioija
CREATE INDEX osaam_osoitt_tyoelama_arvioija_idx
    ON osaamisen_osoittamisen_tyoelama_arvioija(tyoelama_arvioija_id, osaamisen_osoittaminen_id);

-- Table osaamisen_hankkimistavat
CREATE INDEX osaamisen_hankkimistavat_deleted_at_idx
    ON osaamisen_hankkimistavat(deleted_at);

CREATE INDEX osaamisen_hankkimistavat_idx
    ON osaamisen_hankkimistavat(osaamisen_hankkimistapa_koodi_uri, loppu);

-- Table hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
CREATE INDEX hankittavan_paik_tutkinnon_osan_osaamisen_hankkimistavat_idx
    ON hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat(osaamisen_hankkimistapa_id, hankittava_paikallinen_tutkinnon_osa_id);

-- Table aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
CREATE INDEX aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto_idx
    ON aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_yhteinen_tutkinnon_osa_id, deleted_at);

-- Table aiemmin_hankitun_yto_osa_alueen_naytto
CREATE INDEX aiemmin_hankitun_yto_osa_alueen_naytto_idx
    ON aiemmin_hankitun_yto_osa_alueen_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_yto_osa_alue_id, deleted_at);

-- Table hankittavan_ammat_tutkinnon_osan_naytto
CREATE INDEX hank_ammat_tutkinnon_osan_naytto_idx
    ON hankittavan_ammat_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, hankittava_ammat_tutkinnon_osa_id, deleted_at);

-- Table hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
CREATE INDEX hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat_idx
    ON hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat(hankittava_ammat_tutkinnon_osa_id, osaamisen_hankkimistapa_id, deleted_at);

-- Table yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
CREATE INDEX yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat(osaamisen_hankkimistapa_id, yhteisen_tutkinnon_osan_osa_alue_id, deleted_at);

-- Table yhteisen_tutkinnon_osan_osa_alueen_naytot
CREATE INDEX yht_tutk_osan_osa_alueen_naytot_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_naytot(osaamisen_osoittaminen_id, yhteisen_tutkinnon_osan_osa_alue_id, deleted_at);

-- Table hankittavaat_amm_tutkinnon_osat
CREATE INDEX hankittavat_ammat_tutkinnon_osat_idx
    ON hankittavat_ammat_tutkinnon_osat(hoks_id, deleted_at);

-- Table hankittavaat_paikalliset_tutkinnon_osat
CREATE INDEX hankittavat_paikalliset_tutkinnon_osat_idx
    ON hankittavat_paikalliset_tutkinnon_osat(hoks_id, deleted_at);

-- Table hankittavat_yhteiset_tutkinnon_osat
CREATE INDEX hankittavat_yhteiset_tutkinnon_osat_idx
    ON hankittavat_yhteiset_tutkinnon_osat(hoks_id, deleted_at);
