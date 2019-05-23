(ns oph.ehoks.mock-gen
  (:require [clojure.string :as cs]))

(defn generate-hetu []
  (format "%02d%02d%02d-%04d"
          (inc (rand-int 31))
          (inc (rand-int 12))
          (inc (rand-int 99))
          (inc (rand-int 9999))))

(def name-start-parts
  ["sep" "tuo" "lau" "mat" "ta" "sa" "ve" "rai"
   "mi" "me" "ar" "hei" "vil" "sir" "mai"])

(def name-end-parts
  ["po" "mo" "ri" "ti" "tu" "mi" "sa" "mo" "ra" "to" "ni" "le" "pa" "ja"])

(defn generate-first-name []
  (cs/capitalize
    (str
      (get name-start-parts (rand-int (count name-start-parts)))
      (get name-end-parts (rand-int (count name-end-parts))))))

(def last-name-parts
  ["virta" "mäki" "sauna" "kangas" "lampi" "kuusi" "järvi" "mänty" "ranta"
   "metsä" "lumi" "koivu" "lahti" "vuono" "vuori" "haapa" "pelto" "vuono"])

(defn generate-last-name []
  (cs/capitalize
    (str
      (get last-name-parts (rand-int (count last-name-parts)))
      (get last-name-parts (rand-int (count last-name-parts))))))