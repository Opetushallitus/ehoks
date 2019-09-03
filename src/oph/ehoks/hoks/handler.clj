(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [clojure.tools.logging :as log]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.db.postgresql :as pdb]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [schema.core :as s]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(def ^:private hankittava-paikallinen-tutkinnon-osa
  (c-api/context "/hankittava-paikallinen-tutkinnon-osa" []
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" [:as request]
      :summary "Palauttaa HOKSin hankittavan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaPaikallinenTutkinnonOsa)
      (rest/rest-ok (ha/get-hankittava-paikallinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan paikallisen tutkinnon osan"
      :body [ppto hoks-schema/HankittavanPaikallisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ppto-db (ha/save-hankittava-paikallinen-tutkinnon-osa!
                      hoks-id ppto)]
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
          (do (ha/update-hankittava-paikallinen-tutkinnon-osa! ppto-db values)
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
      (rest/rest-ok (ha/get-hankittava-ammat-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo hankittavan ammatillisen osaamisen HOKSiin"
      :body [pao hoks-schema/HankittavaAmmatillinenTutkinnonOsaLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [pao-db (ha/save-hankittava-ammat-tutkinnon-osa!
                     hoks-id pao)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id pao-db))}
          :id (:id pao-db))))

    (c-api/PATCH "/:id" [:as request]
      :summary
      "Päivittää HOKSin hankittavan ammatillisen tutkinnon osan arvoa ja arvoja"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/HankittavaAmmatillinenTutkinnonOsaKentanPaivitys]
      (if-let [hao-db (ha/get-hankittava-ammat-tutkinnon-osa id)]
        (do (ha/update-hankittava-ammat-tutkinnon-osa! hao-db values)
            (response/no-content))
        (response/not-found
          {:error "Hankittava ammatillinen tutkinnon osa not found"})))))

(def ^:private hankittava-yhteinen-tutkinnon-osa
  (c-api/context "/hankittava-yhteinen-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin hankittavan yhteisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HankittavaYTO)
      (rest/rest-ok (ha/get-hankittava-yhteinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) hankittavan yhteisen tutkinnon osat HOKSiin"
      :body [hyto hoks-schema/HankittavaYTOLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [hyto-response (ha/save-hankittava-yhteinen-tutkinnon-osa!
                            (get-in request [:hoks :id]) hyto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id hyto-response))}
          :id (:id hyto-response))))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin hankittavan yhteisen tutkinnon osat arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HankittavaYTOKentanPaivitys]
      (if (not-empty (pdb/select-hankittava-yhteinen-tutkinnon-osa-by-id id))
        (do
          (ha/update-hankittava-yhteinen-tutkinnon-osa! id values)
          (response/no-content))
        (response/not-found {:error "HYTO not found with given HYTO ID"})))))

