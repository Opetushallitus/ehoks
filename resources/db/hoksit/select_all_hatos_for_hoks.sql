SELECT
  osa.id AS osa__id,
  osa.hoks_id AS osa__hoks_id,
  osa.tutkinnon_osa_koodi_uri AS osa__tutkinnon_osa_koodi_uri,
  osa.tutkinnon_osa_koodi_versio AS osa__tutkinnon_osa_koodi_versio,
  osa.tutkinnon_osan_perusteen_diaarinro AS osa__tutkinnon_osan_perusteen_diaarinro,
  osa.vaatimuksista_tai_tavoitteista_poikkeaminen
    AS osa__vaatimuksista_tai_tavoitteista_poikkeaminen,
  osa.koulutuksen_jarjestaja_oid AS osa__koulutuksen_jarjestaja_oid,
  osa.olennainen_seikka AS osa__olennainen_seikka,
  osa.module_id AS osa__module_id,
  osa.opetus_ja_ohjaus_maara AS osa__opetus_ja_ohjaus_maara,
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
  moy.loppu AS moy__loppu,
  oo.id AS oo__id,
  oo.jarjestaja_oppilaitos_oid AS oo__jarjestaja_oppilaitos_oid,
  oo.nayttoymparisto_id AS oo__nayttoymparisto_id,
  oo.vaatimuksista_tai_tavoitteista_poikkeaminen
    AS oo__vaatimuksista_tai_tavoitteista_poikkeaminen,
  oo.alku AS oo__alku,
  oo.loppu AS oo__loppu,
  oo.module_id AS oo__module_id,
  kjoa.id AS kjoa__id,
  kjoa.nimi AS kjoa__nimi,
  kjoa.oppilaitos_oid AS kjoa__oppilaitos_oid,
  toa.id AS toa__id,
  toa.nimi AS toa__nimi,
  toa.organisaatio_nimi AS toa__organisaatio_nimi,
  toa.organisaatio_y_tunnus AS toa__organisaatio_y_tunnus,
  ny.id AS ny__id,
  ny.nimi AS ny__nimi,
  ny.y_tunnus AS ny__y_tunnus,
  ny.kuvaus AS ny__kuvaus,
  oos.id AS oos__id,
  oos.sisallon_kuvaus AS oos__sisallon_kuvaus,
  oooa.osaamisen_osoittaminen_id AS oooa__osaamisen_osoittaminen_id,
  oooa.koodisto_koodi_id AS oooa__koodisto_koodi_id,
  kk.id AS kk__id,
  kk.koodi_uri AS kk__koodi_uri,
  kk.koodi_versio AS kk__koodi_versio,
  ooyk.id AS ooyk__id,
  ooyk.yksilollinen_kriteeri AS ooyk__yksilollinen_kriteeri
FROM hankittavat_ammat_tutkinnon_osat osa
  LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
      AS osajoin
    ON (osa.id = osajoin.hankittava_ammat_tutkinnon_osa_id
      AND osajoin.deleted_at IS NULL)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (osajoin.osaamisen_hankkimistapa_id = oh.id AND oh.deleted_at IS NULL)
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
  LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_naytto AS naytto
    ON (osa.id = naytto.hankittava_ammat_tutkinnon_osa_id
         AND naytto.deleted_at IS NULL)
  LEFT OUTER JOIN osaamisen_osoittamiset AS oo
    ON (naytto.osaamisen_osoittaminen_id = oo.id AND oo.deleted_at IS NULL)
  LEFT OUTER JOIN osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
      AS ookja
    ON (ookja.osaamisen_osoittaminen_id = oo.id AND ookja.deleted_at IS NULL)
  LEFT OUTER JOIN koulutuksen_jarjestaja_osaamisen_arvioijat AS kjoa
    ON (kjoa.id = ookja.koulutuksen_jarjestaja_osaamisen_arvioija_id
         AND kjoa.deleted_at IS NULL)
  LEFT OUTER JOIN osaamisen_osoittamisen_tyoelama_arvioija AS oota
    ON (oota.osaamisen_osoittaminen_id = oo.id AND oota.deleted_at IS NULL)
  LEFT OUTER JOIN tyoelama_osaamisen_arvioijat AS toa
    ON (oota.tyoelama_arvioija_id = toa.id AND toa.deleted_at IS NULL)
  LEFT OUTER JOIN nayttoymparistot AS ny
    ON (ny.id = oo.nayttoymparisto_id AND ny.deleted_at IS NULL)
  LEFT OUTER JOIN osaamisen_osoittamisen_sisallot AS oos
    ON (oos.osaamisen_osoittaminen_id = oo.id AND oos.deleted_at IS NULL)
  LEFT OUTER JOIN osaamisen_osoittamisen_osa_alueet AS oooa
    ON (oooa.osaamisen_osoittaminen_id = oo.id AND oooa.deleted_at IS NULL)
  LEFT OUTER JOIN koodisto_koodit AS kk
    ON (kk.id = oooa.koodisto_koodi_id AND kk.deleted_at IS NULL)
  LEFT OUTER JOIN osaamisen_osoittamisen_yksilolliset_kriteerit AS ooyk
    ON (ooyk.osaamisen_osoittaminen_id = oo.id AND ooyk.deleted_at IS NULL)
WHERE
  osa.hoks_id = ?
  AND osa.deleted_at IS NULL
