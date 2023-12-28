(ns oph.ehoks.resources
  (:require [compojure.core :refer [GET]]
            [ring.util.http-response :as response]
            [ring.util.mime-type :as mime]))

(defn create-routes
  "Create routes under particular path"
  [^String path root]
  (GET (str path (if (.endsWith path "/") "*" "/*"))
    {{resource-path :*} :route-params}
    (response/content-type
      (response/resource-response (str root "/" resource-path))
      (mime/ext-mime-type resource-path))))
