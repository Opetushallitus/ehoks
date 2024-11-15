(ns oph.ehoks.palaute.tapahtuma
  (:require [hugsql.core :as hugsql]
            [oph.ehoks.utils :as utils]
            [plumbing.core :refer [map-vals]]))

(hugsql/def-db-fns "oph/ehoks/db/sql/palautetapahtuma.sql")

(defn build
  [{:keys [hoks jakso] :as ctx} state field reason existing-herate palaute]
  {:pre [(some? state)]}
  {:palaute-id      (:id palaute)
   :vanha-tila      (utils/to-underscore-str (or (:tila existing-herate) state))
   :uusi-tila       (utils/to-underscore-str state)
   :tapahtumatyyppi "hoks_tallennus"
   :syy             (utils/to-underscore-str (or reason :hoks-tallennettu))
   :lisatiedot      (map-vals str (select-keys (merge jakso hoks) [field]))})
