(ns oph.ehoks.hoks.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.postgresql.common :refer [select-kyselylinkki]]
            [oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat :as pdb-ot]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.hoks.vipunen-schema :as hoks-schema-vipunen]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.middleware :refer [wrap-hoks wrap-opiskeluoikeus
                                          wrap-user-details]]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.schema :as schema]
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
      (rest/rest-ok
        (dissoc (ha/get-hankittava-paikallinen-tutkinnon-osa id) :hoks-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan paikallisen tutkinnon osan"
      :body [hpto hoks-schema/HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [hpto-db (ha/save-hankittava-paikallinen-tutkinnon-osa! hoks-id
                                                                   hpto)]
        (assoc
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id hpto-db))}
                        :id (:id hpto-db))
          :audit-data {:new hpto})))
    (c-api/PATCH "/:id" [:as request]
      :summary
      "Päivittää HOKSin hankittavan paikallisen tutkinnon osan arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/HankittavaPaikallinenTutkinnonOsaPatch]
      (if-let [old-hpto (ha/get-hankittava-paikallinen-tutkinnon-osa id)]
        (do (ha/update-hankittava-paikallinen-tutkinnon-osa! old-hpto values)
            (assoc (response/no-content)
                   :audit-data
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
      (rest/rest-ok
        (dissoc (ha/get-hankittava-ammat-tutkinnon-osa id) :hoks-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan ammatillisen osaamisen HOKSiin"
      :body
      [hato hoks-schema/HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [hato-db (ha/save-hankittava-ammat-tutkinnon-osa! hoks-id hato)]
        (assoc
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id hato-db))}
                        :id (:id hato-db))
          :audit-data {:new hato})))

    (c-api/PATCH "/:id" [:as request]
      :summary
      "Päivittää HOKSin hankittavan ammatillisen tutkinnon osan arvoa ja arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HankittavaAmmatillinenTutkinnonOsaPatch]
      (if-let [old-hato (ha/get-hankittava-ammat-tutkinnon-osa id)]
        (do (ha/update-hankittava-ammat-tutkinnon-osa! old-hato values)
            (assoc (response/no-content)
                   :audit-data
                   {:old old-hato
                    :new (ha/get-hankittava-ammat-tutkinnon-osa id)}))
        (response/not-found
          {:error "Hankittava ammatillinen tutkinnon osa not found"})))))

(def ^:private hankittava-yhteinen-tutkinnon-osa
  "Hankittavan yhteisen tutkinnon osan reitit."
  (c-api/context "/hankittava-yhteinen-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin hankittavan yhteisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaYhteinenTutkinnonOsa)
      (rest/rest-ok
        (dissoc (ha/get-hankittava-yhteinen-tutkinnon-osa id) :hoks-id)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) hankittavan yhteisen tutkinnon osat HOKSiin"
      :body [hyto hoks-schema/HankittavaYhteinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [hyto-db (ha/save-hankittava-yhteinen-tutkinnon-osa!
                      (get-in request [:hoks :id]) hyto)]
        (assoc
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id hyto-db))}
                        :id (:id hyto-db))
          :audit-data {:new hyto})))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin hankittavan yhteisen tutkinnon osat arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HankittavaYhteinenTutkinnonOsaPatch]
      (if-let [old-hyto (ha/get-hankittava-yhteinen-tutkinnon-osa id)]
        (do (ha/update-hankittava-yhteinen-tutkinnon-osa!
              (:hoks-id old-hyto) id values)
            (assoc (response/no-content)
                   :audit-data
                   {:old old-hyto
                    :new (ha/get-hankittava-yhteinen-tutkinnon-osa id)}))
        (response/not-found {:error "HYTO not found with given HYTO ID"})))))

(def ^:private aiemmin-hankittu-ammat-tutkinnon-osa
  "Aiemmin hankitun ammatillisen tutkinnon osan reitit."
  (c-api/context "/aiemmin-hankittu-ammat-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin aiemmin hankitun ammatillisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsa)
      (rest/rest-ok (dissoc (ah/get-aiemmin-hankittu-ammat-tutkinnon-osa id)
                            :tarkentavat-tiedot-osaamisen-arvioija-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun ammat tutkinnon osan HOKSiin"
      :body
      [ahato
       hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ahato-db (ah/save-aiemmin-hankittu-ammat-tutkinnon-osa!
                       (get-in request [:hoks :id]) ahato)]
        (assoc
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id ahato-db))}
                        :id (:id ahato-db))
          :audit-data {:new ahato})))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun ammatillisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body [values hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaPatch]
      (if-let [old-ahato (ah/get-aiemmin-hankittu-ammat-tutkinnon-osa id)]
        (do (ah/update-aiemmin-hankittu-ammat-tutkinnon-osa! old-ahato values)
            (assoc (response/no-content)
                   :audit-data
                   {:old old-ahato
                    :new (ah/get-aiemmin-hankittu-ammat-tutkinnon-osa id)}))
        (response/not-found
          {:error "Olemassa oleva ammatillinen tutkinnon osa not found"})))))

