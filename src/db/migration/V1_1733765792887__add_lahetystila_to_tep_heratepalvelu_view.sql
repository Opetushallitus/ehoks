DROP VIEW palaute_for_tep_heratepalvelu;

CREATE VIEW palaute_for_tep_heratepalvelu AS
SELECT
  jakson_yksiloiva_tunniste,
  kyselytyyppi AS internal_kyselytyyppi,
  hankkimistapa_id,
  osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
  hoks_id,
  alkupvm::text AS jakso_alkupvm,
  loppupvm::text AS jakso_loppupvm,
  vastuullinen_tyopaikka_ohjaaja_sahkoposti AS ohjaaja_email,
  vastuullinen_tyopaikka_ohjaaja_nimi AS ohjaaja_nimi,
  vastuullinen_tyopaikka_ohjaaja_puhelinnumero AS ohjaaja_puhelinnumero,
  opiskeluoikeus_oid,
  oppisopimuksen_perusta_koodi_uri AS oppisopimuksen_perusta,
  osa_aikaisuustieto AS osa_aikaisuus,
  CASE tila
    WHEN 'odottaa_kasittelya' THEN 'ei_niputettu'
    WHEN 'vastaajatunnus_muodostettu' THEN 'ei_niputettu'
    ELSE tila::text END AS kasittelytila,
  CASE WHEN EXTRACT(MONTH FROM heratepvm) <= 6
    THEN CONCAT(EXTRACT(YEAR FROM heratepvm) - 1, '-',
                EXTRACT(YEAR FROM heratepvm))
    ELSE CONCAT(EXTRACT(YEAR FROM heratepvm), '-',
                EXTRACT(YEAR FROM heratepvm) + 1) END AS rahoituskausi,
  tutkinnonosa_id,
  tutkinnon_osa_koodi_uri AS tutkinnonosa_koodi,
  tutkinnonosa_nimi,
  tyyppi AS tutkinnonosa_tyyppi,
  tyopaikan_nimi,
  tyopaikan_y_tunnus AS tyopaikan_ytunnus
FROM tep_palaute;
