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

ALTER TABLE  hankittavan_paikallisen_tutkinnon_osan_naytto ADD COLUMN
hankittava_paikallinen_tutkinnon_osa_id_copy INTEGER REFERENCES hankittavat_paikalliset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE hankittavan_paikallisen_tutkinnon_osan_naytto SET
hankittava_paikallinen_tutkinnon_osa_id_copy=hankittava_paikallinen_tutkinnon_osa_id;
ALTER TABLE  hankittavan_paikallisen_tutkinnon_osan_naytto
DROP COLUMN hankittava_paikallinen_tutkinnon_osa_id;
ALTER TABLE hankittavan_paikallisen_tutkinnon_osan_naytto RENAME COLUMN
hankittava_paikallinen_tutkinnon_osa_id_copy TO hankittava_paikallinen_tutkinnon_osa_id;

ALTER TABLE hankittavan_paikallisen_tutkinnon_osan_naytto ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE  hankittavan_paikallisen_tutkinnon_osan_naytto SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE   hankittavan_paikallisen_tutkinnon_osan_naytto DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE hankittavan_paikallisen_tutkinnon_osan_naytto RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
ADD COLUMN hankittava_paikallinen_tutkinnon_osa_id_copy INTEGER REFERENCES
hankittavat_paikalliset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
SET hankittava_paikallinen_tutkinnon_osa_id_copy=hankittava_paikallinen_tutkinnon_osa_id;
ALTER TABLE  hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
DROP COLUMN hankittava_paikallinen_tutkinnon_osa_id;
ALTER TABLE hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
RENAME COLUMN hankittava_paikallinen_tutkinnon_osa_id_copy TO hankittava_paikallinen_tutkinnon_osa_id;

ALTER TABLE  hankittavan_ammat_tutkinnon_osan_naytto ADD COLUMN
hankittava_ammat_tutkinnon_osa_id_copy INTEGER REFERENCES
hankittavat_ammat_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE hankittavan_ammat_tutkinnon_osan_naytto
SET hankittava_ammat_tutkinnon_osa_id_copy=hankittava_ammat_tutkinnon_osa_id;
ALTER TABLE  hankittavan_ammat_tutkinnon_osan_naytto DROP COLUMN hankittava_ammat_tutkinnon_osa_id;
ALTER TABLE hankittavan_ammat_tutkinnon_osan_naytto RENAME COLUMN
hankittava_ammat_tutkinnon_osa_id_copy TO hankittava_ammat_tutkinnon_osa_id;

ALTER TABLE hankittavan_ammat_tutkinnon_osan_naytto ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE  hankittavan_ammat_tutkinnon_osan_naytto SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE   hankittavan_ammat_tutkinnon_osan_naytto DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE hankittavan_ammat_tutkinnon_osan_naytto RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
ADD COLUMN hankittava_ammat_tutkinnon_osa_id_copy INTEGER REFERENCES
hankittavat_ammat_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
SET hankittava_ammat_tutkinnon_osa_id_copy=hankittava_ammat_tutkinnon_osa_id;
ALTER TABLE  hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
DROP COLUMN hankittava_ammat_tutkinnon_osa_id;
ALTER TABLE hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
RENAME COLUMN hankittava_ammat_tutkinnon_osa_id_copy TO hankittava_ammat_tutkinnon_osa_id;

ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueet ADD COLUMN
yhteinen_tutkinnon_osa_id_copy INTEGER REFERENCES
hankittavat_yhteiset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE yhteisen_tutkinnon_osan_osa_alueet
SET yhteinen_tutkinnon_osa_id_copy=yhteinen_tutkinnon_osa_id;
ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueet
DROP COLUMN yhteinen_tutkinnon_osa_id;
ALTER TABLE yhteisen_tutkinnon_osan_osa_alueet RENAME COLUMN
yhteinen_tutkinnon_osa_id_copy TO yhteinen_tutkinnon_osa_id;

ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
ADD COLUMN yhteisen_tutkinnon_osan_osa_alue_id_copy INTEGER REFERENCES
yhteisen_tutkinnon_osan_osa_alueet(id) ON DELETE CASCADE;
UPDATE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
SET yhteisen_tutkinnon_osan_osa_alue_id_copy=yhteisen_tutkinnon_osan_osa_alue_id;
ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
DROP COLUMN yhteisen_tutkinnon_osan_osa_alue_id;
ALTER TABLE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
RENAME COLUMN yhteisen_tutkinnon_osan_osa_alue_id_copy TO yhteisen_tutkinnon_osan_osa_alue_id;

ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_naytot ADD COLUMN
yhteisen_tutkinnon_osan_osa_alue_id_copy INTEGER REFERENCES
yhteisen_tutkinnon_osan_osa_alueet(id) ON DELETE CASCADE;
UPDATE yhteisen_tutkinnon_osan_osa_alueen_naytot
SET yhteisen_tutkinnon_osan_osa_alue_id_copy=yhteisen_tutkinnon_osan_osa_alue_id;
ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_naytot
DROP COLUMN yhteisen_tutkinnon_osan_osa_alue_id;
ALTER TABLE yhteisen_tutkinnon_osan_osa_alueen_naytot RENAME COLUMN
yhteisen_tutkinnon_osan_osa_alue_id_copy TO yhteisen_tutkinnon_osan_osa_alue_id;

