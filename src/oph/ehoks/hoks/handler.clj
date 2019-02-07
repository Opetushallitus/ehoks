(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.schema.generator :as g]
            [schema.core :as s])
  (:import (java.time LocalDate)))

(def ^:private puuttuva-paikallinen-tutkinnon-osa
  (c-api/context "/:hoks-id/puuttuva-paikallinen-tutkinnon-osa" [hoks-id]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin puuttuvan paikallisen tutkinnon osan"
      :return (rest/response
                hoks-schema/PaikallinenTutkinnonOsa)
      (rest/rest-ok {:id 1
                     :amosaa-tunniste 1
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
      "/:id"
      []
      :summary "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan"
      :body
      [_ hoks-schema/PaikallinenTutkinnonOsaPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:id"
      []
      :summary
      "Päivittää HOKSin puuttuvan paikallisen tutkinnon
  osan arvoa tai arvoja"
      :body
      [_ hoks-schema/PaikallinenTutkinnonOsaKentanPaivitys]
      (response/no-content))))

(def ^:private puuttuva-ammatillinen-osaaminen
  (c-api/context "/:hoks-id/puuttuva-ammatillinen-osaaminen" [hoks-id]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin puuttuvan ammatillisen
osaamisen"
      :return (rest/response
                hoks-schema/PuuttuvaAmmatillinenOsaaminen)
      (rest/rest-ok {:id 1
                     :tutkinnon-osa {:tunniste {:koodi-arvo "1"
                                                :koodi-uri "esimerkki_uri"
                                                :versio 1}}
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
      "/:id" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen
    osaamisen"
      :body
      [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:id" []
      :summary
      "Päivittää HOKSin puuttuvan ammatillisen
      osaamisen arvoa tai arvoja"
      :body
      [_ hoks-schema/PuuttuvaAmmatillinenOsaaminenKentanPaivitys]
      (response/no-content))))

(def ^:private puuttuvat-yhteisen-tutkinnon-osat
  (c-api/context "/:hoks-id/puuttuvat-yhteisen-tutkinnon-osat" [hoks-id]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin puuttuvat paikallisen tutkinnon osat	"
      :return (rest/response
                hoks-schema/PuuttuvaYTO)
      (rest/rest-ok {:id 1
                     :osa-alueet []
                     :koulutuksen-jarjestaja-oid "1"
                     :tunniste {:koodi-arvo "1"
                                :koodi-uri "esimerkki_uri"
                                :versio 1}}))

    (c-api/POST "/" []
      :summary
      "Luo (tai korvaa vanhan) puuttuvan paikallisen tutkinnon osat HOKSiin"
      :body
      [_ hoks-schema/PuuttuvaYTOLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT
      "/:id" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen
  osaamisen"
      :body
      [_ hoks-schema/PuuttuvaYTOPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:id" []
      :summary
      "Päivittää HOKSin puuttuvan ammatillisen
    osaamisen arvoa tai arvoja"
      :body
      [_ hoks-schema/PuuttuvaYTOKentanPaivitys]
      (response/no-content))))

(def ^:private opiskeluvalmiuksia-tukevat-opinnot
  (c-api/context "/:hoks-id/opiskeluvalmiuksia-tukevat-opinnot" [hoks-id]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin opiskeluvalmiuksia tukevat opinnot	"
      :return (rest/response
                hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot)
      (rest/rest-ok {:id 1
                     :nimi ""
                     :kuvaus ""
                     :kesto 1
                     :ajankohta {:alku (LocalDate/of 2018 12 12)
                                 :loppu (LocalDate/of 2018 12 20)}}))

    (c-api/POST "/" []
      :summary
      "Luo (tai korvaa vanhan) opiskeluvalmiuksia tukevat opinnot HOKSiin"
      :body
      [_ hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotLuonti]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT
      "/:id" []
      :summary "Päivittää HOKSin opiskeluvalmiuksia tukevat opinnot"
      :body
      [_ hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotPaivitys]
      (response/no-content))

    (c-api/PATCH
      "/:id" []
      :summary
      "Päivittää HOKSin opiskeluvalmiuksia tukevat opintojen arvoa tai arvoja"
      :body
      [_ hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys]
      (response/no-content))))

(def routes
  (c-api/context "/hoks" []
    :tags ["hoks"]

    (c-api/GET "/:id" [id]
      :summary "Palauttaa HOKSin"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HOKS)
      (rest/rest-ok (db/get-hoks-by-id id)))

    (c-api/POST "/" [:as request]
      :summary "Luo uuden HOKSin"
      :body [hoks hoks-schema/HOKSLuonti]
      :return (rest/response schema/POSTResponse)
      (let [h (db/create-hoks! hoks)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id h))})))

    (c-api/PUT "/:id" [id]
      :summary "Päivittää olemassa olevaa HOKSia"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HOKSPaivitys]
      (if (db/update-hoks! id values)
        (response/no-content)
        (response/not-found "HOKS not found with given eHOKS ID")))

    (c-api/PATCH "/:id" []
      :summary "Päivittää olemassa olevan HOKSin arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HOKSKentanPaivitys]
      (if (db/update-hoks-values! id values)
        (response/no-content)
        (response/not-found "HOKS not found with given eHOKS ID")))

    puuttuva-ammatillinen-osaaminen
    puuttuva-paikallinen-tutkinnon-osa
    puuttuvat-yhteisen-tutkinnon-osat
    opiskeluvalmiuksia-tukevat-opinnot))
