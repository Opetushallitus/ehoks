SELECT kyselylinkki,
       hoks_id,
       tyyppi,
       oppija_oid,
       alkupvm,
       sahkoposti,
       lahetyspvm,
       lahetystila,
       vastattu,
       voimassa_loppupvm
FROM kyselylinkit
WHERE oppija_oid = ?
  AND alkupvm <= now()
UNION
SELECT p.kyselylinkki      AS kyselylinkki,
       p.hoks_id           AS hoks_id,
       CASE
           WHEN p.kyselytyyppi = 'aloittaneet' THEN 'aloittaneet'
           WHEN p.kyselytyyppi = 'valmistuneet' THEN 'tutkinnon_suorittaneet'
           WHEN p.kyselytyyppi = 'osia_suorittaneet' THEN 'tutkinnon_osia_suorittaneet'
       END                 AS tyyppi,
       h.oppija_oid        AS oppija_oid,
       p.voimassa_alkupvm  AS alkupvm,
       h.sahkoposti        AS sahkoposti,
       pv.created_at::date AS lahetyspvm,
       pv.tila::text       AS lahetystila,
       null                AS vastattu,
       p.voimassa_loppupvm AS voimassa_loppupvm
FROM palautteet p
         JOIN hoksit h ON h.id = p.hoks_id
         LEFT OUTER JOIN palaute_viestit pv
                         ON (p.id = pv.palaute_id AND pv.viestityyppi = 'email')
WHERE p.kyselytyyppi IN ('aloittaneet', 'valmistuneet', 'osia_suorittaneet')
  AND h.oppija_oid = ?
  AND p.voimassa_alkupvm <= now()
  AND p.kyselylinkki IS NOT NULL
  AND p.deleted_at IS NULL
