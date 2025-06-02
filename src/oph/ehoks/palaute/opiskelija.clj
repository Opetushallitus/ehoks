(ns oph.ehoks.palaute.opiskelija
  "A namespace for everything related to opiskelijapalaute"
  (:require [clojure.string :as str]
            [medley.core :refer [greatest map-vals]]
            [oph.ehoks.db.dynamodb :as dynamodb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date]))

(def kyselytyypit #{"aloittaneet" "valmistuneet" "osia_suorittaneet"})

(def translate-source
  "Translate palaute-db heräte source name to equivalent used in Herätepalvelu."
  {"ehoks_update" "sqs_viesti_ehoksista"
   "koski_update" "tiedot_muuttuneet_koskessa"})

(defn osaamisen-hankkimistavat->tutkinnonosat-hankkimistavoittain
  [ohts]
  (->> ohts
       (map (juxt (comp keyword
                        utils/koodiuri->koodi
                        :osaamisen-hankkimistapa-koodi-uri)
                  :tutkinnon-osa-koodi-uri))
       (filter second)  ; ei paikallisia (joilta tutkinnonosakoodi puuttuu)
       (set)            ; ei duplikaatteja
       (group-by first)
       (map-vals (partial map second))))

(defn build-kyselylinkki-request-body
  "For the given palaute, create Arvo request for creating its kyselylinkki."
  [{:keys [existing-palaute hoks opiskeluoikeus suoritus request-id
           koulutustoimija toimipiste hk-toteuttaja] :as ctx}]
  (let [heratepvm (:heratepvm existing-palaute)
        alkupvm (greatest heratepvm (date/now))]
    {:hankintakoulutuksen_toteuttaja @hk-toteuttaja
     :tutkinnon_suorituskieli (or (suoritus/kieli suoritus) "fi")
     :kyselyn_tyyppi (palaute/translate-kyselytyyppi
                       (:kyselytyyppi existing-palaute))
     :osaamisala (suoritus/get-osaamisalat suoritus heratepvm)
     :tutkinnonosat_hankkimistavoittain
     (osaamisen-hankkimistavat->tutkinnonosat-hankkimistavoittain
       (oht/osaamisen-hankkimistavat hoks))
     :vastaamisajan_alkupvm alkupvm
     :vastaamisajan_loppupvm (palaute/vastaamisajan-loppupvm heratepvm alkupvm)
     :toimipiste_oid toimipiste
     :tutkintotunnus (str (suoritus/tutkintotunnus suoritus))
     :oppilaitos_oid (:oid (:oppilaitos opiskeluoikeus))
     :koulutustoimija_oid (or koulutustoimija "")
     :heratepvm (:heratepvm existing-palaute)
     :request_id request-id}))

(defn build-amisherate-record-for-heratepalvelu
  "Turns the information context into AMISherate in heratepalvelu format."
  [{:keys [existing-palaute hoks koulutustoimija suoritus
           opiskeluoikeus toimipiste hk-toteuttaja arvo-response
           request-id] :as ctx}]
  (let [heratepvm (:heratepvm existing-palaute)
        oppija-oid (:oppija-oid hoks)
        rahoituskausi (palaute/rahoituskausi heratepvm)
        kyselytyyppi (palaute/translate-kyselytyyppi
                       (:kyselytyyppi existing-palaute))
        alkupvm (greatest heratepvm (date/now))]
    (utils/remove-nils
      {:sahkoposti (:sahkoposti hoks)
       :heratepvm heratepvm
       :alkupvm alkupvm
       :voimassa-loppupvm (palaute/vastaamisajan-loppupvm heratepvm alkupvm)
       :toimipiste_oid toimipiste
       :lahetystila ; FIXME when it can have other states
       (if (not-empty (:sahkoposti hoks))
         "ei_lahetetty"
         "ei_laheteta")
       :puhelinnumero (:puhelinnumero hoks)
       :hankintakoulutuksen_toteuttaja @hk-toteuttaja
       :ehoks-id (:id hoks)
       :herate-source (or (translate-source (:herate-source existing-palaute))
                          "sqs_viesti_ehoksista")
       :koulutustoimija koulutustoimija
       :tutkintotunnus (suoritus/tutkintotunnus suoritus)
       :kyselylinkki (or (:kysely_linkki arvo-response)
                         (:kyselylinkki existing-palaute))
       :kyselytyyppi kyselytyyppi
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
       :oppija-oid oppija-oid
       :oppilaitos (:oid (:oppilaitos opiskeluoikeus))
       :osaamisala (str/join "," (suoritus/get-osaamisalat suoritus heratepvm))
       :rahoituskausi rahoituskausi
       :sms-lahetystila (if (and (or (= kyselytyyppi "tutkinnon_suorittaneet")
                                     (= kyselytyyppi
                                        "tutkinnon_osia_suorittaneet"))
                                 (not-empty (:puhelinnumero hoks)))
                          "ei_lahetetty"
                          "ei_laheteta")
       :tallennuspvm (date/now)
       :toimija_oppija (str koulutustoimija "/" oppija-oid)
       :tyyppi_kausi (str kyselytyyppi "/" rahoituskausi)
       :request-id request-id})))

;; these use vars (#') because otherwise with-redefs doesn't work on
;; them (the map has the original definition even if the function in
;; its namespace is redef'd)

(def handlers
  {:arvo-builder #'build-kyselylinkki-request-body
   :arvo-caller #'arvo/create-kyselytunnus!
   :heratepalvelu-builder #'build-amisherate-record-for-heratepalvelu
   :heratepalvelu-caller #'dynamodb/sync-amis-herate!
   :extra-handlers []})
