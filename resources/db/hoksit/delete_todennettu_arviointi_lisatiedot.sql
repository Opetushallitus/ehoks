DELETE FROM todennettu_arviointi_lisatiedot WHERE id IN
  (SELECT tarkentavat_tiedot_osaamisen_arvioija_id
  FROM :tutkinnon-osa-table AS t
  WHERE t.hoks_id=?)
  RETURNING id
