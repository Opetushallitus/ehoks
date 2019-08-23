DELETE FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id IN
(SELECT koulutuksen_jarjestaja_osaamisen_arvioija_id
  FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id
  IN
  (SELECT id FROM osaamisen_osoittamiset WHERE id IN (
   SELECT n.osaamisen_osoittaminen_id
   FROM :tutkinnon-osa-naytto-table AS n
     LEFT OUTER JOIN :tutkinnon-osa-table AS t
     ON
     (n.:tutkinnon-osa-id = t.id AND t.hoks_id = ?))))
     RETURNING id
