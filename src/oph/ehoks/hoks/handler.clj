(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.db.memory :as db]
            [schema.core :as s]
            [clojure.tools.logging :as log])
  (:import (java.time LocalDate)))

(def ^:private puuttuva-paikallinen-tutkinnon-osa
  (c-api/context "/:hoks-eid/puuttuva-paikallinen-tutkinnon-osa" [hoks-eid]

    (c-api/GET "/:eid" [eid]
      :summary "Palauttaa HOKSin puuttuvan paikallisen tutkinnon osan"
      :path-params [eid :- s/Int]
      :return (rest/response
                hoks-schema/PaikallinenTutkinnonOsa)
      (rest/rest-ok (db/get-ppto-by-eid eid)))

    (c-api/POST
      "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan paikallisen
 tutkinnon osan"
      :body
      [ppto hoks-schema/PaikallinenTutkinnonOsaLuonti]
      :return (rest/response schema/POSTResponse)
      (let [ppto-response (db/create-ppto! ppto)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:eid ppto-response))})))

    (c-api/PUT
      "/:eid"
      []
      :summary "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan"
      :path-params [eid :- s/Int]
      :body [values hoks-schema/PaikallinenTutkinnonOsaPaivitys]
      (if (db/update-ppto! eid values)
        (response/no-content)
        (response/not-found "PPTO not found with given PPTO ID")))

    (c-api/PATCH
      "/:eid"
      []
      :summary
      "Päivittää HOKSin puuttuvan paikallisen tutkinnon
  osan arvoa tai arvoja"
      :path-params [eid :- s/Int]
      :body [values hoks-schema/PaikallinenTutkinnonOsaKentanPaivitys]
      (if (db/update-ppto-values! eid values)
        (response/no-content)
        (response/not-found "PPTO not found with given PPTO ID")))))

(def ^:private puuttuva-ammatillinen-osaaminen
  (c-api/context "/:hoks-eid/puuttuva-ammatillinen-osaaminen" [hoks-eid]

    (c-api/GET "/:eid" [eid]
      :summary "Palauttaa HOKSin puuttuvan ammatillisen
osaamisen"
      :path-params [eid :- s/Int]
      :return (rest/response
                hoks-schema/PuuttuvaAmmatillinenOsaaminen)
      (rest/rest-ok (db/get-ppao-by-eid eid)))

    (c-api/POST
      "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan ammatillisen
  osaamisen HOKSiin"
      :body
      [ppao hoks-schema/PuuttuvaAmmatillinenOsaaminenLuonti]
      :return (rest/response schema/POSTResponse)
      (let [ppao-response (db/create-ppao! ppao)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:eid ppao-response))})))

    (c-api/PUT
      "/:eid" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen
    osaamisen"
      :path-params [eid :- s/Int]
      :body
      [values hoks-schema/PuuttuvaAmmatillinenOsaaminenPaivitys]
      (if (db/update-ppao! eid values)
        (response/no-content)
        (response/not-found "PPAO not found with given PPAO ID")))

    (c-api/PATCH "/:eid" []
      :summary   "Päivittää HOKSin puuttuvan ammatillisen
        osaamisen arvoa tai arvoja"
      :path-params [eid :- s/Int]
      :body [values hoks-schema/PuuttuvaAmmatillinenOsaaminenKentanPaivitys]
      (if (db/update-ppao-values! eid values)
        (response/no-content)
        (response/not-found  "PPAO not found with given PPAO ID")))))

(def ^:private puuttuvat-yhteisen-tutkinnon-osat
  (c-api/context "/:hoks-eid/puuttuvat-yhteisen-tutkinnon-osat" [hoks-eid]

    (c-api/GET "/:eid" [eid]
      :summary "Palauttaa HOKSin puuttuvat yhteisen tutkinnon osat	"
      :path-params [eid :- s/Int]
      :return (rest/response
                hoks-schema/PuuttuvaYTO)
      (rest/rest-ok (db/get-pyto-by-eid eid)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan yhteisen tutkinnon osat HOKSiin"
      :body
      [pyto hoks-schema/PuuttuvaYTOLuonti]
      :return (rest/response schema/POSTResponse)
      (let [pyto-response (db/create-pyto! pyto)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:eid pyto-response))})))

    (c-api/PUT
      "/:eid" []
      :summary "Päivittää HOKSin puuttuvan yhteisen tutkinnon osat"
      :path-params [eid :- s/Int]
      :body
      [values hoks-schema/PuuttuvaYTOPaivitys]
      (if (db/update-pyto! eid values)
        (response/no-content)
        (response/not-found "PYTO not found with given PYTO ID")))

    (c-api/PATCH
      "/:eid" []
      :summary
      "Päivittää HOKSin puuttuvan yhteisen tutkinnon osat arvoa tai arvoja"
      :path-params [eid :- s/Int]
      :body
      [values hoks-schema/PuuttuvaYTOKentanPaivitys]
      (if (db/update-pyto-values! eid values)
        (response/no-content)
        (response/not-found "PPTO not found with given PPTO ID")))))

