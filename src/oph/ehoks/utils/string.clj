(ns oph.ehoks.utils.string
  (:require [clojure.string :as str])
  (:import (java.text Normalizer Normalizer$Form)))

(defn- deaccent
  "Poistaa diakriittiset merkit merkkijonosta ja palauttaa muokatun
  merkkijonon."
  [utf8-string]
  (str/replace (Normalizer/normalize utf8-string Normalizer$Form/NFD)
               #"\p{InCombiningDiacriticalMarks}+"
               ""))

(defn normalize
  "Convert non-alphanumeric characters to underscore characters  (`_`) and
  make letters lower case. If the resulting string has an underscore character
  as a prefix or postfix, those underscore characters are removed."
  [string]
  (-> (deaccent string)
      (str/replace #"\W+" "_")
      (str/replace #"(^_|_$)" "")
      (str/lower-case)))
