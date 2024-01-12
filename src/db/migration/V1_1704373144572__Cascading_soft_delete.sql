-- hoks päätaso

CREATE OR REPLACE FUNCTION hoksit_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE hankittavat_koulutuksen_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE hankittavat_yhteiset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE hankittavat_ammat_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE hankittavat_paikalliset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE aiemmin_hankitut_yhteiset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE aiemmin_hankitut_ammat_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE aiemmin_hankitut_paikalliset_tutkinnon_osat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE hoks_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hoksit_casc_delete
  AFTER UPDATE OF deleted_at ON hoksit
  FOR EACH ROW EXECUTE PROCEDURE hoksit_casc_delete();


-- hyto

CREATE OR REPLACE FUNCTION hyto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE yhteisen_tutkinnon_osan_osa_alueet SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE yhteinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hyto_casc_delete
  AFTER UPDATE OF deleted_at ON hankittavat_yhteiset_tutkinnon_osat
  FOR EACH ROW EXECUTE PROCEDURE hyto_casc_delete();

CREATE OR REPLACE FUNCTION hyto_osa_alue_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at WHERE yhteisen_tutkinnon_osan_osa_alue_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE yhteisen_tutkinnon_osan_osa_alueen_naytot SET deleted_at = NEW.deleted_at WHERE yhteisen_tutkinnon_osan_osa_alue_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hyto_osa_alue_casc_delete
  AFTER UPDATE OF deleted_at ON yhteisen_tutkinnon_osan_osa_alueet
  FOR EACH ROW EXECUTE PROCEDURE hyto_osa_alue_casc_delete();

CREATE OR REPLACE FUNCTION hyto_osa_alueen_osaamisen_hankkimistapa_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_hankkimistapa_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hyto_osa_alueen_osaamisen_hankkimistapa_casc_delete
  AFTER UPDATE OF deleted_at ON yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
  FOR EACH ROW EXECUTE PROCEDURE hyto_osa_alueen_osaamisen_hankkimistapa_casc_delete();

CREATE OR REPLACE FUNCTION hyto_osa_alueen_naytto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hyto_osa_alueen_naytto_casc_delete
  AFTER UPDATE OF deleted_at ON yhteisen_tutkinnon_osan_osa_alueen_naytot
  FOR EACH ROW EXECUTE PROCEDURE hyto_osa_alueen_naytto_casc_delete();


-- hato

CREATE OR REPLACE FUNCTION hato_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at WHERE hankittava_ammat_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE hankittavan_ammat_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE hankittava_ammat_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hato_casc_delete
  AFTER UPDATE OF deleted_at ON hankittavat_ammat_tutkinnon_osat
  FOR EACH ROW EXECUTE PROCEDURE hato_casc_delete();

CREATE OR REPLACE FUNCTION hato_osaamisen_hankkimistapa_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_hankkimistapa_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hato_osaamisen_hankkimistapa_casc_delete
  AFTER UPDATE OF deleted_at ON hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
  FOR EACH ROW EXECUTE PROCEDURE hato_osaamisen_hankkimistapa_casc_delete();

CREATE OR REPLACE FUNCTION hato_naytto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hato_naytto_casc_delete
  AFTER UPDATE OF deleted_at ON hankittavan_ammat_tutkinnon_osan_naytto
  FOR EACH ROW EXECUTE PROCEDURE hato_naytto_casc_delete();


-- hpto

CREATE OR REPLACE FUNCTION hpto_casc_delete() RETURNS TRIGGER AS $$
BEGIN
  UPDATE hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at WHERE hankittava_paikallinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
  UPDATE hankittavan_paikallisen_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE hankittava_paikallinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hpto_casc_delete
  AFTER UPDATE OF deleted_at ON hankittavat_paikalliset_tutkinnon_osat
  FOR EACH ROW EXECUTE PROCEDURE hpto_casc_delete();

