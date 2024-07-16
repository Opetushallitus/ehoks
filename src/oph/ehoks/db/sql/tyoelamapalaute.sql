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
