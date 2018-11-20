(ns oph.ehoks.external.lokalisointi
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]))

(defn get-localization-results  [& {:keys [category] :or {category "ehoks"}}]
  (get-in
    (c/with-api-headers
      {:method :get
       :service (:lokalisointi-url config)
       :options {:query-params {:category category}
                 :cookie-policy :standard}})
    [:body :data]))
