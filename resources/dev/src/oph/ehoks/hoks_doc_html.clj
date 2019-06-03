(ns oph.ehoks.hoks-doc-html
  (:require [clojure.string :as cstr]
            [schema.core :as s]
            [hiccup.core :as h]
            [clojure.java.io :as io]
            [oph.ehoks.hoks.schema]
            [clj-time.local :as l]
            [clj-time.format :as f]
            [clojure.string :as cstr]
            [oph.ehoks.schema.generator :as g]
            [oph.ehoks.hoks-doc :refer [translations]]))

(def local-formatter (f/formatter "dd.MM.yyyy HH.mm"))

(def github-url
  "https://github.com/Opetushallitus/ehoks/blob/master/doc/hoks.md")

(def schemas (let [m (ns-publics 'oph.ehoks.hoks.schema)]
               (select-keys
                 m
                 (for [[k v] m :when (not (fn? (deref v)))]
                   k))))

(defn required-str [k]
  (if (= (type k) schema.core.OptionalKey)
    "Ei"
    "Kyllä"))

(defn key-str [k]
  (name
    (if (= (type k) schema.core.OptionalKey)
      (:k k)
      k)))

(defn generate-link [{t :name}]
  [:a {:href (str github-url "#" t)} t])

(defn get-enum-translation [n]
  (format "Joukon alkio (%s)" (cstr/join ", " (map name (rest n)))))

(defn enum? [n]
  (= (and (coll? n) (first n)) 'enum))

(defn translate-fi [n]
  (if (enum? n)
    (get-enum-translation n)
    (get translations n (str n))))

(defn get-name [v]
  (let [m (meta v)]
    (if (some? (:name m))
      (generate-link m)
      (translate-fi (s/explain v)))))

(defn gen-type-element [t]
  (if (sequential? t)
    [:span "[" (get-name (first t)) "]"]
    (get-name t)))

(defn gen-access-str [a]
  (if (= a :optional)
    "valinnainen"
    "pakollinen"))

(defn gen-access-type [v method]
  (let [t (g/get-type v method)
        a (g/get-access v method)]
    (when (not= a :excluded)
      [:span
       (gen-type-element t) ", " (gen-access-str a)])))

(defn generate-restful-header [m-meta]
  [:div
   [:h3 (:name m-meta)]
   [:p (:doc m-meta "")]])

(defn gen-table-row [[k v]]
  [:tr
   [:td (key-str k)]
   [:td (:description v)]
   [:td (gen-access-type v :get)]
   [:td (gen-access-type v :post)]
   [:td (gen-access-type v :put)]
   [:td (gen-access-type v :patch)]])

(defn gen-table-header []
  [:tr
   [:th "Nimi"]
   [:th "Selite"]
   [:th "Luku"]
   [:th "Luonti"]
   [:th "Päivitys"]
   [:th "Osapäivitys"]])

(defn generate-restful-table [m]
  (apply
    vector
    :table
    (gen-table-header)
    (map gen-table-row m)))

(defn generate-doc [s-col]
  (apply
    vector
    :div
    (mapv
      (fn [s]
        (let [m (deref s)
              m-meta (meta m)]
          [:div {:class "model" :id (:name m-meta)}
           (generate-restful-header m-meta)
           (generate-restful-table m)]))
      (vals s-col))))

(defn basic? [v]
  (-> v
      meta
      :restful
      not))

(defn filter-restful [s]
  (filter
    (fn [[_ v]] (not (basic? (deref v))))
    s))

(defn get-restful []
  [:div
   (generate-doc (filter-restful schemas))])

(defn gen-hiccup [content]
  [:html {:lang "fi"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title "eHOKS RESTful kehitysdokumentaatio"]
    [:style
     "table {border-collapse: collapse;}"
     "table, th, td {border: 1px solid black;}"
     ".model {border-top: 1px solid gray;}"]
    [:link {:rel "stylesheet" :href "https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css"}]]
   [:div.container
    [:div
     [:h1 "HOKS doc"]
     [:p "Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin"
      "esittämiseen."]
     [:p
      "Tämä dokumentaatio keskittyy toistaiseksi ainoastaan HOKS-tietomallin"
      "esittämiseen."]
     [:p
      "Katso myös "
      [:a
       {:href "https://github.com/Opetushallitus/ehoks/blob/master/doc/hoks.md"}
       "HOKS API doc"]
      " dokumentaatio."]
     [:p "Generoitu "
      (f/unparse local-formatter (l/to-local-date-time (l/local-now)))]]
    [:div
     content]]])

(defn gen-doc []
  (gen-hiccup (get-restful)))

(defn write-doc! [target]
  (println (str "Generating HTML formatted document to " target))
  (let [hicdoc (gen-doc)]
    (with-open [w (io/writer target)]
      (.write w "<!doctype html>")
      (.write w (h/html hicdoc))))

  (println "HTML document has been generated."))
