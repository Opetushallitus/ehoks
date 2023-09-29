(ns oph.ehoks.scripts
  (:require [clojure.string :as s]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.queries :as q]
            [oph.ehoks.hoks.schema :as sch]))

(defn- invalid-y-tunnus?
  [line]
  (let [y-tunnus (second (reverse line))
        valid? (and (< 7 (count y-tunnus))
                    (some? (re-matches #"^[0-9]{7}-[0-9]$" y-tunnus))
                    (sch/valid-y-tunnus? y-tunnus))]
    (not valid?)))

(defn- y-tunnus-korjaantuu-helposti
  [line]
  (let [y-tunnus (second (reverse line))
        trimmed (.replaceAll y-tunnus "[^0-9-]" "")
        contains-y-tunnus? (and (< 8 (count trimmed))
                                (some? (re-matches #".*[0-9]{7}-[0-9]{1}.*" trimmed)))]
    (if contains-y-tunnus?
      (let [matcher (re-matcher #"\d{7}-\d{1}" trimmed)
            found-y-tunnus (re-find matcher)
            valid? (sch/valid-y-tunnus? found-y-tunnus)]
        (concat line [valid? (if valid? found-y-tunnus "")]))
      (concat line [false ""]))))

; Syö csv:tä formaatissa:
;   "hoks_id"
;   "ensikertainen_hyvaksyminen"
;   "osaamisen_saavuttamisen_pvm"
;   "jakso_loppu"
;   "oppilaitos_oid"
;   "koulutustoimija_oid"
;   "tyyppi"
;   "hankkimistapa_id"
;   "yksiloiva_tunniste"
;   "y_tunnus"
;   "tyopaikan_nimi"
;
; Tuottaa vastaavan csv:n niistä riveistä, joiden y-tunnus ei ole validi,
; ja lisää kentät "korjaantuu helposti" ja "korjattu y-tunnus" seuraavia
; vaiheita varten.
(defn validate-csv
  [input-csv-file]
  (with-open [writer (io/writer "ei-validit-y-tunnukset.csv")]
    (with-open [reader (io/reader input-csv-file)]
      (let [headers ["hoks_id"
                     "ensikertainen_hyvaksyminen"
                     "osaamisen_saavuttamisen_pvm"
                     "jakso_loppu"
                     "oppilaitos_oid"
                     "koulutustoimija_oid"
                     "tyyppi"
                     "hankkimistapa_id"
                     "yksiloiva_tunniste"
                     "y_tunnus"
                     "tyopaikan_nimi"
                     "korjaantuu helposti"
                     "korjattu y-tunnus"]
            with-invalid-y-tunnus
            (filter invalid-y-tunnus? (rest (csv/read-csv reader)))]
        (csv/write-csv writer (cons headers (map y-tunnus-korjaantuu-helposti with-invalid-y-tunnus)))))))

(q/defq select-osaamisen-hankkimistavat-by-id)

(defn- update-y-tunnus
  [line]
  (let [hankkimistapa_id (last (take 8 line))
        yksiloiva_tunniste (last (take 9 line))
        vanha-y-tunnus (last (take 10 line))
        uusi-y-tunnus (last line)
        oh_id (Long/parseLong hankkimistapa_id)
        osaamisen_hankkimistapa (first (db-ops/query [select-osaamisen-hankkimistavat-by-id oh_id] {}))
        tjk_id (:tyopaikalla_jarjestettava_koulutus_id osaamisen_hankkimistapa)]
    (println (str "Päivitetään oh/tjk:n " oh_id "/" tjk_id " y-tunnus arvoon " uusi-y-tunnus))
    (db-ops/update!
      :tyopaikalla_jarjestettavat_koulutukset
      {:tyopaikan_y_tunnus uusi-y-tunnus}
      ["id = ? AND deleted_at IS NULL" tjk_id])
    [hankkimistapa_id tjk_id yksiloiva_tunniste vanha-y-tunnus uusi-y-tunnus]))

(defn- voi-korjata?
  [line]
  (= "true" (second (reverse line))))

; Syö edellisessä vaiheessa tuotettua csv:tä, ja päivittää y-tunnukset eHoksiin.
;
; Tuottaa uuden csv:n, lähinnä Herätepalvelun päivittämistä varten, muotoa:
;   "hankkimistapa_id"
;   "tyopaikalla_jarjestettava_koulutus_id"
;   "yksiloiva_tunniste"
;   "vanha y-tunnus"
;   "uusi y-tunnus"
(defn fix-y-tunnus
  []
  (with-open [writer (io/writer "korjatut-y-tunnukset.csv")]
    (with-open [reader (io/reader "ei-validit-y-tunnukset.csv")]
      (let [headers ["hankkimistapa_id"
                     "tyopaikalla_jarjestettava_koulutus_id"
                     "yksiloiva_tunniste"
                     "vanha y-tunnus"
                     "uusi y-tunnus"]
            updated (map update-y-tunnus (filter voi-korjata? (rest (csv/read-csv reader))))]
        (csv/write-csv writer (cons headers updated))))))
