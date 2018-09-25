(ns oph.ehoks.external.lokalisointi
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn get-localization-results  [& {:keys [category] :or {category "ehoks"}}]
  (-> (client/get (:lokalisointi-url config)
                  {:query-params {"category" category}
                   :cookie-policy :standard})
      :body (cheshire/parse-string true)))
