(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [clojure.tools.logging :as log]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.db.postgresql :as pdb]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.config :refer [config]]
            [schema.core :as s]
            [clojure.data.json :as json]
            [oph.ehoks.user :as user])
  (:import (java.time LocalDate)))

(def method-privileges {:get :read
                        :post :write
                        :patch :update
                        :delete :delete})

(defn value-writer [_ value]
  (cond
    (= (type value) java.util.Date) (str (java.sql.Date. (.getTime value)))
    (= (type value) java.time.LocalDate) (str value)
    :else value))

(defn write-hoks-json-file! [h file]
  (spit
    file
    (json/write-str
      h
      :value-fn value-writer
      :escape-unicode false)))

(defn write-hoks-json! [h]
  (write-hoks-json-file!
    h
    (java.io.File/createTempFile
      (format "hoks_%d_%d"
              (:id h)
              (quot (System/currentTimeMillis) 1000))
      ".json")))

(defn authorized? [hoks ticket-user method]
  (let [oppilaitos-oid (koski/get-opiskeluoikeus-oppilaitos-oid
                         (:opiskeluoikeus-oid hoks))
        organisation-privileges
        (user/get-organisation-privileges ticket-user oppilaitos-oid)]
    (some? (get organisation-privileges method))))

(defn hoks-access? [hoks ticket-user method]
  (and
    (some? (:opiskeluoikeus-oid hoks))
    (authorized? hoks ticket-user method)))

(defn check-hoks-access! [hoks request]
  (if (nil? hoks)
    (response/not-found!)
    (let [ticket-user (:service-ticket-user request)]
      (when-not
       (hoks-access?
         hoks
         ticket-user
         (get method-privileges (:request-method request)))
        (log/warnf "User %s has no access to hoks %d with opiskeluoikeus %s"
                   (:username ticket-user)
                   (:id hoks)
                   (:opiskeluoikeus-oid hoks))
        (response/unauthorized!
          {:error (str "No access is allowed. Check Opintopolku privileges and "
                       "'opiskeluoikeus'")})))))

(defn user-has-access? [request hoks]
  (let [ticket-user (:service-ticket-user request)]
    (hoks-access?
      hoks
      ticket-user
      (get method-privileges (:request-method request)))))

(defn wrap-hoks-access [handler]
  (fn
    ([request respond raise]
      (let [hoks (:hoks request)]
        (if (nil? hoks)
          (respond response/not-found {:error "HOKS not found"})
          (if (user-has-access? request hoks)
            (handler request respond raise)
            (do
              (log/warnf
                "User %s has no access to hoks %d with opiskeluoikeus %s"
                (get-in request [:service-ticket-user :username])
                (:id hoks)
                (:opiskeluoikeus-oid hoks))
              (respond
                (response/unauthorized
                  {:error (str "No access is allowed. Check Opintopolku "
                               "privileges and 'opiskeluoikeus'")})))))))
    ([request]
      (let [hoks (:hoks request)]
        (if (nil? hoks)
          (response/not-found {:error "HOKS not found"})
          (if (user-has-access? request hoks)
            (handler request)
            (do
              (log/warnf
                "User %s has no access to hoks %d with opiskeluoikeus %s"
                (get-in request [:service-ticket-user :username])
                (:id hoks)
                (:opiskeluoikeus-oid hoks))
              (response/unauthorized
                {:error (str "No access is allowed. Check Opintopolku "
                             "privileges and 'opiskeluoikeus'")}))))))))

(defn wrap-require-service-user [handler]
  (fn
    ([request respond raise]
      (if (= (:kayttajaTyyppi (:service-ticket-user request)) "PALVELU")
        (handler request respond raise)
        (respond (response/forbidden
                   {:error "User type 'PALVELU' is required"}))))
    ([request]
      (if (= (:kayttajaTyyppi (:service-ticket-user request)) "PALVELU")
        (handler request)
        (response/forbidden
          {:error "User type 'PALVELU' is required"})))))

(defn add-hoks [request]
  (let [hoks-id (Integer/parseInt (get-in request [:route-params :hoks-id]))
        hoks (pdb/select-hoks-by-id hoks-id)]
    (assoc request :hoks hoks)))

(defn wrap-hoks [handler]
  (fn
    ([request respond raise]
      (handler (add-hoks request) respond raise))
    ([request]
      (handler (add-hoks request)))))

