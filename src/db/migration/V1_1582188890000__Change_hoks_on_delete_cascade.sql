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
    add constraint aiemmin_hankitut_ammat_tarkentavat_tiedot_arvioija_fkey
        foreign key (tarkentavat_tiedot_osaamisen_arvioija_id) references todennettu_arviointi_lisatiedot
            on delete cascade;

alter table aiemmin_hankitut_yhteiset_tutkinnon_osat
    drop constraint olemassa_olevat_yhteiset_tutkinnon_osat_hoks_id_fkey,
    add constraint aiemmin_hankitut_yhteiset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table aiemmin_hankitut_yhteiset_tutkinnon_osat
    drop constraint olemassa_olevat_yhteiset__tarkentavat_tiedot_arvioija__fkey,
    add constraint aiemmin_hankitut_yhteiset_tarkentavat_tiedot_arvioija_fkey
        foreign key (tarkentavat_tiedot_osaamisen_arvioija_id) references todennettu_arviointi_lisatiedot
            on delete cascade;

alter table hankittavat_paikalliset_tutkinnon_osat
    drop constraint puuttuvat_paikalliset_tutkinnon_osat_hoks_id_fkey,
    add constraint hankittavat_paikalliset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table aiemmin_hankitut_paikalliset_tutkinnon_osat
    drop constraint olemassa_olevat_paikalliset__tarkentavat_tiedot_arvioija__fkey,
    add constraint aiemmin_hankitut_paikalliset_tarkentavat_tiedot_arvioija_fkey
        foreign key (tarkentavat_tiedot_osaamisen_arvioija_id) references todennettu_arviointi_lisatiedot
            on delete cascade;

alter table hankittavat_ammat_tutkinnon_osat
    drop constraint puuttuvat_ammatilliset_tutkinnon_osat_hoks_id_fkey,
    add constraint hankittavat_ammatilliset_tutkinnon_osat_hoks_id_fkey
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
    add constraint hankittavan_paik_tutk_nayt_hank_paik_tutk_osa_fkey
        foreign key (hankittava_paikallinen_tutkinnon_osa_id) references hankittavat_paikalliset_tutkinnon_osat
            on delete cascade;

alter table hankittavan_paikallisen_tutkinnon_osan_naytto
    drop constraint puuttuvan_paikallisen_tutkinn_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankittavan_paik_tutk_naytto_osaamisen_osoittamiset_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_paikallisen_tutkin_puuttuva_paikallinen_tutkinn_fkey1,
    add constraint hankittavan_paik_tutk_os_hank_hankitta_paik_tutk_fkey
        foreign key (hankittava_paikallinen_tutkinnon_osa_id) references hankittavat_paikalliset_tutkinnon_osat
            on delete cascade;

alter table hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_paikallisen_tutkinnon_osaamisen_hankkimistapa_id_fkey,
    add constraint hankittavan_paik_tutk_os_hank_osaamisen_hankkimistapa_id_fkey
        foreign key (osaamisen_hankkimistapa_id) references osaamisen_hankkimistavat
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_naytto
    drop constraint puuttuvan_ammatillisen_tutkin_puuttuva_ammatillinen_tutkin_fkey,
    add constraint hankittavan_ammat_tutk_naytto_hank_ammatillinen_tutk_fkey
        foreign key (hankittava_ammat_tutkinnon_osa_id) references hankittavat_ammat_tutkinnon_osat
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_naytto
    drop constraint puuttuvan_ammatillisen_tutkin_hankitun_osaamisen_naytto_id_fkey,
    add constraint hankittavan_ammat_tutk_naytto_osaamisen_osoittamiset_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_ammatillisen_tutki_puuttuva_ammatillinen_tutkin_fkey1,
    add constraint hankittavan_ammat_tutk_os_hankkimistavat_hank_ammat_tutk_fkey
        foreign key (hankittava_ammat_tutkinnon_osa_id) references hankittavat_ammat_tutkinnon_osat
            on delete cascade;

alter table hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    drop constraint puuttuvan_ammatillisen_tutkinno_osaamisen_hankkimistapa_id_fkey,
    add constraint hankittavan_ammat_tutk_os_hankkimist_os_hankkimistavat_fkey
        foreign key (osaamisen_hankkimistapa_id) references osaamisen_hankkimistavat
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueet
    drop constraint yhteisen_tutkinnon_osan_osa_alue_yhteinen_tutkinnon_osa_id_fkey,
    add constraint yhteisen_tutkinnon_osan_osa_alue_yhteinen_tutkinnon_osa_id_fkey
        foreign key (yhteinen_tutkinnon_osa_id) references hankittavat_yhteiset_tutkinnon_osat
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    drop constraint yhteisen_tutkinnon_osan_osa_a_yhteisen_tutkinnon_osan_osa__fkey,
    add constraint yhteisen_tutkinnon_osan_osa_a_yhteisen_tutkinnon_osan_osa_fkey
        foreign key (yhteisen_tutkinnon_osan_osa_alue_id) references yhteisen_tutkinnon_osan_osa_alueet
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    drop constraint yhteisen_tutkinnon_osan_osa_alu_osaamisen_hankkimistapa_id_fkey,
    add constraint yhteisen_tutkinnon_osan_osa_alu_osaamisen_hankkimistapa_id_fkey
        foreign key (osaamisen_hankkimistapa_id) references osaamisen_hankkimistavat
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_naytot
    drop constraint yhteisen_tutkinnon_osan_osa__yhteisen_tutkinnon_osan_osa__fkey1,
    add constraint yhteisen_tutkinnon_osan_osa_yhteisen_tutkinnon_osan_osa_fkey
        foreign key (yhteisen_tutkinnon_osan_osa_alue_id) references yhteisen_tutkinnon_osan_osa_alueet
            on delete cascade;

