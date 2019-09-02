DELETE FROM osaamisen_hankkimistavat WHERE id IN
(SELECT y.osaamisen_hankkimistapa_id FROM
  :yto-osa-alue-hankkimistapa-table AS y
  INNER JOIN :yto-osa-alueet-table as t
  ON (t.id = y.:yto-osa-alue-id)
  INNER JOIN :tutkinnon-osa-table AS o
  ON (o.id = t.:tutkinnon-osa-id AND o.hoks_id = ?))
  RETURNING id  
