-- :name update! :? :1
-- :doc Update osaamisen hankkimistapa in DB
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
update osaamisen_hankkimistavat set
/*~
(string/join ","
  (for [[field _] params]
    (str (identifier-param-quote (.replace (name field) \- \_) options)
      " = :v:" (name field))))
~*/
where id = :id
returning *
