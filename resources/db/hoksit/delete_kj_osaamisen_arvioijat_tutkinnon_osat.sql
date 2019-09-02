DELETE FROM  koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id IN
(SELECT o.koulutuksen_jarjestaja_osaamisen_arvioija_id
 FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija as o
 INNER JOIN :tutkinnon-osa-naytto-table as n
 ON (n.osaamisen_osoittaminen_id = o.osaamisen_osoittaminen_id)
 INNER JOIN :tutkinnon-osa-table as t
 ON (t.hoks_id=? AND t.id = n.:tutkinnon-osa-id))
 RETURNING id
