(ns oph.ehoks.palaute.lahetys
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [hugsql.core :as hugsql]
            [oph.ehoks.db :as db]
            [oph.ehoks.external.viestinvalityspalvelu :as vvp]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.handling :as handling]
            [oph.ehoks.palaute.tapahtuma :as pt]
            [oph.ehoks.palaute.viestit :as v]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as dateutil])
  (:import (java.time LocalDate)
           (clojure.lang ExceptionInfo)))

(declare insert!)
(declare get-by-tila-and-viestityypit!)
(hugsql/def-db-fns "oph/ehoks/db/sql/palauteviesti.sql")

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
  (when-let [loppupvm (:voimassa-loppupvm status)]
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
    [:odottaa-lahetysta nil :voimassa-alkupvm :viesti-lahetys]))

(defn record-palauteviesti!
  "Record in DB the palauteviesti that was (not) sent."
  [{:keys [existing-palaute hoks tx]} msg-state msg-id]
  (insert! tx {:palaute-id (:id existing-palaute)
               :vastaanottaja (:sahkoposti hoks)
               :viestityyppi "email"
               :tila (utils/to-underscore-str msg-state)
               :ulkoinen-tunniste msg-id}))

(defn palaute-check-send-save-and-sync!
  "Tekee kaikki palautekutsun lähetyksen vaiheet"
  [{:keys [existing-palaute hoks] :as ctx} _] ; no handlers used yet
  (let [[msg-state state field reason] (check-palaute-for-sending ctx)
        reporting-msg-state (or msg-state :ei-laheteta)
        additional-info {field (str (get existing-palaute field))
                         :msg-state reporting-msg-state}]
    (log/info "Message state for palaute" (:id existing-palaute)
              "of HOKS" (:id hoks) "is" reporting-msg-state
              "and state for palaute is" (or state "unchanged")
              "because of" reason "in" field)
    ;; do DB stuff first because it's easier to cancel (exceptions will
    ;; roll back the transaction)
    (palaute/update-tila!
      ctx (or state (:tila existing-palaute)) reason additional-info)
    (when msg-state
      (try
        (let [msg-id (when (= msg-state :odottaa-lahetysta)
                       (send-palaute-initial-email! ctx))]
          (record-palauteviesti! ctx msg-state msg-id)
          msg-id)
        (catch ExceptionInfo e
          ;; Most typical reason is that sending failed for some reason
          ;; (and no message has been queued).  This means that there is
          ;; no harm in retrying.  However, we handle the 400 case as
          ;; permanent failure because 400 results are pretty certain to
          ;; persist even if we retry.
          (if (= 400 (:status (ex-data e)))
            (do (record-palauteviesti! ctx :lahetys-epaonnistunut nil)
                (pt/build-and-insert!
                  ctx ::viestin-lahetys-epaonnistui
                  {:errormsg (ex-message e)
                   :body     (:body (ex-data e))})
                nil)  ; no msg-id created
            (throw (ex-info "Viestin lähetyksessä tapahtui virhe"
                            {:type ::viestin-lahetys-epaonnistui :ctx ctx}
                            e))))))))

(defn send-invitation!
  "Lähettää viestin yhdelle palautteelle, jos aiheellista."
  [palaute]
  (log/info "Processing email survey invitation for" (:kyselytyyppi palaute)
            "palaute" (:id palaute))
  (handling/call-with-context-and-error-handling
    :lahetys palaute-check-send-save-and-sync! palaute))

(defn handle-unsent-palaute!
  "Tekee kaiken mitä pitää tehdä palautteelle josta ei ole vielä lähetetty
  viestiä."
  [palaute]
  ;; TODO: also process viestinvalityspalvelu reports
  (send-invitation! palaute))

(defn handle-unsent-palautteet!
  "Hakee ja lähettää viestit (kyselykutsut) kaikille palautteille
  joilla on kyselylinkki mutta viestiä ei ole vielä lähetetty."
  [kyselytyypit]
  ;; tep-viestejä ei ole vielä toteutettu
  (assert (not (contains? (set kyselytyypit) "tyopaikkajakson_suorittaneet")))
  (log/info "Processing messages for kyselytyypit" kyselytyypit)
  (doall (map handle-unsent-palaute!
              (palaute/get-unsent-palautteet!
                db/spec {:kyselytyypit kyselytyypit
                         :viestityyppi "email"}))))
