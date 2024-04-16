-- :name insert! :? :1
-- :doc Insert opiskelijapalautekysely information to DB
insert into palautteet (herate_source, heratepvm, kyselytyyppi, tila, hoks_id)
values (:herate-source,
        :heratepvm,
        :kyselytyyppi,
        'odottaa_kasittelya',
        :hoks-id)
returning id

-- :name get-by-hoks-id-and-kyselytyypit! :? :*
-- :doc Get opiskelijapalaute information by HOKS ID and kyselytyyppi
select * from palautteet
where hoks_id = :hoks-id AND kyselytyyppi in (:v*:kyselytyypit)
