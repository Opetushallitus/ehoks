alter table aiemmin_hankitut_paikalliset_tutkinnon_osat
    drop constraint olemassa_olevat_paikalliset_tutkinnon_osat_hoks_id_fkey,
    add constraint aiemmin_hankitut_paikalliset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table aiemmin_hankitut_ammat_tutkinnon_osat
    drop constraint olemassa_olevat_ammatilliset_tutkinnon_osat_hoks_id_fkey,
    add constraint aiemmin_hankitut_ammatilliset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table aiemmin_hankitut_ammat_tutkinnon_osat
    drop constraint olemassa_olevat_ammatilliset__tarkentavat_tiedot_arvioija__fkey,
    add constraint aiemmin_hankitut_ammatilliset__tarkentavat_tiedot_arvioija__fkey
        foreign key (tarkentavat_tiedot_osaamisen_arvioija_id) references todennettu_arviointi_lisatiedot
            on delete cascade;

alter table aiemmin_hankitut_yhteiset_tutkinnon_osat
    drop constraint olemassa_olevat_yhteiset_tutkinnon_osat_hoks_id_fkey,
    add constraint aiemmin_hankitut_yhteiset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table aiemmin_hankitut_yhteiset_tutkinnon_osat
    drop constraint olemassa_olevat_yhteiset__tarkentavat_tiedot_arvioija__fkey,
    add constraint aiemmin_hankitut_yhteiset__tarkentavat_tiedot_arvioija__fkey
        foreign key (tarkentavat_tiedot_osaamisen_arvioija_id) references todennettu_arviointi_lisatiedot
            on delete cascade;

alter table hankittavat_paikalliset_tutkinnon_osat
    drop constraint puuttuvat_paikalliset_tutkinnon_osat_hoks_id_fkey,
    add constraint hankittavat_paikalliset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table aiemmin_hankitut_paikalliset_tutkinnon_osat
    drop constraint olemassa_olevat_paikalliset__tarkentavat_tiedot_arvioija__fkey,
    add constraint aiemmin_hankitut_paikalliset__tarkentavat_tiedot_arvioija__fkey
        foreign key (tarkentavat_tiedot_osaamisen_arvioija_id) references todennettu_arviointi_lisatiedot
            on delete cascade;

alter table hankittavat_ammat_tutkinnon_osat
    drop constraint puuttuvat_ammat_tutkinnon_osat_hoks_id_fkey,
    add constraint hankittavat_ammat_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table hankittavat_yhteiset_tutkinnon_osat
    drop constraint puuttuvat_yhteiset_tutkinnon_osat_hoks_id_fkey,
    add constraint hankittavat_yhteiset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table opiskeluvalmiuksia_tukevat_opinnot
    drop constraint opiskeluvalmiuksia_tukevat_opinnot_hoks_id_fkey,
    add constraint opiskeluvalmiuksia_tukevat_opinnot_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table hankittavan_paikallisen_tutkinnon_osan_naytto
    drop constraint puuttuvan_paikallisen_tutkinn_puuttuva_paikallinen_tutkinn_fkey,
    add constraint hankittavan_paikallisen_tutkinn_puuttuva_paikallinen_tutkinn_fkey
        foreign key (hankittava_paikallinen_tutkinnon_osa_id) references hankittavat_paikalliset_tutkinnon_osat
            on delete cascade;

alter table hankittavan_paikallisen_tutkinnon_osan_naytto
    drop constraint puuttuvan_paikallisen_tutkinn_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankittavan_paikallisen_tutkinn_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_paikallisen_tutkin_puuttuva_paikallinen_tutkinn_fkey1,
    add constraint hankittavan_paikallisen_tutkin_hankitta_paikallinen_tutkinn_fkey
        foreign key (hankittava_paikallinen_tutkinnon_osa_id) references hankittavat_paikalliset_tutkinnon_osat
            on delete cascade;

