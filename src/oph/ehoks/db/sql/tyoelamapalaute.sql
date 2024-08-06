-- :name get-by-hoks-id-and-yksiloiva-tunniste! :? :1
-- :doc Get palaute information for työpaikkajakso by HOKS ID and yksiloiva
--      tunniste.
select * from palautteet
where hoks_id = :hoks-id
  and jakson_yksiloiva_tunniste = :yksiloiva-tunniste
  and deleted_at is null

-- :name insert! :? :1
-- :doc Insert tyopaikkajakso palaute information to DB
insert into palautteet (herate_source,
                        heratepvm,
                        kyselytyyppi,
                        tila,
                        hoks_id,
                        jakson_yksiloiva_tunniste,
                        koulutustoimija,
                        toimipiste_oid,
                        tutkintonimike,
                        tutkintotunnus,
                        voimassa_alkupvm,
                        voimassa_loppupvm)
values (:herate-source,
        :heratepvm,
        'tyopaikkajakson_suorittaneet',
        'odottaa_kasittelya',
        :hoks-id,
        :yksiloiva-tunniste,
        :koulutustoimija,
        :toimipiste-oid,
        :tutkintonimike,
        :tutkintotunnus,
        :voimassa-alkupvm,
        :voimassa-loppupvm)
returning *

-- :name get-tep-palautteet-needing-vastaajatunnus! :? :*
-- :doc Get all unprocessed palaute for Arvo call.
select * from tep_palaute
where tila = 'odottaa_kasittelya'
  and heratepvm <= :heratepvm
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

-- :name get-for-heratepalvelu-by-hoks-id-and-yksiloiva-tunniste! :? :*
-- :doc get tep-jaksopalaute in the format for putting into herätepalvelu
select * from palaute_for_tep_heratepalvelu
where hoks_id = :hoks-id
  and jakson_yksiloiva_tunniste = :jakson-yksiloiva-tunniste
  and internal_kyselytyyppi = 'tyopaikkajakson_suorittaneet'

-- :name update-tep-kasitelty! :? :1
-- :doc Updates tep_kasitelty flag after getting vastaajatunnus from Arvo.
update osaamisen_hankkimistavat
set tep_kasitelty = :tep-kasitelty, updated_at = now()
where id = :id
returning *
