(ns oph.ehoks.utils.date
  "Date related utility functions. Some of the functions are simple wrappers of
  `java.time/LocalDate` that can be mocked in the tests."
  (:import [java.sql Timestamp]
           [java.time
            LocalDate
            ZonedDateTime
            Instant
            ZoneId
            LocalTime
            DayOfWeek]))

;; these are mainly for overriding in tests
(defn now ^LocalDate [] (LocalDate/now))
(defn now-with-time ^Instant [] (Instant/now))

(def ehoks-zone (ZoneId/of "Europe/Helsinki"))

(defn timestamp->localdate
  "Converts a java.sql.Timestamp to java.time.LocalDate"
  [^Timestamp ts]
  (some-> ts (.toInstant) (.atZone ehoks-zone) (.toLocalDate)))

(defn time->instant
  "Converts a specific time of day into an instant on today"
  [hour minute sec]
  (-> (LocalTime/of hour minute sec)
      (.adjustInto (ZonedDateTime/now ehoks-zone))
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

(defn ->localdate
  "Muuntaa merkkijonomuotoisen päivämäärän LocalDate:ksi. Palauttaa jo valmiin
  LocalDate-arvon sellaisenaan (esim. tietokannasta luetut päivämäärät ovat jo
  LocalDate-tyyppisiä)."
  ^LocalDate [d]
  (if (instance? LocalDate d) d (LocalDate/parse d)))

(defn alku-and-loppu-to-localdate
  "Muuntaa parametrina annetun hashmapin :alku ja :loppu -avaimien
  päivämäärät LocalDate:iksi. Sietää sekä merkkijono- että LocalDate-muotoiset
  päivämäärät."
  [jakso]
  (cond-> jakso
    (:alku jakso)  (update :alku  ->localdate)
    (:loppu jakso) (update :loppu ->localdate)))

(defn date-range
  "Rakentaa laiskan sekvenssin päivämääristä alkupäivämäärän `start` ja
  loppupäivämäärän `end` perusteella. `start` ja `end` kuuluvat mukaan
  sekvenssiin."
  [start end]
  (let [end+1 (.plusDays ^LocalDate end 1)]
    (take-while #(.isBefore ^LocalDate % end+1)
                (iterate #(.plusDays ^LocalDate % 1) start))))
