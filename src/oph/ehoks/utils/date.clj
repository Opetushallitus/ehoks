(ns oph.ehoks.utils.date
  "Date related utility functions. Some of the functions are simple wrappers of
  `java.time/LocalDate` that can be mocked in the tests."
  (:import [java.time
            LocalDate
            ZonedDateTime
            Instant
            ZoneId
            LocalTime
            DayOfWeek]))

;; these are mainly for overriding in tests
(defn now ^LocalDate [] (LocalDate/now))
(defn now-with-time ^Instant [] (Instant/now))

(defn time->instant
  "Converts a specific time of day into an instant on today"
  [hour minute sec]
  (-> (LocalTime/of hour minute sec)
      (.adjustInto (ZonedDateTime/now (ZoneId/of "Europe/Helsinki")))
      (Instant/from)))

(defn finnish-business-hours?
  "Onko ajanhetki ns. toimistoaikaan Suomessa? Ei ota poikkeuspäiviä huomioon."
  [^Instant inst]
  (let [fin-inst (.adjustInto
                   inst
                   (ZonedDateTime/now (ZoneId/of "Europe/Helsinki")))
        hour (.getHour fin-inst)
        dow (.getDayOfWeek fin-inst)]
    (and (not (#{DayOfWeek/SATURDAY DayOfWeek/SUNDAY} dow))
         (<= 7 hour 17))))

(defn is-after
  "Wrapper .isAfter-metodin ympäri, jolla on tyyppianotaatiot."
  [^LocalDate one-date ^LocalDate other-date]
  (.isAfter one-date other-date))

(defn is-same-or-before
  "Käännetty .isAfter"
  [^LocalDate one-date ^LocalDate other-date]
  (not (is-after one-date other-date)))
