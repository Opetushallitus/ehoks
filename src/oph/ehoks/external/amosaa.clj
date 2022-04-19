(ns oph.ehoks.external.amosaa
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]))

(defn get-tutkinnon-osa-by-koodi
  "Hakee tutkinnon osan tiedot amosaasta koodin perusteella."
  [^String koodi]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "amosaa-service-url")
       :url (u/get-url "amosaa-service.get-tutkinnon-osa-by-koodi"
                       (format "paikallinen_tutkinnonosa_%s" koodi))
       :options {:as :json}})
    :body))
