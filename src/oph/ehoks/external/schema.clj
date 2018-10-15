(ns oph.ehoks.external.schema
  (:require [schema.core :as s]))

(def KoskiDate s/Str)
(def KoskiTimestamp s/Str)

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

(s/defschema KoskiHenkilo
             "Henkilötiedot Koskessa"
             {:oid s/Str
              :hetu s/Str
              :syntymäaika s/Str
              :etunimet s/Str
              :kutsumanimi s/Str
              :sukunimi s/Str})

(s/defschema TranslatedValue
             ""
             {:fi s/Str
              (s/optional-key :sv) s/Str
              (s/optional-key :en) s/Str})

(s/defschema KoodistoKoodi
             ""
             {:koodiarvo s/Str
              :nimi TranslatedValue
              (s/optional-key :lyhytNimi) TranslatedValue
              (s/optional-key :koodistoUri) s/Str
              (s/optional-key :koodistoVersio) s/Int})

(s/defschema KoskiOppilaitos
             ""
             {:oid s/Str
              :oppilaitosnumero KoodistoKoodi
              :nimi TranslatedValue
              :kotipaikka KoodistoKoodi})

(s/defschema KoskiOpiskeluoikeusJakso
             ""
             {:alku KoskiDate
              :tila KoodistoKoodi
              (s/optional-key :opintojenRahoitus) KoodistoKoodi})

(s/defschema KoskiOpiskeluoikeusTila
             ""
             {:opiskeluoikeusjaksot [KoskiOpiskeluoikeusJakso]})

(s/defschema KoskiKoulutustoimija
             ""
             {:oid s/Str
              :nimi TranslatedValue
              :yTunnus s/Str
              :kotipaikka KoodistoKoodi})

(s/defschema KoskiOrganisaatio
             ""
             {:oid s/Str
              :oppilaitosnumero KoodistoKoodi
              (s/optional-key :nimi) TranslatedValue
              (s/optional-key :kotipaikka) KoodistoKoodi})

(s/defschema KoskiMyontajaHenkilo
             ""
             {:nimi s/Str
              :titteli TranslatedValue
              :organisaatio KoskiOrganisaatio})

(s/defschema KoskiLaajuus
             ""
             {:arvo s/Num
              :yksikkö KoodistoKoodi})

(s/defschema KoskiKoulutusmoduuli
             ""
             {:tunniste KoodistoKoodi
              (s/optional-key :kieli) KoodistoKoodi
              (s/optional-key :pakollinen) s/Bool
              (s/optional-key :perusteenDiaarinumero) s/Str
              (s/optional-key :laajuus) KoskiLaajuus
              (s/optional-key :kuvaus) TranslatedValue
              (s/optional-key :koulutustyyppi) KoodistoKoodi
              (s/optional-key :perusteenNimi) TranslatedValue})

(s/defschema KoskiArviointi
             ""
             {:arvosana KoodistoKoodi
              :hyväksytty s/Bool
              (s/optional-key :päivä) KoskiDate
              (s/optional-key :kuvaus) TranslatedValue
              (s/optional-key :arvioitsijat) [{:nimi s/Str
                                               (s/optional-key :ntm) s/Bool}]
              (s/optional-key :arviointikohteet) [{:tunniste KoodistoKoodi
                                                   :arvosana KoodistoKoodi}]
              (s/optional-key :arvioinnistaPäättäneet) [KoodistoKoodi]
              (s/optional-key
                :arviointikeskusteluunOsallistuneet) [KoodistoKoodi]})

(s/defschema KoskiTunnisteKuvaus
             ""
             {:tunniste KoodistoKoodi
              :kuvaus TranslatedValue})

(s/defschema KoskiNaytto
             ""
             {:kuvaus TranslatedValue
              :suorituspaikka KoskiTunnisteKuvaus
              :suoritusaika {:alku KoskiDate
                             :loppu KoskiDate}
              :työssäoppimisenYhteydessä s/Bool
              :arviointi KoskiArviointi})

