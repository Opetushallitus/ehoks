(ns oph.ehoks.oppija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.middleware :refer [wrap-authorize]]))

(defn with-opiskeluoikeus [hokses]
  (mapv
    #(assoc
       %
       :opiskeluoikeus
       (koski/get-opiskeluoikeus-info (:opiskeluoikeus-oid %)))
    hokses))

(defn enrich-koodi-all [c kk ks]
  (mapv #(koodisto/enrich % (get % kk) ks) c))

(defn enrich-tutkinnon-osa-koodi-all [c]
  (enrich-koodi-all c :tutkinnon-osa-koodi-uri [:tutkinnon-osa-koodisto-koodi]))

(defn enrich-yto-koodi-all [c]
  (enrich-tutkinnon-osa-koodi-all
    (mapv
      #(update
         %
         :osa-alueet
         enrich-koodi-all
         :osa-alue-koodi-uri
         [:osa-alue-koodisto-koodi])
      c)))

(defn enrich-koodit [hoks]
  (-> hoks
      (koodisto/enrich (:urasuunnitelma-koodi-uri hoks) [:urasuunnitelma])
      (update
        :olemassa-olevat-ammatilliset-tutkinnon-osat
        enrich-tutkinnon-osa-koodi-all)
      (update
        :olemassa-olevat-yhteiset-tutkinnon-osat enrich-yto-koodi-all)
      (update
        :puuttuvat-ammatilliset-tutkinnon-osat enrich-tutkinnon-osa-koodi-all)
      (update
        :puuttuvat-yhteiset-tutkinnon-osat enrich-yto-koodi-all)))

(defn enrich-koodisto-koodit [m hoks]
  (try
    (update
      m
      :data
      conj
      (enrich-koodit hoks))
    (catch Exception e
      (let [data (ex-data e)]
        (when (not= (:type data) :not-found) (throw e))
        (update-in
          (update m :data conj hoks)
          [:meta :errors]
          conj
          {:error-type :not-found
           :keys [:urasuunnitelma]
           :path (:path data)})))))

(defn with-koodisto [hokses]
  (reduce
    enrich-koodisto-koodit
    {:data []
     :meta {:errors []}}
    hokses))

(def routes
  (c-api/context "/oppija" []

    (c-api/context "/external" []
      :tags ["oppija-external"]

      (route-middleware
        [wrap-authorize]
        (c-api/context "/koodisto" []
          (c-api/GET "/:koodi-uri" [koodi-uri]
            :path-params [koodi-uri :- s/Str]
            :summary "Oppijan Koodisto-integraatio.
                      Koodiston haku Koodisto-Koodi-Urilla."
            :return (rest/response s/Any)
            (rest/rest-ok (koodisto/get-koodi koodi-uri))))

        (c-api/context "/eperusteet" []
          (c-api/GET "/:koodi-uri" [koodi-uri]
            :path-params [koodi-uri :- s/Str]
            :summary "Oppijan ePerusteet integraatio.
                      Perusteiden haku Koodisto-Koodi-Urilla."
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
            :return {:data [hoks-schema/OppijaHOKS]
                     :meta schema/KoodistoErrorMeta}
            (if (= (get-in request [:session :user :oid]) oid)
              (let [hokses (db/get-all-hoks-by-oppija oid)]
                (if (empty? hokses)
                  (response/not-found "No HOKSes found")
                  (-> (map #(dissoc % :id) hokses)
                      with-opiskeluoikeus
                      with-koodisto
                      response/ok)))
              (response/unauthorized))))))))
