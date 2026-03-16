-- :name insert! :? :1
-- :doc Insert palaute to DB
-- :require [oph.ehoks.db.sql :as sql]
insert into palautteet (
--~ (sql/target-columns-for-insert params)
) values (
--~ (sql/values-for-insert params)
) returning *

-- :name get-palaute-with-hankkimistapa-id-by-id! :? :*
select	p.*, ohm.hankkimistapa_id
from	palautteet p
left join oht_hoks_mapping ohm
on	(p.hoks_id = ohm.hoks_id
		and p.jakson_yksiloiva_tunniste = ohm.yksiloiva_tunniste)
where	(:hoks-id ::int is null or p.hoks_id = :hoks-id ::int)
and	(:palaute-id ::int is null or p.id = :palaute-id ::int)
and	p.deleted_at is null

-- :name get-palautteet-waiting-for-vastaajatunnus! :? :*
-- :doc List all unhandled palautteet whose herätepäivä has come
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
where h.oppija_oid = :oppija-oid
  and p.kyselytyyppi in (:v*:kyselytyypit)
  and (p.koulutustoimija = :koulutustoimija or (:koulutustoimija)::text is null)
  and p.deleted_at is null
  and h.deleted_at is null

-- :name get-hokses-with-unhandled-palautteet! :? :*
-- :doc List all HOKSes that do not have any palaute records.
select	id from hoksit
where deleted_at is null and palaute_handled_at is null
order by id desc  -- process newer HOKSes first
limit	:batchsize

-- :name mark-hoks-palaute-initiated! :? :1
-- :doc Mark all palautteet as initiated for given HOKS
update hoksit
set palaute_handled_at = now()
where id = :hoks-id
returning *

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
