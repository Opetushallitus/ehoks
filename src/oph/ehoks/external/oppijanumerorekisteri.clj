(ns oph.ehoks.external.oppijanumerorekisteri
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn find-student-by-nat-id [nat-id]
  (-> (client/get (format "%s/henkilo/"
                          (:oppijanumerorekisteri-url config))
                  {:query-params {"hetu" nat-id}
                   :headers {"External-Permission-Service" "KOSKI"}})
      :body
      (cheshire/parse-string true)
      :data))
