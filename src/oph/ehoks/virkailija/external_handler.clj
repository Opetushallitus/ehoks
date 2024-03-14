(ns oph.ehoks.virkailija.external-handler
  (:require [compojure.api.sweet :as c-api]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.external.amosaa :as amosaa]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]))

(def routes
  "Virkailija external handler routes"
  (c-api/context "/external" []
    :header-params [caller-id :- s/Str]
    :tags ["virkailija-external"]

    lokalisointi-handler/routes

    (c-api/context "/organisaatio" []
      (c-api/GET "/find" []
        :query-params [oids :- [s/Str]]
        :summary "Hakee organisaatiot oidien perusteella"
        :return (restful/response [s/Any])
        (restful/ok (organisaatio/find-organisaatiot oids)))

      (c-api/GET "/:oid" []
        :path-params [oid :- s/Str]
        :summary "Organisaation tiedot oidin perusteella"
        :return (restful/response s/Any)
        (try
          (restful/ok (organisaatio/get-organisaatio-info oid))
          (catch Exception e
            (let [data (ex-data e)]
              (if (= (:status data) 404)
                (response/not-found)
                (throw e)))))))

    (c-api/context "/koodisto" []
      (c-api/GET "/:koodi-uri" []
        :path-params [koodi-uri :- s/Str]
        :summary "Koodiston haku Koodisto-Koodi-Urilla."
        :return (restful/response s/Any)
        (restful/ok (koodisto/get-koodi koodi-uri)))

      (c-api/GET "/:koodi-uri/versiot" []
        :path-params [koodi-uri :- s/Str]
        :summary "Koodiston versioiden haku Koodisto-koodi-urilla."
        :return (restful/response s/Any)
        (restful/ok (koodisto/get-koodi-versiot koodi-uri)))

      (c-api/GET "/:koodi-uri/koodi" []
        :path-params [koodi-uri :- s/Str]
        :summary "Koodiston uusimpien versioiden haku."
        :return (restful/response [s/Any])
        (restful/ok (koodisto/get-koodi-latest-versiot koodi-uri))))

    (c-api/context "/eperusteet" []
      (c-api/GET "/tutkinnonosat/:id/viitteet" []
        :path-params [id :- Long]
        :summary "Tutkinnon osan viitteet."
        :return (restful/response [s/Any])
        (try
          (restful/ok (eperusteet/get-tutkinnon-osa-viitteet id))
          (catch Exception e
            (if (= (:status (ex-data e)) 400)
              (response/not-found
                {:message "Tutkinnon osa not found"})
              (throw e)))))

      (c-api/GET "/tutkinnonosat/:id/osaalueet" []
        :path-params [id :- Long]
        :summary "Yhteisen tutkinnon osan osa-alueet."
        :return (restful/response [s/Any])
        (try
          (restful/ok (eperusteet/get-tutkinnon-osan-osa-alueet id))
          (catch Exception e
            (if (= (:status (ex-data e)) 400)
              (response/not-found
                {:message "Tutkinnon osan osa-alue not found"})
              (throw e)))))

      (c-api/GET "/tutkinnot" []
        :query-params [diaarinumero :- String]
        :summary "Tutkinnon haku diaarinumeron perusteella."
        :return (restful/response s/Any)
        (try
          (restful/ok (eperusteet/find-tutkinto diaarinumero))
          (catch Exception e
            (if (= (:status (ex-data e)) 404)
              (response/not-found {:message "Tutkinto not found"})
              (throw e)))))

      (c-api/GET "/tutkinnot/:id/suoritustavat/reformi/rakenne" []
        :path-params [id :- Long]
        :summary "Tutkinnon reformi rakenne."
        :return (restful/response s/Any)
        (restful/with-not-found-handling
          (eperusteet/get-rakenne id "reformi")))

      (c-api/GET "/tutkinnot/:id/suoritustavat/ops/tutkinnonosat" []
        :path-params [id :- Long]
        :summary "Tutkinnon ops rakenne."
        :return (restful/response s/Any)
        (restful/with-not-found-handling
          (eperusteet/get-rakenne id "ops")))

      (c-api/GET "/:koodi-uri" []
        :path-params [koodi-uri :- s/Str]
        :summary "Tutkinnon osan perusteiden haku
                            Koodisto-Koodi-Urilla."
        :return (restful/response [s/Any])
        (-> (eperusteet/find-tutkinnon-osat koodi-uri)
            eperusteet/adjust-tutkinnonosa-arviointi
            restful/ok))

      (c-api/GET "/koulutuksenOsa/:koodi-uri" [:as request]
        :summary "Hakee koulutuksenOsan ePerusteet-palvelusta"
        :path-params [koodi-uri :- s/Str]
        :return (restful/response [s/Any])
        (restful/with-not-found-handling
          (eperusteet/get-koulutuksenOsa-by-koodiUri koodi-uri))))

    (c-api/context "/eperusteet-amosaa" []
      (c-api/GET "/koodi/:koodi" []
        :path-params [koodi :- String]
        :summary "Amosaa tutkinnon osan hakeminen koodin perusteella.
                 Koodiin täydennetään automaattisesti
                 'paikallinen_tutkinnonosa'"
        :return (restful/response [s/Any])
        (restful/ok (amosaa/get-tutkinnon-osa-by-koodi koodi))))))
