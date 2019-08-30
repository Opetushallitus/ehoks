 DELETE FROM nayttoymparistot WHERE id IN(
 SELECT nayttoymparisto_id FROM osaamisen_osoittamiset AS oo WHERE oo.id IN (
 SELECT y.osaamisen_osoittaminen_id FROM :yto-osa-alue-naytto-table AS y
 INNER JOIN :yto-osa-alueet-table AS o
 ON (o.id = y.:yto-osa-alue-id)
 INNER JOIN :tutkinnon-osa-table AS t
 ON (t.hoks_id=? AND t.id = o.:tutkinnon-osa-id))
 RETURNING id
