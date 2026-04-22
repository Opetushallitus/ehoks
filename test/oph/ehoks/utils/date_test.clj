(ns oph.ehoks.utils.date-test
  (:require [clojure.test :refer [deftest testing are]]
            [oph.ehoks.utils.date :as date])
  (:import [java.time ZonedDateTime ZoneId Instant]))

(defn inst
  "Create an Instant at a given date/time in the Helsinki timezone."
  [year month day hour]
  (Instant/from (ZonedDateTime/of year month day hour 0 0 0
                                  (ZoneId/of "Europe/Helsinki"))))

(deftest test-finnish-business-hours?
  (testing "weekday within business hours is true"
    (are [year month day hour]
         (date/finnish-business-hours? (inst year month day hour))
      2026 4 13 7
      2026 4 13 12
      2026 4 13 17
      2026 4 14 9
      2026 4 15 9
      2026 4 16 9
      2026 4 17 9))

  (testing "weekday outside business hours is false"
    (are [year month day hour]
         (not (date/finnish-business-hours? (inst year month day hour)))
      2026 4 13 6
      2026 4 13 18
      2026 4 14 0))

  (testing "weekend is false regardless of hour"
    (are [year month day hour]
         (not (date/finnish-business-hours? (inst year month day hour)))
      2026 4 18 9
      2026 4 18 12
      2026 4 19 9
      2026 4 19 12)))
