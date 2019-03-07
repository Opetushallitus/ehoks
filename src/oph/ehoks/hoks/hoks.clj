(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db :as db]))

(defn get-olemassa-olevat-ammatilliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-ammatilliset-tutkinnon-osat
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn get-hokses-by-oppija [oid]
  (map
    #(-> %
         (get-olemassa-olevat-ammatilliset-tutkinnon-osat))))
