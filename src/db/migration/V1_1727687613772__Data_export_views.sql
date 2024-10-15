CREATE OR REPLACE VIEW v_hankittavat_tutkinnon_osat AS
SELECT concat('yto_', id)         AS id,
       created_at                 AS created_at,
       updated_at                 AS updated_at,
       deleted_at                 AS deleted_at,
       hoks_id                    AS hoks_id,
       'yto'                      AS tyyppi,
       tutkinnon_osa_koodi_uri    AS koodi_uri,
       tutkinnon_osa_koodi_versio AS koodi_versio,
       koulutuksen_jarjestaja_oid AS koulutuksen_jarjestaja_oid,
       module_id                  AS module_id,
       NULL                       AS yto_id,
       NULL                       AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       NULL                       AS olennainen_seikka,
       NULL                       AS opetus_ja_ohjaus_maara,
       NULL::integer              AS laajuus,
       NULL                       AS nimi,
       NULL                       AS tavoitteet_ja_sisallot,
       NULL                       AS amosaa_tunniste
FROM hankittavat_yhteiset_tutkinnon_osat
UNION
SELECT concat('yto_alue_', id)                     AS id,
       created_at                                  AS created_at,
       updated_at                                  AS updated_at,
       deleted_at                                  AS deleted_at,
       NULL                                        AS hoks_id,
       'yto_alue'                                  AS tyyppi,
       osa_alue_koodi_uri                          AS koodi_uri,
       osa_alue_koodi_versio                       AS koodi_versio,
       koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       module_id                                   AS module_id,
       concat('yto_', yhteinen_tutkinnon_osa_id)   AS yto_id,
       vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       olennainen_seikka                           AS olennainen_seikka,
       opetus_ja_ohjaus_maara                      AS opetus_ja_ohjaus_maara,
       NULL::integer                               AS laajuus,
       NULL                                        AS nimi,
       NULL                                        AS tavoitteet_ja_sisallot,
       NULL                                        AS amosaa_tunniste
FROM yhteisen_tutkinnon_osan_osa_alueet
UNION
SELECT concat('ato_', id)                          AS id,
       created_at                                  AS created_at,
       updated_at                                  AS updated_at,
       deleted_at                                  AS deleted_at,
       hoks_id                                     AS hoks_id,
       'ato'                                       AS tyyppi,
       tutkinnon_osa_koodi_uri                     AS koodi_uri,
       tutkinnon_osa_koodi_versio                  AS koodi_versio,
       koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       module_id                                   AS module_id,
       NULL                                        AS yto_id,
       vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       olennainen_seikka                           AS olennainen_seikka,
       opetus_ja_ohjaus_maara                      AS opetus_ja_ohjaus_maara,
       NULL::integer                               AS laajuus,
       NULL                                        AS nimi,
       NULL                                        AS tavoitteet_ja_sisallot,
       NULL                                        AS amosaa_tunniste
FROM hankittavat_ammat_tutkinnon_osat
UNION
SELECT concat('pto_', id)                          AS id,
       created_at                                  AS created_at,
       updated_at                                  AS updated_at,
       deleted_at                                  AS deleted_at,
       hoks_id                                     AS hoks_id,
       'pto'                                       AS tyyppi,
       NULL                                        AS koodi_uri,
       NULL                                        AS koodi_versio,
       koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       module_id                                   AS module_id,
       NULL                                        AS yto_id,
       vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       olennainen_seikka                           AS olennainen_seikka,
       opetus_ja_ohjaus_maara                      AS opetus_ja_ohjaus_maara,
       laajuus                                     AS laajuus,
       nimi                                        AS nimi,
       tavoitteet_ja_sisallot                      AS tavoitteet_ja_sisallot,
       amosaa_tunniste                             AS amosaa_tunniste
FROM hankittavat_paikalliset_tutkinnon_osat;


CREATE OR REPLACE VIEW v_osaamisen_hankkimistavat AS
SELECT oh.id                                   AS id,
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
       oh.yksiloiva_tunniste                   AS yksiloiva_tunniste,
       oh.tyopaikalla_jarjestettava_koulutus_id AS tyopaikalla_jarjestettava_koulutus_id
