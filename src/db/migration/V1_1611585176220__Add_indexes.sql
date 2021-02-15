--SELECT * FROM kyselylinkit WHERE oppija_oid = ? AND alkupvm <= now()

-- Table kyselylinkit
CREATE INDEX kyselylinkit_oppija_oid_alkupvm_idx
    ON kyselylinkit(oppija_oid, alkupvm);

-- SELECT * FROM hoksit WHERE deleted_at IS NULL

-- SELECT eid FROM hoksit WHERE eid = ? AND deleted_at IS NULL

-- SELECT h.oppija_oid, h.opiskeluoikeus_oid
-- FROM hoksit AS h
--    LEFT JOIN opiskeluoikeudet AS o
--                   ON h.opiskeluoikeus_oid = o.oid
-- WHERE o.oid IS NULL;

-- SELECT DISTINCT oppija_oid
-- FROM hoksit AS h
--          LEFT JOIN oppijat AS o
--                    ON h.oppija_oid = o.oid
-- WHERE o.oid IS NULL

-- SELECT DISTINCT oppilaitos_oid
-- FROM opiskeluoikeudet, hoksit
-- WHERE hoksit.opiskeluoikeus_oid = opiskeluoikeudet.oid

-- SELECT DISTINCT oppilaitos_oid
-- FROM opiskeluoikeudet, hoksit
-- WHERE opiskeluoikeudet.koulutustoimija_oid = ?
--   AND hoksit.opiskeluoikeus_oid = opiskeluoikeudet.oid

-- Table hoksit
CREATE INDEX hoksit_deleted_at_eid_idx
    ON hoksit(deleted_at, eid);

CREATE INDEX hoksit_opiskeluoikeus_oid_idx
    ON hoksit(opiskeluoikeus_oid);

CREATE INDEX hoksit_oppija_oid_idx
    ON hoksit(oppija_oid, eid, opiskeluoikeus_oid);

-- SELECT oid FROM opiskeluoikeudet
-- WHERE hankintakoulutus_opiskeluoikeus_oid = ?

-- SELECT COUNT(oid) FROM opiskeluoikeudet WHERE koulutustoimija_oid = ?

-- SELECT COUNT(*) FROM opiskeluoikeudet WHERE tutkinto_nimi->>'fi' = '' OR tutkinto_nimi->>'fi' IS NULL

-- SELECT * FROM opiskeluoikeudet WHERE tutkinto_nimi->>'fi' = '' OR tutkinto_nimi->>'fi' IS NULL

-- SELECT *
-- FROM opiskeluoikeudet
-- WHERE oid = ?

-- SELECT * FROM opiskeluoikeudet WHERE oppija_oid = ?

-- Table opiskeluoikeudet
CREATE INDEX opiskeluoikeudet_oid_hankintakoulutus_opiskeluoikeus_oid_idx
    ON opiskeluoikeudet(oid, hankintakoulutus_opiskeluoikeus_oid);

CREATE INDEX opiskeluoikeudet_idx
    ON opiskeluoikeudet(koulutustoimija_oid, oppilaitos_oid, oid, oppija_oid);

CREATE INDEX opiskeluoikeudet_tutkinto_nimi_idx
    ON opiskeluoikeudet(tutkinto_nimi);

-- SELECT o.oid,
--        o.nimi,
--        oo.oid AS opiskeluoikeus_oid,
--        oo.oppilaitos_oid,
--        oo.koulutustoimija_oid,
--        oo.tutkinto_nimi,
--        oo.osaamisala_nimi
-- FROM oppijat AS o
--          LEFT OUTER JOIN opiskeluoikeudet AS oo
--                          ON (o.oid = oo.oppija_oid)
--          INNER JOIN hoksit AS h
--                     ON (oo.oid = h.opiskeluoikeus_oid)
-- WHERE ((oo.oppilaitos_oid IS NOT NULL AND oo.oppilaitos_oid LIKE ?) OR
--        (oo.koulutustoimija_oid IS NOT NULL AND oo.koulutustoimija_oid LIKE ?))
--   AND o.nimi ILIKE ?
--                        :tutkinto-filter
--                        :osaamisala-filter
-- ORDER BY :order-by-column :desc
-- LIMIT ?
--     OFFSET ?