(def ^:private hankittava-paikallinen-tutkinnon-osa
  (c-api/context "/hankittava-paikallinen-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" [:as request]
      :summary "Palauttaa HOKSin hankittavan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaPaikallinenTutkinnonOsa)
      (rest/rest-ok (h/get-hankittava-paikallinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan paikallisen tutkinnon osan"
      :body [ppto hoks-schema/HankittavanPaikallisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ppto-db (h/save-hankittava-paikallinen-tutkinnon-osa!
                      (:hoks request) ppto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id ppto-db))}
          :id (:id ppto-db))))

    (c-api/PATCH "/:id" [:as request]
      :summary
      "Päivittää HOKSin hankittavan paikallisen tutkinnon osan arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/HankittavaPaikallinenTutkinnonOsaKentanPaivitys]
      (let [ppto-db (pdb/select-hankittava-paikallinen-tutkinnon-osa-by-id id)]
        (if (some? ppto-db)
          (do (h/update-hankittava-paikallinen-tutkinnon-osa! ppto-db values)
              (response/no-content))
          (response/not-found
            {:error "Hankittava paikallinen tutkinnon osa not found"}))))))

(def ^:private hankittava-ammat-tutkinnon-osa
  (c-api/context "/hankittava-ammat-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" [:as request]
      :summary "Palauttaa HOKSin hankittavan ammatillisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaAmmatillinenTutkinnonOsa)
      (rest/rest-ok (h/get-hankittava-ammat-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan ammatillisen osaamisen HOKSiin"
      :body [pao hoks-schema/HankittavaAmmatillinenTutkinnonOsaLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [pao-db (h/save-hankittava-ammat-tutkinnon-osa!
                     (:hoks request) pao)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id pao-db))}
          :id (:id pao-db))))

    (c-api/PATCH "/:id" [:as request]
      :summary
      "Päivittää HOKSin hankittavan ammatillisen tutkinnon osan arvoa ja arvoja"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/HankittavaAmmatillinenTutkinnonOsaKentanPaivitys]
      (if-let [pao-db (h/get-hankittava-ammat-tutkinnon-osa id)]
        (do (h/update-hankittava-ammat-tutkinnon-osa! pao-db values)
            (response/no-content))
        (response/not-found
          {:error "Hankittava ammatillinen tutkinnon osa not found"})))))

(def ^:private aiemmin-hankittu-ammat-tutkinnon-osa
  (c-api/context "/aiemmin-hankittu-ammat-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin aiemmin hankitun ammatillisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsa)
      (rest/rest-ok (h/get-aiemmin-hankittu-ammat-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun ammat tutkinnon osan HOKSiin"
      :body [ooato hoks-schema/AiemminHankitunAmmatillisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ooato-from-db (h/save-aiemmin-hankittu-ammat-tutkinnon-osa!
                            (:id (:hoks request)) ooato)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id ooato-from-db))}
          :id (:id ooato-from-db))))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun ammatillisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/AiemminHankitunAmmatillisenTutkinnonOsanPaivitys]
      (if-let [ooato-from-db
               (pdb/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id
                 id)]
        (do
          (h/update-aiemmin-hankittu-ammat-tutkinnon-osa!
            ooato-from-db values)
          (response/no-content))
        (response/not-found
          {:error "Olemassa oleva ammatillinen tutkinnon osa not found"})))))

(def ^:private aiemmin-hankittu-paikallinen-tutkinnon-osa
  (c-api/context "/aiemmin-hankittu-paikallinen-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin olemassa olevan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/AiemminHankittuPaikallinenTutkinnonOsa)
      (rest/rest-ok (h/get-aiemmin-hankittu-paikallinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo olemassa olevan paikallisen tutkinnon osan HOKSiin"
      :body [oopto hoks-schema/AiemminHankitunPaikallisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [oopto-from-db (h/save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
                            (get-in request [:hoks :id]) oopto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id oopto-from-db))}
          :id (:id oopto-from-db))))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun paikallisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body [values hoks-schema/AiemminHankitunPaikallisenTutkinnonOsanPaivitys]
      (if-let [oopto-from-db
               (pdb/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id
                 id)]
        (do
          (h/update-aiemmin-hankittu-paikallinen-tutkinnon-osa!
            oopto-from-db values)
          (response/no-content))
        (response/not-found
          {:error "Aiemmin hankittu paikallinen tutkinnon osa not found"})))))

(def ^:private aiemmin-hankittu-yhteinen-tutkinnon-osa
  (c-api/context "/aiemmin-hankittu-yhteinen-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin aiemmin hankitun yhteisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/AiemminHankittuYhteinenTutkinnonOsa)
      (rest/rest-ok (h/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun yhteisen tutkinnon osan HOKSiin"
      :body [ooyto hoks-schema/AiemminHankitunYhteisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ooyto-from-db (h/save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
                            (get-in request [:hoks :id]) ooyto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id ooyto-from-db))}
          :id (:id ooyto-from-db))))))

