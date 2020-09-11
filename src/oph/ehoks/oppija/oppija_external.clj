(ns oph.ehoks.oppija.oppija-external
  (:require [oph.ehoks.external.organisaatio :as organisaatio]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [oph.ehoks.restful :as rest]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [compojure.api.sweet :as c-api]
            [oph.ehoks.external.amosaa :as amosaa]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.external.koodisto :as koodisto]))

(def routes
  (route-middleware
    [wrap-authorize]
    (c-api/context "/koodisto" []
      (c-api/GET "/:koodi-uri" [koodi-uri]
        :path-params [koodi-uri :- s/Str]
        :summary "Koodiston haku Koodisto-Koodi-Urilla."
        :return (rest/response s/Any)
        (rest/rest-ok (koodisto/get-koodi koodi-uri))))

    (c-api/context "/eperusteet" []
      (c-api/GET "/tutkinnonosat/:id/viitteet" [id]
        :path-params [id :- Long]
        :summary "Tutkinnon osan viitteet."
        :return (rest/response [s/Any])
        (rest/rest-ok (eperusteet/get-tutkinnon-osa-viitteet id)))

      (c-api/GET "/tutkinnonosat/:id/osaalueet" []
        :path-params [id :- Long]
        :summary "Yhteisen tutkinnon osan osa-alueet."
        :return (rest/response [s/Any])
        (try
          (rest/rest-ok
            (eperusteet/get-tutkinnon-osan-osa-alueet id))
          (catch Exception e
            (if (= (:status (ex-data e)) 400)
              (response/not-found
                {:message "Tutkinnon osan osa-alue not found"})
              (throw e)))))

      (c-api/GET "/tutkinnot" []
        :query-params [diaarinumero :- String]
        :summary "Tutkinnon haku diaarinumeron perusteella."
        :return (rest/response s/Any)
        (rest/rest-ok (eperusteet/find-tutkinto diaarinumero)))

      (c-api/GET "/tutkinnot/:id/suoritustavat/reformi/rakenne" [id]
        :path-params [id :- Long]
        :summary "Tutkinnon rakenne."
        :return (rest/response s/Any)
        (rest/rest-ok (eperusteet/get-suoritustavat id)))

      (c-api/GET "/tutkinnot/:id/suoritustavat/ops/tutkinnonosat" []
        :path-params [id :- Long]
        :summary "Tutkinnon ops suoritustavat"
        :return (rest/response s/Any)
        (rest/with-not-found-handling
          (eperusteet/get-ops-suoritustavat id)))

      (c-api/GET "/:koodi-uri" [koodi-uri]
        :path-params [koodi-uri :- s/Str]
        :summary "Tutkinnon osan perusteiden
                           haku Koodisto-Koodi-Urilla."
        :return (rest/response [s/Any])
        (rest/rest-ok (eperusteet/adjust-tutkinnonosa-arviointi
                        (eperusteet/find-tutkinnon-osat koodi-uri)))))

    (c-api/context "/eperusteet-amosaa" []
      (c-api/GET "/koodi/:koodi" []
        :path-params [koodi :- String]
        :summary "Amosaa tutkinnon osan hakeminen koodin perusteella.
                 Koodiin täydennetään automaattisesti
                 'paikallinen_tutkinnonosa'"
        :return (rest/response [s/Any])
        (rest/rest-ok (amosaa/get-tutkinnon-osa-by-koodi koodi))))

    (c-api/context "/organisaatio" []
      (c-api/GET "/:oid" []
        :path-params [oid :- s/Str]
        :summary "Organisaation tiedot oidin perusteella"
        :return (rest/response s/Any)
        (rest/with-not-found-handling
          (organisaatio/get-organisaatio-info oid))))))
