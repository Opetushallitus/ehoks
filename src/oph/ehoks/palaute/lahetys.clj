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
            [oph.ehoks.external.elisa :as elisa]
            [oph.ehoks.external.organisaatio :as organisaatio]
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
    (log/info "Sending" kyselytyyppi "invitation message to" recipient)
    (vvp/send-message! recipient email-subject email-body)))

(defn vastausaika-loppunut?
  "Onko kyselylinkin arvo-statuksessa vastausajan loppupvm saavutettu?"
  [status]
  (when-let [loppupvm (:voimassa-loppupvm status)]
    (let [enddate (LocalDate/parse (first (str/split loppupvm #"T")))]
      (dateutil/is-after? (dateutil/now) enddate))))

(defn vastattu?
  "Onko kyselylinkin arvo-statuksessa kysely merkitty vastatuksi?"
  [status]
  (:vastattu status))

(declare palauteviesti-tila sms-kyselytyypit)

(def yhteystietokentta
  "Mistä HOKSin kentästä kunkin viestityypin vastaanottaja löytyy."
  {:email :sahkoposti
   :sms :puhelinnumero})

;; this should be put into handlers if the logic is different for tep
(defn check-palaute-for-sending
  "Kertoo mihin tilaan palaute ja sen viesti menee tilanteen perusteella.
  Palauttaa seuraavat tiedot:
  viestin uusi tila (nil = ei tehdä viestiä),
  palautteen uusi tila (nil = ei muutu),
  kenttä jonka perusteella päätös tehty,
  päätöksen syy -tagi."
  [{:keys [hoks existing-palaute existing-ddb-herate arvo-status] :as ctx}
   viestityyppi]
  (let [sms? (= :sms viestityyppi)
        kentta (yhteystietokentta viestityyppi)]
    (cond
      (and sms? (not (palaute/ehoks-sends-sms?)))
      [nil nil kentta :ei-ehoksin-vastuulla]

      (and sms? (not (contains? sms-kyselytyypit
                                (:kyselytyyppi existing-palaute))))
      [nil nil :kyselytyyppi :ei-tekstiviestia]

      ;; eHOKS writes the palaute to Herätepalvelu only after it has
      ;; handled some message for it, so a Herätepalvelu record means
      ;; Herätepalvelu's responsibility only if there are no messages yet
      (and existing-ddb-herate (seq @existing-ddb-herate)
           (not-any? #(palauteviesti-tila ctx %) [:email :sms]))
      [nil :heratepalvelussa :heratepvm :heratepalvelun-vastuulla]

      (some? (palauteviesti-tila ctx viestityyppi))
      [nil nil kentta :jo-kasitelty]

      (empty? (get hoks kentta))
      [:lahetys-epaonnistunut nil kentta :ei-ole]

      (and sms? (not (elisa/valid-finnish-number? (get hoks kentta))))
      [:lahetys-epaonnistunut nil kentta :virheellinen]

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
      [:odottaa-lahetysta nil :voimassa-alkupvm :viesti-lahetys])))

(defn record-palauteviesti!
  "Record in DB the palauteviesti that was (not) sent."
  [{:keys [existing-palaute tx]} viestityyppi vastaanottaja msg-state msg-id]
  (log/info "Recording" viestityyppi "message for palaute"
            (:id existing-palaute) "with state" msg-state
            "and identifier" msg-id)
  (insert! tx {:palaute-id (:id existing-palaute)
               :vastaanottaja (str vastaanottaja)
               :viestityyppi (name viestityyppi)
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
  (log/info "Syncing palaute" (:id existing-palaute) "to Herätepalvelu")
  (-> existing-palaute
      (handling/build-ctx!)
      (merge ctx)  ; because original context has e.g. tx and existing-viesti
      (enrich-with-viestit!)
      (opiskelija/build-amisherate-record-for-heratepalvelu)
      (ddb/sync-amis-herate! :after-viestinvalityspalvelu-status)))

(declare send-palaute-initial-sms! record-viesti-result!)

(defn send-palaute-initial-message!
  "Lähettää annetun viestityypin kutsuviestin.  Palauttaa viestin tilan
  (:tila) sekä mahdollisesti vastaanottajan (:vastaanottaja) ja ulkoisen
  tunnisteen (:ulkoinen-tunniste)."
  [ctx viestityyppi]
  (case viestityyppi
    :email {:tila :odottaa-lahetysta
            :ulkoinen-tunniste (send-palaute-initial-email! ctx)}
    :sms (send-palaute-initial-sms! ctx)))

(defn palaute-check-send-and-save!
  "Tekee kaikki palautekutsun lähetyksen vaiheet annetulle viestityypille
  (:email tai :sms).  Palauttaa viestin ulkoisen tunnisteen tai,
  jos sitä ei ole, viestin tilan."
  [{:keys [existing-palaute hoks] :as ctx} _ viestityyppi] ; no handlers yet
  (let [[msg-state state field reason]
        (check-palaute-for-sending ctx viestityyppi)
        reporting-msg-state (or msg-state :ei-laheteta)
        additional-info {field (str (get existing-palaute field))
                         :msg-state reporting-msg-state
                         :viestityyppi viestityyppi}
        vastaanottaja (get hoks (yhteystietokentta viestityyppi))]
    (log/info "State of" viestityyppi "message for palaute"
              (:id existing-palaute) "of HOKS" (:id hoks) "is"
              reporting-msg-state "and state for palaute is"
              (or state "unchanged") "because of" reason "in" field)
    ;; do DB stuff first because it's easier to cancel (exceptions will
    ;; roll back the transaction)
    (palaute/update-tila!
      ctx (or state (:tila existing-palaute)) reason additional-info)
    (when msg-state
      (try
        (let [{:keys [tila ulkoinen-tunniste] :as sent}
              (if (= msg-state :odottaa-lahetysta)
                (send-palaute-initial-message! ctx viestityyppi)
                {:tila msg-state})]
          (record-palauteviesti!
            ctx viestityyppi (or (:vastaanottaja sent) vastaanottaja)
            tila ulkoinen-tunniste)
          (record-viesti-result! ctx viestityyppi tila)
          (or ulkoinen-tunniste tila))
        (catch ExceptionInfo e
          ;; Most typical reason is that sending failed for some reason
          ;; (and no message has been queued).  This means that there is
          ;; no harm in retrying.  However, we handle the 400 case as
          ;; permanent failure because 400 results are pretty certain to
          ;; persist even if we retry.
          (if (= 400 (:status (ex-data e)))
            (do (record-palauteviesti!
                  ctx viestityyppi vastaanottaja :lahetys-epaonnistunut nil)
                (pt/build-and-insert! ctx ::viestin-lahetys-epaonnistui
                                      {:errormsg (ex-message e)
                                       :body     (:body (ex-data e))
                                       :viestityyppi viestityyppi})
                (record-viesti-result! ctx viestityyppi :lahetys-epaonnistunut)
                :lahetys-epaonnistunut)
            (throw (ex-info "Viestin lähetyksessä tapahtui virhe"
                            {:type ::viestin-lahetys-epaonnistui :ctx ctx}
                            e))))))))

(defn send-invitation!
  "Lähettää viestin yhdelle palautteelle, jos aiheellista."
  [palaute]
  (log/info "Processing email survey invitation for" (:kyselytyyppi palaute)
            "palaute" (:id palaute))
  (handling/call-with-context-and-error-handling
    :lahetys #(palaute-check-send-and-save! %1 %2 :email) palaute))

(declare send-sms-invitation!)

(def sms-kyselytyypit
  "Kyselytyypit, joista lähetetään myös tekstiviesti."
  #{"valmistuneet" "osia_suorittaneet"})

(defn handle-unsent-palaute!
  "Tekee kaiken mitä pitää tehdä palautteelle josta ei ole vielä lähetetty
  viestiä: lähettää sähköpostiviestin ja (jos aiheellista)
  tekstiviestin.  Palauttaa kummankin lähetyksen tuloksen.  Jos
  optiona annetaan :sms? false, lähetetään vain sähköpostiviesti."
  ([palaute] (handle-unsent-palaute! palaute {}))
  ([palaute {:keys [sms?] :or {sms? true}}]
    ;; TODO: also process viestinvalityspalvelu reports
    (let [email-result (send-invitation! palaute)]
      {:email email-result
       ;; palaute is re-read because email handling may have changed it
       :sms (when sms?
              (send-sms-invitation!
                (palaute/get-by-id! db/spec {:id (:id palaute)})))})))

(defn handle-unsent-palautteet!
  "Hakee ja lähettää viestit (kyselykutsut) kaikille palautteille
  joilla on kyselylinkki mutta viestiä ei ole vielä lähetetty.
  Optiot välitetään handle-unsent-palaute!:lle."
  ([kyselytyypit] (handle-unsent-palautteet! kyselytyypit {}))
  ([kyselytyypit opts]
    ;; tep-viestejä ei ole vielä toteutettu
    (assert (not (contains? (set kyselytyypit)
                            "tyopaikkajakson_suorittaneet")))
    (log/info "Processing messages for kyselytyypit" kyselytyypit)
    (doall (map #(handle-unsent-palaute! % opts)
                (palaute/get-unsent-palautteet!
                  db/spec {:kyselytyypit kyselytyypit
                           :viestityyppi "email"})))))

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
    (log/info "Updating Arvo for tunnus" (:arvo-tunniste existing-palaute)
              "to tila lahetetty")
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

(defn palauteviesti-tila
  "Palauttaa palautteen annetun tyyppisen (:email tai :sms) viestin
  tilan, tai nil jos sellaista viestiä ei ole."
  [{:keys [existing-palaute tx]} viestityyppi]
  (->> (get-by-palaute-and-viestityypit!
         (or tx db/spec) {:palaute-id (:id existing-palaute)
                          :viestityypit [(name viestityyppi)]})
       (first)
       :tila))

(defn update-delivery-status!
  "Päivittää lähetysstatuksen yhdelle viestille ja päivittää palautteen
  tiedot vastaavasti."
  [{:keys [existing-palaute existing-viesti tx] :as ctx}]
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
      ;; also temporary fix: Herätepalvelu doesn't ever report failed
      ;; SMS's to Arvo, so we need to report the failed sending here
      ;; already.  If eHOKS sends SMS's, we only report failure when
      ;; SMS sending has failed, too.
      (when (or (not (palaute/ehoks-sends-sms?))
                (= "lahetys_epaonnistunut" (palauteviesti-tila ctx :sms)))
        (arvo/update-kyselytunnus!
          (:arvo-tunniste existing-palaute) "lahetys_epaonnistunut" nil nil))
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

;; SMS-viestit

(defn send-palaute-initial-sms!
  "Lähettää tekstiviestin, jossa kutsutaan vastaamaan Arvossa olevaan
  kyselyyn.  Palauttaa viestin tilan ja Elisan muuntaman numeron.
  Elisa ei kerro viestin perillemenosta, vain siitä, hyväksyttiinkö
  viesti lähetettäväksi."
  [{:keys [existing-palaute hoks opiskeluoikeus]}]
  (let [numero (:puhelinnumero hoks)
        oppilaitos (organisaatio/get-organisaatio!
                     (:oid (:oppilaitos opiskeluoikeus)))
        oppilaitos-nimi (:fi (:nimi oppilaitos))
        body (elisa/amis-msg-body (:kyselylinkki existing-palaute)
                                  oppilaitos-nimi)
        _ (log/info "Sending" (:kyselytyyppi existing-palaute)
                    "SMS invitation for palaute" (:id existing-palaute))
        resp (elisa/send-sms numero body)
        {:keys [status converted]}
        (or (get-in resp [:body :messages (keyword numero)])
            (throw (ex-info "Invalid response from Elisa"
                            {:type ::viestin-lahetys-epaonnistui
                             :body (:body resp)})))]
    (log/info "SMS for palaute" (:id existing-palaute)
              "got Elisa status" status)
    {:tila (if (#{"CREATED" "mock-lahetys"} status)
             :lahetetty
             :lahetys-epaonnistunut)
     :vastaanottaja converted}))

(defn record-viesti-result!
  "Päivittää palautteen, Arvon ja Herätepalvelun tiedot viestin
  lähetyksen (tai lähettämättä jättämisen) jälkeen.  Jos viesti odottaa
  vielä lähetystä, tiedot päivitetään vasta, kun sen tila selviää (ks.
  update-delivery-status!)."
  [{:keys [existing-palaute] :as ctx} viestityyppi viesti-tila]
  (cond
    (= :odottaa-lahetysta viesti-tila)
    nil

    (= "lahetetty" (:tila existing-palaute))
    ;; palaute already sent by another message, so nothing to update
    ;; except message states in Herätepalvelu
    (sync-to-heratepalvelu! ctx)

    (= :lahetetty viesti-tila)
    (record-sending-to-db-hp-and-arvo!
      (assoc ctx :viesti-status viestityyppi))

    :else
    (let [muu-viestityyppi (if (= :sms viestityyppi) :email :sms)]
      ;; failure is reported to Arvo only when all messages have failed
      (when (= "lahetys_epaonnistunut"
               (palauteviesti-tila ctx muu-viestityyppi))
        (arvo/update-kyselytunnus!
          (:arvo-tunniste existing-palaute) "lahetys_epaonnistunut"
          nil nil))
      (sync-to-heratepalvelu! ctx))))

(defn send-sms-invitation!
  "Lähettää tekstiviestin yhdelle palautteelle, jos aiheellista."
  [palaute]
  (log/info "Processing SMS survey invitation for" (:kyselytyyppi palaute)
            "palaute" (:id palaute))
  (handling/call-with-context-and-error-handling
    :lahetys #(palaute-check-send-and-save! %1 %2 :sms) palaute))

(defn handle-palautteet-without-sms!
  "Hakee ja lähettää tekstiviestit (kyselykutsut) kaikille palautteille,
  joiden sähköpostiviesti on käsitelty mutta tekstiviestiä ei.  Ei tee
  mitään, ellei eHOKS ole vastuussa tekstiviestien lähettämisestä."
  [kyselytyypit]
  ;; aloituskyselystä ei lähetetä tekstiviestejä, ja tep-viestejä ei
  ;; ole vielä toteutettu
  (assert (not-any? #{"aloittaneet" "tyopaikkajakson_suorittaneet"}
                    kyselytyypit))
  (if-not (palaute/ehoks-sends-sms?)
    (log/info "handle-palautteet-without-sms!: configured to not send SMS")
    (do (log/info "Processing SMS messages for kyselytyypit" kyselytyypit)
        (doall (map send-sms-invitation!
                    (palaute/get-unsent-palautteet!
                      db/spec {:kyselytyypit kyselytyypit
                               :viestityyppi "sms"}))))))
