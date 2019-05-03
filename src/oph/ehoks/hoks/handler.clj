(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
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
            [oph.ehoks.external.kayttooikeus :as kayttooikeus]
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
       (or (user/oph-super-user? ticket-user)
           (hoks-access?
             hoks
             ticket-user
             (get method-privileges (:request-method request))))
        (response/unauthorized!
          {:error (str "No access is allowed. Check Opintopolku privileges and "
                       "'opiskeluoikeus'")})))))

(defmacro with-hoks [hoks id & body]
  `(let [~hoks (pdb/select-hoks-by-id ~id)]
     (if (some? ~hoks)
       (do ~@body)
       (response/not-found {:error "Hoks not found"}))))

(def ^:private puuttuva-paikallinen-tutkinnon-osa
  (c-api/context "/:hoks-id/puuttuva-paikallinen-tutkinnon-osa" [hoks-id]
    :path-params [hoks-id :- s/Int]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin puuttuvan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/PuuttuvaPaikallinenTutkinnonOsa)
      (rest/rest-ok (h/get-puuttuva-paikallinen-tutkinnon-osa id)))

    (c-api/POST "/" [:as request]
      :summary "Luo puuttuvan paikallisen tutkinnon osan"
      :body [ppto hoks-schema/PuuttuvaPaikallinenTutkinnonOsaLuonti]
      :return (rest/response schema/POSTResponse)
      (with-hoks
        hoks hoks-id
        (let [ppto-db (h/save-puuttuva-paikallinen-tutkinnon-osa! hoks ppto)]
          (rest/rest-ok
            {:uri (format "%s/%d" (:uri request) (:id ppto-db))
             :id (:id ppto-db)}))))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin puuttuvan paikallisen tutkinnon osan arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PuuttuvaPaikallinenTutkinnonOsaKentanPaivitys]
      (let [ppto-db (pdb/select-puuttuva-paikallinen-tutkinnon-osa-by-id id)]
        (if (some? ppto-db)
          (do (h/update-puuttuva-paikallinen-tutkinnon-osa! ppto-db values)
              (response/no-content))
          (response/not-found {:error "PPTO not found with given PPTO ID"}))))))

(def ^:private puuttuva-ammatillinen-osaaminen
  (c-api/context "/:hoks-id/puuttuva-ammatillinen-osaaminen" [hoks-id]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin puuttuvan ammatillisen osaamisen"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/PuuttuvaAmmatillinenOsaaminen)
      (rest/rest-ok (db/get-ppao-by-id id)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan ammatillisen osaamisen HOKSiin"
      :body [ppao hoks-schema/PuuttuvaAmmatillinenOsaaminenLuonti]
      :return (rest/response schema/POSTResponse)
      (let [ppao-response (db/create-ppao! ppao)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id ppao-response))
           :id (:id ppao-response)})))

    (c-api/PUT "/:id" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen osaamisen"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PuuttuvaAmmatillinenOsaaminenPaivitys]
      (if (db/update-ppao! id values)
        (response/no-content)
        (response/not-found "PPAO not found with given PPAO ID")))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin puuttuvan ammatillisen osaamisen arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PuuttuvaAmmatillinenOsaaminenKentanPaivitys]
      (if (db/update-ppao-values! id values)
        (response/no-content)
        (response/not-found  "PPAO not found with given PPAO ID")))))

(def ^:private puuttuvat-yhteisen-tutkinnon-osat
  (c-api/context "/:hoks-id/puuttuvat-yhteisen-tutkinnon-osat" [hoks-id]

    (c-api/GET "/:id" [id]
      :summary "Palauttaa HOKSin puuttuvat yhteisen tutkinnon osat"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/PuuttuvaYTO)
      (rest/rest-ok (db/get-pyto-by-id id)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan yhteisen tutkinnon osat HOKSiin"
      :body [pyto hoks-schema/PuuttuvaYTOLuonti]
      :return (rest/response schema/POSTResponse)
      (let [pyto-response (db/create-pyto! pyto)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id pyto-response))
           :id (:id pyto-response)})))

    (c-api/PUT "/:id" []
      :summary "Päivittää HOKSin puuttuvan yhteisen tutkinnon osat"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PuuttuvaYTOPaivitys]
      (if (db/update-pyto! id values)
        (response/no-content)
        (response/not-found "PYTO not found with given PYTO ID")))

    (c-api/PATCH "/:id" []
      :summary
      "Päivittää HOKSin puuttuvan yhteisen tutkinnon osat arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PuuttuvaYTOKentanPaivitys]
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
      :return (rest/response schema/POSTResponse)
      (let [ovatu-response (db/create-ovatu! ovatu)]
        (rest/rest-ok
          {:uri (format "%s/%d" (:uri request) (:id ovatu-response))
           :id (:id ovatu-response)})))

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
      [wrap-user-details]
      (c-api/GET "/:id" [id :as request]
        :summary "Palauttaa HOKSin"
        :path-params [id :- s/Int]
        :return (rest/response hoks-schema/HOKS)
        (let [hoks (pdb/select-hoks-by-id id)]
          (check-hoks-access! hoks request)
          (rest/rest-ok (h/get-hoks-values hoks))))

      (c-api/POST "/" [:as request]
        :summary "Luo uuden HOKSin"
        :body [hoks hoks-schema/HOKSLuonti]
        :return (rest/response schema/POSTResponse)
        (check-hoks-access! hoks request)
        (when (seq (pdb/select-hoksit-by-opiskeluoikeus-oid
                     (:opiskeluoikeus-oid hoks)))
          (response/bad-request!
            {:error "HOKS with the same opiskeluoikeus-oid already exists"}))
        (let [hoks-db (h/save-hoks! hoks)]
          (when (:save-hoks-json? config)
            (write-hoks-json! hoks))
          (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id hoks-db))
                         :id (:id hoks-db)})))

      (c-api/PATCH "/:id" [id :as request]
        :summary "Päivittää olemassa olevan HOKSin arvoa tai arvoja"
        :path-params [id :- s/Int]
        :body [values hoks-schema/HOKSKentanPaivitys]
        (let [hoks (pdb/select-hoks-by-id id)]
          (check-hoks-access! hoks request))
        (pdb/update-hoks-by-id! id values)
        (response/no-content))

      puuttuva-paikallinen-tutkinnon-osa
      (c-api/undocumented
        puuttuva-ammatillinen-osaaminen
        puuttuvat-yhteisen-tutkinnon-osat
        opiskeluvalmiuksia-tukevat-opinnot))))
