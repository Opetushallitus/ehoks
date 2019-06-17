(ns oph.ehoks.json-post-tool
  (:require [oph.ehoks.external.cas :as cas]
            [oph.ehoks.config :refer [config]]))

(defn send-json [service path json-body]
  (try
    (cas/with-service-ticket
      {:method :post
       :service service
       :url (format "%s/%s" service path)
       :options {:as :json
                 :body json-body
                 :content-type :json
                 :accept :json}})
    (catch Exception e
      (when (:debug config) (.printStackTrace e))
      (let [data (ex-data e)]
        (printf
          "Error: %s \nLocation: %s\nStatus: %d\nBody:\n%s\n"
          (.getMessage e) (:location data) (:status data) (:body data))))))

(defn lein-send-json! [json-file & params]
  (let [options (apply hash-map params)]
    (if (and json-file (get options ":service") (get options ":path"))
      (do
        (when (.endsWith (:opintopolku-host config) "opintopolku.fi")
          (println "Using prod urls")
          (System/setProperty
            "services_file" "resources/prod/services-oph.properties"))
        (require 'oph.ehoks.external.oph-url :reload)
        (send-json
          (get options ":service")
          (get options ":path")
          (slurp json-file)))
      (do (println
            (str "Usage: lein send-json path/to/file.json "
                 ":service https://service.com "
                 ":path api/v1/hoks "))
          (println
            "Config file can be given in environment variable 'CONFIG'.")))))
