(ns oph.ehoks.oppija.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.koodisto :as koodisto]))

(def routes
  (c-api/context "/oppijat" []
    :tags ["oppijat"]

    (c-api/context "/:oid" [oid]

      (c-api/GET "/" []
        :summary "Oppijan perustiedot"
        :return (rest/response [common-schema/Oppija])
        (rest/rest-ok []))

      (c-api/GET "/hoks" [:as request]
        :summary "Oppijan HOKSit kokonaisuudessaan"
        :return {:data [hoks-schema/HOKS]
                 :meta schema/KoodistoErrorMeta}
        (if (= (get-in request [:session :user :oid]) oid)
          (let [hokses (db/get-all-hoks-by-oppija oid)]
            (if (empty? hokses)
              (response/not-found "No HOKSes found")
              (response/ok
                (try
                  {:data (mapv #(koodisto/enrich % [:urasuunnitelma]) hokses)
                   :meta {}}
                  (catch Exception e
                    (let [data (ex-data e)]
                      (when (not= (:type data) :not-found) (throw e))
                      {:data hokses
                       :meta {:error {:error-type :not-found
                                      :keys [:urasuunnitelma]
                                      :uri (:uri data)
                                      :version (:version data)}}}))))))
          (response/unauthorized))))))
