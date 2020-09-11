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

(defn- has-value [contact]
  (some? (:value contact)))

(defn- remove-empty-contact-values [contact-item]
  (filter has-value contact-item))

(defn- convert-contacts [contact-groups]
  (map
    #(-> %
         (select-keys [:id :yhteystieto])
         (rename-keys {:yhteystieto :contact})
         (update :contact convert-contact-values)
         (update :contact remove-empty-contact-values))
    contact-groups))

(defn- has-contact-info [contact-group]
  (not-empty (:contact contact-group)))

(defn- remove-empty-contact-groups [contact-groups]
  (filter has-contact-info contact-groups))

(defn- update-contacts [student-info]
  (let [converted-student-info
        (update student-info :contact-values-group convert-contacts)]
    (update converted-student-info
            :contact-values-group remove-empty-contact-groups)))

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
      (update-contacts converted-values)
      converted-values)))
