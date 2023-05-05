(ns oph.ehoks.virkailija.handler
  (:require [clj-time.core :as t]
            [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.route :as compojure-route]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.resources :as resources]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.virkailija.auth :as auth]
            [oph.ehoks.user :as user]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-oo]
            [oph.ehoks.db.postgresql.common :as pc]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.oppijaindex :as op]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.validation.handler :as validation-handler]
            [clojure.core.async :as a]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.virkailija.system-handler :as system-handler]
            [oph.ehoks.virkailija.external-handler :as external-handler]
            [oph.ehoks.virkailija.cas-handler :as cas-handler]
            [oph.ehoks.heratepalvelu.herate-handler :as herate-handler]
            [oph.ehoks.heratepalvelu.heratepalvelu :as heratepalvelu])
  (:import (clojure.lang ExceptionInfo)
           (java.time LocalDate)))

(def get-oppijat-route
  "Oppijat GET route"
  (c-api/GET "/" request
    :return (restful/response
              [common-schema/OppijaSearchResult]
              :total-count s/Int)
    :query-params [{order-by-column :- s/Str "nimi"}
                   {desc :- s/Bool false}
                   {nimi :- s/Str nil}
                   {tutkinto :- s/Str nil}
                   {osaamisala :- s/Str nil}
                   {item-count :- s/Int 10}
                   {page :- s/Int 0}
                   oppilaitos-oid :- s/Str
                   {locale :- s/Str "fi"}]
    :summary "Listaa virkailijan oppilaitoksen oppijat, joilla on
                       HOKS luotuna. Käyttäjällä pitää olla READ käyttöoikeus
                       annettuun organisaatioon eHOKS-palvelussa."

    (if-not (contains?
              (user/get-organisation-privileges
                (get-in
                  request
                  [:session :virkailija-user])
                oppilaitos-oid)
              :read)
      (do
        (log/warnf
          "User %s privileges does not match oppilaitos %s"
          (get-in request [:session
                           :virkailija-user
                           :oidHenkilo])
          oppilaitos-oid)
        (response/forbidden
          {:error
           (str "User has insufficient privileges for "
                "given organisation")}))
      (let [search-params
            {:desc desc
             :item-count item-count
             :order-by-column order-by-column
             :offset (* page item-count)
             :oppilaitos-oid oppilaitos-oid
             :locale locale
             :nimi nimi
             :tutkinto tutkinto
             :osaamisala osaamisala}
            [oppijat total-count] (op/search! search-params)]
        (restful/rest-ok
          oppijat
          :total-count total-count)))))

(defn- check-opiskeluoikeus-match
  "Check that opiskeluoikeus OID from HOKS matches one held by student"
  [hoks opiskeluoikeudet]
  (if-not
   (op/oppija-opiskeluoikeus-match?
     opiskeluoikeudet (:opiskeluoikeus-oid hoks))
    (assoc
      (response/bad-request!
        {:error "Opiskeluoikeus does not match any held by oppija"})
      :audit-data {:new hoks})))

(defn- add-oppija
  "Insert student whose ID is found in HOKS into database"
  [hoks]
  (try
    (op/add-oppija! (:oppija-oid hoks))
    (catch Exception e
      (if (= (:status (ex-data e)) 404)
        (do
          (log/warn "Oppija with oid "
                    (:oppija-oid hoks)
                    " not found in ONR")
          (response/bad-request!
            {:error
             (str "Oppija not found in"
                  " Oppijanumerorekisteri")}))
        (throw e)))))

(defn- add-opiskeluoikeus
  "Add HOKS opiskeluoikeus to database for HOKS student"
  [hoks]
  (try
    (op/add-opiskeluoikeus!
      (:opiskeluoikeus-oid hoks) (:oppija-oid hoks))
    (catch Exception e
      (cond
        (= (:status (ex-data e)) 404)
        (do
          (log/warn "Opiskeluoikeus with oid "
                    (:opiskeluoikeus-oid hoks)
                    " not found in Koski")
          (response/bad-request!
            {:error "Opiskeluoikeus not found in Koski"}))
        (= (:error (ex-data e)) :hankintakoulutus)
        (response/bad-request!
          {:error (ex-message e)})
        :else (throw e)))))

