SELECT * FROM olemassa_olevat_yhteiset_tutkinnon_osat
  WHERE hoks_id = ? AND deleted_at IS NULL
  ORDER BY version DESC