alter table hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_paikallisen_tutkinnon_osaamisen_hankkimistapa_id_fkey,
    add constraint hankittavan_paikallisen_tutkinnon_osaamisen_hankkimistapa_id_fkey
        foreign key (osaamisen_hankkimistapa_id) references osaamisen_hankkimistavat
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_naytto
    drop constraint puuttuvan_ammatillisen_tutkin_puuttuva_ammatillinen_tutkin_fkey,
    add constraint hankittavan_ammatillisen_tutkin_puuttuva_ammatillinen_tutkin_fkey
        foreign key (hankittava_ammat_tutkinnon_osa_id) references hankittavat_ammat_tutkinnon_osat
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_naytto
    drop constraint puuttuvan_ammatillisen_tutkin_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankittavan_ammatillisen_tutkin_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_ammatillisen_tutki_puuttuva_ammatillinen_tutkin_fkey1,
    add constraint hankittavan_ammatillisen_tutki_puuttuva_ammatillinen_tutkin_fkey1
        foreign key (hankittava_ammat_tutkinnon_osa_id) references hankittavat_ammat_tutkinnon_osat
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_ammatillisen_tutkinno_osaamisen_hankkimistapa_id_fkey,
    add constraint hankittavan_ammatillisen_tutkinno_osaamisen_hankkimistapa_id_fkey
        foreign key (osaamisen_hankkimistapa_id) references osaamisen_hankkimistavat
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueet
    drop constraint yhteisen_tutkinnon_osan_osa_alue_yhteinen_tutkinnon_osa_id_fkey,
    add constraint yhteisen_tutkinnon_osan_osa_alue_yhteinen_tutkinnon_osa_id_fkey
        foreign key (yhteinen_tutkinnon_osa_id) references hankittavat_yhteiset_tutkinnon_osat
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    drop constraint yhteisen_tutkinnon_osan_osa_a_yhteisen_tutkinnon_osan_osa__fkey,
    add constraint yhteisen_tutkinnon_osan_osa_a_yhteisen_tutkinnon_osan_osa__fkey
        foreign key (yhteisen_tutkinnon_osan_osa_alue_id) references yhteisen_tutkinnon_osan_osa_alueet
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    drop constraint yhteisen_tutkinnon_osan_osa_alu_osaamisen_hankkimistapa_id_fkey,
    add constraint yhteisen_tutkinnon_osan_osa_alu_osaamisen_hankkimistapa_id_fkey
        foreign key (osaamisen_hankkimistapa_id) references osaamisen_hankkimistavat
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_naytot
    drop constraint yhteisen_tutkinnon_osan_osa__yhteisen_tutkinnon_osan_osa__fkey1,
    add constraint yhteisen_tutkinnon_osan_osa__yhteisen_tutkinnon_osan_osa__fkey1
        foreign key (yhteisen_tutkinnon_osan_osa_alue_id) references yhteisen_tutkinnon_osan_osa_alueet
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_naytot
    drop constraint yhteisen_tutkinnon_osan_osa_a_hankitun_osaamisen_naytto_id_fkey,
    add constraint yhteisen_tutkinnon_osan_osa_a_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_ammatillisen__olemassa_oleva_ammatillinen__fkey,
    add constraint aiemmin_hankitun_ammatillisen__olemassa_oleva_ammatillinen__fkey
        foreign key (aiemmin_hankittu_ammat_tutkinnon_osa_id) references aiemmin_hankitut_ammat_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_ammatillisen__hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_ammatillisen__hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_paikallisen_t_olemassa_oleva_paikallinen_t_fkey,
    add constraint aiemmin_hankitun_paikallisen_t_olemassa_oleva_paikallinen_t_fkey
        foreign key (aiemmin_hankittu_paikallinen_tutkinnon_osa_id) references aiemmin_hankitut_paikalliset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_paikallisen_t_hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_paikallisen_t_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_paikallisen__olemassa_oleva_paikallinen_t_fkey1,
    add constraint aiemmin_hankitun_paikallisen__olemassa_oleva_paikallinen_t_fkey1
        foreign key (aiemmin_hankittu_paikallinen_tutkinnon_osa_id) references aiemmin_hankitut_paikalliset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_paikallisen_t_koulutuksen_jarjestaja_arvio_fkey,
    add constraint aaiemmin_hankitun_paikallisen_t_koulutuksen_jarjestaja_arvio_fkey
        foreign key (koulutuksen_jarjestaja_osaamisen_arvioija_id) references koulutuksen_jarjestaja_osaamisen_arvioijat
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_yhteisen_tutk_olemassa_oleva_yhteinen_tutk_fkey,
    add constraint aiemmin_hankitun_yhteisen_tutk_olemassa_oleva_yhteinen_tutk_fkey
        foreign key (aiemmin_hankittu_yhteinen_tutkinnon_osa_id) references aiemmin_hankitut_yhteiset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_yhteisen_tutk_hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_yhteisen_tutk_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_yhteisen_tut_olemassa_oleva_yhteinen_tutk_fkey1,
    add constraint aiemmin_hankitun_yhteisen_tut_olemassa_oleva_yhteinen_tutk_fkey1
        foreign key (aiemmin_hankittu_yhteinen_tutkinnon_osa_id) references aiemmin_hankitut_yhteiset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_yhteisen_tutk_koulutuksen_jarjestaja_arvio_fkey,
    add constraint aiemmin_hankitun_yhteisen_tutk_koulutuksen_jarjestaja_arvio_fkey
        foreign key (koulutuksen_jarjestaja_osaamisen_arvioija_id) references koulutuksen_jarjestaja_osaamisen_arvioijat
            on delete cascade;

