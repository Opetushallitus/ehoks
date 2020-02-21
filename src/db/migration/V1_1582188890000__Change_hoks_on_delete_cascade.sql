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

alter table aiemmin_hankitut_yhteiset_tutkinnon_osat
    drop constraint olemassa_olevat_yhteiset_tutkinnon_osat_hoks_id_fkey,
    add constraint aiemmin_hankitut_yhteiset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
            on delete cascade;

alter table hankittavat_paikalliset_tutkinnon_osat
    drop constraint puuttuvat_paikalliset_tutkinnon_osat_hoks_id_fkey,
    add constraint hankittavat_paikalliset_tutkinnon_osat_hoks_id_fkey
        foreign key (hoks_id) references hoksit
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


ALTER TABLE  aiemmin_hankitut_yto_osa_alueet ADD COLUMN
aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy INTEGER REFERENCES
aiemmin_hankitut_yhteiset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitut_yto_osa_alueet SET
aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy=aiemmin_hankittu_yhteinen_tutkinnon_osa_id;
ALTER TABLE  aiemmin_hankitut_yto_osa_alueet DROP COLUMN
aiemmin_hankittu_yhteinen_tutkinnon_osa_id;
ALTER TABLE aiemmin_hankitut_yto_osa_alueet RENAME COLUMN
aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy TO aiemmin_hankittu_yhteinen_tutkinnon_osa_id;

ALTER TABLE  osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE  osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  osaamisen_osoittamisen_osa_alueet ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id)
ON DELETE CASCADE;
UPDATE osaamisen_osoittamisen_osa_alueet SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE  osaamisen_osoittamisen_osa_alueet DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE osaamisen_osoittamisen_osa_alueet RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  osaamisen_osoittamisen_sisallot ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE osaamisen_osoittamisen_sisallot SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE  osaamisen_osoittamisen_sisallot DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE osaamisen_osoittamisen_sisallot RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  osaamisen_osoittamisen_tyoelama_arvioija ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE  osaamisen_osoittamisen_tyoelama_arvioija DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE osaamisen_osoittamisen_tyoelama_arvioija RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  osaamisen_osoittamisen_yksilolliset_kriteerit ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE  osaamisen_osoittamisen_yksilolliset_kriteerit DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE osaamisen_osoittamisen_yksilolliset_kriteerit RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  osaamisen_osoittamiset ADD COLUMN
nayttoymparisto_id_copy INTEGER REFERENCES nayttoymparistot(id)
ON DELETE CASCADE;
UPDATE osaamisen_osoittamiset SET
nayttoymparisto_id_copy=nayttoymparisto_id;
ALTER TABLE  osaamisen_osoittamiset DROP COLUMN
nayttoymparisto_id;
ALTER TABLE osaamisen_osoittamiset RENAME COLUMN
nayttoymparisto_id_copy TO nayttoymparisto_id;

ALTER TABLE  osaamisen_osoittamisen_tyoelama_arvioija ADD COLUMN
tyoelama_arvioija_id_copy INTEGER REFERENCES tyoelama_osaamisen_arvioijat(id) ON DELETE CASCADE;
UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET
tyoelama_arvioija_id_copy=tyoelama_arvioija_id;
ALTER TABLE  osaamisen_osoittamisen_tyoelama_arvioija DROP COLUMN
tyoelama_arvioija_id;
ALTER TABLE osaamisen_osoittamisen_tyoelama_arvioija RENAME COLUMN
tyoelama_arvioija_id_copy TO tyoelama_arvioija_id;

ALTER TABLE  osaamisen_hankkimistavat
ADD COLUMN tyopaikalla_jarjestettava_koulutus_id_copy INTEGER REFERENCES
tyopaikalla_jarjestettavat_koulutukset(id) ON DELETE CASCADE;
UPDATE osaamisen_hankkimistavat
SET tyopaikalla_jarjestettava_koulutus_id_copy=tyopaikalla_jarjestettava_koulutus_id;
ALTER TABLE  osaamisen_hankkimistavat
DROP COLUMN tyopaikalla_jarjestettava_koulutus_id;
ALTER TABLE osaamisen_hankkimistavat
RENAME COLUMN tyopaikalla_jarjestettava_koulutus_id_copy TO tyopaikalla_jarjestettava_koulutus_id;

ALTER TABLE  muut_oppimisymparistot
ADD COLUMN osaamisen_hankkimistapa_id_copy INTEGER REFERENCES
osaamisen_hankkimistavat(id) ON DELETE CASCADE;
UPDATE muut_oppimisymparistot
SET osaamisen_hankkimistapa_id_copy=osaamisen_hankkimistapa_id;
ALTER TABLE  muut_oppimisymparistot
DROP COLUMN osaamisen_hankkimistapa_id;
ALTER TABLE muut_oppimisymparistot
RENAME COLUMN osaamisen_hankkimistapa_id_copy TO osaamisen_hankkimistapa_id;

