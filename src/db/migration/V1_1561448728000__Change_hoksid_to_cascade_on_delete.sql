ALTER TABLE  aiemmin_hankitut_paikalliset_tutkinnon_osat ADD COLUMN hoks_id_copy INTEGER REFERENCES hoksit(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitut_paikalliset_tutkinnon_osat SET hoks_id_copy=hoks_id;
ALTER TABLE  aiemmin_hankitut_paikalliset_tutkinnon_osat DROP COLUMN hoks_id;
ALTER TABLE aiemmin_hankitut_paikalliset_tutkinnon_osat RENAME COLUMN hoks_id_copy TO hoks_id;

ALTER TABLE  aiemmin_hankitut_ammat_tutkinnon_osat ADD COLUMN hoks_id_copy INTEGER REFERENCES hoksit(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitut_ammat_tutkinnon_osat SET hoks_id_copy=hoks_id;
ALTER TABLE  aiemmin_hankitut_ammat_tutkinnon_osat DROP COLUMN hoks_id;
ALTER TABLE aiemmin_hankitut_ammat_tutkinnon_osat RENAME COLUMN hoks_id_copy TO hoks_id;

ALTER TABLE  hankittavat_paikalliset_tutkinnon_osat ADD COLUMN hoks_id_copy INTEGER REFERENCES hoksit(id) ON DELETE CASCADE;
UPDATE hankittavat_paikalliset_tutkinnon_osat SET hoks_id_copy=hoks_id;
ALTER TABLE  hankittavat_paikalliset_tutkinnon_osat DROP COLUMN hoks_id;
ALTER TABLE hankittavat_paikalliset_tutkinnon_osat RENAME COLUMN hoks_id_copy TO hoks_id;

ALTER TABLE  aiemmin_hankitut_yhteiset_tutkinnon_osat ADD COLUMN hoks_id_copy INTEGER REFERENCES hoksit(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitut_yhteiset_tutkinnon_osat SET hoks_id_copy=hoks_id;
ALTER TABLE  aiemmin_hankitut_yhteiset_tutkinnon_osat DROP COLUMN hoks_id;
ALTER TABLE aiemmin_hankitut_yhteiset_tutkinnon_osat RENAME COLUMN hoks_id_copy TO hoks_id;

ALTER TABLE  hankittavat_ammat_tutkinnon_osat ADD COLUMN hoks_id_copy INTEGER REFERENCES hoksit(id) ON DELETE CASCADE;
UPDATE hankittavat_ammat_tutkinnon_osat SET hoks_id_copy=hoks_id;
ALTER TABLE  hankittavat_ammat_tutkinnon_osat DROP COLUMN hoks_id;
ALTER TABLE hankittavat_ammat_tutkinnon_osat RENAME COLUMN hoks_id_copy TO hoks_id;

ALTER TABLE  opiskeluvalmiuksia_tukevat_opinnot ADD COLUMN hoks_id_copy INTEGER REFERENCES hoksit(id) ON DELETE CASCADE;
UPDATE opiskeluvalmiuksia_tukevat_opinnot SET hoks_id_copy=hoks_id;
ALTER TABLE  opiskeluvalmiuksia_tukevat_opinnot DROP COLUMN hoks_id;
ALTER TABLE opiskeluvalmiuksia_tukevat_opinnot RENAME COLUMN hoks_id_copy TO hoks_id;

ALTER TABLE  hankittavat_yhteiset_tutkinnon_osat ADD COLUMN hoks_id_copy INTEGER REFERENCES hoksit(id) ON DELETE CASCADE;
UPDATE hankittavat_yhteiset_tutkinnon_osat SET hoks_id_copy=hoks_id;
ALTER TABLE  hankittavat_yhteiset_tutkinnon_osat DROP COLUMN hoks_id;
ALTER TABLE hankittavat_yhteiset_tutkinnon_osat RENAME COLUMN hoks_id_copy TO hoks_id;
