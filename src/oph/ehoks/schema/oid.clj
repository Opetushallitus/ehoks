(ns oph.ehoks.schema.oid
  "Skeemamääritykset erityyppisille OID:eille"
  (:require [clojure.string :as string]
            [schema.core :refer [cond-pre constrained defschema]]))

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
  IBM 1-3-7 checksum algorithm is used for henkilo/oppija (node 24) OIDs and
  Luhn algorithm for everyting else.

  NOTE: This function expects that validity checks against an OID regexp pattern
  have been made for `oid` before calling this function!"
  [oid]
  (let [splitted-oid                 (string/split oid #"\.")
        node                         (nth splitted-oid 4)
        [first-10-digits last-digit] (split-at 10 (nth splitted-oid 5))
        first-10-digits              (map #(Character/getNumericValue ^char %)
                                          first-10-digits)
        ; Apparently there has been a bug in OID Generator earlier, so that it
        ; has generated OIDs that have 12 digits in the last component instead
        ; of 11 and 10 instead of 0 as a last digit. These kinds of OIDs still
        ; exist today, so we need to take this into account in validation:
        last-digit                   (if (> (count last-digit) 1)
                                       0
                                       (Character/getNumericValue
                                         ^char (first last-digit)))]
    (= last-digit
       (if (= node "24")
         (ibm-1-3-7-checksum first-10-digits) ; Only used for oppija OIDs.
         (luhn-checksum first-10-digits)))))

(defschema OpiskeluoikeusOID
           (constrained #"^1\.2\.246\.562\.15\.[1-9]\d{9}(\d|10)$" valid-oid?))

(defschema OppijaOID
           (constrained #"^1\.2\.246\.562\.24\.[1-9]\d{9}(\d|10)$" valid-oid?))

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
  #"^1\.2\.246\.562\.10\.([1-9]\d{9}(\d|10)|0{10}1|201[34]\d{18})$")
