SELECT t.* FROM tyoelama_arvioijat AS t
  LEFT OUTER JOIN hankitun_osaamisen_nayton_tyoelama_arvioija AS h
    ON (h.tyoelama_arvioija_id = t.id)
  WHERE h.hankitun_osaamisen_naytto_id = ? AND t.deleted_at IS NULL
