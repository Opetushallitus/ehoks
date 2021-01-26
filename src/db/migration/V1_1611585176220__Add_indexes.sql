-- Table hoksit
CREATE INDEX hoksit_eid_idx
    ON hoksit(eid);

CREATE INDEX hoksit_oppija_oid_idx
    ON hoksit(oppija_oid);

CREATE INDEX hoksit_opiskeluoikeus_oid_idx
    ON hoksit(opiskeluoikeus_oid);

-- Table opiskeluoikeudet
CREATE INDEX opiskeluoikeudet_hankintakoulutus_opiskeluoikeus_oid_idx
    ON opiskeluoikeudet(hankintakoulutus_opiskeluoikeus_oid);

CREATE INDEX opiskeluoikeudet_koulutustoimija_oid_idx
    ON opiskeluoikeudet(koulutustoimija_oid);

CREATE INDEX opiskeluoikeudet_tutkinto_nimi_idx
    ON opiskeluoikeudet(tutkinto_nimi);

CREATE INDEX opiskeluoikeudet_oppilaitos_oid_idx
    ON opiskeluoikeudet(oppilaitos_oid);

CREATE INDEX opiskeluoikeudet_oppija_oid_idx
    ON opiskeluoikeudet(oppija_oid);

CREATE INDEX opiskeluoikeudet_oid_idx
    ON opiskeluoikeudet(oid);

-- Table kyselylinkit
CREATE INDEX kyselylinkit_oppija_oid_idx
    ON kyselylinkit(oppija_oid);

CREATE INDEX kyselylinkit_alkupvm_idx
    ON kyselylinkit(alkupvm);

-- Table oppijat
CREATE INDEX oppijat_oid_idx
    ON oppijat(oid);

-- Table sessions
CREATE INDEX sessions_session_key_idx
    ON sessions(session_key);

-- Table user_settings
CREATE INDEX user_settings_user_oid_idx
    ON user_settings(user_oid);

-- Table shared_modules
CREATE INDEX shared_modules_shared_module_uuid_idx
    ON shared_modules(shared_module_uuid);

CREATE INDEX shared_modules_share_id_idx
    ON shared_modules(share_id);

CREATE INDEX shared_modules_hoks_eid_idx
    ON shared_modules(hoks_eid);

-- Table osaamisen_osoittamisen_sisallot
CREATE INDEX os_osoittamisen_sisallot_osaamisen_osoittaminen_id_idx
    ON osaamisen_osoittamisen_sisallot(osaamisen_osoittaminen_id);

-- Table osaamisen_osoittamisen_yksilolliset_kriteerit
CREATE INDEX os_osoittam_yksiloll_kriteerit_osaamisen_osoittaminen_id_idx
    ON osaamisen_osoittamisen_yksilolliset_kriteerit(osaamisen_osoittaminen_id);

-- Table tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
CREATE INDEX tyopaikalla_jarj_koul_tyoteht_tyopaikalla_jarj_koulutus_id_idx
    ON tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat(tyopaikalla_jarjestettava_koulutus_id);

-- Table aiemmin_hankitut_yto_osa_alueet
CREATE INDEX aiemm_hank_yto_osa_alueet_aiemm_hank_yht_tutk_osa_id_idx
    ON aiemmin_hankitut_yto_osa_alueet(aiemmin_hankittu_yhteinen_tutkinnon_osa_id);

-- Table yhteisen_tutkinnon_osan_osa_alueet
CREATE INDEX yht_tutkinnon_osan_osa_alueet_yhteinen_tutkinnon_osa_id_idx
    ON yhteisen_tutkinnon_osan_osa_alueet(yhteinen_tutkinnon_osa_id);

-- Table todennettu_arviointi_arvioijat
CREATE INDEX todenn_arv_arvioijat_todennettu_arviointi_lisatiedot_id_idx
    ON todennettu_arviointi_arvioijat(todennettu_arviointi_lisatiedot_id);

-- Table aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
CREATE INDEX aiemm_hank_yht_tutk_osan_naytto_osaamisen_osoittaminen_id_idx
    ON aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id);

CREATE INDEX aiemm_hank_yht_tutk_osan_naytto_aiemm_hank_yht_tutk_osa_id_idx
    ON aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto(aiemmin_hankittu_yhteinen_tutkinnon_osa_id);

-- Table aiemmin_hankitun_yto_osa_alueen_naytto
CREATE INDEX aiemm_hank_yto_osa_alueen_naytto_osaamisen_osoittaminen_id_idx
    ON aiemmin_hankitun_yto_osa_alueen_naytto(osaamisen_osoittaminen_id);

CREATE INDEX aiemm_hank_yto_osa_alueen_naytto_aiemm_hank_yto_osa_alue_id_idx
    ON aiemmin_hankitun_yto_osa_alueen_naytto(aiemmin_hankittu_yto_osa_alue_id);

-- Table hankittavan_ammat_tutkinnon_osan_naytto
CREATE INDEX hank_ammat_tutkinnon_osan_naytto_osaamisen_osoittaminen_id_idx
    ON hankittavan_ammat_tutkinnon_osan_naytto(osaamisen_osoittaminen_id);

CREATE INDEX hank_ammat_tutk_osan_naytto_hank_ammat_tutkinnon_osa_id_idx
    ON hankittavan_ammat_tutkinnon_osan_naytto(hankittava_ammat_tutkinnon_osa_id);

