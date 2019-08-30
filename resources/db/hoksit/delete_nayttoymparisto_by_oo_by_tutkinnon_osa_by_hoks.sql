DELETE FROM nayttoymparistot WHERE id IN
(SELECT oo.nayttoymparisto_id FROM osaamisen_osoittamiset AS oo
INNER JOIN :tutkinnon-osa-naytto-table as y
ON (y.osaamisen_osoittaminen_id = oo.id)
INNER JOIN :tutkinnon-osa-table as t
ON (t.id = y.:tutkinnon-osa-id AND t.hoks_id=?))
RETURNING id
