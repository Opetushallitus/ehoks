 DELETE FROM tyopaikalla_jarjestettavat_koulutukset WHERE id IN (
  SELECT o.tyopaikalla_jarjestettava_koulutus_id FROM osaamisen_hankkimistavat AS o
  INNER JOIN :tutkinnon-osa-hankkimistapa-table AS h
  ON (h.osaamisen_hankkimistapa_id = o.id)
  INNER JOIN :tutkinnon-osa-table AS t
  ON (t.hoks_id = ? AND t.id = h.:tutkinnon-osa-id))
  RETURNING id
