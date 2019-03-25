SELECT k.* FROM koulutuksen_jarjestaja_arvioijat AS k
  LEFT OUTER JOIN olemassa_olevan_yhteisen_tutkinnon_osan_arvioijat AS o
    ON (o.koulutuksen_jarjestaja_arvioija_id = k.id)
  WHERE o.olemassa_oleva_yhteinen_tutkinnon_osa_id = ? AND k.deleted_at IS NULL
