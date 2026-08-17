(ns oph.ehoks.palaute.tyoelama.kesto
  (:require [clojure.tools.logging :as log]
            [medley.core :refer [map-vals]]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.utils.date :as dateutil])
  (:import (java.lang Math)
           (java.time LocalDate)))

(def round-vals (partial map-vals Math/round))

(defn ids
  "Returns an array-map of identifiers (HOKS ID & yksiloiva tunniste) that
  fully identify työpaikkajakso."
  [jakso]
  (select-keys jakso [:hoks_id :yksiloiva_tunniste]))

(defn not-in-keskeytymisajanjakso?
  "Varmistaa, että annettu päivämäärä ei kuulu keskeytymisajanjaksoon."
  [^LocalDate date keskeytymisajanjaksot]
  (every? #(or (and (:alku %) (.isBefore date (:alku %)))
               (and (:loppu %) (.isAfter date (:loppu %))))
          keskeytymisajanjaksot))

(defn osa-aikaisuuskerroin
  "Hakee osa-aikaisuustiedon jaksosta ja varmistaa että se on validi (ts.
  kokonaisluku väliltä 1 - 100). Palauttaa osa-aikaisuuskertoimen, joka
  lasketaan jakamalla prosentuaalinen osa-aikaisuus 100 %:lla,
  esim. 80 % / 100 % = 0.8. Jos osa-aikaisuustieto puuttuu tai on ei-validi,
  palautetaan osa-aikaisuuskertoimena nolla."
  [jakso]
  (let [hoks-id            (:hoks_id jakso)
        yksiloiva-tunniste (:yksiloiva_tunniste jakso)
        osa-aikaisuus      (:osa_aikaisuus jakso)]
    (or (cond
          (nil? osa-aikaisuus)
          (log/warnf
            (str "Osa-aikaisuustieto puuttuu jakson (HOKS `%d`, yksilöivä "
                 "tunniste `%s`) tiedoista. Jakson kestoksi asetetaan nolla.")
            hoks-id
            yksiloiva-tunniste)
          (not (and (integer? osa-aikaisuus)
                    (pos? osa-aikaisuus)
                    (>= 100 osa-aikaisuus)))
          (log/warnf
            (str "Jakson (HOKS `%d`, yksilöivä tunniste `%s`) osa-aikaisuus "
                 "`%s` ei ole validi. Jakson kestoksi asetetaan nolla.")
            hoks-id
            yksiloiva-tunniste
            osa-aikaisuus)
          :else (/ osa-aikaisuus 100.0))
        0)))

(defn add-loppu-to-tilajaksot
  "Lisää jokaiseen paitsi viimeiseen jaksoon kentän :loppu, joka on päivää
  ennen kuin seuraavan :alku"
  [tilajaksot]
  (conj (mapv (fn [current next]
                (let [^LocalDate next-starts (:alku next)]
                  (assoc current :loppu (.minusDays next-starts 1))))
              tilajaksot
              (rest tilajaksot))
        (last tilajaksot)))

(defn get-opiskeluoikeusjaksot
  [opiskeluoikeus]
  (->> (:opiskeluoikeusjaksot (:tila opiskeluoikeus))
       (map dateutil/alku-and-loppu-to-localdate)
       (sort-by :alku)
       add-loppu-to-tilajaksot))

(defn keskeytymisajanjaksot
  [jakso opiskeluoikeus]
  (let [keskeytynyt-tila? #{"valiaikaisestikeskeytynyt"}
        kjaksot-in-jakso (map dateutil/alku-and-loppu-to-localdate
                              (:keskeytymisajanjaksot jakso))
        kjaksot-in-opiskeluoikeus
        (filter #(keskeytynyt-tila? (:koodiarvo (:tila %)))
                (get-opiskeluoikeusjaksot opiskeluoikeus))]
    (concat kjaksot-in-jakso kjaksot-in-opiskeluoikeus)))

