(ns oph.ehoks.hoks-doc
  (:require [clojure.string :as cstr]
            [schema.core :as s]
            [clojure.java.io :as io]
            [oph.ehoks.hoks.schema]))

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
  (format "[%s](#%s)" t t))

(def translations '{Int "Kokonaisluku"
                    Inst "Aikaleima"
                    Str "Merkkijono"
                    Bool "Totuusarvo"
                    java.time.LocalDate "Päivämäärä"
                    (maybe Str) "Valinnainen merkkijono"})

(defn translate-fi [n]
  (get translations n (str n)))

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
     (get-in m [:json-schema :description])
     (required-str k))))

(defn generate-markdown [m]
  (let [m-meta (meta m)]
    (conj
     (apply
      conj
      [(str "### " (:name m-meta) "  ")
       ""
       (get-in m-meta [:json-schema :description])
       ""
       "| Nimi | Tyyppi | Selite | Vaaditaan |"
       "| ---- | ------ | ------ | --------- |"]
      (map #(generate-md-row % (get m %)) (keys m)))
     "")))

(defn generate-doc []
  (map
   #(generate-markdown (deref %))
   (vals schemas)))

(defn write-doc! [target]
  (println (str "Generating markdown formatted document to " target))
  (with-open [w (io/writer target)]
    (.write w "# HOKS API doc\n")
    (.write
     w
     (str "Automaattisesti generoitu dokumentaatiotiedosto HOKS-tietomallin "
          "esittämiseen.\n"))
    (doseq [line (flatten (generate-doc))]
      (assert (string? line) (str "Line must be string. Got: " line))
      (try
        (.write w line)
        (catch Exception e
          (println (str "Error in line: " line))
          (throw e)))
      (.newLine w)))
  (println "Markdown document has been generated."))
