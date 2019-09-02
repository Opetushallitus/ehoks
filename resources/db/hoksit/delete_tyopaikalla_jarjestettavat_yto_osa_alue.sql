DELETE FROM tyopaikalla_jarjestettavat_koulutukset WHERE id IN (
  SELECT o.tyopaikalla_jarjestettava_koulutus_id FROM osaamisen_hankkimistavat AS o
  INNER JOIN :yto-osa-alue-hankkimistapa-table AS y
  ON (y.osaamisen_hankkimistapa_id = o.id)
  INNER JOIN :yto-osa-alueet-table AS a
  ON (a.id = y.:yto-osa-alue-id)
  INNER JOIN :tutkinnon-osa-table AS t
  ON (t.id = a.:tutkinnon-osa-id AND t.hoks_id = ?))
 RETURNING id
