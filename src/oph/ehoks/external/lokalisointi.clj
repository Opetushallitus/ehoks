(ns oph.ehoks.external.lokalisointi
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.oph-url :as u]))

(defn get-localization-results  [& {:keys [category] :or {category "ehoks"}}]
  (get
    (c/with-api-headers
      {:method :get
       :service (u/get-url "lokalisointi-url")
       :url (u/get-url "lokalisointi-url")
       :options {:query-params {:category category}
                 :cookie-policy :standard
                 :as :json}})
    :body))
