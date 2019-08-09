(ns oph.ehoks.external.koodisto
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]))

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

(defn with-koodisto-get [url]
  (try
    (:body
      (cache/with-cache!
        {:method :get
         :service (u/get-url "koodisto-service-url")
         :url url
         :options {:as :json}}))
    (catch clojure.lang.ExceptionInfo e
      ; Koodisto returns Internal Server Error 500 with NotFoundException
      ; if element is not found.
      (throw
        (if (= (:body (ex-data e)) "error.codeelement.not.found")
          (ex-info "Code Element not found"
                   {:type :not-found
                    :url url}
                   e)
          e)))))

(defn get-koodi [uri]
  (with-koodisto-get (u/get-url "koodisto-service.get-latest-by-uri" uri)))

(defn get-koodi-versiot [uri]
  (with-koodisto-get
    (u/get-url "koodisto-service.get-versiot-by-uri" uri)))

(defn get-koodi-versio [uri versio]
  (with-koodisto-get
    (u/get-url "koodisto-service.get-versio-by-uri" uri versio)))

(defn convert-metadata [m]
  {:nimi (:nimi m)
   :lyhyt-nimi (:lyhytNimi m)
   :kuvaus (:kuvaus m)
   :kieli (:kieli m)})
