(ns oph.ehoks.opiskeluoikeus
  (:require [clojure.tools.logging :as log]
            [medley.core :refer [find-first greatest-by]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.koski :as koski])
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
  ([opiskeluoikeus-oid]
    (if (:prevent-finished-opiskeluoikeus-updates? config)
      (active? (koski/get-existing-opiskeluoikeus! opiskeluoikeus-oid))
      true))
  ([hoks opiskeluoikeudet]
    (if (:prevent-finished-opiskeluoikeus-updates? config)
      (let [opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)]
        (some->> opiskeluoikeudet
                 (find-first #(= (:oid %) opiskeluoikeus-oid))
                 active?))
      true)))

(defn get-opiskeluoikeusjakso-for-date
  "Hakee opiskeluoikeudesta jakson, joka on voimassa tiettynä päivänä."
  [opiskeluoikeus ^String vahvistus-pvm mode]
  (let [offset (if (= mode :one-day-offset) 1 0)
        jaksot (sort-by :alku (:opiskeluoikeusjaksot (:tila opiskeluoikeus)))]
    (reduce (fn [res next]
              (if (>= (compare vahvistus-pvm (:alku next)) offset)
                next
                (reduced res)))
            (first jaksot)
            jaksot)))

(defn in-terminal-state?
  "Returns `true` if opiskeluoikeus is in terminal state. Different terminal
  states are defined in `terminal-statuses`. Note that the function also returns
  `true` in case when opiskeluoikeus has transferred into terminal state during
  date `pvm`."
  [opiskeluoikeus pvm]
  (let [jakso (get-opiskeluoikeusjakso-for-date
                opiskeluoikeus (str pvm) :one-day-offset)
        tila  (get-in jakso [:tila :koodiarvo])]
    (some? (when (terminal-states tila)
             (log/warnf "Opiskeluoikeus `%s` on terminaalitilassa `%s`."
                        (:oid opiskeluoikeus)
                        tila)
             true))))

(defn linked-to-another?
  "Returns `true` if `opiskeluoikeus` is linked to another opiskeluoikeus."
  [opiskeluoikeus]
  (some? (:sisältyyOpiskeluoikeuteen opiskeluoikeus)))

(defn tuva?
  "Onko opiskeluoikeus TUVA?"
  [opiskeluoikeus]
  (= "tuva" (get-in opiskeluoikeus [:tyyppi :koodiarvo])))
