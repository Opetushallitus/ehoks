(ns oph.ehoks.external.lokalisointi
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [cheshire.core :as cheshire]))

(defn get-localization-results  [& {:keys [category] :or {category "ehoks"}}]
  (get-in
    (c/with-api-headers
      :get
      (:lokalisointi-url config)
      {:query-params {"category" category}
       :cookie-policy :standard})
    [:body :data]))
