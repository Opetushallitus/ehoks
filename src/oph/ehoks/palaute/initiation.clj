(ns oph.ehoks.palaute.initiation
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [oph.ehoks.db :as db]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.utils.date :as date]))

(defn initiate-all-palautteet!
  "Initialise all palautteet (opiskelija & tyoelama) that should be."
  [{:keys [hoks] :as ctx}]
  (try
    (op/initiate-if-needed! ctx :aloituskysely)
    (op/initiate-if-needed! ctx :paattokysely)
    (tep/initiate-all-uninitiated! ctx)
    (palaute/mark-hoks-palaute-initiated! db/spec {:hoks-id (:id hoks)})
    (catch clojure.lang.ExceptionInfo e
      (if (= ::organisaatio/organisation-not-found (:type (ex-data e)))
        (throw (ex-info (str "HOKS contains an unknown organisation"
                             (:organisation-oid (ex-data e)))
                        (assoc (ex-data e) :type ::disallowed-update)))
        (log/error e "exception in heräte initiation with" (ex-data e))))
    (catch Exception e
      (log/error e "exception in heräte initiation"))))

(defn initiate-all-palautteet-for-hoks-ids!
  "Call initiate-all-palautteet! for the hokses with hoks-id's"
  [hoks-ids]
  (doseq [hoks-id hoks-ids]
    (log/info "initiate-all-palautteet-for-hoks-ids!: HOKS id" hoks-id)
    (let [hoks (hoks/get-by-id hoks-id)
          opiskeluoikeus (koski/get-opiskeluoikeus! (:opiskeluoikeus-oid hoks))
          ctx {:hoks hoks :opiskeluoikeus opiskeluoikeus}]
      (initiate-all-palautteet! ctx))))

(defn reinit-palautteet-for-uninitiated-hokses!
  "Fetch <batchsize> HOKSes from DB that do not have corresponding palaute
  records, and initiate palautteet for them to make sure that their
  palautteet will be handled when they are due (their heratepvm)."
  [batchsize]
  (log/info "reinit-palautteet-for-uninitiated-hokses!: making batch of"
            batchsize "HOKSes")
  (->> {:batchsize batchsize}
       (palaute/get-hokses-with-unhandled-palautteet! db/spec)
       (map :id)
       (initiate-all-palautteet-for-hoks-ids!)))