CREATE OR REPLACE FUNCTION hpto_osaamisen_hankkimistapa_casc_delete() RETURNS TRIGGER AS $$
BEGIN
  UPDATE osaamisen_hankkimistavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_hankkimistapa_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hpto_osaamisen_hankkimistapa_casc_delete
  AFTER UPDATE OF deleted_at ON hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
  FOR EACH ROW EXECUTE PROCEDURE hpto_osaamisen_hankkimistapa_casc_delete();

CREATE OR REPLACE FUNCTION hpto_naytto_casc_delete() RETURNS TRIGGER AS $$
BEGIN
  UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_hpto_naytto_casc_delete
  AFTER UPDATE OF deleted_at ON hankittavan_paikallisen_tutkinnon_osan_naytto
  FOR EACH ROW EXECUTE PROCEDURE hpto_naytto_casc_delete();


-- ahyto

CREATE OR REPLACE FUNCTION ahyto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE aiemmin_hankitut_yto_osa_alueet SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_yhteinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahyto_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitut_yhteiset_tutkinnon_osat
  FOR EACH ROW EXECUTE PROCEDURE ahyto_casc_delete();

CREATE OR REPLACE FUNCTION ahyto_osa_alue_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE aiemmin_hankitun_yto_osa_alueen_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_yto_osa_alue_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahyto_osa_alue_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitut_yto_osa_alueet
  FOR EACH ROW EXECUTE PROCEDURE ahyto_osa_alue_casc_delete();

CREATE OR REPLACE FUNCTION ahyto_osa_alueen_naytto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahyto_osa_alueen_naytto_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitun_yto_osa_alueen_naytto
  FOR EACH ROW EXECUTE PROCEDURE ahyto_osa_alueen_naytto_casc_delete();

CREATE OR REPLACE FUNCTION ahyto_naytto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahyto_naytto_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
  FOR EACH ROW EXECUTE PROCEDURE ahyto_naytto_casc_delete();

CREATE OR REPLACE FUNCTION ahyto_arvioija_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahyto_arvioija_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
  FOR EACH ROW EXECUTE PROCEDURE ahyto_arvioija_casc_delete();


-- ahato

CREATE OR REPLACE FUNCTION ahato_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE aiemmin_hankitun_ammat_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_ammat_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahato_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitut_ammat_tutkinnon_osat
  FOR EACH ROW EXECUTE PROCEDURE ahato_casc_delete();

CREATE OR REPLACE FUNCTION ahato_naytto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahato_naytto_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitun_ammat_tutkinnon_osan_naytto
  FOR EACH ROW EXECUTE PROCEDURE ahato_naytto_casc_delete();


-- ahpto

CREATE OR REPLACE FUNCTION ahpto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_paikallinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE todennettu_arviointi_lisatiedot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.tarkentavat_tiedot_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat SET deleted_at = NEW.deleted_at WHERE aiemmin_hankittu_paikallinen_tutkinnon_osa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahpto_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitut_paikalliset_tutkinnon_osat
  FOR EACH ROW EXECUTE PROCEDURE ahpto_casc_delete();

CREATE OR REPLACE FUNCTION ahpto_naytto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_osoittamiset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.osaamisen_osoittaminen_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahpto_naytto_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
  FOR EACH ROW EXECUTE PROCEDURE ahpto_naytto_casc_delete();

CREATE OR REPLACE FUNCTION ahpto_arvioija_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_ahpto_arvioija_casc_delete
  AFTER UPDATE OF deleted_at ON aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat
  FOR EACH ROW EXECUTE PROCEDURE ahpto_arvioija_casc_delete();


-- common

CREATE OR REPLACE FUNCTION osaamisen_hankkimistapa_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE tyopaikalla_jarjestettavat_koulutukset SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.tyopaikalla_jarjestettava_koulutus_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE keskeytymisajanjaksot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE osaamisen_hankkimistapa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE muut_oppimisymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE osaamisen_hankkimistapa_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_osaamisen_hankkimistapa_casc_delete
  AFTER UPDATE OF deleted_at ON osaamisen_hankkimistavat
  FOR EACH ROW EXECUTE PROCEDURE osaamisen_hankkimistapa_casc_delete();

