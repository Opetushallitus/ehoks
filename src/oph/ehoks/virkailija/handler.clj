(ns oph.ehoks.virkailija.handler
  (:require [clj-time.core :as t]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as string]
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
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.heratepalvelu.herate-handler :as herate-handler]
            [oph.ehoks.heratepalvelu.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.middleware :refer [wrap-hoks wrap-opiskeluoikeus]]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.resources :as resources]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.schema.oid :as oid-s]
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
                   oppilaitos-oid :- oid-s/OrganisaatioOID
                   {locale :- s/Str "fi"}]
    :summary "Listaa virkailijan oppilaitoksen oppijoiden opiskeluoikeudet,
             joilla on HOKS luotuna. Käyttäjällä pitää olla READ-käyttöoikeus
             annettuun organisaatioon eHOKS-palvelussa."

    (let [user       (get-in request [:session :virkailija-user])
          oppilaitos (organisaatio/get-existing-organisaatio! oppilaitos-oid)]
      (assoc
        (if-not (contains? (user/organisation-privileges oppilaitos user) :read)
          (do (log/warnf "User %s privileges does not match oppilaitos %s"
                         (:oidHenkilo user)
                         oppilaitos-oid)
              (response/forbidden
                {:error (str "User has insufficient privileges for given "
                             "organisation")}))
          (let [search-params        {:desc desc
                                      :item-count item-count
                                      :order-by-column order-by-column
                                      :offset (* page item-count)
                                      :oppilaitos-oid oppilaitos-oid
                                      :locale locale
                                      :nimi nimi
                                      :tutkinto tutkinto
                                      :osaamisala osaamisala
                                      :hoks-id hoks-id}
                [oppijat total-count] (oi/search! search-params)]
            (restful/ok oppijat :total-count total-count)))
        ::audit/target {:oppilaitos-oid oppilaitos-oid
                        :hoks-id hoks-id}))))

(defn- check-virkailija-privileges
  "Check whether virkailija user has write privileges in HOKS"
  [hoks request]
  (let [user (get-in request [:session :virkailija-user])]
    (when-not (user/has-privilege-to-hoks? hoks user :write)
      (log/warnf "User %s privileges don't match oppija %s"
                 (get-in request [:session
                                  :virkailija-user
                                  :oidHenkilo])
                 (:oppija-oid hoks))
      (response/forbidden!
        {:error
         (str "User has unsufficient privileges")}))))

(defn- post-hoks!
  "Add new HOKS for oppija"
  [hoks request]
  (oi/add-hoks-dependents-in-index! hoks)
  (check-virkailija-privileges hoks request)
  (let [hoks-db (-> (hoks/add-missing-oht-yksiloiva-tunniste hoks)
                    (assoc :manuaalisyotto true)
                    (hoks/check-and-save!))]
    (-> {:uri (format "%s/%d" (:uri request) (:id hoks-db))}
        (restful/ok :id (:id hoks-db))
        (assoc ::audit/changes {:new hoks}
               ::audit/target  (audit/hoks-target-data hoks-db)))))

(defn- any-hoks-has-active-opiskeluoikeus?
  "Check if any of the HOKSes has an active opiskeluoikeus"
  [hoksit]
  (some #(some-> (koski/get-opiskeluoikeus! %) opiskeluoikeus/active?)
        (map :opiskeluoikeus-oid hoksit)))

(defn- get-oppijan-hoksit-for-virkailija
  "Get basic information from HOKS"
  [oppija-oid ticket-user]
  (if-let [hoksit (db-hoks/select-hoks-by-oppija-oid oppija-oid)]
    (let [virkailijan-oppilaitosten-hoksit
          (filter #(user/has-privilege-to-hoks? % ticket-user :read)
                  hoksit)
          visible-hoksit (if (any-hoks-has-active-opiskeluoikeus?
                               virkailijan-oppilaitosten-hoksit)
                           hoksit
                           virkailijan-oppilaitosten-hoksit)]
      (assoc (restful/ok visible-hoksit)
             ::audit/target
             {:hoks-ids            (map :id visible-hoksit)
              :oppija-oid          (:oppija-oid (first visible-hoksit))
              :opiskeluoikeus-oids (map :opiskeluoikeus-oid visible-hoksit)}))
    (response/not-found {:message "HOKS not found"})))

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
  (let [hoksit (db-hoks/select-hoks-by-oppija-oid oppija-oid)]
    (if-let [hoksit-oppilaitoksessa (seq (get-hoksit-by-oppilaitos
                                           oppilaitos-oid hoksit))]
      ;; OY-2850: User should not be able to see HOKSes from other oppilaitoses
      ;; if users oppilaitos doesn't have any oppijas HOKSes with active OO.
      (restful/ok (if (any-hoks-has-active-opiskeluoikeus?
                        hoksit-oppilaitoksessa)
                    hoksit
                    hoksit-oppilaitoksessa))
      (response/not-found {:message "HOKS not found for oppilaitos"}))))

