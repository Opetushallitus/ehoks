(ns oph.ehoks.virkailija.external-handler
  (:require [compojure.api.sweet :as c-api]
            [schema.core :as s]
            [oph.ehoks.schema.oid :as oid-schema]
            [ring.util.http-response :as response]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.eperusteet :as eperusteet]
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
        :path-params [oid :- oid-schema/OrganisaatioOID]
        :summary "Organisaation tiedot oidin perusteella"
        :return (restful/response s/Any)
        (if-let [organisation (organisaatio/get-organisaatio! oid)]
          (restful/ok organisation)
          (response/not-found))))

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
          (eperusteet/get-koulutuksenOsa-by-koodiUri koodi-uri))))))
