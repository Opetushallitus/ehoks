ALTER TABLE aiemmin_hankitut_ammat_tutkinnon_osat
    RENAME COLUMN uuid TO module_id;

ALTER TABLE aiemmin_hankitut_paikalliset_tutkinnon_osat
    RENAME COLUMN uuid TO module_id;

ALTER TABLE aiemmin_hankitut_yhteiset_tutkinnon_osat
    RENAME COLUMN uuid TO module_id;

ALTER TABLE hankittavat_ammat_tutkinnon_osat
    RENAME COLUMN uuid TO module_id;

ALTER TABLE hankittavat_paikalliset_tutkinnon_osat
    RENAME COLUMN uuid TO module_id;

ALTER TABLE hankittavat_yhteiset_tutkinnon_osat
    RENAME COLUMN uuid TO module_id;

ALTER TABLE aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    RENAME COLUMN uuid TO module_id;

ALTER TABLE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    RENAME COLUMN uuid TO module_id;

ALTER TABLE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    RENAME COLUMN uuid TO module_id;

ALTER TABLE hankittavan_ammat_tutkinnon_osan_naytto
    RENAME COLUMN uuid TO module_id;

ALTER TABLE hankittavan_paikallisen_tutkinnon_osan_naytto
    RENAME COLUMN uuid TO module_id;

ALTER TABLE aiemmin_hankitut_yto_osa_alueet
    RENAME COLUMN uuid TO module_id;

ALTER TABLE osaamisen_osoittamiset
    RENAME COLUMN uuid TO module_id;

ALTER TABLE osaamisen_hankkimistavat
    RENAME COLUMN uuid TO module_id;

ALTER TABLE aiemmin_hankitun_yto_osa_alueen_naytto
    ADD COLUMN module_id UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE yhteisen_tutkinnon_osan_osa_alueet
    ADD COLUMN module_id UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE yhteisen_tutkinnon_osan_osa_alueen_naytot
    ADD COLUMN module_id UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    ADD COLUMN module_id UUID UNIQUE DEFAULT gen_random_uuid();


