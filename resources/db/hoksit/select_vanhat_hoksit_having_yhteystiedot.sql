WITH
  hyto AS (
    SELECT hyto.hoks_id
    FROM hankittavat_yhteiset_tutkinnon_osat hyto
      JOIN yhteisen_tutkinnon_osan_osa_alueet ytooa ON ytooa.yhteinen_tutkinnon_osa_id = hyto.id
      JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat ytooaoh ON ytooaoh.yhteisen_tutkinnon_osan_osa_alue_id = ytooa.id
      JOIN osaamisen_hankkimistavat oh ON ytooaoh.osaamisen_hankkimistapa_id = oh.id
    WHERE oh.loppu >= current_date - interval '3 months'
  ),
  hpto AS (
    SELECT hpto.hoks_id
    FROM hankittavat_paikalliset_tutkinnon_osat hpto
      JOIN hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat hptooh ON hptooh.hankittava_paikallinen_tutkinnon_osa_id = hpto.id
      JOIN osaamisen_hankkimistavat oh ON hptooh.osaamisen_hankkimistapa_id = oh.id
    WHERE oh.loppu >= current_date - interval '3 months'
  ),
  hato AS (
    SELECT hato.hoks_id
    FROM hankittavat_ammat_tutkinnon_osat hato
      JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat hatooh ON hatooh.hankittava_ammat_tutkinnon_osa_id = hato.id
      join osaamisen_hankkimistavat oh ON hatooh.osaamisen_hankkimistapa_id = oh.id
    WHERE oh.loppu >= current_date - interval '3 months'
  )
SELECT id, opiskeluoikeus_oid
FROM hoksit h
WHERE (sahkoposti IS NOT NULL OR puhelinnumero IS NOT NULL)
  AND updated_at < current_timestamp - interval '3 months'
  AND deleted_at IS NULL
  AND (
    (osaamisen_saavuttamisen_pvm IS NULL
      AND NOT exists(SELECT 1 FROM hyto WHERE hyto.hoks_id = h.id)
      AND NOT exists(SELECT 1 FROM hpto WHERE hpto.hoks_id = h.id)
      AND NOT exists(SELECT 1 FROM hato WHERE hato.hoks_id = h.id))
    OR (osaamisen_saavuttamisen_pvm IS NOT NULL
      AND osaamisen_saavuttamisen_pvm < current_date - interval '3 months')
  )
LIMIT ?
