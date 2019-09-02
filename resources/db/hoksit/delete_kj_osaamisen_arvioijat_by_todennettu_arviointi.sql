  DELETE FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id  IN
  (SELECT t.koulutuksen_jarjestaja_osaamisen_arvioija_id FROM todennettu_arviointi_arvioijat
  AS t
  INNER JOIN todennettu_arviointi_lisatiedot AS n
  ON (n.id = t.todennettu_arviointi_lisatiedot_id)
  INNER JOIN :tutkinnon-osa-table AS o
  ON (o.hoks_id = ? AND o.tarkentavat_tiedot_osaamisen_arvioija_id = n.id))
  RETURNING id
