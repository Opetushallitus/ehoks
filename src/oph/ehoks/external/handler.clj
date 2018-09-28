(ns oph.ehoks.external.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.external.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [clojure.core.async :as a]))

(defn- map-perusteet [values]
  (map
    (fn [v]
      (-> (select-keys v [:id :nimi :osaamisalat :tutkintonmimikkeet])
          (update :nimi select-keys [:fi :en :sv])
          (update :osaamisalat (fn [x] (map #(select-keys % [:nimi]) x)))
          (update :tutkintonimikkeet
                  (fn [x] (map #(select-keys % [:nimi]) x)))))
    values))

(def routes
  (c-api/context "/external" []

    (c-api/GET "/eperusteet/" [:as request]
      :summary "Hakee perusteiden tietoja ePerusteet-palvelusta"
      :query-params [nimi :- String]
      :return (rest/response [schema/Peruste])
      (a/go
        (let [data (eperusteet/search-perusteet-info nimi)
              values (map-perusteet data)]
          (rest/rest-ok values))))))
