CREATE OR REPLACE VIEW v_hankittavat_tutkinnon_osat AS
SELECT concat('yto_', yto.id)         AS id,
       yto.created_at                 AS created_at,
       yto.updated_at                 AS updated_at,
       yto.deleted_at                 AS deleted_at,
       yto.hoks_id                    AS hoks_id,
       'yto'                          AS tyyppi,
       yto.tutkinnon_osa_koodi_uri    AS koodi_uri,
       yto.tutkinnon_osa_koodi_versio AS koodi_versio,
       yto.koulutuksen_jarjestaja_oid AS koulutuksen_jarjestaja_oid,
       yto.module_id                  AS module_id,
       NULL                           AS yto_id,
       NULL                           AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       NULL                           AS olennainen_seikka,
       NULL                           AS opetus_ja_ohjaus_maara,
       NULL::integer                  AS laajuus,
       NULL                           AS nimi,
       NULL                           AS tavoitteet_ja_sisallot,
       NULL                           AS amosaa_tunniste
FROM hankittavat_yhteiset_tutkinnon_osat yto
UNION
SELECT concat('yto_alue_', yto_alue.id)                     AS id,
       yto_alue.created_at                                  AS created_at,
       yto_alue.updated_at                                  AS updated_at,
       yto_alue.deleted_at                                  AS deleted_at,
       NULL                                                 AS hoks_id,
       'yto_alue'                                           AS tyyppi,
       yto_alue.osa_alue_koodi_uri                          AS koodi_uri,
       yto_alue.osa_alue_koodi_versio                       AS koodi_versio,
       yto_alue.koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       yto_alue.module_id                                   AS module_id,
       concat('yto_', yto_alue.yhteinen_tutkinnon_osa_id)   AS yto_id,
       yto_alue.vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       yto_alue.olennainen_seikka                           AS olennainen_seikka,
       yto_alue.opetus_ja_ohjaus_maara                      AS opetus_ja_ohjaus_maara,
       NULL::integer                                        AS laajuus,
       NULL                                                 AS nimi,
       NULL                                                 AS tavoitteet_ja_sisallot,
       NULL                                                 AS amosaa_tunniste
FROM yhteisen_tutkinnon_osan_osa_alueet yto_alue
UNION
SELECT concat('ato_', ato.id)                          AS id,
       ato.created_at                                  AS created_at,
       ato.updated_at                                  AS updated_at,
       ato.deleted_at                                  AS deleted_at,
       ato.hoks_id                                     AS hoks_id,
       'ato'                                           AS tyyppi,
       ato.tutkinnon_osa_koodi_uri                     AS koodi_uri,
       ato.tutkinnon_osa_koodi_versio                  AS koodi_versio,
       ato.koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       ato.module_id                                   AS module_id,
       NULL                                            AS yto_id,
       ato.vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       ato.olennainen_seikka                           AS olennainen_seikka,
       ato.opetus_ja_ohjaus_maara                      AS opetus_ja_ohjaus_maara,
       NULL::integer                                   AS laajuus,
       NULL                                            AS nimi,
       NULL                                            AS tavoitteet_ja_sisallot,
       NULL                                            AS amosaa_tunniste
FROM hankittavat_ammat_tutkinnon_osat ato
UNION
SELECT concat('pto_', pto.id)                          AS id,
       pto.created_at                                  AS created_at,
       pto.updated_at                                  AS updated_at,
       pto.deleted_at                                  AS deleted_at,
       pto.hoks_id                                     AS hoks_id,
       'pto'                                           AS tyyppi,
       NULL                                            AS koodi_uri,
       NULL                                            AS koodi_versio,
       pto.koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       pto.module_id                                   AS module_id,
       NULL                                            AS yto_id,
       pto.vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       pto.olennainen_seikka                           AS olennainen_seikka,
       pto.opetus_ja_ohjaus_maara                      AS opetus_ja_ohjaus_maara,
       pto.laajuus                                     AS laajuus,
       pto.nimi                                        AS nimi,
       pto.tavoitteet_ja_sisallot                      AS tavoitteet_ja_sisallot,
       pto.amosaa_tunniste                             AS amosaa_tunniste
FROM hankittavat_paikalliset_tutkinnon_osat pto;


