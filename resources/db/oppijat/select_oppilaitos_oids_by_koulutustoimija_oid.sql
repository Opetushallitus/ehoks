SELECT DISTINCT oppilaitos_oid
  FROM opiskeluoikeudet, hoksit
  WHERE opiskeluoikeudet.koulutustoimija_oid = ?
  AND hoksit.opiskeluoikeus_oid = opiskeluoikeudet.oid