-- SELECT
--     COUNT(o.oid)
-- FROM oppijat AS o
--          LEFT OUTER JOIN opiskeluoikeudet AS oo
--                          ON (o.oid = oo.oppija_oid)
--          INNER JOIN hoksit AS h
--                     ON (oo.oid = h.opiskeluoikeus_oid)
-- WHERE
--     ((oo.oppilaitos_oid IS NOT NULL AND oo.oppilaitos_oid LIKE ?) OR
--      (oo.koulutustoimija_oid IS NOT NULL AND oo.koulutustoimija_oid LIKE ?)) AND
--         o.nimi ILIKE ?
--                          :tutkinto-filter
--                          :osaamisala-filter

-- SELECT *
-- FROM oppijat
-- WHERE oid = ?

-- Table oppijat
 CREATE INDEX oppijat_oid_nimi_idx
     ON oppijat(oid, nimi);

-- SELECT * FROM sessions WHERE session_key = ?

-- Table sessions
CREATE INDEX sessions_session_key_idx
    ON sessions(session_key);

-- SELECT * FROM user_settings WHERE user_oid = ?

-- Table user_settings
CREATE INDEX user_settings_user_oid_idx
    ON user_settings(user_oid);

-- SELECT
--     o.nimi AS oppija_nimi,
--     o.oid AS oppija_oid,
--     opo.tutkinto_nimi,
--     opo.osaamisala_nimi,
--     sm.tutkinnonosa_tyyppi AS tutkinnonosa_tyyppi,
--     sm.voimassaolo_alku,
--     sm.voimassaolo_loppu
-- FROM shared_modules sm
--          LEFT OUTER JOIN hoksit AS h
--                          ON (sm.hoks_eid = h.eid)
--          LEFT OUTER JOIN oppijat AS o
--                          ON (h.oppija_oid = o.oid)
--          LEFT OUTER JOIN opiskeluoikeudet AS opo
--                          ON (h.opiskeluoikeus_oid = opo.oid)
-- WHERE sm.share_id = ?;

-- SELECT share_id, voimassaolo_alku, voimassaolo_loppu
-- FROM shared_modules
-- WHERE shared_module_uuid = ?

-- SELECT *
-- FROM shared_modules
-- WHERE share_id = ?

-- Table shared_modules
CREATE INDEX shared_modules_shared_id_hoks_eid_idx
    ON shared_modules(share_id, hoks_eid);

CREATE INDEX shared_modules_shared_module_uuid_idx
    ON shared_modules(shared_module_uuid);

-- SELECT * FROM :table
-- WHERE :column = ? AND deleted_at IS NULL

-- {:table "osaamisen_osoittamisen_sisallot"
--   :column "osaamisen_osoittaminen_id"}

-- Table osaamisen_osoittamisen_sisallot
CREATE INDEX osaamisen_osoittamisen_sisallot_idx
    ON osaamisen_osoittamisen_sisallot(osaamisen_osoittaminen_id);

-- {:table "osaamisen_osoittamisen_yksilolliset_kriteerit"
--   :column "osaamisen_osoittaminen_id"}

-- Table osaamisen_osoittamisen_yksilolliset_kriteerit
CREATE INDEX osaamisen_osoittamisen_yksilolliset_kriteerit_idx
    ON osaamisen_osoittamisen_yksilolliset_kriteerit(osaamisen_osoittaminen_id);

-- {:table "tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat"
--   :column "tyopaikalla_jarjestettava_koulutus_id"}

-- Table tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
CREATE INDEX tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat_idx
    ON tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat(tyopaikalla_jarjestettava_koulutus_id);

-- {:table "aiemmin_hankitut_yto_osa_alueet"
--  :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}

-- Table aiemmin_hankitut_yto_osa_alueet
CREATE INDEX aiemm_hank_yto_osa_alueet_idx
    ON aiemmin_hankitut_yto_osa_alueet(aiemmin_hankittu_yhteinen_tutkinnon_osa_id);

-- {:table "yhteisen_tutkinnon_osan_osa_alueet"
--   :column "yhteinen_tutkinnon_osa_id"}

-- Table yhteisen_tutkinnon_osan_osa_alueet
CREATE INDEX yht_tutkinnon_osan_osa_alueet_idx
    ON yhteisen_tutkinnon_osan_osa_alueet(yhteinen_tutkinnon_osa_id);

