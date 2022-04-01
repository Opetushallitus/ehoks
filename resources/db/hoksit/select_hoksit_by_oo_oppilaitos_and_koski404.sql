SELECT h.id AS hoksId,
       oo.oid AS opiskeluoikeusOid,
       oo.oppilaitos_oid AS oppilaitosOid
FROM hoksit h
LEFT OUTER JOIN opiskeluoikeudet oo
                ON h.opiskeluoikeus_oid = oo.oid
WHERE oo.oppilaitos_oid = ?
  AND oo.koski404 = true
  AND h.deleted_at IS NULL;