CREATE OR REPLACE VIEW v_osaamisen_hankkimistavat AS
SELECT DISTINCT oh.id                                   AS id,
                oh.created_at                           AS created_at,
                oh.updated_at                           AS updated_at,
                oh.deleted_at                           AS deleted_at,
                yto.hoks_id                             AS hoks_id,
                concat('yto_alue_', ytooa.id)           AS osa_id,
                oh.jarjestajan_edustaja_nimi            AS jarjestajan_edustaja_nimi,
                oh.jarjestajan_edustaja_rooli           AS jarjestajan_edustaja_rooli,
                oh.jarjestajan_edustaja_oppilaitos_oid  AS jarjestajan_edustaja_oppilaitos_oid,
                oh.ajanjakson_tarkenne                  AS ajanjakson_tarkenne,
                oh.osaamisen_hankkimistapa_koodi_uri    AS osaamisen_hankkimistapa_koodi_uri,
                oh.osaamisen_hankkimistapa_koodi_versio AS osaamisen_hankkimistapa_koodi_versio,
                oh.hankkijan_edustaja_nimi              AS hankkijan_edustaja_nimi,
                oh.hankkijan_edustaja_rooli             AS hankkijan_edustaja_rooli,
                oh.hankkijan_edustaja_oppilaitos_oid    AS hankkijan_edustaja_oppilaitos_oid,
                oh.alku                                 AS alku,
                oh.loppu                                AS loppu,
                oh.module_id                            AS module_id,
                oh.tep_kasitelty                        AS tep_kasitelty,
                oh.osa_aikaisuustieto                   AS osa_aikaisuustieto,
                oh.oppisopimuksen_perusta_koodi_uri     AS oppisopimuksen_perusta_koodi_uri,
                oh.oppisopimuksen_perusta_koodi_versio  AS oppisopimuksen_perusta_koodi_versio,
                oh.yksiloiva_tunniste                   AS yksiloiva_tunniste
FROM osaamisen_hankkimistavat oh
         JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat ytooaoh
              on oh.id = ytooaoh.osaamisen_hankkimistapa_id
         JOIN yhteisen_tutkinnon_osan_osa_alueet ytooa
              on ytooaoh.yhteisen_tutkinnon_osan_osa_alue_id = ytooa.id
         JOIN hankittavat_yhteiset_tutkinnon_osat yto
              on ytooa.yhteinen_tutkinnon_osa_id = yto.id
UNION
SELECT DISTINCT oh.id                                   AS id,
                oh.created_at                           AS created_at,
                oh.updated_at                           AS updated_at,
                oh.deleted_at                           AS deleted_at,
                ato.hoks_id                             AS hoks_id,
                concat('ato_', ato.id)                  AS osa_id,
                oh.jarjestajan_edustaja_nimi            AS jarjestajan_edustaja_nimi,
                oh.jarjestajan_edustaja_rooli           AS jarjestajan_edustaja_rooli,
                oh.jarjestajan_edustaja_oppilaitos_oid  AS jarjestajan_edustaja_oppilaitos_oid,
                oh.ajanjakson_tarkenne                  AS ajanjakson_tarkenne,
                oh.osaamisen_hankkimistapa_koodi_uri    AS osaamisen_hankkimistapa_koodi_uri,
                oh.osaamisen_hankkimistapa_koodi_versio AS osaamisen_hankkimistapa_koodi_versio,
                oh.hankkijan_edustaja_nimi              AS hankkijan_edustaja_nimi,
                oh.hankkijan_edustaja_rooli             AS hankkijan_edustaja_rooli,
                oh.hankkijan_edustaja_oppilaitos_oid    AS hankkijan_edustaja_oppilaitos_oid,
                oh.alku                                 AS alku,
                oh.loppu                                AS loppu,
                oh.module_id                            AS module_id,
                oh.tep_kasitelty                        AS tep_kasitelty,
                oh.osa_aikaisuustieto                   AS osa_aikaisuustieto,
                oh.oppisopimuksen_perusta_koodi_uri     AS oppisopimuksen_perusta_koodi_uri,
                oh.oppisopimuksen_perusta_koodi_versio  AS oppisopimuksen_perusta_koodi_versio,
                oh.yksiloiva_tunniste                   AS yksiloiva_tunniste
FROM osaamisen_hankkimistavat oh
         JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat atooh
              ON oh.id = atooh.osaamisen_hankkimistapa_id
         JOIN hankittavat_ammat_tutkinnon_osat ato
              on atooh.hankittava_ammat_tutkinnon_osa_id = ato.id
