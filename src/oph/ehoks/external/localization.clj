(ns oph.ehoks.external.localization
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn get-localization-results  [& {:keys [category] :or {category "ehoks"}}]
  (-> (client/get (format "%s/cxf/rest/v1/localisation" (:localization-url config))
                  {:query-params {"category" category}})
      :body
      (cheshire/parse-string true)))
