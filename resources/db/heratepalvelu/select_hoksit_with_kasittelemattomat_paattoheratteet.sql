SELECT
  h.*
FROM hoksit h
  LEFT OUTER JOIN amisherate_kasittelytilat AS a
    ON h.id = a.hoks_id
WHERE
  h.deleted_at IS NULL
  AND (h.sahkoposti IS NOT NULL OR h.puhelinnumero IS NOT NULL)
  AND h.osaamisen_saavuttamisen_pvm IS NOT NULL
  AND h.osaamisen_hankkimisen_tarve = true
  AND h.created_at >= ?
  AND h.created_at <= ?
  AND a.paattoherate_kasitelty = false
LIMIT ?
