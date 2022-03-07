(ns oph.ehoks.hoks.partial-hoks-schema
  "Schemas for updating parts of hoks (tutkinnon osa)"
  (:require [schema.core :as s]
            [oph.ehoks.schema-tools :refer [modify]]
            [oph.ehoks.hoks.schema :as hoks-schema]))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaLuonti
  (modify
    hoks-schema/HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus
    "Hankittavan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)"
    {:removed [:id :module-id]}))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaPaivitys
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
  HankittavaYTOLuonti
  (modify
    hoks-schema/HankittavaYTOLuontiJaMuokkaus
    (str "Hankittavan yhteinen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id :module-id]}))

(s/defschema
  HankittavaYTOPaivitys
  (modify
    hoks-schema/HankittavaYTOPatch
    (str "Hankittavan yhteinen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:removed [:module-id]
     :optionals
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
  OpiskeluvalmiuksiaTukevatOpinnotPaivitys
  (modify
    hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:nimi :kuvaus :kesto :alku :loppu]}))

(s/defschema
  AiemminHankitunPaikallisenTutkinnonOsanLuonti
  (modify
    hoks-schema/AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus
    (str "Aiemmin hankitun paikallisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST")
    {:removed [:id :module-id]}))

(s/defschema
  AiemminHankitunPaikallisenTutkinnonOsanPaivitys
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
  (modify
    hoks-schema/HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus
    (str "Hankittavan paikallisen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id :module-id]}))

(s/defschema
  HankittavaPaikallinenTutkinnonOsaPaivitys
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
  (modify
    hoks-schema/AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus
    (str "Aiemmin hankitun yhteisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  AiemminHankitunYhteisenTutkinnonOsanPaivitys
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
  (modify
    hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus
    (str "Aiemmin hankitun ammatillisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST)")
    {:removed [:id :module-id]}))

(s/defschema
  AiemminHankitunAmmatillisenTutkinnonOsanPaivitys
  (modify
    hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsaPatch
    (str "Aiemmin hankitun ammatillisen tutkinnon osan tiedot "
         "kenttää tai kenttiä päivittäessä (PATCH)")
    {:removed [:module-id]
     :optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :tutkinnon-osa-koodi-versio
                 :tutkinnon-osa-koodi-uri]}))
