SELECT * FROM olemassa_olevat_yto_osa_alueet
  WHERE olemassa_oleva_yhteinen_tutkinnon_osa_id = ? AND deleted_at IS NULL
