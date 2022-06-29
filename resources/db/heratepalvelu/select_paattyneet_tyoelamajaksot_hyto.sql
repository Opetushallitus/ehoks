SELECT
  h.id AS hoks_id,
  h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
  h.oppija_oid AS oppija_oid,
  osa.id AS tutkinnonosa_id,
  osat.tutkinnon_osa_koodi_uri AS tutkinnonosa_nimi,
  oh.id AS hankkimistapa_id,
  oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
  oh.alku AS alkupvm,
  oh.loppu AS loppupvm,
  oh.osa_aikaisuustieto AS osa_aikaisuus,
  oh.oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta,
  tjk.tyopaikan_nimi AS tyopaikan_nimi,
  tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
  tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
  tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS tyopaikkaohjaaja_email,
  tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS tyopaikkaohjaaja_puhelinnumero
FROM hoksit h
  JOIN hankittavat_yhteiset_tutkinnon_osat AS osat
    ON (h.id = osat.hoks_id)
  JOIN yhteisen_tutkinnon_osan_osa_alueet AS osa
    ON (osat.id = osa.yhteinen_tutkinnon_osa_id)
  JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat AS ytooh
    ON (osa.id = ytooh.yhteisen_tutkinnon_osan_osa_alue_id)
  JOIN osaamisen_hankkimistavat AS oh
    ON (ytooh.osaamisen_hankkimistapa_id = oh.id)
  LEFT JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
WHERE
  oh.osaamisen_hankkimistapa_koodi_uri IN ('osaamisenhankkimistapa_koulutussopimus', 'osaamisenhankkimistapa_oppisopimus')
  AND oh.loppu >= ?
  AND oh.loppu <= ?
  AND oh.tep_kasitelty = false
  AND osa.deleted_at IS NULL
  AND osat.deleted_at IS NULL
LIMIT ?
