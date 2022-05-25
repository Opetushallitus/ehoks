ALTER TABLE hankittavat_ammat_tutkinnon_osat
  ADD COLUMN opetus_ja_ohjaus_maara FLOAT;
ALTER TABLE hankittavat_paikalliset_tutkinnon_osat
  ADD COLUMN opetus_ja_ohjaus_maara FLOAT;
ALTER TABLE yhteisen_tutkinnon_osan_osa_alueet
  ADD COLUMN opetus_ja_ohjaus_maara FLOAT;
