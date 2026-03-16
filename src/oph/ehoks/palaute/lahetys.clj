(ns oph.ehoks.palaute.lahetys
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [oph.ehoks.db :as db]
            [oph.ehoks.external.viestinvalityspalvelu :as vvp]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.vastaajatunnus :as vt]
            [oph.ehoks.palaute.viestit :as v]
            [oph.ehoks.utils.date :as dateutil])
  (:import (java.time LocalDate)))

(defn send-palaute-initial-email!
  "Lähettää viestin, jossa kutsutaan vastaamaan Arvossa olevaan kyselyyn."
  [{:keys [existing-palaute hoks suoritus] :as ctx}]
  (let [recipient (:sahkoposti hoks)
        kyselytyyppi (:kyselytyyppi existing-palaute)
        kyselylinkki (:kyselylinkki existing-palaute)
        suorituskieli (suoritus/kieli suoritus)
        email-body (v/amispalaute-html {:suorituskieli suorituskieli
                                        :kyselytyyppi kyselytyyppi
                                        :kyselylinkki kyselylinkki})
        email-subject (v/palaute-message-subject ctx)]
    (vvp/send-message! recipient email-subject email-body)))

(defn vastausaika-loppunut?
  "Onko kyselylinkin arvo-statuksessa vastausajan loppupvm saavutettu?"
  [status]
  (when-let [loppupvm (:voimassa_loppupvm status)]
    (let [enddate (LocalDate/parse (first (str/split loppupvm #"T")))]
      (dateutil/is-after (dateutil/now) enddate))))

(defn vastattu?
  "Onko kyselylinkin arvo-statuksessa kysely merkitty vastatuksi?"
  [status]
  (:vastattu status))

;; this should be put into handlers if the logic is different for tep
(defn check-palaute-for-sending
  "Kertoo mihin tilaan palaute ja sen viesti menee tilanteen perusteella.
  Palauttaa seuraavat tiedot:
  viestin uusi tila (nil = ei tehdä viestiä),
  palautteen uusi tila (nil = ei muutu),
  kenttä jonka perusteella päätös tehty,
  päätöksen syy -tagi."
  [{:keys [hoks existing-palaute existing-ddb-herate arvo-status]}]
  (cond
    (and existing-ddb-herate (seq @existing-ddb-herate))
    [nil :heratepalvelussa :heratepvm :heratepalvelun-vastuulla]

    (empty? (:sahkoposti hoks))
    [:lahetys-epaonnistunut nil :sahkoposti :ei-ole]

    (empty? (:kyselylinkki existing-palaute))
    [nil :odottaa-kasittelya :kyselylinkki :ei-ole]

    (and arvo-status (empty? @arvo-status))
    [nil nil :kyselylinkki :ei-loydy]

    (and arvo-status (vastausaika-loppunut? @arvo-status))
    [nil :vastausaika-loppunut :voimassa-loppupvm :arvo-paivitys]

    (and arvo-status (vastattu? @arvo-status))
    [nil :vastattu :heratepvm :arvo-paivitys]

    :else
    [:odottaa-lahetysta nil :voimassa-alkupvm :kysely-muodostettu]))

(defn palaute-check-send-save-and-sync!
  "Tekee kaikki palautekutsun lähetyksen vaiheet"
  [{:keys [existing-palaute hoks tx] :as ctx} _] ; no handlers used yet
  (let [[msg-state state field reason] (check-palaute-for-sending ctx)]
    (log/info "Message state for palaute" (:id existing-palaute)
              "of HOKS" (:id hoks) "is" (or msg-state :ei-laheteta)
              "and state for palaute is" (or state "unchanged")
              "because of" reason "in" field)
    nil))  ; stub

(defn handle-unsent-palaute!
  "Lähettää viestin yhdelle palautteelle, jos aiheellista."
  [palaute]
  (log/info "Sending survey invitation for" (:kyselytyyppi palaute)
            "palaute" (:id palaute))
  (vt/call-with-context-and-error-handling
    :lahetys palaute-check-send-save-and-sync! palaute))

(defn handle-unsent-palautteet!
  "Hakee ja lähettää viestit (kyselykutsut) kaikille palautteille
  joilla on kyselylinkki mutta viestiä ei ole vielä lähetetty."
  [kyselytyypit]
  ;; tep-viestejä ei ole vielä toteutettu
  (assert (not (contains? (set kyselytyypit) "tyopaikkajakson_suorittaneet")))
  (log/info "Sending messages for kyselytyypit" kyselytyypit)
  (doall (map handle-unsent-palaute!
              (palaute/get-unsent-palautteet!
                db/spec {:kyselytyypit kyselytyypit
                         :viestityyppi "email"}))))
