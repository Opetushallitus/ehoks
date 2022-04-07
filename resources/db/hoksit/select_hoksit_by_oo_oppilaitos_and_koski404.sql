SELECT h.id AS hoksId,
       h.oppija_oid AS oppijaoid,
       h.eid AS hokseid,
       oo.oid AS opiskeluoikeusoid,
       oo.oppilaitos_oid AS oppilaitosoid
FROM hoksit h
LEFT OUTER JOIN opiskeluoikeudet oo
                ON h.opiskeluoikeus_oid = oo.oid
WHERE oo.oppilaitos_oid = ?
  AND oo.koski404 = true
  AND h.deleted_at IS NULL;
