CREATE OR REPLACE FUNCTION cascading_shallow_delete() RETURNS TRIGGER AS $$
  DECLARE
    osaamisen_hankkimistapa RECORD;
    tyopaikalla_jarj_koulutus RECORD;
    tyopaikalla_jarj_koulutus_tyotehtava RECORD;
    keskeytymisajanjakso RECORD;
    muu_oppimisymparisto RECORD;
    osaamisen_osoittaminen RECORD;
    osaamisen_osoittamisen_sisalto RECORD;
    osaamisen_osoittamisen_yksilollinen_kriteeri RECORD;
    osaamisen_osoittamisen_osa_alue RECORD;
    osaamisen_osoittamisen_arvioija RECORD;
    osaamisen_osoittamisen_koulutuksen_jarjestaja_arv RECORD;
    tyoelama_osaamisen_arvioija RECORD;
    nayttoymparisto RECORD;
    koodisto_koodi RECORD;
    todennettu_arviointi_lisatieto RECORD;
    todennettu_arviointi_arvioija RECORD;
    koulutuksen_jarjestaja_osaamisen_arvioija RECORD;
    hko RECORD;
    hyto RECORD;
    hyto_osa_alue RECORD;
    hyto_osa_alueen_osaamisen_hankkimistapa RECORD;
    hyto_osa_alueen_naytto RECORD;
    hato RECORD;
    hato_osaamisen_hankkimistapa RECORD;
    hato_naytto RECORD;
    hpto RECORD;
    hpto_osaamisen_hankkimistapa RECORD;
    hpto_naytto RECORD;
    ahyto RECORD;
    ahyto_osa_alue RECORD;
    ahyto_osa_alueen_naytto RECORD;
    ahyto_naytto RECORD;
    ahyto_arvioija RECORD;
    ahato RECORD;
    ahato_naytto RECORD;
    ahpto RECORD;
    ahpto_naytto RECORD;
    ahpto_arvioija RECORD;

  BEGIN
    -- OLD == hoks ennen päivitystä
    -- NEW == hoks päivityksen jälkeen

    -- hko
    FOR hko IN
      SELECT * FROM hankittavat_koulutuksen_osat WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
      LOOP
        UPDATE hankittavat_koulutuksen_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = hko.id;
      END LOOP;

    -- hyto
    FOR hyto IN
      SELECT * FROM hankittavat_yhteiset_tutkinnon_osat WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
      LOOP
        UPDATE hankittavat_yhteiset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = hyto.id;
        FOR hyto_osa_alue IN
          SELECT * FROM yhteisen_tutkinnon_osan_osa_alueet WHERE yhteinen_tutkinnon_osa_id = hyto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE yhteisen_tutkinnon_osan_osa_alueet SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = hyto_osa_alue.id;
            FOR hyto_osa_alueen_osaamisen_hankkimistapa IN
              SELECT * FROM yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat WHERE yhteisen_tutkinnon_osan_osa_alue_id = hyto_osa_alue.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at WHERE yhteisen_tutkinnon_osan_osa_alue_id = hyto_osa_alueen_osaamisen_hankkimistapa.yhteisen_tutkinnon_osan_osa_alue_id AND osaamisen_hankkimistapa_id = hyto_osa_alueen_osaamisen_hankkimistapa.osaamisen_hankkimistapa_id;
                FOR osaamisen_hankkimistapa IN
                  SELECT * FROM osaamisen_hankkimistavat WHERE id = hyto_osa_alueen_osaamisen_hankkimistapa.osaamisen_hankkimistapa_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_hankkimistapa.id;
                    FOR tyopaikalla_jarj_koulutus IN
                      SELECT * FROM tyopaikalla_jarjestettavat_koulutukset WHERE id = osaamisen_hankkimistapa.tyopaikalla_jarjestettava_koulutus_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyopaikalla_jarjestettavat_koulutukset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyopaikalla_jarj_koulutus.id;
                        FOR tyopaikalla_jarj_koulutus_tyotehtava IN
                          SELECT * FROM tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat WHERE tyopaikalla_jarjestettava_koulutus_id = tyopaikalla_jarj_koulutus.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                          LOOP
                            UPDATE tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyopaikalla_jarj_koulutus_tyotehtava.id;
                          END LOOP;
                      END LOOP;
                    FOR keskeytymisajanjakso IN
                      SELECT * FROM keskeytymisajanjaksot WHERE osaamisen_hankkimistapa_id = osaamisen_hankkimistapa.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE keskeytymisajanjaksot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = keskeytymisajanjakso.id;
                      END LOOP;
                    FOR muu_oppimisymparisto IN
                      SELECT * FROM muut_oppimisymparistot WHERE osaamisen_hankkimistapa_id = osaamisen_hankkimistapa.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE muut_oppimisymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = muu_oppimisymparisto.id;
                      END LOOP;
                  END LOOP;
            END LOOP;
            FOR hyto_osa_alueen_naytto IN
              SELECT * FROM yhteisen_tutkinnon_osan_osa_alueen_naytot WHERE yhteisen_tutkinnon_osan_osa_alue_id = hyto_osa_alue.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE yhteisen_tutkinnon_osan_osa_alueen_naytot SET deleted_at = NEW.deleted_at WHERE yhteisen_tutkinnon_osan_osa_alue_id = hyto_osa_alueen_naytto.yhteisen_tutkinnon_osan_osa_alue_id AND osaamisen_osoittaminen_id = hyto_osa_alueen_naytto.osaamisen_osoittaminen_id;
                FOR osaamisen_osoittaminen IN
                  SELECT * FROM osaamisen_osoittamiset WHERE id = hyto_osa_alueen_naytto.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittaminen.id;
                    FOR osaamisen_osoittamisen_sisalto IN
                      SELECT * FROM osaamisen_osoittamisen_sisallot WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_sisalto.id;
                      END LOOP;
                    FOR osaamisen_osoittamisen_yksilollinen_kriteeri IN
                      SELECT * FROM osaamisen_osoittamisen_yksilolliset_kriteerit WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_yksilollinen_kriteeri.id;
                      END LOOP;
                    FOR nayttoymparisto IN
                      SELECT * FROM nayttoymparistot WHERE id = osaamisen_osoittaminen.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = nayttoymparisto.id;
                      END LOOP;
                    FOR osaamisen_osoittamisen_osa_alue IN
                      SELECT * FROM osaamisen_osoittamisen_osa_alueet WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_osa_alue.osaamisen_osoittaminen_id;
                        FOR koodisto_koodi IN
                          SELECT * FROM koodisto_koodit WHERE id = osaamisen_osoittamisen_osa_alue.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                          LOOP
                            UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koodisto_koodi.id;
                          END LOOP;
                      END LOOP;
                    FOR osaamisen_osoittamisen_arvioija IN
                      SELECT * FROM osaamisen_osoittamisen_tyoelama_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_arvioija.osaamisen_osoittaminen_id;
                        FOR tyoelama_osaamisen_arvioija IN
                          SELECT * FROM tyoelama_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_arvioija.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                          LOOP
                            UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyoelama_osaamisen_arvioija.id;
                          END LOOP;
                      END LOOP;
                    FOR osaamisen_osoittamisen_koulutuksen_jarjestaja_arv IN
                      SELECT * FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.osaamisen_osoittaminen_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                        FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                          SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                          LOOP
                            UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                          END LOOP;
                      END LOOP;
                  END LOOP;
              END LOOP;
          END LOOP;
      END LOOP;

    -- hato
    FOR hato IN
      SELECT * FROM hankittavat_ammat_tutkinnon_osat WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
      LOOP
        UPDATE hankittavat_ammat_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = hato.id;
        FOR hato_osaamisen_hankkimistapa IN
          SELECT * FROM hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat WHERE hankittava_ammat_tutkinnon_osa_id = hato.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at WHERE hankittava_ammat_tutkinnon_osa_id = hato_osaamisen_hankkimistapa.hankittava_ammat_tutkinnon_osa_id AND osaamisen_hankkimistapa_id = hato_osaamisen_hankkimistapa.osaamisen_hankkimistapa_id;
            FOR osaamisen_hankkimistapa IN
              SELECT * FROM osaamisen_hankkimistavat WHERE id = hato_osaamisen_hankkimistapa.osaamisen_hankkimistapa_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_hankkimistapa.id;
                FOR tyopaikalla_jarj_koulutus IN
                  SELECT * FROM tyopaikalla_jarjestettavat_koulutukset WHERE id = osaamisen_hankkimistapa.tyopaikalla_jarjestettava_koulutus_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE tyopaikalla_jarjestettavat_koulutukset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyopaikalla_jarj_koulutus.id;
                    FOR tyopaikalla_jarj_koulutus_tyotehtava IN
                      SELECT * FROM tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat WHERE tyopaikalla_jarjestettava_koulutus_id = tyopaikalla_jarj_koulutus.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyopaikalla_jarj_koulutus_tyotehtava.id;
                      END LOOP;
                  END LOOP;
                FOR keskeytymisajanjakso IN
                  SELECT * FROM keskeytymisajanjaksot WHERE osaamisen_hankkimistapa_id = osaamisen_hankkimistapa.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE keskeytymisajanjaksot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = keskeytymisajanjakso.id;
                  END LOOP;
                FOR muu_oppimisymparisto IN
                  SELECT * FROM muut_oppimisymparistot WHERE osaamisen_hankkimistapa_id = osaamisen_hankkimistapa.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE muut_oppimisymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = muu_oppimisymparisto.id;
                  END LOOP;
              END LOOP;
          END LOOP;
        FOR hato_naytto IN
          SELECT * FROM hankittavan_ammat_tutkinnon_osan_naytto WHERE hankittava_ammat_tutkinnon_osa_id = hato.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE hankittavan_ammat_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE hankittava_ammat_tutkinnon_osa_id = hato_naytto.hankittava_ammat_tutkinnon_osa_id AND osaamisen_osoittaminen_id = hato_naytto.osaamisen_osoittaminen_id;
            FOR osaamisen_osoittaminen IN
              SELECT * FROM osaamisen_osoittamiset WHERE id = hato_naytto.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittaminen.id;
                FOR osaamisen_osoittamisen_sisalto IN
                  SELECT * FROM osaamisen_osoittamisen_sisallot WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_sisalto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_yksilollinen_kriteeri IN
                  SELECT * FROM osaamisen_osoittamisen_yksilolliset_kriteerit WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_yksilollinen_kriteeri.id;
                  END LOOP;
                FOR nayttoymparisto IN
                  SELECT * FROM nayttoymparistot WHERE id = osaamisen_osoittaminen.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = nayttoymparisto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_osa_alue IN
                  SELECT * FROM osaamisen_osoittamisen_osa_alueet WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_osa_alue.osaamisen_osoittaminen_id;
                    FOR koodisto_koodi IN
                      SELECT * FROM koodisto_koodit WHERE id = osaamisen_osoittamisen_osa_alue.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koodisto_koodi.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_arvioija IN
                  SELECT * FROM osaamisen_osoittamisen_tyoelama_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_arvioija.osaamisen_osoittaminen_id;
                    FOR tyoelama_osaamisen_arvioija IN
                      SELECT * FROM tyoelama_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_arvioija.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyoelama_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_koulutuksen_jarjestaja_arv IN
                  SELECT * FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.osaamisen_osoittaminen_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                    FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                      SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
              END LOOP;
          END LOOP;
      END LOOP;

    -- hpto
    FOR hpto IN
      SELECT * FROM hankittavat_paikalliset_tutkinnon_osat WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
      LOOP
        UPDATE hankittavat_paikalliset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = hpto.id;
        FOR hpto_osaamisen_hankkimistapa IN
          SELECT * FROM hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat WHERE hankittava_paikallinen_tutkinnon_osa_id = hpto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at WHERE hankittava_paikallinen_tutkinnon_osa_id = hpto_osaamisen_hankkimistapa.hankittava_paikallinen_tutkinnon_osa_id AND osaamisen_hankkimistapa_id = hpto_osaamisen_hankkimistapa.osaamisen_hankkimistapa_id;
            FOR osaamisen_hankkimistapa IN
              SELECT * FROM osaamisen_hankkimistavat WHERE id = hpto_osaamisen_hankkimistapa.osaamisen_hankkimistapa_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_hankkimistapa.id;
                FOR tyopaikalla_jarj_koulutus IN
                  SELECT * FROM tyopaikalla_jarjestettavat_koulutukset WHERE id = osaamisen_hankkimistapa.tyopaikalla_jarjestettava_koulutus_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE tyopaikalla_jarjestettavat_koulutukset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyopaikalla_jarj_koulutus.id;
                    FOR tyopaikalla_jarj_koulutus_tyotehtava IN
                      SELECT * FROM tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat WHERE tyopaikalla_jarjestettava_koulutus_id = tyopaikalla_jarj_koulutus.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyopaikalla_jarj_koulutus_tyotehtava.id;
                      END LOOP;
                  END LOOP;
                FOR keskeytymisajanjakso IN
                  SELECT * FROM keskeytymisajanjaksot WHERE osaamisen_hankkimistapa_id = osaamisen_hankkimistapa.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE keskeytymisajanjaksot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = keskeytymisajanjakso.id;
                  END LOOP;
                FOR muu_oppimisymparisto IN
                  SELECT * FROM muut_oppimisymparistot WHERE osaamisen_hankkimistapa_id = osaamisen_hankkimistapa.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE muut_oppimisymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = muu_oppimisymparisto.id;
                  END LOOP;
              END LOOP;
          END LOOP;
        FOR hpto_naytto IN
          SELECT * FROM hankittavan_paikallisen_tutkinnon_osan_naytto WHERE hankittava_paikallinen_tutkinnon_osa_id = hpto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE hankittavan_paikallisen_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE hankittava_paikallinen_tutkinnon_osa_id = hpto_naytto.hankittava_paikallinen_tutkinnon_osa_id AND osaamisen_osoittaminen_id = hpto_naytto.osaamisen_osoittaminen_id;
            FOR osaamisen_osoittaminen IN
              SELECT * FROM osaamisen_osoittamiset WHERE id = hpto_naytto.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittaminen.id;
                FOR osaamisen_osoittamisen_sisalto IN
                  SELECT * FROM osaamisen_osoittamisen_sisallot WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_sisalto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_yksilollinen_kriteeri IN
                  SELECT * FROM osaamisen_osoittamisen_yksilolliset_kriteerit WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_yksilollinen_kriteeri.id;
                  END LOOP;
                FOR nayttoymparisto IN
                  SELECT * FROM nayttoymparistot WHERE id = osaamisen_osoittaminen.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = nayttoymparisto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_osa_alue IN
                  SELECT * FROM osaamisen_osoittamisen_osa_alueet WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_osa_alue.osaamisen_osoittaminen_id;
                    FOR koodisto_koodi IN
                      SELECT * FROM koodisto_koodit WHERE id = osaamisen_osoittamisen_osa_alue.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koodisto_koodi.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_arvioija IN
                  SELECT * FROM osaamisen_osoittamisen_tyoelama_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_arvioija.osaamisen_osoittaminen_id;
                    FOR tyoelama_osaamisen_arvioija IN
                      SELECT * FROM tyoelama_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_arvioija.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyoelama_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_koulutuksen_jarjestaja_arv IN
                  SELECT * FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.osaamisen_osoittaminen_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                    FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                      SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
              END LOOP;
          END LOOP;
      END LOOP;

    -- ahyto
    FOR ahyto IN
      SELECT * FROM aiemmin_hankitut_yhteiset_tutkinnon_osat WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
      LOOP
        UPDATE aiemmin_hankitut_yhteiset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = ahyto.id;
        FOR ahyto_osa_alue IN
          SELECT * FROM aiemmin_hankitut_yto_osa_alueet WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ahyto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE aiemmin_hankitut_yto_osa_alueet SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = ahyto_osa_alue.id;
            FOR ahyto_osa_alueen_naytto IN
              SELECT * FROM aiemmin_hankitun_yto_osa_alueen_naytto WHERE aiemmin_hankittu_yto_osa_alue_id = ahyto_osa_alue.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE aiemmin_hankitun_yto_osa_alueen_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_yto_osa_alue_id = ahyto_osa_alueen_naytto.aiemmin_hankittu_yto_osa_alue_id AND osaamisen_osoittaminen_id = ahyto_osa_alueen_naytto.osaamisen_osoittaminen_id;
                FOR osaamisen_osoittaminen IN
                  SELECT * FROM osaamisen_osoittamiset WHERE id = ahyto_osa_alueen_naytto.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittaminen.id;
                    FOR osaamisen_osoittamisen_sisalto IN
                      SELECT * FROM osaamisen_osoittamisen_sisallot WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_sisalto.id;
                      END LOOP;
                    FOR osaamisen_osoittamisen_yksilollinen_kriteeri IN
                      SELECT * FROM osaamisen_osoittamisen_yksilolliset_kriteerit WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_yksilollinen_kriteeri.id;
                      END LOOP;
                    FOR nayttoymparisto IN
                      SELECT * FROM nayttoymparistot WHERE id = osaamisen_osoittaminen.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = nayttoymparisto.id;
                      END LOOP;
                    FOR osaamisen_osoittamisen_osa_alue IN
                      SELECT * FROM osaamisen_osoittamisen_osa_alueet WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_osa_alue.osaamisen_osoittaminen_id;
                        FOR koodisto_koodi IN
                          SELECT * FROM koodisto_koodit WHERE id = osaamisen_osoittamisen_osa_alue.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                          LOOP
                            UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koodisto_koodi.id;
                          END LOOP;
                      END LOOP;
                    FOR osaamisen_osoittamisen_arvioija IN
                      SELECT * FROM osaamisen_osoittamisen_tyoelama_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_arvioija.osaamisen_osoittaminen_id;
                        FOR tyoelama_osaamisen_arvioija IN
                          SELECT * FROM tyoelama_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_arvioija.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                          LOOP
                            UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyoelama_osaamisen_arvioija.id;
                          END LOOP;
                      END LOOP;
                    FOR osaamisen_osoittamisen_koulutuksen_jarjestaja_arv IN
                      SELECT * FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.osaamisen_osoittaminen_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                        FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                          SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                          LOOP
                            UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                          END LOOP;
                      END LOOP;
                  END LOOP;
              END LOOP;
            FOR todennettu_arviointi_lisatieto IN
              SELECT * FROM todennettu_arviointi_lisatiedot WHERE id = ahyto_osa_alue.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = todennettu_arviointi_lisatieto.id;
                FOR todennettu_arviointi_arvioija IN
                  SELECT * FROM todennettu_arviointi_arvioijat WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_lisatieto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE todennettu_arviointi_arvioijat SET deleted_at = NEW.deleted_at WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_arvioija.todennettu_arviointi_lisatiedot_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                    FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                      SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
              END LOOP;
          END LOOP;
        FOR ahyto_naytto IN
          SELECT * FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ahyto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ahyto_naytto.aiemmin_hankittu_yhteinen_tutkinnon_osa_id AND osaamisen_osoittaminen_id = ahyto_naytto.osaamisen_osoittaminen_id;
            FOR osaamisen_osoittaminen IN
              SELECT * FROM osaamisen_osoittamiset WHERE id = ahyto_naytto.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittaminen.id;
                FOR osaamisen_osoittamisen_sisalto IN
                  SELECT * FROM osaamisen_osoittamisen_sisallot WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_sisalto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_yksilollinen_kriteeri IN
                  SELECT * FROM osaamisen_osoittamisen_yksilolliset_kriteerit WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_yksilollinen_kriteeri.id;
                  END LOOP;
                FOR nayttoymparisto IN
                  SELECT * FROM nayttoymparistot WHERE id = osaamisen_osoittaminen.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = nayttoymparisto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_osa_alue IN
                  SELECT * FROM osaamisen_osoittamisen_osa_alueet WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_osa_alue.osaamisen_osoittaminen_id;
                    FOR koodisto_koodi IN
                      SELECT * FROM koodisto_koodit WHERE id = osaamisen_osoittamisen_osa_alue.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koodisto_koodi.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_arvioija IN
                  SELECT * FROM osaamisen_osoittamisen_tyoelama_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_arvioija.osaamisen_osoittaminen_id;
                    FOR tyoelama_osaamisen_arvioija IN
                      SELECT * FROM tyoelama_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_arvioija.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyoelama_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_koulutuksen_jarjestaja_arv IN
                  SELECT * FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.osaamisen_osoittaminen_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                    FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                      SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
              END LOOP;
          END LOOP;
        FOR ahyto_arvioija IN
          SELECT * FROM aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ahyto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ahyto_arvioija.aiemmin_hankittu_yhteinen_tutkinnon_osa_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = ahyto_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id;
            FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
              SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = ahyto_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
              END LOOP;
          END LOOP;
        FOR todennettu_arviointi_lisatieto IN
          SELECT * FROM todennettu_arviointi_lisatiedot WHERE id = ahyto.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = todennettu_arviointi_lisatieto.id;
            FOR todennettu_arviointi_arvioija IN
              SELECT * FROM todennettu_arviointi_arvioijat WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_lisatieto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE todennettu_arviointi_arvioijat SET deleted_at = NEW.deleted_at WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_arvioija.todennettu_arviointi_lisatiedot_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                  SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                  END LOOP;
              END LOOP;
          END LOOP;
      END LOOP;

    -- ahato
    FOR ahato IN
      SELECT * FROM aiemmin_hankitut_ammat_tutkinnon_osat WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
      LOOP
        UPDATE aiemmin_hankitut_ammat_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = ahato.id;
        FOR ahato_naytto IN
          SELECT * FROM aiemmin_hankitun_ammat_tutkinnon_osan_naytto WHERE aiemmin_hankittu_ammat_tutkinnon_osa_id = ahato.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE aiemmin_hankitun_ammat_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_ammat_tutkinnon_osa_id = ahato_naytto.aiemmin_hankittu_ammat_tutkinnon_osa_id AND osaamisen_osoittaminen_id = ahato_naytto.osaamisen_osoittaminen_id;
            FOR osaamisen_osoittaminen IN
              SELECT * FROM osaamisen_osoittamiset WHERE id = ahato_naytto.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittaminen.id;
                FOR osaamisen_osoittamisen_sisalto IN
                  SELECT * FROM osaamisen_osoittamisen_sisallot WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_sisalto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_yksilollinen_kriteeri IN
                  SELECT * FROM osaamisen_osoittamisen_yksilolliset_kriteerit WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_yksilollinen_kriteeri.id;
                  END LOOP;
                FOR nayttoymparisto IN
                  SELECT * FROM nayttoymparistot WHERE id = osaamisen_osoittaminen.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = nayttoymparisto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_osa_alue IN
                  SELECT * FROM osaamisen_osoittamisen_osa_alueet WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_osa_alue.osaamisen_osoittaminen_id;
                    FOR koodisto_koodi IN
                      SELECT * FROM koodisto_koodit WHERE id = osaamisen_osoittamisen_osa_alue.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koodisto_koodi.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_arvioija IN
                  SELECT * FROM osaamisen_osoittamisen_tyoelama_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_arvioija.osaamisen_osoittaminen_id;
                    FOR tyoelama_osaamisen_arvioija IN
                      SELECT * FROM tyoelama_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_arvioija.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyoelama_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_koulutuksen_jarjestaja_arv IN
                  SELECT * FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.osaamisen_osoittaminen_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                    FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                      SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
              END LOOP;
          END LOOP;
        FOR todennettu_arviointi_lisatieto IN
          SELECT * FROM todennettu_arviointi_lisatiedot WHERE id = ahato.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = todennettu_arviointi_lisatieto.id;
            FOR todennettu_arviointi_arvioija IN
              SELECT * FROM todennettu_arviointi_arvioijat WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_lisatieto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE todennettu_arviointi_arvioijat SET deleted_at = NEW.deleted_at WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_arvioija.todennettu_arviointi_lisatiedot_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                  SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                  END LOOP;
              END LOOP;
          END LOOP;
      END LOOP;

    -- ahpto
    FOR ahpto IN
      SELECT * FROM aiemmin_hankitut_paikalliset_tutkinnon_osat WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
      LOOP
        UPDATE aiemmin_hankitut_paikalliset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = ahpto.id;
        FOR ahpto_naytto IN
          SELECT * FROM aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto WHERE aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ahpto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ahpto_naytto.aiemmin_hankittu_paikallinen_tutkinnon_osa_id AND osaamisen_osoittaminen_id = ahpto_naytto.osaamisen_osoittaminen_id;
            FOR osaamisen_osoittaminen IN
              SELECT * FROM osaamisen_osoittamiset WHERE id = ahpto_naytto.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittaminen.id;
                FOR osaamisen_osoittamisen_sisalto IN
                  SELECT * FROM osaamisen_osoittamisen_sisallot WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_sisalto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_yksilollinen_kriteeri IN
                  SELECT * FROM osaamisen_osoittamisen_yksilolliset_kriteerit WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = osaamisen_osoittamisen_yksilollinen_kriteeri.id;
                  END LOOP;
                FOR nayttoymparisto IN
                  SELECT * FROM nayttoymparistot WHERE id = osaamisen_osoittaminen.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = nayttoymparisto.id;
                  END LOOP;
                FOR osaamisen_osoittamisen_osa_alue IN
                  SELECT * FROM osaamisen_osoittamisen_osa_alueet WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_osa_alue.osaamisen_osoittaminen_id;
                    FOR koodisto_koodi IN
                      SELECT * FROM koodisto_koodit WHERE id = osaamisen_osoittamisen_osa_alue.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koodisto_koodi.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_arvioija IN
                  SELECT * FROM osaamisen_osoittamisen_tyoelama_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_arvioija.osaamisen_osoittaminen_id;
                    FOR tyoelama_osaamisen_arvioija IN
                      SELECT * FROM tyoelama_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_arvioija.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = tyoelama_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
                FOR osaamisen_osoittamisen_koulutuksen_jarjestaja_arv IN
                  SELECT * FROM osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija WHERE osaamisen_osoittaminen_id = osaamisen_osoittaminen.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.osaamisen_osoittaminen_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                    FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                      SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = osaamisen_osoittamisen_koulutuksen_jarjestaja_arv.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                      LOOP
                        UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                      END LOOP;
                  END LOOP;
              END LOOP;
          END LOOP;
        FOR todennettu_arviointi_lisatieto IN
          SELECT * FROM todennettu_arviointi_lisatiedot WHERE id = ahpto.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = todennettu_arviointi_lisatieto.id;
            FOR todennettu_arviointi_arvioija IN
              SELECT * FROM todennettu_arviointi_arvioijat WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_lisatieto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE todennettu_arviointi_arvioijat SET deleted_at = NEW.deleted_at WHERE todennettu_arviointi_lisatiedot_id = todennettu_arviointi_arvioija.todennettu_arviointi_lisatiedot_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id;
                FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
                  SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = todennettu_arviointi_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
                  LOOP
                    UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
                  END LOOP;
              END LOOP;
          END LOOP;
        FOR ahpto_arvioija IN
          SELECT * FROM aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat WHERE aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ahpto.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
          LOOP
            UPDATE aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ahpto_arvioija.aiemmin_hankittu_paikallinen_tutkinnon_osa_id AND koulutuksen_jarjestaja_osaamisen_arvioija_id = ahpto_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id;
            FOR koulutuksen_jarjestaja_osaamisen_arvioija IN
              SELECT * FROM koulutuksen_jarjestaja_osaamisen_arvioijat WHERE id = ahpto_arvioija.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at)
              LOOP
                UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = koulutuksen_jarjestaja_osaamisen_arvioija.id;
              END LOOP;
          END LOOP;
      END LOOP;

    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cascading_shallow_delete
  AFTER UPDATE OF deleted_at ON hoksit
  FOR EACH ROW
  EXECUTE PROCEDURE cascading_shallow_delete();
