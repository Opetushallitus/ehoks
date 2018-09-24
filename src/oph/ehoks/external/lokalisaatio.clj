(ns oph.ehoks.external.lokalisaatio
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn get-localization-results  [& {:keys [category] :or {category "ehoks"}}]
  (-> (client/get (:lokalisaatio-url config)
                  {:query-params {"category" category}
                   :cookie-policy :standard})
      :body (cheshire/parse-string true)))
