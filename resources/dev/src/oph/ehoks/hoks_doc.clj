(ns oph.ehoks.hoks-doc
  (:require [clojure.string :as cstr]
            [schema.core :as s]
            [clojure.java.io :as io]
            [oph.ehoks.hoks.schema]
            [clj-time.local :as l]
            [clj-time.format :as f]
            [clojure.string :as cstr]))

(def doc-url "https://testiopintopolku.fi/ehoks-backend/hoks-doc/index.html")

(def local-formatter (f/formatter "dd.MM.yyyy HH.mm"))

(def schemas (let [m (ns-publics 'oph.ehoks.hoks.schema)]
               (select-keys
                 m
                 (for [[k v] m
                       :when
                       (and
                         (not (:restful (meta (deref v))))
                         (not (fn? (deref v))))]
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
  (format "[%s](#%s)" t t))

(def translations '{Int "Kokonaisluku"
                    Inst "Aikaleima"
                    Str "Merkkijono"
                    Bool "Totuusarvo"
                    java.time.LocalDate "Päivämäärä"
                    (maybe Str) "Valinnainen merkkijono"})

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

(defn generate-md-row [k v]
  (let [m (meta v)]
    (format
      "| %s | %s | %s | %s |"
      (key-str k)
      (if (sequential? v)
        (format "[%s]" (get-name (first v)))
        (get-name v))
      (or
        (get-in m [:json-schema :description])
        (:doc m)
        "")
      (required-str k))))

(defn generate-markdown [m]
  (let [m-meta (meta m)]
    (conj
      (apply
        conj
        [(str "### " (:name m-meta) "  ")
         ""
         (or
           (get-in m-meta [:json-schema :description])
           (:doc m-meta)
           "")
         ""
         "| Nimi | Tyyppi | Selite | Vaaditaan |"
         "| ---- | ------ | ------ | --------- |"]
        (map #(generate-md-row % (get m %)) (keys m)))
      "")))

(defn generate-doc [s]
  (map
    #(generate-markdown (deref %))
    (vals s)))

(defn write-doc! [target]
  (println (str "Generating markdown formatted document to " target))
  (with-open [w (io/writer target)]
    (.write w "# HOKS API doc\n")
    (.write
      w
      (str "Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin "
           "esittämiseen.\n\n"
           "Generoitu "
           (f/unparse local-formatter (l/to-local-date-time (l/local-now)))
           "\n\n"))
    (.write
      w
      (format "Katso myös [HOKS doc](%s)\n\n" doc-url))
    (doseq [line (flatten (generate-doc schemas))]
      (assert
        (string? line)
        (format "Line must be string. Got: %s (%s) " line  (type line)))
      (try
        (.write w line)
        (catch Exception e
          (println (str "Error in line: " line))
          (throw e)))
      (.newLine w)))
  (println "Markdown document has been generated."))
