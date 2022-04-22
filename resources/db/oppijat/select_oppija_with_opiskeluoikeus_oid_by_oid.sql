SELECT oppijat.*,
       oo.oid as opiskeluoikeus_oid
  FROM oppijat
  LEFT JOIN opiskeluoikeudet oo ON oppijat.oid = oo.oppija_oid
  WHERE oppijat.oid = ?
