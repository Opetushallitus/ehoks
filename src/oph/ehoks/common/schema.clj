(ns oph.ehoks.common.schema
  (:require [schema.core :as s]
            [schema.coerce :as coerce]
            [clojure.string :as string]
            [compojure.api.coercion.core :as cc]
            [compojure.api.coercion.schema :as schema-coercion]))

(s/defschema Translated
             "Translated string"
             {:fi s/Str
              (s/optional-key :en) (s/maybe s/Str)
              (s/optional-key :sv) (s/maybe s/Str)})

(s/defschema HealthcheckStatus
             "Service healthcheck status"
             {})

(s/defschema Lokalisointi
             "Localization Service"
             [{:category s/Str
               :createdBy  s/Str
               :key  s/Str
               :force s/Bool
               :locale  s/Str
               :value  s/Str
               :created s/Num
               :modified s/Any
               :accessed s/Any
               :accesscount s/Int
               :id s/Int
               :modifiedBy  s/Str}])

(s/defschema
  OppijaSearchResult
  "Oppijan haun tulos"
  {:oid s/Str
   :nimi s/Str
   :opiskeluoikeus-oid s/Str
   (s/optional-key :hoks-id) s/Int
   (s/optional-key :tutkinto-nimi) {(s/optional-key :fi) s/Str
                                    (s/optional-key :en) s/Str
                                    (s/optional-key :sv) s/Str}
   (s/optional-key :osaamisala-nimi) {(s/optional-key :fi) s/Str
                                      (s/optional-key :en) s/Str
                                      (s/optional-key :sv) s/Str}})

(s/defschema
  Oppija
  "Oppijan perustiedot"
  {:oid s/Str
   :nimi s/Str
   (s/optional-key :opiskeluoikeus-oid) s/Str})

(s/defschema
  KoodiMetadata
  "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"
  {(s/optional-key :nimi) (s/maybe s/Str)
   (s/optional-key :lyhyt-nimi) (s/maybe s/Str)
   (s/optional-key :kuvaus) (s/maybe s/Str)
   :kieli s/Str})

(s/defschema
  KoodistoKoodi
  "Koodisto-koodi"
  {:koodi-arvo s/Str
   :koodi-uri s/Str
   :versio s/Int
   (s/optional-key :metadata) [KoodiMetadata]})

(defn- trim-strings-matcher
  [schema]
  (when (= s/Str schema)
    (fn [value]
      (if (string? value)
        (string/trim value)
        value))))

; https://github.com/metosin/compojure-api/wiki/Coercion#custom-schema-coercion
(defmethod cc/named-coercion :custom-schema [_]
  (schema-coercion/create-coercion
    (assoc-in
      schema-coercion/default-options
      [:body :formats "application/json"]
      (coerce/first-matcher
        [trim-strings-matcher schema-coercion/json-coercion-matcher]))))