(def ^:private aiemmin-hankittu-paikallinen-tutkinnon-osa
  "Aiemmin hankitun paikallisen tutkinnon osan reitit."
  (c-api/context "/aiemmin-hankittu-paikallinen-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin olemassa olevan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/AiemminHankittuPaikallinenTutkinnonOsa)
      (rest/rest-ok
        (dissoc (ah/get-aiemmin-hankittu-paikallinen-tutkinnon-osa id)
                :tarkentavat-tiedot-osaamisen-arvioija-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo olemassa olevan paikallisen tutkinnon osan HOKSiin"
      :body
      [hpto hoks-schema/AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ahpto-db (ah/save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
                       (get-in request [:hoks :id]) hpto)]
        (assoc
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id ahpto-db))}
                        :id (:id ahpto-db))
          :audit-data {:new hpto})))

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
              :audit-data
              {:old old-ahpto
               :new (ah/get-aiemmin-hankittu-paikallinen-tutkinnon-osa id)}))
        (response/not-found
          {:error "Aiemmin hankittu paikallinen tutkinnon osa not found"})))))

(def ^:private aiemmin-hankittu-yhteinen-tutkinnon-osa
  "Aiemmin hankitun yhteisen tutkinnon osan reitit."
  (c-api/context "/aiemmin-hankittu-yhteinen-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin aiemmin hankitun yhteisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/AiemminHankittuYhteinenTutkinnonOsa)
      (rest/rest-ok
        (dissoc (ah/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)
                :tarkentavat-tiedot-osaamisen-arvioija-id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun yhteisen tutkinnon osan HOKSiin"
      :body
      [ahyto hoks-schema/AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ahyto-db (ah/save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
                       (get-in request [:hoks :id]) ahyto)]
        (assoc
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id ahyto-db))}
                        :id (:id ahyto-db))
          :audit-data {:new ahyto})))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun yhteisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body [values hoks-schema/AiemminHankittuYhteinenTutkinnonOsaPatch]
      (if-let [old-ahyto (ah/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)]
        (do (ah/update-aiemmin-hankittu-yhteinen-tutkinnon-osa! old-ahyto
                                                                values)
            (assoc (response/no-content)
                   :audit-data
                   {:old old-ahyto
                    :new (ah/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)}))
        (response/not-found
          {:error "Aiemmin hankitun yhteinen tutkinnon osa not found"})))))

(def ^:private opiskeluvalmiuksia-tukevat-opinnot
  "Opiskeluvalimuksia tukevien opintojen reitit."
  (c-api/context "/opiskeluvalmiuksia-tukevat-opinnot" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin opiskeluvalmiuksia tukevat opinnot"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot)
      (rest/rest-ok (ot/get-opiskeluvalmiuksia-tukeva-opinto id)))

    (c-api/POST "/"  [:as request]
      :summary
      "Luo (tai korvaa vanhan) opiskeluvalmiuksia tukevat opinnot HOKSiin"
      :body [oto hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotLuontiJaMuokkaus]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [oto-response (ot/save-opiskeluvalmiuksia-tukeva-opinto!
                           (get-in request [:hoks :id]) oto)]
        (assoc (rest/rest-ok {:uri (format "%s/%d"
                                           (:uri request)
                                           (:id oto-response))}
                             :id (:id oto-response))
               :audit-data {:new oto})))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin opiskeluvalmiuksia tukevat opintojen arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotPatch]
      (if-let [oto-from-db
               (pdb-ot/select-opiskeluvalmiuksia-tukevat-opinnot-by-id id)]
        (do (pdb-ot/update-opiskeluvalmiuksia-tukevat-opinnot-by-id! id values)
            (assoc
              (response/no-content)
              :audit-data
              {:old oto-from-db
               :new
               (pdb-ot/select-opiskeluvalmiuksia-tukevat-opinnot-by-id id)}))
        (response/not-found {:error "OTO not found with given OTO ID"})))))

(defn- post-hoks!
  "Käsittelee HOKS-luontipyynnön."
  [hoks request]
  (try
    (oppijaindex/add-hoks-dependents-in-index! hoks)
    (m/check-hoks-access! hoks request)
    (let [hoks-db (h/check-and-save-hoks! hoks)
          resp-body {:uri (format "%s/%d" (:uri request) (:id hoks-db))}]
      (-> resp-body
          (rest/rest-ok :id (:id hoks-db))
          (assoc :audit-data {:new hoks})))
    (catch Exception e
      (case (:error (ex-data e))
        :disallowed-update (response/bad-request! {:error (.getMessage e)})
        :duplicate (do (log/warnf
                         "HOKS with opiskeluoikeus-oid %s already exists"
                         (:opiskeluoikeus-oid hoks))
                       (response/bad-request! {:error (.getMessage e)}))
        (throw e)))))

