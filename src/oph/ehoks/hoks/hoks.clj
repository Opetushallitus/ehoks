(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]))

(defn set-olemassa-olevat-ammatilliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-ammatilliset-tutkinnon-osat
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn set-puuttuvat-paikalliset-tutkinnon-osat [h]
  (assoc
    h
    :puuttuvat-paikalliset-tutkinnon-osat
    (db/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn set-olemassa-olevat-paikalliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-paikalliset-tutkinnon-osat
    (db/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn get-hokses-by-oppija [oid]
  (map
    #(-> %
         set-olemassa-olevat-ammatilliset-tutkinnon-osat
         set-puuttuvat-paikalliset-tutkinnon-osat)
    (db/select-hoks-by-oppija-oid oid)))
