SELECT
  h.id AS hoks_id,
  h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
  oh.id AS hankkimistapa_id,
  oh.yksiloiva_tunniste AS yksiloiva_tunniste,
  oh.alku AS jakso_alkupvm,
  oh.loppu AS jakso_loppupvm,
  oh.osa_aikaisuustieto AS osa_aikaisuus
FROM hoksit h
  LEFT OUTER JOIN hankittavat_yhteiset_tutkinnon_osat AS osat
    ON (h.id = osat.hoks_id AND osat.deleted_at IS NULL)
  LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueet AS osa
    ON (osat.id = osa.yhteinen_tutkinnon_osa_id AND osa.deleted_at IS NULL)
  LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat AS ytooh
    ON (osa.id = ytooh.yhteisen_tutkinnon_osan_osa_alue_id)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (ytooh.osaamisen_hankkimistapa_id = oh.id)
WHERE
  (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' OR
  oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
  AND h.oppija_oid = ?
  AND oh.alku <= ?
  AND oh.loppu >= ?
