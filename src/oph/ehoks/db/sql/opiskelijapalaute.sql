-- :name insert-palaute! :? :1
-- :doc Insert opiskelijapalautekysely to DB
insert into palautteet (herate_source,
                        heratepvm,
                        kyselytyyppi,
                        tila,
                        hankintakoulutuksen_toteuttaja,
                        hoks_id,
                        koulutustoimija,
                        suorituskieli,
                        toimipiste_oid,
                        tutkintonimike,
                        tutkintotunnus,
                        voimassa_alkupvm,
                        voimassa_loppupvm)
values (:herate-source,
        :heratepvm,
        :kyselytyyppi,
        :tila,
        :hankintakoulutuksen-toteuttaja,
        :hoks-id,
        :koulutustoimija,
        :suorituskieli,
        :toimipiste-oid,
        :tutkintonimike,
        :tutkintotunnus,
        :voimassa-alkupvm,
        :voimassa-loppupvm)
returning id

-- :name update-palaute! :? :1
-- :doc Update opiskelijapalautekysely in DB
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

-- :name insert-palaute-tapahtuma! :? :1
-- :doc Create new tapahtuma for given palaute
insert into palaute_tapahtumat (
	palaute_id,
	vanha_tila,
	uusi_tila,
	tyyppi,
	syy,
	lisatiedot)
values (
	:palaute-id,
	:vanha-tila,
	:uusi-tila,
	:tapahtumatyyppi,
	:syy,
	:lisatiedot)
returning id

-- :name palaute-tapahtumat! :? :*
-- :doc All palautteet with their tapahtumat for a given HOKS
select p.kyselytyyppi, p.heratepvm, pt.*
from palaute_tapahtumat pt
join palautteet p on (pt.palaute_id = p.id)
where hoks_id = :hoks-id

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

-- :name get-for-heratepalvelu-by-hoks-id-and-kyselytyypit! :? :*
-- :doc get AMIS-palaute in the format for putting into herätepalvelu
select * from palaute_for_amis_heratepalvelu
where ehoks_id = :hoks-id
  and internal_kyselytyyppi in (:v*:kyselytyypit)