alter table yhteisen_tutkinnon_osan_osa_alueen_naytot
    drop constraint yhteisen_tutkinnon_osan_osa_a_hankitun_osaamisen_naytto_id_fkey,
    add constraint yhteisen_tutkinnon_osan_osa_a_hankitun_osaamisen_naytto_id_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_ammatillisen__olemassa_oleva_ammatillinen__fkey,
    add constraint aiemmin_hankitun_ammat_tutk_naytto_aiemmin_hank_tutk_osa_fkey
        foreign key (aiemmin_hankittu_ammat_tutkinnon_osa_id) references aiemmin_hankitut_ammat_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_ammatillisen__hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_ammat_tutk_naytto_os_osoittamiset_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_paikallisen_t_olemassa_oleva_paikallinen_t_fkey,
    add constraint aiemmin_hankitun_paik_tutk_naytto_aiem_hank_paik_tutk_fkey
        foreign key (aiemmin_hankittu_paikallinen_tutkinnon_osa_id) references aiemmin_hankitut_paikalliset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_paikallisen_t_hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_paik_tutk_naytto_os_osoittamiset_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_paikallisen__olemassa_oleva_paikallinen_t_fkey1,
    add constraint aiemmin_hank_paik_tutk_arvioijat_aiemm_hank_paik_tutk_fkey
        foreign key (aiemmin_hankittu_paikallinen_tutkinnon_osa_id) references aiemmin_hankitut_paikalliset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_paikallisen_t_koulutuksen_jarjestaja_arvio_fkey,
    add constraint aiemmin_hankitun_paik_tutk_arv_koul_jarj_os_arvioijat_fkey
        foreign key (koulutuksen_jarjestaja_osaamisen_arvioija_id) references koulutuksen_jarjestaja_osaamisen_arvioijat
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_yhteisen_tutk_olemassa_oleva_yhteinen_tutk_fkey,
    add constraint aiemmin_hankitun_yht_tutk_naytto_aiem_hank_yht_tutk_fkey
        foreign key (aiemmin_hankittu_yhteinen_tutkinnon_osa_id) references aiemmin_hankitut_yhteiset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    drop constraint olemassa_olevan_yhteisen_tutk_hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_yht_tutk_naytto_os_osoittamiset_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_yhteisen_tut_olemassa_oleva_yhteinen_tutk_fkey1,
    add constraint aiemmin_hankitun_yht_tutk_arv_aiem_hank_yht_tutk_fkey
        foreign key (aiemmin_hankittu_yhteinen_tutkinnon_osa_id) references aiemmin_hankitut_yhteiset_tutkinnon_osat
            on delete cascade;

alter table aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    drop constraint olemassa_olevan_yhteisen_tutk_koulutuksen_jarjestaja_arvio_fkey,
    add constraint aiemmin_hankitun_yht_tutk_arv_koul_jarj_os_arvioija_fkey
        foreign key (koulutuksen_jarjestaja_osaamisen_arvioija_id) references koulutuksen_jarjestaja_osaamisen_arvioijat
            on delete cascade;

alter table aiemmin_hankitun_yto_osa_alueen_naytto
    drop constraint olemassa_olevan_yto_osa_aluee_olemassa_oleva_yto_osa_alue__fkey,
    add constraint aiemmin_hankitun_yto_osa_al_naytto_aiem_hank_yto_osa_alue_fkey
        foreign key (aiemmin_hankittu_yto_osa_alue_id) references aiemmin_hankitut_yto_osa_alueet
            on delete cascade;

alter table aiemmin_hankitun_yto_osa_alueen_naytto
    drop constraint olemassa_olevan_yto_osa_aluee_hankitun_osaamisen_naytto_id_fkey,
    add constraint aiemmin_hankitun_yto_osa_al_naytto_os_osoittamiset_fkey
        foreign key (osaamisen_osoittaminen_id) references osaamisen_osoittamiset
            on delete cascade;

alter table aiemmin_hankitut_yto_osa_alueet
    drop constraint olemassa_olevat_yto_osa_aluee_olemassa_oleva_yhteinen_tutk_fkey,
    add constraint aiemmin_hankitut_yto_osa_al_aiem_hank_yht_tutk_fkey
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
    add constraint osaamisen_hankkimistavat_tyopaikalla_hankittava_osaaminen_fkey
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
    add constraint tyopaikalla_hankittavat_osaa_tyopaikalla_hankittava_osaam_fkey
        foreign key (tyopaikalla_jarjestettava_koulutus_id) references tyopaikalla_jarjestettavat_koulutukset
            on delete cascade;