(def ^:private hankittava-yhteinen-tutkinnon-osa
  (c-api/context "/:hoks-id/hankittava-yhteinen-tutkinnon-osa" [hoks-id]

    (c-api/GET "/:id" [id]
      :summary "Palauttaa HOKSin hankittavan yhteisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaYTO)
      (rest/rest-ok (db/get-pyto-by-id id)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) hankittavan yhteisen tutkinnon osat HOKSiin"
      :body [pyto hoks-schema/HankittavaYTOLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [pyto-response (db/create-pyto! pyto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id pyto-response))}
          :id (:id pyto-response))))

    (c-api/PUT "/:id" []
      :summary "Päivittää HOKSin hankittavan yhteisen tutkinnon osat"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HankittavaYTOPaivitys]
      (if (db/update-pyto! id values)
        (response/no-content)
        (response/not-found "PYTO not found with given PYTO ID")))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin hankittavan yhteisen tutkinnon osat arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HankittavaYTOKentanPaivitys]
      (if (db/update-pyto-values! id values)
        (response/no-content)
        (response/not-found "PPTO not found with given PPTO ID")))))

(def ^:private opiskeluvalmiuksia-tukevat-opinnot
  (c-api/context "/:hoks-id/opiskeluvalmiuksia-tukevat-opinnot" [hoks-id]

    (c-api/GET "/:id" [id]
      :summary "Palauttaa HOKSin opiskeluvalmiuksia tukevat opinnot"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot)
      (rest/rest-ok (db/get-ovatu-by-id id)))

    (c-api/POST "/"  [:as request]
      :summary
      "Luo (tai korvaa vanhan) opiskeluvalmiuksia tukevat opinnot HOKSiin"
      :body [ovatu hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ovatu-response (db/create-ovatu! ovatu)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id ovatu-response))}
          :id (:id ovatu-response))))

    (c-api/PUT "/:id" []
      :summary "Päivittää HOKSin opiskeluvalmiuksia tukevat opinnot"
      :path-params [id :- s/Int]
      :body [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotPaivitys]
      (if (db/update-ovatu! id values)
        (response/no-content)
        (response/not-found "OVATU not found with given OVATU ID")))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin opiskeluvalmiuksia tukevat opintojen arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys]
      (if (db/update-ovatu-values! id values)
        (response/no-content)
        (response/not-found "OVATU not found with given OVATU ID")))))

(def routes
  (c-api/context "/hoks" []
    :tags ["hoks"]

    (route-middleware
      [wrap-user-details wrap-require-service-user wrap-audit-logger]

      (c-api/POST "/" [:as request]
        :summary "Luo uuden HOKSin"
        :body [hoks hoks-schema/HOKSLuonti]
        :return (rest/response schema/POSTResponse :id s/Int)
        (check-hoks-access! hoks request)
        (when (seq (pdb/select-hoksit-by-opiskeluoikeus-oid
                     (:opiskeluoikeus-oid hoks)))
          (response/bad-request!
            {:error "HOKS with the same opiskeluoikeus-oid already exists"}))
        (let [hoks-db (h/save-hoks! hoks)]
          (when (:save-hoks-json? config)
            (write-hoks-json! hoks))
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id hoks-db))}
                        :id (:id hoks-db))))

      (c-api/GET "/opiskeluoikeus/:opiskeluoikeus-oid" [oid :as request]
        :summary "Palauttaa HOKSin opiskeluoikeuden oidilla"
        :path-params [opiskeluoikeus-oid :- s/Str]
        :return (rest/response hoks-schema/HOKS)
        (let [hoks (first (pdb/select-hoksit-by-opiskeluoikeus-oid
                            opiskeluoikeus-oid))]
          (check-hoks-access! hoks request)
          (rest/rest-ok hoks)))

      (c-api/context "/:hoks-id" []
        :path-params [hoks-id :- s/Int]

        (route-middleware
          [wrap-hoks wrap-hoks-access]

          (c-api/GET "/" [id :as request]
            :summary "Palauttaa HOKSin"
            :return (rest/response hoks-schema/HOKS)
            (rest/rest-ok (h/get-hoks-values (:hoks request))))

          (c-api/undocumented
            (c-api/PATCH "/" [id :as request]
              :summary "Päivittää olemassa olevan HOKSin arvoa tai arvoja"
              :body [values hoks-schema/HOKSKentanPaivitys]
              (pdb/update-hoks-by-id! id values)
              (response/no-content)))

          hankittava-paikallinen-tutkinnon-osa
          aiemmin-hankittu-ammat-tutkinnon-osa
          aiemmin-hankittu-paikallinen-tutkinnon-osa
          aiemmin-hankittu-yhteinen-tutkinnon-osa
          hankittava-ammat-tutkinnon-osa))

      (c-api/undocumented
        hankittava-yhteinen-tutkinnon-osa
        opiskeluvalmiuksia-tukevat-opinnot))))
