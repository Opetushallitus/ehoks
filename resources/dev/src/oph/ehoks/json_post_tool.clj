(ns oph.ehoks.json-post-tool
  (:require [oph.ehoks.external.cas :as cas]))

(defn send-json [service path json-body]
  (try
    (cas/with-service-ticket
     {:method :post
      :service service
      :path path
      :options {:as :json
                :body json-body
                :content-type :json
                :accept :json}})
    (catch Exception e

      (let [data (ex-data e)]
        (printf "Error: %s \nStatus: %d\nBody:\n%s\n"
                (.getMessage e) (:status data) (:body data))))))

(defn lein-send-json! [json-file & params]
  (let [options (apply hash-map params)]
    (if (and json-file (get options ":service") (get options ":path"))
      (do
        (when (get options ":config")
          (System/setProperty "config" (get options ":config"))
          (require 'oph.ehoks.config :reload))
        (send-json
          (get options ":service")
          (get options ":path")
          (slurp json-file)))
      (println
        (str "Usage: lein send-json path/to/file.json "
             ":service https://service.com "
             ":path api/v1/hoks "
             ":config path/to/config.edn")))))