(s/defschema KoskiTunnustettu
             ""
             {:osaaminen {:koulutusmoduuli KoskiKoulutusmoduuli
                          :tyyppi KoodistoKoodi}
              :selite TranslatedValue
              :rahoituksenPiirissä s/Bool})

(s/defschema KoskiOsasuoritus
             ""
             {:koulutusmoduuli KoskiKoulutusmoduuli
              (s/optional-key :yksilöllistettyOppimäärä) s/Bool
              (s/optional-key :painotettuOpetus) s/Bool
              :arviointi [KoskiArviointi]
              :tyyppi KoodistoKoodi
              (s/optional-key :tutkinnonOsanRyhmä) KoodistoKoodi
              (s/optional-key :toimipiste) KoskiOppilaitos
              (s/optional-key :näyttö) KoskiNaytto
              (s/optional-key :lisätiedot) [KoskiTunnisteKuvaus]
              (s/optional-key :osasuoritukset) [KoskiOsasuoritus]
              (s/optional-key :alkamispäivä) KoskiDate
              (s/optional-key :tunnustettu) KoskiTunnustettu})

(s/defschema KoskiTyossaoppimisjakso
             ""
             {:alku KoskiDate
              :loppu KoskiDate
              :työssäoppimispaikka TranslatedValue
              :paikkakunta KoodistoKoodi
              :maa KoodistoKoodi
              :työtehtävät TranslatedValue
              :laajuus KoskiLaajuus})

(s/defschema KoskiSuoritus
             ""
             {:vahvistus {:päivä KoskiDate
                          :paikkakunta KoodistoKoodi
                          :myöntäjäOrganisaatio KoskiOrganisaatio
                          :myöntäjäHenkilöt [KoskiMyontajaHenkilo]}
              :suorituskieli KoodistoKoodi
              (s/optional-key :luokka) s/Str
              :koulutusmoduuli KoskiKoulutusmoduuli
              (s/optional-key :alkamispäivä) KoskiDate
              (s/optional-key :osasuoritukset) [KoskiOsasuoritus]
              :toimipiste KoskiOrganisaatio
              :tyyppi KoodistoKoodi
              (s/optional-key :jääLuokalle) s/Bool
              (s/optional-key :suoritustapa) KoodistoKoodi
              (s/optional-key :muutSuorituskielet) [KoodistoKoodi]
              (s/optional-key :kielikylpykieli) KoodistoKoodi
              (s/optional-key :käyttäytymisenArvio) KoskiArviointi
              (s/optional-key :osaamisala) [{:osaamisala KoodistoKoodi}]
              (s/optional-key :ryhmä) s/Str
              (s/optional-key :tutkintonimike) [KoodistoKoodi]
              (s/optional-key :järjestämismuodot)
              [{:alku KoskiDate
                :järjestämismuoto {:tunniste KoodistoKoodi}}]
              (s/optional-key :työssäoppimisjaksot) [KoskiTyossaoppimisjakso]})

(s/defschema KoskiOpiskeluoikeus
             "Oppijan opiskeluoikeus Koskessa"
             {:tila KoskiOpiskeluoikeusTila
              :aikaleima KoskiTimestamp
              :päättymispäivä KoskiDate
              :oppilaitos KoskiOppilaitos
              :oid s/Str
              :alkamispäivä KoskiDate
              :koulutustoimija KoskiKoulutustoimija
              :versionumero s/Int
              :suoritukset [KoskiSuoritus]
              :lähdejärjestelmänId {:id s/Str
                                    :lähdejärjestelmä KoodistoKoodi}
              :tyyppi KoodistoKoodi
              (s/optional-key :arvioituPäättymispäivä) KoskiDate})

(s/defschema KoskiOppija
             "Oppijan tiedot Koskessa"
             {:henkilö KoskiHenkilo
              :opiskeluoikeudet [KoskiOpiskeluoikeus]})
