SELECT
  h.id AS hoks_id,
  h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
  h.oppija_oid AS oppija_oid,
  h.eid AS hoks_eid,
  oh.osaamisen_hankkimistapa_koodi_uri AS osaamisen_hankkimistapa_koodi_uri,
  oh.osaamisen_hankkimistapa_koodi_versio AS osaamisen_hankkimistapa_koodi_versio,
  oh.alku AS alkupvm,
  oh.loppu AS loppupvm,
  oh.osa_aikaisuustieto AS osa_aikaisuus,
  oh.oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta_koodi_uri,
  oh.oppisopimuksen_perusta_koodi_versio AS oppisopimuksen_perusta_koodi_versio,
  tjk.tyopaikan_nimi AS tyopaikan_nimi,
  tjk.tyopaikan_y_tunnus AS ytunnus,
  tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS ohjaaja_nimi,
  tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS ohjaaja_email,
  tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS ohjaaja_puhelinnumero,
  osa.tutkinnon_osa_koodi_uri AS tutkinnon_osa_koodi_uri,
  osa.tutkinnon_osa_koodi_versio AS tutkinnon_osa_koodi_versio
FROM hoksit h
  LEFT OUTER JOIN hankittavat_ammat_tutkinnon_osat AS osa
    ON (h.id = osa.hoks_id AND osa.deleted_at IS NULL)
  LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat AS osajoin
    ON (osa.id = osajoin.hankittava_ammat_tutkinnon_osa_id)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (osajoin.osaamisen_hankkimistapa_id = oh.id)
  LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id AND oh.deleted_at IS NULL)
  LEFT OUTER JOIN opiskeluoikeudet AS oo
    ON (oo.oid = h.opiskeluoikeus_oid)
WHERE
  (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' or
  oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
  AND oh.osa_aikaisuustieto IS NULL
  AND oo.tutkinto_nimi @> ? ::jsonb
  AND oo.oppilaitos_oid = ?
  AND oh.loppu >= ?
  AND oh.loppu <= ?
ORDER BY h.id desc