(defn- change-hoks!
  "Käsittelee HOKS-muutospyynnön."
  [hoks request db-handler]
  (let [old-hoks (:hoks request)]
    (if (empty? old-hoks)
      (response/not-found {:error "HOKS not found with given HOKS ID"})
      (try
        (h/check-hoks-for-update! old-hoks hoks)
        (let [new-hoks (db-handler (get-in request [:hoks :id]) hoks)]
          (assoc (response/no-content) :audit-data {:old old-hoks
                                                    :new new-hoks}))
        (catch Exception e
          (if (= (:error (ex-data e)) :disallowed-update)
            (response/bad-request! {:error (.getMessage e)})
            (throw e)))))))

(def routes
  "HOKS handlerin reitit."
  (c-api/context "/hoks" []
    :tags ["hoks"]
    :header-params [ticket :- s/Str
                    caller-id :- s/Str]

    (route-middleware
      [wrap-user-details m/wrap-require-service-user wrap-audit-logger]

      (c-api/GET "/opiskeluoikeus/:opiskeluoikeus-oid" request
        :summary "Palauttaa HOKSin opiskeluoikeuden oidilla"
        :path-params [opiskeluoikeus-oid :- s/Str]
        :return (rest/response hoks-schema/HOKS)
        (let [hoks (first (db-hoks/select-hoksit-by-opiskeluoikeus-oid
                            opiskeluoikeus-oid))]
          (if hoks
            (do
              (m/check-hoks-access! hoks request)
              (rest/rest-ok hoks))
            (do
              (log/warn "No HOKS found with given opiskeluoikeus "
                        opiskeluoikeus-oid)
              (response/not-found
                {:error "No HOKS found with given opiskeluoikeus"})))))

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
              raw-result (h/get-hokses-from-id from-id limit updated-after)
              result (map h/mark-as-deleted raw-result)
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
          (rest/rest-ok {:last-id (or last-id from-id)
                         :failed-ids (sort failed-ids)
                         :result result-after-validation})))

      (route-middleware
        [m/wrap-require-oph-privileges]

        (c-api/GET "/osaamisen-hankkimistapa/:oht-id" request
          :summary "Palauttaa osaamisen hankkimistavan ID:llä"
          :path-params [oht-id :- s/Int]
          :return (rest/response hoks-schema/OsaamisenHankkimistapa)
          (let [oht (ha/get-osaamisen-hankkimistapa-by-id oht-id)]
            (if oht
              (rest/rest-ok oht)
              (do
                (log/warn "No osaamisen hankkimistapa found with ID: " oht-id)
                (response/not-found
                  {:error "No osaamisen hankkimistapa found with given ID"})))))

        (c-api/PATCH "/kyselylinkki" request
          :summary "Lisää lähetystietoja kyselylinkille"
          :body [data hoks-schema/kyselylinkki-lahetys]
          (if-let [old-data (select-kyselylinkki (:kyselylinkki data))]
            (do (h/update-kyselylinkki! data)
                (assoc (response/no-content)
                       :audit-data
                       {:old old-data
                        :new (select-kyselylinkki (:kyselylinkki data))}))

            (response/not-found
              {:error "No kyselylinkki found"}))))

      (c-api/POST "/" [:as request]
        :middleware [wrap-opiskeluoikeus]
        :summary "Luo uuden HOKSin"
        :body [hoks hoks-schema/HOKSLuonti]
        :return (rest/response schema/POSTResponse :id s/Int)
        (post-hoks! hoks request))

      (c-api/GET "/:hoks-id" request
        :middleware [wrap-hoks m/wrap-hoks-access]
        :summary "Palauttaa HOKSin"
        :return (rest/response hoks-schema/HOKS)
        (rest/rest-ok (h/get-hoks-values (:hoks request))))

      (c-api/context "/:hoks-id" []
        (route-middleware
          [wrap-hoks m/wrap-hoks-access wrap-opiskeluoikeus]

          (c-api/PATCH "/" request
            :summary
            "Päivittää olemassa olevan HOKSin ylätason arvoa tai arvoja"
            :body [hoks hoks-schema/HOKSPaivitys]
            (change-hoks! hoks request h/update-hoks!))

          (c-api/PUT "/" request
            :summary "Ylikirjoittaa olemassa olevan HOKSin arvon tai arvot"
            :body [hoks hoks-schema/HOKSKorvaus]
            (change-hoks! hoks request h/replace-hoks!))

          (c-api/GET "/hankintakoulutukset" request
            :summary "Palauttaa hoksin hankintakoulutus opiskeluoikeus-oidit"
            (let [oids (oppijaindex/get-hankintakoulutus-oids-by-master-oid
                         (get-in request [:hoks :opiskeluoikeus-oid]))]
              (response/ok (map :oid oids))))

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
                  (h/insert-kyselylinkki! enriched-data)
                  (assoc (response/no-content)
                         :audit-data {:new enriched-data}))
                (response/not-found
                  {:error "HOKS not found with given HOKS ID"})))))))))
