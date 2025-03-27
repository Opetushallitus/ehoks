(ns oph.ehoks.db.sql
  (:require [clojure.string :as str]
            [hugsql.parameters :refer [identifier-param-quote]]
            [oph.ehoks.utils :as utils]))

(defn target-columns-for-insert
  "Takes `params` (a hashmap of parameter data where the keys match parameter
  placeholder names in SQL) and returns target column list (column names
  separated by comma) for INSERT command."
  [params]
  (str/join "," (map utils/to-underscore-str (keys params))))

(defn values-for-insert
  "Takes `params` (a hashmap of parameter data where the keys match parameter
  placeholder names in SQL) and returns values (separated by comma) for INSERT
  command."
  [params]
  (str/join "," (map #(str ":v:" (name %)) (keys params))))

(defn set-clause-for-update
  "Takes `params` (a hashmap of parameter data where the keys match parameter
  placeholder names in SQL) and a hashmap of HugSQL-specific `options`. Returns
  a SET clause (part of UPDATE command) as a string, e.g.,
  \"set key1 = 1,key2 = 'val2'\" when `params` is {:key1 1 :key2 \"val2\"})`."
  [params options]
  (str "set updated_at = now(), "
       (str/join "," (for [[field _] params]
                       (-> (utils/to-underscore-str field)
                           (identifier-param-quote options)
                           (str " = :v:" (name field)))))))