UNION
SELECT DISTINCT oh.id                                   AS id,
                oh.created_at                           AS created_at,
                oh.updated_at                           AS updated_at,
                oh.deleted_at                           AS deleted_at,
                pto.hoks_id                             AS hoks_id,
                concat('pto_', pto.id)                  AS osa_id,
                oh.jarjestajan_edustaja_nimi            AS jarjestajan_edustaja_nimi,
                oh.jarjestajan_edustaja_rooli           AS jarjestajan_edustaja_rooli,
                oh.jarjestajan_edustaja_oppilaitos_oid  AS jarjestajan_edustaja_oppilaitos_oid,
                oh.ajanjakson_tarkenne                  AS ajanjakson_tarkenne,
                oh.osaamisen_hankkimistapa_koodi_uri    AS osaamisen_hankkimistapa_koodi_uri,
                oh.osaamisen_hankkimistapa_koodi_versio AS osaamisen_hankkimistapa_koodi_versio,
                oh.hankkijan_edustaja_nimi              AS hankkijan_edustaja_nimi,
                oh.hankkijan_edustaja_rooli             AS hankkijan_edustaja_rooli,
                oh.hankkijan_edustaja_oppilaitos_oid    AS hankkijan_edustaja_oppilaitos_oid,
                oh.alku                                 AS alku,
                oh.loppu                                AS loppu,
                oh.module_id                            AS module_id,
                oh.tep_kasitelty                        AS tep_kasitelty,
                oh.osa_aikaisuustieto                   AS osa_aikaisuustieto,
                oh.oppisopimuksen_perusta_koodi_uri     AS oppisopimuksen_perusta_koodi_uri,
                oh.oppisopimuksen_perusta_koodi_versio  AS oppisopimuksen_perusta_koodi_versio,
                oh.yksiloiva_tunniste                   AS yksiloiva_tunniste
FROM osaamisen_hankkimistavat oh
         JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat ptooh
              ON oh.id = ptooh.osaamisen_hankkimistapa_id
         JOIN hankittavat_paikalliset_tutkinnon_osat pto
              on ptooh.hankittava_paikallinen_tutkinnon_osa_id = pto.id;


CREATE OR REPLACE VIEW v_aiemmin_hankitut_tutkinnon_osat AS
SELECT concat('yto_', yto.id)         AS id,
       yto.created_at                 AS created_at,
       yto.updated_at                 AS updated_at,
       yto.deleted_at                 AS deleted_at,
       yto.hoks_id                    AS hoks_id,
       'yto'                          AS tyyppi,
       yto.tutkinnon_osa_koodi_uri    AS koodi_uri,
       yto.tutkinnon_osa_koodi_versio AS koodi_versio,
       yto.koulutuksen_jarjestaja_oid AS koulutuksen_jarjestaja_oid,
       yto.valittu_todentamisen_prosessi_koodi_uri AS valittu_todentamisen_prosessi_koodi_uri,
       yto.valittu_todentamisen_prosessi_koodi_versio AS valittu_todentamisen_prosessi_koodi_versio,
       yto.lahetetty_arvioitavaksi    AS lahetetty_arvioitavaksi,
       yto.tarkentavat_tiedot_osaamisen_arvioija_id AS tarkentavat_tiedot_osaamisen_arvioija_id,
       yto.module_id                  AS module_id,
       NULL                           AS yto_id,
       NULL                           AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       NULL                           AS olennainen_seikka,
       NULL::integer                  AS laajuus,
       NULL                           AS nimi,
       NULL                           AS tavoitteet_ja_sisallot,
       NULL                           AS amosaa_tunniste
FROM aiemmin_hankitut_yhteiset_tutkinnon_osat yto
UNION
SELECT concat('yto_alue_', yto_alue.id)                     AS id,
       yto_alue.created_at                                  AS created_at,
       yto_alue.updated_at                                  AS updated_at,
       yto_alue.deleted_at                                  AS deleted_at,
       NULL                                                 AS hoks_id,
       'yto_alue'                                           AS tyyppi,
       yto_alue.osa_alue_koodi_uri                          AS koodi_uri,
       yto_alue.osa_alue_koodi_versio                       AS koodi_versio,
       yto_alue.koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       yto_alue.valittu_todentamisen_prosessi_koodi_uri     AS valittu_todentamisen_prosessi_koodi_uri,
       yto_alue.valittu_todentamisen_prosessi_koodi_versio  AS valittu_todentamisen_prosessi_koodi_versio,
       NULL                                                 AS lahetetty_arvioitavaksi,
       yto_alue.tarkentavat_tiedot_osaamisen_arvioija_id    AS tarkentavat_tiedot_osaamisen_arvioija_id,
       yto_alue.module_id                                   AS module_id,
       concat('yto_', yto_alue.aiemmin_hankittu_yhteinen_tutkinnon_osa_id) AS yto_id,
       yto_alue.vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       yto_alue.olennainen_seikka                           AS olennainen_seikka,
       NULL::integer                                        AS laajuus,
       NULL                                                 AS nimi,
       NULL                                                 AS tavoitteet_ja_sisallot,
       NULL                                                 AS amosaa_tunniste
