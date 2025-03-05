with oht as not materialized (
  select * from osaamisen_hankkimistavat oh
  where oh.deleted_at is null
  and oh.osaamisen_hankkimistapa_koodi_uri in ('osaamisenhankkimistapa_koulutussopimus','osaamisenhankkimistapa_oppisopimus')
  and oh.loppu >= :alkupvm
  and oh.loppu <= :loppupvm
)
select
    hato.hoks_id,
	hato.tutkinnon_osa_koodi_uri as tutkinnonosa_koodi,
	'hato' as tutkinnonosa_tyyppi,
	split_part(oht.osaamisen_hankkimistapa_koodi_uri, '_', 2) as hankkimistapa_tyyppi,
	oht.osa_aikaisuustieto as osa_aikaisuus,
        oht.alku as jakso_alkupvm,
        oht.loppu as jakso_loppupvm,
        split_part(oht.oppisopimuksen_perusta_koodi_uri, '_', 2) as oppisopimuksen_perusta,
	tjk.vastuullinen_tyopaikka_ohjaaja_nimi as ohjaaja_nimi,
	tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti as ohjaaja_email,
	tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero as ohjaaja_puhelinnumero,
	tjk.tyopaikan_nimi,
	tjk.tyopaikan_y_tunnus as tyopaikan_ytunnus,
	p.hoks_id,
	p.jakson_yksiloiva_tunniste as yksiloiva_tunniste,
	p.koulutustoimija,
	p.heratepvm as jakso_loppupvm,
	p.toimipiste_oid,
	p.tutkintonimike,
        p.tutkintotunnus as tutkinto,
	h.opiskeluoikeus_oid,
        h.oppija_oid,
	o.oppilaitos_oid as oppilaitos
FROM	hankittavat_ammat_tutkinnon_osat hato
JOIN	hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat hatooh
ON	(hato.id = hatooh.hankittava_ammat_tutkinnon_osa_id)
JOIN	oht
ON	(hatooh.osaamisen_hankkimistapa_id = oht.id)
join tyopaikalla_jarjestettavat_koulutukset tjk
on (tjk.id = oht.tyopaikalla_jarjestettava_koulutus_id)
join palautteet p
on (hato.hoks_id = p.hoks_id and oht.yksiloiva_tunniste = p.jakson_yksiloiva_tunniste)
join hoksit h
on (h.id = p.hoks_id)
join opiskeluoikeudet o
on (o."oid" = h.opiskeluoikeus_oid)
WHERE	hato.deleted_at IS NULL
AND	hatooh.deleted_at IS NULL
AND	oht.deleted_at IS null
and p.deleted_at is null
and p.kyselytyyppi = 'tyopaikkajakson_suorittaneet'
and p.tila = 'odottaa_kasittelya'
union all
select
    hpto.hoks_id,
	null as tutkinnonosa_koodi,
	'hpto' as tutkinnonosa_tyyppi,
	split_part(oht.osaamisen_hankkimistapa_koodi_uri, '_', 2) as hankkimistapa_tyyppi,
	oht.osa_aikaisuustieto as osa_aikaisuus,
        oht.alku as jakso_alkupvm,
        oht.loppu as jakso_loppupvm,
        split_part(oht.oppisopimuksen_perusta_koodi_uri, '_', 2) as oppisopimuksen_perusta,
	tjk.vastuullinen_tyopaikka_ohjaaja_nimi as ohjaaja_nimi,
	tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti as ohjaaja_email,
	tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero as ohjaaja_puhelinnumero,
	tjk.tyopaikan_nimi,
	tjk.tyopaikan_y_tunnus as tyopaikan_ytunnus,
	p.hoks_id,
	p.jakson_yksiloiva_tunniste as yksiloiva_tunniste,
	p.koulutustoimija,
	p.heratepvm as jakso_loppupvm,
	p.toimipiste_oid,
	p.tutkintonimike,
        p.tutkintotunnus as tutkinto,
	h.opiskeluoikeus_oid,
        h.oppija_oid,
	o.oppilaitos_oid as oppilaitos
