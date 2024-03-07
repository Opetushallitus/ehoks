(ns oph.ehoks.virkailija.handler
  (:require [clj-time.core :as t]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [compojure.route :as compojure-route]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.common.utils :refer [apply-when]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-oo]
            [oph.ehoks.db.postgresql.common :as pc]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.heratepalvelu.herate-handler :as herate-handler]
            [oph.ehoks.heratepalvelu.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.middleware :refer [wrap-require-user-type-and-auth
                                          wrap-hoks
                                          wrap-opiskeluoikeus]]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.resources :as resources]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.schema.oid :as oid-schema]
            [oph.ehoks.user :as user]
            [oph.ehoks.validation.handler :as validation-handler]
            [oph.ehoks.virkailija.auth :as auth]
            [oph.ehoks.virkailija.cas-handler :as cas-handler]
            [oph.ehoks.virkailija.external-handler :as external-handler]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.virkailija.system-handler :as system-handler]
            [ring.util.http-response :as response]
            [schema.core :as s])
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
                   {hoks-id :- s/Int nil}
                   {item-count :- s/Int 10}
                   {page :- s/Int 0}
                   oppilaitos-oid :- oid-schema/OrganisaatioOID
                   {locale :- s/Str "fi"}]
    :summary "Listaa virkailijan oppilaitoksen oppijoiden opiskeluoikeudet,
             joilla on HOKS luotuna. Käyttäjällä pitää olla READ-käyttöoikeus
             annettuun organisaatioon eHOKS-palvelussa."
    (assoc
      (let [virkailija-user (user/get request ::user/virkailija)]
        (if (user/has-privilege-in-oppilaitos? virkailija-user
                                              :read
                                              oppilaitos-oid)
          (let [[oppijat total-count]
                (oi/search! {:desc desc
                              :item-count item-count
                              :order-by-column order-by-column
                              :offset (* page item-count)
                              :oppilaitos-oid oppilaitos-oid
                              :locale locale
                              :nimi nimi
                              :tutkinto tutkinto
                              :osaamisala osaamisala
                              :hoks-id hoks-id})]
            (restful/rest-ok oppijat :total-count total-count))
          (response/forbidden {:error (str "User has insufficient privileges "
                                           "for given organisation")})))
        :audit-data
        {:target (dissoc (:params request) :order-by-column :desc :locale)})))

(defn- post-hoks!
  "Add new HOKS for oppija"
  [hoks request]
  (let [virkailija (user/get request ::user/virkailija)]
    (assoc-in
      (try (oi/add-hoks-dependents-in-index! hoks)
           (if (user/has-privilege-to-hoks? virkailija :write hoks)
             (let [hoks-db (-> (h/add-missing-oht-yksiloiva-tunniste hoks)
                               (assoc :manuaalisyotto true)
                               (h/check-and-save-hoks!))]
               (-> {:uri (format "%s/%d" (:uri request) (:id hoks-db))}
                   (restful/rest-ok :id (:id hoks-db))
                   (assoc :audit-data {:target {:hoks-id (:id hoks-db)}
                                       :new    hoks})))
             (do (log/warnf "User %s privileges don't match oppija %s"
                            (:oidHenkilo virkailija)
                            (:oppija-oid hoks))
                 (response/forbidden
                   {:error (str "User has unsufficient privileges")})))
           (catch ExceptionInfo e
             (if (contains? #{:disallowed-update :duplicate-opiskeluoikeus}
                            (:error (ex-data e)))
               (do (log/warn e) (response/bad-request {:error (ex-message e)}))
               (throw e))))
      [:audit-data :target :opiskeluoikeus-oid] (:opiskeluoikeus-oid hoks))))


(defn- get-hoks-perustiedot
  "Get basic information from HOKS"
  [oppija-oid]
  (let [hoksit   (seq (db-hoks/select-hoks-by-oppija-oid oppija-oid))
        response (if hoksit
                   (restful/rest-ok hoksit)
                   (response/not-found {:message "HOKS not found"}))]
    (assoc response
           :audit-data {:target {:hoks-id (map :id hoksit)
                                 :oppija-oid oppija-oid
                                 :opiskeluoikeus-oid
                                 (map :opiskeluoikeus-oid hoksit)}})))

