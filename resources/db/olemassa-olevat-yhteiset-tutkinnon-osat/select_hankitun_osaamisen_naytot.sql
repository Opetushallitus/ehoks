SELECT h.* FROM hankitun_osaamisen_naytot AS h
  LEFT OUTER JOIN olemassa_olevan_yhteisen_tutkinnon_osan_hankitun_osaamisen_naytto AS o
    ON (o.hankitun_osaamisen_naytto_id = h.id)
  WHERE o.olemassa_oleva_yhteinen_tutkinnon_osa_id = ? AND h.deleted_at IS NULL
