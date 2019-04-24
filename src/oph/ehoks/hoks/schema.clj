(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]
            [schema-tools.core :as st]
            [ring.swagger.json-schema :as rsjs]
            [oph.ehoks.schema-tools :refer [describe modify]]
            [oph.ehoks.schema.generator :as g]
            [oph.ehoks.common.schema :as common-schema])
  (:import (java.time LocalDate)))

(def TutkinnonOsaKoodiUri
  "Tutkinnon osan Koodisto-koodi-URI ePerusteet palvelussa (tutkinnonosat)."
  #"^tutkinnonosat_\d+$")

(def OsaamisenHankkimistapaKoodiUri
  #"^osaamisenhankkimistapa_.+$")

(def OsaAlueKoodiUri
  #"^ammatillisenoppiaineet_.+$")

(def TodentamisenProsessiKoodiUri
  "Valitun todentamisen prosessin Koodisto-koodi-URI"
  #"^osaamisentodentamisenprosessi_\d+$")

(def UrasuunnitelmaKoodiUri
  #"^urasuunnitelma_\d{4}$")

(def Oid
  #"^1\.2\.246\.562\.[0-3]\d\.\d{11}$")

(def OpiskeluoikeusOid
  #"^1\.2\.246\.562\.15\.\d{11}$")

(s/defschema
  KoodistoKoodi
  (describe
    "Koodisto Koodi"
    :koodi-uri s/Str "Koodisto-koodi URI"
    :koodi-versio s/Int "Koodisto-koodin versio"))

(s/defschema
  Organisaatio
  (describe
    "Organisaatio"
    :nimi s/Str "Organisaation nimi"
    (s/optional-key :y-tunnus) s/Str "Mikäli organisaatiolla on y-tunnus,
    organisaation y-tunnus"))

(s/defschema
  TyoelamaOrganisaatio
  (modify
    Organisaatio
    "Työelämän toimijan organisaatio, jossa näyttö tai osaamisen osoittaminen
     annetaan"))

(s/defschema
  KoulutuksenJarjestajaOrganisaatio
  (modify
    Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    {:removed [:nimi :y-tunnus]
     :added
     (describe
       ""
       (s/optional-key :oppilaitos-oid) Oid
       "Mikäli kyseessä oppilaitos, oppilaitoksen oid-tunniste
       Opintopolku-palvelussa.")}))

(s/defschema
  Nayttoymparisto
  (modify
    Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    {:added
     (describe
       ""
       (s/optional-key :kuvaus) s/Str
       (str "Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen "
            "näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, "
            "esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, "
            "simulaattori"))}))

(s/defschema
  Henkilo
  (describe
    "Henkilö"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :organisaatio Organisaatio "Henkilön organisaatio"
    :nimi s/Str "Henkilön nimi"
    (s/optional-key :rooli) s/Str "Henkilön rooli"))

(s/defschema
  HoksToimija
  (modify
    Henkilo
    "Hoksin hyväksyjä tai päivittäjä koulutusjärjestäjän organisaatiossa"
    {:removed [:organisaatio :rooli :id]}))

(s/defschema
  VastuullinenOhjaaja
  (modify
    Henkilo
    "Vastuullinen ohjaaja"
    {:removed [:organisaatio :rooli]
     :added
     (describe
       ""
       (s/optional-key :sahkoposti) s/Str
       "Vastuullisen ohjaajan sähköpostiosoite")}))

(s/defschema
  Oppilaitoshenkilo
  (modify
    Henkilo
    "Oppilaitoksen edustaja"
    {:removed [:organisaatio]
     :added
     (describe
       ""
       :oppilaitos-oid Oid
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
    :lisatiedot s/Bool
    "Lisätietoja, esim. opiskelijalla tunnistettu ohjauksen ja tuen tarvetta"))

(s/defschema
  MuuOppimisymparisto
  (describe
    "Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu"
    :oppimisymparisto-koodi-uri s/Str
    "Oppimisympäristön tarkenne, eHOKS Koodisto-koodi-URI, koodisto
    oppimisympäristöt eli muotoa oppimisymparistot_xxxx, esim.
    oppimisymparistot_0001"
    :oppimisymparisto-koodi-versio s/Int
    "Koodisto-koodin versio, koodistolle oppimisympäristöt"
    :selite s/Str "Oppimisympäristön nimi"
    :lisatiedot s/Bool
    "Lisätiedoisssa, onko tunnistettu ohjauksen ja tuen tarvetta"))

(s/defschema
  OsaamisenHankkimistapa
  (describe
    "Osaamisen hankkimisen tapa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :alku LocalDate "Alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Loppupäivämäärä muodossa YYYY-MM-DD"
    (s/optional-key :ajanjakson-tarkenne) s/Str
    "Tarkentava teksti ajanjaksolle, jos useita aikavälillä."
    :osaamisen-hankkimistapa-koodi-uri OsaamisenHankkimistapaKoodiUri
    "Osaamisen hankkimisen Koodisto-koodi-URI (osaamisenhankkimistapa)
    eli muotoa osaamisenhankkimistapa_xxx eli esim.
    osaamisenhankkimistapa_koulutussopimus"
    :osaamisen-hankkimistapa-koodi-versio s/Int
    "Koodisto-koodin versio, koodistolle osaamisenhankkimistapa"
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
    [MuuOppimisymparisto]
    (str "Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen "
         "liittyvät tiedot")))

(s/defschema
  NaytonJarjestaja
  (describe
    "Näytön tai osaamisen osoittamisen järjestäjä"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :oppilaitos-oid) Oid
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  Arvioija
  (describe
    "Arvioija"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Arvioijan nimi"
    :organisaatio Organisaatio "Arvioijan organisaatio"))

(s/defschema
  TyoelamaArvioija
  (modify
    Arvioija
    "Työelämän arvioija"
    {:removed [:organisaatio]
     :added
     (describe
       ""
       :organisaatio TyoelamaOrganisaatio "Työelämän arvioijan organisaatio")}))

(s/defschema
  KoulutuksenJarjestajaArvioija
  (modify
    Arvioija
    "Työelämän arvioija"
    {:removed [:organisaatio]
     :added
     (describe
       ""
       :organisaatio KoulutuksenJarjestajaOrganisaatio
       "KoulutuksenJarjestajan arvioijan organisaatio")}))

(s/defschema
  HankitunOsaamisenNaytto
  (describe
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen
    osoittaminen"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :jarjestaja) NaytonJarjestaja
    "Näytön tai osaamisen osoittamisen järjestäjä"
    (s/optional-key :osa-alueet) [KoodistoKoodi]
    (str "Suoritettavan tutkinnon osan näyttöön sisältyvän"
         "yton osa-alueiden Koodisto-koodi-URIt
         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxx"
         "esim. ammatillisenoppiaineet_etk")
    :nayttoymparisto Nayttoymparisto
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    (s/optional-key :keskeiset-tyotehtavat-naytto) [s/Str] "Keskeiset
    työtehtävät"
    :alku LocalDate
    "Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa
    YYYY-MM-DD"
    :loppu LocalDate
    "Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa
    YYYY-MM-DD"
    (s/optional-key :koulutuksen-jarjestaja-arvioijat)
    [KoulutuksenJarjestajaArvioija] "Näytön tai osaamisen osoittamisen
    arvioijat"
    (s/optional-key :tyoelama-arvioijat) [TyoelamaArvioija] "Näytön tai
    osaamisen osoittamisen arvioijat"))

(s/defschema
  HankitunPaikallisenOsaamisenNaytto
  (modify
    HankitunOsaamisenNaytto
    "Hankitun paikallisen osaamisen osoittaminen: Näyttö tai muu osaamisen
     osoittaminen"))

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
    :osa-alue-koodi-uri OsaAlueKoodiUri
    "Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet)"
    :osa-alue-koodi-versio s/Int
    "Osa-alueen Koodisto-koodi-URIn versio (ammatillisenoppiaineet)"
    (s/optional-key :osaamisen-hankkimistavat) [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    (s/optional-key :hankitun-osaamisen-naytto) [HankitunYTOOsaamisenNaytto]
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"))

(s/defschema
  OlemassaOlevanYTOOsaAlue
  (describe
    "Olemassaolevan YTOn osa-alueen tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :osa-alue-koodi-uri OsaAlueKoodiUri
    "Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet)"
    :osa-alue-koodi-versio s/Int
    "Osa-alueen Koodisto-koodi-URIn versio (ammatillisenoppiaineet)"
    (s/optional-key :koulutuksen-jarjestaja-oid) Oid
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    :valittu-todentamisen-prosessi-koodi-uri TodentamisenProsessiKoodiUri
    "Todentamisen prosessin kuvauksen (suoraan/arvioijien kautta/näyttö)
    koodi-uri. Koodisto Osaamisen todentamisen prosessi, eli muotoa
    osaamisentodentamisenprosessi_xxxx"
    :valittu-todentamisen-prosessi-koodi-versio s/Int
    "Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio
    (Osaamisen todentamisen prosessi)"
    (s/optional-key :tarkentavat-tiedot) [HankitunOsaamisenNaytto]
    "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."))

(s/defschema
  YhteinenTutkinnonOsa
  (describe
    "Yhteinen Tutkinnon osa (YTO)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :osa-alueet [YhteisenTutkinnonOsanOsaAlue] "YTO osa-alueet"
    :tutkinnon-osa-koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa
    (tutkinnonosat) eli muotoa  tutkinnonosat_xxxxxx eli esim.
    tutkinnonosat_100002"
    :tutkinnon-osa-koodi-versio s/Int
    "Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa
     (tutkinnonosat)"
    (s/optional-key :koulutuksen-jarjestaja-oid) Oid
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
  OpiskeluvalmiuksiaTukevatOpinnot
  (describe
    "Opiskeluvalmiuksia tukevat opinnot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Opintojen nimi"
    :kuvaus s/Str "Opintojen kuvaus"
    :alku LocalDate "Opintojen alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Opintojen loppupäivämäärä muodossa YYYY-MM-DD"))

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
     [:nimi :kuvaus :kesto :alku :loppu]}))

(s/defschema
  PuuttuvaAmmatillinenOsaaminen
  (describe
    "Puuttuvan ammatillisen osaamisen tiedot (GET)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :tutkinnon-osa-koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat)"
    :tutkinnon-osa-koodi-versio s/Int
    "Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa
     (tutkinnonosat)"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    (str "Tekstimuotoinen selite ammattitaitovaatimuksista tai "
         "osaamistavoitteista poikkeamiseen")
    (s/optional-key :hankitun-osaamisen-naytto) [HankitunOsaamisenNaytto]
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :koulutuksen-jarjestaja-oid) Oid
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
     [:tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio
      :osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid]}))

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
     [:osa-alueet :koulutuksen-jarjestaja-oid :tutkinnon-osa-koodi-uri
      :tutkinnon-osa-koodi-versio]}))

(s/defschema
  PuuttuvaPaikallinenTutkinnonOsa
  (describe
    "Puuttuva paikallinen tutkinnon osa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :amosaa-tunniste) s/Str
    "Tunniste ePerusteet AMOSAA -palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi"
    (s/optional-key :laajuus) s/Int "Tutkinnon osan laajuus"
    (s/optional-key :tavoitteet-ja-sisallot) s/Str
    (str "Paikallisen tutkinnon osan ammattitaitovaatimukset tai"
         "osaamistavoitteet")
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :koulutuksen-jarjestaja-oid) Oid
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    :hankitun-osaamisen-naytto [HankitunPaikallisenOsaamisenNaytto]
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"))

(s/defschema
  TodennettuArviointiLisatiedot
  (describe
    "Mikäli arvioijan kautta todennettu, annetaan myös arvioijan lisätiedot"
    (s/optional-key :lahetetty-arvioitavaksi) LocalDate "Päivämäärä, jona
    lähetetty arvioitavaksi, muodossa YYYY-MM-DD"
    (s/optional-key :aiemmin-hankitun-osaamisen-arvioijat)
    [KoulutuksenJarjestajaArvioija]
    "Mikäli todennettu arvioijan kautta, annetaan arvioijien tiedot."))

(s/defschema
  OlemassaOlevaPaikallinenTutkinnonOsa
  (modify
    PuuttuvaPaikallinenTutkinnonOsa
    "Olemassa oleva yhteinen tutkinnon osa"
    {:removed [:osaamisen-hankkimistavat :hankitun-osaamisen-naytto]
     :added
     (describe
       ""
       :valittu-todentamisen-prosessi-koodi-uri TodentamisenProsessiKoodiUri
       "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
       :valittu-todentamisen-prosessi-koodi-versio s/Int
       "Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio
       (Osaamisen todentamisen prosessi)"
       (s/optional-key :tarkentavat-tiedot-naytto) [HankitunOsaamisenNaytto]
       "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."
       (s/optional-key :tarkentavat-tiedot-arvioija)
       TodennettuArviointiLisatiedot "Mikäli arvioijan kautta todennettu,
       annetaan myös arvioijan lisätiedot")}))

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
  OlemassaOlevaYhteinenTutkinnonOsa
  (modify
    YhteinenTutkinnonOsa
    "Olemassa oleva yhteinen tutkinnon osa"
    {:removed [:osa-alueet]
     :added
     (describe
       ""
       :osa-alueet [OlemassaOlevanYTOOsaAlue] "YTO osa-alueet"
       :valittu-todentamisen-prosessi-koodi-uri TodentamisenProsessiKoodiUri
       "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
       :valittu-todentamisen-prosessi-koodi-versio s/Int
       "Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio
       (Osaamisen todentamisen prosessi)"
       (s/optional-key :tarkentavat-tiedot-naytto) [HankitunOsaamisenNaytto]
       "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."
       (s/optional-key :tarkentavat-tiedot-arvioija)
       TodennettuArviointiLisatiedot "Mikäli arvioijan kautta todennettu,
       annetaan myös arvioijan lisätiedot")}))

(s/defschema
  OlemassaOlevaAmmatillinenTutkinnonOsa
  (modify
    OlemassaOlevaYhteinenTutkinnonOsa
    "Olemassa oleva yhteinen tutkinnon osa"
    {:removed [:osa-alueet]}))

(def HOKSModel
  ^{:doc "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti"
    :restful true
    :name "HOKSModel"}
  {:id {:methods {:post :excluded}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :eid {:methods {:any :excluded
                   :get :required}
         :types {:any s/Str}
         :description "HOKSin generoitu ulkoinen tunniste eHOKS-järjestelmässä"}
   :oppija-oid {:methods {:patch :optional}
                :types {:any Oid}
                :description "Oppijan tunniste Opintopolku-ympäristössä"}
   :sahkoposti {:methods {:any :optional}
                :types {:any s/Str}
                :description "Oppijan sähköposti, merkkijono."}
   :opiskeluoikeus-oid
   {:methods {:patch :optional}
    :types {:any OpiskeluoikeusOid}
    :description "Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa
                  '1.2.246.562.15.00000000001'"}
   :tutkinto {:methods {:get :optional
                        :any :excluded}
              :types {:any common-schema/Tutkinto}
              :description "Tutkinnon tiedot ePerusteet palvelussa"}
   :urasuunnitelma-koodi-uri
   {:methods {:any :optional}
    :types {:any UrasuunnitelmaKoodiUri}
    :description "Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto
    Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim.
    urasuunnitelma_0001"}
   :urasuunnitelma-koodi-versio
   {:methods {:any :optional}
    :types {:any s/Int}
    :description "Opiskelijan tavoitteen Koodisto-koodin versio"}
   :versio {:methods {:any :optional}
            :types {:any s/Int}
            :description "HOKS-dokumentin versio"}
   :laatija {:methods {:any :optional}
             :types {:any HoksToimija}
             :description "HOKS-dokumentin luoneen henkilön nimi"}
   :paivittaja {:methods {:post :optional
                          :patch :required}
                :types {:any HoksToimija}
                :description
                "HOKS-dokumenttia viimeksi päivittäneen henkilön nimi"}
   :hyvaksyja {:methods {:patch :optional}
               :types {:any HoksToimija}
               :description "Luodun HOKS-dokumentn hyväksyjän nimi"}
   :luotu {:methods {:any :optional}
           :types {:any s/Inst}
           :description
           "HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"}
   :ensikertainen-hyvaksyminen {:methods {:patch :optional}
                                :types {:any LocalDate}
                                :description
                                "HOKS-dokumentin ensimmäinen hyväksymisaika
                                muodossa YYYY-MM-DD"}
   :olemassa-olevat-ammatilliset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [OlemassaOlevaAmmatillinenTutkinnonOsa]}
    :description "Olemassa oleva ammatillinen osaaminen"}
   :olemassa-olevat-yhteiset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [OlemassaOlevaYhteinenTutkinnonOsa]}
    :description "Olemassa olevat yhteiset tutkinnon osat (YTO)"}
   :olemassa-olevat-paikalliset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [OlemassaOlevaPaikallinenTutkinnonOsa]}
    :description "Olemassa oleva paikallinen tutkinnon osa"}
   :hyvaksytty
   {:methods {:any :optional}
    :types {:any s/Inst}
    :description
    "HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"}
   :paivitetty {:methods {:any :optional}
                :types {:any s/Inst}
                :description (str "HOKS-dokumentin viimeisin päivitysaika "
                                  "muodossa YYYY-MM-DDTHH:mm:ss.sssZ")}
   :opiskeluvalmiuksia-tukevat-opinnot
   {:methods {:any :optional}
    :types {:any [OpiskeluvalmiuksiaTukevatOpinnot]}
    :description "Opiskeluvalmiuksia tukevat opinnot"}
   :puuttuvat-ammatilliset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [PuuttuvaAmmatillinenOsaaminen]}
    :description
    "Puuttuvan ammatillisen osaamisen hankkimisen tiedot"}
   :puuttuvat-yhteiset-tutkinnon-osat
   {:methods {:any :optional}
    :types {:any [PuuttuvaYTO]}
    :description "Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot"}
   :puuttuvat-paikalliset-tutkinnon-osat
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
