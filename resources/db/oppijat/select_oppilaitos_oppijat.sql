SELECT o.oid,
       o.nimi,
       oo.oid AS opiskeluoikeus_oid,
       oo.tutkinto_nimi,
       oo.osaamisala_nimi,
       h.id AS hoks_id
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
ORDER BY CASE ?
		WHEN 'nimi_asc' THEN o.nimi
		WHEN 'tutkinto_asc' THEN oo.tutkinto_nimi->>?
		WHEN 'osaamisala_asc' THEN oo.osaamisala_nimi->>?
	END ASC,
	CASE ?
		WHEN 'nimi_desc' THEN o.nimi
		WHEN 'tutkinto_desc' THEN oo.tutkinto_nimi->>?
		WHEN 'osaamisala_desc' THEN oo.osaamisala_nimi->>?
	END DESC
LIMIT ?
OFFSET ?
