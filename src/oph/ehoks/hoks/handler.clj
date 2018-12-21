(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest])
  (:import (java.time LocalDate)))

(def ^:private puuttuva-paikallinen-tutkinnon-osa
  (c-api/context "/:hoks-eid/puuttuva-paikallinen-tutkinnon-osa" [hoks-eid]

    (c-api/GET "/:eid" [:as eid]
      :summary "Palauttaa HOKSin puuttuvan paikallisen tutkinnon osan"
      :return (rest/response
                hoks-schema/PaikallinenTutkinnonOsa)
      (rest/rest-ok {:eid 1
                     :amosaa-tunniste ""
                     :nimi ""
                     :laajuus 0
                     :kuvaus ""
                     :osaamisen-hankkimistavat []
                     :koulutuksen-jarjestaja-oid ""
                     :hankitun-osaamisen-naytto
                     {:jarjestaja {:nimi ""}
                      :nayttoymparisto {:nimi ""}
                      :kuvaus ""
                      :ajankohta {:alku (LocalDate/of 2018 12 12)
                                  :loppu (LocalDate/of 2018 12 20)}
                      :sisalto ""
                      :ammattitaitovaatimukset []
                      :arvioijat []}
                     :tarvittava-opetus ""}))

    (c-api/POST
      "/" []
      :summary
      "Luo (tai korvaa vanhan) puuttuvan paikallisen
 tutkinnon osan"
      :body
      [_ hoks-schema/PaikallinenTutkinnonOsaLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT
      "/:eid"
      []
      :summary "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan"
      :body
      [_ hoks-schema/PaikallinenTutkinnonOsaPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:eid"
      []
      :summary
      "Päivittää HOKSin puuttuvan paikallisen tutkinnon
  osan arvoa tai arvoja"
      :body
      [_ hoks-schema/PaikallinenTutkinnonOsaKentanPaivitys]
      (response/no-content))))

(def ^:private puuttuva-ammatillinen-osaaminen
  (c-api/context "/:hoks-eid/puuttuva-ammatillinen-osaaminen" [hoks-eid]

    (c-api/GET "/:eid" [:as eid]
      :summary "Palauttaa HOKSin puuttuvan ammatillisen
osaamisen"
      :return (rest/response
                hoks-schema/PuuttuvaAmmatillinenOsaaminen)
      (rest/rest-ok {:eid 1
                     :tutkinnon-osa {:tunniste {:koodi-arvo "1"
                                                :koodi-uri "esimerkki_uri"
                                                :versio 1}
                                     :eperusteet-id ""}
                     :vaatimuksista-tai-tavoitteista-poikkeaminen ""
                     :osaamisen-hankkimistavat []
                     :koulutuksen-jarjestaja-oid ""
                     :tarvittava-opetus ""}))

    (c-api/POST "/" []
      :summary
      "Luo (tai korvaa vanhan) puuttuvan ammatillisen
  osaamisen HOKSiin"
      :body
      [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT
      "/:eid" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen
    osaamisen"
      :body
      [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:eid" []
      :summary
      "Päivittää HOKSin puuttuvan ammatillisen
      osaamisen arvoa tai arvoja"
      :body
      [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenKentanPaivitys]
      (response/no-content))))

(def ^:private puuttuvat-yhteisen-tutkinnon-osat
  (c-api/context "/:hoks-eid/puuttuvat-yhteisen-tutkinnon-osat" [hoks-eid]

    (c-api/GET "/:eid" [:as eid]
      :summary "Palauttaa HOKSin puuttuvat paikallisen tutkinnon osat	"
      :return (rest/response
                hoks-schema/PuuttuvaYTO)
      (rest/rest-ok {:eid 1
                     :eperusteet-id 1
                     :tutkinnon-osat []
                     :koulutuksen-jarjestaja-oid "1"}))

    (c-api/POST "/" []
      :summary
      "Luo (tai korvaa vanhan) puuttuvan paikallisen tutkinnon osat HOKSiin"
      :body
      [_ hoks-schema/PuuttuvaYTOLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT
      "/:eid" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen
  osaamisen"
      :body
      [_ hoks-schema/PuuttuvaYTOPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:eid" []
      :summary
      "Päivittää HOKSin puuttuvan ammatillisen
    osaamisen arvoa tai arvoja"
      :body
      [_ hoks-schema/PuuttuvaYTOKentanPaivitys]
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
      :summary "Päivittää olemassa olevan HOKSin arvoa
tai arvoja"
      :body [_ hoks-schema/HOKSKentanPaivitys]
      (response/no-content))

    puuttuva-ammatillinen-osaaminen
    puuttuva-paikallinen-tutkinnon-osa
    puuttuvat-yhteisen-tutkinnon-osat))