(defn- add-hankintakoulutukset-to-index
  "Add hankintakoulutukset to index for opiskeluoikeus and oppija in HOKS"
  [hoks opiskeluoikeudet]
  (op/add-oppija-hankintakoulutukset opiskeluoikeudet
                                     (:opiskeluoikeus-oid hoks)
                                     (:oppija-oid hoks)))

(defn- check-opiskeluoikeus-validity
  "Check whether opiskeluoikeus is still valid"
  ([hoks-values]
    (if-not
     (op/opiskeluoikeus-still-active? (:opiskeluoikeus-oid hoks-values))
      (assoc
        (response/bad-request!
          {:error (format "Opiskeluoikeus %s is no longer active"
                          (:opiskeluoikeus-oid hoks-values))})
        :audit-data {:new hoks-values})))
  ([hoks opiskeluoikeudet]
    (if-not
     (op/opiskeluoikeus-still-active? hoks opiskeluoikeudet)
      (assoc
        (response/bad-request!
          {:error (format "Opiskeluoikeus %s is no longer active"
                          (:opiskeluoikeus-oid hoks))})
        :audit-data {:new hoks}))))

(defn- check-virkailija-privileges
  "Check whether virkailija user has write privileges in HOKS"
  [hoks request]
  (let [virkailija-user
        (get-in request [:session :virkailija-user])]
    (when-not
     (m/virkailija-has-privilege-in-opiskeluoikeus?
       virkailija-user (:opiskeluoikeus-oid hoks) :write)
      (log/warnf "User %s privileges don't match oppija %s"
                 (get-in request [:session
                                  :virkailija-user
                                  :oidHenkilo])
                 (:oppija-oid hoks))
      (response/forbidden!
        {:error
         (str "User has unsufficient privileges")}))))

(defn- check-for-missing-tyopaikan-y-tunnus
  "Check whether any osaamisen hankkimistavat are missing työpaikan Y-tunnus"
  [hoks]
  (let [osaamisen-hankkimistavat (h/get-osaamisen-hankkimistavat hoks)
        oh-missing-tyopaikan-y-tunnus (h/missing-tyopaikan-y-tunnus?
                                        osaamisen-hankkimistavat)]
    (when (some? oh-missing-tyopaikan-y-tunnus)
      (assoc
        (response/bad-request!
          {:error (str "tyopaikan-y-tunnus missing for "
                       "osaamisen hankkimistapa: "
                       oh-missing-tyopaikan-y-tunnus)})
        :audit-data {:new hoks}))))

(defn- save-hoks
  "Save HOKS to database"
  [hoks request]
  (try
    (let [hoks-db (h/save-hoks!
                    (assoc hoks :manuaalisyotto true))]
      (assoc
        (restful/rest-ok
          {:uri (format "%s/%d"
                        (:uri request)
                        (:id hoks-db))}
          :id (:id hoks-db))
        :audit-data {:new hoks}))
    (catch Exception e
      (case (:error (ex-data e))
        :disallowed-update (assoc
                             (response/bad-request! {:error (.getMessage e)})
                             :audit-data {:new hoks})
        :duplicate (do
                     (log/warnf
                       "HOKS with opiskeluoikeus-oid %s already exists"
                       (:opiskeluoikeus-oid hoks))
                     (response/bad-request! {:error (.getMessage e)}))
        (throw e)))))

(defn- post-oppija
  "Add new HOKS for oppija"
  [hoks request]
  (let [opiskeluoikeudet
        (koski/fetch-opiskeluoikeudet-by-oppija-id (:oppija-oid hoks))]
    (check-opiskeluoikeus-match hoks opiskeluoikeudet)
    (check-opiskeluoikeus-validity hoks opiskeluoikeudet)
    (check-for-missing-tyopaikan-y-tunnus hoks)
    (add-oppija hoks)
    (add-opiskeluoikeus hoks)
    (add-hankintakoulutukset-to-index hoks opiskeluoikeudet))
  (check-virkailija-privileges hoks request)
  (save-hoks hoks request))

