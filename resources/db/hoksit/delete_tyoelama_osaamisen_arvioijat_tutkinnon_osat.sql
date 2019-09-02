DELETE FROM tyoelama_osaamisen_arvioijat WHERE id IN (
SELECT o.tyoelama_arvioija_id FROM osaamisen_osoittamisen_tyoelama_arvioija AS o
INNER JOIN :tutkinnon-osa-naytto-table as t
ON (t.osaamisen_osoittaminen_id = o.osaamisen_osoittaminen_id)
INNER JOIN :tutkinnon-osa-table as y
ON (y.hoks_id = ? AND t.:tutkinnon-osa-id = y.id))
RETURNING id
