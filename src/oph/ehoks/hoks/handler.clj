(ns oph.ehoks.hoks.handler
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.postgresql.common :refer [select-kyselylinkki]]
            [oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat :as pdb-ot]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.hoks.vipunen-schema :as hoks-schema-vipunen]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.middleware :as mw]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.schema.oid :as oid-schema]
            [ring.util.http-response :as response]
            [schema.core :as s]))

(def ^:private hankittava-paikallinen-tutkinnon-osa
  "Hankittavan paikallisen tutkinnon osan reitit."
  (c-api/context "/hankittava-paikallinen-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" [:as request]
      :summary "Palauttaa HOKSin hankittavan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaPaikallinenTutkinnonOsa)
      (rest/ok
        (dissoc (ha/get-hankittava-paikallinen-tutkinnon-osa id) :hoks-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan paikallisen tutkinnon osan"
      :body [hpto hoks-schema/HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [hpto-db (ha/save-hankittava-paikallinen-tutkinnon-osa! hoks-id
                                                                   hpto)]
        (assoc (rest/ok {:uri (format "%s/%d" (:uri request) (:id hpto-db))}
                        :id (:id hpto-db))
               ::audit/changes {:new hpto})))

    (c-api/PATCH "/:id" [:as request]
      :summary
      "Päivittää HOKSin hankittavan paikallisen tutkinnon osan arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/HankittavaPaikallinenTutkinnonOsaPatch]
      (if-let [old-hpto (ha/get-hankittava-paikallinen-tutkinnon-osa id)]
        (do (ha/update-hankittava-paikallinen-tutkinnon-osa! old-hpto values)
            (assoc (response/no-content)
                   ::audit/changes
                   {:old old-hpto
                    :new (ha/get-hankittava-paikallinen-tutkinnon-osa id)}))
        (response/not-found
          {:error "Hankittava paikallinen tutkinnon osa not found"})))))

(def ^:private hankittava-ammat-tutkinnon-osa
  "Hankittavan ammatillisen tutkinnon osan reitit."
  (c-api/context "/hankittava-ammat-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" [:as request]
      :summary "Palauttaa HOKSin hankittavan ammatillisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaAmmatillinenTutkinnonOsa)
      (rest/ok (dissoc (ha/get-hankittava-ammat-tutkinnon-osa id) :hoks-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan ammatillisen osaamisen HOKSiin"
      :body
      [hato hoks-schema/HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [hato-db (ha/save-hankittava-ammat-tutkinnon-osa! hoks-id hato)]
        (assoc (rest/ok {:uri (format "%s/%d" (:uri request) (:id hato-db))}
                        :id (:id hato-db))
               ::audit/changes {:new hato})))

    (c-api/PATCH "/:id" [:as request]
      :summary
      "Päivittää HOKSin hankittavan ammatillisen tutkinnon osan arvoa ja arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HankittavaAmmatillinenTutkinnonOsaPatch]
      (if-let [old-hato (ha/get-hankittava-ammat-tutkinnon-osa id)]
        (do (ha/update-hankittava-ammat-tutkinnon-osa! old-hato values)
            (assoc (response/no-content)
                   ::audit/changes
                   {:old old-hato
                    :new (ha/get-hankittava-ammat-tutkinnon-osa id)}))
        (response/not-found
          {:error "Hankittava ammatillinen tutkinnon osa not found"})))))

(def ^:private hankittava-yhteinen-tutkinnon-osa
  "Hankittavan yhteisen tutkinnon osan reitit."
  (c-api/context "/hankittava-yhteinen-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin hankittavan yhteisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaYhteinenTutkinnonOsa)
      (rest/ok (dissoc (ha/get-hankittava-yhteinen-tutkinnon-osa id) :hoks-id)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) hankittavan yhteisen tutkinnon osat HOKSiin"
      :body [hyto hoks-schema/HankittavaYhteinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [hyto-db (ha/save-hankittava-yhteinen-tutkinnon-osa! hoks-id hyto)]
        (assoc (rest/ok {:uri (format "%s/%d" (:uri request) (:id hyto-db))}
                        :id (:id hyto-db))
               ::audit/changes {:new hyto})))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin hankittavan yhteisen tutkinnon osat arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HankittavaYhteinenTutkinnonOsaPatch]
      (if-let [old-hyto (ha/get-hankittava-yhteinen-tutkinnon-osa id)]
        (do (ha/update-hankittava-yhteinen-tutkinnon-osa!
              (:hoks-id old-hyto) id values)
            (assoc (response/no-content)
                   ::audit/changes
                   {:old old-hyto
                    :new (ha/get-hankittava-yhteinen-tutkinnon-osa id)}))
        (response/not-found {:error "HYTO not found with given HYTO ID"})))))

