SELECT * FROM {{table_name}}
  WHERE id = ? AND deleted_at IS NULL
