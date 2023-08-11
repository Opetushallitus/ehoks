(ns oph.ehoks.hoks.partial-hoks-schema
  "Schemas for updating parts of hoks (tutkinnon osa)"
  (:require [schema.core :as s]
            [oph.ehoks.schema-tools :refer [modify]]
            [oph.ehoks.hoks.schema :as hoks-schema]))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaLuonti
  "Schema hankittavan ammatillisen tutkinnon osan luontikyselyyn."
  (modify
    hoks-schema/HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus
    "Hankittavan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)"
    {:removed [:id :module-id]}))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaPaivitys
  "Schema hankittavan ammatillisen tutkinnon osan päivityskyselyyn."
  (modify
    hoks-schema/HankittavaAmmatillinenTutkinnonOsaPatch
    (str "Hankittavan ammatillisen osaamisen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:removed [:module-id]
     :optionals
     [:tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio
      :osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid]}))

(s/defschema
  HankittavaYhteinenTutkinnonOsaLuonti
  "Schema hankittavan yhteisen tutkinnon osan luontikyselyyn."
  (modify
    hoks-schema/HankittavaYhteinenTutkinnonOsaLuontiJaMuokkaus
    (str "Hankittavan yhteinen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id :module-id]}))

(s/defschema
  HankittavaYhteinenTutkinnonOsaPaivitys
  "Schema hankittavan yhteisen tutkinnon osan päivityskyselyyn."
  (modify
    hoks-schema/HankittavaYhteinenTutkinnonOsaPatch
    (str "Hankittavan yhteinen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:removed [:module-id]
     :optionals
     [:osa-alueet :koulutuksen-jarjestaja-oid :tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio]}))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotLuonti
  "Schema opiskeluvalmiuksia tukevien opintojen luontikyselyyn."
  (modify
    hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotPaivitys
  "Schema opiskeluvalmiuksia tukevien opintojen päivityskyselyyn."
  (modify
    hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:nimi :kuvaus :kesto :alku :loppu]}))

(s/defschema
  AiemminHankitunPaikallisenTutkinnonOsanLuonti
  "Schema aiemmin hankitun paikallisen tutkinnon osan luontikyselyyn."
  (modify
    hoks-schema/AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus
    (str "Aiemmin hankitun paikallisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST")
    {:removed [:id :module-id]}))

(s/defschema
  AiemminHankitunPaikallisenTutkinnonOsanPaivitys
  "Schema aiemmin hankitun paikallisen tutkinnon osan päivityskyselyyn."
  (modify
    hoks-schema/AiemminHankittuPaikallinenTutkinnonOsaPatch
    (str "Aiemmin hankitun paikallisen tutkinnon osan tiedot "
         "kenttää tai kenttiä päivitettäessä (PATCH)")
    {:removed [:module-id]
     :optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :koulutuksen-jarjestaja-oid
                 :kuvaus
                 :laajuus
                 :nimi]}))

(s/defschema
  HankittavanPaikallisenTutkinnonOsanLuonti
  "Schema hankittavan paikallisen tutkinnon osan luontikyselyyn."
  (modify
    hoks-schema/HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus
    (str "Hankittavan paikallisen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id :module-id]}))

(s/defschema
  HankittavaPaikallinenTutkinnonOsaPaivitys
  "Schema hankittavan paikallisen tutkinnon osan päivityskyselyyn."
  (modify
    hoks-schema/HankittavaPaikallinenTutkinnonOsaPatch
    (str "Hankittavan paikallisen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:removed [:module-id]
     :optionals
     [:osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid
      :osaamisen-osoittaminen
      :kuvaus
      :laajuus
      :nimi]}))

(s/defschema
  AiemminHankitunYhteisenTutkinnonOsanLuonti
  "Schema aiemmin hankitun yhteisen tutkinnon osan luontikyselyyn."
  (modify
    hoks-schema/AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus
    (str "Aiemmin hankitun yhteisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  AiemminHankitunYhteisenTutkinnonOsanPaivitys
  "Schema aiemmin hankitun yhteisen tutkinnon osan päivityskyselyyn."
  (modify
    hoks-schema/AiemminHankittuYhteinenTutkinnonOsaPatch
    (str "Aiemmin hankitun yhteisen tutkinnon osan tiedot "
         "kenttää tai kenttiä päivitettäessä (PATCH)")
    {:removed [:module-id]
     :optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :tutkinnon-osa-koodi-versio
                 :tutkinnon-osa-koodi-uri
                 :osa-alueet]}))

(s/defschema
  AiemminHankitunAmmatillisenTutkinnonOsanLuonti
  "Schema aiemmin hankitun ammatillisen tutkinnon osan luontikyselyyn."
  (modify
    hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus
    (str "Aiemmin hankitun ammatillisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST)")
    {:removed [:id :module-id]}))

(s/defschema
  AiemminHankitunAmmatillisenTutkinnonOsanPaivitys
  "Schema aiemmin hankitun ammatillisen tutkinnon osan päivityskyselyyn."
  (modify
    hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaPatch
    (str "Aiemmin hankitun ammatillisen tutkinnon osan tiedot "
         "kenttää tai kenttiä päivittäessä (PATCH)")
    {:removed [:module-id]
     :optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :tutkinnon-osa-koodi-versio
                 :tutkinnon-osa-koodi-uri]}))
