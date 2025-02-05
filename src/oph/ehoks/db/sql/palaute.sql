-- :name insert! :? :1
-- :doc Insert palaute to DB
-- :require [oph.ehoks.db.sql :as sql]
insert into palautteet (
--~ (sql/target-columns-for-insert params)
) values (
--~ (sql/values-for-insert params)
) returning *

-- :name get-tep-palautteet-waiting-for-vastaajatunnus! :? :*
-- :doc Get all unprocessed palaute for Arvo call.
select * from tep_palaute
where tila = 'odottaa_kasittelya'
  and heratepvm <= current_date
  and arvo_tunniste is null
  and tep_kasitelty = false

-- :name get-amis-palautteet-waiting-for-kyselylinkki! :? :*
-- :doc Get HOKS-id and kyselytyyppi of amispalaute without kyselylinkki
select	id, hoks_id, tila, kyselytyyppi
from	palautteet
where	tila = 'odottaa_kasittelya'
and	kyselytyyppi in ('aloittaneet','valmistuneet','osia_suorittaneet')
and	heratepvm <= :heratepvm
and	arvo_tunniste is null
and	deleted_at is null

-- :name get-palautteet-waiting-for-vastaajatunnus! :? :*
-- :doc List all unhandled palautteet whose herätepäivä has come
with oht_hoks_mapping as not materialized (
 select	oht.id as hankkimistapa_id,
	oht.yksiloiva_tunniste,
	osa.hoks_id
 from	hankittavat_ammat_tutkinnon_osat osa
 join	hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat osaoh
	on (osa.id = osaoh.hankittava_ammat_tutkinnon_osa_id)
 join	osaamisen_hankkimistavat oht
	on (osaoh.osaamisen_hankkimistapa_id = oht.id)
 where	osa.deleted_at is null
 and	osaoh.deleted_at is null
 and	oht.deleted_at is null
union
 select	oht.id as hankkimistapa_id,
	oht.yksiloiva_tunniste,
	osa.hoks_id
 from	hankittavat_paikalliset_tutkinnon_osat osa
 join	hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat osaoh
	on (osa.id = osaoh.hankittava_paikallinen_tutkinnon_osa_id)
 join	osaamisen_hankkimistavat oht
	on (osaoh.osaamisen_hankkimistapa_id = oht.id)
 where	osa.deleted_at is null
 and	osaoh.deleted_at is null
 and	oht.deleted_at is null
union
 select	oht.id as hankkimistapa_id,
	oht.yksiloiva_tunniste,
	osa.hoks_id
 from	hankittavat_yhteiset_tutkinnon_osat osa
 join	yhteisen_tutkinnon_osan_osa_alueet alue
	on (osa.id = alue.yhteinen_tutkinnon_osa_id)
 join	yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat alueoh
	on (alue.id = alueoh.yhteisen_tutkinnon_osan_osa_alue_id)
 join	osaamisen_hankkimistavat oht
	on (alueoh.osaamisen_hankkimistapa_id = oht.id)
 where	osa.deleted_at is null
 and	alue.deleted_at is null
 and	alueoh.deleted_at is null
 and	oht.deleted_at is null
)
select	p.id, p.hoks_id, p.heratepvm, p.tila, p.kyselytyyppi,
	p.jakson_yksiloiva_tunniste, ohm.hankkimistapa_id
from	palautteet p
left join oht_hoks_mapping ohm
on	(p.hoks_id = ohm.hoks_id
		and p.jakson_yksiloiva_tunniste = ohm.yksiloiva_tunniste)
where	p.tila = 'odottaa_kasittelya'
and	p.kyselytyyppi in (:v*:kyselytyypit)
and	p.heratepvm <= now()
and	p.deleted_at is null
order by hoks_id asc

-- :name get-tep-palaute-waiting-for-vastaajatunnus! :? :1
-- :doc Get single unprocessed palaute for Arvo call.
select * from tep_palaute
where id = :palaute-id
  and tila = 'odottaa_kasittelya'
  and heratepvm <= current_date
  and arvo_tunniste is null
  and tep_kasitelty = false

-- :name update-arvo-tunniste! :? :*
-- :doc Update arvo-tunniste for palaute with given id.
update	palautteet
set	arvo_tunniste = :tunnus,
	kyselylinkki = :url,
	updated_at = now(),
	tila = :tila
where	id = :id
  and	arvo_tunniste is null
  and	tila = 'odottaa_kasittelya'
returning *

-- :name update! :? :1
-- :doc Update palaute in DB
-- :require [oph.ehoks.db.sql :as sql]
update palautteet
--~ (sql/set-clause-for-update params options)
where	id = :id
returning *

-- :name get-by-hoks-id-and-kyselytyypit! :? :*
-- :doc Get opiskelijapalaute information by HOKS ID and kyselytyyppi
select * from palautteet
where hoks_id = :hoks-id and kyselytyyppi in (:v*:kyselytyypit)

-- :name get-by-kyselytyyppi-oppija-and-koulutustoimija! :? :*
-- :doc Get kyselyt by kyselytyyppi, oppija OID, and koulutustoimija.
select p.id, p.heratepvm, p.tila
from palautteet p
join hoksit h on (h.id = p.hoks_id)
where h.oppija_oid = :oppija-oid  -- FIXME: should probably have deleted_at cond
  and p.kyselytyyppi in (:v*:kyselytyypit)
  and (p.koulutustoimija = :koulutustoimija or (:koulutustoimija)::text is null)

