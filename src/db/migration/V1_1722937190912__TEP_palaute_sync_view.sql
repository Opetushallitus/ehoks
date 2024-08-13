CREATE VIEW palaute_for_tep_heratepalvelu AS
SELECT
  jakson_yksiloiva_tunniste,
  kyselytyyppi AS internal_kyselytyyppi,
  hankkimistapa_id,
  osaamisen_hankkimistapa_koodi_uri,
  hoks_id,
  alkupvm::text AS jakso_alkupvm,
  loppupvm::text AS jakso_loppupvm,
  vastuullinen_tyopaikka_ohjaaja_sahkoposti,
  vastuullinen_tyopaikka_ohjaaja_nimi,
  vastuullinen_tyopaikka_ohjaaja_puhelinnumero,
  opiskeluoikeus_oid,
  oppisopimuksen_perusta_koodi_uri,
  osa_aikaisuustieto,
  CASE WHEN EXTRACT(MONTH FROM heratepvm) <= 6
    THEN CONCAT(EXTRACT(YEAR FROM heratepvm) - 1, '-',
                EXTRACT(YEAR FROM heratepvm))
    ELSE CONCAT(EXTRACT(YEAR FROM heratepvm), '-',
                EXTRACT(YEAR FROM heratepvm) + 1) END AS rahoituskausi,
  tutkinnonosa_id,
  tutkinnon_osa_koodi_uri,
  tutkinnonosa_nimi,
  tyyppi AS tutkinnonosa_tyyppi,
  tyopaikan_nimi,
  tyopaikan_y_tunnus
FROM tep_palaute