(defn- any-hoks-has-active-opiskeluoikeus?
  "Check if any of the HOKSes have an active opiskeluoikeus"
  [hoksit]
  (some #(oi/opiskeluoikeus-active? (koski/get-opiskeluoikeus-info %))
        (map :opiskeluoikeus-oid hoksit)))

(defn- get-oppilaitos-oid-by-oo-oid
  "Get oppilaitos OID by opiskeluoikeus OID"
  [opiskeluoikeus-oid]
  (let [opiskeluoikeus
        (db-oo/select-opiskeluoikeus-by-oid opiskeluoikeus-oid)]
    (:oppilaitos-oid opiskeluoikeus)))

(defn- get-hoksit-by-oppilaitos
  "Filters for HOKSes associated with a particular oppilaitos"
  [oppilaitos-oid hoksit]
  (filter
    #(= oppilaitos-oid (get-oppilaitos-oid-by-oo-oid (:opiskeluoikeus-oid %)))
    hoksit))

(defn- get-oppilaitoksen-oppijan-hoksit
  [oppilaitos-oid oppija-oid]
  (update-in
    (let [hoksit (db-hoks/select-hoks-by-oppija-oid oppija-oid)]
      (if-let [hoksit-oppilaitoksessa (seq (get-hoksit-by-oppilaitos
                                             oppilaitos-oid hoksit))]
        (let [hoksit (if (any-hoks-has-active-opiskeluoikeus?
                           hoksit-oppilaitoksessa)
                       hoksit-oppilaitoksessa
                       hoksit)]
          (assoc (restful/rest-ok hoksit)
                :audit-data {:target {:hoks-id (map :id hoksit)
                                      :opiskeluoikeus-oid
                                      (map :opiskeluoikeus-oid hoksit)}}))
        (response/not-found {:message "HOKS not found for oppilaitos"})))
    [:audit-data :target] assoc :oppilaitos-oid oppilaitos-oid
                                :oppija-oid     oppija-oid))

(defn- get-hoks
  "Get HOKS by ID, if user has sufficient permissions"
  [request]
  (if-let [hoks (:hoks request)]
    (let [virkailija-user (user/get request ::user/virkailija)]
      (if (user/has-read-privileges-to-oppija?! virkailija-user
                                                (:oppija-oid hoks))
        (restful/rest-ok (h/get-hoks-values hoks))
        (response/forbidden {:error (str "User has insufficient privileges")})))
    (response/not-found {:error "No HOKS found with given `id`."})))

(defn- change-hoks!
  "Change contents of HOKS with particular ID"
  [hoks request db-handler]
  (assoc-in
    (try
      (let [old-hoks (:hoks request)]
        (h/check-hoks-for-update! old-hoks hoks)
        (let [new-hoks (db-handler (:id (:hoks request))
                                  (h/add-missing-oht-yksiloiva-tunniste hoks))]
          (assoc (response/no-content) :audit-data {:old old-hoks
                                                    :new new-hoks})))
      (catch ExceptionInfo e
        (if (= (:error (ex-data e)) :disallowed-update)
          (do (log/warn e) (response/bad-request {:error (ex-message e)}))
          (throw e))))
    [:audit-data :target] {:hoks-id            (:id hoks)
                           :oppija-oid         (:oppija-oid hoks)
                           :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)}))

