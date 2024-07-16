-- :name insert! :? :1
-- :doc Insert palaute to DB
insert into palautteet (
  --~ (when (:arvo-tunniste params) "arvo_tunniste,")
  --~ (when (:hankintakoulutuksen-toteuttaja params) "hankintakoulutuksen_toteuttaja,")
  --~ (when (:yksiloiva-tunniste params) "jakson_yksiloiva_tunniste,")
  --~ (when (:kyselylinkki params) "kyselylinkki,")
  --~ (when (:nippu-id params) "nippu_id,")
  --~ (when (:suorituskieli params) "suorituskieli,")
  herate_source, heratepvm, hoks_id, koulutustoimija, kyselytyyppi, tila,
  toimipiste_oid, tutkintonimike, tutkintotunnus, voimassa_alkupvm,
  voimassa_loppupvm
) values (
  --~ (when (:arvo-tunniste params) ":arvo-tunniste,")
  --~ (when (:hankintakoulutuksen-toteuttaja params) ":hankintakoulutuksen-toteuttaja,")
  --~ (when (:yksiloiva-tunniste params) ":yksiloiva-tunniste,")
  --~ (when (:kyselylinkki params) ":kyselylinkki,")
  --~ (when (:nippu-id params) ":nippu-id,")
  --~ (when (:suorituskieli params) ":suorituskieli,")
  :herate-source, :heratepvm, :hoks-id, :koulutustoimija, :kyselytyyppi, :tila,
  :toimipiste-oid, :tutkintonimike, :tutkintotunnus, :voimassa-alkupvm,
  :voimassa-loppupvm
) returning *

-- :name get-tep-palautteet-needing-vastaajatunnus! :? :*
-- :doc Get all unprocessed palaute for Arvo call.
select * from tep_palaute
where tila = 'odottaa_kasittelya'
  and heratepvm <= current_date
  and arvo_tunniste is null
  and tep_kasitelty = false

-- :name update-arvo-tunniste! :? :*
-- :doc Update arvo-tunniste for palaute with given id.
update palautteet
set arvo_tunniste = :tunnus, updated_at = now(), tila = 'vastaajatunnus_muodostettu'
where id = :id
  and arvo_tunniste is null
  and tila = 'odottaa_kasittelya'
returning *

-- :name get-single-palaute-needing-vastaajatunnus! :? :1
-- :doc Get single palaute data for Arvo call.
select * from tep_palaute
where id = :id

-- :name update! :? :1
-- :doc Update palaute in DB
update	palautteet
set	herate_source = :herate-source,
	heratepvm = :heratepvm,
	kyselytyyppi = :kyselytyyppi,
	tila = :tila,
	hankintakoulutuksen_toteuttaja = :hankintakoulutuksen-toteuttaja,
	hoks_id = :hoks-id,
	koulutustoimija = :koulutustoimija,
	suorituskieli = :suorituskieli,
	toimipiste_oid = :toimipiste-oid,
	tutkintonimike = :tutkintonimike,
	tutkintotunnus = :tutkintotunnus,
	voimassa_alkupvm = :voimassa-alkupvm,
	voimassa_loppupvm = :voimassa-loppupvm
where	id = :id
returning id

-- :name get-by-hoks-id-and-kyselytyypit! :? :*
-- :doc Get opiskelijapalaute information by HOKS ID and kyselytyyppi
select * from palautteet
where hoks_id = :hoks-id and kyselytyyppi in (:v*:kyselytyypit)

-- :name get-by-kyselytyyppi-oppija-and-koulutustoimija! :? :*
-- :doc Get kyselyt by kyselytyyppi, oppija OID, and koulutustoimija.
select p.id, p.heratepvm, p.tila
from palautteet p
join hoksit h on (h.id = p.hoks_id)
where h.oppija_oid = :oppija-oid
  and p.kyselytyyppi in (:v*:kyselytyypit)
  and p.koulutustoimija = :koulutustoimija

-- :name get-by-hoks-id-and-yksiloiva-tunniste! :? :1
-- :doc Get palaute information for työpaikkajakso by HOKS ID and yksiloiva
--      tunniste.
select * from palautteet
where hoks_id = :hoks-id AND jakson_yksiloiva_tunniste = :yksiloiva-tunniste

-- :name get-for-heratepalvelu-by-hoks-id-and-kyselytyypit! :? :*
-- :doc get AMIS-palaute in the format for putting into herätepalvelu
select * from palaute_for_amis_heratepalvelu
where ehoks_id = :hoks-id
  and internal_kyselytyyppi in (:v*:kyselytyypit)
