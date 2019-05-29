SELECT DISTINCT oppija_oid
  FROM hoksit AS h
    LEFT JOIN oppijat AS o
      ON h.oppija_oid = o.oid
        WHERE o.oid IS NULL