(defn in-jakso?
  "Tarkistaa sisältyykö päivämäärä `pvm` jaksoon `jakso`. Oletus on, että
  sekä pvm että jakson avaimet :alku ja :loppu ovat tyyppiä `LocalDate`.
  Palauttaa `true` jos päivämäärä sisältyy jaksoon, muuten `false`."
  [^LocalDate pvm jakso]
  (let [alku (:alku jakso) loppu (:loppu jakso)]
    (and (dateutil/is-same-or-before alku pvm)
         (or (nil? loppu) (dateutil/is-same-or-before pvm loppu)))))

(defn jakso-active?
  "Tarkistaa, onko `jakso` aktiivinen päivämääränä `pvm`. Ts. funktiossa
  tarkistetaan, kuuluuko `pvm` jaksoon ja sisältyykö se mihinkään jakson tai
  opiskeluoikeuden tiedoissa olevaan keskeytymisajanjaksoon."
  [jakso opiskeluoikeus pvm]
  (and (in-jakso? pvm jakso)
       (some? opiskeluoikeus)
       (not-any? #(in-jakso? pvm %)
                 (keskeytymisajanjaksot jakso opiskeluoikeus))))

(defn oppijan-jaksojen-yhden-paivan-kestot
  "Laskee yhden oppijan aktiivisena olevien jaksojen kestot yhden päivän osalta,
  eli suorittaa niin sanotun 'jyvityksen'. Tällä tarkoitetaan sitä, että yhden
  päivän kesto jaetaan tasaisesti kaikille samanaikaisesti aktiivisena oleville
  (ei keskeytyneille) jaksoille kyseisen päivän osalta.

  Funktio olettaa, että parametrina saadut `jaksot` ovat kaikki saman oppijan
  jaksoja. `opiskeluoikeudet` hashmap (oid -> opiskeluoikeus) voi sisältää
  muidenkin oppijoiden opiskeluoikeuksia, sillä kutakin jaksoa vastaava
  opiskeluoikeus haetaan `opiskeluoikeudet` hashmapista käyttäen jakson
  tiedoista löytyvää opiskeluoikeuden oid:ta.

  Funktio palauttaa päivänä `pvm` aktiivisena olleiden jaksojen jyvittämällä
  lasketut kestot hashmapissa, jossa avaimina jaksojen id:t
  (osaamisenhankkimistapojen) ja arvoina lasketut kestot reaalilukuina.

  Esimerkki:
  `jaksot` listassa on 4 jaksoa id:illä 1, 2, 3 ja 4. Näistä 1, 2 ja 4 ovat
  aktiivisia päivänä `pvm` ja 3 on keskeytynyt. Tällöin funktio palauttaa
  {1 0.333... 2 0.333... 4 0.333...}. Keskeytyneen jakson nollakesto ei ole
  siis mukana palautettavassa hashmapissa."
  [jaksot opiskeluoikeudet pvm]
  (let [active-jakso-ids ; Päivänä `pvm` aktiivisena olevien jaksojen id:t
        (map ids
             (filter #(jakso-active? %
                                     (get opiskeluoikeudet
                                          (:opiskeluoikeus_oid %))
                                     pvm)
                     jaksot))
        num-of-active-jaksos (count active-jakso-ids)
        kesto (/ 1.0 num-of-active-jaksos)] ; Jyvitys
    (zipmap active-jakso-ids (repeat kesto))))

(defn harmonize-date-keys
  "Harmonisoi jakson alku- ja loppupäivämääriä vastaavat avaimet, jotta
  kestonlaskennassa voidaan hyödyntää mahdollisimman paljon samoja funktiota."
  [jakso]
  (cond-> jakso
    (:jakso_alkupvm jakso)  (assoc :alku (:jakso_alkupvm jakso))
    (:jakso_loppupvm jakso) (assoc :loppu (:jakso_loppupvm jakso))))