ALTER TABLE  todennettu_arviointi_arvioijat
ADD COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy INTEGER REFERENCES
koulutuksen_jarjestaja_osaamisen_arvioijat(id) ON DELETE CASCADE;
UPDATE todennettu_arviointi_arvioijat
SET koulutuksen_jarjestaja_osaamisen_arvioija_id_copy=koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE  todennettu_arviointi_arvioijat
DROP COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE todennettu_arviointi_arvioijat
RENAME COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy TO koulutuksen_jarjestaja_osaamisen_arvioija_id;

ALTER TABLE  osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
ADD COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy INTEGER REFERENCES
koulutuksen_jarjestaja_osaamisen_arvioijat(id) ON DELETE CASCADE;
UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
SET koulutuksen_jarjestaja_osaamisen_arvioija_id_copy=koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE  osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
DROP COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
RENAME COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy TO koulutuksen_jarjestaja_osaamisen_arvioija_id;


ALTER TABLE  aiemmin_hankitut_ammat_tutkinnon_osat ADD COLUMN tarkentavat_tiedot_osaamisen_arvioija_id_copy INTEGER REFERENCES todennettu_arviointi_lisatiedot(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitut_ammat_tutkinnon_osat SET tarkentavat_tiedot_osaamisen_arvioija_id_copy=tarkentavat_tiedot_osaamisen_arvioija_id;
ALTER TABLE  aiemmin_hankitut_ammat_tutkinnon_osat DROP COLUMN tarkentavat_tiedot_osaamisen_arvioija_id;
ALTER TABLE aiemmin_hankitut_ammat_tutkinnon_osat RENAME COLUMN tarkentavat_tiedot_osaamisen_arvioija_id_copy TO tarkentavat_tiedot_osaamisen_arvioija_id;

ALTER TABLE  aiemmin_hankitut_paikalliset_tutkinnon_osat ADD COLUMN tarkentavat_tiedot_osaamisen_arvioija_id_copy INTEGER REFERENCES todennettu_arviointi_lisatiedot(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitut_paikalliset_tutkinnon_osat SET tarkentavat_tiedot_osaamisen_arvioija_id_copy=tarkentavat_tiedot_osaamisen_arvioija_id;
ALTER TABLE  aiemmin_hankitut_paikalliset_tutkinnon_osat DROP COLUMN tarkentavat_tiedot_osaamisen_arvioija_id;
ALTER TABLE aiemmin_hankitut_paikalliset_tutkinnon_osat RENAME COLUMN tarkentavat_tiedot_osaamisen_arvioija_id_copy TO tarkentavat_tiedot_osaamisen_arvioija_id;

ALTER TABLE  aiemmin_hankitut_yhteiset_tutkinnon_osat ADD COLUMN tarkentavat_tiedot_osaamisen_arvioija_id_copy INTEGER REFERENCES todennettu_arviointi_lisatiedot(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitut_yhteiset_tutkinnon_osat SET tarkentavat_tiedot_osaamisen_arvioija_id_copy=tarkentavat_tiedot_osaamisen_arvioija_id;
ALTER TABLE  aiemmin_hankitut_yhteiset_tutkinnon_osat DROP COLUMN tarkentavat_tiedot_osaamisen_arvioija_id;
ALTER TABLE aiemmin_hankitut_yhteiset_tutkinnon_osat RENAME COLUMN tarkentavat_tiedot_osaamisen_arvioija_id_copy TO tarkentavat_tiedot_osaamisen_arvioija_id;

ALTER TABLE  todennettu_arviointi_arvioijat
ADD COLUMN todennettu_arviointi_lisatiedot_id_copy INTEGER REFERENCES
todennettu_arviointi_lisatiedot(id) ON DELETE CASCADE;
UPDATE todennettu_arviointi_arvioijat
SET todennettu_arviointi_lisatiedot_id_copy=todennettu_arviointi_lisatiedot_id;
ALTER TABLE  todennettu_arviointi_arvioijat
DROP COLUMN todennettu_arviointi_lisatiedot_id;
ALTER TABLE todennettu_arviointi_arvioijat
RENAME COLUMN todennettu_arviointi_lisatiedot_id_copy TO todennettu_arviointi_lisatiedot_id;

ALTER TABLE  tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
ADD COLUMN tyopaikalla_jarjestettava_koulutus_id_copy INTEGER REFERENCES
tyopaikalla_jarjestettavat_koulutukset(id) ON DELETE CASCADE;
UPDATE tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
SET tyopaikalla_jarjestettava_koulutus_id_copy=tyopaikalla_jarjestettava_koulutus_id;
ALTER TABLE  tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
DROP COLUMN tyopaikalla_jarjestettava_koulutus_id;
ALTER TABLE tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
RENAME COLUMN tyopaikalla_jarjestettava_koulutus_id_copy TO tyopaikalla_jarjestettava_koulutus_id;
