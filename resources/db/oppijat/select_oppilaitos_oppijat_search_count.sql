SELECT
  COUNT(oo.oid) AS count
FROM oppijat AS o
       LEFT OUTER JOIN opiskeluoikeudet AS oo
                       ON (o.oid = oo.oppija_oid)
       INNER JOIN hoksit AS h
                       ON (oo.oid = h.opiskeluoikeus_oid)
WHERE oo.oppilaitos_oid = ?
  AND (?::text[] IS NULL OR o.nimi ILIKE ALL (?::text[]))
  AND (?::text IS NULL OR oo.tutkinto_nimi->>? ILIKE ?::text)
  AND (?::text IS NULL OR oo.osaamisala_nimi->>? ILIKE ?::text)
  AND (?::int IS NULL OR h.id = ?::int)
  AND h.deleted_at IS NULL
