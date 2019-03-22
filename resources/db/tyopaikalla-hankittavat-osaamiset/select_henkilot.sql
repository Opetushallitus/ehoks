SELECT * FROM tyopaikalla_hankittavat_osaamisen_henkilot
  WHERE tyopaikalla_hankittava_osaaminen_id = ? AND deleted_at IS NULL
