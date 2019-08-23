DELETE FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id IN
(SELECT koulutuksen_jarjestaja_osaamisen_arvioija_id
  FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id
  IN
  (SELECT y.osaamisen_osoittaminen_id
    FROM :yto-osa-alue-naytto-table AS y
    LEFT OUTER JOIN :yto-osa-alueet-table AS o
    ON
    (y.:yto-osa-alue-id = o.id
      AND o.:tutkinnon-osa-id IN
    (SELECT id
     FROM :tutkinnon-osa-table AS t WHERE t.hoks_id = ?))))
   RETURNING id