-- SELECT t.* FROM :table AS t
--                     LEFT OUTER JOIN :join AS h
--                                     ON (h.:secondary-column = t.:primary-column AND h.deleted_at IS NULL)
-- WHERE h.:column = ? AND t.deleted_at IS NULL

-- (generate-select-join
--     {:table "osaamisen_osoittamiset"
--      :join "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
--      :secondary-column "osaamisen_osoittaminen_id"
--      :primary-column "id"
--      :column "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))

-- Table aiemmin_hankitun_ammat_tutkinnon_osan_naytto
CREATE INDEX aiemmin_hankitun_ammat_tutkinnon_osan_naytto_idx
    ON aiemmin_hankitun_ammat_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_ammat_tutkinnon_osa_id);

-- (generate-select-join
--     {:table "koodisto_koodit"
--      :join "osaamisen_osoittamisen_osa_alueet"
--      :secondary-column "koodisto_koodi_id"
--      :primary-column "id"
--      :column "osaamisen_osoittaminen_id"})

-- Table koodisto_koodit
 CREATE INDEX koodisto_koodit_deleted_at_idx
     ON koodisto_koodit(deleted_at);

-- Table osaamisen_osoittamisen_osa_alueet
CREATE INDEX osaamisen_osoittamisen_osa_alueet_idx
    ON osaamisen_osoittamisen_osa_alueet(osaamisen_osoittaminen_id, koodisto_koodi_id);

-- (generate-select-join
--     {:table "osaamisen_osoittamiset"
--      :join "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
--      :secondary-column "osaamisen_osoittaminen_id"
--      :primary-column "id"
--      :column "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"})

-- Table aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
CREATE INDEX aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto_idx
    ON aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_paikallinen_tutkinnon_osa_id);

--   (generate-select-join
--     {:table "osaamisen_osoittamiset"
--      :join "hankittavan_paikallisen_tutkinnon_osan_naytto"
--      :secondary-column "osaamisen_osoittaminen_id"
--      :primary-column "id"
--      :column "hankittava_paikallinen_tutkinnon_osa_id"}))

-- Table hankittavan_paikallisen_tutkinnon_osan_naytto
CREATE INDEX hank_paik_tutkinnon_osan_naytto_idx
    ON hankittavan_paikallisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, hankittava_paikallinen_tutkinnon_osa_id);

--   (generate-select-join
--     {:table "koulutuksen_jarjestaja_osaamisen_arvioijat"
--      :join "osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija"
--      :secondary-column "koulutuksen_jarjestaja_osaamisen_arvioija_id"
--      :primary-column "id"
--      :column "osaamisen_osoittaminen_id"})

-- Table osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
CREATE INDEX osaam_osoitt_koul_jarj_arv_koul_jarj_osaamisen_arvioija_idx
    ON osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija(koulutuksen_jarjestaja_osaamisen_arvioija_id, osaamisen_osoittaminen_id);

-- (def select-arvioijat-by-todennettu-arviointi-id
--   (generate-select-join
--     {:table "koulutuksen_jarjestaja_osaamisen_arvioijat"
--      :join "todennettu_arviointi_arvioijat"
--      :secondary-column "koulutuksen_jarjestaja_osaamisen_arvioija_id"
--      :primary-column "id"
--      :column "todennettu_arviointi_lisatiedot_id"}))

-- Table todennettu_arviointi_arvioijat
CREATE INDEX todennettu_arviointi_arvioijat_idx
    ON todennettu_arviointi_arvioijat(koulutuksen_jarjestaja_osaamisen_arvioija_id, todennettu_arviointi_lisatiedot_id);

-- (def select-tyoelama-osaamisen-arvioijat-by-hon-id
--   (generate-select-join
--     {:table "tyoelama_osaamisen_arvioijat"
--      :join "osaamisen_osoittamisen_tyoelama_arvioija"
--      :secondary-column "tyoelama_arvioija_id"
--      :primary-column "id"
--      :column "osaamisen_osoittaminen_id"}))

-- Table tyoelama_osaamisen_arvioijat
CREATE INDEX tyoelama_osaamisen_arvioijat_deleted_at_idx
    ON tyoelama_osaamisen_arvioijat(deleted_at);

