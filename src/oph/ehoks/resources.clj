(ns oph.ehoks.resources
  (:require [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [ring.util.http-response :as response]
            [ring.util.mime-type :as mime]))

(defn create-routes [path root]
  (GET (str path (if (.endsWith path "/") "*" "/*"))
    {{resource-path :*} :route-params}
    (response/content-type
      (response/resource-response (str root "/" resource-path))
      (mime/ext-mime-type resource-path))))
