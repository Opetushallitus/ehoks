SELECT h.* FROM hankitun_osaamisen_naytot AS h
  LEFT OUTER JOIN puuttuvan_paikallisen_tutkinnon_osan_hankitun_osaamisen_naytto AS p
    ON (p.hankitun_osaamisen_naytto_id = h.id)
  WHERE p.puuttuva_paikallinen_tutkinnon_osa_id = ? AND h.deleted_at IS NULL
