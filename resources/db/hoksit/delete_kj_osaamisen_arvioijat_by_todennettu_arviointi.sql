DELETE FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id IN
(SELECT koulutuksen_jarjestaja_osaamisen_arvioija_id FROM todennettu_arviointi_arvioijat as t WHERE todennettu_arviointi_lisatiedot_id
  IN
(SELECT n.id
  FROM todennettu_arviointi_lisatiedot AS n
  LEFT OUTER JOIN :tutkinnon-osa-table AS to
  ON
  (to.hoks_id = ? AND to.tarkentavat_tiedot_osaamisen_arvioija_id = n.id)))
  RETURNING id
