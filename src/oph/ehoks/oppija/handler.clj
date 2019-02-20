(ns oph.ehoks.oppija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.schema.generator :as g]
            [oph.ehoks.middleware :refer [wrap-authorize]]))

(def routes
  (c-api/context "/oppija" []
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
            :return {:data [hoks-schema/OppijaHOKS]
                     :meta schema/KoodistoErrorMeta}
            (if (= (get-in request [:session :user :oid]) oid)
              (let [hokses (db/get-all-hoks-by-oppija oid)]
                (if (empty? hokses)
                  (response/not-found "No HOKSes found")
                  (response/ok
                    (reduce
                      (fn [c n]
                        (try
                          (update
                            c :data conj (koodisto/enrich n [:urasuunnitelma]))
                          (catch Exception e
                            (let [data (ex-data e)]
                              (when (not= (:type data) :not-found) (throw e))
                              (update-in
                                (update c :data conj n)
                                [:meta :errors]
                                conj
                                {:error-type :not-found
                                 :keys [:urasuunnitelma]
                                 :uri (:uri data)
                                 :version (:version data)})))))
                      {:data []
                       :meta {:errors []}}
                      hokses))))
              (response/unauthorized))))))))
