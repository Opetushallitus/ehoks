SELECT h.id AS hoks_id,
       oo.oid AS opiskeluoikeus_oid,
       oo.koulutustoimija_oid
FROM hoksit h
LEFT OUTER JOIN opiskeluoikeudet oo
                ON h.opiskeluoikeus_oid = oo.oid
WHERE oo.koulutustoimija_oid = ?
  AND oo.koski404 = true
  AND h.deleted_at IS NULL;
