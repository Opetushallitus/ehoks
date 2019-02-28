(ns oph.ehoks.hoks-json-converter
  (:require [clojure.data.json :as json]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [schema.core :as s]
            [clojure.set :refer [rename-keys]]
            [oph.ehoks.dev-server :refer [parse-date]]
            [oph.ehoks.hoks.handler :refer [write-hoks-json-file!]]))

(defn rename-all-in [c k km]
  (mapv #(update % k rename-keys km) c))

(defn rename-all [c km]
  (mapv #(rename-keys % km) c))

(defn parse-or-set-date [s]
  (if (empty? s)
    (java.util.Date.)
    (parse-date s)))

(defn set-koodi-uris [c k p]
  (mapv #(update % k (fn [s] (str p "_" s))) c))

(defn convert [h]
  (as-> h x
    (rename-keys x {:urasuunnitelma :urasuunnitelma-koodi-uri})
    (update x :puuttuva-ammatillinen-tutkinnon-osat
            rename-all-in
            :tutkinnon-osa {:koodi-uri :tutkinnon-osa-koodi-uri})
    (update x :puuttuva-yhteisen-tutkinnon-osat
            rename-all {:koodi-uri :tutkinnon-osa-koodi-uri})
    (update x :puuttuva-yhteisen-tutkinnon-osat
            set-koodi-uris :tutkinnon-osa-koodi-uri "tutkinnonosat")
    (update x :ensikertainen-hyvaksyminen parse-or-set-date)))

(defn validate [h]
  (s/validate hoks-schema/HOKSLuonti h))

(defn to-json-file [j target]
  (write-hoks-json-file! j target))

(defn convert-file [file target validate?]
  (let [j (json/read-str (slurp file) :key-fn keyword)
        h (convert j)]
    (when validate?
      (validate h))
    (to-json-file h target)))
