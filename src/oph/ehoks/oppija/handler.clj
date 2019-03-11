(ns oph.ehoks.oppija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.oppija.schema :as oppija-schema]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.middleware :refer [wrap-authorize]]))

(def routes
  (c-api/context "/oppija" []

    (c-api/context "/external" []
      :tags ["oppija-external"]

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

          (c-api/GET "/:koodi-uri" [koodi-uri]
            :path-params [koodi-uri :- s/Str]
            :summary "Tutkinnon osan perusteiden haku Koodisto-Koodi-Urilla."
            :return (rest/response [s/Any])
            (rest/rest-ok (eperusteet/find-tutkinnon-osat koodi-uri))))))

    (c-api/context "/oppijat" []
      :tags ["oppijat"]

      (c-api/context "/:oid" [oid]

        (route-middleware
          [wrap-authorize]
          (c-api/GET "/" []
            :summary "Oppijan perustiedot"
            :return (rest/response [common-schema/Oppija])
            (rest/rest-ok []))

          (c-api/GET "/hoks" [:as request]
            :summary "Oppijan HOKSit kokonaisuudessaan"
            :return (rest/response [oppija-schema/OppijaHOKS])
            (if (= (get-in request [:session :user :oid]) oid)
              (let [hokses (h/get-hokses-by-oppija oid)]
                (if (empty? hokses)
                  (response/not-found {:message "No HOKSes found"})
                  (rest/rest-ok
                    (map
                      #(dissoc
                         % :id :updated-at :created-at :deleted-at :version)
                      hokses))))
              (response/forbidden))))))))