alter table aiemmin_hankitun_yto_osa_alueen_naytto
    drop constraint olemassa_olevan_yto_osa_aluee_olemassa_oleva_yto_osa_alue__fkey,
    add constraint aiemmin_hankitun_yto_osa_aluee_olemassa_oleva_yto_osa_alue__fkey
        foreign key (aiemmin_hankittu_yto_osa_alue_id) references aiemmin_hankitut_yto_osa_alueet
            on delete cascade;

alter table aiemmin_hankitun_yto_osa_alueen_naytto
    drop constraint olemassa_olevan_yto_osa_aluee_hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_yto_osa_aluee_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitut_yto_osa_alueet
    drop constraint olemassa_olevat_yto_osa_aluee_olemassa_oleva_yhteinen_tutk_fkey,
    add constraint aiemmin_hankitut_yto_osa_aluee_olemassa_oleva_yhteinen_tutk_fkey
        foreign key (aiemmin_hankittu_yhteinen_tutkinnon_osa_id) references aiemmin_hankitut_yhteiset_tutkinnon_osat
            on delete cascade;

alter table osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
    drop constraint hankitun_osaamisen_nayton_kou_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankitun_osaamisen_nayton_kou_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
    drop constraint hankitun_osaamisen_nayton_kou_koulutuksen_jarjestaja_arvio_fkey,
    add constraint hankitun_osaamisen_nayton_kou_koulutuksen_jarjestaja_arvio_fkey
        foreign key (koulutuksen_jarjestaja_osaamisen_arvioija_id) references koulutuksen_jarjestaja_osaamisen_arvioijat
            on delete cascade;

alter table osaamisen_osoittamisen_osa_alueet
    drop constraint hankitun_osaamisen_nayton_osa_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankitun_osaamisen_nayton_osa_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table osaamisen_osoittamisen_sisallot
    drop constraint hankitun_osaamisen_tyotehtava_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankitun_osaamisen_tyotehtava_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table osaamisen_osoittamisen_tyoelama_arvioija
    drop constraint hankitun_osaamisen_nayton_tyo_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankitun_osaamisen_nayton_tyo_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table osaamisen_osoittamisen_tyoelama_arvioija
    drop constraint hankitun_osaamisen_nayton_tyoelama_ar_tyoelama_arvioija_id_fkey,
    add constraint hankitun_osaamisen_nayton_tyoelama_ar_tyoelama_arvioija_id_fkey
        foreign key (tyoelama_arvioija_id) references tyoelama_osaamisen_arvioijat
            on delete cascade;

alter table osaamisen_osoittamisen_yksilolliset_kriteerit
    drop constraint osaamisen_osoittamisen_yksilolli_osaamisen_osoittaminen_id_fkey,
    add constraint osaamisen_osoittamisen_yksilolli_osaamisen_osoittaminen_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table osaamisen_osoittamiset
    drop constraint hankitun_osaamisen_naytot_nayttoymparisto_id_fkey,
    add constraint hankitun_osaamisen_naytot_nayttoymparisto_id_fkey
        foreign key (nayttoymparisto_id) references nayttoymparistot
            on delete cascade;

alter table osaamisen_hankkimistavat
    drop constraint osaamisen_hankkimistavat_tyopaikalla_hankittava_osaaminen__fkey,
    add constraint osaamisen_hankkimistavat_tyopaikalla_hankittava_osaaminen__fkey
        foreign key (tyopaikalla_jarjestettava_koulutus_id) references tyopaikalla_jarjestettavat_koulutukset
            on delete cascade;

alter table muut_oppimisymparistot
    drop constraint muut_oppimisymparistot_osaamisen_hankkimistapa_id_fkey,
    add constraint muut_oppimisymparistot_osaamisen_hankkimistapa_id_fkey
        foreign key (osaamisen_hankkimistapa_id) references osaamisen_hankkimistavat
            on delete cascade;

alter table todennettu_arviointi_arvioijat
    drop constraint todennettu_arviointi_arvioija_koulutuksen_jarjestaja_arvio_fkey,
    add constraint todennettu_arviointi_arvioija_koulutuksen_jarjestaja_arvio_fkey
        foreign key (koulutuksen_jarjestaja_osaamisen_arvioija_id) references koulutuksen_jarjestaja_osaamisen_arvioijat
            on delete cascade;

alter table todennettu_arviointi_arvioijat
    drop constraint todennettu_arviointi_arvioija_todennettu_arviointi_lisatie_fkey,
    add constraint todennettu_arviointi_arvioija_todennettu_arviointi_lisatie_fkey
        foreign key (todennettu_arviointi_lisatiedot_id) references todennettu_arviointi_lisatiedot
            on delete cascade;

alter table tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
    drop constraint tyopaikalla_hankittavat_osaa_tyopaikalla_hankittava_osaam_fkey1,
    add constraint tyopaikalla_hankittavat_osaa_tyopaikalla_hankittava_osaam_fkey1
        foreign key (tyopaikalla_jarjestettava_koulutus_id) references tyopaikalla_jarjestettavat_koulutukset
            on delete cascade;
