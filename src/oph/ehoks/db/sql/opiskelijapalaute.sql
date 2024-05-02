-- :name insert! :? :1
-- :doc Insert opiskelijapalautekysely information to DB
insert into palautteet (herate_source,
                        heratepvm,
                        kyselytyyppi,
                        tila,
                        hoks_id,
                        koulutustoimija,
                        suorituskieli,
                        voimassa_alkupvm,
                        voimassa_loppupvm)
values (:herate-source,
        :heratepvm,
        :kyselytyyppi,
        'odottaa_kasittelya',
        :hoks-id,
        :koulutustoimija,
        :suorituskieli,
        :voimassa-alkupvm,
        :voimassa-loppupvm)
returning id

-- :name get-by-hoks-id-and-kyselytyypit! :? :*
-- :doc Get opiskelijapalaute information by HOKS ID and kyselytyyppi
select * from palautteet
where hoks_id = :hoks-id AND kyselytyyppi in (:v*:kyselytyypit)
