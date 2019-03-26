SELECT * FROM {{table_name}}
  WHERE hoks_id = ? AND deleted_at IS NULL
