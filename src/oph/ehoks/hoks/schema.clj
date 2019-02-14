(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]
            [schema-tools.core :as st]
            [ring.swagger.json-schema :as rsjs]
            [oph.ehoks.schema-tools :refer [describe modify]]
            [oph.ehoks.schema.generator :as g]
            [oph.ehoks.common.schema :as common-schema])
  (:import (java.time LocalDate)))

(def TutkinnonOsaKoodiUri
  "Tutkinnon osan Koodisto-koodi-URI ePerusteet palvelussa (tutkinnonosa_1234)."
  #"^tutkinnonosat_\d+$")

(def OsaamisenHankkimistapaKoodiUri
  #"^osaamisenhankkimistapa_\d+$")

(s/defschema
  Organisaatio
  (describe
    "Organisaatio"
    :nimi s/Str "Organisaation nimi"
    (s/optional-key :y-tunnus) s/Str "Organisaation y-tunnus"))

(s/defschema
  TutkinnonOsa
  (describe
    "Tutkinnon osa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat)"))

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
    (s/optional-key :rooli) s/Str "Henkilön rooli"))

(s/defschema
  VastuullinenOhjaaja
  (modify
    Henkilo
    "Vastuullinen ohjaaja"
    {:removed [:organisaatio]}))

(s/defschema
  Oppilaitoshenkilo
  (modify
    Henkilo
    "Oppilaitoksen edustaja"
    {:removed [:organisaatio]
     :added
     (describe
       ""
       :oppilaitoksen-oid s/Str
       "Oppilaitoksen oid-tunniste Opintopolku-palvelussa.")}))

(s/defschema
  TyopaikallaHankittavaOsaaminen
  (describe
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :vastuullinen-ohjaaja VastuullinenOhjaaja "Vastuullinen työpaikkaohjaaja"
    :tyopaikan-nimi s/Str "Työpaikan nimi"
    (s/optional-key :tyopaikan-y-tunnus) s/Str "Työpaikan y-tunnus"
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
    :tarkenne-koodi-uri s/Str
    "Oppimisympäristön tarkenne, eHOKS Koodisto-koodi-URI"
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
    :koodi-uri OsaamisenHankkimistapaKoodiUri
    "Osaamisen hankkimisen Koodisto-koodi-URI (osaamisenhankkimistapa)"
    (s/optional-key :jarjestajan-edustaja) Oppilaitoshenkilo
    "Koulutuksen järjestäjän edustaja"
    (s/optional-key :hankkijan-edustaja) Oppilaitoshenkilo
    "Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja"
    (s/optional-key :tyopaikalla-hankittava-osaaminen)
    TyopaikallaHankittavaOsaaminen
    (str "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot. "
         "Tämä tieto tuodaan, jos hankkimistapa on oppisopimuskoulutus tai "
         "koulutussopimus.")
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
    :rooli s/Str "Arvioijan roolin Koodisto-koodi-URI"
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
    :koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat)"
    (s/optional-key :osaamisen-hankkimistavat) [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    (s/optional-key :hankitun-osaamisen-naytto) HankitunYTOOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"))

(s/defschema
  OlemassaOlevanYTOOsaAlue
  (describe
    "Olemassaolevan YTOn osa-alueen tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :koodi-uri s/Str "Osa-alueen Koodisto-koodi-URI"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    :valittu-todentamisen-prosessi
    (s/enum :valittu-todentaminen-suoraan
            :valittu-todentaminen-arvioijat
            :valittu-todentaminen-naytto)
    "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
    (s/optional-key :tarkentavat-tiedot) [HankitunOsaamisenNaytto]
    "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."))

(s/defschema
  YhteinenTutkinnonOsa
  (describe
    "Yhteinen Tutkinnon osa (YTO)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :osa-alueet [YhteisenTutkinnonOsanOsaAlue] "YTO osa-alueet"
    :koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat)"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  PuuttuvaYTO
  (modify
    YhteinenTutkinnonOsa
    "Puuttuvan yhteinen tutkinnon osan (YTO) tiedot"
    {:removed [:vaatimuksista-tai-tavoitteista-poikkeaminen]}))

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
    (str "Tekstimuotoinen selite ammattitaitovaatimuksista tai "
         "osaamistavoitteista poikkeamiseen")
    (s/optional-key :hankitun-osaamisen-naytto) HankitunOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

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
     [:osa-alueet :koulutuksen-jarjestaja-oid :koodi-uri]}))

