SELECT t.* FROM :table AS t
  LEFT OUTER JOIN :join AS h
    ON (h.:secondary-column = t.:primary-column AND h.deleted_at IS NULL)
  WHERE h.:column = ? AND t.deleted_at IS NULL
