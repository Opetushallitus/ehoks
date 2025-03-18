-- :name insert! :? :1
-- :doc Insert palaute to DB
-- :require [oph.ehoks.db.sql :as sql]
insert into palautteet (
--~ (sql/target-columns-for-insert params)
) values (
--~ (sql/values-for-insert params)
) returning *

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
select	p.id,
	p.hoks_id,
	p.heratepvm,
	p.tila,
	p.herate_source,
	p.kyselytyyppi,
	p.jakson_yksiloiva_tunniste,
	ohm.hankkimistapa_id
from	palautteet p
left join oht_hoks_mapping ohm
on	(p.hoks_id = ohm.hoks_id
		and p.jakson_yksiloiva_tunniste = ohm.yksiloiva_tunniste)
where	p.tila = 'odottaa_kasittelya'
and	p.kyselytyyppi in (:v*:kyselytyypit)
and	(:hoks-id ::int is null or p.hoks_id = :hoks-id ::int)
and	(:palaute-id ::int is null or p.id = :palaute-id ::int)
and	p.heratepvm <= now()
and	p.deleted_at is null
order by hoks_id asc

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
where hoks_id = :hoks-id
  and kyselytyyppi in (:v*:kyselytyypit)
  and deleted_at is null

-- :name get-by-kyselytyyppi-oppija-and-koulutustoimija! :? :*
-- :doc Get kyselyt by kyselytyyppi, oppija OID, and koulutustoimija.
select p.id, p.heratepvm, p.tila
from palautteet p
join hoksit h on (h.id = p.hoks_id)
where h.oppija_oid = :oppija-oid  -- FIXME: should probably have deleted_at cond
  and p.kyselytyyppi in (:v*:kyselytyypit)
  and (p.koulutustoimija = :koulutustoimija or (:koulutustoimija)::text is null)
  and p.deleted_at is null
  and h.deleted_at is null

-- :name get-by-id! :? :1
-- :doc Get palaute by palaute id.
select * from palautteet
where id = :id

-- :name get-by-hoks-id-and-yksiloiva-tunniste! :? :1
-- :doc Get palaute information for työpaikkajakso by HOKS ID and yksiloiva
--      tunniste.
SELECT * FROM palautteet
WHERE hoks_id = :hoks-id
  AND jakson_yksiloiva_tunniste = :yksiloiva-tunniste
  AND deleted_at is null
