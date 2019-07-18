SELECT DISTINCT oppilaitos_oid
  FROM opiskeluoikeudet, hoksit
  WHERE hoksit.opiskeluoikeus_oid = opiskeluoikeudet.oid
