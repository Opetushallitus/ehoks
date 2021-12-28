SELECT
  *
FROM osaamisen_hankkimistavat oh
       LEFT OUTER JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat AS hptoosajoin
                       ON (oh.id = hptoosajoin.osaamisen_hankkimistapa_id)
       LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat AS hatoosajoin
                       ON (oh.id = hatoosajoin.osaamisen_hankkimistapa_id)
       LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat AS ytoohjoin
                       ON (oh.id = ytoohjoin.yhteisen_tutkinnon_osan_osa_alue_id)
       LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueet AS ytoosa
                       ON (ytoohjoin.yhteisen_tutkinnon_osan_osa_alue_id = ytoosa.id AND ytoosa.deleted_at IS NULL)
       LEFT OUTER JOIN hankittavat_paikalliset_tutkinnon_osat AS hptoosat
                       ON (hptoosajoin.hankittava_paikallinen_tutkinnon_osa_id = hptoosat.id AND hptoosat.deleted_at IS NULL)
       LEFT OUTER JOIN hankittavat_ammat_tutkinnon_osat AS hatoosat
                       ON (hatoosajoin.hankittava_ammat_tutkinnon_osa_id = hatoosat.id AND hatoosat.deleted_at IS NULL)
       LEFT OUTER JOIN hankittavat_yhteiset_tutkinnon_osat AS ytoosat
                       ON (ytoosa.yhteinen_tutkinnon_osa_id = ytoosat.id AND ytoosat.deleted_at IS NULL)
WHERE ? IN(hptoosat.hoks_id,
           hatoosat.hoks_id,
           ytoosat.hoks_id)
  AND oh.tunniste = ?;
