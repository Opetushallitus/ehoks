(ns oph.ehoks.external.schema
  (:require [schema.core :as s]))

(s/defschema Nimi
             "Nimitieto eri kielillä"
             {(s/optional-key :fi) s/Str
              (s/optional-key :sv) s/Str
              (s/optional-key :en) s/Str})

(s/defschema Peruste
             "Peruste-tieto ePerusteet-palvelusta"
             {:id s/Int
              (s/optional-key :nimi) Nimi
              (s/optional-key :osaamisalat) [{:nimi Nimi}]
              (s/optional-key :tutkintonimikkeet) [{:nimi Nimi}]})

(s/defschema KoodistoItem
             "Koodiston koodi (KoodistoItemDto)"
             {:koodistoUri s/Str
              :organisaatioOid s/Str
              :koodistoVersios [s/Int]})

(s/defschema KoodiMetadata
             "Koodiston koodin metadata (KoodiMetaData)"
             {:nimi s/Str
              :kuvaus s/Str
              :lyhytNimi s/Str
              :kayttoohje s/Str
              :kasite s/Str
              :sisaltaaMerkityksen s/Str
              :eiSisallaMerkitysta s/Str
              :huomioitavaKoodi s/Str
              :sisaltaaKoodiston s/Str
              :kieli (s/enum "FI" "SV" "EN")})

(s/defschema ExtendedKoodistoKoodi
             "Laajennettu Koodiston koodi (ExtendedKoodiDto)"
             {:tila (s/enum "PASSIIVINEN" "LUONNOS" "HYVAKSYTTY")
              :koodiArvo s/Str
              :voimassaLoppuPvm (s/maybe s/Str)
              :voimassaAlkuPvm (s/maybe s/Str)
              :koodisto KoodistoItem
              :versio s/Int
              :koodiUri s/Str
              :resourceUri s/Str
              :paivitysPvm s/Int
              :version s/Int
              :metadata [KoodiMetadata]})
