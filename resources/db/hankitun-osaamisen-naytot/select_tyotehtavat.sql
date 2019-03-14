SELECT * FROM hankitun_osaamisen_tyotehtavat
  WHERE hankitun_osaamisen_naytto_id = ? AND deleted_at IS NULL
  ORDER BY version DESC