(def ^:private aiemmin-hankittu-ammat-tutkinnon-osa
  "Aiemmin hankitun ammatillisen tutkinnon osan reitit."
  (c-api/context "/aiemmin-hankittu-ammat-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin aiemmin hankitun ammatillisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsa)
      (rest/ok (dissoc (ah/get-aiemmin-hankittu-ammat-tutkinnon-osa id)
                       :tarkentavat-tiedot-osaamisen-arvioija-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun ammat tutkinnon osan HOKSiin"
      :body
      [ahato
       hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ahato-db (ah/save-aiemmin-hankittu-ammat-tutkinnon-osa!
                       hoks-id ahato)]
        (assoc (rest/ok {:uri (format "%s/%d" (:uri request) (:id ahato-db))}
                        :id (:id ahato-db))
               ::audit/changes {:new ahato})))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun ammatillisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body [values hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaPatch]
      (if-let [old-ahato (ah/get-aiemmin-hankittu-ammat-tutkinnon-osa id)]
        (do (ah/update-aiemmin-hankittu-ammat-tutkinnon-osa! old-ahato values)
            (assoc (response/no-content)
                   ::audit/changes
                   {:old old-ahato
                    :new (ah/get-aiemmin-hankittu-ammat-tutkinnon-osa id)}))
        (response/not-found
          {:error "Olemassa oleva ammatillinen tutkinnon osa not found"})))))

(def ^:private aiemmin-hankittu-paikallinen-tutkinnon-osa
  "Aiemmin hankitun paikallisen tutkinnon osan reitit."
  (c-api/context "/aiemmin-hankittu-paikallinen-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin olemassa olevan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/AiemminHankittuPaikallinenTutkinnonOsa)
      (rest/ok (dissoc (ah/get-aiemmin-hankittu-paikallinen-tutkinnon-osa id)
                       :tarkentavat-tiedot-osaamisen-arvioija-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo olemassa olevan paikallisen tutkinnon osan HOKSiin"
      :body
      [hpto hoks-schema/AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ahpto-db (ah/save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
                       hoks-id hpto)]
        (assoc (rest/ok {:uri (format "%s/%d" (:uri request) (:id ahpto-db))}
                        :id (:id ahpto-db))
               ::audit/changes {:new hpto})))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun paikallisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/AiemminHankittuPaikallinenTutkinnonOsaPatch]
      (if-let [old-ahpto (ah/get-aiemmin-hankittu-paikallinen-tutkinnon-osa id)]
        (do (ah/update-aiemmin-hankittu-paikallinen-tutkinnon-osa! old-ahpto
                                                                   values)
            (assoc
              (response/no-content)
              ::audit/changes
              {:old old-ahpto
               :new (ah/get-aiemmin-hankittu-paikallinen-tutkinnon-osa id)}))
        (response/not-found
          {:error "Aiemmin hankittu paikallinen tutkinnon osa not found"})))))

(def ^:private aiemmin-hankittu-yhteinen-tutkinnon-osa
  "Aiemmin hankitun yhteisen tutkinnon osan reitit."
  (c-api/context "/aiemmin-hankittu-yhteinen-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin aiemmin hankitun yhteisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/AiemminHankittuYhteinenTutkinnonOsa)
      (rest/ok (dissoc (ah/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)
                       :tarkentavat-tiedot-osaamisen-arvioija-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun yhteisen tutkinnon osan HOKSiin"
      :body
      [ahyto hoks-schema/AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ahyto-db (ah/save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
                       (get-in request [:hoks :id]) ahyto)]
        (assoc (rest/ok {:uri (format "%s/%d" (:uri request) (:id ahyto-db))}
                        :id (:id ahyto-db))
               ::audit/changes {:new ahyto})))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun yhteisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body [values hoks-schema/AiemminHankittuYhteinenTutkinnonOsaPatch]
      (if-let [old-ahyto (ah/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)]
        (do (ah/update-aiemmin-hankittu-yhteinen-tutkinnon-osa! old-ahyto
                                                                values)
            (assoc (response/no-content)
                   ::audit/changes
                   {:old old-ahyto
                    :new (ah/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)}))
        (response/not-found
          {:error "Aiemmin hankitun yhteinen tutkinnon osa not found"})))))

