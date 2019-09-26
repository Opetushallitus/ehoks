(ns oph.ehoks.external.eperusteet
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]))

(defn map-perusteet
  "Map perusteet values"
  [values]
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
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.find-perusteet")
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
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-perusteet" id)
       :options {:as :json}})))

(defn find-tutkinnon-osat [^String koodi-uri]
  (get-in
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.find-tutkinnonosat")
       :options {:as :json
                 :query-params {:koodiUri koodi-uri}}})
    [:body :data]))

(defn get-tutkinnon-osa-viitteet [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-tutkinnonosa-viitteet" id)
       :options {:as :json}})
    :body))

(defn find-tutkinto [^String diaarinumero]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.find-perusteet-by-diaari")
       :options {:as :json
                 :query-params {:diaarinumero diaarinumero}}})
    :body))

(defn get-suoritustavat [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-rakenne" id)
       :options {:as :json}})
    :body))

(defn get-ops-suoritustavat [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-ops-rakenne" id)
       :options {:as :json}})
    :body))