DELETE FROM osaamisen_osoittamiset WHERE id IN (
SELECT n.osaamisen_osoittaminen_id
FROM :tutkinnon-osa-naytto-table AS n
INNER JOIN :tutkinnon-osa-table AS h
ON (n.:tutkinnon-osa-id = h.id  AND h.hoks_id=?))
RETURNING id
