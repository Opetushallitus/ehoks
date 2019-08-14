(ns oph.ehoks.virkailija.external-handler
  (:require [compojure.api.sweet :as c-api]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]))

(def routes
  (c-api/context "/external" []
    :tags ["virkailija-external"]

    lokalisointi-handler/routes

    (c-api/context "/organisaatio" []
      (c-api/GET "/find" []
        :query-params [oids :- [s/Str]]
        :summary "Hakee organisaatiot oidien perusteella"
        :return (restful/response s/Any)
        (restful/rest-ok
          (organisaatio/find-organisaatiot oids))))

    (c-api/context "/koodisto" []
      (c-api/GET "/:koodi-uri" []
        :path-params [koodi-uri :- s/Str]
        :summary "Koodiston haku Koodisto-Koodi-Urilla."
        :return (restful/response s/Any)
        (restful/rest-ok (koodisto/get-koodi koodi-uri)))

      (c-api/GET "/:koodi-uri/versiot" []
        :path-params [koodi-uri :- s/Str]
        :summary "Koodiston versioiden haku Koodisto-koodi-urilla."
        :return (restful/response s/Any)
        (restful/rest-ok (koodisto/get-koodi-versiot koodi-uri)))

      (c-api/GET "/:koodi-uri/koodi" []
        :path-params [koodi-uri :- s/Str]
        :summary "Koodiston uusimpien versioiden haku."
        :return (restful/response s/Any)
        (restful/rest-ok
          (koodisto/get-koodi-latest-versiot koodi-uri))))

    (c-api/context "/eperusteet" []
      (c-api/GET "/tutkinnonosat/:id/viitteet" []
        :path-params [id :- Long]
        :summary "Tutkinnon osan viitteet."
        :return (restful/response [s/Any])
        (try
          (restful/rest-ok
            (eperusteet/get-tutkinnon-osa-viitteet id))
          (catch Exception e
            (if (= (:status (ex-data e)) 400)
              (response/not-found
                {:message "Tutkinnon osa not found"})
              (throw e)))))

      (c-api/GET "/tutkinnot" []
        :query-params [diaarinumero :- String]
        :summary "Tutkinnon haku diaarinumeron perusteella."
        :return (restful/response s/Any)
        (try
          (restful/rest-ok (eperusteet/find-tutkinto diaarinumero))
          (catch Exception e
            (if (= (:status (ex-data e)) 404)
              (response/not-found {:message "Tutkinto not found"})
              (throw e)))))

      (c-api/GET "/tutkinnot/:id/suoritustavat/reformi/rakenne" []
        :path-params [id :- Long]
        :summary "Tutkinnon rakenne."
        :return (restful/response s/Any)
        (try
          (restful/rest-ok (eperusteet/get-suoritustavat id))
          (catch Exception e
            (if (= (:status (ex-data e)) 404)
              (response/not-found {:message "Rakenne not found"})
              (throw e)))))

      (c-api/GET "/:koodi-uri" []
        :path-params [koodi-uri :- s/Str]
        :summary "Tutkinnon osan perusteiden haku
                            Koodisto-Koodi-Urilla."
        :return (restful/response [s/Any])
        (restful/rest-ok
          (eperusteet/find-tutkinnon-osat koodi-uri))))))