-- Table osaamisen_osoittamisen_tyoelama_arvioija
CREATE INDEX osaam_osoitt_tyoelama_arvioija_idx
    ON osaamisen_osoittamisen_tyoelama_arvioija(tyoelama_arvioija_id, osaamisen_osoittaminen_id);

-- (generate-select-join
--     {:table "osaamisen_hankkimistavat"
--      :join "hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat"
--      :secondary-column "osaamisen_hankkimistapa_id"
--      :primary-column "id"
--      :column "hankittava_paikallinen_tutkinnon_osa_id"})

-- Table osaamisen_hankkimistavat
CREATE INDEX osaamisen_hankkimistavat_deleted_at_idx
  ON osaamisen_hankkimistavat(deleted_at);

CREATE INDEX osaamisen_hankkimistavat_idx
    ON osaamisen_hankkimistavat(osaamisen_hankkimistapa_koodi_uri, loppu);

-- Table hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
CREATE INDEX hankittavan_paik_tutkinnon_osan_osaamisen_hankkimistavat_idx
    ON hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat(osaamisen_hankkimistapa_id, hankittava_paikallinen_tutkinnon_osa_id);

--   (generate-select-join
--     {:table "osaamisen_osoittamiset"
--      :join "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
--      :secondary-column "osaamisen_osoittaminen_id"
--      :primary-column "id"
--      :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))

-- Table aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
CREATE INDEX aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto_idx
    ON aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_yhteinen_tutkinnon_osa_id, deleted_at);

--   (generate-select-join
--     {:table "osaamisen_osoittamiset"
--      :join "aiemmin_hankitun_yto_osa_alueen_naytto"
--      :secondary-column "osaamisen_osoittaminen_id"
--      :primary-column "id"
--      :column "aiemmin_hankittu_yto_osa_alue_id"}))

-- Table aiemmin_hankitun_yto_osa_alueen_naytto
CREATE INDEX aiemmin_hankitun_yto_osa_alueen_naytto_idx
    ON aiemmin_hankitun_yto_osa_alueen_naytto(osaamisen_osoittaminen_id, aiemmin_hankittu_yto_osa_alue_id, deleted_at);

--   (generate-select-join
--     {:table "osaamisen_osoittamiset"
--      :join "hankittavan_ammat_tutkinnon_osan_naytto"
--      :secondary-column "osaamisen_osoittaminen_id"
--      :primary-column "id"
--      :column "hankittava_ammat_tutkinnon_osa_id"}))

-- Table hankittavan_ammat_tutkinnon_osan_naytto
CREATE INDEX hank_ammat_tutkinnon_osan_naytto_idx
    ON hankittavan_ammat_tutkinnon_osan_naytto(osaamisen_osoittaminen_id, hankittava_ammat_tutkinnon_osa_id, deleted_at);

--   (generate-select-join
--     {:table "osaamisen_hankkimistavat"
--      :join "hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat"
--      :secondary-column "osaamisen_hankkimistapa_id"
--      :primary-column "id"
--      :column "hankittava_ammat_tutkinnon_osa_id"}))

-- Table hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
CREATE INDEX hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat_idx
    ON hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat(hankittava_ammat_tutkinnon_osa_id, osaamisen_hankkimistapa_id, deleted_at);

-- (generate-select-join
--     {:table "osaamisen_hankkimistavat"
--      :join "yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat"
--      :secondary-column "osaamisen_hankkimistapa_id"
--      :primary-column "id"
--      :column "yhteisen_tutkinnon_osan_osa_alue_id"})

-- Table yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
CREATE INDEX yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat(osaamisen_hankkimistapa_id, yhteisen_tutkinnon_osan_osa_alue_id, deleted_at);

-- (generate-select-join
--     {:table "osaamisen_osoittamiset"
--      :join "yhteisen_tutkinnon_osan_osa_alueen_naytot"
--      :secondary-column "osaamisen_osoittaminen_id"
--      :primary-column "id"
--      :column "yhteisen_tutkinnon_osan_osa_alue_id"})