(defn- get-hoks
  "Get HOKS by ID, if user has sufficient permissions"
  [hoks-id request]
  (let [hoks (db-hoks/select-hoks-by-id hoks-id)
        virkailija-user (get-in
                          request
                          [:session :virkailija-user])]
    (if (user/has-read-privileges-to-oppija? virkailija-user (:oppija-oid hoks))
      (assoc (restful/ok (hoks/get-by-id hoks-id))
             ::audit/target (audit/hoks-target-data hoks))
      (do
        (log/warnf
          "User %s privileges don't match oppija %s"
          (get-in request [:session
                           :virkailija-user
                           :oidHenkilo])
          (get-in request [:params :oppija-oid]))
        (response/forbidden {:error "User has insufficient privileges"})))))

(defn- change-hoks!
  "Change contents of HOKS with particular ID"
  [hoks request db-handler]
  (let [old-hoks (if (= (:request-method request) :put)
                   (hoks/get-values (:hoks request))
                   (:hoks request))]
    (hoks/check-for-update! old-hoks hoks)
    (let [new-hoks (db-handler (:id (:hoks request))
                               (hoks/add-missing-oht-yksiloiva-tunniste hoks))]
      (hoks/handle-oppija-oid-changes-in-indexes! new-hoks old-hoks)
      (assoc (response/no-content)
             ::audit/changes {:old old-hoks :new new-hoks}))))

