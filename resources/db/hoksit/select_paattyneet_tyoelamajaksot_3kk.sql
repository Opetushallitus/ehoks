SELECT
  oh.id AS hankkimistapa_id,
  tjk.id AS tjk_id
FROM osaamisen_hankkimistavat AS oh
  JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
WHERE
  oh.osaamisen_hankkimistapa_koodi_uri IN ('osaamisenhankkimistapa_koulutussopimus', 'osaamisenhankkimistapa_oppisopimus')
  AND oh.loppu < current_date - interval '3 months'
  AND (tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti IS NOT NULL
    OR tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero IS NOT NULL)
LIMIT ?
