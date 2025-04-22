(ns oph.ehoks.opiskeluoikeus
  (:require [medley.core :refer [find-first greatest-by]]
            [oph.ehoks.config :refer [config]])
  (:import [java.time LocalDate]))

(def inactive-statuses
  #{"valmistunut" "eronnut" "katsotaaneronneeksi" "peruutettu"})
(def terminal-states
  #{"eronnut" "katsotaaneronneeksi" "mitatoity" "peruutettu"
    "valiaikaisestikeskeytynyt"})

(defn- latest-jakso
  [jaksot]
  (apply greatest-by #(LocalDate/parse (:alku %)) jaksot))

(defn tila
  [opiskeluoikeus]
  (-> (get-in opiskeluoikeus [:tila :opiskeluoikeusjaksot])
      latest-jakso
      (get-in [:tila :koodiarvo])))

(defn inactive?
  "Check whether opiskeluoikeus tila is inactive"
  [opiskeluoikeus]
  (contains? inactive-statuses (tila opiskeluoikeus)))

(defn active?
  "Checks if the given opiskeluoikeus is still valid, ie. not valmistunut,
  eronnut, katsotaaneronneeksi."
  [opiskeluoikeus]
  (not (inactive? opiskeluoikeus)))

(defn still-active?
  "Checks if the given opiskeluoikeus is still valid, ie. not valmistunut,
  eronnut, katsotaaneronneeksi.
  Alternatively checks from the list of all opiskeluoikeudet held by the oppija
  that the opiskeluoikeus associated with the hoks is still valid."
  [opiskeluoikeus]
  (if (:prevent-finished-opiskeluoikeus-updates? config)
    (active? opiskeluoikeus)
    true))

(defn string-leq?
  "Is s1 lexicographically less than or equal to s2?"
  [s1 s2]
  (<= (compare s1 s2) 0))

(defn get-opiskeluoikeusjakso-for-date
  "Hakee opiskeluoikeudesta jakson, joka on voimassa tiettynä päivänä."
  [opiskeluoikeus ^String pvm]
  (->> (get-in opiskeluoikeus [:tila :opiskeluoikeusjaksot])
       (sort-by :alku)
       (partition 2 1 [:viimeinen])
       (find-first (fn [[this-jakso next-jakso]]
                     (and (string-leq? (:alku this-jakso) pvm)
                          (or (= :viimeinen next-jakso)
                              (not (string-leq? (:alku next-jakso) pvm))))))
       (first)))

(defn in-terminal-state-on-date?
  "Is opiskeluoikeus in terminal state on the given day?"
  [opiskeluoikeus pvm]
  (-> (get-opiskeluoikeusjakso-for-date opiskeluoikeus (str pvm))
      (get-in [:tila :koodiarvo])
      (terminal-states)
      (some?)))

(defn prev-date
  "Returns the previous date (as string) for a date (a string)"
  [date]
  (str (.minusDays (LocalDate/parse (str date)) 1)))

(defn in-terminal-state?
  "Returns `true` if opiskeluoikeus is in terminal state.  As per EH-1228,
  a terminal state that has begun on the same day as the herate is on
  is not deemed a terminal state."
  [opiskeluoikeus pvm]
  (and (in-terminal-state-on-date? opiskeluoikeus pvm)
       (in-terminal-state-on-date? opiskeluoikeus (prev-date pvm))))

(defn linked-to-another?
  "Returns `true` if `opiskeluoikeus` is linked to another opiskeluoikeus."
  [opiskeluoikeus]
  (some? (:sisältyyOpiskeluoikeuteen opiskeluoikeus)))

(defn tuva?
  "Onko opiskeluoikeus TUVA?"
  [opiskeluoikeus]
  (= "tuva" (get-in opiskeluoikeus [:tyyppi :koodiarvo])))
