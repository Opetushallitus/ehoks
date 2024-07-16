CREATE VIEW tep_palaute AS
  WITH tyopaikkajaksot AS (
    SELECT
      osa.hoks_id,
      'hato' AS tyyppi,
      osa.id AS tutkinnonosa_id,
      osa.tutkinnon_osa_koodi_uri AS tutkinnonosa_koodi,
      NULL AS tutkinnonosa_nimi,
      oh.id AS hankkimistapa_id,
      oh.yksiloiva_tunniste,
      oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
      oh.alku AS alkupvm,
      oh.loppu AS loppupvm,
      oh.osa_aikaisuustieto AS osa_aikaisuus,
      oh.oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta,
      tjk.tyopaikan_nimi AS tyopaikan_nimi,
      tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
      tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
      tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS tyopaikkaohjaaja_email,
      tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero as tyopaikkaohjaaja_puhelinnumero,
      oh.tep_kasitelty
    FROM hankittavat_ammat_tutkinnon_osat AS osa
      JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat osaoh ON osa.id = osaoh.hankittava_ammat_tutkinnon_osa_id
      JOIN osaamisen_hankkimistavat oh ON osaoh.osaamisen_hankkimistapa_id = oh.id
      JOIN tyopaikalla_jarjestettavat_koulutukset tjk ON oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id
    WHERE oh.osaamisen_hankkimistapa_koodi_uri IN ('osaamisenhankkimistapa_koulutussopimus', 'osaamisenhankkimistapa_oppisopimus')
      AND osa.deleted_at IS NULL
      AND osaoh.deleted_at IS NULL
      AND oh.deleted_at IS NULL
      AND tjk.deleted_at IS NULL
    UNION ALL
    SELECT
      osa.hoks_id,
      'hpto' AS tyyppi,
      osa.id AS tutkinnonosa_id,
      NULL AS tutkinnonosa_koodi,
      osa.nimi AS tutkinnonosa_nimi,
      oh.id AS hankkimistapa_id,
      oh.yksiloiva_tunniste,
      oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
      oh.alku AS alkupvm,
      oh.loppu AS loppupvm,
      oh.osa_aikaisuustieto AS osa_aikaisuus,
      oh.oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta,
      tjk.tyopaikan_nimi AS tyopaikan_nimi,
      tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
      tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
      tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS tyopaikkaohjaaja_email,
      tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS tyopaikkaohjaaja_puhelinnumero,
      oh.tep_kasitelty
    FROM hankittavat_paikalliset_tutkinnon_osat AS osa
      JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat osaoh ON osa.id = osaoh.hankittava_paikallinen_tutkinnon_osa_id
      JOIN osaamisen_hankkimistavat oh ON osaoh.osaamisen_hankkimistapa_id = oh.id
      JOIN tyopaikalla_jarjestettavat_koulutukset tjk ON oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id
    WHERE oh.osaamisen_hankkimistapa_koodi_uri IN ('osaamisenhankkimistapa_koulutussopimus', 'osaamisenhankkimistapa_oppisopimus')
      AND osa.deleted_at IS NULL
      AND osaoh.deleted_at IS NULL
      AND oh.deleted_at IS NULL
      AND tjk.deleted_at IS NULL
    UNION ALL
    SELECT
      osa.hoks_id,
      'hyto' AS tyyppi,
      alue.id AS tutkinnonosa_id,
      osa.tutkinnon_osa_koodi_uri AS tutkinnonosa_koodi,
      NULL AS tutkinnonosa_nimi,
      oh.id AS hankkimistapa_id,
      oh.yksiloiva_tunniste,
      oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
      oh.alku AS alkupvm,
      oh.loppu AS loppupvm,
      oh.osa_aikaisuustieto AS osa_aikaisuus,
      oh.oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta,
      tjk.tyopaikan_nimi AS tyopaikan_nimi,
      tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
      tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
      tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS tyopaikkaohjaaja_email,
      tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS tyopaikkaohjaaja_puhelinnumero,
      oh.tep_kasitelty
    FROM hankittavat_yhteiset_tutkinnon_osat AS osa
      JOIN yhteisen_tutkinnon_osan_osa_alueet alue ON osa.id = alue.yhteinen_tutkinnon_osa_id
      JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat alueoh ON alue.id = alueoh.yhteisen_tutkinnon_osan_osa_alue_id
      JOIN osaamisen_hankkimistavat oh ON alueoh.osaamisen_hankkimistapa_id = oh.id
      JOIN tyopaikalla_jarjestettavat_koulutukset tjk ON oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id
    WHERE oh.osaamisen_hankkimistapa_koodi_uri IN ('osaamisenhankkimistapa_koulutussopimus', 'osaamisenhankkimistapa_oppisopimus')
      AND osa.deleted_at IS NULL
      AND alue.deleted_at IS NULL
      AND alueoh.deleted_at IS NULL
      AND oh.deleted_at IS NULL
      AND tjk.deleted_at IS NULL
  )
  SELECT
    p.id,
    p.hoks_id,
    p.heratepvm,
    p.arvo_tunniste,
    p.tila,
    p.kyselytyyppi,
    p.jakson_yksiloiva_tunniste,
    h.opiskeluoikeus_oid,
    h.oppija_oid,
    t.tyyppi,
    t.tutkinnonosa_id,
    t.tutkinnonosa_koodi,
    t.tutkinnonosa_nimi,
    t.hankkimistapa_id,
    t.hankkimistapa_tyyppi,
    t.alkupvm,
    t.loppupvm,
    t.osa_aikaisuus,
    t.oppisopimuksen_perusta,
    t.tyopaikan_nimi,
    t.tyopaikan_ytunnus,
    t.tyopaikkaohjaaja_nimi,
    t.tyopaikkaohjaaja_email,
    t.tyopaikkaohjaaja_puhelinnumero,
    t.tep_kasitelty
  FROM palautteet p
    JOIN hoksit h ON h.id = p.hoks_id
    JOIN tyopaikkajaksot t ON t.hoks_id = p.hoks_id AND t.yksiloiva_tunniste = p.jakson_yksiloiva_tunniste
  WHERE p.kyselytyyppi = 'tyopaikkajakson_suorittaneet'
    AND p.deleted_at IS NULL
