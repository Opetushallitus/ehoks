DELETE FROM osaamisen_hankkimistavat WHERE id IN
(SELECT osaamisen_hankkimistapa_id
FROM :yto-osa-alue-hankkimistapa-table AS h
LEFT OUTER JOIN :yto-osa-alueet-table AS y
ON
(h.:yto-osa-alue-id = y.id AND y.:tutkinnon-osa-id IN
(SELECT id
  FROM :tutkinnon-osa-table AS t WHERE t.hoks_id = ?)))
  RETURNING id
