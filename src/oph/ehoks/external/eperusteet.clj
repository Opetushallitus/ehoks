(ns oph.ehoks.external.eperusteet
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [com.rpl.specter :as spc :refer [ALL NONE FIRST]]))

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

(def asteikkomuunnos
  "Scale tranformations"
  {:1 {:1 ""}
   :2 {:2 "1" :3 "3" :4 "5"}
   :3 {:5 "1" :7 "3" :9 "5"}})

(defn- adjust-osaamistaso
  "Convert osaamistaso according to values in asteikkomuunnos"
  [asteikko osaamistaso]
  (get-in asteikkomuunnos [(keyword asteikko) (keyword osaamistaso)]))

(defn- remove-empty-kriteerit
  "Remove empty kriteerit from several subfields"
  [values]
  (spc/setval [ALL :arviointi :arvioinninKohdealueet ALL
               :arvioinninKohteet ALL :osaamistasonKriteerit ALL
               #(empty? (:kriteerit %))]
              NONE values))

(defn- adjust-osaamistaso-based-on-asteikko
  "Adjusts the osaamistaso in each appropriate spot in object"
  [asteikko values]
  (spc/transform [ALL :arviointi :arvioinninKohdealueet ALL
                  :arvioinninKohteet ALL :osaamistasonKriteerit
                  ALL :_osaamistaso]
                 #(adjust-osaamistaso asteikko %) values))

(defn adjust-tutkinnonosa-arviointi
  "Adjusts osaamistasonKriteerit based on the osaamistaso of the tutkinnonosa"
  [values]
  ; Every tutkinnonosa should currently have the same arviointiAsteikko
  ; for all of its arvioinninKohteet
  (let [asteikko (spc/select-first
                   [ALL :arviointi :arvioinninKohdealueet ALL
                    :arvioinninKohteet FIRST :_arviointiAsteikko] values)]
    (->> values
         (remove-empty-kriteerit)
         (adjust-osaamistaso-based-on-asteikko asteikko))))

(defn- get-peruste-by-id
  "Get perusteet by ID. Uses eperusteet external api."
  [^Long id]
  (let [result (c/with-api-headers
                 {:method :get
                  :service (u/get-url "eperusteet-service-url")
                  :url (u/get-url "eperusteet-service.external-api.find-peruste"
                                  id)
                  :options {:as :json}})
        body (:body result)]
    body))

(defn get-koulutuksenOsa-by-koodiUri
  "Search for perusteet that match a koodiUri. Uses eperusteet external api."
  [^String koodiUri]
  (let [result
        (c/with-api-headers
          {:method :get
           :service (u/get-url "eperusteet-service-url")
           :url (u/get-url
                  "eperusteet-service.external-api.find-perusteet-by-koodi")
           :options {:as :json
                     :query-params {:koodi koodiUri}}})
        body (get-in result [:body :data])]
    (when (empty? (seq body))
      (throw (ex-info (str "eperusteet not found with koodiUri " koodiUri)
                      {:status 404})))
    (let [id (:id (first body))
          peruste (get-peruste-by-id id)
          koulutuksenOsat (:koulutuksenOsat peruste)
          koulutuksenOsa
          (filter #(= koodiUri (get-in % [:nimiKoodi :uri])) koulutuksenOsat)
          ;; TODO: tarvittava id UI:n ePerusteet urlia varten
          ;; ATM lähetetään vain 12345
          koulutuksenOsaPeruste
          (map
            (fn [v]
              (-> (select-keys v [:id :nimi :osaamisalat])
                  (update :nimi select-keys [:fi :en :sv])
                  (update
                    :osaamisalat (fn [x] (map #(select-keys % [:nimi]) x)))
                  (assoc :koulutuksenOsaId "12345")))
            koulutuksenOsa)]
      koulutuksenOsaPeruste)))

(defn search-perusteet-info
  "Search for perusteet that match a particular name"
  [nimi]
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

(defn get-perusteet
  "Get perusteet by ID"
  [^Long id]
  (:body
    (c/with-api-headers
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-perusteet" id)
       :options {:as :json}})))

(defn find-tutkinnon-osat
  "Find tutkinnon osat by koodi URL"
  [^String koodi-uri]
  (get-in
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.find-tutkinnonosat")
       :options {:as :json
                 :query-params {:koodiUri koodi-uri}}})
    [:body :data]))

(defn get-tutkinnon-osa-viitteet
  "Get tutkinnon osa viitteet by ID"
  [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-tutkinnonosa-viitteet" id)
       :options {:as :json}})
    :body))

(defn get-tutkinnon-osan-osa-alueet
  "Get tutkinnon osa-alueet by ID"
  [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-tutkinnonosa-osa-alueet" id)
       :options {:as :json}})
    :body))

(defn find-tutkinto
  "Get perusteet by diaari number"
  [^String diaarinumero]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.find-perusteet-by-diaari")
       :options {:as :json
                 :query-params {:diaarinumero diaarinumero}}})
    :body))

(defn get-suoritustavat
  "Get suoritustavat by ID"
  [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-rakenne" id)
       :options {:as :json}})
    :body))

(defn get-ops-suoritustavat
  "Get OPS suoritustavat by ID"
  [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-ops-rakenne" id)
       :options {:as :json}})
    :body))
