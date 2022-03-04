(ns oph.ehoks.hoks.partial-hoks-schema
  "Schemas for updating parts of hoks (tutkinnon osa)"
  (:require [schema.core :as s]
            [oph.ehoks.schema-tools :refer [describe modify]]
            [oph.ehoks.hoks.schema :as hoks-schema]))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaLuonti
  (modify
    hoks-schema/HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus
    "Hankittavan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)"
    {:removed [:id :module-id]}))

(s/defschema
  OsaamisenHankkimistapaPaivitys
  (s/constrained
    (modify
      hoks-schema/OsaamisenHankkimistapa
      "Osaamisen hankkimisen tavan päivitys (PATCH)"
      {:removed [:module-id]})
    hoks-schema/oppisopimus-has-perusta?
    "Tieto oppisopimuksen perustasta puuttuu."))

(s/defschema
  OsaamisenOsoittaminenPaivitys
  (modify
    hoks-schema/OsaamisenOsoittaminen
    "Osaamisen hankkimisen osoittamisen päivitys (PATCH)"
    {:removed [:module-id]}))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaPaivitys
  (modify
    hoks-schema/HankittavaAmmatillinenTutkinnonOsa
    (str "Hankittavan ammatillisen osaamisen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:removed [:module-id :osaamisen-hankkimistavat :osaamisen-osoittaminen]
     :optionals
              [:tutkinnon-osa-koodi-uri
               :tutkinnon-osa-koodi-versio
               :osaamisen-hankkimistavat
               :koulutuksen-jarjestaja-oid]
     :added
      (describe
        ""
        (s/optional-key :osaamisen-hankkimistavat)
        [OsaamisenHankkimistapaPaivitys] "Osaamisen hankkimistavat"
        (s/optional-key :osaamisen-osoittaminen)
        [OsaamisenOsoittaminenPaivitys]
        (str "Hankitun osaamisen osoittaminen: "
             "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  YhteisenTutkinnonOsanOsaAluePaivitys
  (modify
    hoks-schema/YhteisenTutkinnonOsanOsaAlue
    "Hankittavan yhteinen tutkinnon osan (YTO) osa-alueen paivitys (PATCH)"
    {:removed [:module-id :osaamisen-osoittaminen :osaamisen-hankkimistavat :id]
     :added
              (describe
                ""
                (s/optional-key :osaamisen-hankkimistavat)
                [OsaamisenHankkimistapaPaivitys] "Osaamisen hankkimistavat"
                (s/optional-key :osaamisen-osoittaminen)
                [OsaamisenOsoittaminenPaivitys]
                (str "Hankitun osaamisen osoittaminen: "
                     "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  HankittavaYTOLuonti
  (modify
    hoks-schema/HankittavaYTO
    "Hankittavan yhteisen tutkinnnon osan (POST, PUT)"
    {:removed
     [:module-id
      :osaamisen-hankkimistavat
      :osaamisen-osoittaminen
      :osa-alueet
      :id]
     :added
     (describe
       ""
       :osa-alueet [YhteisenTutkinnonOsanOsaAluePaivitys]
       "YTO osa-alueet"
       (s/optional-key :osaamisen-hankkimistavat)
       [OsaamisenHankkimistapaPaivitys] "Osaamisen hankkimistavat"
       (s/optional-key :osaamisen-osoittaminen)
       [OsaamisenOsoittaminenPaivitys]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  HankittavaYTOPaivitys
  (modify
    hoks-schema/HankittavaYTO
    (str "Hankittavan yhteinen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:removed
     [:module-id
      :osaamisen-hankkimistavat
      :osaamisen-osoittaminen
      :osa-alueet]
     :optionals
     [:osa-alueet :koulutuksen-jarjestaja-oid :tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio]
     :added
     (describe
       ""
       (s/optional-key :osa-alueet)
       [YhteisenTutkinnonOsanOsaAluePaivitys]
       "YTO osa-alueet"
       (s/optional-key :osaamisen-hankkimistavat)
       [OsaamisenHankkimistapaPaivitys] "Osaamisen hankkimistavat"
       (s/optional-key :osaamisen-osoittaminen)
       [OsaamisenOsoittaminenPaivitys]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

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
    hoks-schema/AiemminHankittuPaikallinenTutkinnonOsa
    "Aiemmin hankitun paikallisen osaamisen päivitys (PATCH)"
    {:removed [:module-id :tarkentavat-tiedot-naytto]
     :optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :koulutuksen-jarjestaja-oid
                 :kuvaus
                 :laajuus
                 :nimi]
     :added
              (describe
                ""
                (s/optional-key :tarkentavat-tiedot-naytto)
                [OsaamisenOsoittaminenPaivitys]
                (str "Hankitun osaamisen osoittaminen: "
                     "Näyttö tai muu osaamisen osoittaminen"))}))

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
    hoks-schema/HankittavaPaikallinenTutkinnonOsa
    "Hankittavan paikallisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :osaamisen-hankkimistavat :osaamisen-osoittaminen]
     :optionals
              [:osaamisen-hankkimistavat
               :koulutuksen-jarjestaja-oid
               :osaamisen-osoittaminen
               :kuvaus
               :laajuus
               :nimi]
     :added
              (describe
                ""
                (s/optional-key :osaamisen-hankkimistavat)
                [OsaamisenHankkimistapaPaivitys] "Osaamisen hankkimistavat"
                (s/optional-key :osaamisen-osoittaminen)
                [OsaamisenOsoittaminenPaivitys]
                (str "Hankitun osaamisen osoittaminen: "
                     "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  AiemminHankitunYhteisenTutkinnonOsanLuonti
  (modify
    hoks-schema/AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus
    (str "Aiemmin hankitun yhteisen tutkinnon osan tiedot uutta "
         "merkintää luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  AiemminHankitunYTOOsaAluePaivitys
  (modify
    hoks-schema/AiemminHankitunYTOOsaAlue
    "AiemminHankitun YTOn osa-alueen tiedot (POST, PUT)"
    {:removed [:module-id :tarkentavat-tiedot-naytto]
     :added
              (describe
                ""
                (s/optional-key :tarkentavat-tiedot-naytto)
                [hoks-schema/OsaamisenOsoittaminenLuontiJaMuokkaus]
                (str "Hankitun osaamisen osoittaminen: "
                     "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  AiemminHankitunYhteisenTutkinnonOsanPaivitys
  (modify
    hoks-schema/AiemminHankittuYhteinenTutkinnonOsa
    "Aiemmin hankitun yhteisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :tarkentavat-tiedot-naytto :osa-alueet]
     :optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :tutkinnon-osa-koodi-versio
                 :tutkinnon-osa-koodi-uri
                 :osa-alueet]
     :added
              (describe
                ""
                (s/optional-key :tarkentavat-tiedot-naytto)
                [OsaamisenOsoittaminenPaivitys]
                (str "Hankitun osaamisen osoittaminen: "
                     "Näyttö tai muu osaamisen osoittaminen")
                :osa-alueet [AiemminHankitunYTOOsaAluePaivitys]
                "YTO osa-alueet")}))

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
    hoks-schema/AiemminHankittuAmmatillinenTutkinnonOsa
    "Aiemmin hankitun ammatillisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :tarkentavat-tiedot-naytto]
     :optionals [:valittu-todentamisen-prosessi-koodi-versio
                 :valittu-todentamisen-prosessi-koodi-uri
                 :tutkinnon-osa-koodi-versio
                 :tutkinnon-osa-koodi-uri]
     :added
              (describe
                ""
                (s/optional-key :tarkentavat-tiedot-naytto)
                [hoks-schema/OsaamisenOsoittaminenLuontiJaMuokkaus]
                (str "Hankitun osaamisen osoittaminen: "
                     "Näyttö tai muu osaamisen osoittaminen"))}))
