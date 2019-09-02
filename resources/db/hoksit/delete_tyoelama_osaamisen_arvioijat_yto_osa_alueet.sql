DELETE FROM tyoelama_osaamisen_arvioijat WHERE id IN (
SELECT o.tyoelama_arvioija_id FROM osaamisen_osoittamisen_tyoelama_arvioija AS o
INNER JOIN :yto-osa-alue-naytto-table as n
ON (n.osaamisen_osoittaminen_id = o.osaamisen_osoittaminen_id)
INNER JOIN :yto-osa-alueet-table as a
ON (n.:yto-osa-alue-id = a.id)
INNER JOIN :tutkinnon-osa-table as y
ON (a.:tutkinnon-osa-id = y.id AND y.hoks_id = ?))
RETURNING id