(s/defschema
  PuuttuvaPaikallinenTutkinnonOsa
  (describe
    "Puuttuva paikallinen tutkinnon osa"
    :id s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :amosaa-tunniste) Long
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
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"))

(s/defschema
  OlemassaOlevaPaikallinenTutkinnonOsa
  (modify
    PuuttuvaPaikallinenTutkinnonOsa
    "Olemassa oleva paikallinen tutkinnon osa"
    {:removed [:osaamisen-hankkimistavat
               :hankitun-osaamisen-naytto]
     :added
     (describe
       ""
       {:valittu-todentamisen-prosessi
        (s/enum :valittu-todentaminen-suoraan
                :valittu-todentaminen-arvioijat
                :valittu-todentaminen-naytto)
        "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
        (s/optional-key :tarkentavat-tiedot) [HankitunOsaamisenNaytto]
        "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."})}))

(s/defschema
  PuuttuvaPaikallinenTutkinnonOsaLuonti
  (modify
    PuuttuvaPaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:id]}))

(s/defschema
  PuuttuvaPaikallinenTutkinnonOsaPaivitys
  (modify
    PuuttuvaPaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot merkintää "
         "ylikirjoittaessa (PUT)")))

(s/defschema
  PuuttuvaPaikallinenTutkinnonOsaKentanPaivitys
  (modify
    PuuttuvaPaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid
      :hankitun-osaamisen-naytto
      :kuvaus
      :laajuus
      :nimi]}))

(s/defschema
  OlemassaOlevaAmmatillinenTutkinnonOsa
  (describe
    (str
      "Ammatillinen osaaminen, joka osaamisen tunnustamisen perusteella "
      "sisällytetty suoraan osaksi opiskelijan tutkintoa.")
    :koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat)"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    :valittu-todentamisen-prosessi
    (s/enum :valittu-todentaminen-suoraan
            :valittu-todentaminen-arvioijat
            :valittu-todentaminen-naytto)
    "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
    (s/optional-key :tarkentavat-tiedot) [HankitunOsaamisenNaytto]
    "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."))

(s/defschema
  OlemassaOlevaYhteinenTutkinnonOsa
  (describe
    "Yhteinen Tutkinnon osa (YTO)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :osa-alueet [OlemassaOlevanYTOOsaAlue]
    "OlemassaOlevanYhteisenTutkinnonOsanOsa:n osa-alueet"
    :koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon Koodisto-koodi-URI ePerusteet-palvelussa (tutkinnonosat)"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    :valittu-todentamisen-prosessi
    (s/enum :valittu-todentaminen-suoraan
            :valittu-todentaminen-arvioijat
            :valittu-todentaminen-naytto)
    "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
    (s/optional-key :tarkentavat-tiedot) [HankitunOsaamisenNaytto]
    "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."))

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
   :opiskeluoikeus-oid
   {:methods {:patch :optional}
    :types {:any s/Str}
    :description "Opiskeluoikeuden oid-tunniste Koski-järjestelmässä"}
   :tutkinto {:methods {:get :optional
                        :any :excluded}
              :types {:any common-schema/Tutkinto}
              :description "Tutkinnon tiedot ePerusteet palvelussa"}
   :urasuunnitelma
   {:methods {:any :optional}
    :types {:any s/Str}
    :description "Opiskelijan tavoitteen Koodisto-koodi-URI"}
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
   :olemassa-olevat-ammatilliset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [OlemassaOlevaAmmatillinenTutkinnonOsa]}
    :description "Olemassa oleva ammatillinen osaaminen"}
   :olemassa-olevat-yhteiset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [OlemassaOlevaYhteinenTutkinnonOsa]}
    :description "Olemassa olevat yhteiset tutkinnon osat (YTO)"}
   :olemassa-oleva-paikallinen-tutkinnon-osa
   {:methods {:any :optional}
    :types {:any [OlemassaOlevaPaikallinenTutkinnonOsa]}
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
   :puuttuva-ammatillinen-tutkinnon-osa
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
    :types {:any [PuuttuvaPaikallinenTutkinnonOsa]}
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

(s/defschema
  OppijaHOKS
  (modify
    HOKS
    "Oppijan HOKS"
    {:replaced-in {[:urasuunnitelma] common-schema/KoodistoKoodi}}))