FROM aiemmin_hankitut_yto_osa_alueet yto_alue
UNION
SELECT concat('ato_', ato.id)                          AS id,
       ato.created_at                                  AS created_at,
       ato.updated_at                                  AS updated_at,
       ato.deleted_at                                  AS deleted_at,
       ato.hoks_id                                     AS hoks_id,
       'ato'                                           AS tyyppi,
       ato.tutkinnon_osa_koodi_uri                     AS koodi_uri,
       ato.tutkinnon_osa_koodi_versio                  AS koodi_versio,
       ato.koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       ato.valittu_todentamisen_prosessi_koodi_uri     AS valittu_todentamisen_prosessi_koodi_uri,
       ato.valittu_todentamisen_prosessi_koodi_versio  AS valittu_todentamisen_prosessi_koodi_versio,
       NULL                                            AS lahetetty_arvioitavaksi,
       ato.tarkentavat_tiedot_osaamisen_arvioija_id    AS tarkentavat_tiedot_osaamisen_arvioija_id,
       ato.module_id                                   AS module_id,
       NULL                                            AS yto_id,
       NULL                                            AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       ato.olennainen_seikka                           AS olennainen_seikka,
       NULL::integer                                   AS laajuus,
       NULL                                            AS nimi,
       NULL                                            AS tavoitteet_ja_sisallot,
       NULL                                            AS amosaa_tunniste
FROM aiemmin_hankitut_ammat_tutkinnon_osat ato
UNION
SELECT concat('pto_', pto.id)                          AS id,
       pto.created_at                                  AS created_at,
       pto.updated_at                                  AS updated_at,
       pto.deleted_at                                  AS deleted_at,
       pto.hoks_id                                     AS hoks_id,
       'pto'                                           AS tyyppi,
       NULL                                            AS koodi_uri,
       NULL                                            AS koodi_versio,
       pto.koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       pto.valittu_todentamisen_prosessi_koodi_uri     AS valittu_todentamisen_prosessi_koodi_uri,
       pto.valittu_todentamisen_prosessi_koodi_versio  AS valittu_todentamisen_prosessi_koodi_versio,
       pto.lahetetty_arvioitavaksi                     AS lahetetty_arvioitavaksi,
       pto.tarkentavat_tiedot_osaamisen_arvioija_id    AS tarkentavat_tiedot_osaamisen_arvioija_id,
       pto.module_id                                   AS module_id,
       NULL                                            AS yto_id,
       pto.vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       pto.olennainen_seikka                           AS olennainen_seikka,
       pto.laajuus                                     AS laajuus,
       pto.nimi                                        AS nimi,
       pto.tavoitteet_ja_sisallot                      AS tavoitteet_ja_sisallot,
       pto.amosaa_tunniste                             AS amosaa_tunniste
FROM aiemmin_hankitut_paikalliset_tutkinnon_osat pto;


