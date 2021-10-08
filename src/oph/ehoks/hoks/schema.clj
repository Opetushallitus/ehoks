(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]
            [oph.ehoks.schema-tools :refer [describe modify]]
            [oph.ehoks.schema.generator :as g])
  (:import (java.time LocalDate)
           (java.util UUID)))

(def TutkinnonOsaKoodiUri
  "Tutkinnon osan Koodisto-koodi-URI ePerusteet palvelussa (tutkinnonosat)."
  #"^tutkinnonosat_\d+$")

(def OsaamisenHankkimistapaKoodiUri
  #"^osaamisenhankkimistapa_.+$")

(def OppisopimuksenPerustaKoodiUri
  #"^oppisopimuksenperusta_.+$")

(def OsaAlueKoodiUri
  #"^ammatillisenoppiaineet_.+$")

(def OppimisymparistoKoodiUri
  #"^oppimisymparistot_\d{4}$")

(def TodentamisenProsessiKoodiUri
  "Valitun todentamisen prosessin Koodisto-koodi-URI"
  #"^osaamisentodentamisenprosessi_\d+$")

(def UrasuunnitelmaKoodiUri
  #"^urasuunnitelma_\d{4}$")

(def Oid
  #"^1\.2\.246\.562\.[0-3]\d\.\d+$")

(def OpiskeluoikeusOid
  #"^1\.2\.246\.562\.15\.\d+$")

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
  VastuullinenTyopaikkaOhjaaja
  (modify
    Henkilo
    "Vastuullinen ohjaaja"
    {:removed [:organisaatio :rooli]
     :added
     (describe
       ""
       (s/optional-key :sahkoposti) s/Str
       "Vastuullisen ohjaajan sähköpostiosoite"
       (s/optional-key :puhelinnumero) s/Str
       "Vastuullisen ohjaajan puhelinnumero")}))

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
  TyopaikallaJarjestettavaKoulutus
  (describe
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :vastuullinen-tyopaikka-ohjaaja VastuullinenTyopaikkaOhjaaja "Vastuullinen
    työpaikkaohjaaja"
    :tyopaikan-nimi s/Str "Työpaikan nimi"
    (s/optional-key :tyopaikan-y-tunnus) s/Str "Työpaikan y-tunnus"
    :keskeiset-tyotehtavat [s/Str] "Keskeiset työtehtävät"))

(s/defschema
  MuuOppimisymparisto
  (describe
    "Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu"
    :oppimisymparisto-koodi-uri OppimisymparistoKoodiUri
    "Oppimisympäristön tarkenne, eHOKS Koodisto-koodi-URI, koodisto
    oppimisympäristöt eli muotoa oppimisymparistot_xxxx, esim.
    oppimisymparistot_0001"
    :oppimisymparisto-koodi-versio s/Int
    "Koodisto-koodin versio, koodistolle oppimisympäristöt"
    :alku LocalDate
    "Muussa oppimisympäristössä tapahtuvan osaamisen hankkimisen
    aloituspäivämäärä."
    :loppu LocalDate
    "Muussa oppimisympäristössä tapahtuvan osaamisen hankkimisen
    päättymispäivämäärä."))

(s/defschema
  Keskeytymisajanjakso
  (describe
    (str "Ajanjakso, jolloin tutkinnon osan osaamisen hankkiminen kyseisellä "
         "työpaikalla on ollut keskeytyneenä.")
    :alku LocalDate
    "Keskeytymisajanjakson aloituspäivämäärä."
    (s/optional-key :loppu) LocalDate
    "Keskeytymisajanjakson päättymispäivämäärä."))

(defn- not-overlapping? [jaksot]
  (or (<= (count jaksot) 1)
      (reduce #(if (and (:loppu %1) (.isBefore (:loppu %1) (:alku %2)))
                 %2
                 (reduced false))
              (sort-by :alku (seq jaksot)))))

(s/defschema
  OsaamisenHankkimistapa
  (describe
    "Osaamisen hankkimisen tapa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :alku LocalDate "Alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Loppupäivämäärä muodossa YYYY-MM-DD"
    :module-id UUID (str "Tietorakenteen yksilöivä tunniste "
                         "esimerkiksi tiedon jakamista varten")
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
    (s/optional-key :tyopaikalla-jarjestettava-koulutus)
    TyopaikallaJarjestettavaKoulutus
    (str "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot. "
         "Tämä tieto tuodaan, jos hankkimistapa on oppisopimuskoulutus tai "
         "koulutussopimus.")
    (s/optional-key :muut-oppimisymparistot)
    [MuuOppimisymparisto]
    (str "Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen "
         "liittyvät tiedot")
    (s/optional-key :osa-aikaisuustieto) s/Int
    (str "Osaamisen hankkimisen osa-aikaisuuden määrä prosentteina (1-100). "
         "Käytetään työelämäpalautteen työpaikkajakson keston laskemiseen.")
    (s/optional-key :oppisopimuksen-perusta-koodi-uri)
    OppisopimuksenPerustaKoodiUri
    "Oppisopimuksen perustan Koodisto-uri."
    (s/optional-key :oppisopimuksen-perusta-koodi-versio) s/Int
    "Oppisopimuksen perustan Koodisto-versio."
    (s/optional-key :keskeytymisajanjaksot)
    (s/constrained [Keskeytymisajanjakso] not-overlapping?)
    (str "Ajanjaksot, jolloin tutkinnon osan osaamisen hankkiminen kyseisellä "
         "työpaikalla on ollut keskeytyneenä. Tietoa hyödynnetään "
         "työelämäpalautteessa tarvittavan työpaikkajakson keston "
         "laskemiseen.")))

(defn- oppisopimus-has-perusta? [oht]
  (or (not= (:osaamisen-hankkimistapa-koodi-uri oht)
            "osaamisenhankkimistapa_oppisopimus")
      (.isBefore (:loppu oht) (LocalDate/of 2021 7 1))
      (:oppisopimuksen-perusta-koodi-uri oht)))

(s/defschema
  OsaamisenHankkimistapaLuontiJaMuokkaus
  (s/constrained
    (modify
      OsaamisenHankkimistapa
      "Osaamisen hankkimisen tavan luonti ja muokkaus (POST, PUT)"
      {:removed [:module-id]})
    oppisopimus-has-perusta?
    "Tieto oppisopimuksen perustasta puuttuu."))

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
  TyoelamaOsaamisenArvioija
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
    "Koulutuksenjärjestäjän arvioija"
    {:removed [:organisaatio]
     :added
     (describe
       ""
       :organisaatio KoulutuksenJarjestajaOrganisaatio
       "Koulutuksenjärjestäjän arvioijan organisaatio")}))

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
  OsaamisenOsoittaminen
  (describe
    "Hankittavaan tutkinnon osaan tai yhteisen tutkinnon osan osa-alueeseen
    sisältyvä osaamisen osoittaminen: näyttö tai muu osaamisen osoittaminen."
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :module-id UUID (str "Tietorakenteen yksilöivä tunniste "
                         "esimerkiksi tiedon jakamista varten")
    (s/optional-key :jarjestaja) NaytonJarjestaja
    "Näytön tai osaamisen osoittamisen järjestäjä"
    (s/optional-key :osa-alueet) [KoodistoKoodi]
    (str "Suoritettavan tutkinnon osan näyttöön sisältyvän"
         "yton osa-alueiden Koodisto-koodi-URIt
         eperusteet-järjestelmässä muotoa ammatillisenoppiaineet_xxx"
         "esim. ammatillisenoppiaineet_etk")
    :nayttoymparisto Nayttoymparisto
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    :sisallon-kuvaus [s/Str]
    (str "Tiivis kuvaus (esim. lista) työtilanteista ja työprosesseista, joiden
    avulla ammattitaitovaatimusten tai osaamistavoitteiden mukainen osaaminen
    osoitetaan. Vastaavat tiedot muusta osaamisen osoittamisesta siten, että
    tieto kuvaa sovittuja tehtäviä ja toimia, joiden avulla osaaminen
    osoitetaan.")
    :alku LocalDate
    "Näytön tai osaamisen osoittamisen alkupäivämäärä muodossa
    YYYY-MM-DD"
    :loppu LocalDate
    "Näytön tai osaamisen osoittamisen loppupäivämäärä muodossa
    YYYY-MM-DD"
    (s/optional-key :koulutuksen-jarjestaja-osaamisen-arvioijat)
    [KoulutuksenJarjestajaArvioija] "Näytön tai osaamisen osoittamisen
    arvioijat"
    (s/optional-key :tyoelama-osaamisen-arvioijat) [TyoelamaOsaamisenArvioija]
    "Näytön tai osaamisen osoittamisen arvioijat"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    (str "Tutkinnon osan tai osa-alueen perusteisiin sisältyvät
    ammattitaitovaatimukset tai osaamistavoitteet, joista opiskelijan kohdalla
    poiketaan.")
    (s/optional-key :yksilolliset-kriteerit) [s/Str]
    (str "Ammattitaitovaatimus tai osaamistavoite, johon yksilölliset
    arviointikriteerit kohdistuvat ja yksilölliset arviointikriteerit kyseiseen
    ammattitaitovaatimukseen tai osaamistavoitteeseen.")))

(s/defschema
  OsaamisenOsoittaminenLuontiJaMuokkaus
  (modify
    OsaamisenOsoittaminen
    "Osaamisen hankkimisen tavan luonti ja muokkaus (POST, PUT)"
    {:removed [:module-id]}))

(s/defschema
  YhteisenTutkinnonOsanOsaAlue
  (describe
    "Hankittavan yhteinen tutkinnon osan (YTO) osa-alueen tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :module-id UUID (str "Tietorakenteen yksilöivä tunniste "
                         "esimerkiksi tiedon jakamista varten")
    :osa-alue-koodi-uri OsaAlueKoodiUri
    "Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet)"
    :osa-alue-koodi-versio s/Int
    "Osa-alueen Koodisto-koodi-URIn versio (ammatillisenoppiaineet)"
    (s/optional-key :osaamisen-hankkimistavat) [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    (s/optional-key :osaamisen-osoittaminen)
    [OsaamisenOsoittaminen]
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :koulutuksen-jarjestaja-oid) Oid
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    (s/optional-key :olennainen-seikka) s/Bool
    (str "Tieto sellaisen seikan
    olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon
    osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai
    osoittamisessa.")))

(s/defschema
  YhteisenTutkinnonOsanOsaAlueLuontiJaMuokkaus
  (modify
    YhteisenTutkinnonOsanOsaAlue
    "Hankittavan yhteinen tutkinnon osan (YTO) osa-alueen tiedot (POST, PUT)"
    {:removed [:module-id :osaamisen-osoittaminen :osaamisen-hankkimistavat]
     :added
     (describe
       ""
       (s/optional-key :osaamisen-hankkimistavat)
       [OsaamisenHankkimistapaLuontiJaMuokkaus] "Osaamisen hankkimistavat"
       (s/optional-key :osaamisen-osoittaminen)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  AiemminHankitunYTOOsaAlue
  (describe
    "AiemminHankitun YTOn osa-alueen tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :module-id UUID (str "Tietorakenteen yksilöivä tunniste "
                         "esimerkiksi tiedon jakamista varten")
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
    (s/optional-key :tarkentavat-tiedot-naytto) [OsaamisenOsoittaminen]
    "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."
    (s/optional-key :olennainen-seikka) s/Bool
    (str "Tieto sellaisen seikan
    olemassaolosta, jonka koulutuksen järjestäjä katsoo oleelliseksi tutkinnon
    osaan tai osa-alueeseen liittyvän osaamisen hankkimisessa tai
    osoittamisessa.")
    (s/optional-key :tarkentavat-tiedot-osaamisen-arvioija)
    TodennettuArviointiLisatiedot
    "Mikäli arvioijan kautta todennettu, annetaan myös arvioijan lisätiedot"))

(s/defschema
  AiemminHankitunYTOOsaAlueLuontiJaMuokkaus
  (modify
    AiemminHankitunYTOOsaAlue
    "AiemminHankitun YTOn osa-alueen tiedot (POST, PUT)"
    {:removed [:module-id :tarkentavat-tiedot-naytto]
     :added
     (describe
       ""
       (s/optional-key :tarkentavat-tiedot-naytto)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  YhteinenTutkinnonOsa
  (describe
    "Yhteinen Tutkinnon osa (YTO)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :module-id UUID (str "Tietorakenteen yksilöivä tunniste "
                         "esimerkiksi tiedon jakamista varten")
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
  HankittavaYTO
  (modify
    YhteinenTutkinnonOsa
    "Hankittavan yhteinen tutkinnon osan (YTO) tiedot"
    {:removed [:vaatimuksista-tai-tavoitteista-poikkeaminen]}))

(s/defschema
  HankittavaYTOLuontiJaMuokkaus
  (modify
    HankittavaYTO
    "Hankittavan yhteisen tutkinnnon osan (POST, PUT)"
    {:removed
     [:module-id :osaamisen-hankkimistavat :osaamisen-osoittaminen :osa-alueet]
     :added
     (describe
       ""
       :osa-alueet [YhteisenTutkinnonOsanOsaAlueLuontiJaMuokkaus]
       "YTO osa-alueet"
       (s/optional-key :osaamisen-hankkimistavat)
       [OsaamisenHankkimistapaLuontiJaMuokkaus] "Osaamisen hankkimistavat"
       (s/optional-key :osaamisen-osoittaminen)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

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
  HankittavaAmmatillinenTutkinnonOsa
  (describe
    "Hankittavan ammatillisen osaamisen tiedot (GET)"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :module-id UUID (str "Tietorakenteen yksilöivä tunniste "
                         "esimerkiksi tiedon jakamista varten")
    :tutkinnon-osa-koodi-uri TutkinnonOsaKoodiUri
    "Tutkinnon osan Koodisto-koodi-URI (tutkinnonosat)"
    :tutkinnon-osa-koodi-versio s/Int
    "Tutkinnon osan Koodisto-koodi-URIn versio ePerusteet-palvelussa
     (tutkinnonosat)"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    (str "Tekstimuotoinen selite ammattitaitovaatimuksista tai "
         "osaamistavoitteista poikkeamiseen")
    (s/optional-key :osaamisen-osoittaminen) [OsaamisenOsoittaminen]
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :osaamisen-hankkimistavat) [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :koulutuksen-jarjestaja-oid) Oid
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    (s/optional-key :olennainen-seikka) s/Bool
    (str "Tieto sellaisen seikan olemassaolosta, jonka koulutuksen
   järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen
   liittyvän osaamisen hankkimisessa tai osoittamisessa.")))

(s/defschema
  HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus
  (modify
    HankittavaAmmatillinenTutkinnonOsa
    "Hankittavan ammatillisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :osaamisen-hankkimistavat :osaamisen-osoittaminen]
     :added
     (describe
       ""
       (s/optional-key :osaamisen-hankkimistavat)
       [OsaamisenHankkimistapaLuontiJaMuokkaus] "Osaamisen hankkimistavat"
       (s/optional-key :osaamisen-osoittaminen)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  HankittavaPaikallinenTutkinnonOsa
  (describe
    "Hankittava paikallinen tutkinnon osa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :module-id UUID (str "Tietorakenteen yksilöivä tunniste "
                         "esimerkiksi tiedon jakamista varten")
    (s/optional-key :amosaa-tunniste) s/Str
    "Tunniste ePerusteet AMOSAA -palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi"
    (s/optional-key :laajuus) s/Int "Tutkinnon osan laajuus"
    (s/optional-key :tavoitteet-ja-sisallot) s/Str
    (str "Paikallisen tutkinnon osan ammattitaitovaatimukset tai"
         "osaamistavoitteet")
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    (s/optional-key :osaamisen-hankkimistavat) [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :koulutuksen-jarjestaja-oid) Oid
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    (s/optional-key :osaamisen-osoittaminen) [OsaamisenOsoittaminen]
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :olennainen-seikka) s/Bool
    (str "Tieto sellaisen seikan olemassaolosta, jonka koulutuksen
    järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen
    liittyvän osaamisen hankkimisessa tai osoittamisessa.")))

(s/defschema
  HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus
  (modify
    HankittavaPaikallinenTutkinnonOsa
    "Hankittavan paikallisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :osaamisen-hankkimistavat :osaamisen-osoittaminen]
     :added
     (describe
       ""
       (s/optional-key :osaamisen-hankkimistavat)
       [OsaamisenHankkimistapaLuontiJaMuokkaus] "Osaamisen hankkimistavat"
       (s/optional-key :osaamisen-osoittaminen)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  AiemminHankittuPaikallinenTutkinnonOsa
  (modify
    HankittavaPaikallinenTutkinnonOsa
    "Aiemmin hankittu yhteinen tutkinnon osa"
    {:removed [:osaamisen-hankkimistavat :osaamisen-osoittaminen]
     :added
     (describe
       ""
       :valittu-todentamisen-prosessi-koodi-uri TodentamisenProsessiKoodiUri
       "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
       :valittu-todentamisen-prosessi-koodi-versio s/Int
       "Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio
       (Osaamisen todentamisen prosessi)"
       (s/optional-key :tarkentavat-tiedot-naytto) [OsaamisenOsoittaminen]
       "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."
       (s/optional-key :tarkentavat-tiedot-osaamisen-arvioija)
       TodennettuArviointiLisatiedot "Mikäli arvioijan kautta todennettu,
       annetaan myös arvioijan lisätiedot")}))

(s/defschema
  AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus
  (modify
    AiemminHankittuPaikallinenTutkinnonOsa
    "Aiemmin hankitun paikallisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :tarkentavat-tiedot-naytto]
     :added
     (describe
       ""
       (s/optional-key :tarkentavat-tiedot-naytto)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

(s/defschema
  AiemminHankittuYhteinenTutkinnonOsa
  (modify
    YhteinenTutkinnonOsa
    "Aiemmin hankittu yhteinen tutkinnon osa"
    {:removed [:osa-alueet]
     :added
     (describe
       ""
       :osa-alueet [AiemminHankitunYTOOsaAlue] "YTO osa-alueet"
       (s/optional-key :valittu-todentamisen-prosessi-koodi-uri)
       TodentamisenProsessiKoodiUri
       "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
       (s/optional-key :valittu-todentamisen-prosessi-koodi-versio) s/Int
       "Todentamisen prosessin kuvauksen Koodisto-koodi-URIn versio
       (Osaamisen todentamisen prosessi)"
       (s/optional-key :tarkentavat-tiedot-naytto) [OsaamisenOsoittaminen]
       "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."
       (s/optional-key :tarkentavat-tiedot-osaamisen-arvioija)
       TodennettuArviointiLisatiedot "Mikäli arvioijan kautta todennettu,
       annetaan myös arvioijan lisätiedot")}))

(s/defschema
  AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus
  (modify
    AiemminHankittuYhteinenTutkinnonOsa
    "Aiemmin hankitun yhteisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :tarkentavat-tiedot-naytto :osa-alueet]
     :added
     (describe
       ""
       (s/optional-key :tarkentavat-tiedot-naytto)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen")
       :osa-alueet [AiemminHankitunYTOOsaAlueLuontiJaMuokkaus]
       "YTO osa-alueet")}))

(s/defschema
  AiemminHankittuAmmatillinenTutkinnonOsa
  (modify
    AiemminHankittuYhteinenTutkinnonOsa
    "Aiemmin hankittu ammatillisen tutkinnon osa"
    {:removed [:osa-alueet]
     :added
     (describe
       ""
       (s/optional-key :olennainen-seikka) s/Bool
       (str "Tieto sellaisen seikan olemassaolosta, jonka koulutuksen
    järjestäjä katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen
    liittyvän osaamisen hankkimisessa tai osoittamisessa."))}))

(s/defschema
  AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus
  (modify
    AiemminHankittuAmmatillinenTutkinnonOsa
    "Aiemmin hankitun ammatillisen osaamisen tiedot (POST, PUT)"
    {:removed [:module-id :tarkentavat-tiedot-naytto]
     :added
     (describe
       ""
       (s/optional-key :tarkentavat-tiedot-naytto)
       [OsaamisenOsoittaminenLuontiJaMuokkaus]
       (str "Hankitun osaamisen osoittaminen: "
            "Näyttö tai muu osaamisen osoittaminen"))}))

(def ^:private ahato-part-of-hoks
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [AiemminHankittuAmmatillinenTutkinnonOsa]
           :post [AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus]
           :put [AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus]}
   :description "Aiemmin hankittu ammatillinen osaaminen"})

(def ^:private ahyto-part-of-hoks
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [AiemminHankittuYhteinenTutkinnonOsa]
           :post [AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus]
           :put [AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus]}
   :description "Aiemmin hankitut yhteiset tutkinnon osat (YTO)"})

(def ^:private ahpto-part-of-hoks
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [AiemminHankittuPaikallinenTutkinnonOsa]
           :post [AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus]
           :put [AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus]}
   :description "Aiemmin hankittu paikallinen tutkinnon osa"})

(def ^:private oto-part-of-hoks
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [OpiskeluvalmiuksiaTukevatOpinnot]}
   :description "Opiskeluvalmiuksia tukevat opinnot"})

(def ^:private hato-part-of-hoks
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [HankittavaAmmatillinenTutkinnonOsa]
           :post [HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus]
           :put [HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus]}
   :description
   "Hankittavan ammatillisen osaamisen hankkimisen tiedot"})

(def ^:private hyto-part-of-hoks
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [HankittavaYTO]
           :post [HankittavaYTOLuontiJaMuokkaus]
           :put [HankittavaYTOLuontiJaMuokkaus]}
   :description "Hankittavan yhteisen tutkinnon osan hankkimisen tiedot"})

(def ^:private hpto-part-of-hoks
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [HankittavaPaikallinenTutkinnonOsa]
           :post [HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus]
           :put [HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus]}
   :description "Hankittavat paikallisen tutkinnon osat"})

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
   :oppija-oid {:methods {:any :optional
                          :post :required}
                :types {:any Oid}
                :description "Oppijan tunniste Opintopolku-ympäristössä"}
   :sahkoposti {:methods {:any :optional}
                :types {:any s/Str}
                :description "Oppijan sähköposti, merkkijono."}
   :opiskeluoikeus-oid
   {:methods {:any :optional
              :post :required}
    :types {:any OpiskeluoikeusOid}
    :description "Opiskeluoikeuden oid-tunniste Koski-järjestelmässä muotoa
                  '1.2.246.562.15.00000000001'."}
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
   :ensikertainen-hyvaksyminen {:methods {:patch :optional}
                                :types {:any LocalDate}
                                :description
                                "HOKS-dokumentin ensimmäinen hyväksymisaika
                                muodossa YYYY-MM-DD"}
   :hyvaksytty
   {:methods {:any :optional}
    :types {:any s/Inst}
    :description
    "HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"}
   :paivitetty {:methods {:any :optional}
                :types {:any s/Inst}
                :description (str "HOKS-dokumentin viimeisin päivitysaika "
                                  "muodossa YYYY-MM-DDTHH:mm:ss.sssZ")}
   :osaamisen-saavuttamisen-pvm {:methods {:any :optional}
                                 :types {:any LocalDate}
                                 :description
                                 (str "HOKSin osaamisen saavuttamisen "
                                      "ajankohta muodossa YYYY-MM-DD")}
   :osaamisen-hankkimisen-tarve {:methods {:any :required
                                           :patch :optional
                                           :get :optional}
                                 :types {:any s/Bool}
                                 :description
                                 "Tutkintokoulutuksen ja muun tarvittavan
                               ammattitaidon hankkimisen tarve; osaamisen
                               tunnistamis- ja tunnustamisprosessin
                               lopputulos."}
   :manuaalisyotto {:methods {:any :excluded
                              :get :optional}
                    :types {:any s/Bool}
                    :description "Tieto, onko HOKS tuotu manuaalisyötön kautta"}
   :aiemmin-hankitut-ammat-tutkinnon-osat ahato-part-of-hoks
   :aiemmin-hankitut-yhteiset-tutkinnon-osat ahyto-part-of-hoks
   :aiemmin-hankitut-paikalliset-tutkinnon-osat ahpto-part-of-hoks
   :opiskeluvalmiuksia-tukevat-opinnot oto-part-of-hoks
   :hankittavat-ammat-tutkinnon-osat hato-part-of-hoks
   :hankittavat-yhteiset-tutkinnon-osat hyto-part-of-hoks
   :hankittavat-paikalliset-tutkinnon-osat hpto-part-of-hoks})

(def HOKS
  (with-meta
    (g/generate HOKSModel :get)
    {:doc "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)"
     :name "HOKS"}))

(def HOKSPaivitys
  (with-meta
    (g/generate HOKSModel :patch)
    {:doc "HOKS-dokumentin ylikirjoitus (PATCH)"
     :name "HOKSPaivitys"}))

(def HOKSKorvaus
  (with-meta
    (g/generate HOKSModel :put)
    {:doc "HOKS-dokumentin ylikirjoitus (PUT)"
     :name "HOKSKorvaus"}))

(def HOKSLuonti
  (with-meta
    (g/generate HOKSModel :post)
    {:doc "HOKS-dokumentin arvot uutta merkintää luotaessa (POST)"
     :name "HOKSLuonti"}))

(s/defschema
  kyselylinkki
  {:kyselylinkki s/Str
   :alkupvm LocalDate
   :tyyppi s/Str
   (s/optional-key :lahetyspvm) LocalDate
   (s/optional-key :sahkoposti) s/Str
   (s/optional-key :lahetystila) s/Str})

(s/defschema
  kyselylinkki-lahetys
  {:kyselylinkki s/Str
   :lahetyspvm LocalDate
   :sahkoposti s/Str
   (s/optional-key :lahetystila) s/Str})

(s/defschema
  palaute-resend
  {:tyyppi s/Str})

(s/defschema
  shallow-delete-hoks
  {:oppilaitos-oid s/Str})
