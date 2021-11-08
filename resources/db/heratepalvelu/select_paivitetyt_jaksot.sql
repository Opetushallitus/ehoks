SELECT
  oh.oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta
FROM hoksit h
  LEFT OUTER JOIN hankittavat_ammat_tutkinnon_osat AS osa
    ON (h.id = osa.hoks_id AND osa.deleted_at IS NULL)
  LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat AS osajoin
    ON (osa.id = osajoin.hankittava_ammat_tutkinnon_osa_id)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (osajoin.osaamisen_hankkimistapa_id = oh.id)
  LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
WHERE
  h.opiskeluoikeus_oid = ?
  AND tjk.vastuullinen_tyopaikka_ohjaaja_nimi = ?
  AND tjk.tyopaikan_nimi = ?
  AND tjk.tyopaikan_y_tunnus = ?
ORDER BY oh.created_at DESC
