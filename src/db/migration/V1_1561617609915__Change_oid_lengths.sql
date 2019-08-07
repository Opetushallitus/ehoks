ALTER TABLE aiemmin_hankitut_ammat_tutkinnon_osat
  ALTER COLUMN koulutuksen_jarjestaja_oid TYPE VARCHAR(256);

ALTER TABLE aiemmin_hankitut_paikalliset_tutkinnon_osat
  ALTER COLUMN koulutuksen_jarjestaja_oid TYPE VARCHAR(256);

ALTER TABLE aiemmin_hankitut_yhteiset_tutkinnon_osat
  ALTER COLUMN koulutuksen_jarjestaja_oid TYPE VARCHAR(256);

ALTER TABLE aiemmin_hankitut_yto_osa_alueet
  ALTER COLUMN koulutuksen_jarjestaja_oid TYPE VARCHAR(256);

ALTER TABLE hankittavat_ammat_tutkinnon_osat
  ALTER COLUMN koulutuksen_jarjestaja_oid TYPE VARCHAR(256);

ALTER TABLE hankittavat_paikalliset_tutkinnon_osat
  ALTER COLUMN koulutuksen_jarjestaja_oid TYPE VARCHAR(256);

ALTER TABLE hankittavat_yhteiset_tutkinnon_osat
  ALTER COLUMN koulutuksen_jarjestaja_oid TYPE VARCHAR(256);

ALTER TABLE hoksit
  ALTER COLUMN opiskeluoikeus_oid TYPE VARCHAR(256),
  ALTER COLUMN oppija_oid TYPE VARCHAR(256);

ALTER TABLE koulutuksen_jarjestaja_osaamisen_arvioijat
  ALTER COLUMN oppilaitos_oid TYPE VARCHAR(256);

ALTER TABLE opiskeluoikeudet
  ALTER COLUMN oid TYPE VARCHAR(256),
  ALTER COLUMN oppija_oid TYPE VARCHAR(256),
  ALTER COLUMN oppilaitos_oid TYPE VARCHAR(256),
  ALTER COLUMN koulutustoimija_oid TYPE VARCHAR(256);

ALTER TABLE oppijat
  ALTER COLUMN oid TYPE VARCHAR(256);

ALTER TABLE osaamisen_hankkimistavat
  ALTER COLUMN jarjestajan_edustaja_oppilaitos_oid TYPE VARCHAR(256),
  ALTER COLUMN hankkijan_edustaja_oppilaitos_oid TYPE VARCHAR(256);

ALTER TABLE osaamisen_osoittamiset
  ALTER COLUMN jarjestaja_oppilaitos_oid TYPE VARCHAR(256);