CREATE OR REPLACE FUNCTION tyopaikalla_jarj_koulutus_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE tyopaikalla_jarjestettava_koulutus_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_tyopaikalla_jarj_koulutus_casc_delete
  AFTER UPDATE OF deleted_at ON tyopaikalla_jarjestettavat_koulutukset
  FOR EACH ROW EXECUTE PROCEDURE tyopaikalla_jarj_koulutus_casc_delete();

CREATE OR REPLACE FUNCTION osaamisen_osoittaminen_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE osaamisen_osoittamisen_sisallot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE osaamisen_osoittaminen_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE osaamisen_osoittamisen_yksilolliset_kriteerit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE osaamisen_osoittaminen_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE nayttoymparistot SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.nayttoymparisto_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE osaamisen_osoittamisen_osa_alueet SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE osaamisen_osoittamisen_tyoelama_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    UPDATE osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija SET deleted_at = NEW.deleted_at WHERE osaamisen_osoittaminen_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_osaamisen_osoittaminen_casc_delete
  AFTER UPDATE OF deleted_at ON osaamisen_osoittamiset
  FOR EACH ROW EXECUTE PROCEDURE osaamisen_osoittaminen_casc_delete();

CREATE OR REPLACE FUNCTION osaamisen_osoittamisen_osa_alue_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE koodisto_koodit SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.koodisto_koodi_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_osaamisen_osoittamisen_osa_alue_casc_delete
  AFTER UPDATE OF deleted_at ON osaamisen_osoittamisen_osa_alueet
  FOR EACH ROW EXECUTE PROCEDURE osaamisen_osoittamisen_osa_alue_casc_delete();

CREATE OR REPLACE FUNCTION osaamisen_osoittamisen_arvioija_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE tyoelama_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.tyoelama_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_osaamisen_osoittamisen_arvioija_casc_delete
  AFTER UPDATE OF deleted_at ON osaamisen_osoittamisen_tyoelama_arvioija
  FOR EACH ROW EXECUTE PROCEDURE osaamisen_osoittamisen_arvioija_casc_delete();

CREATE OR REPLACE FUNCTION osaamisen_osoittamisen_koulutuksen_jarjestaja_arv_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_osaamisen_osoittamisen_koulutuksen_jarjestaja_arv_casc_delete
  AFTER UPDATE OF deleted_at ON osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
  FOR EACH ROW EXECUTE PROCEDURE osaamisen_osoittamisen_koulutuksen_jarjestaja_arv_casc_delete();

CREATE OR REPLACE FUNCTION todennettu_arviointi_lisatieto_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE todennettu_arviointi_arvioijat SET deleted_at = NEW.deleted_at WHERE todennettu_arviointi_lisatiedot_id = OLD.id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_todennettu_arviointi_lisatieto_casc_delete
  AFTER UPDATE OF deleted_at ON todennettu_arviointi_lisatiedot
  FOR EACH ROW EXECUTE PROCEDURE todennettu_arviointi_lisatieto_casc_delete();

CREATE OR REPLACE FUNCTION todennettu_arviointi_arvioija_casc_delete() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE koulutuksen_jarjestaja_osaamisen_arvioijat SET deleted_at = NEW.deleted_at, updated_at = coalesce(NEW.deleted_at, current_timestamp) WHERE id = OLD.koulutuksen_jarjestaja_osaamisen_arvioija_id AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER t_todennettu_arviointi_arvioija_casc_delete
  AFTER UPDATE OF deleted_at ON todennettu_arviointi_arvioijat
  FOR EACH ROW EXECUTE PROCEDURE todennettu_arviointi_arvioija_casc_delete();
