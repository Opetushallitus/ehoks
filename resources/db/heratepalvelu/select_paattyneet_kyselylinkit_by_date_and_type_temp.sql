SELECT kl.hoks_id, h.osaamisen_saavuttamisen_pvm, h.opiskeluoikeus_oid
FROM kyselylinkit as kl
       INNER JOIN hoksit as h
                  ON kl.hoks_id = h.id
WHERE kl.alkupvm >= ?
  AND kl.alkupvm <= ?
  AND (kl.tyyppi = 'tutkinnon_osia_suorittaneet'
  OR kl.tyyppi = 'tutkinnon_suorittaneet')
  AND kl.hoks_id > ?
ORDER BY kl.hoks_id ASC
LIMIT ?;