(def ^:private opiskeluvalmiuksia-tukevat-opinnot
  "Opiskeluvalimuksia tukevien opintojen reitit."
  (c-api/context "/opiskeluvalmiuksia-tukevat-opinnot" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin opiskeluvalmiuksia tukevat opinnot"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot)
      (rest/ok (ot/get-opiskeluvalmiuksia-tukeva-opinto id)))

    (c-api/POST "/"  [:as request]
      :summary
      "Luo (tai korvaa vanhan) opiskeluvalmiuksia tukevat opinnot HOKSiin"
      :body [oto hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [oto-db (ot/save-opiskeluvalmiuksia-tukeva-opinto! hoks-id oto)]
        (assoc (rest/ok {:uri (format "%s/%d" (:uri request) (:id oto-db))}
                        :id (:id oto-db))
               ::audit/changes {:new oto})))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin opiskeluvalmiuksia tukevat opintojen arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotPatch]
      (if-let [oto-db
               (pdb-ot/select-opiskeluvalmiuksia-tukevat-opinnot-by-id id)]
        (do (pdb-ot/update-opiskeluvalmiuksia-tukevat-opinnot-by-id! id values)
            (assoc
              (response/no-content)
              ::audit/changes
              {:old oto-db
               :new
               (pdb-ot/select-opiskeluvalmiuksia-tukevat-opinnot-by-id id)}))
        (response/not-found {:error "OTO not found with given OTO ID"})))))

(def ^:private tuva-hoks-msg-template
  "HOKS `%s` is a TUVA-HOKS or rinnakkainen ammatillinen HOKS.")

(defn- initiate-all-palautteet!
  [{:keys [hoks] :as ctx}]
  (if (hoks/tuva-related? hoks)
    (db-hoks/set-amisherate-kasittelytilat-to-true!
      (:id hoks) (format tuva-hoks-msg-template (:id hoks)))
    (try
      (op/initiate-if-needed! ctx :aloituskysely)
      (op/initiate-if-needed! ctx :paattokysely)
      (tep/initiate-all-uninitiated! ctx)
      (catch clojure.lang.ExceptionInfo e
        (if (= :organisaatio/organisation-not-found (:type (ex-data e)))
          (throw (ex-info (str "HOKS contains an unknown organisation"
                               (:organisation-oid (ex-data e)))
                          (assoc (ex-data e) :type ::disallowed-update)))
          (log/error e "exception in heräte initiation with" (ex-data e))))
      (catch Exception e
        (log/error e "exception in heräte initiation")))))

(defn save-hoks-and-initiate-all-palautteet!
  "Saves a HOKS to DB and initializes all palautteet (opiskelija & tyoelama)
  that need to be initialized."
  [{:keys [hoks] :as ctx}]
  (jdbc/with-db-transaction
    [tx db/spec]
    (let [hoks (assoc hoks :id (:id (hoks/save! hoks)))]
      (initiate-all-palautteet! (assoc ctx :hoks hoks :tx tx))
      hoks)))

(defn change-hoks-and-initiate-all-palautteet!
  "Updates a HOKS in DB with `db-handler` (either update or replace) and
  initializes all palautteet (opiskelija & tyoelama) that need to be
  initialized."
  [{:keys [hoks] :as ctx} db-handler]
  (jdbc/with-db-transaction
    [tx db/spec]
    (let [updated-hoks (db-handler hoks)]
      (initiate-all-palautteet! (assoc ctx :hoks updated-hoks :tx tx))
      updated-hoks)))

(defn post-hoks!
  "Käsittelee HOKS-luontipyynnön."
  [{:keys [request hoks opiskeluoikeus] :as ctx} check-privileges]
  (oppijaindex/add-hoks-dependents-in-index! hoks)
  (check-privileges hoks request)
  (hoks/check hoks opiskeluoikeus)
  (let [hoks (save-hoks-and-initiate-all-palautteet! ctx)]
    (-> {:uri (format "%s/%d" (:uri request) (:id hoks))}
        (rest/ok :id (:id hoks))
        (assoc ::audit/changes {:new hoks}
               ::audit/target  (audit/hoks-target-data hoks)))))

