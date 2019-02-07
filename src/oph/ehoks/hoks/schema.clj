(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]
            [schema-tools.core :as st]
            [ring.swagger.json-schema :as rsjs]
            [oph.ehoks.schema-tools :refer [describe modify]]
            [oph.ehoks.schema.generator :as g])
  (:import (java.time LocalDate)))

(s/defschema
  Organisaatio
  (describe
    "Organisaatio"
    :nimi s/Str "Organisaation nimi"
    (s/optional-key :y-tunnus) s/Str "Organisaation y-tunnus"))

(s/defschema
  KoodiMetadata
  (describe
    "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"
    (s/optional-key :nimi) (s/maybe s/Str) "Koodisto-koodin nimi"
    (s/optional-key :lyhyt-nimi) (s/maybe s/Str) "Koodisto-koodin lyhyt nimi"
    (s/optional-key :kuvaus) (s/maybe s/Str) "Koodisto-koodin kuvaus"
    :kieli s/Str "Koodisto-koodin kieli"))

(s/defschema
  KoodistoKoodi
  (describe
    "Koodisto-koodi"
    :koodi-arvo s/Str "Koodisto-koodin arvo"
    :koodi-uri s/Str "Koodiston URI"
    :versio s/Int "Koodisto-koodin versio"
    (s/optional-key :metadata) [KoodiMetadata]
    "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"))

(s/defschema
  KoodistoKoodiLuonti
  (modify
    KoodistoKoodi
    "Koodisto-koodin lisäys tai päivitys"
    {:removed [:metadata]}))

(s/defschema
  TutkinnonOsa
  (describe
    "Tutkinnon osa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :tunniste KoodistoKoodi "Koodisto-koodi"
    (s/optional-key :laajuus) s/Int "Tutkinnon laajuus ePerusteet palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    (s/optional-key :kuvaus) s/Str
    "Tutkinnon osan kuvaus ePerusteet-palvelussa"))

(s/defschema
  Aikavali
  (describe
    "Aikaväli"
    :alku LocalDate "Alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Loppupäivämäärä muodossa YYYY-MM-DD"))

(s/defschema
  Henkilo
  (describe
    "Henkilö"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :organisaatio Organisaatio "Henkilön organisaatio"
    :nimi s/Str "Henkilön nimi"
    :rooli s/Str "Henkilön rooli"))

(s/defschema
  TyopaikallaHankittavaOsaaminen
  (describe
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :hankkijan-edustaja Henkilo
    "Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja"
    :vastuullinen-ohjaaja Henkilo "Vastuullinen työpaikkaohjaaja"
    :jarjestajan-edustaja Henkilo "Koulutuksen järjestäjän edustaja"
    (s/optional-key :muut-osallistujat) [Henkilo]
    "Muut ohjaukseen osallistuvat henkilöt"
    :keskeiset-tyotehtavat [s/Str] "Keskeiset työtehtävät"
    :ohjaus-ja-tuki s/Bool
    "Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta"
    :erityinen-tuki s/Bool
    (str "Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä "
         "erityisen tuen päätös")
    (s/optional-key :erityisen-tuen-aika) Aikavali
    (str "Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon tai"
         "koulutuksen osassa")))

(s/defschema
  MuuOppimisymparisto
  (describe
    "Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu"
    :tarkenne KoodistoKoodi "Oppimisympäristön tarkenne, eHOS Koodisto-koodi"
    :selite s/Str "Oppimisympäristön nimi"
    :ohjaus-ja-tuki s/Bool
    "Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta"
    :erityinen-tuki s/Bool
    (str "Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä "
         "erityisen tuen päätös")
    (s/optional-key :erityisen-tuen-aika) Aikavali
    (str "Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon tai"
         "koulutuksen osassa")))

(s/defschema
  OsaamisenHankkimistapa
  (describe
    "Osaamisen hankkimisen tapa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :ajankohta Aikavali "Hankkimisen ajankohta"
    :osaamisen-hankkimistavan-tunniste KoodistoKoodi
    "Osaamisen hankkimisen Koodisto-koodi (URI: osaamisenhankkimistapa)"
    (s/optional-key :tyopaikalla-hankittava-osaaminen)
    TyopaikallaHankittavaOsaaminen
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    (s/optional-key :muut-oppimisymparisto)
    MuuOppimisymparisto
    (str "Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen "
         "liittyvät tiedot")))