-- Table hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
CREATE INDEX hank_amm_tutkinn_osan_os_hanktav_hank_ammat_tutk_osa_id_idx
    ON hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat(hankittava_ammat_tutkinnon_osa_id);

CREATE INDEX hank_ammat_tutk_osan_os_hanktav_osaamisen_hankkimistapa_id_idx
    ON hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat(osaamisen_hankkimistapa_id);

-- Table yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
CREATE INDEX yht_tutk_osan_osa_alueen_os_hanktav_osaam_hankkimistapa_id_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat(osaamisen_hankkimistapa_id);

CREATE INDEX yht_tutk_osan_osa_al_osaam_hanktav_yht_tutk_os_osa_alue_id_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat(yhteisen_tutkinnon_osan_osa_alue_id);

CREATE INDEX yht_tutk_osan_osa_al_osaam_hanktav_osaam_hanktapa_id_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat(osaamisen_hankkimistapa_id);

-- Table yhteisen_tutkinnon_osan_osa_alueen_naytot
CREATE INDEX yht_tutk_osan_osa_alueen_naytot_osaamisen_osoittaminen_id_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_naytot(osaamisen_osoittaminen_id);

CREATE INDEX yht_tutk_osan_osa_al_naytot_yht_tutkinnon_osan_osa_alue_id_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_naytot(yhteisen_tutkinnon_osan_osa_alue_id);

-- Table hankittavaat_paikalliset_tutkinnon_osat
CREATE INDEX hankittavat_paikalliset_tutkinnon_osat_hoks_id_idx
    ON hankittavat_paikalliset_tutkinnon_osat(hoks_id);

-- Table hankittavaat_yhteiset_tutkinnon_osat
CREATE INDEX hankittavat_yhteiset_tutkinnon_osat_hoks_id_idx
    ON hankittavat_yhteiset_tutkinnon_osat(hoks_id);

-- Table hankittavaat_amm_tutkinnon_osat
CREATE INDEX hankittavat_ammat_tutkinnon_osat_hoks_id_idx
    ON hankittavat_ammat_tutkinnon_osat(hoks_id);

-- Table aiemmin_hankitun_ammat_tutkinnon_osan_naytto
CREATE INDEX aiemmin_hank_ammat_tutk_osan_naytto_osaamisen_osoitt_id_idx
    ON aiemmin_hankitun_ammat_tutkinnon_osan_naytto(osaamisen_osoittaminen_id);

CREATE INDEX aiemm_hank_ammat_tutk_os_naytto_aiemm_hank_amm_tutk_osa_id_idx
    ON aiemmin_hankitun_ammat_tutkinnon_osan_naytto(aiemmin_hankittu_ammat_tutkinnon_osa_id);

-- Table osaamisen_osoittamisen_osa_alueet
CREATE INDEX osaamisen_osoittamisen_osa_alueet_koodisto_koodi_id_idx
    ON osaamisen_osoittamisen_osa_alueet(koodisto_koodi_id);

CREATE INDEX osaamisen_osoittamisen_osa_alueet_osaamisen_osoittaminen_id_idx
    ON osaamisen_osoittamisen_osa_alueet(osaamisen_osoittaminen_id);

-- Table aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
CREATE INDEX aiemm_hank_paik_tutk_osan_naytto_osaamisen_osoittaminen_id_idx
    ON aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id);

CREATE INDEX aiemm_hank_paik_tutk_os_naytto_aiemm_hank_paik_tutk_osa_id_idx
    ON aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto(aiemmin_hankittu_paikallinen_tutkinnon_osa_id);

-- Table hankittavan_paikallisen_tutkinnon_osan_naytto
CREATE INDEX hank_paik_tutkinnon_osan_naytto_osaamisen_osoittaminen_id_idx
    ON hankittavan_paikallisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id);

CREATE INDEX hank_paik_tutk_osan_naytto_hank_paik_tutkinnon_osa_id_idx
    ON hankittavan_paikallisen_tutkinnon_osan_naytto(hankittava_paikallinen_tutkinnon_osa_id);

-- Table osaamisen_osoittamisen_tyoelama_arvioija
CREATE INDEX osaam_osoitt_tyoelama_arvioija_tyoelama_arvioija_id_idx
    ON osaamisen_osoittamisen_tyoelama_arvioija(tyoelama_arvioija_id);

CREATE INDEX osaam_osoitt_tyoelama_arvioija_osaamisen_osoittaminen_id_idx
    ON osaamisen_osoittamisen_tyoelama_arvioija(osaamisen_osoittaminen_id);

-- Table osaamisen_hankkimistavat
CREATE INDEX osaamisen_hankkimistavat_tyopaikalla_jarj_koulutus_id_idx
    ON osaamisen_hankkimistavat(tyopaikalla_jarjestettava_koulutus_id);

CREATE INDEX osaamisen_hankkimistavat_osaamisen_hankkimistapa_koodi_uri_idx
    ON osaamisen_hankkimistavat(osaamisen_hankkimistapa_koodi_uri);

CREATE INDEX osaamisen_hankkimistavat_loppu_idx
    ON osaamisen_hankkimistavat(loppu);

-- Table osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
CREATE INDEX osaam_osoitt_koul_jarj_arv_koul_jarj_osaamisen_arvioija_id_idx
    ON osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija(koulutuksen_jarjestaja_osaamisen_arvioija_id);

CREATE INDEX osaam_osoitt_koul_jarj_arvioija_osaamisen_osoittaminen_id_idx
    ON osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija(osaamisen_osoittaminen_id);
