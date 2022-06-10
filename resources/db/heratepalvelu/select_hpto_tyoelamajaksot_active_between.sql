SELECT
  h.id AS hoks_id,
  h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
  h.oppija_oid AS oppija_oid,
  osa.id AS tutkinnonosa_id,
  osa.nimi AS tutkinnonosa_nimi,
  oh.id AS hankkimistapa_id,
  oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
  oh.alku AS jakso_alkupvm,
  oh.loppu AS jakso_loppupvm,
  oh.osa_aikaisuustieto AS osa_aikaisuus,
  oh.oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta,
  tjk.tyopaikan_nimi AS tyopaikan_nimi,
  tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
  tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
  tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS tyopaikkaohjaaja_email,
  tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS tyopaikkaohjaaja_puhelinnumero
FROM hoksit h
  LEFT OUTER JOIN hankittavat_paikalliset_tutkinnon_osat AS osa
    ON (h.id = osa.hoks_id AND osa.deleted_at IS NULL)
  LEFT OUTER JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat AS osajoin
    ON (osa.id = osajoin.hankittava_paikallinen_tutkinnon_osa_id)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (osajoin.osaamisen_hankkimistapa_id = oh.id)
  LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
WHERE
  (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' OR
  oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
  AND h.oppija_oid = ?
  AND oh.alku <= ?
  AND oh.loppu >= ?
