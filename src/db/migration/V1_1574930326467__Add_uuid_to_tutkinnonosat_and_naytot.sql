ALTER TABLE aiemmin_hankitut_ammat_tutkinnon_osat
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE aiemmin_hankitut_paikalliset_tutkinnon_osat
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE aiemmin_hankitut_yhteiset_tutkinnon_osat
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE hankittavat_ammat_tutkinnon_osat
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE hankittavat_paikalliset_tutkinnon_osat
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE hankittavat_yhteiset_tutkinnon_osat
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE hankittavan_ammat_tutkinnon_osan_naytto
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();

ALTER TABLE hankittavan_paikallisen_tutkinnon_osan_naytto
    ADD COLUMN uuid UUID UNIQUE DEFAULT gen_random_uuid();
