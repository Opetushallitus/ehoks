(ns oph.ehoks.external.oppijanumerorekisteri
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.cache :as cache]
            [clojure.set :refer [rename-keys]]))

(defn find-student-by-nat-id [nat-id]
  (cache/with-cache!
    {:method :get
     :service (:oppijanumerorekisteri-url config)
     :path "henkilo"
     :options {:query-params {:hetu nat-id}
               :as :json}
     :authenticate? true}))

(defn find-student-by-oid [oid]
  (cache/with-cache!
    {:method :get
     :service (:oppijanumerorekisteri-url config)
     :path (str "henkilo/" oid)
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

(defn convert-student-info [values]
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