(defn harmonize-alku-and-loppu-dates
  "Harmonisoi :jakso_alkupvm ja :jakso_loppupvm avaimet avaimiksi
  :alku ja :loppu myöhempää prosessointia varten. Muuttaa myös vastaavat
  päivämäärät myös LocalDate-objekteiksi."
  [jakso]
  (dateutil/alku-and-loppu-to-localdate (harmonize-date-keys jakso)))

(defn oppijan-jaksojen-kestot
  "Laskee kestot jaksoille `jaksot`. Funktio olettaa, että `jaksot` pitävät
  sisällään ainoastaan yhden oppijan jaksoja. `opiskeluoikeudet` tulee olla
  hashmap (oid -> opiskeluoikeus). Se voi sisältää muidenkin oppijoiden
  opiskeluoikeuksia, sillä jaksoja `jaksot` vastaavat opiskeluoikeudet haetaan
  `opiskeluoikeudet` hashmapista jakson tiedoista löytyvän opiskeluoikeus oid:n
  perusteella.

  Funktio palauttaa yhden oppijan jaksojen kestot aikavälillä, missä aikavälin
  alku on jaksojen `jaksot` alkupäivämääristä varhaisin ja loppu vastaavasi
  päättymispäivistä myöhäisin. Palautuva arvo on hashmap, jossa avaimina
  jaksojen id:t (osaamisenhankkimistapojen) ja arvoina kestot kokonaislukuina.
  Yksittäisen jakson kesto voi olla nolla, jos kyseiselle jaksolle ei saada
  opiskeluoikeutta Koskesta 404-virheen vuoksi, tai jos jakson
  osa-aikaisuustieto puuttuu tai on virheellinen. Jakson kesto voi myös
  pyöristyä nollaan, mikäli kokonaiskesto on jotain 0 ja 0.5 väliltä."
  [oppijan-jaksot opiskeluoikeudet]
  (when (not-empty oppijan-jaksot)
    (let [jaksot (map harmonize-alku-and-loppu-dates oppijan-jaksot)
          ids    (map ids jaksot)]
      (round-vals ; Pyöristetään kestot lähimpään kokonaislukuun.
        (merge-with
          * ; Kerrotaan kestot osa-aikaisuuskertoimilla
          (reduce (partial merge-with +) ; Summataan yksittäisten päivien kestot
                  ; Alustetaan alla kaikki kestot nollaksi:
                  (zipmap ids (repeat 0))
                  (map (partial oppijan-jaksojen-yhden-paivan-kestot
                                jaksot
                                opiskeluoikeudet)
                       (dateutil/date-range
                         (first (sort (map :alku jaksot)))
                         (last  (sort (map :loppu jaksot))))))
          (zipmap ids (map osa-aikaisuuskerroin jaksot)))))))

(defn get-and-memoize-opiskeluoikeudet!
  "Funktio hakee `jaksot` listan jaksojen opiskeluoikeuksia Koskesta.
  Opiskeluoikeudet tallennetaan muistiin hakujen välillä, joten jos listassa on
  jaksoja jotka jakavat saman opiskeluoikeuden, näiden opiskeluoikeudet
  tarvitsee hakea Koskesta vain kerran. Näin vältetään turhia GET-pyyntöjä
  Koskeen."
  [jaksot]
  (->> (map :opiskeluoikeus_oid jaksot)
       (set)
       (keep #(if-let [opiskeluoikeus (koski/get-opiskeluoikeus! %)]
                [% opiskeluoikeus]
                (let [affected-jaksot
                      (keep (fn [jakso] (and (= % (:opiskeluoikeus_oid jakso))
                                             [(:hoks_id jakso)
                                              (:yksiloiva_tunniste jakso)]))
                            jaksot)]
                  (log/warn "Opiskeluoikeutta `" % "` ei saatu Koskesta."
                            "Jaksoille" affected-jaksot
                            "kestoksi asetetaan nolla."))))
       (into {})))
