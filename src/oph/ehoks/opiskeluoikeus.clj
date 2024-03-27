(ns oph.ehoks.opiskeluoikeus
  (:require [medley.core :refer [find-first greatest-by]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.koski :as koski])
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
