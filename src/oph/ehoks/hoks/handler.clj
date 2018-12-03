(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest]))

(def ^:private puuttuva-paikallinen-tutkinnon-osa
  (c-api/context "/:hoks-eid/puuttuva-paikallinen-tutkinnon-osa" [hoks-eid]
    (c-api/GET "/:eid" [:as eid]
      :summary "Palauttaa HOKSin puuttuvan paikallisen tutkinnon osan"
      :return (rest/response hoks-schema/PuuttuvaPaikallinenTutkinnonOsa)
      (rest/rest-ok {}))

    (c-api/POST "/" []
      :summary
      "Luo (tai korvaa vanhan) puuttuvan paikallisen tutkinnon osan"
      :body [_ hoks-schema/PuuttuvaPaikallinenTutkinnonOsaLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT "/:eid" []
      :summary "Päivittää HOKSin puuttuvan paikallisen tutkinnon osan"
      :body [_ hoks-schema/PuuttuvaPaikallinenTutkinnonOsaPaivitys]
      (response/no-content))

    (c-api/PATCH "/:eid" []
      :summary
      "Päivittää HOKSin puuttuvan paikallisen tutkinnon osan arvoa tai arvoja"
      :body [_ hoks-schema/PuuttuvaPaikallinenTutkinnonOsaKentanPaivitys]
      (response/no-content))))

(def ^:private puuttuva-ammatillinen-osaaminen
  (c-api/context "/:hoks-eid/puuttuva-ammatillinen-osaaminen" [hoks-eid]
    (c-api/GET "/:eid" [:as eid]
      :summary "Palauttaa HOKSin puuttuvan ammatillisen osaamisen"
      :return (rest/response hoks-schema/PuuttuvaAmmatillinenOsaaminen)
      (rest/rest-ok {}))

    (c-api/POST "/" []
      :summary
      "Luo (tai korvaa vanhan) puuttuvan ammatillisen osaamisen HOKSiin"
      :body [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT "/:eid" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen osaamisen"
      :body [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenPaivitys]
      (response/no-content))

    (c-api/PATCH "/:eid" []
      :summary
      "Päivittää HOKSin puuttuvan ammatillisen osaamisen arvoa tai arvoja"
      :body [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenKentanPaivitys]
      (response/no-content))))

(def routes
  (c-api/context "/hoks" []
    :tags ["hoks"]

    (c-api/GET "/:eid" [:as eid]
      :summary "Palauttaa HOKSin"
      :return (rest/response hoks-schema/HOKS)
      (rest/rest-ok {}))

    (c-api/POST "/" []
      :summary "Luo uuden HOKSin"
      :body [_ hoks-schema/HOKSLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT "/:eid" []
      :summary "Päivittää olemassa olevaa HOKSia"
      :body [_ hoks-schema/HOKSPaivitys]
      (response/no-content))

    (c-api/PATCH "/:eid" []
      :summary "Päivittää olemassa olevan HOKSin arvoa tai arvoja"
      :body [_ hoks-schema/HOKSKentanPaivitys]
      (response/no-content))

    puuttuva-ammatillinen-osaaminen
    puuttuva-paikallinen-tutkinnon-osa))
