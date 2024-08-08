(ns oph.ehoks.palaute.tapahtuma
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "oph/ehoks/db/sql/palautetapahtuma.sql")
