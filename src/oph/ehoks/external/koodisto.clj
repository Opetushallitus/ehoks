(ns oph.ehoks.external.koodisto
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.cache :as cache]))

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

(defn with-koodisto-get [path]
  (try
    (cache/with-cache!
      {:method :get
       :service (:koodisto-url config)
       :path path
       :options {:as :json}})
    (catch clojure.lang.ExceptionInfo e
      ; Koodisto returns Internal Server Error 500 with NotFoundException
      ; if element is not found.
      (throw
        (if (= (:body (ex-data e)) "error.codeelement.not.found")
          (ex-info "Code Element not found"
                   {:type :not-found
                    :uri uri
                    :version versio} e)
          e)))))

(defn convert-metadata [m]
  {:nimi (:nimi m)
   :lyhyt-nimi (:lyhytNimi m)
   :kuvaus (:kuvaus m)
   :kieli (:kieli m)})

(defn enrich [m ks]
  (if-let [k (get-in m ks)]
    (let [koodisto-value
          (filter-koodisto-values
            (:body (get-koodi-versio (:koodi-uri k) (:versio k))))]
      (assoc-in m (conj ks :metadata)
                (map convert-metadata (:metadata koodisto-value))))
    m))
