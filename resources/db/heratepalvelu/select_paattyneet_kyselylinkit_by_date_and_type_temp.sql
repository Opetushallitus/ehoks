SELECT kl.hoks_id
FROM kyselylinkit as kl
INNER JOIN hoksit as h
  ON kl.hoks_id = h.id
WHERE kl.alkupvm >= '2021-07-01'
  AND (kl.tyyppi = 'tutkinnon_osia_suorittaneet'
         OR kl.tyyppi = 'tutkinnon_suorittaneet');
