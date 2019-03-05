(ns oph.ehoks.initial-migration
  (:require [oph.ehoks.hoks.schema]
            [oph.ehoks.hoks-doc :refer [schemas]]
            [clojure.string :as cstr]
            [camel-snake-kebab.core :as csk]
            [schema.core :as s]))

; One time use script for generating initial migration of schema

(def models
  '#{Arvioija
     Arviointikriteeri
     HOKS
     HankitunOsaamisenNaytto
     HankitunPaikallisenOsaamisenNaytto
     HankitunYTOOsaamisenNaytto
     Henkilo
     HoksToimija
     KoulutuksenJarjestajaOrganisaatio
     KoulutuksenjarjestajaArvioija
     MuuOppimisymparisto
     MuuTutkinnonOsa
     NaytonJarjestaja
     NayttoYmparisto
     OlemassaOlevaAmmatillinenTutkinnonOsa
     OlemassaOlevaPaikallinenTutkinnonOsa
     OlemassaOlevaYhteinenTutkinnonOsa
     OlemassaOlevanYTOOsaAlue
     OpiskeluvalmiuksiaTukevatOpinnot
     Oppilaitoshenkilo
     Organisaatio
     OsaamisenHankkimistapa
     PuuttuvaAmmatillinenOsaaminen
     PuuttuvaPaikallinenTutkinnonOsa
     PuuttuvaYTO
     TodennettuArviointiLisatiedot
     TyoelamaArvioija
     TyoelamaOrganisaatio
     TyopaikallaHankittavaOsaaminen
     VastuullinenOhjaaja
     YhteinenTutkinnonOsa
     YhteisenTutkinnonOsanOsaAlue})

(defn generate-create-table [table-name]
  (format "CREATE TABLE %s" (csk/->snake_case table-name)))

(defn generate-id []
  {:name "id"
   :type 'Id})

(defn generate-created-at []
  {:name "created_at"
   :type 'Inst
   :not-null? true
   :default "now()"})

(defn generate-updated-at []
  {:name "updated_at"
   :type 'Inst
   :not-null? true
   :default "now()"})

(defn generate-deleted-at []
  {:name "deleted_at"
   :type 'Inst})

(defn generate-version []
  {:name "version"
   :type 'Int
   :default 0})

(defn generate-base-model-body []
  [(generate-id)
   (generate-created-at)
   (generate-updated-at)
   (generate-deleted-at)
   (generate-version)])

(def regexes
  #{"#\"^tutkinnonosat_\\d+$\""
    "#\"^urasuunnitelma_\\d{4}$\""
    "#\"^1\\.2\\.246\\.562\\.15\\.\\d{11}$\""
    "#\"^osaamisenhankkimistapa_.+$\""
    "#\"^ammatillisenoppiaineet_.+$\""
    "#\"^1\\.2\\.246\\.562\\.[0-3]\\d\\.\\d{11}$\""
    "#\"^valittuprosessi_\\d+$\""})

(defn get-type [v]
  (let [e (s/explain v)]
    (cond
      (map? e) (:name (meta v))
      (vector? e) :reference
      (contains? regexes (str e)) :varchar256
      :else e)))

(defn get-reference [k v]
  (cond
    (:name (meta v))
    {:to (-> v meta :name)}
    (vector? v)
    {:many-to-many? true
     :to (format
           "%s_%s"
           (name (csk/->snake_case (get k :k k)))
           (csk/->snake_case
             (str
               (or
                 (-> v first meta :name)
                 (if (= (first v) java.lang.String) "string" v)))))}))

(defn generate-column [[k v]]
  {:name (if (= (type k) schema.core.OptionalKey)
           (:k k)
           k)
   :reference (get-reference k v)
   :type (get-type v)})

(defn replace-date-times [c]
  (if (some #(= (:type %) 'Aikavali) c)
    (conj
      (filter #(not= (:type %) 'Aikavali) c)
      {:name :alku
       :type 'Inst}
      {:name :loppu
       :type 'Inst})
    c))

(defn generate-clause [[k model]]
  (let [model-meta (meta model)]
    {:create (generate-create-table (:name model-meta))
     :body (into
             (generate-base-model-body)
             (replace-date-times
               (mapv
                 generate-column
                 (filter
                   #(not= (get-in % [0 :k] (first %)) :id)
                   (deref model)))))}))

(def sql-types
  '{Inst "TIMESTAMP WITH TIME ZONE"
    Int "INTEGER"
    Str "TEXT"
    Id "SERIAL PRIMARY KEY"
    Bool "BOOLEAN"
    java.lang.Long "BIGINT"
    java.time.LocalDate "TIMESTAMP WITH TIME ZONE"
    (maybe Str) "TEXT"
    :varchar256 "VARCHAR(256)"})

(defn generate-reference [c]
  (if (get-in c [:reference :to])
    (format
      "REFERENCES TO %s(id)" (csk/->snake_case (get-in c [:reference :to])))
    (throw (ex-info "Unknown type without reference" c))))

(defn to-sql-type [c]
  (let [t (get
            sql-types
            (:type c))]
    (cond
      (some? t) t
      (.startsWith (str (:type c)) "(enum ") "VARCHAR(30)"
      :else (generate-reference c))))

(defn generate-sql [c]
  (str
    (reduce
      (fn [c n]
        (if (some? n) (str c n) c))
      ""
      [(-> c :name csk/->snake_case name)
       (str " " (to-sql-type c))
       (if (:not-null? c) " NOT NULL" "")
       (if (:default c) (format " DEFAULT %s" (:default c)) "")])))

(defn generate-sql-create [c]
  (str
    (:create c)
    "(\n"
    (clojure.string/join ",\n" (mapv generate-sql (:body c)))
    ");\n"))

(defn generate-migration []
  (let [clauses
        (mapv
          generate-clause
          (filter
            #(get models (first %))
            schemas))]
    (clojure.string/join
      "\n"
      (mapv generate-sql-create clauses))))
