SELECT
  *
FROM
  amisherate_kasittelytilat
WHERE
  hoks_id = ?
  AND deleted_at IS NULL