ALTER TABLE  aiemmin_hankitun_ammat_tutkinnon_osan_naytto
ADD COLUMN aiemmin_hankittu_ammat_tutkinnon_osa_id_copy INTEGER REFERENCES
aiemmin_hankitut_ammat_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_ammat_tutkinnon_osan_naytto
SET aiemmin_hankittu_ammat_tutkinnon_osa_id_copy=aiemmin_hankittu_ammat_tutkinnon_osa_id;
ALTER TABLE  aiemmin_hankitun_ammat_tutkinnon_osan_naytto
DROP COLUMN aiemmin_hankittu_ammat_tutkinnon_osa_id;
ALTER TABLE aiemmin_hankitun_ammat_tutkinnon_osan_naytto RENAME COLUMN
aiemmin_hankittu_ammat_tutkinnon_osa_id_copy TO aiemmin_hankittu_ammat_tutkinnon_osa_id;

ALTER TABLE aiemmin_hankitun_ammat_tutkinnon_osan_naytto ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE  aiemmin_hankitun_ammat_tutkinnon_osan_naytto SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE   aiemmin_hankitun_ammat_tutkinnon_osan_naytto DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE aiemmin_hankitun_ammat_tutkinnon_osan_naytto RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto ADD COLUMN
aiemmin_hankittu_paikallinen_tutkinnon_osa_id_copy INTEGER REFERENCES
aiemmin_hankitut_paikalliset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto SET
aiemmin_hankittu_paikallinen_tutkinnon_osa_id_copy=aiemmin_hankittu_paikallinen_tutkinnon_osa_id;
ALTER TABLE  aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
DROP COLUMN aiemmin_hankittu_paikallinen_tutkinnon_osa_id;
ALTER TABLE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
RENAME COLUMN aiemmin_hankittu_paikallinen_tutkinnon_osa_id_copy TO aiemmin_hankittu_paikallinen_tutkinnon_osa_id;

ALTER TABLE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE  aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE   aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
ADD COLUMN aiemmin_hankittu_paikallinen_tutkinnon_osa_id_copy INTEGER REFERENCES
aiemmin_hankitut_paikalliset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
SET aiemmin_hankittu_paikallinen_tutkinnon_osa_id_copy=aiemmin_hankittu_paikallinen_tutkinnon_osa_id;
ALTER TABLE  aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
DROP COLUMN aiemmin_hankittu_paikallinen_tutkinnon_osa_id;
ALTER TABLE aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat RENAME COLUMN
aiemmin_hankittu_paikallinen_tutkinnon_osa_id_copy TO aiemmin_hankittu_paikallinen_tutkinnon_osa_id;

ALTER TABLE  aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
ADD COLUMN aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy INTEGER REFERENCES
aiemmin_hankitut_yhteiset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
SET aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy=aiemmin_hankittu_yhteinen_tutkinnon_osa_id;
ALTER TABLE  aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
DROP COLUMN aiemmin_hankittu_yhteinen_tutkinnon_osa_id;
ALTER TABLE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
RENAME COLUMN aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy TO aiemmin_hankittu_yhteinen_tutkinnon_osa_id;

ALTER TABLE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE  aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE   aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

ALTER TABLE  aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
ADD COLUMN aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy INTEGER REFERENCES
 aiemmin_hankitut_yhteiset_tutkinnon_osat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat SET
aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy=aiemmin_hankittu_yhteinen_tutkinnon_osa_id;
ALTER TABLE  aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
DROP COLUMN aiemmin_hankittu_yhteinen_tutkinnon_osa_id;
ALTER TABLE aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
RENAME COLUMN aiemmin_hankittu_yhteinen_tutkinnon_osa_id_copy TO aiemmin_hankittu_yhteinen_tutkinnon_osa_id;

