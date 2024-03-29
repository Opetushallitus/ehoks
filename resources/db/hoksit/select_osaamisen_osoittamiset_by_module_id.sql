SELECT
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
FROM osaamisen_osoittamiset oo
  LEFT OUTER JOIN osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija AS ookja
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
  oo.module_id = ?
  AND oo.deleted_at IS NULL