(defn- update-kyselylinkki-status!
  "Takes a `linkki-info` map, fetches the latest kyselylinkki status from Arvo
  and returns an updated `kyselylinkki-info` map. The functions *throws* if
  kyselytunnus status cannot be fetched from Arvo or if the database update
  fails for some reason."
  [linkki-info]
  (let [kyselylinkki (:kyselylinkki linkki-info)
        tunnus       (last (string/split kyselylinkki #"/"))
        status       (arvo/get-kyselytunnus-status! tunnus)
        loppupvm     (LocalDate/parse
                       (first (string/split (:voimassa_loppupvm status) #"T")))]
    (kyselylinkki/update! {:kyselylinkki kyselylinkki
                           :voimassa_loppupvm loppupvm
                           :vastattu (:vastattu status)})
    (assoc linkki-info
           :voimassa-loppupvm loppupvm
           :vastattu          (:vastattu status))))

(defn- get-vastaajatunnus-info!
  "Get details of vastaajatunnus from database or Arvo"
  [tunnus]
  (assoc
    (if-let [linkki-info (some-> (pc/select-kyselylinkit-by-tunnus tunnus)
                                 first
                                 not-empty
                                 (apply-when #(not (:vastattu %))
                                             update-kyselylinkki-status!))]
      (-> (koski/get-opiskeluoikeus! (:opiskeluoikeus-oid linkki-info))
          :koulutustoimija
          (select-keys [:oid :nimi])
          (rename-keys {:oid  :koulutustoimijan-oid
                        :nimi :koulutustoimijan-nimi})
          (->> (merge linkki-info))
          (dissoc :hoks-id)
          restful/ok)
      (response/not-found {:error "No survey link found with given `tunnus`"}))
    ::audit/target {:tunnus tunnus}))

(defn- delete-vastaajatunnus
  "Delete vastaajatunnus from Arvo, database, and Herätepalvelu"
  [tunnus]
  (try
    (let [linkit (pc/select-kyselylinkit-by-tunnus tunnus)]
      (arvo/delete-kyselytunnus tunnus)
      (pc/delete-kyselylinkki-by-tunnus tunnus)
      (sqs/send-delete-tunnus-message (:kyselylinkki (first linkit)))
      (assoc (response/ok)
             ::audit/changes {:old {:tunnus tunnus}}
             ::audit/target  {:tunnus tunnus}))
    (catch ExceptionInfo e
      (if (and (= 404 (:status (ex-data e)))
               (.contains ^String (:body (ex-data e))
                          "Tunnus ei ole poistettavissa"))
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

(defn- paginate
  "Paginate the query `result` using `page-size` and `page-offset`"
  [result page-size page-offset]
  (let [row-count-total  (count result)
        page-count-total (int (Math/ceil (/ row-count-total page-size)))
        start-row        (* page-size page-offset)
        end-row          (if (<= (+ start-row page-size) row-count-total)
                           (+ start-row page-size)
                           row-count-total)
        pageresult       (subvec (vec result) start-row end-row)]
    {:count row-count-total :pagecount page-count-total :result pageresult}))

(defn- get-tep-jakso-raportti-handler!
  "Handler for `GET /tep-jakso-raportti`"
  [request & {:keys [tutkinto oppilaitos-oid start end page-size page-offset]}]
  ; This handler does different things depending on type of user
  ; (super user vs. oppilaitos virkailija).
  (let [user       (get-in request [:session :virkailija-user])
        oppilaitos (organisaatio/get-organisaatio! oppilaitos-oid)]
    (assoc
      (cond
        (user/oph-super-user? user)
        (-> (pc/get-oht-by-tutkinto-between-memoized! tutkinto start end)
            (paginate page-size page-offset)
            restful/ok)

        (nil? oppilaitos)
        (response/bad-request {:error "`oppilaitos` missing from request."})

        (contains? (user/organisation-privileges oppilaitos user) :read)
        (-> (pc/get-oppilaitos-oids-cached-memoized! ;; 5min cache
              tutkinto oppilaitos-oid start end)
            (paginate page-size page-offset)
            restful/ok)

        :else (response/forbidden
                {:error "User privileges do not match organisation"}))
      ::audit/target {:tutkinto       tutkinto
                      :oppilaitos-oid oppilaitos
                      :start          start
                      :end            end})))

(defn- get-hoksit-with-missing-opiskeluoikeus-handler!
  "Handler for `GET /missing-oo-hoksit/:oppilaitosoid`"
  [request oppilaitos-oid page-size page-offset]
  (assoc
    (if (contains? (user/organisation-privileges
                     (organisaatio/get-existing-organisaatio! oppilaitos-oid)
                     (get-in request [:session :virkailija-user]))
                   :read)
      (-> (db-hoks/select-hoksit-by-oo-oppilaitos-and-koski404 oppilaitos-oid)
          (paginate page-size page-offset)
          restful/ok)
      (response/forbidden
        {:error "User privileges does not match organisation"}))
    ::audit/target {:oppilaitos-oid oppilaitos-oid}))

(defn- shallow-delete-hoks-handler!
  [request hoks-id data]
  (let [hoks               (db-hoks/select-hoks-by-id hoks-id)
        opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
        oppilaitos-oid     (or (:oppilaitos-oid data)
                               (:oppilaitos-oid (oi/get-opiskeluoikeus-by-oid!
                                                  opiskeluoikeus-oid)))
        opiskeluoikeus     (koski/get-opiskeluoikeus! opiskeluoikeus-oid)]
    (cond
      (and (some? opiskeluoikeus) (not (opiskeluoikeus/active? opiskeluoikeus)))
      (response/forbidden
        {:error (format "opiskeluoikeus %s is no longer active"
                        opiskeluoikeus-oid)})

      (nil? oppilaitos-oid)
      (response/forbidden {:error (str "Oppilaitos-oid not found. Contact eHOKS"
                                       " support for more " "information.")})

      (not (contains? (user/organisation-privileges
                        (organisaatio/get-existing-organisaatio! oppilaitos-oid)
                        (get-in request [:session :virkailija-user]))
                      :hoks_delete))
      (response/forbidden {:error "User privileges do not match organisation"})

      :else (do (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
                (when (nil? opiskeluoikeus)
                  (db-hoks/delete-opiskeluoikeus-by-oid opiskeluoikeus-oid))
                (assoc
                  (response/ok {:success hoks-id})
                  ::audit/changes {:old hoks
                                   :new (assoc hoks :deleted_at "*ADDED*")}
                  ::audit/target  (audit/hoks-target-data hoks))))))

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
          [wrap-hoks audit/wrap-logger]

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
                               oppilaitos :- (s/maybe oid-s/OrganisaatioOID)
                               start :- LocalDate
                               end :- LocalDate
                               {pagesize :- s/Int 25}
                               {pageindex :- s/Int 0}]
                :return (restful/response {:count s/Int
                                           :pagecount s/Int
                                           :result [s/Any]})
                (get-tep-jakso-raportti-handler! request
                                                 {:tutkinto tutkinto
                                                  :oppilaitos-oid oppilaitos
                                                  :start start
                                                  :end end
                                                  :page-size pagesize
                                                  :page-offset pageindex}))

              (c-api/GET "/vastaajatunnus/:tunnus" []
                :summary "Vastaajatunnuksen tiedot"
                :header-params [caller-id :- s/Str]
                :path-params [tunnus :- s/Str]
                :return s/Any
                (get-vastaajatunnus-info! tunnus))

              (c-api/DELETE "/vastaajatunnus/:tunnus" []
                :summary "Vastaajatunnuksen poisto"
                :header-params [caller-id :- s/Str]
                :path-params [tunnus :- s/Str]
                (delete-vastaajatunnus tunnus))

              (c-api/GET "/missing-oo-hoksit/:oppilaitos-oid" request
                :summary "Palauttaa listan hokseista, joiden
                          opiskeluoikeus puuttuu"
                :header-params [caller-id :- s/Str]
                :path-params [oppilaitos-oid :- oid-s/OrganisaatioOID]
                :query-params [{pagesize :- s/Int 25}
                               {pageindex :- s/Int 0}]
                :return (restful/response {:count s/Int
                                           :pagecount s/Int
                                           :result [s/Any]})
                (get-hoksit-with-missing-opiskeluoikeus-handler!
                  request oppilaitos-oid pagesize pageindex))

              (c-api/GET "/paattyneet-kyselylinkit-temp" request
                :summary "Palauttaa tietoja kyselylinkkeihin liittyvistä
                          hokseista."
                :query-params [{alkupvm :- LocalDate (LocalDate/parse
                                                       "2021-09-01")}
                               {alkupvm-loppu :- LocalDate (LocalDate/now)}
                               {limit :- s/Int 2000}
                               {from-id :- s/Int 0}]
                :return (restful/response
                          {:last-id s/Int
                           :paattokysely-total-count s/Int
                           :o-s-pvm-ilman-vahvistuspvm-count s/Int
                           :vahvistuspvm-ilman-o-s-pvm-count s/Int
                           :vahvistuspvm-ja-o-s-pvm s/Int})
                (let [data (db-hoks/select-kyselylinkit-by-date-and-type-temp
                             alkupvm alkupvm-loppu from-id limit)
                      last-id (:hoks-id (last data))
                      hoks-infos (map
                                   (fn [{ospvm :osaamisen-saavuttamisen-pvm
                                         oo-id :opiskeluoikeus-oid}]
                                     {:osaamisen-saavuttamisen-pvm ospvm
                                      :vahvistus-pvm
                                      (some-> (koski/get-opiskeluoikeus! oo-id)
                                              get-vahvistus-pvm)})
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
                                         hoks-infos))})))

              (c-api/GET "/puuttuvat-opiskeluoikeudet-temp" request
                :summary "Palauttaa listan hoks-id:sta, joiden opiskeluoikeutta
                ei löydy. Asettaa löydettyjen hoksien oo-indeksiin koski404 =
                true."
                :query-params [{limit :- s/Int 2000}
                               {from-id :- s/Int 0}]
                :return (restful/response {:count s/Int
                                           :ids [s/Int]
                                           :last-id s/Int})
                (let [hoksit (db-hoks/select-hokses-greater-than-id
                               from-id
                               limit
                               nil)
                      last-id (:id (last hoksit))]
                  (try
                    (let [hokses-without-oo
                          (filter
                            some?
                            (pmap (fn [hoks]
                                    (when (nil? (koski/get-opiskeluoikeus!
                                                  (:opiskeluoikeus-oid hoks)))
                                      hoks))
                                  hoksit))
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
                  :path-params [oppija-oid :- oid-s/OppijaOID]

                  (c-api/POST "/index" []
                    :summary "Indeksoi oppijan tiedot. DEPRECATED"
                    (response/gone {:message "Route is deprected."}))

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
                      (c-api/GET "/" [:as request]
                        :return (restful/response [hoks-schema/HOKS])
                        :summary "Oppijan hoksit (perustiedot)"
                        (get-oppijan-hoksit-for-virkailija
                          oppija-oid
                          (get-in request [:session :virkailija-user])))

                      (c-api/GET "/oppilaitos/:oppilaitos-oid" request
                        :path-params
                        [oppilaitos-oid :- oid-s/OrganisaatioOID]
                        :return (restful/response [hoks-schema/HOKS])
                        :summary "Oppijan hoksit (rajoitettu uusi versio)"
                        (let [user (get-in request [:session :virkailija-user])]
                          (assoc
                            (if (contains?
                                  (user/organisation-privileges
                                    (organisaatio/get-existing-organisaatio!
                                      oppilaitos-oid)
                                    user)
                                  :read)
                              (get-oppilaitoksen-oppijan-hoksit oppilaitos-oid
                                                                oppija-oid)
                              (response/forbidden
                                {:error (str "User privileges does not "
                                             "match organisation")}))
                            ::audit/target {:oppilaitos-oid oppilaitos-oid})))

                      (c-api/GET "/:hoks-id" request
                        :path-params [hoks-id :- s/Int]
                        :summary "Hoksin tiedot.
                                Vaatii manuaalisyöttäjän oikeudet"
                        :return (restful/response hoks-schema/HOKS)
                        (get-hoks hoks-id request))

                      (c-api/POST "/:hoks-id/resend-palaute" request
                        :summary "Lähettää herätepalveluun pyynnön palautelinkin
                                  uudelleen lähetykselle"
                        :path-params [hoks-id :- s/Int]
                        :body [data hoks-schema/palaute-resend]
                        :return (restful/response {:sahkoposti s/Str})
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
                            (kyselylinkki/update!
                              {:kyselylinkki (:kyselylinkki kyselylinkki)
                               :sahkoposti (:sahkoposti hoks)
                               :lahetyspvm (LocalDate/parse (str (t/today)))
                               :lahetystila "lahetetty"}))
                          (assoc
                            (restful/ok {:sahkoposti (:sahkoposti hoks)})
                            ::audit/operation ::heratepalvelu/resend-palaute)))

                      (c-api/GET "/:hoks-id/opiskelijapalaute" request
                        :summary "Palauttaa tietoja oppijan aktiivisista
                                  kyselylinkeistä (ilman kyselytunnuksia)"
                        :path-params [hoks-id :- s/Int]
                        :return [s/Any]
                        (let [kyselylinkit
                              (heratepalvelu/get-oppija-kyselylinkit oppija-oid)
                              lahetysdata
                              (map
                                #(dissoc %1 :kyselylinkki :vastattu)
                                (filter
                                  #(and
                                     (= (:hoks-id %1) hoks-id)
                                     (not (nil? (:lahetystila %1)))
                                     (not= "ei_lahetetty" (:lahetystila %1)))
                                  kyselylinkit))]
                          (restful/ok lahetysdata)))

                      (c-api/context "/:hoks-id" []
                        :path-params [hoks-id :- s/Int]

                        (route-middleware
                          [m/wrap-virkailija-write-access
                           wrap-opiskeluoikeus]

                          (c-api/PUT "/" request
                            :summary
                            "Ylikirjoittaa olemassa olevan HOKSin arvon tai
                             arvot"
                            :body [hoks-values
                                   (hoks-schema/generate-hoks-schema
                                     "HOKSKorvaus-virkailija" :put-virkailija
                                     "HOKS-dokumentin korvaus")]
                            (change-hoks! hoks-values request hoks/replace!))

                          (c-api/PATCH "/" request
                            :body [hoks-values
                                   (hoks-schema/generate-hoks-schema
                                     "HOKSPaivitys-virkailija" :patch-virkailija
                                     "HOKS-dokumentin päivitys")]
                            :summary "Oppijan hoksin päätason arvojen päivitys"
                            (change-hoks! hoks-values request hoks/update!))))

                      (c-api/context "/:hoks-id" []
                        :path-params [hoks-id :- s/Int]

                        (c-api/PATCH "/shallow-delete" request
                          :summary "Asettaa HOKSin
                              poistetuksi(shallow delete) id:n perusteella."
                          :body [data hoks-schema/shallow-delete-hoks]
                          :return (restful/response {:success s/Int})
                          (shallow-delete-hoks-handler! request hoks-id data)))

                      (route-middleware
                        [m/wrap-oph-super-user]

                        (c-api/GET "/" []
                          :summary "Kaikki hoksit (perustiedot).
                        Tarvitsee OPH-pääkäyttäjän oikeudet"
                          :return (restful/response [s/Any])
                          (restful/ok (db-hoks/select-hoksit))))))

                  (route-middleware
                    [m/wrap-virkailija-oppija-access]

                    (c-api/GET "/opiskeluoikeudet" [:as request]
                      :summary "Oppijan opiskeluoikeudet"
                      :return (restful/response [s/Any])
                      (restful/ok
                        (koski/get-oppija-opiskeluoikeudet oppija-oid)))

                    (c-api/GET "/" []
                      :return (restful/response common-schema/Oppija)
                      :summary "Oppijan tiedot"
                      (if-let [oppija (oi/get-oppija-by-oid oppija-oid)]
                        (restful/ok oppija)
                        (response/not-found)))

                    (c-api/GET "/with-oo" []
                      :return (restful/response common-schema/Oppija)
                      :summary "Oppijan tiedot. Opiskeluoikeus-oid lisättynä."
                      (if-let [oppija (oi/get-oppija-with-oo-oid-by-oid
                                        oppija-oid)]
                        (restful/ok oppija)
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
    {:coercion :custom-schema
     :swagger
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
