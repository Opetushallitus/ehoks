SELECT * FROM {{table_name}}
  WHERE {{column_name}} = ? AND deleted_at IS NULL
