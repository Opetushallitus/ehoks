(ns oph.ehoks.opiskeluoikeus
  (:require [medley.core :refer [greatest-by]])
  (:import [java.time LocalDate]))

(def inactive-statuses
  #{"valmistunut" "eronnut" "katsotaaneronneeksi" "peruutettu"})

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
