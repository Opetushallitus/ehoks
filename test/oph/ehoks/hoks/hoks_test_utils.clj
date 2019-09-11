(ns oph.ehoks.hoks.hoks-test-utils
  (:require [oph.ehoks.utils :as utils :refer [eq]]
            [ring.mock.request :as mock]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.external.cache :as cache]))

(defn create-app [session-store]
  (cache/clear-cache!)
  (common-api/create-app handler/app-routes session-store))
