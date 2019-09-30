(ns oph.ehoks.db.db-operations.opiskeluoikeus
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.string :as cs]))

(defn get-like [v]
  (format "%%%s%%" (or v "")))

(def translated-oppija-columns
  {:tutkinto "tutkinto_nimi->>" :osaamisala "osaamisala_nimi->>"})

(def default-locale "fi")

(defn- get-locale [params]
  (str "'" (get params :locale default-locale) "'"))

(defn- get-translated-oppija-column [column params]
  (str (get translated-oppija-columns column) (get-locale params)))

(defn- get-oppija-order-by-column [params]
  (let [column (:order-by-column params)]
    (case column
      :nimi "nimi"
      :tutkinto (get-translated-oppija-column column params)
      :osaamisala (get-translated-oppija-column column params)
      "nimi")))

(defn- get-translated-column-filter [column params]
  (str
    "AND oo."
    (get-translated-oppija-column column params)
    " ILIKE '"
    (get-like (column params))
    "'"))

(defn set-oppijat-query [params]
  (-> queries/select-oppilaitos-oppijat
      (cs/replace ":order-by-column"
                  (get-oppija-order-by-column params))
      (cs/replace ":desc" (if (:desc params) "DESC" "ASC"))
      (cs/replace ":tutkinto-filter"
                  (if (:tutkinto params)
                    (get-translated-column-filter :tutkinto params)
                    ""))
      (cs/replace ":osaamisala-filter"
                  (if (:osaamisala params)
                    (get-translated-column-filter :osaamisala params)
                    ""))))

(defn set-oppijat-count-query [params]
  (-> queries/select-oppilaitos-oppijat-search-count
      (cs/replace ":tutkinto-filter"
                  (if (:tutkinto params)
                    (get-translated-column-filter :tutkinto params)
                    ""))
      (cs/replace ":osaamisala-filter"
                  (if (:osaamisala params)
                    (get-translated-column-filter :osaamisala params)
                    ""))))

(defn select-opiskeluoikeudet-without-tutkinto []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto]))

(defn select-opiskeluoikeudet-without-tutkinto-count []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto-count]))

(defn select-opiskeluoikeudet-by-oppija-oid [oppija-oid]
  (db-ops/query
    [queries/select-opiskeluoikeudet-by-oppija-oid oppija-oid]
    {:row-fn db-ops/from-sql}))

(defn select-opiskeluoikeus-by-oid [oid]
  (first
    (db-ops/query
      [queries/select-opiskeluoikeudet-by-oid oid]
      {:row-fn db-ops/from-sql})))

(defn insert-opiskeluoikeus! [opiskeluoikeus]
  (db-ops/insert-one! :opiskeluoikeudet (db-ops/to-sql opiskeluoikeus)))

(defn update-opiskeluoikeus! [oid opiskeluoikeus]
  (db-ops/update!
    :opiskeluoikeudet
    (db-ops/to-sql opiskeluoikeus)
    ["oid = ?" oid]))
