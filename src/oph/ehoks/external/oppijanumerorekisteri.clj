(ns oph.ehoks.external.oppijanumerorekisteri
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as connection]))

(defn find-student-by-nat-id [nat-id]
  (connection/api-get
    (:oppijanumerorekisteri-url config)
    "henkilo"
    {:query-params {:hetu nat-id}}))
