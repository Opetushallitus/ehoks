SELECT
  osa.*,
  osajoin.*,
  oh.*,
  tjk.*,
  tjkt.*,
  kj.*,
  moy.*,
  naytto.*,
  oo.*,
  ookja.*,
  kjoa.*,
  oota.*,
  toa.*,
  ny.*,
  oos.*,
  oooa.*,
  kk.*,
  ooyk.*
  -- TODO uskon, että täytyy mainita jokainen nimi erikseen tässä :(:(:(
FROM hankittavat_ammat_tutkinnon_osat osa
  LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat AS osajoin
    ON (osa.id = hatojoin.hankittava_ammat_tutkinnon_osa_id)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (osajoin.osaamisen_hankkimistapa_id = oh.id)
  LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
  LEFT OUTER JOIN tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat AS tjkt
    ON (tjkt.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
  LEFT OUTER JOIN keskeytymisajanjaksot AS kj
    ON (oh.id = kj.osaamisen_hankkimistapa_id AND kj.deleted_at IS NULL)
  LEFT OUTER JOIN muut_oppimisymparistot AS moy
    ON (moy.osaamisen_hankkimistapa_id = oh.id AND moy.deleted_at IS NULL)
  LEFT OUTER JOIN hankittavan_ammat_tutkinnon_osan_naytto AS naytto
    ON (osa.id = naytto.hankittava_ammat_tutkinnon_osa_id)
  LEFT OUTER JOIN osaamisen_osoittamiset AS oo
    ON (naytto.osaamisen_osoittaminen_id = oo.id)
  LEFT OUTER JOIN osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija AS ookja
    ON (ookja.osaamisen_osoittaminen_id = oo.id)
  LEFT OUTER JOIN koulutuksen_jarjestaja_osaamisen_arvioijat AS kjoa
    ON (kjoa.id = ookja.koulutuksen_jarjestaja_osaamisen_arvioija_id)
  LEFT OUTER JOIN osaamisen_osoittaminen_tyoelama_arvioija AS oota
    ON (oota.osaamisen_osoittaminen_id = oo.id)
  LEFT OUTER JOIN tyoelama_osaamisen_arvioijat AS toa
    ON (toa.id oota.osaamisen_osoittamisen_tyoelama_arvioija)
  LEFT OUTER JOIN nayttoymparistot AS ny
    ON (ny.id = oo.nayttoymparisto_id)
  LEFT OUTER JOIN osaamisen_osoittamisen_sisallot AS oos
    ON (oos.osaamisen_osoittaminen_id = oo.id)
  LEFT OUTER JOIN osaamisen_osoittamisen_osa_alueet AS oooa
    ON (oooa.osaamisen_osoittaminen_id = oo.id)
  LEFT OUTER JOIN koodisto_koodit AS kk
    ON (kk.id = oooa.koodisto_koodi_id)
  LEFT OUTER JOIN osaamisen_osoittamisen_yksilolliset_kriteerit AS ooyk
    ON (ooyk.osaamisen_osoittaminen_id = oo.id)
WHERE
  osa.id = ?
  AND osa.deleted_at IS NULL
