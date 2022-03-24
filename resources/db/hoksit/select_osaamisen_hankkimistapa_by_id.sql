SELECT
  oh.id AS oh__id,
  oh.jarjestajan_edustaja_nimi AS oh__jarjestajan_edustaja_nimi,
  oh.jarjestajan_edustaja_rooli AS oh__jarjestajan_edustaja_rooli,
  oh.jarjestajan_edustaja_oppilaitos_oid
    AS oh__jarjestajan_edustaja_oppilaitos_oid,
  oh.ajanjakson_tarkenne AS oh__ajanjakson_tarkenne,
  oh.osaamisen_hankkimistapa_koodi_uri AS oh__osaamisen_hankkimistapa_koodi_uri,
  oh.osaamisen_hankkimistapa_koodi_versio
    AS oh__osaamisen_hankkimistapa_koodi_versio,
  oh.tyopaikalla_jarjestettava_koulutus_id
    AS oh__tyopaikalla_jarjestettava_koulutus_id,
  oh.hankkijan_edustaja_nimi AS oh__hankkijan_edustaja_nimi,
  oh.hankkijan_edustaja_rooli AS oh__hankkijan_edustaja_rooli,
  oh.hankkijan_edustaja_oppilaitos_oid
    AS oh__hankkijan_edustaja_oppilaitos_oid,
  oh.alku AS oh__alku,
  oh.loppu AS oh__loppu,
  oh.module_id AS oh__module_id,
  oh.osa_aikaisuustieto AS oh__osa_aikaisuustieto,
  oh.oppisopimuksen_perusta_koodi_uri AS oh__oppisopimuksen_perusta_koodi_uri,
  oh.oppisopimuksen_perusta_koodi_versio
    AS oh__oppisopimuksen_perusta_koodi_versio,
  oh.yksiloiva_tunniste AS oh__yksiloiva_tunniste,
  tjk.id AS tjk__id,
  tjk.vastuullinen_tyopaikka_ohjaaja_nimi
    AS tjk__vastuullinen_tyopaikka_ohjaaja_nimi,
  tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti
    AS tjk__vastuullinen_tyopaikka_ohjaaja_sahkoposti,
  tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero
    AS tjk__vastuullinen_tyopaikka_ohjaaja_puhelinnumero,
  tjk.tyopaikan_nimi AS tjk__tyopaikan_nimi,
  tjk.tyopaikan_y_tunnus AS tjk__tyopaikan_y_tunnus,
  tjkt.id AS tjkt__id,
  tjkt.tyotehtava AS tjkt__tyotehtava,
  kj.id AS kj__id,
  kj.alku AS kj__alku,
  kj.loppu AS kj__loppu,
  moy.id AS moy__id,
  moy.oppimisymparisto_koodi_uri AS moy__oppimisymparisto_koodi_uri,
  moy.oppimisymparisto_koodi_versio AS moy__oppimisymparisto_koodi_versio,
  moy.alku AS moy__alku,
  moy.loppu AS moy__loppu
FROM osaamisen_hankkimistavat oh
  LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id
         AND tjk.deleted_at IS NULL)
  LEFT OUTER JOIN tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat AS tjkt
    ON (tjkt.tyopaikalla_jarjestettava_koulutus_id = tjk.id
         AND tjkt.deleted_at IS NULL)
  LEFT OUTER JOIN keskeytymisajanjaksot AS kj
    ON (oh.id = kj.osaamisen_hankkimistapa_id AND kj.deleted_at IS NULL)
  LEFT OUTER JOIN muut_oppimisymparistot AS moy
    ON (moy.osaamisen_hankkimistapa_id = oh.id AND moy.deleted_at IS NULL)
WHERE
  oh.id = ?
  AND oh.deleted_at IS NULL
