SELECT * FROM hoksit
  WHERE oppija_oid = ? AND deleted_at IS NULL
  ORDER BY version DESC
