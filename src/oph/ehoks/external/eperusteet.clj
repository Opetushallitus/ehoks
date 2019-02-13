(ns oph.ehoks.external.eperusteet
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]))

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
  (get-in
    (c/with-api-headers
      {:method :get
       :service (:eperusteet-url config)
       :path "perusteet"
       :options {:as :json
                 :query-params {:nimi nimi
                                :tutkintonimikkeet true
                                :tutkinnonosat true
                                :osaamisalat true}}})
    [:body :data]))

(defn get-perusteet [^Long id]
  (:body
    (c/with-api-headers
      {:method :get
       :service (:eperusteet-url config)
       :path (format "perusteet/%d" id)
       :options {:as :json}})))

(defn find-tutkinnon-osat [^String koodi-uri]
  (get-in
    (c/with-api-headers
      {:method :get
       :service (:eperusteet-url config)
       :path "tutkinnonosat"
       :options {:as :json
                 :query-params {:koodiUri koodi-uri}}})
    [:body :data]))
