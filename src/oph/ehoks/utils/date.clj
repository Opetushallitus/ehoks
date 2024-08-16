(ns oph.ehoks.utils.date
  "Date related utility functions. Some of the functions are simple wrappers of
  `java.time/LocalDate` that can be mocked in the tests."
  (:import [java.time LocalDate]))

(defn now ^LocalDate [] (LocalDate/now))

(defn is-after
  "Wrapper .isAfter-metodin ympäri, jolla on tyyppianotaatiot."
  [^LocalDate one-date ^LocalDate other-date]
  (.isAfter one-date other-date))

(defn is-same-or-before
  "Käännetty .isAfter"
  [^LocalDate one-date ^LocalDate other-date]
  (not (is-after one-date other-date)))

(defn is-before
  "Wrapper .isBefore-metodin ympäri, jolla on tyyppianotaatiot."
  [^LocalDate one-date ^LocalDate other-date]
  (.isBefore one-date other-date))
