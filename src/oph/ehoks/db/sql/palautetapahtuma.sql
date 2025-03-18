-- :name insert! :? :1
-- :doc Create new palautetapahtuma for given palaute
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

-- :name get-all-by-hoks-id-and-kyselytyypit! :? :*
-- :doc All palautetapahtumat for a given HOKS
select p.kyselytyyppi, p.heratepvm, pt.*
from palaute_tapahtumat pt
join palautteet p on (pt.palaute_id = p.id)
where p.hoks_id = :hoks-id
  and p.kyselytyyppi in (:v*:kyselytyypit)
  and p.deleted_at is null

