(ns oph.ehoks.external.eperusteet
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn map-perusteet [values]
  (map
    (fn [v]
      (-> (select-keys v [:id :nimi :osaamisalat :tutkintonimikkeet])
          (update :nimi select-keys [:fi :en :sv])
          (update :osaamisalat (fn [x] (map #(select-keys % [:nimi]) x)))
          (update :tutkintonimikkeet
                  (fn [x] (map #(select-keys % [:nimi]) x)))))
    values))

(defn search-perusteet-info [nimi]
  (-> (client/get (format "%s/perusteet/info" (:eperusteet-url config))
                  {:query-params {:nimi nimi
                                  :tutkintonimikkeet true
                                  :tutkinnonosat true
                                  :osaamisalat true}})
      :body
      (cheshire/parse-string true)
      :data))
