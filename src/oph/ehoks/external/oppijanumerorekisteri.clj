(ns oph.ehoks.external.oppijanumerorekisteri
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as connection]))

(defn find-student-by-nat-id [nat-id]
  (connection/api-get (format "%s/henkilo/"
                              (:oppijanumerorekisteri-url config))
                      {:query-params {"hetu" nat-id}}))
