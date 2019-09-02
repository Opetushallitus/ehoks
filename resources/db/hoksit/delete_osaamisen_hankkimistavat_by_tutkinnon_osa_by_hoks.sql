  DELETE FROM osaamisen_hankkimistavat WHERE id IN
    (SELECT h.osaamisen_hankkimistapa_id FROM
      :tutkinnon-osa-hankkimistapa-table as h
    INNER JOIN :tutkinnon-osa-table as t
    ON (t.id = h.:tutkinnon-osa-id AND t.hoks_id = ?))
    RETURNING id
