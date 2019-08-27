DELETE FROM nayttoymparistot WHERE id IN
(SELECT n.osaamisen_osoittaminen_id
 FROM :tutkinnon-osa-naytto-table AS n
  LEFT OUTER JOIN :tutkinnon-osa-table AS t
  ON
  (n.:tutkinnon-osa-id = t.id AND t.hoks_id = ?))
   RETURNING id