-- :name get-by-id! :? :1
-- :doc Get palaute by palaute id.
select * from palautteet
where id = :id

-- :name get-by-hoks-id-and-yksiloiva-tunniste! :? :1
-- :doc Get palaute information for työpaikkajakso by HOKS ID and yksiloiva
--      tunniste.
select * from palautteet
where hoks_id = :hoks-id AND jakson_yksiloiva_tunniste = :yksiloiva-tunniste

-- :name get-for-heratepalvelu-by-hoks-id-and-kyselytyypit! :? :*
-- :doc get AMIS-palaute in the format for putting into herätepalvelu
select * from palaute_for_amis_heratepalvelu
where ehoks_id = :hoks-id  -- FIXME: should probably have deleted_at cond
			   -- FIXME: should choose the newest palaute
  and internal_kyselytyyppi in (:v*:kyselytyypit)

-- :name get-for-arvo-by-hoks-id-and-kyselytyyppi! :? :1
-- :doc get AMIS-palaute in the format for creating vastaajatunnus in Arvo
WITH tutkinnonosat AS NOT MATERIALIZED (
SELECT	hato.hoks_id,
	hato.tutkinnon_osa_koodi_uri,
	oht.osaamisen_hankkimistapa_koodi_uri
FROM	hankittavat_ammat_tutkinnon_osat hato
JOIN	hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat hatooh
ON	(hato.id = hatooh.hankittava_ammat_tutkinnon_osa_id)
JOIN	osaamisen_hankkimistavat oht
ON	(hatooh.osaamisen_hankkimistapa_id = oht.id)
WHERE	hato.deleted_at IS NULL
AND	hatooh.deleted_at IS NULL
AND	oht.deleted_at IS NULL
UNION ALL
SELECT	hyto.hoks_id,
	hyto.tutkinnon_osa_koodi_uri,
	oht.osaamisen_hankkimistapa_koodi_uri
FROM	hankittavat_yhteiset_tutkinnon_osat hyto
JOIN	yhteisen_tutkinnon_osan_osa_alueet hytooa
ON	(hytooa.yhteinen_tutkinnon_osa_id = hyto.id)
JOIN	yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat hytooaoh
ON	(hytooaoh.yhteisen_tutkinnon_osan_osa_alue_id = hytooa.id)
JOIN	osaamisen_hankkimistavat oht
ON	(hytooaoh.osaamisen_hankkimistapa_id = oht.id)
WHERE	hyto.deleted_at IS NULL
AND	hytooa.deleted_at IS NULL
AND	hytooaoh.deleted_at IS NULL
AND	oht.deleted_at IS NULL
)
SELECT	p.tila,
	p.hankintakoulutuksen_toteuttaja,
	p.voimassa_alkupvm AS vastaamisajan_alkupvm,
	p.suorituskieli AS tutkinnon_suorituskieli,
	p.kyselytyyppi AS kyselyn_tyyppi,
	NULL AS osaamisala,  -- TODO: populate in opiskeluoikeudet
	ARRAY(
		SELECT DISTINCT	tutkinnon_osa_koodi_uri
		FROM	tutkinnonosat
		WHERE	tutkinnonosat.hoks_id = p.hoks_id
		AND	osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
	AS tutkinnonosat_oppisopimus,
	ARRAY(
		SELECT DISTINCT	tutkinnon_osa_koodi_uri
		FROM	tutkinnonosat
		WHERE	tutkinnonosat.hoks_id = p.hoks_id
		AND	osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus')
	AS tutkinnonosat_koulutussopimus,
	ARRAY(
		SELECT DISTINCT	tutkinnon_osa_koodi_uri
		FROM	tutkinnonosat
		WHERE	tutkinnonosat.hoks_id = p.hoks_id
		AND	osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus')
	AS tutkinnonosat_oppilaitosmuotoinenkoulutus,
	p.toimipiste_oid,
	p.voimassa_loppupvm AS vastaamisajan_loppupvm,
	p.tutkintotunnus,
	oo.oppilaitos_oid,
	p.koulutustoimija AS koulutustoimija_oid,
	p.heratepvm
FROM	palautteet p
JOIN	hoksit h
ON	(p.hoks_id = h.id)
JOIN	opiskeluoikeudet oo
ON	(h.opiskeluoikeus_oid = oo.oid)
WHERE	p.hoks_id = :hoks-id
AND	p.kyselytyyppi = :kyselytyyppi
AND	p.tila = 'odottaa_kasittelya'
AND	p.arvo_tunniste IS NULL

-- :name get-for-heratepalvelu-by-hoks-id-and-yksiloiva-tunniste! :? :*
-- :doc get tep-jaksopalaute in the format for putting into herätepalvelu
select * from palaute_for_tep_heratepalvelu
where hoks_id = :hoks-id
  and jakson_yksiloiva_tunniste = :jakson-yksiloiva-tunniste
  and internal_kyselytyyppi = 'tyopaikkajakson_suorittaneet'
