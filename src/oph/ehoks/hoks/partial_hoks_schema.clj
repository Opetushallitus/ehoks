(ns oph.ehoks.hoks.partial-hoks-schema
  "Schemas for updating parts of hoks (tutkinnon osa)"
  (:require [schema.core :as s]
            [oph.ehoks.schema-tools :refer [modify]]
            [oph.ehoks.hoks.schema :as hoks-schema]))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaLuonti
  (modify
    hoks-schema/HankittavaAmmatillinenTutkinnonOsa
    "Hankittavan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)"
    {:removed [:id]}))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaPaivitys
  (modify
    hoks-schema/HankittavaAmmatillinenTutkinnonOsa
    (str "Hankittavan ammatillisen osaamisen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio
      :osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid]}))

(s/defschema
  HankittavaYTOLuonti
  (modify
    HankittavaYTO
    (str "Hankittavan yhteinen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  HankittavaYTOKentanPaivitys
  (modify
    HankittavaYTO
    (str "Hankittavan yhteinen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:osa-alueet :koulutuksen-jarjestaja-oid :tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio]}))
