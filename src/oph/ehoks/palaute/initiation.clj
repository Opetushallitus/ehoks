(ns oph.ehoks.palaute.initiation
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.tyoelama :as tep]))

(defn initiate-all-palautteet!
  "Initialise all palautteet (opiskelija & tyoelama) that should be."
  [ctx]
  (try
    (op/initiate-if-needed! ctx :aloituskysely)
    (op/initiate-if-needed! ctx :paattokysely)
    (tep/initiate-all-uninitiated! ctx)
    (catch clojure.lang.ExceptionInfo e
      (if (= ::organisaatio/organisation-not-found (:type (ex-data e)))
        (throw (ex-info (str "HOKS contains an unknown organisation"
                             (:organisation-oid (ex-data e)))
                        (assoc (ex-data e) :type ::disallowed-update)))
        (log/error e "exception in heräte initiation with" (ex-data e))))
    (catch Exception e
      (log/error e "exception in heräte initiation"))))

