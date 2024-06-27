SELECT
  h.id AS "hoksId",
  h.opiskeluoikeus_oid AS "opiskeluoikeusOid",
  h.oppija_oid AS "oppijaOid",
  h.eid AS "hoksEid",
  oh.osaamisen_hankkimistapa_koodi_uri AS "hankkimistapaTyyppi",
  oh.alku AS alkupvm,
  oh.loppu AS loppupvm,
  oh.osa_aikaisuustieto AS "osaAikaisuus",
  oh.oppisopimuksen_perusta_koodi_uri AS "oppisopimuksenPerusta",
  oo.oppilaitos_oid AS "oppilaitosOid",
  tjk.tyopaikan_nimi AS "tyopaikanNimi",
  tjk.tyopaikan_y_tunnus AS ytunnus,
  tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS "ohjaajaNimi",
  tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS "ohjaajaEmail",
  tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS "ohjaajaPuhelinnumero"
  /* osa.tutkinnon_osa_koodi_uri AS "tutkinnonOsaKoodiUri" */
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
  AND h.deleted_at IS NULL
  AND oh.osa_aikaisuustieto IS NULL
  AND oo.tutkinto_nimi @> ? ::jsonb
  AND oh.loppu >= ?
  AND oh.loppu <= ?
