(ns oph.ehoks.schema.oid
  "Skeemamääritykset erityyppisille OID:eille"
  (:require [clojure.string :as string]
            [schema.core :refer [constrained defschema]]))

;;;; Opetushallitukselle on myönnetty koulutustoimialan OID-solmuluokka:
;;;; 1.2.246.562.NN.XXXXXXXXXXY, missä NN vaihtelee käyttötarkoituksen
;;;; mukaan (esim. 15 = opiskeluoikeus, 10 = organisaatio, jne.) ja
;;;; loppuosa XXXXXXXXXX on satunnaisgeneroitu luku välillä
;;;; 1 000 000 000 - 9 999 999 999 (OID:in loppuosa ei siis koskaan ala
;;;; nollalla) ja Y on XXXXXXXXXX osasta laskettu tarkistussumma.
;;;;
;;;; NOTE: Apparently there has been a bug in OID Generator earlier, so that it
;;;; has generated OIDs that have 12 digits in the last component instead of 11
;;;; and 10 instead of 0 as a last digit. These kinds of OIDs still exist today,
;;;; so we need to take this into account in validation.

(def organisaatio-oid-nodes #{"10" "99" "199" "299"})

(defn- luhn-checksum
  "Takes a sequence of `digits` and returns a checksum which is calculated using
  Luhn algorithm. The same algortihm used in OIDGenerator.java class in
  `Opetushallitus/java-utils` repository and is replicated here."
  [digits]
  (loop [remaining (reverse digits)
         alternate true
         sum       0]
    (if-let [digit (first remaining)]
      (recur (rest remaining)
             (not alternate)
             (+ sum
                (if alternate
                  (let [n (* digit 2)]
                    (if (> n 9)
                      (inc (mod n 10))
                      n))
                  digit)))
      (mod (- 10 (mod sum 10)) 10))))

(defn- ibm-1-3-7-checksum
  "Takes a sequence of `digits` and returns a checksum which is calculated using
  IBM 1-3-7 algorithm. The same algortihm is used in OIDGenerator.java class in
  `Opetushallitus/java-utils` repository and is replicated here."
  [digits]
  (loop [remaining   (reverse digits)
         multipliers (cycle [7 3 1])
         sum         0]
    (if-let [digit (first remaining)]
      (recur (rest remaining)
             (rest multipliers)
             (+ sum (* digit (first multipliers))))
      (mod (- 10 (mod sum 10)) 10))))

(defn valid-oid?
  "Takes an `oid` and returns `true` if it's valid. For the last component of
  the OID, a checksum is calculated and checked that it matches the last digit.
  IBM 1-3-7 checksum algorithm is used for henkilo/oppija (nodes 24, 98, 198,
  and 298) OIDs and Luhn algorithm for everyting else.

  NOTE: This function expects that validity checks against an OID regexp pattern
  have been made for `oid` before calling this function!"
  [oid]
  (let [splitted-oid                  (string/split oid #"\.")
        node                          (nth splitted-oid 4)
        [first-10-digits last-digits] (->> (nth splitted-oid 5)
                                           (map #(Character/getNumericValue
                                                   ^char %))
                                           (split-at 10))
        ; Apparently there has been a bug in OID Generator earlier, so that it
        ; has generated OIDs that have 12 digits in the last component instead
        ; of 11. In OIDs where the checksum should be 0 (being the last digit),
        ; there is 10 instead. These kinds of OIDs still exist today, so we
        ; need to take this into account in validation:
        expected-last-digit           (if (= last-digits [1 0])
                                        0
                                        (first last-digits))]
    (= expected-last-digit
       (if (= node "24")
         (ibm-1-3-7-checksum first-10-digits) ; Used for oppija OIDs in prod.
         (luhn-checksum first-10-digits)))))

(defschema OpiskeluoikeusOID
           (constrained #"^1\.2\.246\.562\.15\.[1-9]\d{9}(\d|10)$" valid-oid?))

(defschema OppijaOID
           (constrained #"^1\.2\.246\.562\.(24|[12]?98)\.[1-9]\d{9}(\d|10)$"
                        valid-oid?))

; There are a few organisaatio OIDs where the check digit doesn't match with the
; calculated Luhn checksum. This is why we don't do check digit check in case of
; organisaatio OIDs.
;
; Some remarks:
; - These has been a bug in org oid generation few years ago. That's why some
;   OIDs have 12 digits in the last part instead of 11. The last two digits are
;   "10" in those cases.
; - Very old OIDs have some sort of timestamp in the last part, e.g.,
;   "1.2.246.562.10.2013102416241359289612". The length of the last part is
;   22 digits in those cases.
(def OrganisaatioOID
  #"^1\.2\.246\.562\.(10|[12]?99)\.([1-9]\d{9}(\d|10)|0{10}1|201[34]\d{18})$")
