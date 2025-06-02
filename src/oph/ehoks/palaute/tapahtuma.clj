(ns oph.ehoks.palaute.tapahtuma
  (:require [hugsql.core :as hugsql]
            [oph.ehoks.db :as db]
            [oph.ehoks.utils :as utils]))

(hugsql/def-db-fns "oph/ehoks/db/sql/palautetapahtuma.sql")

(defn build
  ([{:keys [existing-palaute] :as ctx} reason lisatiedot]
    (build ctx (:tila existing-palaute) reason lisatiedot nil))

  ([ctx state reason lisatiedot]
    (build ctx state reason lisatiedot nil))

  ([{:keys [::type existing-palaute request-id] :as ctx}
    state reason lisatiedot palaute]
    {:pre [(some? type) (some? state)]}
    {:palaute-id      (or (:id palaute) (:id existing-palaute))
     :vanha-tila      (utils/to-underscore-str
                        (or (:tila existing-palaute) state))
     :uusi-tila       (utils/to-underscore-str state)
     :tapahtumatyyppi (utils/to-underscore-str type)
     :syy             (utils/to-underscore-str (or reason :hoks-tallennettu))
     :lisatiedot      (assoc lisatiedot :request-id request-id)}))

(defn build-and-insert!
  ([{:keys [existing-palaute] :as ctx} reason lisatiedot]
    (build-and-insert! ctx (:tila existing-palaute) reason lisatiedot nil))
  ([ctx state reason lisatiedot]
    (build-and-insert! ctx state reason lisatiedot nil))
  ([{:keys [tx] :as ctx} state reason lisatiedot palaute]
    (insert! (or tx db/spec) (build ctx state reason lisatiedot palaute))))
