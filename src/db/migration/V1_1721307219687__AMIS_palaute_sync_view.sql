
CREATE VIEW palaute_for_amis_heratepalvelu AS
SELECT
	h.sahkoposti,
	p.heratepvm,
	p.voimassa_loppupvm,
	p.toimipiste_oid,
	h.puhelinnumero,
	p.hankintakoulutuksen_toteuttaja,
	p.voimassa_alkupvm AS alkupvm,
	p.hoks_id AS ehoks_id,
	CASE p.herate_source
		WHEN 'ehoks_update' THEN 'sqs_viesti_ehoksista'
		WHEN 'koski_update' THEN 'tiedot_muuttuneet_koskessa'
		ELSE p.herate_source::TEXT END AS herate_source,
	p.koulutustoimija,
	p.kyselylinkki,
	p.tyyppi AS kyselytyyppi,
	p.kyselytyyppi as internal_kyselytyyppi,
	h.opiskeluoikeus_oid,
	h.oppija_oid,
	oo.oppilaitos_oid AS oppilaitos,
	NULL AS osaamisala,  -- TODO: populate this in opiskeluoikeudet
	p.rahoituskausi,
	p.suorituskieli,
	date(p.created_at) AS tallennuspvm,
	p.tutkintotunnus,
	concat(p.koulutustoimija, '/', oo.oppija_oid) AS toimija_oppija,
	concat(p.tyyppi, '/', p.rahoituskausi) AS tyyppi_kausi
FROM (SELECT *,
	CASE WHEN EXTRACT(MONTH FROM heratepvm) <= 6
		THEN CONCAT(EXTRACT(YEAR FROM heratepvm) - 1, '-',
			EXTRACT(YEAR FROM heratepvm))
		ELSE CONCAT(EXTRACT(YEAR FROM heratepvm), '-',
			EXTRACT(YEAR FROM heratepvm) + 1) END
		AS rahoituskausi,
	CASE kyselytyyppi
		WHEN 'valmistuneet' THEN 'tutkinnon_suorittaneet'
		WHEN 'osia_suorittaneet' THEN 'tutkinnon_osia_suorittaneet'
		ELSE kyselytyyppi::TEXT END
		AS tyyppi
	FROM palautteet
	WHERE kyselytyyppi IN
		('aloittaneet','valmistuneet','osia_suorittaneet'))
	AS p
JOIN hoksit h ON (p.hoks_id = h.id)
JOIN opiskeluoikeudet oo ON (h.opiskeluoikeus_oid = oo.oid);
