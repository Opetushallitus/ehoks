UPDATE palautteet
SET tila = 'heratepalvelussa'
WHERE tila in (
	'ei_laheteta',
	'odottaa_kasittelya',
	'kysely_muodostettu',
	'vastaajatunnus_muodostettu')
AND (kyselytyyppi in ('aloittaneet', 'valmistuneet', 'osia_suorittaneet')
	OR (kyselytyyppi = 'tyopaikkajakson_suorittaneet'
		AND heratepvm <= now()));
