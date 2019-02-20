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

(def filtered-schemas '#{OppijaHOKS})

(def schemas (let [m (ns-publics 'oph.ehoks.hoks.schema)]
               (select-keys
                 m
                 (for [[k v] m
                       :when
                       (and
                         (nil? (get filtered-schemas (:name (meta (deref v)))))
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
                    java.lang.Long "Suuri kokonaisluku"
                    java.time.LocalDate "Päivämäärä"
                    (maybe Str) "Valinnainen merkkijono"})

(defn get-regex-translation [s]
  (case s
    "#\"^tutkinnonosat_\\d+$\"" "Merkkijono, esim. tutkinnonosat_123456"
    "#\"^osaamisenhankkimistapa_.+$\""
    "Merkkijono, esim. osaamisenhankkimistapa_oppisopimus"
    "#\"^ammatillisenoppiaineet_.+$\""
    "Merkkijono, esim. ammatillisenoppiaineet_aa"
    s))

(defn get-enum-translation [n]
  (format "Joukon alkio (%s)" (cstr/join ", " (map name (rest n)))))

(defn enum? [n]
  (= (and (coll? n) (first n)) 'enum))

(defn translate-fi [n]
  (cond
    (enum? n) (get-enum-translation n)
    (.startsWith (str n) "#") (get-regex-translation (str n))
    :else (get translations n (str n))))

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
      (cstr/replace
        (or
          (get-in m [:json-schema :description])
          (:doc m)
          "")
        #"\n" "<br>")
      (required-str k))))

(defn generate-markdown [m]
  (let [m-meta (meta m)]
    (concat
      [(str "### " (:name m-meta) "  ")
       ""
       (or
         (get-in m-meta [:json-schema :description])
         (:doc m-meta)
         "")]
      (when (not= (type m) java.util.regex.Pattern)
        [""
         "| Nimi | Tyyppi | Selite | Vaaditaan |"
         "| ---- | ------ | ------ | --------- |"])
      (when (not= (type m) java.util.regex.Pattern)
        (map
          #(generate-md-row % (get m %))
          (remove #(= (or (:k %) %) :id) (keys m))))
      [""])))

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