(defn change-hoks!
  "Käsittelee HOKS-muutospyynnön."
  [{:keys [request hoks opiskeluoikeus] :as ctx} db-handler]
  (let [old-hoks (if (= (:request-method request) :put)
                   (hoks/get-values (:hoks request))
                   (:hoks request))]
    (if (empty? old-hoks)
      (response/not-found {:error "HOKS not found with given HOKS ID"})
      (do (hoks/check-for-update! old-hoks hoks opiskeluoikeus)
          (let [new-hoks (change-hoks-and-initiate-all-palautteet!
                           ctx db-handler)]
            (hoks/handle-oppija-oid-changes-in-indexes! new-hoks old-hoks)
            (assoc (response/no-content)
                   ::audit/changes {:old old-hoks :new new-hoks}))))))

(def routes
  "HOKS handlerin reitit."
  (c-api/context "/hoks" []
    :tags ["hoks"]
    :header-params [ticket :- s/Str
                    caller-id :- s/Str]

    (route-middleware
      [mw/wrap-user-details
       m/wrap-require-service-user
       mw/wrap-hoks
       audit/wrap-logger]

      (c-api/GET "/opiskeluoikeus/:opiskeluoikeus-oid" request
        :summary "Palauttaa HOKSin opiskeluoikeuden oidilla"
        :path-params [opiskeluoikeus-oid :- oid-schema/OpiskeluoikeusOID]
        :return (rest/response hoks-schema/HOKS)
        (assoc-in
          (if-let [hoks (first (db-hoks/select-hoksit-by-opiskeluoikeus-oid
                                 opiskeluoikeus-oid))]
            (do (m/check-hoks-access! hoks request)
                (assoc (rest/ok hoks)
                       ::audit/target (audit/hoks-target-data hoks)))
            (do (log/warn "No HOKS found with given opiskeluoikeus "
                          opiskeluoikeus-oid)
                (response/not-found
                  {:error "No HOKS found with given opiskeluoikeus"})))
          [::audit/target :opiskeluoikeus-oid] opiskeluoikeus-oid))

      (c-api/GET "/paged" request
        :summary "Palauttaa halutun määrän annetusta id:stä seuraavia
                 hokseja kasvavassa id-järjestyksessä.
                 Seuraavan sivun saa antamalla from-id -parametriksi suurimman
                 jo saadun hoksin id:n kunnes palautuu tyhjä tulosjoukko.
                 Vähintään 1, enintään 1000, oletus 500 kerralla.
                 Kaikki hoksit saa haettua aloittamalla from-id:llä 0
                 ja kutsumalla rajapintaa toistuvasti edellisestä vastauksesta
                 poimitulla last-id:llä kunnes sekä result- että
                 failed-ids-kentät ovat tyhjiä.
                 Updated-after parametrin lisäämällä endpoint palauttaa
                 hoksit, joita on muutettu annetun päivämäärän
                 (esim. 2021-01-20T12:55:02) jälkeen."
        :query-params [{amount :- s/Int 500}
                       {from-id :- s/Int 0}
                       {updated-after :- s/Inst nil}]
        :return (rest/response {:last-id s/Int
                                :failed-ids [s/Int]
                                :result
                                [hoks-schema-vipunen/HOKSVipunen]})
        (let [limit (min (max 1 amount) 1000)
              raw-result (hoks/get-starting-from-id! from-id
                                                     limit
                                                     updated-after)
              result (map hoks/mark-as-deleted raw-result)
              last-id (first (sort > (map :id result)))
              schema-checker (s/checker hoks-schema-vipunen/HOKSVipunen)
              result-after-validation (filter
                                        (fn [hoks] (nil? (schema-checker hoks)))
                                        result)
              failed-ids (seq (clojure.set/difference
                                (set (map :id result))
                                (set (map :id result-after-validation))))]
          (when (not-empty failed-ids)
            (log/info "Failed ids for paged call:" failed-ids
                      "params" {:from-id from-id :amount amount}))
          (assoc (rest/ok {:last-id (or last-id from-id)
                           :failed-ids (sort failed-ids)
                           :result result-after-validation})
                 ::audit/target {:from-id from-id
                                 :amount  amount
                                 :updated-after updated-after})))

      (route-middleware
        [m/wrap-require-oph-privileges]

        (c-api/GET "/osaamisen-hankkimistapa/:oht-id" request
          :summary "Palauttaa osaamisen hankkimistavan ID:llä"
          :path-params [oht-id :- s/Int]
          :return (rest/response hoks-schema/OsaamisenHankkimistapa)
          (assoc
            (if-let [oht (ha/get-osaamisen-hankkimistapa-by-id oht-id)]
              (rest/ok oht)
              (do
                (log/warn "No osaamisen hankkimistapa found with ID: " oht-id)
                (response/not-found
                  {:error "No osaamisen hankkimistapa found with given ID"})))
            ::audit/target {:oht-id oht-id}))

        (c-api/PATCH "/kyselylinkki" request
          :summary "Lisää lähetystietoja kyselylinkille"
          :body [data hoks-schema/kyselylinkki-lahetys]
          (assoc
            (if-let [old-data (select-kyselylinkki (:kyselylinkki data))]
              (do (kyselylinkki/update! data)
                  (assoc (response/no-content)
                         ::audit/changes
                         {:old old-data
                          :new (select-kyselylinkki (:kyselylinkki data))}))
              (response/not-found {:error "No kyselylinkki found"}))
            ::audit/target {:kyselylinkki (:kyselylinkki data)})))

      (c-api/POST "/" [:as request]
        :middleware [mw/wrap-opiskeluoikeus]
        :summary "Luo uuden HOKSin"
        :body [hoks hoks-schema/HOKSLuonti]
        :return (rest/response schema/POSTResponse :id s/Int)
        (post-hoks! {:request        request
                     :hoks           hoks
                     :opiskeluoikeus (mw/get-current-opiskeluoikeus)}
                    m/check-hoks-access!))

      (c-api/GET "/:hoks-id" request
        :middleware [m/wrap-hoks-access!]
        :summary "Palauttaa HOKSin"
        :return (rest/response hoks-schema/HOKS)
        (rest/ok (hoks/get-values (:hoks request))))

      (c-api/context "/:hoks-id" []
        (route-middleware
          [m/wrap-hoks-access! mw/wrap-opiskeluoikeus]

          (c-api/PATCH "/" request
            :summary
            "Päivittää olemassa olevan HOKSin ylätason arvoa tai arvoja"
            :body [hoks hoks-schema/HOKSPaivitys]
            (change-hoks! {:request        request
                           :hoks           hoks
                           :opiskeluoikeus (mw/get-current-opiskeluoikeus)}
                          hoks/update!))

          (c-api/PUT "/" request
            :summary "Ylikirjoittaa olemassa olevan HOKSin arvon tai arvot"
            :body [hoks hoks-schema/HOKSKorvaus]
            (change-hoks! {:request        request
                           :hoks           hoks
                           :opiskeluoikeus (mw/get-current-opiskeluoikeus)}
                          hoks/replace!))

          (c-api/GET "/hankintakoulutukset" request
            :summary "Palauttaa hoksin hankintakoulutus opiskeluoikeus-oidit"
            (let [oids (oppijaindex/get-hankintakoulutus-oids-by-master-oid
                         (get-in request [:hoks :opiskeluoikeus-oid]))]
              (response/ok oids)))

          aiemmin-hankittu-ammat-tutkinnon-osa
          aiemmin-hankittu-paikallinen-tutkinnon-osa
          aiemmin-hankittu-yhteinen-tutkinnon-osa
          hankittava-ammat-tutkinnon-osa
          hankittava-paikallinen-tutkinnon-osa
          hankittava-yhteinen-tutkinnon-osa
          opiskeluvalmiuksia-tukevat-opinnot

          (route-middleware
            [m/wrap-require-oph-privileges]

            (c-api/POST "/kyselylinkki" request
              :summary "Lisää kyselylinkin hoksille"
              :body [data hoks-schema/kyselylinkki]
              (if (not-empty (:hoks request))
                (let [enriched-data
                      (assoc
                        data
                        :oppija-oid (get-in request [:hoks :oppija-oid])
                        :hoks-id (get-in request [:hoks :id]))]
                  (kyselylinkki/insert! enriched-data)
                  (assoc (response/no-content)
                         ::audit/changes {:new enriched-data}))
                (response/not-found
                  {:error "HOKS not found with given HOKS ID"})))))))))
