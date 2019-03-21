SELECT k.* FROM koulutuksen_jarjestaja_arvioijat AS k
  LEFT OUTER JOIN hankitun_osaamisen_nayton_koulutuksen_jarjestaja_arvioija AS h
    ON (h.koulutuksen_jarjestaja_arvioija_id = k.id)
  WHERE h.hankitun_osaamisen_naytto_id = ? AND k.deleted_at IS NULL
