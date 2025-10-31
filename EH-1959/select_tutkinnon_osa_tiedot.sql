select json_agg(objects) from (
  select json_build_object(
      'tutkinnon_osa', split_part(tutkinnon_osa, '_', 2),
      'osaamisen_hankkimistavat', coalesce(
          json_agg(distinct split_part(osaamisen_hankkimistapa_koodi_uri, '_', 2))
          filter (where osaamisen_hankkimistapa_koodi_uri is not null),  -- filter out `null` values
          '[]'::json  -- Empty array instead of `null`
      ),
      'oppisopimuksen_perustat', coalesce(
          json_agg(distinct split_part(oppisopimuksen_perusta_koodi_uri, '_', 2))
          filter (where oppisopimuksen_perusta_koodi_uri is not null),  -- filter out `null` values
          '[]'::json  -- Empty array instead of `null`
      ),
      'paikallinen_tutkinnon_osa', tyyppi = 'hpto',
      'paikallinen_tutkinnon_osa_nimi', hpto_nimi
  ) as objects from (
  select hoks_id,
         'hato' as tyyppi,
         hato.id as tutkinnon_osa_id,
         hato.tutkinnon_osa_koodi_uri as tutkinnon_osa,
         null as hpto_nimi,
         oh.osaamisen_hankkimistapa_koodi_uri,
         oh.oppisopimuksen_perusta_koodi_uri
  from hankittavat_ammat_tutkinnon_osat hato
  left join hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat hatooh
  on hato.id = hatooh.hankittava_ammat_tutkinnon_osa_id and hatooh.deleted_at is null
  left join osaamisen_hankkimistavat oh
  on hatooh.osaamisen_hankkimistapa_id = oh.id and oh.deleted_at is null
  where hato.deleted_at is null
  union all
  select hoks_id,
         'hpto' as tyyppi,
         hpto.id as tutkinnon_osa_id,
         null as tutkinnon_osa,
         hpto.nimi as hpto_nimi,
         oh.osaamisen_hankkimistapa_koodi_uri,
         oh.oppisopimuksen_perusta_koodi_uri
  from hankittavat_paikalliset_tutkinnon_osat hpto
  left join hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat hptooh
  on hpto.id = hptooh.hankittava_paikallinen_tutkinnon_osa_id and hptooh.deleted_at is null
  left join osaamisen_hankkimistavat oh
  on hptooh.osaamisen_hankkimistapa_id = oh.id and oh.deleted_at is null
  where hpto.deleted_at is null
  union all
  select hoks_id,
         'hyto' as tyyppi,
         hyto.id as tutkinnon_osa_id,
         hyto.tutkinnon_osa_koodi_uri as tutkinnon_osa,
         null as hpto_nimi, oh.osaamisen_hankkimistapa_koodi_uri,
         oh.oppisopimuksen_perusta_koodi_uri
  from hankittavat_yhteiset_tutkinnon_osat hyto
  left join yhteisen_tutkinnon_osan_osa_alueet ytooa
  on hyto.id = ytooa.yhteinen_tutkinnon_osa_id and ytooa.deleted_at is null
  left join yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat ytooaoh
  on ytooa.id = ytooaoh.yhteisen_tutkinnon_osan_osa_alue_id and ytooaoh.deleted_at is null
  left join osaamisen_hankkimistavat oh
  on ytooaoh.osaamisen_hankkimistapa_id = oh.id and oh.deleted_at is null
  where hyto.deleted_at is null
  ) s
  where hoks_id = %s
  group by tyyppi, tutkinnon_osa_id, tutkinnon_osa, hpto_nimi
) t
