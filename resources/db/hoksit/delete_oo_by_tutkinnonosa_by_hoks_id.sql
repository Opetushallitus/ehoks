DELETE FROM osaamisen_osoittamiset WHERE id IN (
SELECT osaamisen_osoittaminen_id FROM :tutkinnon-osa-naytto-table as n
WHERE n.:tutkinnon-osa-id IN
(SELECT id FROM :tutkinnon-osa-table WHERE hoks_id=?))
RETURNING id
