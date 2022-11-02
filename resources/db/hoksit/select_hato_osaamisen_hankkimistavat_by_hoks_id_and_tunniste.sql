SELECT
  oh.*
FROM osaamisen_hankkimistavat oh
       LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat AS hatoosajoin
                       ON (oh.id = hatoosajoin.osaamisen_hankkimistapa_id)
       LEFT OUTER JOIN hankittavat_ammat_tutkinnon_osat AS hatoosat
                       ON (hatoosajoin.hankittava_ammat_tutkinnon_osa_id = hatoosat.id AND hatoosat.deleted_at IS NULL)
WHERE hatoosat.hoks_id = ?
  AND oh.deleted_at IS NULL
  AND oh.yksiloiva_tunniste = ?;
