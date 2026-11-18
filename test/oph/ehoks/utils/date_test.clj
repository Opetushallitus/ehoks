(ns oph.ehoks.utils.date-test
  (:require [clojure.test :refer [deftest testing are]]
            [oph.ehoks.utils.date :as date])
  (:import [java.time ZonedDateTime ZoneId Instant]))

(defn inst
  "Create an Instant at a given date/time in the Helsinki timezone."
  [year month day hour]
  (Instant/from (ZonedDateTime/of year month day hour 0 0 0
                                  (ZoneId/of "Europe/Helsinki"))))

(deftest test-palaute-lahetysaika?
  (testing "any day of week within sending hours is true"
    (are [year month day hour]
         (date/palaute-lahetysaika? (inst year month day hour))
      2026 4 13 8
      2026 4 13 12
      2026 4 13 20
      2026 4 14 9
      2026 4 15 9
      2026 4 16 9
      2026 4 17 9
      2026 4 18 8
      2026 4 18 20
      2026 4 19 12))

  (testing "outside sending hours is false regardless of day of week"
    (are [year month day hour]
         (not (date/palaute-lahetysaika? (inst year month day hour)))
      2026 4 13 7
      2026 4 13 21
      2026 4 14 0
      2026 4 18 7
      2026 4 19 21
      2026 4 19 23)))
