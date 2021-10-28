SELECT
  hoksit.*
FROM hoksit h
  LEFT OUTER JOIN kyselylinkit AS k
    ON (h.id = k.hoks_id AND k.tyyppi = 'aloittaneet')
WHERE
  h.deleted_at IS NULL
  AND h.osaamisen_hankkimisen_tarve = true
  AND k.kyselylinkki IS NULL
  AND h.created_at >= ?
  AND h.created_at <= ?
LIMIT ?
