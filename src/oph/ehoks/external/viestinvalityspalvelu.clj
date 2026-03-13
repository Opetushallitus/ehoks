(ns oph.ehoks.external.viestinvalityspalvelu
  (:require [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.oph-url :as u]
            [clj-http.cookies :as cookie]
            [clojure.data.json :as json])
  (:import (clojure.lang ExceptionInfo)))

(def session-store (cookie/cookie-store))

(defn login!
  "Create a session cookie to viestinvalityspalvelu by making a CAS call
  to its login endpoint."
  []
  (->> {:method :get
        :options {:redirect-strategy :none
                  :cookie-store session-store}
        :service (u/get-url "viestinvalityspalvelu-service")
        :url (u/get-url "viestinvalityspalvelu.login")}
       (cas/with-service-ticket)))

(defn call!
  "A generic HTTP client routine for all viestinvalityspalvelu calls."
  ([request] (call! request 1))
  ([request retries]
    (try
      (-> request
          (assoc-in [:options :cookie-store] session-store)
          (assoc :service (u/get-url "viestinvalityspalvelu-service"))
          (c/with-api-headers)
          :body)
      (catch ExceptionInfo e
        (if (and (= 401 (:status (ex-data e)))
                 (> retries 0))
          (do (login!) (call! request (dec retries)))
          (throw e))))))

(defn send-message!
  "Send a message with given parameters via viestinvalityspalvelu"
  [recipient title body]
  (->> {:otsikko title
        :sisalto body
        :sisallonTyyppi :html
        :kielet [:fi :sv :en]
        :lahettaja {:nimi "Opetushallitus – Utbildningsstyrelsen – EDUFI"
                    :sahkopostiOsoite "noreply@opintopolku.fi"}
        :vastaanottajat [{:sahkopostiOsoite recipient}]
        :lahettavaPalvelu "eHOKS"
        :prioriteetti :normaali
        :sailytysaika 365}
       (json/write-str)
       (assoc {:content-type :json :as :json} :body)
       (assoc {:method :post
               :url (u/get-url "viestinvalityspalvelu.send-message")}
              :options)
       (call!)
       :lahetysTunniste))

(defn message-state!
  "Query the state of a given message by message ID (lahetysTunnus)."
  [message-id]
  (->> {:method :get
        :url (u/get-url "viestinvalityspalvelu.message-state" message-id)
        :options {:as :json}}
       (call!)
       :vastaanottajat
       (map :tila)))
