SELECT COUNT(*)
  FROM hoksit AS h
    LEFT JOIN opiskeluoikeudet AS o
      ON h.opiskeluoikeus_oid = o.oid
        WHERE o.oid IS NULL