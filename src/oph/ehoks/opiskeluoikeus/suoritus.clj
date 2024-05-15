(ns oph.ehoks.opiskeluoikeus.suoritus
  (:require [clojure.string :as string]))

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
  (string/lower-case (:koodiarvo (:suorituskieli suoritus))))
