-- :name update! :? :1
-- :doc Update osaamisen hankkimistapa in DB
-- :require [oph.ehoks.db.sql :as sql]
update osaamisen_hankkimistavat
--~ (sql/set-clause-for-update params options)
where id = :id
returning *

-- :name get-keskeytymisajanjaksot! :? :*
-- :doc Get keskeytymisajanjaksot for osaamisen hankkimistapa
select * from keskeytymisajanjaksot
where osaamisen_hankkimistapa_id = :oht-id