FROM osaamisen_hankkimistavat oh
         JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat ytooaoh
              on oh.id = ytooaoh.osaamisen_hankkimistapa_id
         JOIN yhteisen_tutkinnon_osan_osa_alueet ytooa
              on ytooaoh.yhteisen_tutkinnon_osan_osa_alue_id = ytooa.id
         JOIN hankittavat_yhteiset_tutkinnon_osat yto
              on ytooa.yhteinen_tutkinnon_osa_id = yto.id
UNION
SELECT oh.id                                   AS id,
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
       oh.yksiloiva_tunniste                   AS yksiloiva_tunniste,
       oh.tyopaikalla_jarjestettava_koulutus_id AS tyopaikalla_jarjestettava_koulutus_id
FROM osaamisen_hankkimistavat oh
         JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat atooh
              ON oh.id = atooh.osaamisen_hankkimistapa_id
         JOIN hankittavat_ammat_tutkinnon_osat ato
              on atooh.hankittava_ammat_tutkinnon_osa_id = ato.id
UNION
SELECT oh.id                                   AS id,
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
       oh.yksiloiva_tunniste                   AS yksiloiva_tunniste,
       oh.tyopaikalla_jarjestettava_koulutus_id AS tyopaikalla_jarjestettava_koulutus_id
FROM osaamisen_hankkimistavat oh
         JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat ptooh
              ON oh.id = ptooh.osaamisen_hankkimistapa_id
         JOIN hankittavat_paikalliset_tutkinnon_osat pto
              on ptooh.hankittava_paikallinen_tutkinnon_osa_id = pto.id;


CREATE OR REPLACE VIEW v_aiemmin_hankitut_tutkinnon_osat AS
SELECT concat('yto_', id)                         AS id,
       created_at                                 AS created_at,
       updated_at                                 AS updated_at,
       deleted_at                                 AS deleted_at,
       hoks_id                                    AS hoks_id,
       'yto'                                      AS tyyppi,
       tutkinnon_osa_koodi_uri                    AS koodi_uri,
       tutkinnon_osa_koodi_versio                 AS koodi_versio,
       koulutuksen_jarjestaja_oid                 AS koulutuksen_jarjestaja_oid,
       valittu_todentamisen_prosessi_koodi_uri    AS valittu_todentamisen_prosessi_koodi_uri,
       valittu_todentamisen_prosessi_koodi_versio AS valittu_todentamisen_prosessi_koodi_versio,
       lahetetty_arvioitavaksi                    AS lahetetty_arvioitavaksi,
       tarkentavat_tiedot_osaamisen_arvioija_id   AS tarkentavat_tiedot_osaamisen_arvioija_id,
       module_id                                  AS module_id,
       NULL                                       AS yto_id,
       NULL                                       AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       NULL                                       AS olennainen_seikka,
       NULL::integer                              AS laajuus,
       NULL                                       AS nimi,
       NULL                                       AS tavoitteet_ja_sisallot,
       NULL                                       AS amosaa_tunniste
FROM aiemmin_hankitut_yhteiset_tutkinnon_osat
UNION
SELECT concat('yto_alue_', id)                                    AS id,
       created_at                                                 AS created_at,
       updated_at                                                 AS updated_at,
       deleted_at                                                 AS deleted_at,
       NULL                                                       AS hoks_id,
       'yto_alue'                                                 AS tyyppi,
       osa_alue_koodi_uri                                         AS koodi_uri,
       osa_alue_koodi_versio                                      AS koodi_versio,
       koulutuksen_jarjestaja_oid                                 AS koulutuksen_jarjestaja_oid,
       valittu_todentamisen_prosessi_koodi_uri                    AS valittu_todentamisen_prosessi_koodi_uri,
       valittu_todentamisen_prosessi_koodi_versio                 AS valittu_todentamisen_prosessi_koodi_versio,
       NULL                                                       AS lahetetty_arvioitavaksi,
       tarkentavat_tiedot_osaamisen_arvioija_id                   AS tarkentavat_tiedot_osaamisen_arvioija_id,
       module_id                                                  AS module_id,
       concat('yto_', aiemmin_hankittu_yhteinen_tutkinnon_osa_id) AS yto_id,
       vaatimuksista_tai_tavoitteista_poikkeaminen                AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       olennainen_seikka                                          AS olennainen_seikka,
       NULL::integer                                              AS laajuus,
       NULL                                                       AS nimi,
       NULL                                                       AS tavoitteet_ja_sisallot,
       NULL                                                       AS amosaa_tunniste
