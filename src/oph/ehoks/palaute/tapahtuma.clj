(ns oph.ehoks.palaute.tapahtuma
  (:require [hugsql.core :as hugsql]
            [oph.ehoks.utils :as utils]
            [medley.core :refer [map-vals]]))

(hugsql/def-db-fns "oph/ehoks/db/sql/palautetapahtuma.sql")

(defn build
  [{:keys [hoks jakso existing-palaute] :as ctx} state field reason palaute]
  {:pre [(some? state)]}
  {:palaute-id      (:id palaute)
   :vanha-tila      (utils/to-underscore-str
                      (or (:tila existing-palaute) state))
   :uusi-tila       (utils/to-underscore-str state)
   :tapahtumatyyppi "hoks_tallennus"
   :syy             (utils/to-underscore-str (or reason :hoks-tallennettu))
   :lisatiedot      (map-vals str (select-keys (merge jakso hoks) [field]))})
