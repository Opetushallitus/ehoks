SELECT
  oh.*
FROM osaamisen_hankkimistavat oh
       LEFT OUTER JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat AS hptoosajoin
                       ON (oh.id = hptoosajoin.osaamisen_hankkimistapa_id)
       LEFT OUTER JOIN hankittavat_paikalliset_tutkinnon_osat AS hptoosat
                       ON (hptoosajoin.hankittava_paikallinen_tutkinnon_osa_id = hptoosat.id AND hptoosat.deleted_at IS NULL)
WHERE hptoosat.hoks_id = ?
  AND oh.deleted_at IS NULL
  AND oh.yksiloiva_tunniste = ?;