(def ^:private opiskeluvalmiuksia-tukevat-opinnot
  (c-api/context "/:hoks-eid/opiskeluvalmiuksia-tukevat-opinnot" [hoks-eid]

    (c-api/GET "/:eid" [eid]
      :summary "Palauttaa HOKSin opiskeluvalmiuksia tukevat opinnot	"
      :path-params [eid :- s/Int]
      :return (rest/response
                hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot)
      (rest/rest-ok (db/get-ovatu-by-eid eid)))

    (c-api/POST "/"  [:as request]
      :summary
      "Luo (tai korvaa vanhan) opiskeluvalmiuksia tukevat opinnot HOKSiin"
      :body
      [ovatu hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotLuonti]
      :return (rest/response schema/POSTResponse)
      (let [ovatu-response (db/create-ovatu! ovatu)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:eid ovatu-response))})))

    (c-api/PUT
      "/:eid" []
      :summary "Päivittää HOKSin opiskeluvalmiuksia tukevat opinnot"
      :path-params [eid :- s/Int]
      :body
      [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotPaivitys]
      (if (db/update-ovatu! eid values)
        (response/no-content)
        (response/not-found "OVATU not found with given OVATU ID")))

    (c-api/PATCH
      "/:eid" []
      :summary
      "Päivittää HOKSin opiskeluvalmiuksia tukevat opintojen arvoa tai arvoja"
      :path-params [eid :- s/Int]
      :body
      [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys]
      (if (db/update-ovatu-values! eid values)
        (response/no-content)
        (response/not-found "OVATU not found with given OVATU ID")))))

(def ^:private olemassa-oleva-osaaminen
  (c-api/context "/:hoks-eid/olemassa-oleva-osaaminen" [hoks-eid]

    (c-api/GET "/:eid" [:as eid]
      :summary "Palauttaa HOKSin olemassa olevan osaamisen tunnustamisen
       perusteella sisällytetyn osaamisen	"
      :return (rest/response
                hoks-schema/OlemassaOlevaOsaaminen)
      (rest/rest-ok {:eid 1
                     :olemassaoleva-ammatillinen-osaaminen []
                     :olemassaolevat-yto-osa-alueet []
                     :olemassaoleva-paikallinen-tutkinnon-osa []}))

    (c-api/POST "/" []
      :summary
      "Luo (tai korvaa vanhan) olemassa olevan osaamisen tunnustamisen
      perusteella sisällytetyn osaamisen HOKSiin"
      :body
      [_ hoks-schema/OlemassaOlevaOsaaminenLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT
      "/:eid" []
      :summary "Päivittää HOKSin olemassa olevan osaamisen tunnustamisen
      perusteella sisällytetyn osaamisen"
      :body
      [_ hoks-schema/OlemassaOlevaOsaaminenPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:eid" []
      :summary
      "Päivittää HOKSin olemassa olevan osaamisen tunnustamisen perusteella
      sisällytetyn osaamisen arvoa tai arvoja"
      :body
      [_ hoks-schema/OlemassaOlevaOsaaminenKentanPaivitys]
      (response/no-content))))

(def routes
  (c-api/context "/hoks" []
    :tags ["hoks"]

    (c-api/GET "/:eid" [eid]
      :summary "Palauttaa HOKSin"
      :path-params [eid :- s/Int]
      :return (rest/response hoks-schema/HOKS)
      (rest/rest-ok (db/get-hoks-by-eid eid)))

    (c-api/POST "/" [:as request]
      :summary "Luo uuden HOKSin"
      :body [hoks hoks-schema/HOKSLuonti]
      :return (rest/response schema/POSTResponse)
      (let [h (db/create-hoks! hoks)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:eid h))})))

    (c-api/PUT "/:eid" [eid]
      :summary "Päivittää olemassa olevaa HOKSia"
      :path-params [eid :- s/Int]
      :body [values hoks-schema/HOKSPaivitys]
      (if (db/update-hoks! eid values)
        (response/no-content)
        (response/not-found "HOKS not found with given eHOKS ID")))

    (c-api/PATCH "/:eid" []
      :summary "Päivittää olemassa olevan HOKSin arvoa tai arvoja"
      :path-params [eid :- s/Int]
      :body [values hoks-schema/HOKSKentanPaivitys]
      (if (db/update-hoks-values! eid values)
        (response/no-content)
        (response/not-found "HOKS not found with given eHOKS ID")))

    puuttuva-ammatillinen-osaaminen
    puuttuva-paikallinen-tutkinnon-osa
    puuttuvat-yhteisen-tutkinnon-osat
    opiskeluvalmiuksia-tukevat-opinnot
    olemassa-oleva-osaaminen))
