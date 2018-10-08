(ns oph.ehoks.external.koodisto
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]))

(defn filter-koodisto-values [values]
  (let [filtered
        (select-keys
          values
          [:tila :koodiArvo :voimassaLoppuPvm :voimassaAlkuPvm :resourceUri
           :koodisto :versio :koodiUri :paivitysPvm :version :metadata])]
    (if (some? (:metadata filtered))
      (update filtered
              :metadata
              (fn [x]
                (map #(select-keys
                        %
                        [:nimi :kuvaus :lyhytNimi :kayttoohje :kasite
                         :sisaltaaMerkityksen :eiSisallaMerkitysta
                         :huomioitavaKoodi :sisaltaaKoodiston :kieli])
                     x)))
      filtered)))

(defn get-koodi-versio [uri versio]
  (c/with-api-headers
    :get
    (format "%s/rest/codeelement/%s/%d"
            (:koodisto-url config) uri versio)
    {:as :json}))