(defn- update-kyselylinkki-status!
  "Takes a `linkki-info` map, fetches the latest kyselylinkki status from Arvo
  and returns an updated `kyselylinkki-info` map. The functions *throws* if
  kyselytunnus status cannot be fetched from Arvo or if the database update
  fails for some reason."
  [linkki-info]
  (let [kyselylinkki (:kyselylinkki linkki-info)
        tunnus       (last (str/split kyselylinkki #"/"))
        status       (arvo/get-kyselytunnus-status! tunnus)
        loppupvm     (LocalDate/parse
                       (first (str/split (:voimassa_loppupvm status) #"T")))]
    (h/update-kyselylinkki! {:kyselylinkki kyselylinkki
                             :voimassa_loppupvm loppupvm
                             :vastattu (:vastattu status)})
    (assoc linkki-info :voimassa-loppupvm loppupvm
                       :vastattu (:vastattu status))))

(defn- get-vastaajatunnus-info
  "Get details of vastaajatunnus from database or Arvo. Can *throw* if the
  latest kyselylinkki status cannot be fetched from Arvo, if the database update
  fails or if opiskeluoikeus info cannot be fetched from Koski."
  [tunnus]
  ; FIXME: See EH-1641 in Jira -------------------.
  (assoc-in ;                                     v
    (if-let [linkki-info (some-> (pc/select-kyselylinkit-by-tunnus tunnus)
                                 first
                                 not-empty
                                 (apply-when #(not (:vastattu %))
                                             update-kyselylinkki-status!))]
      (-> (koski/get-opiskeluoikeus-info (:opiskeluoikeus-oid linkki-info))
          :koulutustoimija
          (select-keys [:oid :nimi])
          (rename-keys {:oid  :koulutustoimijan-oid
                        :nimi :koulutustoimijan-nimi})
          (->> (merge linkki-info))
          (dissoc :hoks-id)
          restful/rest-ok
          (assoc :audit-data
                 {:target {:hoks-id    (:hoks-id linkki-info)
                           :oppija-oid (:oppija-oid linkki-info)
                           :opiskeluoikeus-oid
                           (:opiskeluoikeus-oid linkki-info)}}))
      (response/not-found {:error "No survey link found with given `tunnus`"}))
    [:audit-data :target :tunnus] tunnus))

(defn- delete-vastaajatunnus
  "Delete vastaajatunnus from Arvo, database, and Herätepalvelu"
  [tunnus]
  ; FIXME: See EH-1641 in Jira
  (if-let [linkki-info (not-empty
                         (first (pc/select-kyselylinkit-by-tunnus tunnus)))]
    (try
        (arvo/delete-kyselytunnus tunnus)
        (pc/delete-kyselylinkki-by-tunnus tunnus)
        (sqs/send-delete-tunnus-message (:kyselylinkki linkki-info))
        (assoc (response/ok)
               :audit-data
               {:target {:hoks-id            (:hoks-id linkki-info)
                         :oppija-oid         (:oppija-oid linkki-info)
                         :opiskeluoikeus-oid (:opiskeluoikeus-oid linkki-info)}
                :old {:tunnus tunnus}})
      (catch ExceptionInfo e
        (if (and (= 404 (:status (ex-data e)))
                 (.contains ^String (:body (ex-data e))
                            "Tunnus ei ole poistettavissa"))
          (response/bad-request {:error "Survey ID cannot be removed"})
          (throw e))))
    (assoc (response/not-found
             {:error "No vastaajatunnus found with given `tunnus`."})
           :audit-data {:target {:tunnus tunnus}})))

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

(defn- paginate
  "Paginate the query `result` using `page-size` and `page-offset`"
  [result page-size page-offset]
  ; FIXME: Pagination is better handled in postgres with ORDER BY, LIMIT and
  ; OFFSET."
  (let [row-count-total  (count result)
        page-count-total (int (Math/ceil (/ row-count-total page-size)))
        start-row        (* page-size page-offset)
        end-row          (if (<= (+ start-row page-size) row-count-total)
                           (+ start-row page-size)
                           row-count-total)
        pageresult       (subvec (vec result) start-row end-row)]
    {:count row-count-total :pagecount page-count-total :result pageresult}))

(defn- ok-response-with-audit-data
  "Helper function used in a couple of handlers"
  [response-data]
  (assoc
    (restful/rest-ok response-data)
    :audit-data
    (let [result (:result response-data)]
      {:target
       {:hoks-id            (map #(get % "hoksId") result)
        :oppija-oid         (map #(get % "oppijaOid") result)
        :opiskeluoikeus-oid (map #(get % "opiskeluoikeusOid")
                                 result)}})))

(defn- get-tep-jakso-raportti-handler!
  "Handler for `GET /tep-jakso-raportti`"
  [request & {:keys [tutkinto oppilaitos start end page-size page-offset]}]
  ; FIXME: This handler does different things depending on type of user
  ; (super user vs. oppilaitos virkailija). This handler was only simplified
  ; as part of refactoring, but this obviously still needs to be fixed.
  (assoc-in
    (let [user (user/get request ::user/virkailija)]
      (cond
        (user/oph-super-user? user)
        (restful/rest-ok (pc/select-oht-by-tutkinto-between tutkinto start end))

        (nil? oppilaitos)
        (response/bad-request {:error "`oppilaitos` missing from request."})

        (user/has-privilege-in-oppilaitos? user :read oppilaitos)
        (-> (pc/get-oppilaitos-oids-cached-memoized! ;; 5min cache
              tutkinto oppilaitos start end)
            (paginate page-size page-offset)
            ok-response-with-audit-data)

        :else
        (response/forbidden
          {:error "User privileges do not match organisation"})))
        [:audit-data :target :oppilaitos-oid] oppilaitos))

(defn- get-hoksit-with-missing-opiskeluoikeus-handler!
  "Handler for `GET /missing-oo-hoksit/:oppilaitosoid`"
  [request oppilaitos-oid page-size page-offset]
  (assoc-in
    (if (user/has-privilege-in-oppilaitos? (user/get request ::user/virkailija)
                                           :read
                                           oppilaitos-oid)
      (-> (db-hoks/select-hoksit-by-oo-oppilaitos-and-koski404
            oppilaitos-oid)
          (paginate page-size page-offset)
          ok-response-with-audit-data)
      (response/forbidden
        {:error "User privileges does not match organisation"}))
      [:audit-data :target :oppilaitos-oid] oppilaitos-oid))

(defn- shallow-delete-hoks!
  [request hoks-id data]
  (let [hoks (h/get-hoks-values (:hoks request))
        opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
        oppilaitos-oid (or (:oppilaitos-oid data)
                           (:oppilaitos-oid (oi/get-opiskeluoikeus-by-oid!
                                              opiskeluoikeus-oid)))
        opiskeluoikeus (koski/get-opiskeluoikeus-info opiskeluoikeus-oid)]
    (cond
      (and (some? opiskeluoikeus)
           (not (oi/opiskeluoikeus-active? opiskeluoikeus)))
      (response/forbidden
        {:error (format "Opiskeluoikeus %s is no longer active"
                        opiskeluoikeus-oid)})

      (nil? oppilaitos-oid)
      (response/forbidden {:error (str "Oppilaitos-oid not found. Contact eHOKS"
                                       " support for more " "information.")})

      (not (contains? (user/organisation-privileges!
                        (user/get request ::user/virkailija)
                        oppilaitos-oid)
                      :hoks_delete))
      (response/forbidden {:error "User privileges do not match organisation"})

      :else (do (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
                (when (nil? opiskeluoikeus)
                  (db-hoks/delete-opiskeluoikeus-by-oid opiskeluoikeus-oid))
                (assoc (response/ok {:success hoks-id})
                       :audit-data {:old hoks
                                    :new (assoc hoks
                                                :deleted_at
                                                "*ADDED*")})))))

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
          [wrap-hoks wrap-audit-logger]

          validation-handler/routes

          (c-api/context "/virkailija" []
            :tags ["virkailija"]
            auth/routes

            (route-middleware
              [(wrap-require-user-type-and-auth ::user/virkailija)
               m/wrap-require-virkailija-user]

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
                :return {:count s/Int
                         :pagecount s/Int
                         :result [s/Any]}
                (get-tep-jakso-raportti-handler! request
                                                 {:tutkinto tutkinto
                                                  :oppilaitos oppilaitos
                                                  :start start
                                                  :end end
                                                  :page-size pagesize
                                                  :page-offset pageindex}))

              (c-api/GET "/vastaajatunnus/:tunnus" []
                :summary "Vastaajatunnuksen tiedot"
                :header-params [caller-id :- s/Str]
                :path-params [tunnus :- s/Str]
                :return s/Any
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
                :path-params   [oppilaitos-oid :- oid-schema/OrganisaatioOID]
                :query-params  [{pagesize :- s/Int 25}
                                {pageindex :- s/Int 0}]
                :return {:count s/Int
                         :pagecount s/Int
                         :result [s/Any]}
                (get-hoksit-with-missing-opiskeluoikeus-handler!
                  request oppilaitos-oid pagesize pageindex))

              ; Disabled: see EH-1230 and EH-1292.
              #_(c-api/GET "/paattyneet-kyselylinkit-temp" request
                :summary "Palauttaa tietoja kyselylinkkeihin liittyvistä
                          hokseista."
                :query-params [{alkupvm :- LocalDate (LocalDate/parse
                                                       "2021-09-01")}
                               {alkupvm-loppu :- LocalDate (LocalDate/now)}
                               {limit :- s/Int 2000}
                               {from-id :- s/Int 0}]
                :return {:last-id s/Int
                         :paattokysely-total-count s/Int
                         :o-s-pvm-ilman-vahvistuspvm-count s/Int
                         :vahvistuspvm-ilman-o-s-pvm-count s/Int
                         :vahvistuspvm-ja-o-s-pvm s/Int}
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

              ; This was meant to be temporary, disabled for now, see OY-3105.
              #_(c-api/GET "/puuttuvat-opiskeluoikeudet-temp" request
                :summary "Palauttaa listan hoks-id:sta, joiden opiskeluoikeutta
                ei löydy. Asettaa löydettyjen hoksien oo-indeksiin koski404 =
                true."
                :query-params [{limit :- s/Int 2000}
                               {from-id :- s/Int 0}]
                :return {:count s/Int
                         :ids [s/Int]
                         :last-id s/Int}
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
                            (oi/set-opiskeluoikeus-koski404 oo-oid))))
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

                  (c-api/context "/hoksit" []
                    (c-api/POST "/" [:as request]
                      :middleware [wrap-opiskeluoikeus]
                      :summary (str "Luo uuden HOKSin. "
                                    "Vaatii manuaalisyöttäjän oikeudet")
                      :body [hoks (hoks-schema/generate-hoks-schema
                                    "HOKSLuonti-virkailija" :post-virkailija
                                    "HOKS-dokumentin luonti")]
                      :return (restful/response schema/POSTResponse :id s/Int)
                      (post-hoks! hoks request))

                    (route-middleware
                      [m/wrap-virkailija-oppija-access]
                      (c-api/GET "/" []
                        :return (restful/response [hoks-schema/HOKS])
                        :summary "Oppijan hoksit (perustiedot)"
                        (get-hoks-perustiedot oppija-oid))

                      (c-api/GET "/oppilaitos/:oppilaitos-oid" request
                        :path-params
                        [oppilaitos-oid :- oid-schema/OrganisaatioOID]
                        :return (restful/response [hoks-schema/HOKS])
                        :summary "Oppijan hoksit (rajoitettu uusi versio)"
                        (let [virkailija-user (user/get request
                                                        ::user/virkailija)]
                          (if (user/has-privilege-in-oppilaitos? virkailija-user
                                                                 :read
                                                                 oppilaitos-oid)
                            (get-oppilaitoksen-oppijan-hoksit oppilaitos-oid
                                                              oppija-oid)
                            (assoc (response/forbidden
                                     {:error (str "User privileges does not "
                                                  "match organisation")})
                                   :audit-data
                                   {:target {:oppilaitos-oid oppilaitos-oid
                                             :oppija-oid     oppija-oid}}))))

                      (c-api/GET "/:hoks-id" request
                        :path-params [hoks-id :- s/Int]
                        :summary "Hoksin tiedot.
                                Vaatii manuaalisyöttäjän oikeudet"
                        :return hoks-schema/HOKS
                        (get-hoks request))

                      (c-api/POST "/:hoks-id/resend-palaute" request
                        :summary "Lähettää herätepalveluun pyynnön palautelinkin
                                  uudelleen lähetykselle"
                        :path-params [hoks-id :- s/Int]
                        :body [data hoks-schema/palaute-resend]
                        :return {:sahkoposti s/Str}
                        (let [kyselylinkit
                              (heratepalvelu/get-oppija-kyselylinkit
                                oppija-oid)
                              kyselylinkki (first
                                             (filter
                                               #(and (= (:hoks-id %1) hoks-id)
                                                     (= (:tyyppi %1)
                                                        (:tyyppi data)))
                                               kyselylinkit))
                              hoks (:hoks request)]
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
                        :return [s/Any]
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

                      (c-api/context "/:hoks-id" []
                        :path-params [hoks-id :- s/Int]

                        (route-middleware
                          [m/wrap-virkailija-write-access wrap-opiskeluoikeus]

                          (c-api/PUT "/" request
                            :summary
                            "Ylikirjoittaa olemassa olevan HOKSin arvon tai
                             arvot"
                            :body [hoks-values
                                   (hoks-schema/generate-hoks-schema
                                     "HOKSKorvaus-virkailija" :put-virkailija
                                     "HOKS-dokumentin korvaus")]
                            (change-hoks! hoks-values request h/replace-hoks!))

                          (c-api/PATCH "/" request
                            :body [hoks-values
                                   (hoks-schema/generate-hoks-schema
                                     "HOKSPaivitys-virkailija" :patch-virkailija
                                     "HOKS-dokumentin päivitys")]
                            :summary "Oppijan hoksin päätason arvojen päivitys"
                            (change-hoks! hoks-values request h/update-hoks!))))

                      (c-api/context "/:hoks-id" []
                        :path-params [hoks-id :- s/Int]

                        (c-api/PATCH "/shallow-delete" request
                          :summary "Asettaa HOKSin
                              poistetuksi (shallow delete) id:n perusteella."
                          :body [data hoks-schema/shallow-delete-hoks]
                          :return {:success s/Int}
                          (shallow-delete-hoks! request hoks-id data)))

                      (route-middleware
                        [m/wrap-oph-super-user]

                        (c-api/GET "/" []
                          :summary "Kaikki hoksit (perustiedot).
                        Tarvitsee OPH-pääkäyttäjän oikeudet"
                          :return [s/Any]
                          (restful/rest-ok (db-hoks/select-hoksit))))))

                  (route-middleware
                    [m/wrap-virkailija-oppija-access]

                    (c-api/GET "/opiskeluoikeudet" [:as request]
                      :summary "Oppijan opiskeluoikeudet"
                      :return (restful/response [s/Any])
                      (let [opiskeluoikeudet
                            (koski/get-oppija-opiskeluoikeudet oppija-oid)]
                        (assoc (restful/rest-ok opiskeluoikeudet)
                               :audit-data
                               {:target {:oppija-oid oppija-oid
                                         :opiskeluoikeus-oid
                                         (map :oid opiskeluoikeudet)}})))

                    (c-api/GET "/" []
                      :return (restful/response common-schema/Oppija)
                      :summary "Oppijan tiedot"
                      (if-let [oppija (oi/get-oppija-by-oid oppija-oid)]
                        (restful/rest-ok oppija)
                        (response/not-found)))

                    (c-api/GET "/with-oo" []
                      :return (restful/response common-schema/Oppija)
                      :summary "Oppijan tiedot. Opiskeluoikeus-oid lisättynä."
                      (if-let [oppija (oi/get-oppija-with-oo-oid-by-oid
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
