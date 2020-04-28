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
    hoks-schema/HankittavaYTO
    (str "Hankittavan yhteinen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  HankittavaYTOKentanPaivitys
  (modify
    hoks-schema/HankittavaYTO
    (str "Hankittavan yhteinen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:osa-alueet :koulutuksen-jarjestaja-oid :tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio]}))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotLuonti
  (modify
    hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys
  (modify
    hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:nimi :kuvaus :kesto :alku :loppu]}))

(s/defschema
  AiemminHankitunPaikallisenTutkinnonOsanLuonti
  (modify
    hoks-schema/AiemminHankittuPaikallinenTutkinnonOsa
    (str "Aiemmin hankitun paikallisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST")
    {:removed [:id]}))

(s/defschema
  AiemminHankitunPaikallisenTutkinnonOsanPaivitys
  (modify
    hoks-schema/AiemminHankittuPaikallinenTutkinnonOsa
    (str "Aiemmin hankitun paikallisen tutkinnon osan tiedot "
         "kenttää tai kenttiä päivitettäessä (PATCH)")
    {:optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :koulutuksen-jarjestaja-oid
                 :kuvaus
                 :laajuus
                 :nimi]}))

(s/defschema
  HankittavanPaikallisenTutkinnonOsanLuonti
  (modify
    hoks-schema/HankittavaPaikallinenTutkinnonOsa
    (str "Hankittavan paikallisen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  HankittavaPaikallinenTutkinnonOsaPaivitys
  (modify
    hoks-schema/HankittavaPaikallinenTutkinnonOsa
    (str "Hankittavan paikallisen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid
      :osaamisen-osoittaminen
      :kuvaus
      :laajuus
      :nimi]}))

(s/defschema
  AiemminHankitunYhteisenTutkinnonOsanLuonti
  (modify
    hoks-schema/AiemminHankittuYhteinenTutkinnonOsa
    (str "Aiemmin hankitun yhteisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  AiemminHankitunYhteisenTutkinnonOsanPaivitys
  (modify
    hoks-schema/AiemminHankittuYhteinenTutkinnonOsa
    (str "Aiemmin hankitun yhteisen tutkinnon osan tiedot "
         "kenttää tai kenttiä päivitettäessä (PATCH)")
    {:optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :tutkinnon-osa-koodi-versio
                 :tutkinnon-osa-koodi-uri
                 :osa-alueet]}))
