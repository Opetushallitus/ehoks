SELECT h.id, h.opiskeluoikeus_oid, h.oppija_oid
FROM hoksit h
  LEFT OUTER JOIN kyselylinkit kl
    ON (h.id = kl.hoks_id AND kl.tyyppi = 'tutkinnon_suorittaneet')
WHERE
  kl.hoks_id IS NULL
  AND osaamisen_saavuttamisen_pvm IS NULL
  AND ensikertainen_hyvaksyminen >= (current_date - INTERVAL '2 years');
