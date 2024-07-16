CREATE VIEW palaute_for_tep_heratepalvelu AS
SELECT
  jakson_yksiloiva_tunniste,
  kyselytyyppi AS internal_kyselytyyppi,
  hankkimistapa_id,
  hankkimistapa_tyyppi,
  hoks_id,
  alkupvm::text AS jakso_alkupvm,
  loppupvm::text AS jakso_loppupvm,
  tyopaikkaohjaaja_email AS ohjaaja_email,
  tyopaikkaohjaaja_nimi AS ohjaaja_nimi,
  tyopaikkaohjaaja_puhelinnumero AS ohjaaja_puhelinnumero,
  opiskeluoikeus_oid,
  oppisopimuksen_perusta,
  osa_aikaisuus,
  CASE WHEN EXTRACT(MONTH FROM heratepvm) <= 6
    THEN CONCAT(EXTRACT(YEAR FROM heratepvm) - 1, '-',
                EXTRACT(YEAR FROM heratepvm))
    ELSE CONCAT(EXTRACT(YEAR FROM heratepvm), '-',
                EXTRACT(YEAR FROM heratepvm) + 1) END AS rahoituskausi,
  tutkinnonosa_id,
  tutkinnonosa_koodi,
  tutkinnonosa_nimi,
  tyyppi AS tutkinnonosa_tyyppi,
  tyopaikan_nimi,
  tyopaikan_ytunnus
FROM tep_palaute
