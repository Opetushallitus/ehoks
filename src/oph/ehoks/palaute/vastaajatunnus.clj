(ns oph.ehoks.palaute.vastaajatunnus
  (:require [oph.ehoks.palaute.opiskelija :as amis]
            [oph.ehoks.palaute.tyoelama :as tep]))

(defn handle-palautteet-waiting-for-heratepvm!
  "Fetch all unhandled palautteet whose heratepvm has come, check that
  they are part of kohderyhmä now (on their handling date) and create
  and save vastaajatunnus if so."
  [opts]
  (amis/create-and-save-arvo-kyselylinkki-for-all-needed! opts)
  (tep/handle-all-palautteet-waiting-for-vastaajatunnus! opts))

