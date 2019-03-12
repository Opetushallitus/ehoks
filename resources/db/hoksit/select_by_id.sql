SELECT * FROM hoksit
  WHERE id = ? AND deleted_at IS NULL
  ORDER BY version DESC