FROM	hankittavat_paikalliset_tutkinnon_osat hpto
JOIN	hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat hptooh
ON	(hpto.id = hptooh.hankittava_paikallinen_tutkinnon_osa_id)
JOIN	oht
ON	(hptooh.osaamisen_hankkimistapa_id = oht.id)
join tyopaikalla_jarjestettavat_koulutukset tjk
on (tjk.id = oht.tyopaikalla_jarjestettava_koulutus_id)
join palautteet p
on (hpto.hoks_id = p.hoks_id and oht.yksiloiva_tunniste = p.jakson_yksiloiva_tunniste)
join hoksit h
on (h.id = p.hoks_id)
join opiskeluoikeudet o
on (o."oid" = h.opiskeluoikeus_oid)
where hpto.deleted_at IS NULL
AND	hptooh.deleted_at IS NULL
AND	oht.deleted_at IS null
and p.deleted_at is null
and p.kyselytyyppi = 'tyopaikkajakson_suorittaneet'
and p.tila = 'odottaa_kasittelya'
union all
select
    hyto.hoks_id,
	hyto.tutkinnon_osa_koodi_uri as tutkinnonosa_koodi,
	'hyto' as tutkinnonosa_tyyppi,
	split_part(oht.osaamisen_hankkimistapa_koodi_uri, '_', 2) as hankkimistapa_tyyppi,
	oht.osa_aikaisuustieto as osa_aikaisuus,
        oht.alku as jakso_alkupvm,
        oht.loppu as jakso_loppupvm,
        split_part(oht.oppisopimuksen_perusta_koodi_uri, '_', 2) as oppisopimuksen_perusta,
	tjk.vastuullinen_tyopaikka_ohjaaja_nimi as ohjaaja_nimi,
	tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti as ohjaaja_email,
	tjk.vastuullinen_tyopaikka_ohjaaja_puhelinnumero as ohjaaja_puhelinnumero,
	tjk.tyopaikan_nimi,
	tjk.tyopaikan_y_tunnus as tyopaikan_ytunnus,
	p.hoks_id,
	p.jakson_yksiloiva_tunniste as yksiloiva_tunniste,
	p.koulutustoimija,
	p.heratepvm as jakso_loppupvm,
	p.toimipiste_oid,
	p.tutkintonimike,
        p.tutkintotunnus as tutkinto,
	h.opiskeluoikeus_oid,
        h.oppija_oid,
	o.oppilaitos_oid as oppilaitos
from hankittavat_yhteiset_tutkinnon_osat hyto
join yhteisen_tutkinnon_osan_osa_alueet ytooa
on (hyto.id = ytooa.yhteinen_tutkinnon_osa_id)
join yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat ytooaoh
ON	(hyto.id = ytooaoh.yhteisen_tutkinnon_osan_osa_alue_id)
JOIN	oht
ON	(ytooaoh.osaamisen_hankkimistapa_id = oht.id)
join tyopaikalla_jarjestettavat_koulutukset tjk
on (tjk.id = oht.tyopaikalla_jarjestettava_koulutus_id)
join palautteet p
on (hyto.hoks_id = p.hoks_id and oht.yksiloiva_tunniste = p.jakson_yksiloiva_tunniste)
join hoksit h
on (h.id = p.hoks_id)
join opiskeluoikeudet o
on (o."oid" = h.opiskeluoikeus_oid)
where hyto.deleted_at IS null
and ytooa.deleted_at is null
AND	ytooaoh.deleted_at IS NULL
AND	oht.deleted_at IS null
and p.deleted_at is null
and p.kyselytyyppi = 'tyopaikkajakson_suorittaneet'
and p.tila = 'odottaa_kasittelya'