FROM aiemmin_hankitut_yto_osa_alueet
UNION
SELECT concat('ato_', id)                         AS id,
       created_at                                 AS created_at,
       updated_at                                 AS updated_at,
       deleted_at                                 AS deleted_at,
       hoks_id                                    AS hoks_id,
       'ato'                                      AS tyyppi,
       tutkinnon_osa_koodi_uri                    AS koodi_uri,
       tutkinnon_osa_koodi_versio                 AS koodi_versio,
       koulutuksen_jarjestaja_oid                 AS koulutuksen_jarjestaja_oid,
       valittu_todentamisen_prosessi_koodi_uri    AS valittu_todentamisen_prosessi_koodi_uri,
       valittu_todentamisen_prosessi_koodi_versio AS valittu_todentamisen_prosessi_koodi_versio,
       NULL                                       AS lahetetty_arvioitavaksi,
       tarkentavat_tiedot_osaamisen_arvioija_id   AS tarkentavat_tiedot_osaamisen_arvioija_id,
       module_id                                  AS module_id,
       NULL                                       AS yto_id,
       NULL                                       AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       olennainen_seikka                          AS olennainen_seikka,
       NULL::integer                              AS laajuus,
       NULL                                       AS nimi,
       NULL                                       AS tavoitteet_ja_sisallot,
       NULL                                       AS amosaa_tunniste
FROM aiemmin_hankitut_ammat_tutkinnon_osat
UNION
SELECT concat('pto_', id)                          AS id,
       created_at                                  AS created_at,
       updated_at                                  AS updated_at,
       deleted_at                                  AS deleted_at,
       hoks_id                                     AS hoks_id,
       'pto'                                       AS tyyppi,
       NULL                                        AS koodi_uri,
       NULL                                        AS koodi_versio,
       koulutuksen_jarjestaja_oid                  AS koulutuksen_jarjestaja_oid,
       valittu_todentamisen_prosessi_koodi_uri     AS valittu_todentamisen_prosessi_koodi_uri,
       valittu_todentamisen_prosessi_koodi_versio  AS valittu_todentamisen_prosessi_koodi_versio,
       lahetetty_arvioitavaksi                     AS lahetetty_arvioitavaksi,
       tarkentavat_tiedot_osaamisen_arvioija_id    AS tarkentavat_tiedot_osaamisen_arvioija_id,
       module_id                                   AS module_id,
       NULL                                        AS yto_id,
       vaatimuksista_tai_tavoitteista_poikkeaminen AS vaatimuksista_tai_tavoitteista_poikkeaminen,
       olennainen_seikka                           AS olennainen_seikka,
       laajuus                                     AS laajuus,
       nimi                                        AS nimi,
       tavoitteet_ja_sisallot                      AS tavoitteet_ja_sisallot,
       amosaa_tunniste                             AS amosaa_tunniste
FROM aiemmin_hankitut_paikalliset_tutkinnon_osat;


