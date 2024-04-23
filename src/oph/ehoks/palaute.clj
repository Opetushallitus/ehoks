(ns oph.ehoks.palaute
  (:require [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(defn current-rahoituskausi-alkupvm
  ^LocalDate []
  (let [current-year            (.getYear (date/now))
        ^int rahoituskausi-year (if (< (.getMonthValue (date/now)) 7)
                                  (dec current-year)
                                  current-year)]
    (LocalDate/of rahoituskausi-year 7 1)))

(defn valid-herate-date?
  "onko herätteen päivämäärä aikaisintaan kuluvan rahoituskauden alkupvm
  (1.7.)?"
  [^LocalDate herate-date]
  (not (.isAfter (current-rahoituskausi-alkupvm) herate-date)))
