SELECT h.id AS hoks_id,
       oo.oid AS opiskeluoikeus_oid,
       oo.oppilaitos_oid AS oppilaitos_oid
FROM hoksit h
LEFT OUTER JOIN opiskeluoikeudet oo
                ON h.opiskeluoikeus_oid = oo.oid
WHERE oo.oppilaitos_oid = ?
  AND oo.koski404 = true
  AND h.deleted_at IS NULL;