CREATE OR REPLACE VIEW v_aiemmin_hankitun_tutkinnon_osan_naytto AS
SELECT created_at                                              AS created_at,
       deleted_at                                              AS deleted_at,
       concat('ato_', aiemmin_hankittu_ammat_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id                               AS osaamisen_osoittaminen_id,
       module_id                                               AS module_id,
       'ato'                                                   AS tyyppi
FROM aiemmin_hankitun_ammat_tutkinnon_osan_naytto
UNION
SELECT created_at                                                    AS created_at,
       deleted_at                                                    AS deleted_at,
       concat('pto_', aiemmin_hankittu_paikallinen_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id                                     AS osaamisen_osoittaminen_id,
       module_id                                                     AS module_id,
       'pto'                                                         AS tyyppi
FROM aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
UNION
SELECT created_at                                                 AS created_at,
       deleted_at                                                 AS deleted_at,
       concat('yto_', aiemmin_hankittu_yhteinen_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id                                  AS osaamisen_osoittaminen_id,
       module_id                                                  AS module_id,
       'yto'                                                      AS tyyppi
FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
UNION
SELECT created_at                                            AS created_at,
       deleted_at                                            AS deleted_at,
       concat('yto_alue_', aiemmin_hankittu_yto_osa_alue_id) AS osa_id,
       osaamisen_osoittaminen_id                             AS osaamisen_osoittaminen_id,
       module_id                                             AS module_id,
       'yto_alue'                                            AS tyyppi
FROM aiemmin_hankitun_yto_osa_alueen_naytto;


CREATE OR REPLACE VIEW v_aiemmin_hankitun_tutkinnon_osan_arvioijat AS
SELECT created_at                                                    AS created_at,
       deleted_at                                                    AS deleted_at,
       concat('pto_', aiemmin_hankittu_paikallinen_tutkinnon_osa_id) AS osa_id,
       koulutuksen_jarjestaja_osaamisen_arvioija_id                  AS koulutuksen_jarjestaja_osaamisen_arvioija_id,
       'pto'                                                         AS tyyppi
FROM aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
UNION
SELECT created_at                                                 AS created_at,
       deleted_at                                                 AS deleted_at,
       concat('yto_', aiemmin_hankittu_yhteinen_tutkinnon_osa_id) AS osa_id,
       koulutuksen_jarjestaja_osaamisen_arvioija_id               AS koulutuksen_jarjestaja_osaamisen_arvioija_id,
       'yto'                                                      AS tyyppi
FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat;


CREATE OR REPLACE VIEW v_hankittavan_tutkinnon_osan_naytto AS
SELECT created_at                                        AS created_at,
       deleted_at                                        AS deleted_at,
       concat('ato_', hankittava_ammat_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id                         AS osaamisen_osoittaminen_id,
       module_id                                         AS module_id,
       'ato'                                             AS tyyppi
FROM hankittavan_ammat_tutkinnon_osan_naytto
UNION
SELECT created_at                                              AS created_at,
       deleted_at                                              AS deleted_at,
       concat('pto_', hankittava_paikallinen_tutkinnon_osa_id) AS osa_id,
       osaamisen_osoittaminen_id                               AS osaamisen_osoittaminen_id,
       module_id                                               AS module_id,
       'pto'                                                   AS tyyppi
FROM hankittavan_paikallisen_tutkinnon_osan_naytto
UNION
SELECT created_at                                               AS created_at,
       deleted_at                                               AS deleted_at,
       concat('yto_alue_', yhteisen_tutkinnon_osan_osa_alue_id) AS osa_id,
       osaamisen_osoittaminen_id                                AS osaamisen_osoittaminen_id,
       module_id                                                AS module_id,
       'yto_alue'                                               AS tyyppi
FROM yhteisen_tutkinnon_osan_osa_alueen_naytot;


CREATE OR REPLACE FUNCTION add_reporting_constraints(target_schema TEXT) RETURNS BOOLEAN AS
$BODY$
BEGIN
    IF lower(target_schema) = 'public' THEN
        RETURN false;
    ELSE
        -- Primary keys
        EXECUTE format('ALTER TABLE %I.oppijat ADD CONSTRAINT oppijat_pkey PRIMARY KEY (oid)', target_schema);
        EXECUTE format('ALTER TABLE %I.opiskeluoikeudet ADD CONSTRAINT opiskeluoikeudet_pkey PRIMARY KEY (oid)', target_schema);
        EXECUTE format('ALTER TABLE %I.hoksit ADD CONSTRAINT hoksit_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.aiemmin_hankitut_tutkinnon_osat ADD CONSTRAINT aiemmin_hankitut_tutkinnon_osat_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.hankittavat_tutkinnon_osat ADD CONSTRAINT hankittavat_tutkinnon_osat_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.hankittavat_koulutuksen_osat ADD CONSTRAINT hankittavat_koulutuksen_osat_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_hankkimistavat ADD CONSTRAINT osaamisen_hankkimistavat_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamiset ADD CONSTRAINT osaamisen_osoittamiset_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_yksilolliset_kriteerit ADD CONSTRAINT osaamisen_osoittamisen_yksilolliset_kriteerit_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_sisallot ADD CONSTRAINT osaamisen_osoittamisen_sisallot_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.nayttoymparistot ADD CONSTRAINT nayttoymparistot_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.keskeytymisajanjaksot ADD CONSTRAINT keskeytymisajanjaksot_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.koulutuksen_jarjestaja_osaamisen_arvioijat ADD CONSTRAINT koulutuksen_jarjestaja_osaamisen_arvioijat_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.koodisto_koodit ADD CONSTRAINT koodisto_koodit_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.palautteet ADD CONSTRAINT palautteet_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.muut_oppimisymparistot ADD CONSTRAINT muut_oppimisymparistot_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.tyopaikalla_jarjestettavat_koulutukset ADD CONSTRAINT tyopaikalla_jarjestettavat_koulutukset_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat ADD CONSTRAINT tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.opiskeluvalmiuksia_tukevat_opinnot ADD CONSTRAINT opiskeluvalmiuksia_tukevat_opinnot_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.tyoelama_osaamisen_arvioijat ADD CONSTRAINT tyoelama_osaamisen_arvioijat_pkey PRIMARY KEY (id)', target_schema);
        EXECUTE format('ALTER TABLE %I.todennettu_arviointi_lisatiedot ADD CONSTRAINT todennettu_arviointi_lisatiedot_pkey PRIMARY KEY (id)', target_schema);

        -- Foreign keys
        EXECUTE format('ALTER TABLE %I.opiskeluoikeudet ADD CONSTRAINT oppijat_fkey FOREIGN KEY (oppija_oid) REFERENCES %I.oppijat (oid)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.aiemmin_hankitut_tutkinnon_osat ADD CONSTRAINT hoksit_fkey FOREIGN KEY (hoks_id) REFERENCES %I.hoksit (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.hankittavat_tutkinnon_osat ADD CONSTRAINT hoksit_fkey FOREIGN KEY (hoks_id) REFERENCES %I.hoksit (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.hankittavat_koulutuksen_osat ADD CONSTRAINT hoksit_fkey FOREIGN KEY (hoks_id) REFERENCES %I.hoksit (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_hankkimistavat ADD CONSTRAINT hoksit_fkey FOREIGN KEY (hoks_id) REFERENCES %I.hoksit (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.opiskeluvalmiuksia_tukevat_opinnot ADD CONSTRAINT hoksit_fkey FOREIGN KEY (hoks_id) REFERENCES %I.hoksit (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_hankkimistavat ADD CONSTRAINT tyopaikalla_jarjestettavat_koulutukset_fkey FOREIGN KEY (tyopaikalla_jarjestettava_koulutus_id) REFERENCES %I.tyopaikalla_jarjestettavat_koulutukset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_hankkimistavat ADD CONSTRAINT hankittavat_tutkinnon_osat_fkey FOREIGN KEY (osa_id) REFERENCES %I.hankittavat_tutkinnon_osat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat ADD CONSTRAINT tyopaikalla_jarjestettavat_koulutukset_fkey FOREIGN KEY (tyopaikalla_jarjestettava_koulutus_id) REFERENCES %I.tyopaikalla_jarjestettavat_koulutukset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.aiemmin_hankitun_tutkinnon_osan_arvioijat ADD CONSTRAINT koulutuksen_jarjestaja_osaamisen_arvioijat_fkey FOREIGN KEY (koulutuksen_jarjestaja_osaamisen_arvioija_id) REFERENCES %I.koulutuksen_jarjestaja_osaamisen_arvioijat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.aiemmin_hankitun_tutkinnon_osan_arvioijat ADD CONSTRAINT aiemmin_hankitut_tutkinnon_osat_fkey FOREIGN KEY (osa_id) REFERENCES %I.aiemmin_hankitut_tutkinnon_osat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.aiemmin_hankitun_tutkinnon_osan_naytto ADD CONSTRAINT aiemmin_hankitut_tutkinnon_osat_fkey FOREIGN KEY (osa_id) REFERENCES %I.aiemmin_hankitut_tutkinnon_osat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.aiemmin_hankitun_tutkinnon_osan_naytto ADD CONSTRAINT osaamisen_osoittamiset_fkey FOREIGN KEY (osaamisen_osoittaminen_id) REFERENCES %I.osaamisen_osoittamiset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.hankittavan_tutkinnon_osan_naytto ADD CONSTRAINT osaamisen_osoittamiset_fkey FOREIGN KEY (osaamisen_osoittaminen_id) REFERENCES %I.osaamisen_osoittamiset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.hankittavan_tutkinnon_osan_naytto ADD CONSTRAINT hankittavat_tutkinnon_osat_fkey FOREIGN KEY (osa_id) REFERENCES %I.hankittavat_tutkinnon_osat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamiset ADD CONSTRAINT nayttoymparistot_fkey FOREIGN KEY (nayttoymparisto_id) REFERENCES %I.nayttoymparistot (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_osa_alueet ADD CONSTRAINT koodisto_koodit_fkey FOREIGN KEY (koodisto_koodi_id) REFERENCES %I.koodisto_koodit (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_osa_alueet ADD CONSTRAINT osaamisen_osoittamiset_fkey FOREIGN KEY (osaamisen_osoittaminen_id) REFERENCES %I.osaamisen_osoittamiset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_tyoelama_arvioija ADD CONSTRAINT osaamisen_osoittamiset_fkey FOREIGN KEY (osaamisen_osoittaminen_id) REFERENCES %I.osaamisen_osoittamiset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_tyoelama_arvioija ADD CONSTRAINT tyoelama_osaamisen_arvioijat_fkey FOREIGN KEY (tyoelama_arvioija_id) REFERENCES %I.tyoelama_osaamisen_arvioijat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija ADD CONSTRAINT osaamisen_osoittamiset_fkey FOREIGN KEY (osaamisen_osoittaminen_id) REFERENCES %I.osaamisen_osoittamiset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija ADD CONSTRAINT koulutuksen_jarjestaja_osaamisen_arvioijat_fkey FOREIGN KEY (koulutuksen_jarjestaja_osaamisen_arvioija_id) REFERENCES %I.koulutuksen_jarjestaja_osaamisen_arvioijat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_yksilolliset_kriteerit ADD CONSTRAINT osaamisen_osoittamiset_fkey FOREIGN KEY (osaamisen_osoittaminen_id) REFERENCES %I.osaamisen_osoittamiset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.osaamisen_osoittamisen_sisallot ADD CONSTRAINT osaamisen_osoittamiset_fkey FOREIGN KEY (osaamisen_osoittaminen_id) REFERENCES %I.osaamisen_osoittamiset (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.keskeytymisajanjaksot ADD CONSTRAINT osaamisen_hankkimistavat_fkey FOREIGN KEY (osaamisen_hankkimistapa_id) REFERENCES %I.osaamisen_hankkimistavat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.muut_oppimisymparistot ADD CONSTRAINT osaamisen_hankkimistavat_fkey FOREIGN KEY (osaamisen_hankkimistapa_id) REFERENCES %I.osaamisen_hankkimistavat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.todennettu_arviointi_arvioijat ADD CONSTRAINT todennettu_arviointi_lisatiedot_fkey FOREIGN KEY (todennettu_arviointi_lisatiedot_id) REFERENCES %I.todennettu_arviointi_lisatiedot (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.todennettu_arviointi_arvioijat ADD CONSTRAINT koulutuksen_jarjestaja_osaamisen_arvioijat_fkey FOREIGN KEY (koulutuksen_jarjestaja_osaamisen_arvioija_id) REFERENCES %I.koulutuksen_jarjestaja_osaamisen_arvioijat (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.palaute_viestit ADD CONSTRAINT palautteet_fkey FOREIGN KEY (palaute_id) REFERENCES %I.palautteet (id)', target_schema, target_schema);
        EXECUTE format('ALTER TABLE %I.palaute_tapahtumat ADD CONSTRAINT palautteet_fkey FOREIGN KEY (palaute_id) REFERENCES %I.palautteet (id)', target_schema, target_schema);

        RETURN true;
    END IF;
END;
$BODY$
    LANGUAGE plpgsql;


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

        SELECT add_reporting_constraints(target_schema);

        RETURN true;
    END IF;
END;
$BODY$
LANGUAGE plpgsql;
