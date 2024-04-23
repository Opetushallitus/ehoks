(ns oph.ehoks.utils.date
  "Date related utility functions. Some of the functions are simple wrappers of
  `java.time/LocalDate` that can be mocked in the tests."
  (:import [java.time LocalDate]))

(defn now ^LocalDate [] (LocalDate/now))