-- Table yhteisen_tutkinnon_osan_osa_alueen_naytot
CREATE INDEX yht_tutk_osan_osa_alueen_naytot_osaamisen_osoittaminen_id_idx
    ON yhteisen_tutkinnon_osan_osa_alueen_naytot(osaamisen_osoittaminen_id, yhteisen_tutkinnon_osan_osa_alue_id, deleted_at);

-- SELECT
--     h.id AS hoks_id,
--     h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
--     h.oppija_oid AS oppija_oid,
--     osa.id AS tutkinnonosa_id,
--     oh.id AS hankkimistapa_id,
--     oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
--     oh.alku AS alkupvm,
--     oh.loppu AS loppupvm,
--     tjk.tyopaikan_nimi AS tyopaikan_nimi,
--     tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
--     tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
--     tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS tyopaikkaohjaaja_email
-- FROM hoksit h
--          LEFT OUTER JOIN hankittavat_ammat_tutkinnon_osat AS osa
--                          ON (h.id = osa.hoks_id AND osa.deleted_at IS NULL)
--          LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat AS osajoin
--                          ON (osa.id = osajoin.hankittava_ammat_tutkinnon_osa_id)
--          LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
--                          ON (osajoin.osaamisen_hankkimistapa_id = oh.id)
--          LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
--                          ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
-- WHERE
--     (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' or
--      oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
--   AND oh.loppu >= ?
--   AND oh.loppu <= ?

-- Table hankittavaat_amm_tutkinnon_osat
CREATE INDEX hankittavat_ammat_tutkinnon_osat_idx
    ON hankittavat_ammat_tutkinnon_osat(hoks_id, deleted_at);

-- SELECT
--     h.id AS hoks_id,
--     h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
--     h.oppija_oid AS oppija_oid,
--     osa.id AS tutkinnonosa_id,
--     oh.id AS hankkimistapa_id,
--     oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
--     oh.alku AS alkupvm,
--     oh.loppu AS loppupvm,
--     tjk.tyopaikan_nimi AS tyopaikan_nimi,
--     tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
--     tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
--     tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS tyopaikkaohjaaja_email
-- FROM hoksit h
--          LEFT OUTER JOIN hankittavat_paikalliset_tutkinnon_osat AS osa
--                          ON (h.id = osa.hoks_id AND osa.deleted_at IS NULL)
--          LEFT OUTER JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat AS osajoin
--                          ON (osa.id = osajoin.hankittava_paikallinen_tutkinnon_osa_id)
--          LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
--                          ON (osajoin.osaamisen_hankkimistapa_id = oh.id)
--          LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
--                          ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
-- WHERE
--     (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' or
--      oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
--   AND oh.loppu >= ?
--   AND oh.loppu <= ?

-- Table hankittavaat_paikalliset_tutkinnon_osat
CREATE INDEX hankittavat_paikalliset_tutkinnon_osat_idx
    ON hankittavat_paikalliset_tutkinnon_osat(hoks_id, deleted_at);

-- SELECT
--     h.id AS hoks_id,
--     h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
--     h.oppija_oid AS oppija_oid,
--     osa.id AS tutkinnonosa_id,
--     oh.id AS hankkimistapa_id,
--     oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
--     oh.alku AS alkupvm,
--     oh.loppu AS loppupvm,
--     tjk.tyopaikan_nimi AS tyopaikan_nimi,
--     tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
--     tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
--     tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS tyopaikkaohjaaja_email
-- FROM hoksit h
--          LEFT OUTER JOIN hankittavat_yhteiset_tutkinnon_osat AS osat
--                          ON (h.id = osat.hoks_id AND osat.deleted_at IS NULL)
--          LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueet AS osa
--                          ON (osat.id = osa.yhteinen_tutkinnon_osa_id)
--          LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat AS ytooh
--                          ON (osa.id = ytooh.yhteisen_tutkinnon_osan_osa_alue_id)
--          LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
--                          ON (ytooh.osaamisen_hankkimistapa_id = oh.id)
--          LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
--                          ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
-- WHERE
--     (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' or
--      oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
--   AND oh.loppu >= ?
--   AND oh.loppu <= ?

-- Table hankittavat_yhteiset_tutkinnon_osat
CREATE INDEX hankittavat_yhteiset_tutkinnon_osat_idx
    ON hankittavat_yhteiset_tutkinnon_osat(hoks_id, deleted_at);
