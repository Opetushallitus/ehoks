-- :name get-jakso-by-hoks-id-and-yksiloiva-tunniste! :? :1
-- :doc Get palaute information for työpaikkajakso by HOKS ID and yksiloiva
--      tunniste.
select * from palautteet
where hoks_id = :hoks-id AND jakson_yksiloiva_tunniste = :yksiloiva-tunniste

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
returning id
