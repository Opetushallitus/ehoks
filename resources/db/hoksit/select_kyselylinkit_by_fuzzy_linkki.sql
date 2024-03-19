SELECT
  k.*,
  k.hoks_id AS hoks_id,
  o.oid AS oppijan_oid,
  o.nimi AS oppijan_nimi,
  h.opiskeluoikeus_oid AS opiskeluoikeus_oid
FROM kyselylinkit k
  LEFT OUTER JOIN oppijat AS o ON o.oid = k.oppija_oid
  LEFT OUTER JOIN hoksit AS h ON h.id = k.hoks_id
WHERE k.kyselylinkki LIKE ?
