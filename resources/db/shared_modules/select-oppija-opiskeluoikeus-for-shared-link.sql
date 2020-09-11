SELECT
  o.nimi AS oppija_nimi,
  o.oid AS oppija_oid,
  opo.tutkinto_nimi,
  opo.osaamisala_nimi,
  sm.tutkinnonosa_tyyppi AS tutkinnonosa_tyyppi,
  sm.voimassaolo_alku,
  sm.voimassaolo_loppu
FROM shared_modules sm
  LEFT OUTER JOIN hoksit AS h
    ON (sm.hoks_eid = h.eid)
  LEFT OUTER JOIN oppijat AS o
    ON (h.oppija_oid = o.oid)
  LEFT OUTER JOIN opiskeluoikeudet AS opo
    ON (h.opiskeluoikeus_oid = opo.oid)
WHERE sm.share_id = ?;