(def ^:private aiemmin-hankittu-ammat-tutkinnon-osa
  (c-api/context "/aiemmin-hankittu-ammat-tutkinnon-osa" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin aiemmin hankitun ammatillisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsa)
      (rest/rest-ok (ah/get-aiemmin-hankittu-ammat-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun ammat tutkinnon osan HOKSiin"
      :body [ooato hoks-schema/AiemminHankitunAmmatillisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ooato-from-db (ah/save-aiemmin-hankittu-ammat-tutkinnon-osa!
                            (get-in request [:hoks :id]) ooato)]
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
          (ah/update-aiemmin-hankittu-ammat-tutkinnon-osa!
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
      (rest/rest-ok (ah/get-aiemmin-hankittu-paikallinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo olemassa olevan paikallisen tutkinnon osan HOKSiin"
      :body [oopto hoks-schema/AiemminHankitunPaikallisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [oopto-from-db (ah/save-aiemmin-hankittu-paikallinen-tutkinnon-osa!
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
          (ah/update-aiemmin-hankittu-paikallinen-tutkinnon-osa!
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
      (rest/rest-ok (ah/get-aiemmin-hankittu-yhteinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo aiemmin hankitun yhteisen tutkinnon osan HOKSiin"
      :body [ooyto hoks-schema/AiemminHankitunYhteisenTutkinnonOsanLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [ooyto-from-db (ah/save-aiemmin-hankittu-yhteinen-tutkinnon-osa!
                            (get-in request [:hoks :id]) ooyto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id ooyto-from-db))}
          :id (:id ooyto-from-db))))

    (c-api/PATCH "/:id" []
      :summary (str "Päivittää HOKSin aiemmin hankitun yhteisen tutkinnon "
                    "osan arvoa tai arvoja")
      :path-params [id :- s/Int]
      :body [values hoks-schema/AiemminHankitunYhteisenTutkinnonOsanPaivitys]
      (if-let [ahyto-from-db
               (pdb/select-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id id)]
        (do
          (ah/update-aiemmin-hankittu-yhteinen-tutkinnon-osa!
            ahyto-from-db values)
          (response/no-content))
        (response/not-found
          {:error "Aiemmin hankitun yhteinen tutkinnon osa not found"})))))

(def ^:private opiskeluvalmiuksia-tukevat-opinnot
  (c-api/context "/opiskeluvalmiuksia-tukevat-opinnot" []

    (c-api/GET "/:id" []
      :summary "Palauttaa HOKSin opiskeluvalmiuksia tukevat opinnot"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot)
      (rest/rest-ok (ot/get-opiskeluvalmiuksia-tukeva-opinto id)))

    (c-api/POST "/"  [:as request]
      :summary
      "Luo (tai korvaa vanhan) opiskeluvalmiuksia tukevat opinnot HOKSiin"
      :body [oto hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotLuonti]
      :return (rest/response schema/POSTResponse :id s/Int)
      (let [oto-response (ot/save-opiskeluvalmiuksia-tukeva-opinto!
                           (get-in request [:hoks :id]) oto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id oto-response))}
          :id (:id oto-response))))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin opiskeluvalmiuksia tukevat opintojen arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys]
      (let [count-of-updated-rows
            (first
              (pdb/update-opiskeluvalmiuksia-tukevat-opinnot-by-id! id values))]
        (if (pos? count-of-updated-rows)
          (response/no-content)
          (response/not-found {:error "OTO not found with given OTO ID"}))))))

(def routes
  (c-api/context "/hoks" []
    :tags ["hoks"]
    :header-params [ticket :- s/Str
                    caller-id :- s/Str]

    (route-middleware
      [wrap-user-details m/wrap-require-service-user]

      (c-api/POST "/" [:as request]
        :summary "Luo uuden HOKSin"
        :body [hoks hoks-schema/HOKSLuonti]
        :return (rest/response schema/POSTResponse :id s/Int)
        (try
          (oppijaindex/add-oppija! (:oppija-oid hoks))
          (catch Exception e
            (if (= (:status (ex-data e)) 404)
              (response/bad-request!
                {:error "Oppija not found in Oppijanumerorekisteri"})
              (throw e))))
        (try
          (oppijaindex/add-opiskeluoikeus!
            (:opiskeluoikeus-oid hoks) (:oppija-oid hoks))
          (catch Exception e
            (if (= (:status (ex-data e)) 404)
              (response/bad-request!
                {:error "Opiskeluoikeus not found in Koski"})
              (throw e))))
        (m/check-hoks-access! hoks request)
        (try
          (let [hoks-db (h/save-hoks! hoks)]
            (assoc
              (rest/rest-ok {:uri
                             (format "%s/%d" (:uri request) (:id hoks-db))}
                            :id (:id hoks-db))
              :audit-data {:new hoks}))
          (catch Exception e
            (if (= (:error (ex-data e)) :duplicate)
              (assoc
                (response/bad-request!
                  {:error
                   "HOKS with the same opiskeluoikeus-oid already exists"})
                :audit-data {:new hoks})
              (throw e)))))

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

      (c-api/context "/:hoks-id" []

        (route-middleware
          [m/wrap-hoks m/wrap-hoks-access]

          (c-api/GET "/" request
            :summary "Palauttaa HOKSin"
            :return (rest/response hoks-schema/HOKS)
            (rest/rest-ok (h/get-hoks-values (:hoks request))))

          (c-api/PATCH "/" request
            :summary
            "Päivittää olemassa olevan HOKSin ylätason arvoa tai arvoja"
            :body [values hoks-schema/HOKSPaivitys]
            (if (not-empty (:hoks request))
              (do
                (h/update-hoks! (get-in request [:hoks :id]) values)
                (response/no-content))
              (response/not-found
                {:error "HOKS not found with given HOKS ID"})))

          (c-api/PUT "/" request
            :summary "Ylikirjoittaa olemassa olevan HOKSin arvon tai arvot"
            :body [values hoks-schema/HOKSKorvaus]
            (if (not-empty (:hoks request))
              (do
                (h/replace-hoks! (get-in request [:hoks :id]) values)
                (response/no-content))
              (response/not-found
                {:error "HOKS not found with given HOKS ID"})))

          aiemmin-hankittu-ammat-tutkinnon-osa
          aiemmin-hankittu-paikallinen-tutkinnon-osa
          aiemmin-hankittu-yhteinen-tutkinnon-osa
          hankittava-ammat-tutkinnon-osa
          hankittava-paikallinen-tutkinnon-osa
          hankittava-yhteinen-tutkinnon-osa
          opiskeluvalmiuksia-tukevat-opinnot)))))
