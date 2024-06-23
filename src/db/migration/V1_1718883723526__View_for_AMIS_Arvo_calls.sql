CREATE VIEW palaute_for_arvo AS
SELECT p.hankintakoulutuksen_toteuttaja,
	p.voimassa_alkupvm AS vastaamisajan_alkupvm,
	p.suorituskieli AS tutkinnon_suorituskieli,
	p.kyselytyyppi AS kyselyn_tyyppi,
	p.tila AS palautteen_tila,
	oo.osaamisala_nimi AS osaamisala,
	p.toimipiste_oid,
	p.voimassa_loppupvm AS vastaamisajan_loppupvm,
	p.tutkintotunnus,
	oo.oppilaitos_oid,
	oo.koulutustoimija_oid,
	p.heratepvm
FROM palautteet p
JOIN hoksit h ON (p.hoks_id = h.id)
JOIN opiskeluoikeudet oo ON (h.opiskeluoikeus_oid = oo.oid);
