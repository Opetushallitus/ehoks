(ns oph.ehoks.mocked-routes.mock-gen
  (:require [clojure.string :as cs]
            [cheshire.core :as cheshire]
            [clojure.java.io :as io]
            [ring.util.http-response :as response]))

(defn json-response [value]
  (assoc-in
    (response/ok
      (cheshire/generate-string
        value))
    [:headers "Content-Type"] "application/json"))

(defn json-response-file [f]
  (-> (io/resource f)
      slurp
      (cheshire/parse-string true)
      json-response))

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

(defn generate-oppilaitos-oid []
  (format "1.2.246.562.10.12%09d" (rand-int 999999999)))

(defn generate-henkilo-oid []
  (format "1.2.246.562.24.44%09d" (rand-int 999999999)))

(def tutkinto-parts
  ["sähkö" "tietotekniikka" "terveydenhoito" "kaupan" "ohjelmatuotanto"
   "rakennus" "liikenne" "valtiohallinto" "vartiointi" "palvelu" "siivous"])

(defn generate-tutkinto []
  (cs/capitalize
    (format "%salan perustutkinto"
            (get tutkinto-parts (rand-int (count tutkinto-parts))))))