(defn- get-hoks-perustiedot
  "Get basic information from HOKS"
  [oppija-oid]
  (if-let [hoks
           (db-hoks/select-hoks-by-oppija-oid oppija-oid)]
    (restful/rest-ok hoks)
    (response/not-found {:message "HOKS not found"})))

(defn- hoks-has-active-opiskeluoikeus
  "Check if HOKS has an active opiskeluoikeus"
  [hoks]
  (some #(op/opiskeluoikeus-active? (koski/get-opiskeluoikeus-info %))
        (map :opiskeluoikeus-oid hoks)))

(defn- get-oppilaitos-oid-by-oo-oid
  "Get oppilaitos OID by opiskeluoikeus OID"
  [opiskeluoikeus-oid]
  (let [opiskeluoikeus
        (db-oo/select-opiskeluoikeus-by-oid opiskeluoikeus-oid)]
    (:oppilaitos-oid opiskeluoikeus)))

(defn- get-hoks-by-oppilaitos
  "Filters for HOKSes associated with a particular oppilaitos"
  [oppilaitos-oid hoksit]
  (filter
    #(= oppilaitos-oid (get-oppilaitos-oid-by-oo-oid (:opiskeluoikeus-oid %)))
    hoksit))

(defn- get-hoks
  "Get HOKS by ID, if user has sufficient permissions"
  [hoks-id request]
  (let [hoks (db-hoks/select-hoks-by-id hoks-id)
        virkailija-user (get-in
                          request
                          [:session :virkailija-user])]
    (if (m/virkailija-has-privilege?
          virkailija-user (:oppija-oid hoks) :read)
      (restful/rest-ok (h/get-hoks-by-id hoks-id))
      (do
        (log/warnf
          "User %s privileges don't match oppija %s"
          (get-in request [:session
                           :virkailija-user
                           :oidHenkilo])
          (get-in request [:params :oppija-oid]))
        (response/forbidden
          {:error
           (str "User has insufficient privileges")})))))

(defn- put-hoks
  "Replace HOKS with particular ID"
  [hoks-values hoks-id]
  (try
    (check-opiskeluoikeus-validity hoks-values)
    (let [hoks-db
          (h/replace-hoks! hoks-id hoks-values)]
      (assoc
        (response/no-content)
        :audit-data
        {:new  hoks-values}))
    (catch Exception e
      (if (= (:error (ex-data e)) :disallowed-update)
        (assoc
          (response/bad-request!
            {:error
             (.getMessage e)})
          :audit-data {:new hoks-values})
        (throw e)))))

(defn- patch-hoks
  "Patch HOKS with particular ID"
  [hoks-values hoks-id]
  (try
    (check-opiskeluoikeus-validity hoks-values)
    (let [hoks-db
          (h/update-hoks! hoks-id hoks-values)]
      (assoc
        (response/no-content)
        :audit-data
        {:new  hoks-values}))
    (catch Exception e
      (if (= (:error (ex-data e)) :disallowed-update)
        (assoc
          (response/bad-request!
            {:error
             (.getMessage e)})
          :audit-data {:new hoks-values})
        (throw e)))))

