(ns oph.ehoks.palaute.lahetys
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [hugsql.core :as hugsql]
            [medley.core :refer [greatest find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.viestinvalityspalvelu :as vvp]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.handling :as handling]
            [oph.ehoks.palaute.opiskelija :as opiskelija]
            [oph.ehoks.palaute.tapahtuma :as pt]
            [oph.ehoks.palaute.viestit :as v]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as dateutil])
  (:import (java.time LocalDate)
           (clojure.lang ExceptionInfo)))

(declare insert!)
(declare get-by-tila-and-viestityypit!)
(declare get-by-palaute-and-viestityypit!)
(declare update-tila!)
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

    (not (dateutil/finnish-business-hours? (dateutil/now-with-time)))
    [nil nil :heratepvm :kasittely-odottaa-toimistoaikaa]

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
               :vastaanottaja (str (:sahkoposti hoks))
               :viestityyppi "email"
               :tila (utils/to-underscore-str msg-state)
               :ulkoinen-tunniste msg-id}))

(defn enrich-with-viestit!
  "Add the :palaute-email and :palaute-sms fields to ctx to allow syncing
  correct lahetyspvm, lahetystila, sms-lahetystila and viestintapalvelu-id
  for amis-heräte."
  [{:keys [existing-palaute tx] :as ctx}]
  (let [viestit (get-by-palaute-and-viestityypit!
                  tx {:palaute-id (:id existing-palaute)
                      :viestityypit ["email" "sms"]})
        email (find-first #(= "email" (:viestityyppi %)) viestit)
        sms (find-first #(= "sms" (:viestityyppi %)) viestit)]
    (assoc ctx :palaute-email email :palaute-sms sms)))

(defn sync-to-heratepalvelu!
  "Enrich context with enough information to sync palaute into
  herätepalvelu and then do the sync.  Currently only works for
  AMIS-kysely kyselytyypit."
  [{:keys [existing-palaute] :as ctx}]
  (-> existing-palaute
      (handling/build-ctx!)
      (merge ctx)  ; because original context has e.g. tx and existing-viesti
      (enrich-with-viestit!)
      (opiskelija/build-amisherate-record-for-heratepalvelu)
      (ddb/sync-amis-herate! :after-viestinvalityspalvelu-status)))

(defn palaute-check-send-and-save!
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
          ;; temporary hack to sync failed messages to Heratepalvelu
          (when (= msg-state :lahetys-epaonnistunut)
            (sync-to-heratepalvelu! ctx))
          msg-id)
        (catch ExceptionInfo e
          ;; Most typical reason is that sending failed for some reason
          ;; (and no message has been queued).  This means that there is
          ;; no harm in retrying.  However, we handle the 400 case as
          ;; permanent failure because 400 results are pretty certain to
          ;; persist even if we retry.
          (if (= 400 (:status (ex-data e)))
            (do (record-palauteviesti! ctx :lahetys-epaonnistunut nil)
                (pt/build-and-insert! ctx ::viestin-lahetys-epaonnistui
                                      {:errormsg (ex-message e)
                                       :body     (:body (ex-data e))})
                ;; temporary hack to sync failed messages to Heratepalvelu
                (sync-to-heratepalvelu! ctx)
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
    :lahetys palaute-check-send-and-save! palaute))

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

(defn record-sending-to-db-hp-and-arvo!
  "Päivittää Arvoon ja tietokantaan palautteen tilan sekä vastaamisajan
  alku- ja loppupäivän sillä hetkellä kun viestin saadaan tietää lähteneen"
  [{:keys [existing-palaute viesti-status tx] :as ctx}]
  (let [heratepvm (:heratepvm existing-palaute)
        new-alkupvm (greatest heratepvm (dateutil/now))
        new-loppupvm (palaute/vastaamisajan-loppupvm heratepvm new-alkupvm)
        voimassaolo {:voimassa-alkupvm (str new-alkupvm)
                     :voimassa-loppupvm (str new-loppupvm)}]
    (log/info "Palaute" (:id existing-palaute) "has now been sent"
              "so updating vastausaika to"
              (str new-alkupvm " -- " new-loppupvm))
    (palaute/update! tx (assoc voimassaolo :id (:id existing-palaute)))
    (palaute/update-tila! ctx :lahetetty :viesti-status
                          (assoc voimassaolo :viesti-status viesti-status))
    (arvo/update-kyselytunnus!
      (:arvo-tunniste existing-palaute) "lahetetty" new-alkupvm new-loppupvm)
    (sync-to-heratepalvelu! ctx)))

(def vvp-state->viesti-tila
  {["SKANNAUS"] :odottaa-lahetysta,
   ["ODOTTAA"] :odottaa-lahetysta,
   ["LAHETYKSESSA"] :odottaa-lahetysta,
   ["VIRHE"] :lahetys-epaonnistunut,
   ["LAHETETTY"] :lahetetty,
   ["DELIVERY"] :lahetetty,
   ["BOUNCE"] :lahetys-epaonnistunut,
   ["COMPLAINT"] :lahetetty,
   ["REJECT"] :lahetys-epaonnistunut,
   ["DELIVERYDELAY"] :odottaa-lahetysta})

(defn update-delivery-status!
  "Päivittää lähetysstatuksen yhdelle viestille ja päivittää palautteen
  tiedot vastaavasti."
  [{:keys [existing-viesti tx] :as ctx}]
  (log/info "Updating delivery status for message" (:viesti-id existing-viesti)
            "(external id" (:ulkoinen-tunniste existing-viesti) ")")
  (let [status (vvp/message-state! (:ulkoinen-tunniste existing-viesti))
        viesti-tila (vvp-state->viesti-tila status)]
    (log/info "Delivery status for message" (:viesti-id existing-viesti)
              "is" status "which means" viesti-tila)
    (if viesti-tila
      (update-tila! tx {:id (:viesti-id existing-viesti)
                        :tila (utils/to-underscore-str viesti-tila)})
      (throw (ex-info "Unknown delivery status"
                      {:type ::viestistatuksen-haku-epaonnistui :ctx ctx})))
    (if (= :lahetetty viesti-tila)
      (record-sending-to-db-hp-and-arvo! (assoc ctx :viesti-status status))
      (pt/build-and-insert! ctx :viesti-status {:viesti-status status}))
    ;; temporary fix until we also send SMS's: sync even failed
    ;; palautteet to herätepalvelu for trying to send SMS
    (when (= :lahetys-epaonnistunut viesti-tila)
      (sync-to-heratepalvelu! ctx))))

(defn handle-palaute-waiting-for-sending-status!
  "Tekee asiat, mitä tarvitsee tehdä yhdelle viestille jonka lähetysstatusta
  ei vielä tiedetä."
  [palaute-viesti]
  (jdbc/with-db-transaction
    [tx db/spec]
    (update-delivery-status! {:existing-palaute palaute-viesti
                              :existing-viesti palaute-viesti
                              :tapahtumatyyppi :lahetys
                              :tx tx})))

(defn handle-palautteet-waiting-for-sending-status!
  "Päivittää lähetysstatuksen viesteille, joille sitä ei ole vielä tiedetä."
  []
  (log/info "Checking delivery status for new messages")
  (doall (map handle-palaute-waiting-for-sending-status!
              (get-by-tila-and-viestityypit!
                db/spec {:tila "odottaa_lahetysta"
                         :viestityypit ["email" "email_muistutus_1"
                                        "email_muistutus_2"]}))))
