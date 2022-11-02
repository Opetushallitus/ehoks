SELECT
  oh.*
FROM osaamisen_hankkimistavat oh
       LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat AS ytoohjoin
                       ON (oh.id = ytoohjoin.osaamisen_hankkimistapa_id)
       LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueet AS ytoosa
                       ON (ytoohjoin.yhteisen_tutkinnon_osan_osa_alue_id = ytoosa.id AND ytoosa.deleted_at IS NULL)
       LEFT OUTER JOIN hankittavat_yhteiset_tutkinnon_osat AS ytoosat
                       ON (ytoosa.yhteinen_tutkinnon_osa_id = ytoosat.id AND ytoosat.deleted_at IS NULL)
WHERE ytoosat.hoks_id = ?
  AND oh.deleted_at IS NULL
  AND oh.yksiloiva_tunniste = ?;
