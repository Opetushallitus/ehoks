(ns oph.ehoks.opiskeluoikeus.suoritus
  (:require [clojure.string :as string])
  (:import (java.time LocalDate)))

(defn tyyppi [suoritus] (get-in suoritus [:tyyppi :koodiarvo]))

(defn ammatillinen?
  "Varmistaa, että suorituksen tyyppi on joko ammatillinen tutkinto tai
  osittainen ammatillinen tutkinto."
  [suoritus]
  (some? (#{"ammatillinentutkinto" "ammatillinentutkintoosittainen"}
           (tyyppi suoritus))))

(defn telma?
  "Tarkistaa, onko suorituksen tyyppi TELMA (työhön ja elämään valmentava)."
  [suoritus]
  (some? (#{"telma", "telmakoulutuksenosa"} (tyyppi suoritus))))

(defn kieli
  [suoritus]
  (some-> suoritus :suorituskieli :koodiarvo (string/lower-case)))

(defn tutkintotunnus
  [suoritus]
  (get-in suoritus [:koulutusmoduuli :tunniste :koodiarvo]))

(defn tutkintonimike
  "Palauttaa suoritukseen liittyvien tutkintonimikkeiden koodiarvot merkkijonona
  muodossa (\"12345\",\"23456\")."
  [suoritus]
  (->> (:tutkintonimike suoritus)
       (map :koodiarvo)
       (string/join "\",\"")
       (format "(\"%s\")")))

(defn get-osaamisalat
  "Hakee voimassa olevat osaamisalat suorituksesta."
  [suoritus opiskeluoikeus-oid]
  (->> (:osaamisala suoritus)
       (filter #(and (or (nil? (:loppu %1))
                         (>= (compare (:loppu %1)
                                      (str (LocalDate/now)))
                             0))
                     (or (nil? (:alku %1))
                         (<= (compare (:alku %1)
                                      (str (LocalDate/now)))
                             0))))
       (map #(or (:koodiarvo (:osaamisala %1))
                 (:koodiarvo %1)))))