(s/defschema
  NaytonJarjestaja
  (describe
    "Näytön tai osaamisen osoittamisen järjestäjä"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Näytön tai osaamisen osoittamisen järjestäjän nimi"
    (s/optional-key :oid) s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  Arvioija
  (describe
    "Arvioija"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Arvioijan nimi"
    :rooli KoodistoKoodi "Arvioijan roolin Koodisto-koodi"
    :organisaatio Organisaatio "Arvioijan organisaatio"))

(s/defschema
  HankitunOsaamisenNaytto
  (describe
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :jarjestaja NaytonJarjestaja "Näytön tai osaamisen osoittamisen järjestäjä"
    :nayttoymparisto Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    :kuvaus s/Str
    (str "Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen "
         "näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, "
         "esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, "
         "simulaattori")
    :ajankohta Aikavali "Näytön tai osaamisen osoittamisen ajankohta"
    :sisalto s/Str "Näytön tai osaamisen osoittamisen sisältö tai työtehtävät"
    (s/optional-key :ammattitaitovaatimukset) [s/Str]
    (str "Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. "
         "Tunnisteen tyyppi voi vielä päivittyä. "
         "Tulevaisuudessa, jos tarvitaan, tähän voidaan lisätä yksilölliset "
         "arviointikriteerit")
    :arvioijat [Arvioija] "Näytön tai osaamisen osoittamisen arvioijat"))

(s/defschema
  HankitunYTOOsaamisenNaytto
  "Hankitun YTO osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
  (st/assoc
    (st/dissoc
      HankitunOsaamisenNaytto
      :ammattitaitovaatimukset)
    (s/optional-key :osaamistavoitteet)
    (rsjs/describe
      [s/Str]
      (str "Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan."
           "Tunnisteen tyyppi voi vielä päivittyä ja tähän saattaa tulla vielä "
           "Yksilölliset arvioinnin kriteerit"))))

(s/defschema
  YhteisenTutkinnonOsanOsaAlue
  (describe
    "Puuttuvan yhteinen tutkinnon osan (YTO) osa-alueen tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :tunniste KoodistoKoodi "Koodisto-koodi"
    (s/optional-key :laajuus) s/Int "Tutkinnon laajuus ePerusteet palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    (s/optional-key :osaamisen-hankkimistavat) [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    (s/optional-key :hankitun-osaamisen-naytto) HankitunYTOOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :tarvittava-opetus) s/Str "Tarvittava opetus"))

(s/defschema
  YhteinenTutkinnonOsa
  (describe
    "Yhteinen Tutkinnon osa (YTO)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :osa-alueet [YhteisenTutkinnonOsanOsaAlue] "YTO osa-alueet"
    :tunniste KoodistoKoodi "Koodisto-koodi (tutkinnonosat)"
    (s/optional-key :laajuus) s/Int "Tutkinnon laajuus ePerusteet palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    (s/optional-key :kuvaus) s/Str
    "Tutkinnon osan kuvaus ePerusteet-palvelussa"
    (s/optional-key :pakollinen) s/Bool "Onko tutkinnon osa pakollinen vai ei"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  PuuttuvaYTO
  (modify
    YhteinenTutkinnonOsa
    "Puuttuvan yhteinen tutkinnon osan (YTO) tiedot"
    {:removed [:tarvittava-opetus
               :vaatimuksista-tai-tavoitteista-poikkeaminen
               :laajuus
               :nimi
               :kuvaus
               :pakollinen]}))

(s/defschema
  MuuTutkinnonOsa
  (describe
    "Muu tutkinnon osa (ei ePerusteet-palvelussa)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Tutkinnon osan nimi"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :laajuus s/Int "Tutkinnon osan laajuus osaamispisteissä"
    :kesto s/Int "Tutkinnon osan kesto päivinä"
    :suorituspvm LocalDate "Tutkinnon suorituspäivä muodossa YYYY-MM-DD"))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnot
  (describe
    "Opiskeluvalmiuksia tukevat opinnot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Opintojen nimi"
    :kuvaus s/Str "Opintojen kuvaus"
    :kesto s/Int "Opintojen kesto päivinä"
    :ajankohta Aikavali "Opintojen ajoittuminen"))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotLuonti
  (modify
    OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotPaivitys
  (modify
    OpiskeluvalmiuksiaTukevatOpinnot
    "Opiskeluvalmiuksia tukevien opintojen tiedot merkintää ylikirjoittaessa
     (PUT)"))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys
  (modify
    OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:nimi :kuvaus :kesto :ajankohta]}))

(s/defschema
  Arviointikriteeri
  (describe
    "Arviointikriteeri"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :osaamistaso s/Int "Osaamistaso"
    :kuvaus s/Str "Arviointikriteerin kuvaus"))

(s/defschema
  HankitunPaikallisenOsaamisenNaytto
  (describe
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :jarjestaja NaytonJarjestaja "Näytön tai osaamisen osoittamisen järjestäjä"
    :nayttoymparisto Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    :kuvaus s/Str
    (str "Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen "
         "näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, "
         "esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, "
         "simulaattori")
    :ajankohta Aikavali "Näytön tai osaamisen osoittamisen ajankohta"
    :sisalto s/Str "Näytön tai osaamisen osoittamisen sisältö tai työtehtävät"
    (s/optional-key :ammattitaitovaatimukset) [s/Str]
    (str "Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. "
         "Saattaa sisältää tulevaisuudessa yksilölliset arviointikriteerit.")
    :arvioijat [Arvioija] "Näytön tai osaamisen osoittamisen arvioijat"))

(s/defschema
  PuuttuvaAmmatillinenOsaaminen
  (describe
    "Puuttuvan ammatillisen osaamisen tiedot (GET)"
    :id s/Int "Tunniste eHOKS-järjestelmässä"
    :tutkinnon-osa TutkinnonOsa "Tutkinnon osa"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen"
    (s/optional-key :hankitun-osaamisen-naytto) HankitunOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    (s/optional-key :tarvittava-opetus) s/Str "Tarvittava opetus"))

(s/defschema
  PuuttuvaAmmatillinenOsaaminenLuonti
  (modify
    PuuttuvaAmmatillinenOsaaminen
    "Puuttuvan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)"
    {:removed [:id]}))

(s/defschema
  PuuttuvaAmmatillinenOsaaminenPaivitys
  (modify
    PuuttuvaAmmatillinenOsaaminen
    "Puuttuvan ammatillisen osaamisen tiedot merkintää ylikirjoittaessa (PUT)"))

(s/defschema
  PuuttuvaAmmatillinenOsaaminenKentanPaivitys
  (modify
    PuuttuvaAmmatillinenOsaaminen
    (str "Puuttuvan ammatillisen osaamisen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:tutkinnon-osa :osaamisen-hankkimistavat :koulutuksen-jarjestaja-oid]}))

(s/defschema
  PuuttuvaYTOLuonti
  (modify
    PuuttuvaYTO
    (str "Puuttuvan yhteinen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  PuuttuvaYTOPaivitys
  (modify
    PuuttuvaYTO
    (str "Puuttuvan yhteinen tutkinnon osa tiedot merkintää "
         "ylikirjoittaessa (PUT)")))

(s/defschema
  PuuttuvaYTOKentanPaivitys
  (modify
    PuuttuvaYTO
    (str "Puuttuvan yhteinen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:osa-alueet :koulutuksen-jarjestaja-oid]}))

(s/defschema
  PaikallinenTutkinnonOsa
  (describe
    "Puuttuva paikallinen tutkinnon osa"
    :id s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :amosaa-tunniste) s/Int
    "Tunniste ePerusteet AMOSAA -palvelussa"
    :nimi s/Str "Tutkinnon osan nimi"
    :laajuus s/Int "Tutkinnon osan laajuus"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    :hankitun-osaamisen-naytto HankitunPaikallisenOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :tarvittava-opetus s/Str "Tarvittava opetus"))

(s/defschema
  PaikallinenTutkinnonOsaLuonti
  (modify
    PaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  PaikallinenTutkinnonOsaPaivitys
  (modify
    PaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot merkintää "
         "ylikirjoittaessa (PUT)")))

(s/defschema
  PaikallinenTutkinnonOsaKentanPaivitys
  (modify
    PaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:nimi :laajuus :kuvaus :osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid :hankitun-osaamisen-naytto
      :tarvittava-opetus]}))

(s/defschema
  Tutkinto
  (describe
    "Tutkinnon perustiedot ePerusteet järjestelmässä"
    :laajuus s/Int "Tutkinnon laajuus"
    :nimi s/Str "Tutkinnon nimi"))

(s/defschema
  Opiskeluoikeus
  (describe
    "Opiskeluoikeuden tiedot Koski-järjestelmässä"
    :oid s/Str "Opinto-oikeuden tunniste Opintopolku-ympäristössä"
    :tutkinto Tutkinto "Opinto-oikeuden tutkinto"))

(s/defschema
  OlemassaOlevaAmmatillinenOsaaminen
  (describe
    (str "Ammatillinen osaaminen, joka osaamisen tunnustamisen perusteella
    sisällytetty suoraan osaksi "
         "opiskelijan tutkintoa")

    (s/optional-key :tutkinnon-tunniste) KoodistoKoodi
    (str "Tutkinnon osan, johon tunnistettava olemassa oleva osaaminen "
         "liittyy, Koodisto-koodi")
    :valittu-todentamisen-prosessi
    (s/enum :valittu-todentaminen-suoraan
            :valittu-todentaminen-arvioijat
            :valittu-todentaminen-naytto)
    "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
    (s/optional-key :tarkentavat-tiedot) [HankitunOsaamisenNaytto]
    "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."))

(s/defschema
  TunnustettavanaOlevaOsaaminen
  (describe
    "Osaaminen, joka on toimitettu arvioijille osaamisen tunnustamista varten"
    :todentajan-nimi s/Str
    "Osaamisen todentaneen toimivaltaisen viranomaisen nimi"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :ammatilliset-opinnot) [TutkinnonOsa]
    "Osaamisen ammattilliset opinnot"
    (s/optional-key :yhteiset-tutkinnon-osat) [YhteinenTutkinnonOsa]
    "Osaamisen yhteiset tutkinnon osat (YTO)"
    (s/optional-key :paikalliset-osaamiset) [PaikallinenTutkinnonOsa]
    "Osaamisen paikallisen tutkinnon osat"))

(def HOKSModel
  ^{:doc "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti"
    :restful true
    :name "HOKSModel"}
  {:id {:methods {:post :excluded}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :oppija-oid {:methods {:patch :optional}
                :types {:any s/Str}
                :description "Oppijan tunniste Opintopolku-ympäristössä"}
   :opiskeluoikeus
   {:methods {:patch :optional}
    :types {:any Opiskeluoikeus}
    :description "Opiskeluoikeuden tiedot Koski-järjestelmässä"}
   :urasuunnitelma {:methods {:any :optional}
                    :types {:any KoodistoKoodiLuonti
                            :get KoodistoKoodi}
                    :description
                    "Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi"}
   :versio {:methods {:any :excluded
                      :get :required}
            :types {:any s/Int}
            :description "HOKS-dokumentin versio"}
   :luonut {:methods {:patch :optional
                      :put :excluded}
            :types {:any s/Str}
            :description "HOKS-dokumentin luoneen henkilön nimi"}
   :paivittanyt {:methods {:patch :optional}
                 :types {:any s/Str}
                 :description
                 "HOKS-dokumenttia viimeksi päivittäneen henkilön nimi"}
   :hyvaksynyt {:methods {:patch :optional}
                :types {:any s/Str}
                :description "Luodun HOKS-dokumentn hyväksyjän nimi"}
   :luotu {:methods {:any :excluded
                     :get :required}
           :types {:any s/Inst}
           :description
           "HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"}
   :olemassa-oleva-ammatillinen-osaaminen
   {:methods {:any :optional}
    :types {:any [OlemassaOlevaAmmatillinenOsaaminen]}
    :description "Olemassa oleva ammatillinen osaaminen"}
   :olemassa-olevat-yhteiset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [YhteinenTutkinnonOsa]}
    :description "Olemassa olevat yhteiset tutkinnon osat (YTO)"}
   :olemassa-oleva-paikallinen-tutkinnon-osa
   {:methods {:any :optional}
    :types {:any [PaikallinenTutkinnonOsa]}
    :description "Olemassa oleva paikallinen tutkinnon osa"}
   :hyvaksytty
   {:methods {:patch :excluded
              :post :excluded
              :get :required}
    :types {:any s/Inst}
    :description
    "HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"}
   :paivitetty {:methods {:any :excluded
                          :get :required}
                :types {:any s/Inst}
                :description (str "HOKS-dokumentin viimeisin päivitysaika "
                                  "muodossa YYYY-MM-DDTHH:mm:ss.sssZ")}
   :opiskeluvalmiuksia-tukevat-opinnot
   {:methods {:any :optional}
    :types {:any OpiskeluvalmiuksiaTukevatOpinnot}
    :description "Opiskeluvalmiuksia tukevat opinnot"}
   :puuttuva-ammatillinen-osaaminen
   {:methods {:any :optional}
    :types {:any [PuuttuvaAmmatillinenOsaaminen]}
    :description
    "Puuttuvan ammatillisen osaamisen hankkimisen tiedot"}
   :puuttuva-yhteisen-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [PuuttuvaYTO]}
    :description "Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot"}
   :puuttuva-paikallinen-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [PaikallinenTutkinnonOsa]}
    :description "Puuttuvat paikallisen tutkinnon osat"}})

; Following four schemas are only for generated markdown doc

(def HOKS
  (with-meta
    (g/generate HOKSModel :get)
    {:doc "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)"
     :name "HOKS"}))

(def HOKSPaivitys
  (with-meta
    (g/generate HOKSModel :put)
    {:doc "HOKS-dokumentin ylikirjoitus (PUT)"
     :name "HOKSPaivitys"}))

(def HOKSKentanPaivitys
  (with-meta
    (g/generate HOKSModel :patch)
    {:doc "HOKS-dokumentin ylikirjoitus (PATCH)"
     :name "HOKSKentanPaivitys"}))

(def HOKSLuonti
  (with-meta
    (g/generate HOKSModel :post)
    {:doc "HOKS-dokumentin arvot uutta merkintää luotaessa (POST)"
     :name "HOKSLuonti"}))