(defn- get-vastaajatunnus-info
  "Get details of vastaajatunnus from database or Arvo"
  [tunnus]
  (let [linkit (pc/select-kyselylinkit-by-tunnus tunnus)]
    (if (seq linkit)
      (let [linkki-info (if (:vastattu (first linkit))
                          (first linkit)
                          (let [status (arvo/get-kyselytunnus-status tunnus)
                                loppupvm (LocalDate/parse
                                           (first
                                             (str/split
                                               (:voimassa_loppupvm status)
                                               #"T")))]
                            (h/update-kyselylinkki!
                              {:kyselylinkki (:kyselylinkki (first linkit))
                               :voimassa_loppupvm loppupvm
                               :vastattu (:vastattu status)})
                            (assoc (first linkit)
                                   :voimassa-loppupvm loppupvm
                                   :vastattu (:vastattu status))))
            opiskeluoikeus (koski/get-opiskeluoikeus-info
                             (:opiskeluoikeus-oid linkki-info))
            linkki-info (assoc linkki-info
                               :koulutustoimijan-oid
                               (:oid (:koulutustoimija opiskeluoikeus))
                               :koulutustoimijan-nimi
                               (:nimi (:koulutustoimija opiskeluoikeus)))]
        (restful/rest-ok linkki-info))
      (response/bad-request {:error "Survey ID not found"}))))

(defn- delete-vastaajatunnus
  "Delete vastaajatunnus from Arvo, database, and Herätepalvelu"
  [tunnus]
  (try
    (let [linkit (pc/select-kyselylinkit-by-tunnus tunnus)]
      (arvo/delete-kyselytunnus tunnus)
      (pc/delete-kyselylinkki-by-tunnus tunnus)
      (sqs/send-delete-tunnus-message (:kyselylinkki (first linkit)))
      (response/ok))
    (catch ExceptionInfo e
      (if (and (= 404 (:status (ex-data e)))
               (.contains (:body (ex-data e)) "Tunnus ei ole poistettavissa"))
        (response/bad-request {:error "Survey ID cannot be removed"})
        (throw e)))))

(defn check-suoritus-type?
  "Check that suoritus is either ammatillinen tutkinto or ammatillinen
  osittainen tutkinto"
  [suoritus]
  (or (= (:koodiarvo (:tyyppi suoritus)) "ammatillinentutkinto")
      (= (:koodiarvo (:tyyppi suoritus)) "ammatillinentutkintoosittainen")))

(defn- get-vahvistus-pvm
  "Extract vahvistuspäivämäärä from opiskeluoikeus"
  [opiskeluoikeus]
  (if-let [vahvistus-pvm
           (reduce
             (fn [_ suoritus]
               (when (check-suoritus-type? suoritus)
                 (reduced (get-in suoritus [:vahvistus :päivä]))))
             nil (:suoritukset opiskeluoikeus))]
    vahvistus-pvm
    (log/warn "Opiskeluoikeudessa" (:oid opiskeluoikeus)
              "ei vahvistus päivämäärää")))

(def routes
  "Virkailija handler routes"
  (c-api/context "/ehoks-virkailija-backend" []
    :tags ["ehoks"]

    (c-api/context "/cas-security-check" []
      cas-handler/routes)

    (c-api/context "/api" []
      :tags ["api"]
      (c-api/context "/v1" []
        :tags ["v1"]

        hoks-handler/routes
        herate-handler/routes

        (route-middleware
          [wrap-audit-logger]

          validation-handler/routes

          (c-api/context "/virkailija" []
            :tags ["virkailija"]
            auth/routes

            (route-middleware
              [m/wrap-virkailija-authorize m/wrap-require-virkailija-user]

              external-handler/routes
              system-handler/routes

              (c-api/GET "/tep-jakso-raportti" request
                :summary "Työpaikkajaksojen raportti"
                :header-params [caller-id :- s/Str]
                :query-params [tutkinto :- s/Str
                               oppilaitos :- (s/maybe s/Str)
                               start :- LocalDate
                               end :- LocalDate
                               {pagesize :- s/Int 25}
                               {pageindex :- s/Int 0}]
                (cond (and oppilaitos
                           (contains?
                             (user/get-organisation-privileges
                               (get-in request [:session :virkailija-user])
                               oppilaitos)
                             :read))
                      (let [result
                            (pc/get-oppilaitos-oids-cached-memoized ;;5min cache
                              tutkinto
                              oppilaitos
                              start
                              end)
                            row-count-total (count result)
                            page-count-total (int (Math/ceil
                                                    (/ row-count-total
                                                       pagesize)))
                            start-row (* pagesize pageindex)
                            end-row (if (<= (+ start-row pagesize)
                                            row-count-total)
                                      (+ start-row pagesize)
                                      row-count-total)
                            pageresult (subvec (vec result) start-row end-row)]
                        (restful/rest-ok
                          {:count row-count-total
                           :pagecount page-count-total
                           :result pageresult}))
                      (user/oph-super-user?
                        (get-in request [:session :virkailija-user]))
                      (restful/rest-ok
                        (pc/select-oht-by-tutkinto-between tutkinto start end))
                      :else
                      (do (log/warnf
                            "TEP jaksoraportti:"
                            "User %s privileges do not match oppilaitos %s"
                            (get-in request
                                    [:session :virkailija-user :oidHenkilo])
                            oppilaitos)
                          (response/forbidden
                            {:error
                             "User privileges do not match organisation"}))))

              (c-api/GET "/vastaajatunnus/:tunnus" []
                :summary "Vastaajatunnuksen tiedot"
                :header-params [caller-id :- s/Str]
                :path-params [tunnus :- s/Str]
                (get-vastaajatunnus-info tunnus))

              (c-api/DELETE "/vastaajatunnus/:tunnus" []
                :summary "Vastaajatunnuksen poisto"
                :header-params [caller-id :- s/Str]
                :path-params [tunnus :- s/Str]
                (delete-vastaajatunnus tunnus))

              (c-api/GET "/missing-oo-hoksit/:oppilaitosoid" request
                :summary "Palauttaa listan hokseista, joiden
                          opiskeluoikeus puuttuu"
                :header-params [caller-id :- s/Str]
                :path-params [oppilaitosoid :- s/Str]
                :query-params [{pagesize :- s/Int 25}
                               {pageindex :- s/Int 0}]
                (if (contains? (user/get-organisation-privileges
                                 (get-in request [:session :virkailija-user])
                                 oppilaitosoid)
                               :read)
                  (let [result
                        (db-hoks/select-hoksit-by-oo-oppilaitos-and-koski404
                          oppilaitosoid)
                        row-count-total (count result)
                        page-count-total (int (Math/ceil
                                                (/ row-count-total
                                                   pagesize)))
                        start-row (* pagesize pageindex)
                        end-row (if (<= (+ start-row pagesize)
                                        row-count-total)
                                  (+ start-row pagesize)
                                  row-count-total)
                        pageresult (subvec (vec result) start-row end-row)]
                    (restful/rest-ok
                      {:count row-count-total
                       :pagecount page-count-total
                       :result pageresult}))
                  (response/forbidden
                    {:error (str "User privileges does not match "
                                 "organisation")})))

              (c-api/GET "/paattyneet-kyselylinkit-temp" request
                :summary "Palauttaa tietoja kyselylinkkeihin liittyvistä
                          hokseista."
                :query-params [{alkupvm :- LocalDate (LocalDate/parse
                                                       "2021-09-01")}
                               {alkupvm-loppu :- LocalDate (LocalDate/now)}
                               {limit :- s/Int 2000}
                               {from-id :- s/Int 0}]
                (let [data (db-hoks/select-kyselylinkit-by-date-and-type-temp
                             alkupvm alkupvm-loppu from-id limit)
                      last-id (:hoks-id (last data))]
                  (try
                    (let [hoks-infos
                          (map
                            (fn [{ospvm :osaamisen-saavuttamisen-pvm
                                  oo-id :opiskeluoikeus-oid}]
                              (let [opiskeluoikeus
                                    (when oo-id
                                      (koski/get-opiskeluoikeus-info oo-id))
                                    vahvistus-pvm
                                    (when opiskeluoikeus
                                      (get-vahvistus-pvm opiskeluoikeus))]
                                {:osaamisen-saavuttamisen-pvm ospvm
                                 :vahvistus-pvm vahvistus-pvm}))
                            data)]
                      (response/ok {:last-id last-id
                                    :paattokysely-total-count (count hoks-infos)
                                    :o-s-pvm-ilman-vahvistuspvm-count
                                    (count (filter
                                             #(and
                                                (some?
                                                  (:osaamisen-saavuttamisen-pvm
                                                    %))
                                                (nil? (:vahvistus-pvm %)))
                                             hoks-infos))
                                    :vahvistuspvm-ilman-o-s-pvm-count
                                    (count (filter
                                             #(and
                                                (some? (:vahvistus-pvm %))
                                                (nil?
                                                  (:osaamisen-saavuttamisen-pvm
                                                    %)))
                                             hoks-infos))
                                    :vahvistuspvm-ja-o-s-pvm
                                    (count (filter
                                             #(and
                                                (some?
                                                  (:osaamisen-saavuttamisen-pvm
                                                    %))
                                                (some? (:vahvistus-pvm %)))
                                             hoks-infos))}))
                    (catch Exception e
                      (response/bad-request {:error e})))))

              (c-api/GET "/puuttuvat-opiskeluoikeudet-temp" request
                :summary "Palauttaa listan hoks-id:sta, joiden opiskeluoikeutta
                ei löydy. Asettaa löydettyjen hoksien oo-indeksiin koski404 =
                true."
                :query-params [{limit :- s/Int 2000}
                               {from-id :- s/Int 0}]
                (let [hoksit (db-hoks/select-hokses-greater-than-id
                               from-id
                               limit
                               nil)
                      last-id (:id (last hoksit))]
                  (try
                    (let [hokses-without-oo
                          (filter
                            some?
                            (pmap
                              (fn [x]
                                (when (nil?
                                        (koski/get-opiskeluoikeus-info
                                          (:opiskeluoikeus-oid x))) x)) hoksit))
                          result (map :id hokses-without-oo)]
                      (doseq [oo-oid (map :opiskeluoikeus-oid
                                          hokses-without-oo)]
                        (let [opiskeluoikeus
                              (db-oo/select-opiskeluoikeus-by-oid oo-oid)]
                          (when (some? opiskeluoikeus)
                            (op/set-opiskeluoikeus-koski404 oo-oid))))
                      (response/ok {:count (count result)
                                    :ids result
                                    :last-id last-id}))
                    (catch Exception e
                      (response/bad-request {:error e})))))

              (c-api/context "/oppijat" []
                :header-params [caller-id :- s/Str]
                get-oppijat-route

                (c-api/context "/:oppija-oid" []
                  :path-params [oppija-oid :- s/Str]

                  (c-api/POST "/index" []
                    :summary
                    "Indeksoi oppijan tiedot, jos on tarpeen. DEPRECATED"
                    (a/go
                      (response/ok {:message "Route is deprected."})))

                  (c-api/context "/hoksit" []
                    (c-api/POST "/" [:as request]
                      :summary (str "Luo uuden HOKSin. "
                                    "Vaatii manuaalisyöttäjän oikeudet")
                      :body [hoks hoks-schema/HOKSLuonti]
                      :return (restful/response schema/POSTResponse :id s/Int)
                      (post-oppija hoks request))

                    (route-middleware
                      [m/wrap-virkailija-oppija-access]
                      (c-api/GET "/" []
                        :return (restful/response [hoks-schema/HOKS])
                        :summary "Oppijan hoksit (perustiedot)"
                        (get-hoks-perustiedot oppija-oid))

                      (c-api/GET "/oppilaitos/:oppilaitos-oid" request
                        :path-params [oppilaitos-oid :- s/Str]
                        :return (restful/response [hoks-schema/HOKS])
                        :summary "Oppijan hoksit (perustiedot,
                                                  rajoitettu uusi versio)"
                        (if (contains?
                              (user/get-organisation-privileges
                                (get-in
                                  request
                                  [:session :virkailija-user])
                                oppilaitos-oid)
                              :read)
                          (if-let [hoks (db-hoks/select-hoks-by-oppija-oid
                                          oppija-oid)]
                            (if-let [oppilaitos-hoks (get-hoks-by-oppilaitos
                                                       oppilaitos-oid hoks)]
                              (restful/rest-ok
                                (if
                                 (hoks-has-active-opiskeluoikeus
                                   oppilaitos-hoks)
                                  hoks
                                  oppilaitos-hoks))
                              (response/not-found
                                {:message
                                 "HOKS not found for oppilaitos"}))
                            (response/not-found {:message "HOKS not found"}))
                          (do
                            (log/warnf "User %s privileges
                          don't match oppilaitos %s"
                                       (get-in request
                                               [:session
                                                :virkailija-user :oidHenkilo])
                                       (get-in request [:params
                                                        :oppilaitos-oidd]))
                            (response/forbidden
                              {:error (str "User privileges does not match "
                                           "organisation")}))))

                      (c-api/GET "/:hoks-id" request
                        :path-params [hoks-id :- s/Int]
                        :summary "Hoksin tiedot.
                                Vaatii manuaalisyöttäjän oikeudet"
                        (get-hoks hoks-id request))

                      (c-api/POST "/:hoks-id/resend-palaute" request
                        :summary "Lähettää herätepalveluun pyynnön palautelinkin
                                  uudelleen lähetykselle"
                        :path-params [hoks-id :- s/Int]
                        :body [data hoks-schema/palaute-resend]
                        (let [kyselylinkit
                              (heratepalvelu/get-oppija-kyselylinkit
                                oppija-oid)
                              kyselylinkki (first
                                             (filter
                                               #(and (= (:hoks-id %1) hoks-id)
                                                     (= (:tyyppi %1)
                                                        (:tyyppi data)))
                                               kyselylinkit))
                              hoks (db-hoks/select-hoks-by-id hoks-id)]
                          (if-not (:vastattu kyselylinkki)
                            (sqs/send-palaute-resend-message
                              {:kyselylinkki (:kyselylinkki kyselylinkki)
                               :sahkoposti (:sahkoposti hoks)})
                            (h/update-kyselylinkki!
                              {:kyselylinkki (:kyselylinkki kyselylinkki)
                               :sahkoposti (:sahkoposti hoks)
                               :lahetyspvm (LocalDate/parse (str (t/today)))
                               :lahetystila "lahetetty"}))
                          (restful/rest-ok
                            {:sahkoposti (:sahkoposti hoks)})))

                      (c-api/GET "/:hoks-id/opiskelijapalaute" request
                        :summary "Palauttaa tietoja oppijan aktiivisista
                                  kyselylinkeistä (ilman kyselytunnuksia)"
                        :path-params [hoks-id :- s/Int]
                        (let [kyselylinkit
                              (heratepalvelu/get-oppija-kyselylinkit
                                oppija-oid)
                              lahetysdata
                              (map
                                #(dissoc %1 :kyselylinkki :vastattu)
                                (filter
                                  #(and
                                     (= (:hoks-id %1) hoks-id)
                                     (not (nil? (:lahetystila %1)))
                                     (not= "ei_lahetetty" (:lahetystila %1)))
                                  kyselylinkit))]
                          (restful/rest-ok lahetysdata)))

                      (route-middleware
                        [m/wrap-virkailija-write-access]

                        (c-api/context "/:hoks-id" []
                          :path-params [hoks-id :- s/Int]

                          (c-api/PUT "/" request
                            :summary
                            "Ylikirjoittaa olemassa olevan HOKSin arvon tai
                             arvot"
                            :body [hoks-values hoks-schema/HOKSKorvaus]
                            (put-hoks hoks-values hoks-id))

                          (c-api/PATCH "/" request
                            :body [hoks-values hoks-schema/HOKSPaivitys]
                            :summary "Oppijan hoksin päätason arvojen päivitys"
                            (patch-hoks hoks-values hoks-id))))

                      (c-api/context "/:hoks-id" []
                        :path-params [hoks-id :- s/Int]

                        (c-api/PATCH "/shallow-delete" request
                          :summary "Asettaa HOKSin
                              poistetuksi(shallow delete) id:n perusteella."
                          :body [data hoks-schema/shallow-delete-hoks]
                          (let [hoks (h/get-hoks-by-id hoks-id)
                                oppilaitos-oid (if (seq (:oppilaitos-oid data))
                                                 (:oppilaitos-oid data)
                                                 (:oppilaitos-oid
                                                   (op/get-opiskeluoikeus-by-oid
                                                     (:opiskeluoikeus-oid
                                                       hoks))))
                                opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
                                opiskeluoikeus (koski/get-opiskeluoikeus-info
                                                 opiskeluoikeus-oid)]
                            (if (or (nil? opiskeluoikeus)
                                    (op/opiskeluoikeus-active? opiskeluoikeus))
                              (if (seq oppilaitos-oid)
                                (if (contains?
                                      (user/get-organisation-privileges
                                        (get-in
                                          request [:session :virkailija-user])
                                        oppilaitos-oid)
                                      :hoks_delete)
                                  (try
                                    (db-hoks/shallow-delete-hoks-by-hoks-id
                                      hoks-id)
                                    (when (nil? opiskeluoikeus)
                                      (db-hoks/delete-opiskeluoikeus-by-oid
                                        opiskeluoikeus-oid))
                                    (assoc
                                      (response/ok {:success hoks-id})
                                      :audit-data {:old hoks
                                                   :new (assoc
                                                          hoks
                                                          :deleted_at
                                                          "*ADDED*")})
                                    (catch Exception e
                                      (response/bad-request!
                                        {:error (ex-message e)})))
                                  (response/forbidden
                                    {:error
                                     (str "User privileges do not match "
                                          "organisation")}))
                                (response/forbidden
                                  {:error
                                   (str "Oppilaitos-oid not found. Contact "
                                        "eHOKS support for more "
                                        "information.")}))
                              (response/forbidden
                                {:error
                                 (format
                                   "Opiskeluoikeus %s is no longer active"
                                   (:opiskeluoikeus-oid hoks))})))))

                      (route-middleware
                        [m/wrap-oph-super-user]

                        (c-api/GET "/" []
                          :summary "Kaikki hoksit (perustiedot).
                        Tarvitsee OPH-pääkäyttäjän oikeudet"
                          (restful/rest-ok (db-hoks/select-hoksit))))))

                  (route-middleware
                    [m/wrap-virkailija-oppija-access]

                    (c-api/GET "/opiskeluoikeudet" [:as request]
                      :summary "Oppijan opiskeluoikeudet"
                      :return (restful/response [s/Any])
                      (restful/rest-ok
                        (koski/get-oppija-opiskeluoikeudet oppija-oid)))

                    (c-api/GET "/" []
                      :return (restful/response common-schema/Oppija)
                      :summary "Oppijan tiedot"
                      (if-let [oppija (op/get-oppija-by-oid oppija-oid)]
                        (restful/rest-ok oppija)
                        (response/not-found)))

                    (c-api/GET "/with-oo" []
                      :return (restful/response common-schema/Oppija)
                      :summary "Oppijan tiedot. Opiskeluoikeus-oid lisättynä."
                      (if-let [oppija (op/get-oppija-with-oo-oid-by-oid
                                        oppija-oid)]
                        (restful/rest-ok oppija)
                        (response/not-found)))))))))

        healthcheck-handler/routes
        misc-handler/routes))

    (c-api/undocumented
      (c-api/GET "/buildversion.txt" []
        (response/content-type
          (response/resource-response "buildversion.txt") "text/plain"))
      (resources/create-routes "/json-viewer" "json-viewer"))))

(def app-routes
  "Virkailija handler app routes"
  (c-api/api
    {:swagger
     {:ui "/ehoks-virkailija-backend/doc"
      :spec "/ehoks-virkailija-backend/doc/swagger.json"
      :data {:info {:title "eHOKS virkailija backend"
                    :description "eHOKS virkailijan näkymän ja
                                  HOKS-rajapinnan backend"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers common-api/handlers}}

    routes
    (c-api/undocumented
      (compojure-route/not-found
        (response/not-found {:reason "Route not found"})))))
