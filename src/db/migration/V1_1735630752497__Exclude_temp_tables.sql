CREATE OR REPLACE FUNCTION refresh_reporting(target_schema TEXT) RETURNS BOOLEAN AS
$BODY$
DECLARE
    tbl RECORD;
    ts TIMESTAMP WITH TIME ZONE;
BEGIN
    IF lower(target_schema) NOT LIKE 'repo%' THEN
        RETURN false;
    ELSE
        SELECT now() INTO ts;
        EXECUTE format('DROP SCHEMA IF EXISTS %I CASCADE', target_schema);
        EXECUTE format('CREATE SCHEMA %I', target_schema);
        FOR tbl IN
            SELECT *
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_type = 'BASE TABLE'
              AND table_name NOT LIKE 'tmp_%'
              AND table_name NOT LIKE 'temp_%'
              AND table_name NOT IN ('flyway_schema_history',
                                     'aiemmin_hankitun_ammat_tutkinnon_osan_naytto',
                                     'aiemmin_hankitut_ammat_tutkinnon_osat',
                                     'aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat',
                                     'aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto',
                                     'aiemmin_hankitut_paikalliset_tutkinnon_osat',
                                     'aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat',
                                     'aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto',
                                     'aiemmin_hankitut_yhteiset_tutkinnon_osat',
                                     'aiemmin_hankitun_yto_osa_alueen_naytto',
                                     'aiemmin_hankitut_yto_osa_alueet',
                                     'hankittavan_paikallisen_tutkinnon_osan_naytto',
                                     'hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat',
                                     'hankittavat_paikalliset_tutkinnon_osat',
                                     'hankittavan_ammat_tutkinnon_osan_naytto',
                                     'hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat',
                                     'hankittavat_ammat_tutkinnon_osat',
                                     'hankittavat_yhteiset_tutkinnon_osat',
                                     'kyselylinkit',
                                     'osaamisen_hankkimistavat',
                                     'sessions',
                                     'shared_modules',
                                     'opiskeluoikeudet',
                                     'oppijat',
                                     'yhteisen_tutkinnon_osan_osa_alueen_naytot',
                                     'yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat',
                                     'yhteisen_tutkinnon_osan_osa_alueet')
            LOOP
                EXECUTE format('CREATE TABLE %I.%I AS SELECT * FROM public.%I WHERE created_at <= $1',
                               target_schema, tbl.table_name, tbl.table_name) USING ts;
            END LOOP;
        EXECUTE format(
                'CREATE TABLE %I.kyselylinkit AS SELECT * FROM public.kyselylinkit k WHERE exists(SELECT 1 FROM hoksit WHERE id = k.hoks_id)',
                target_schema);
        EXECUTE format(
                'CREATE TABLE %I.shared_modules AS SELECT * FROM public.shared_modules k WHERE exists(SELECT 1 FROM hoksit WHERE eid = k.hoks_eid)',
                target_schema);
        EXECUTE format(
                'CREATE TABLE %I.opiskeluoikeudet AS SELECT * FROM public.opiskeluoikeudet',
                target_schema);
        EXECUTE format(
                'CREATE TABLE %I.oppijat AS SELECT * FROM public.oppijat',
                target_schema);
        EXECUTE format(
                'CREATE TABLE %I.aiemmin_hankitut_tutkinnon_osat AS SELECT * FROM public.v_aiemmin_hankitut_tutkinnon_osat WHERE created_at <= $1',
                target_schema) USING ts;
        EXECUTE format(
                'CREATE TABLE %I.hankittavat_tutkinnon_osat AS SELECT * FROM public.v_hankittavat_tutkinnon_osat WHERE created_at <= $1',
                target_schema) USING ts;
        EXECUTE format(
                'CREATE TABLE %I.osaamisen_hankkimistavat AS SELECT * FROM public.v_osaamisen_hankkimistavat WHERE created_at <= $1',
                target_schema) USING ts;
        EXECUTE format(
                'CREATE TABLE %I.aiemmin_hankitun_tutkinnon_osan_naytto AS SELECT * FROM public.v_aiemmin_hankitun_tutkinnon_osan_naytto WHERE created_at <= $1',
                target_schema) USING ts;
        EXECUTE format(
                'CREATE TABLE %I.aiemmin_hankitun_tutkinnon_osan_arvioijat AS SELECT * FROM public.v_aiemmin_hankitun_tutkinnon_osan_arvioijat WHERE created_at <= $1',
                target_schema) USING ts;
        EXECUTE format(
                'CREATE TABLE %I.hankittavan_tutkinnon_osan_naytto AS SELECT * FROM public.v_hankittavan_tutkinnon_osan_naytto WHERE created_at <= $1',
                target_schema) USING ts;

        PERFORM add_reporting_constraints(target_schema);

        RETURN true;
    END IF;
END;
$BODY$
    LANGUAGE plpgsql;
