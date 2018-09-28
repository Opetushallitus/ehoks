(ns oph.ehoks.external.eperusteet
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn search-perusteet-info [nimi]
  (-> (client/get (format "%s/perusteet/info" (:eperusteet-url config))
                  {:query-params {:nimi nimi
                                  :tutkintonimikkeet true
                                  :tutkinnonosat true
                                  :osaamisalat true}})
      :body
      (cheshire/parse-string true)
      :data))
