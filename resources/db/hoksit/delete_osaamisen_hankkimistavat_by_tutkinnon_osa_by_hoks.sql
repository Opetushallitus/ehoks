DELETE FROM osaamisen_hankkimistavat WHERE id IN
(SELECT osaamisen_hankkimistapa_id
  FROM :tutkinnon-osa-hankkimistapa-table as h
  LEFT OUTER JOIN :tutkinnon-osa-table AS t
  ON
  (h.:tutkinnon-osa-id = t.id AND t.hoks_id = ?))
  RETURNING id
