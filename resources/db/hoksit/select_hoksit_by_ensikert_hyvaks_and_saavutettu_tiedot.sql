SELECT h.id, h.opiskeluoikeus_oid, h.oppija_oid
FROM hoksit h LEFT OUTER JOIN kyselylinkit kl ON (h.id = kl.hoks_id)
WHERE
  ((kl.tyyppi != 'tutkinnon_suorittaneet'
    AND kl.tyyppi != 'tutkinnon_osia_suorittaneet')
    OR kl.tyyppi IS NULL)
  AND osaamisen_saavuttamisen_pvm IS NULL
  AND ensikertainen_hyvaksyminen >= (current_date - INTERVAL '2 years');
