(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]))

(defn set-olemassa-olevat-ammatilliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-ammatilliset-tutkinnon-osat
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn get-hokses-by-oppija [oid]
  (map
    #(-> %
         set-olemassa-olevat-ammatilliset-tutkinnon-osat)
    (db/select-hoks-by-oppija-oid oid)))
