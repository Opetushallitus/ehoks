SELECT * FROM puuttuvat_paikalliset_tutkinnon_osat
  WHERE hoks_id = ? AND deleted_at IS NULL
  ORDER BY version DESC