CREATE OR REPLACE VIEW v_aiemmin_hankitun_tutkinnon_osan_naytto AS
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('ato_', aiemmin_hankittu_ammat_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id AS osaamisen_osoittaminen_id,
       module_id AS module_id,
       'ato' AS tyyppi
FROM aiemmin_hankitun_ammat_tutkinnon_osan_naytto
UNION
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('pto_', aiemmin_hankittu_paikallinen_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id,
       module_id AS module_id,
       'pto' AS tyyppi
FROM public.aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
UNION
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('yto_', aiemmin_hankittu_yhteinen_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id,
       module_id AS module_id,
       'yto' AS tyyppi
FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
UNION
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('yto_alue_', aiemmin_hankittu_yto_osa_alue_id) AS osa_id,
       osaamisen_osoittaminen_id,
       module_id AS module_id,
       'yto_alue' AS tyyppi
FROM aiemmin_hankitun_yto_osa_alueen_naytto;


CREATE OR REPLACE VIEW v_aiemmin_hankitun_tutkinnon_osan_arvioijat AS
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('pto_', aiemmin_hankittu_paikallinen_tutkinnon_osa_id) AS osa_id,
       koulutuksen_jarjestaja_osaamisen_arvioija_id AS koulutuksen_jarjestaja_osaamisen_arvioija_id,
       'pto' AS tyyppi
FROM aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
UNION
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('yto_', aiemmin_hankittu_yhteinen_tutkinnon_osa_id) AS osa_id,
       koulutuksen_jarjestaja_osaamisen_arvioija_id AS koulutuksen_jarjestaja_osaamisen_arvioija_id,
       'yto' AS tyyppi
FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat;


CREATE OR REPLACE VIEW v_hankittavan_tutkinnon_osan_naytto AS
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('ato_', hankittava_ammat_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id AS osaamisen_osoittaminen_id,
       module_id AS module_id,
       'ato' AS tyyppi
FROM hankittavan_ammat_tutkinnon_osan_naytto
UNION
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('pto_', hankittava_paikallinen_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id AS osaamisen_osoittaminen_id,
       module_id AS module_id,
       'pto' AS tyyppi
FROM hankittavan_paikallisen_tutkinnon_osan_naytto
UNION
SELECT created_at AS created_at,
       deleted_at AS deleted_at,
       concat('yto_alue_', yhteisen_tutkinnon_osan_osa_alue_id) AS osa_id,
       osaamisen_osoittaminen_id AS osaamisen_osoittaminen_id,
       module_id AS module_id,
       'yto_alue' AS tyyppi
FROM yhteisen_tutkinnon_osan_osa_alueen_naytot;


CREATE OR REPLACE FUNCTION refresh_reporting (target_schema TEXT) RETURNS BOOLEAN AS
$BODY$
DECLARE tbl RECORD;
BEGIN
    IF lower(target_schema) = 'public' THEN
        RETURN false;
    ELSE
        EXECUTE format('DROP SCHEMA IF EXISTS %I CASCADE', target_schema);
        EXECUTE format('CREATE SCHEMA %I', target_schema);
        FOR tbl IN
            SELECT *
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_type = 'BASE TABLE'
              AND table_name NOT IN ('flyway_schema_history',
                                     'aiemmin_hankitun_ammat_tutkinnon_osan_naytto',
                                     'aiemmin_hankitut_ammat_tutkinnon_osat',
                                     'aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat',
                                     'aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto',
                                     'aiemmin_hankitut_paikalliset_tutkinnon_osat',
                                     'aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat',
                                     'aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto',
                                     'aiemmin_hankitut_yhteiset_tutkinnon_osat',
                                     'aiemmin_hankitun_yto_osa_alueen_naytto',
                                     'aiemmin_hankitut_yto_osa_alueet',
                                     'hankittavan_paikallisen_tutkinnon_osan_naytto',
                                     'hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat',
                                     'hankittavat_paikalliset_tutkinnon_osat',
                                     'hankittavan_ammat_tutkinnon_osan_naytto',
                                     'hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat',
                                     'hankittavat_ammat_tutkinnon_osat',
                                     'hankittavat_yhteiset_tutkinnon_osat',
                                     'osaamisen_hankkimistavat',
                                     'sessions',
                                     'yhteisen_tutkinnon_osan_osa_alueen_naytot',
                                     'yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat',
                                     'yhteisen_tutkinnon_osan_osa_alueet')
            LOOP
                EXECUTE format('CREATE TABLE %I.%I AS SELECT * FROM public.%I', target_schema, tbl.table_name, tbl.table_name);
            END LOOP;
        EXECUTE format('CREATE TABLE %I.aiemmin_hankitut_tutkinnon_osat AS SELECT * FROM public.v_aiemmin_hankitut_tutkinnon_osat', target_schema);
        EXECUTE format('CREATE TABLE %I.hankittavat_tutkinnon_osat AS SELECT * FROM public.v_hankittavat_tutkinnon_osat', target_schema);
        EXECUTE format('CREATE TABLE %I.osaamisen_hankkimistavat AS SELECT * FROM public.v_osaamisen_hankkimistavat', target_schema);
        EXECUTE format('CREATE TABLE %I.aiemmin_hankitun_tutkinnon_osan_naytto AS SELECT * FROM public.v_aiemmin_hankitun_tutkinnon_osan_naytto', target_schema);
        EXECUTE format('CREATE TABLE %I.aiemmin_hankitun_tutkinnon_osan_arvioijat AS SELECT * FROM public.v_aiemmin_hankitun_tutkinnon_osan_arvioijat', target_schema);
        EXECUTE format('CREATE TABLE %I.hankittavan_tutkinnon_osan_naytto AS SELECT * FROM public.v_hankittavan_tutkinnon_osan_naytto', target_schema);
        RETURN true;
    END IF;
END;
$BODY$
LANGUAGE plpgsql;
