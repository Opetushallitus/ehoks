SELECT
  hoksit.*
FROM hoksit h
  LEFT OUTER JOIN kyselylinkit AS k
    ON (h.id = k.hoks_id AND (k.tyyppi = 'tutkinnon_suorittaneet'
                              OR k.tyyppi = 'tutkinnon_osia_suorittaneet'))
WHERE
  h.deleted_at IS NULL
  AND h.osaamisen_saavuttamisen_pvm IS NOT NULL
  AND k.kyselylinkki IS NULL
  AND h.created_at >= ?
  AND h.created_at <= ?
LIMIT ?
