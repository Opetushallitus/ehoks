select
  p.hoks_id as "ehoks-id",
  p.heratepvm,
  p.kyselylinkki,
  p.koulutustoimija,
  p.suorituskieli,
  p.toimipiste_oid as "toimipiste-oid",
  p.tutkintotunnus,
  p.voimassa_loppupvm as "voimassa-loppupvm",
  case p.kyselytyyppi
    when 'aloittaneet' then 'aloittaneet'
    when 'valmistuneet' then 'tutkinnon_suorittaneet'
    when 'osia_suorittaneet' then 'tutkinnon_osia_suorittaneet'
  end kyselytyyppi,
  h.oppija_oid as "oppija-oid",
  h.opiskeluoikeus_oid as "opiskeluoikeus-oid",
  h.sahkoposti,
  h.puhelinnumero,
  o.oppilaitos_oid as oppilaitos,
  p.hankintakoulutuksen_toteuttaja as "hankintakoulutuksen-toteuttaja"
from palautteet p
join hoksit h
on (h.id = p.hoks_id and h.deleted_at is null and p.deleted_at is null)
join opiskeluoikeudet o
on (h.opiskeluoikeus_oid = o."oid")
where p.kyselytyyppi in ('aloittaneet', 'valmistuneet', 'osia_suorittaneet')
and p.heratepvm >= :alkupvm
and p.heratepvm <= :loppupvm
and p.tila in (:tilat)
