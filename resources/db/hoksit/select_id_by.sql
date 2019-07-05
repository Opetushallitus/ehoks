SELECT :id FROM :table
  WHERE :column = ? AND deleted_at IS NULL
