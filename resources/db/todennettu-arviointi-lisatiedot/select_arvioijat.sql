SELECT a.* FROM koulutuksen_jarjestaja_arvioijat AS a
  LEFT OUTER JOIN todennettu_arviointi_arvioijat AS t
    ON (t.koulutuksen_jarjestaja_arvioija_id = a.id)
  WHERE t.todennettu_arviointi_lisatiedot_id = ? AND a.deleted_at IS NULL
