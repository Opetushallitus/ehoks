(ns oph.ehoks.external.oppijanumerorekisteri
  (:require [oph.ehoks.external.cache :as cache]
            [clojure.set :refer [rename-keys]]
            [oph.ehoks.external.oph-url :as u]))

(defn find-student-by-nat-id
  "Find oppija with given HETU"
  [nat-id]
  (cache/with-cache!
    {:method :get
     :service (u/get-url "oppijanumerorekisteri-url")
     :url (u/get-url "oppijanumerorekisteri.search-henkilo")
     :options {:query-params {:hetu nat-id}
               :as :json}
     :authenticate? true}))

(defn find-student-by-oid [oid]
  (cache/with-cache!
    {:method :get
     :service (u/get-url "oppijanumerorekisteri-url")
     :url (u/get-url "oppijanumerorekisteri.get-henkilo-by-oid" oid)
     :options {:as :json}
     :authenticate? true}))

(defn- convert-contact-values [contact-item]
  (map
    (fn [c]
      (rename-keys
        c
        {:yhteystietoArvo :value
         :yhteystietoTyyppi :type}))
    contact-item))

(defn- convert-contacts [group]
  (map
    #(-> %
         (select-keys [:id :yhteystieto])
         (rename-keys {:yhteystieto :contact})
         (update :contact convert-contact-values))
    group))

(defn convert-student-info
  "Convert student info to snake case"
  [values]
  (let [converted-values
        (-> values
            (select-keys [:oidHenkilo :etunimet :sukunimi
                          :kutsumanimi :oid :yhteystiedotRyhma])
            (rename-keys {:oidHenkilo :oid
                          :etunimet :first-name
                          :sukunimi :surname
                          :kutsumanimi :common-name
                          :yhteystiedotRyhma :contact-values-group}))]
    (if (seq (:contact-values-group converted-values))
      (update converted-values :contact-values-group convert-contacts)
      converted-values)))
