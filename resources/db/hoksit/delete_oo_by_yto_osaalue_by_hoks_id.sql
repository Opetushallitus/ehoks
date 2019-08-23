DELETE FROM osaamisen_osoittamiset WHERE id IN (
SELECT y.osaamisen_osoittaminen_id
  FROM :yto-osa-alue-naytto-table AS y
  LEFT OUTER JOIN :yto-osa-alueet-table AS o
  ON
  (y.:yto-osa-alue-id = o.id
    AND o.:tutkinnon-osa-id IN
  (SELECT id
   FROM :tutkinnon-osa-table AS t WHERE t.hoks_id = ?)))
   RETURNING id
