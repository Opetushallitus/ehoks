-- :name insert-palaute! :? :1
-- :doc Insert opiskelijapalautekysely information to DB
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
        'odottaa_kasittelya',
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

-- :name get-by-hoks-id-and-kyselytyypit! :? :*
-- :doc Get opiskelijapalaute information by HOKS ID and kyselytyyppi
select * from palautteet
where hoks_id = :hoks-id and kyselytyyppi in (:v*:kyselytyypit)

-- :name get-by-kyselytyyppi-oppija-and-koulutustoimija! :? :*
-- :doc Get kyselyt by kyselytyyppi, oppija OID, and koulutustoimija.
select p.heratepvm from palautteet p
join hoksit h on (h.id = p.hoks_id)
where h.oppija_oid = :oppija-oid
  and p.kyselytyyppi in (:v*:kyselytyypit)
  and p.koulutustoimija = :koulutustoimija

-- :name get-for-heratepalvelu-by-hoks-id-and-kyselytyypit! :? :*
-- :doc get AMIS-palaute in the format for putting into herätepalvelu
select * from palaute_for_amis_heratepalvelu
where ehoks_id = :hoks-id
  and internal_kyselytyyppi in (:v*:kyselytyypit)
