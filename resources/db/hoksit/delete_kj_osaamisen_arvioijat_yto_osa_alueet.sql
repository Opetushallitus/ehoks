DELETE FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id IN
(SELECT o.koulutuksen_jarjestaja_osaamisen_arvioija_id
 FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija AS o
 INNER JOIN :yto-osa-alue-naytto-table as n
 ON (n.osaamisen_osoittaminen_id = o.osaamisen_osoittaminen_id)
 INNER JOIN :yto-osa-alueet-table as y
 ON (n.:yto-osa-alue-id = y.id)
 INNER JOIN :tutkinnon-osa-table as t
 ON (t.id = y.:tutkinnon-osa-id AND t.hoks_id=?))
 RETURNING id