ALTER TABLE  aiemmin_hankitun_yto_osa_alueen_naytto ADD COLUMN
aiemmin_hankittu_yto_osa_alue_id_copy INTEGER REFERENCES
aiemmin_hankitut_yto_osa_alueet(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_yto_osa_alueen_naytto SET
aiemmin_hankittu_yto_osa_alue_id_copy=aiemmin_hankittu_yto_osa_alue_id;
ALTER TABLE  aiemmin_hankitun_yto_osa_alueen_naytto
DROP COLUMN aiemmin_hankittu_yto_osa_alue_id;
ALTER TABLE aiemmin_hankitun_yto_osa_alueen_naytto RENAME COLUMN
aiemmin_hankittu_yto_osa_alue_id_copy TO aiemmin_hankittu_yto_osa_alue_id;

ALTER TABLE aiemmin_hankitun_yto_osa_alueen_naytto ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE  aiemmin_hankitun_yto_osa_alueen_naytto SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE   aiemmin_hankitun_yto_osa_alueen_naytto DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE aiemmin_hankitun_yto_osa_alueen_naytto RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

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

ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_naytot ADD COLUMN
osaamisen_osoittaminen_id_copy INTEGER REFERENCES osaamisen_osoittamiset(id) ON DELETE CASCADE;
UPDATE yhteisen_tutkinnon_osan_osa_alueen_naytot SET
osaamisen_osoittaminen_id_copy=osaamisen_osoittaminen_id;
ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_naytot DROP COLUMN
osaamisen_osoittaminen_id;
ALTER TABLE yhteisen_tutkinnon_osan_osa_alueen_naytot RENAME COLUMN
osaamisen_osoittaminen_id_copy TO osaamisen_osoittaminen_id;

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

ALTER TABLE  aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
ADD COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy INTEGER REFERENCES
koulutuksen_jarjestaja_osaamisen_arvioijat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
SET koulutuksen_jarjestaja_osaamisen_arvioija_id_copy=koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE  aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
DROP COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
RENAME COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy TO koulutuksen_jarjestaja_osaamisen_arvioija_id;

ALTER TABLE  aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
ADD COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy INTEGER REFERENCES
koulutuksen_jarjestaja_osaamisen_arvioijat(id) ON DELETE CASCADE;
UPDATE aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
SET koulutuksen_jarjestaja_osaamisen_arvioija_id_copy=koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE  aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
DROP COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id;
ALTER TABLE aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
RENAME COLUMN koulutuksen_jarjestaja_osaamisen_arvioija_id_copy TO koulutuksen_jarjestaja_osaamisen_arvioija_id;

ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
ADD COLUMN osaamisen_hankkimistapa_id_copy INTEGER REFERENCES
osaamisen_hankkimistavat(id) ON DELETE CASCADE;
UPDATE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
SET osaamisen_hankkimistapa_id_copy=osaamisen_hankkimistapa_id;
ALTER TABLE  yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
DROP COLUMN osaamisen_hankkimistapa_id;
ALTER TABLE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
RENAME COLUMN osaamisen_hankkimistapa_id_copy TO osaamisen_hankkimistapa_id;

ALTER TABLE  hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
ADD COLUMN osaamisen_hankkimistapa_id_copy INTEGER REFERENCES
osaamisen_hankkimistavat(id) ON DELETE CASCADE;
UPDATE hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
SET osaamisen_hankkimistapa_id_copy=osaamisen_hankkimistapa_id;
ALTER TABLE  hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
DROP COLUMN osaamisen_hankkimistapa_id;
ALTER TABLE hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
RENAME COLUMN osaamisen_hankkimistapa_id_copy TO osaamisen_hankkimistapa_id;

ALTER TABLE  hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
ADD COLUMN osaamisen_hankkimistapa_id_copy INTEGER REFERENCES
osaamisen_hankkimistavat(id) ON DELETE CASCADE;
UPDATE hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
SET osaamisen_hankkimistapa_id_copy=osaamisen_hankkimistapa_id;
ALTER TABLE  hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
DROP COLUMN osaamisen_hankkimistapa_id;
ALTER TABLE hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
RENAME COLUMN osaamisen_hankkimistapa_id_copy TO osaamisen_hankkimistapa_id;

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