SELECT
  h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
  oh.id AS hankkimistapa_id,
  oh.alku AS jakso_alkupvm,
  oh.loppu AS jakso_loppupvm,
  oh.osa_aikaisuustieto AS osa_aikaisuus
FROM hoksit h
  LEFT OUTER JOIN hankittavat_paikalliset_tutkinnon_osat AS osa
    ON (h.id = osa.hoks_id AND osa.deleted_at IS NULL)
  LEFT OUTER JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat AS osajoin
    ON (osa.id = osajoin.hankittava_paikallinen_tutkinnon_osa_id)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (osajoin.osaamisen_hankkimistapa_id = oh.id)
WHERE
  (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' OR
  oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
  AND h.oppija_oid = ?
  AND oh.alku <= ?
  AND oh.loppu >= ?
