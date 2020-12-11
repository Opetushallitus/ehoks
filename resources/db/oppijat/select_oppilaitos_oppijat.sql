SELECT o.oid,
       o.nimi,
       oo.oid AS opiskeluoikeus_oid,
       oo.oppilaitos_oid,
       oo.koulutustoimija_oid,
       oo.tutkinto_nimi,
       oo.osaamisala_nimi
FROM oppijat AS o
       LEFT OUTER JOIN opiskeluoikeudet AS oo
                       ON (o.oid = oo.oppija_oid)
       LEFT OUTER JOIN hoksit AS h
                       ON (oo.oid = h.opiskeluoikeus_oid)
WHERE ((oo.oppilaitos_oid IS NOT NULL AND oo.oppilaitos_oid LIKE ?) OR
       (oo.koulutustoimija_oid IS NOT NULL AND oo.koulutustoimija_oid LIKE ?))
  AND h.opiskeluoikeus_oid IS NOT NULL
  AND o.nimi ILIKE ?
      :tutkinto-filter
      :osaamisala-filter
ORDER BY :order-by-column :desc
LIMIT ?
OFFSET ?
