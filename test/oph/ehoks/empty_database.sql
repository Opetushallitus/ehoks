DELETE FROM aiemmin_hankitun_ammat_tutkinnon_osan_naytto;
DELETE FROM aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat;
DELETE FROM aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto;
DELETE FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat;
DELETE FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto;
DELETE FROM aiemmin_hankitun_yto_osa_alueen_naytto;
DELETE FROM aiemmin_hankitut_ammat_tutkinnon_osat;
ALTER SEQUENCE olemassa_olevat_ammatilliset_tutkinnon_osat_id_seq RESTART WITH 1;
DELETE FROM aiemmin_hankitut_paikalliset_tutkinnon_osat;
ALTER SEQUENCE olemassa_olevat_paikalliset_tutkinnon_osat_id_seq RESTART WITH 1;
DELETE FROM aiemmin_hankitut_yhteiset_tutkinnon_osat;
ALTER SEQUENCE olemassa_olevat_yhteiset_tutkinnon_osat_id_seq RESTART WITH 1;
DELETE FROM aiemmin_hankitut_yto_osa_alueet;
DELETE FROM hankittavan_ammat_tutkinnon_osan_naytto;
DELETE FROM hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat;
DELETE FROM hankittavan_paikallisen_tutkinnon_osan_naytto;
DELETE FROM hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat;
DELETE FROM hankittavat_ammat_tutkinnon_osat;
ALTER SEQUENCE puuttuvat_ammatilliset_tutkinnon_osat_id_seq RESTART WITH 1;
DELETE FROM hankittavat_paikalliset_tutkinnon_osat;
ALTER SEQUENCE puuttuvat_paikalliset_tutkinnon_osat_id_seq RESTART WITH 1;
DELETE FROM hankittavat_yhteiset_tutkinnon_osat;
ALTER SEQUENCE puuttuvat_yhteiset_tutkinnon_osat_id_seq RESTART WITH 1;
DELETE FROM koulutuksen_jarjestaja_osaamisen_arvioijat;
DELETE FROM kyselylinkit;
DELETE FROM muut_oppimisymparistot;
DELETE FROM tyopaikkajakson_keskeytymisajanjaksot;
DELETE FROM nayttoymparistot;
DELETE FROM opiskeluoikeudet;
DELETE FROM opiskeluvalmiuksia_tukevat_opinnot;
ALTER SEQUENCE opiskeluvalmiuksia_tukevat_opinnot_id_seq RESTART WITH 1;
DELETE FROM oppijat;
DELETE FROM osaamisen_hankkimistavat;
DELETE FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija;
DELETE FROM osaamisen_osoittamisen_osa_alueet;
DELETE FROM osaamisen_osoittamisen_sisallot;
DELETE FROM osaamisen_osoittamisen_tyoelama_arvioija;
DELETE FROM osaamisen_osoittamisen_yksilolliset_kriteerit;
DELETE FROM osaamisen_osoittamiset;
DELETE FROM sessions;
DELETE FROM shared_modules;
DELETE FROM todennettu_arviointi_arvioijat;
DELETE FROM todennettu_arviointi_lisatiedot;
DELETE FROM tyoelama_osaamisen_arvioijat;
DELETE FROM tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat;
DELETE FROM tyopaikalla_jarjestettavat_koulutukset;
DELETE FROM user_settings;
DELETE FROM yhteisen_tutkinnon_osan_osa_alueen_naytot;
DELETE FROM yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat;
DELETE FROM yhteisen_tutkinnon_osan_osa_alueet;
DELETE FROM koodisto_koodit;
DELETE FROM hoksit;
ALTER SEQUENCE hoksit_id_seq RESTART WITH 1;
