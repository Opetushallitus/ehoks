(ns oph.ehoks.hoks.schema
  (:require [oph.ehoks.schema.generator :as g]
            [oph.ehoks.hoks.hoks :refer [y-tunnus-missing?]]
            [oph.ehoks.schema-tools :refer [describe modify]]
            [schema.core :as s]
            [clojure.tools.logging :as log])
  (:import (java.time LocalDate)
           (java.util UUID)
           (clojure.lang ExceptionInfo)))

(def TutkinnonOsaKoodiUri
  "Tutkinnon osan Koodisto-koodi-URI ePerusteet palvelussa (tutkinnonosat)."
  #"^tutkinnonosat_\d+$")

(def OsaamisenHankkimistapaKoodiUri
  "Osaamisen hankkimistavan koodi-URI:n regex."
  #"^osaamisenhankkimistapa_.+$")

(def OppisopimuksenPerustaKoodiUri
  "Oppisopimuksen perustan koodi-URI:n regex."
  #"^oppisopimuksenperusta_.+$")

(def OsaAlueKoodiUri
  "Osa-alueen koodi-URI:n regex."
  #"^ammatillisenoppiaineet_.+$")

(def OppimisymparistoKoodiUri
  "Oppimisympäristön koodi-URI:n regex."
  #"^oppimisymparistot_\d{4}$")

(def TodentamisenProsessiKoodiUri
  "Valitun todentamisen prosessin Koodisto-koodi-URI"
  #"^osaamisentodentamisenprosessi_\d+$")

(def UrasuunnitelmaKoodiUri
  "Urasuunnitelman koodi-URI:n regex."
  #"^urasuunnitelma_\d{4}$")

(def KoulutuksenOsaKoodiUri
  "Koulutuksen osan (TUVA) koodi-URI:n regex."
  #"^koulutuksenosattuva_\d{3}$")

(def Oid
  "OID:n regex."
  #"^1\.2\.246\.562\.[0-3]\d\.\d+$")

(def OpiskeluoikeusOid
  "Opiskeluoikeuden OID:n regex."
  #"^1\.2\.246\.562\.15\.\d+$")

(defn- calculate-y-tunnus-checksum
  "Laskee Y-tunnuksen tarkistusnumeron tunnuksen 7 ensimmäisen numeron
  perusteella."
  [y-tunnus]
  (as-> (take 7 y-tunnus) n
    (map #(Character/getNumericValue %) n)
    (map * n [7 9 10 5 8 4 2])
    (reduce + n)
    (mod n 11)
    (case n
      0 0
      1 nil
      (- 11 n))))

(defn- valid-y-tunnus?
  "Tarkistaa, täsmääkö Y-tunnuksen laskettu tarkistusnumero tunnuksen
  viimeiseen numeroon."
  [y-tunnus]
  (= (calculate-y-tunnus-checksum y-tunnus)
     (Character/getNumericValue (last y-tunnus))))

(s/defschema
  Y-tunnus
  "Y-tunnuksen schema."
  (s/constrained #"^[0-9]{7}-[0-9]$" valid-y-tunnus? "Kelvollinen Y-tunnus"))

(s/defschema
  KoodistoKoodi
  "Koodistokoodin schema."
  (describe
    "Koodisto Koodi"
    :koodi-uri s/Str "Koodisto-koodi URI"
    :koodi-versio s/Int "Koodisto-koodin versio"))

(s/defschema
  Organisaatio
  "Organisaation schema."
  (describe
    "Organisaatio"
    :nimi s/Str "Organisaation nimi"
    (s/optional-key :y-tunnus) Y-tunnus
    "Mikäli organisaatiolla on y-tunnus, organisaation y-tunnus"))

(s/defschema
  TyoelamaOrganisaatio
  "Työelämäorganisaation schema."
  (modify
    Organisaatio
    "Työelämän toimijan organisaatio, jossa näyttö tai osaamisen osoittaminen
     annetaan"))

(s/defschema
  KoulutuksenJarjestajaOrganisaatio
  "Koulutuksen järjestäjä -organisaation schema."
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
  "Näyttöympäristön schema."
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
  "Henkilön schema."
  (describe
    "Henkilö"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :organisaatio Organisaatio "Henkilön organisaatio"
    :nimi s/Str "Henkilön nimi"
    (s/optional-key :rooli) s/Str "Henkilön rooli"))

(s/defschema
  VastuullinenTyopaikkaOhjaaja
  "Vastuullisen työpaikkaohjaajan schema."
  (modify
    Henkilo
    "Vastuullinen ohjaaja"
    {:removed [:organisaatio :rooli]
     :added
     (describe
       ""
       (s/optional-key :sahkoposti) s/Str
       "Vastuullisen ohjaajan sähköpostiosoite"
       (s/optional-key :puhelinnumero)
       (s/constrained s/Str
                      #(and (<= (count %) 256) (re-matches #"[-+0-9() ]*" %))
                      "Puhelinnumero virheellinen.")
       "Vastuullisen ohjaajan puhelinnumero")}))

(s/defschema
  Oppilaitoshenkilo
  "Oppilaitoshenkilön schema."
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
  "Työpaikalla järjestettävän koulutuksen schema."
  (describe
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :vastuullinen-tyopaikka-ohjaaja VastuullinenTyopaikkaOhjaaja "Vastuullinen
    työpaikkaohjaaja"
    :tyopaikan-nimi s/Str "Työpaikan nimi"
    (s/optional-key :tyopaikan-y-tunnus) Y-tunnus "Työpaikan y-tunnus"
    :keskeiset-tyotehtavat [s/Str] "Keskeiset työtehtävät"))

(s/defschema
  MuuOppimisymparisto
  "Muun oppimisympäristön schema."
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
  "Keskeytymisajanjakson schema."
  (describe
    (str "Ajanjakso, jolloin tutkinnon osan osaamisen hankkiminen kyseisellä "
         "työpaikalla on ollut keskeytyneenä.")
    :alku LocalDate
    "Keskeytymisajanjakson aloituspäivämäärä."
    (s/optional-key :loppu) LocalDate
    "Keskeytymisajanjakson päättymispäivämäärä."))

(defn- not-overlapping?
  "Varmistaa, että keskeytymisajanjaksot eivät mene päällekkäin."
  [jaksot]
  (or (<= (count jaksot) 1)
      (reduce #(if (and (:loppu %1) (.isBefore (:loppu %1) (:alku %2)))
                 %2
                 (reduced false))
              (sort-by :alku (seq jaksot)))))

(defn- osa-aikaisuustieto-valid?
  "Varmistaa, että jakson osa-aikaisuustieto on välillä 1-100, mikäli
  työpaikkajakson loppupäivä on 1.7.2023 tai sen jälkeen."
  [oht]
  (let [osa-aikaisuustieto (:osa-aikaisuustieto oht)
        hankkimistapa (:osaamisen-hankkimistapa-koodi-uri oht)]
    (if (and (.isAfter (:loppu oht) (LocalDate/of 2023 6 30))
             (or
               (= hankkimistapa
                  "osaamisenhankkimistapa_koulutussopimus")
               (= hankkimistapa
                  "osaamisenhankkimistapa_oppisopimus")))
      (and (some? osa-aikaisuustieto)
           (<= osa-aikaisuustieto 100)
           (>= osa-aikaisuustieto 1))
      true)))

(defn- oppisopimus-has-perusta?
  "Varmistaa, että osaamisen hankkimistavassa on oppisopimuksen perusta, jos
  osaamisen hankkimistapatyyppi on oppisopimus."
  [oht]
  (or (not= (:osaamisen-hankkimistapa-koodi-uri oht)
            "osaamisenhankkimistapa_oppisopimus")
      (.isBefore (:loppu oht) (LocalDate/of 2021 7 1))
      (:oppisopimuksen-perusta-koodi-uri oht)))

(defn- nonnegative-duration?
  "Osaamisen hankkimistavan päiväykset ovat oikein päin"
  [oht]
  (not (.isBefore (:loppu oht) (:alku oht))))

(defn- tyopaikkajakso-has-yksiloiva-tunniste?
  [oht]
  (or (-> (:osaamisen-hankkimistapa-koodi-uri oht)
          #{"osaamisenhankkimistapa_oppisopimus"
            "osaamisenhankkimistapa_koulutussopimus"}
          (not))
      (:yksiloiva-tunniste oht)))

(defn- duration-max-5-years?
  "Osaamisen hankkimistapa kestää enintään 5 vuotta"
  [oht]
  (not (.isAfter (:loppu oht)
                 (.plusYears (:alku oht) 5))))

(def OsaamisenHankkimistapa-template
  "Osaamisen hankkimistavan schema eri toiminnoille."
  ^{:doc "Osaamisen hankkimistavan schema eri toiminnoille."
    :type ::g/schema-template
    :constraints
    [{:check osa-aikaisuustieto-valid?
      :description "Lisää osa-aikaisuustieto, joka on välillä 1-100."}
     {:check tyopaikkajakso-has-yksiloiva-tunniste?
      :except-methods #{:put-virkailija :post-virkailija :patch-virkailija}
      :description "Lisää työpaikkajaksoon yksilöivä tunniste."}
     {:check #(not (y-tunnus-missing? %))
      :description "Lisää työpaikkajaksoon työpaikan Y-tunnus."}
     {:check oppisopimus-has-perusta?
      :description "Lisää jaksoon oppisopimuksen perustan koodi-uri."}
     {:check nonnegative-duration?
      :description "Korjaa alku- ja loppupäivämäärä oikein päin."}
     {:check duration-max-5-years?
      :description "Korjaa jakso enintään 5 vuoden pituiseksi."}]
    :name "OsaamisenHankkimistapa"}
  {:id {:methods {:any :excluded, :patch :optional, :get :optional}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :alku {:methods {:any :required}
          :types {:any LocalDate}
          :description "Alkupäivämäärä muodossa YYYY-MM-DD"}
   :loppu {:methods {:any :required}
           :types {:any LocalDate}
           :description "Loppupäivämäärä muodossa YYYY-MM-DD"}
   :module-id {:methods {:any :excluded, :get :required}
               :types {:any UUID}
               :description (str "Tietorakenteen yksilöivä tunniste "
                                 "esimerkiksi tiedon jakamista varten")}
   :yksiloiva-tunniste
   {:methods {:any :optional}
    :types {:any (s/constrained s/Str not-empty)}
    :description "Tietorakenteen yksilöivä tunniste yhden Hoksin kontekstissa."}
   :ajanjakson-tarkenne
   {:methods {:any :optional} :types {:any s/Str}
    :description "Tarkentava teksti ajanjaksolle, jos useita aikavälillä."}
   :osaamisen-hankkimistapa-koodi-uri
   {:methods {:any :required} :types {:any OsaamisenHankkimistapaKoodiUri}
    :description
    (str "Osaamisen hankkimisen Koodisto-koodi-URI (osaamisenhankkimistapa) "
         "eli muotoa osaamisenhankkimistapa_xxx eli esim. "
         "osaamisenhankkimistapa_koulutussopimus")}
   :osaamisen-hankkimistapa-koodi-versio
   {:methods {:any :required} :types {:any s/Int}
    :description "Koodisto-koodin versio, koodistolle osaamisenhankkimistapa"}
   :jarjestajan-edustaja
   {:methods {:any :optional} :types {:any Oppilaitoshenkilo}
    :description "Koulutuksen järjestäjän edustaja"}
   :hankkijan-edustaja
   {:methods {:any :optional} :types {:any Oppilaitoshenkilo}
    :description (str "Oppisopimuskoulutusta hankkineen koulutuksen "
                      "järjestäjän edustaja")}
   :tyopaikalla-jarjestettava-koulutus
   {:methods {:any :optional} :types {:any TyopaikallaJarjestettavaKoulutus}
    :description
    (str "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot. "
         "Tämä tieto tuodaan, jos hankkimistapa on oppisopimuskoulutus tai "
         "koulutussopimus.")}
   :muut-oppimisymparistot
   {:methods {:any :optional} :types {:any [MuuOppimisymparisto]}
    :description (str "Muussa oppimisympäristössä tapahtuvaan osaamisen "
                      "hankkimiseen liittyvät tiedot")}
   :osa-aikaisuustieto
   {:methods {:any :optional} :types {:any s/Int}
    :description
    (str "Osaamisen hankkimisen osa-aikaisuuden määrä prosentteina (1-100). "
         "100 tarkoittaa, että työ on kokoaikaista. Esimerkiksi 80 tarkoittaa, "
         "että työ on osa-aikaista, 80 % normaalista kokonaistyöajasta. "
         "Käytetään työelämäpalautteen työpaikkajakson keston laskemiseen. "
         "Pakollinen 1.7.2023 ja sen jälkeen päättyvillä työpaikkajaksoilla.")}
   :oppisopimuksen-perusta-koodi-uri
   {:methods {:any :optional} :types {:any OppisopimuksenPerustaKoodiUri}
    :description "Oppisopimuksen perustan Koodisto-uri."}
   :oppisopimuksen-perusta-koodi-versio
   {:methods {:any :optional} :types {:any s/Int}
    :description "Oppisopimuksen perustan Koodisto-versio."}
   :keskeytymisajanjaksot
   {:methods {:any :optional}
    :types {:any (s/constrained [Keskeytymisajanjakso] not-overlapping?)}
    :description
    (str "Ajanjaksot, jolloin tutkinnon osan osaamisen hankkiminen kyseisellä "
         "työpaikalla on ollut keskeytyneenä. Tietoa hyödynnetään "
         "työelämäpalautteessa tarvittavan työpaikkajakson keston "
         "laskemiseen.")}})

(s/defschema OsaamisenHankkimistapa
             (g/generate OsaamisenHankkimistapa-template :get))

(s/defschema
  NaytonJarjestaja
  "Näytön järjestäjän schema."
  (describe
    "Näytön tai osaamisen osoittamisen järjestäjä"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :oppilaitos-oid) Oid
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  Arvioija
  "Arvioijan schema."
  (describe
    "Arvioija"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Arvioijan nimi"
    :organisaatio Organisaatio "Arvioijan organisaatio"))

(s/defschema
  TyoelamaOsaamisenArvioija
  "Työelämäosaamisen arvioijan schema."
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
  "Koulutuksen järjestäjän arvioijan schema."
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
  "Todennetun arvioinnin lisätietojen schema."
  (describe
    "Mikäli arvioijan kautta todennettu, annetaan myös arvioijan lisätiedot"
    (s/optional-key :lahetetty-arvioitavaksi) LocalDate "Päivämäärä, jona
    lähetetty arvioitavaksi, muodossa YYYY-MM-DD"
    (s/optional-key :aiemmin-hankitun-osaamisen-arvioijat)
    [KoulutuksenJarjestajaArvioija]
    "Mikäli todennettu arvioijan kautta, annetaan arvioijien tiedot."))

(def OsaamisenOsoittaminen-template
  ^{:doc (str "Hankittavaan tutkinnon osaan tai yhteisen tutkinnon osan "
              "osa-alueeseen sisältyvä osaamisen osoittaminen: "
              "näyttö tai muu osaamisen osoittaminen.")
    :type ::g/schema-template
    :name "OsaamisenOsoittaminen"}
  {:id {:methods {:any :excluded, :patch :optional, :get :optional}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :module-id {:methods {:any :excluded, :get :required}
               :types {:any UUID}
               :description (str "Tietorakenteen yksilöivä tunniste "
                                 "esimerkiksi tiedon jakamista varten")}
   :jarjestaja {:methods {:any :optional}
                :types {:any NaytonJarjestaja}
                :description "Näytön tai osaamisen osoittamisen järjestäjä"}
   :osa-alueet
   {:methods {:any :optional}
    :types {:any [KoodistoKoodi]}
    :description
    (str "Suoritettavan tutkinnon osan näyttöön sisältyvän yton osa-alueiden "
         "Koodisto-koodi-URIt eperusteet-järjestelmässä muotoa "
         "ammatillisenoppiaineet_xxx, esim. ammatillisenoppiaineet_etk")}
   :nayttoymparisto {:methods {:any :required}
                     :types {:any Nayttoymparisto}
                     :description (str "Organisaatio, jossa näyttö tai "
                                       "osaamisen osoittaminen annetaan")}
   :sisallon-kuvaus
   {:methods {:any :required}
    :types {:any [s/Str]}
    :description
    (str "Tiivis kuvaus (esim. lista) työtilanteista ja työprosesseista, "
         "joiden avulla ammattitaitovaatimusten tai osaamistavoitteiden "
         "mukainen osaaminen osoitetaan. Vastaavat tiedot muusta osaamisen "
         "osoittamisesta siten, että tieto kuvaa sovittuja tehtäviä ja toimia, "
         "joiden avulla osaaminen osoitetaan.")}
   :alku {:methods {:any :required}
          :types {:any LocalDate}
          :description "Näytön tai osaamisen osoittamisen alkupäivämäärä"}
   :loppu {:methods {:any :required}
           :types {:any LocalDate}
           :description "Näytön tai osaamisen osoittamisen loppupäivämäärä"}
   :koulutuksen-jarjestaja-osaamisen-arvioijat
   {:methods {:any :optional}
    :types {:any [KoulutuksenJarjestajaArvioija]}
    :description "Näytön tai osaamisen osoittamisen arvioijat"}
   :tyoelama-osaamisen-arvioijat
   {:methods {:any :optional}
    :types {:any [TyoelamaOsaamisenArvioija]}
    :description "Näytön tai osaamisen osoittamisen arvioijat"}
   :vaatimuksista-tai-tavoitteista-poikkeaminen
   {:methods {:any :optional}
    :types {:any s/Str}
    :description (str "Tutkinnon osan tai osa-alueen perusteisiin sisältyvät "
                      "ammattitaitovaatimukset tai osaamistavoitteet, joista "
                      "opiskelijan kohdalla poiketaan.")}
   :yksilolliset-kriteerit
   {:methods {:any :optional}
    :types {:any [s/Str]}
    :description
    (str "Ammattitaitovaatimus tai osaamistavoite, johon yksilölliset"
         "arviointikriteerit kohdistuvat ja yksilölliset arviointikriteerit "
         "kyseiseen ammattitaitovaatimukseen tai osaamistavoitteeseen.")}})

(s/defschema OsaamisenOsoittaminen
             (g/generate OsaamisenOsoittaminen-template :get))

(defn hankittava->aiemmin-hankittu
  "Muuttaa hankittavan tutkinnon osan skeema-mallineen aiemmin hankitun
  tutkinnon osan skeema-mallineeksi."
  [schema & flags]
  (let [prosessi-mode (if (some #{:prosessi-required} flags)
                        :required :optional)]
    (-> schema
        (dissoc :osaamisen-hankkimistavat :osaamisen-osoittaminen :osa-alueet
                :opetus-ja-ohjaus-maara)
        (assoc :valittu-todentamisen-prosessi-koodi-uri
               {:methods {:any prosessi-mode}
                :types {:any TodentamisenProsessiKoodiUri}
                :description
                (str "Todentamisen prosessin kuvauksen (suoraan/arvioijien "
                     "kautta/näyttö) koodi-uri. Koodisto osaamisen "
                     "todentamisen prosessi, eli muotoa "
                     "osaamisentodentamisenprosessi_xxxx")}
               :valittu-todentamisen-prosessi-koodi-versio
               {:methods {:any prosessi-mode}
                :types {:any s/Int}
                :description
                (str "Todentamisen prosessin kuvauksen Koodisto-koodi-URIn "
                     "versio (Osaamisen todentamisen prosessi)")}
               :tarkentavat-tiedot-naytto
               {:methods {:any :optional}
                :types {:any [OsaamisenOsoittaminen-template]}
                :description
                "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."}
               :tarkentavat-tiedot-osaamisen-arvioija
               {:methods {:any :optional}
                :types {:any TodennettuArviointiLisatiedot}
                :description (str "Mikäli arvioijan kautta todennettu, "
                                  "annetaan myös arvioijan lisätiedot")}))))

(def HankittavanYTOnOsaAlue-template
  ^{:doc "Hankittavan yhteinen tutkinnon osan (YTO) osa-alueen tiedot"
    :type ::g/schema-template
    :name "HankittavanYTOnOsaAlue"}
  {:id {:methods {:any :excluded, :get :optional, :patch :optional}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :module-id {:methods {:any :excluded, :get :required}
               :types {:any UUID}
               :description (str "Tietorakenteen yksilöivä tunniste "
                                 "esimerkiksi tiedon jakamista varten")}
   :osa-alue-koodi-uri
   {:methods {:any :required}
    :types {:any OsaAlueKoodiUri}
    :description "Osa-alueen Koodisto-koodi-URI (ammatillisenoppiaineet)"}
   :osa-alue-koodi-versio
   {:methods {:any :required}
    :types {:any s/Int}
    :description "Osa-alueen Koodisto-koodi-URIn versio"}
   :osaamisen-hankkimistavat {:methods {:any :optional}
                              :types {:any [OsaamisenHankkimistapa-template]}
                              :description "Osaamisen hankkimistavat"}
   :vaatimuksista-tai-tavoitteista-poikkeaminen
   {:methods {:any :optional}
    :types {:any s/Str}
    :description "vaatimuksista tai osaamistavoitteista poikkeaminen"}
   :osaamisen-osoittaminen
   {:methods {:any :optional}
    :types {:any [OsaamisenOsoittaminen-template]}
    :description (str "Hankitun osaamisen osoittaminen: "
                      "Näyttö tai muu osaamisen osoittaminen")}
   :koulutuksen-jarjestaja-oid
   {:methods {:any :optional}
    :types {:any Oid}
    :description
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")}
   :olennainen-seikka
   {:methods {:any :optional}
    :types {:any s/Bool}
    :description
    (str "Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä "
         "katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän "
         "osaamisen hankkimisessa tai osoittamisessa.")}
   :opetus-ja-ohjaus-maara
   {:methods {:any :optional}
    :types {:any (s/constrained
                   s/Num #(not (neg? %))
                   "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.")}
    :description (str "Tutkinnon osan osa-alueeseen suunnitellun opetuksen "
                      "ja ohjauksen määrä tunteina.")}})

(s/defschema HankittavanYTOnOsaAlue
             (g/generate HankittavanYTOnOsaAlue-template :get))

(def AiemminHankitunYTOnOsaAlue-template
  (with-meta
    (hankittava->aiemmin-hankittu
      HankittavanYTOnOsaAlue-template
      :prosessi-required)
    {:doc "Aiemmin hankitun yhteisen tutkinnon osan osa-alueen schema."
     :type ::g/schema-template
     :name "AiemminHankitunYTOnOsaAlue"}))

(s/defschema AiemminHankitunYTOnOsaAlue
             (g/generate AiemminHankitunYTOnOsaAlue-template :get))

(def HankittavaYhteinenTutkinnonOsa-template
  ^{:doc "Hankittava Yhteinen Tutkinnon osa (YTO)"
    :type ::g/schema-template
    :name "HankittavaYhteinenTutkinnonOsa"}
  {:id {:methods {:any :excluded, :patch :optional, :get :optional}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :module-id {:methods {:any :excluded, :get :required}
               :types {:any UUID}
               :description (str "Tietorakenteen yksilöivä tunniste "
                                 "esimerkiksi tiedon jakamista varten")}
   :osa-alueet {:methods {:any :required}
                :types {:any [HankittavanYTOnOsaAlue-template]}
                :description "yhteisen tutkinnon osan osa-alueet"}
   :tutkinnon-osa-koodi-uri
   {:methods {:any :required}
    :types {:any TutkinnonOsaKoodiUri}
    :description (str "Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa "
                      "(tutkinnonosat) muotoa tutkinnonosat_xxxxxx eli esim. "
                      "tutkinnonosat_100002")}
   :tutkinnon-osa-koodi-versio
   {:methods {:any :required}
    :types {:any s/Int}
    :description (str "Tutkinnon osan Koodisto-koodi-URIn versio "
                      "ePerusteet-palvelussa (tutkinnonosat)")}
   :koulutuksen-jarjestaja-oid
   {:methods {:any :optional}
    :types {:any Oid}
    :description
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")}})

(s/defschema HankittavaYhteinenTutkinnonOsa
             (g/generate HankittavaYhteinenTutkinnonOsa-template :get))

(s/defschema HankittavaYhteinenTutkinnonOsaLuontiJaMuokkaus
             (g/generate HankittavaYhteinenTutkinnonOsa-template :post))

(s/defschema HankittavaYhteinenTutkinnonOsaPatch
             (g/generate HankittavaYhteinenTutkinnonOsa-template :patch))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnot
  "Opiskeluvalmiuksia tukevien opintojen schema."
  (describe
    "Opiskeluvalmiuksia tukevat opinnot"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Opintojen nimi"
    :kuvaus s/Str "Opintojen kuvaus"
    :alku LocalDate "Opintojen alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Opintojen loppupäivämäärä muodossa YYYY-MM-DD"))

(def HankittavaAmmatillinenTutkinnonOsa-template
  ^{:doc "Hankittavan ammatillisen tutkinnon osan schema."
    :type ::g/schema-template
    :name "HankittavaAmmatillinenTutkinnonOsa"}
  {:id {:methods {:any :excluded, :patch :optional, :get :optional}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :module-id {:methods {:any :excluded, :get :required}
               :types {:any UUID}
               :description (str "Tietorakenteen yksilöivä tunniste "
                                 "esimerkiksi tiedon jakamista varten")}
   :tutkinnon-osa-koodi-uri
   {:methods {:any :required}
    :types {:any TutkinnonOsaKoodiUri}
    :description (str "Tutkinnon osan Koodisto-koodi-URI ePerusteet-palvelussa "
                      "(tutkinnonosat) muotoa tutkinnonosat_xxxxxx eli esim. "
                      "tutkinnonosat_100002")}
   :tutkinnon-osa-koodi-versio
   {:methods {:any :required}
    :types {:any s/Int}
    :description (str "Tutkinnon osan Koodisto-koodi-URIn versio "
                      "ePerusteet-palvelussa (tutkinnonosat)")}
   :vaatimuksista-tai-tavoitteista-poikkeaminen
   {:methods {:any :optional}
    :types {:any s/Str}
    :description (str "Tekstimuotoinen selite ammattitaitovaatimuksista tai "
                      "osaamistavoitteista poikkeamiseen")}
   :osaamisen-osoittaminen
   {:methods {:any :optional}
    :types {:any [OsaamisenOsoittaminen-template]}
    :description (str "Hankitun osaamisen osoittaminen: "
                      "Näyttö tai muu osaamisen osoittaminen")}
   :osaamisen-hankkimistavat {:methods {:any :optional}
                              :types {:any [OsaamisenHankkimistapa-template]}
                              :description "Osaamisen hankkimistavat"}
   :koulutuksen-jarjestaja-oid
   {:methods {:any :optional}
    :types {:any Oid}
    :description
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")}
   :olennainen-seikka
   {:methods {:any :optional}
    :types {:any s/Bool}
    :description
    (str "Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä "
         "katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän "
         "osaamisen hankkimisessa tai osoittamisessa.")}
   :opetus-ja-ohjaus-maara
   {:methods {:any :optional}
    :types {:any (s/constrained
                   s/Num #(not (neg? %))
                   "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.")}
    :description (str "Tutkinnon osan osa-alueeseen suunnitellun opetuksen "
                      "ja ohjauksen määrä tunteina.")}})

(s/defschema HankittavaAmmatillinenTutkinnonOsa
             (g/generate HankittavaAmmatillinenTutkinnonOsa-template :get))

(s/defschema HankittavaAmmatillinenTutkinnonOsaLuontiJaMuokkaus
             (g/generate HankittavaAmmatillinenTutkinnonOsa-template :post))

(s/defschema HankittavaAmmatillinenTutkinnonOsaPatch
             (g/generate HankittavaAmmatillinenTutkinnonOsa-template :patch))

(def HankittavaPaikallinenTutkinnonOsa-template
  ^{:doc "Hankittavan paikallisen tutkinnon osan schema."
    :type ::g/schema-template
    :name "HankittavaPaikallinenTutkinnonOsa"}
  {:id {:methods {:any :excluded, :patch :optional, :get :optional}
        :types {:any s/Int}
        :description "Tunniste eHOKS-järjestelmässä"}
   :module-id {:methods {:any :excluded, :get :required}
               :types {:any UUID}
               :description (str "Tietorakenteen yksilöivä tunniste "
                                 "esimerkiksi tiedon jakamista varten")}
   :amosaa-tunniste {:methods {:any :optional}
                     :types {:any s/Str}
                     :description "Tunniste ePerusteet AMOSAA -palvelussa"}
   :nimi {:methods {:any :optional}
          :types {:any s/Str}
          :description "Tutkinnon osan nimi"}
   :laajuus {:methods {:any :optional}
             :types {:any s/Int}
             :description "Tutkinnon osan laajuus"}
   :tavoitteet-ja-sisallot
   {:methods {:any :optional}
    :types {:any s/Str}
    :description
    "Paikallisen tutkinnon osan ammattitaitovaatimukset tai osaamistavoitteet"}
   :vaatimuksista-tai-tavoitteista-poikkeaminen
   {:methods {:any :optional}
    :types {:any s/Str}
    :description (str "Tekstimuotoinen selite ammattitaitovaatimuksista tai "
                      "osaamistavoitteista poikkeamiseen")}
   :osaamisen-osoittaminen
   {:methods {:any :optional}
    :types {:any [OsaamisenOsoittaminen-template]}
    :description (str "Hankitun osaamisen osoittaminen: "
                      "Näyttö tai muu osaamisen osoittaminen")}
   :osaamisen-hankkimistavat {:methods {:any :optional}
                              :types {:any [OsaamisenHankkimistapa-template]}
                              :description "Osaamisen hankkimistavat"}
   :koulutuksen-jarjestaja-oid
   {:methods {:any :optional}
    :types {:any Oid}
    :description
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")}
   :olennainen-seikka
   {:methods {:any :optional}
    :types {:any s/Bool}
    :description
    (str "Tieto sellaisen seikan olemassaolosta, jonka koulutuksen järjestäjä "
         "katsoo oleelliseksi tutkinnon osaan tai osa-alueeseen liittyvän "
         "osaamisen hankkimisessa tai osoittamisessa.")}
   :opetus-ja-ohjaus-maara
   {:methods {:any :optional}
    :types {:any (s/constrained
                   s/Num #(not (neg? %))
                   "Opetuksen ja ohjauksen määrä ei saa olla negatiivinen.")}
    :description (str "Tutkinnon osan osa-alueeseen suunnitellun opetuksen "
                      "ja ohjauksen määrä tunteina.")}})

(s/defschema HankittavaPaikallinenTutkinnonOsa
             (g/generate HankittavaPaikallinenTutkinnonOsa-template :get))

(s/defschema HankittavaPaikallinenTutkinnonOsaLuontiJaMuokkaus
             (g/generate HankittavaPaikallinenTutkinnonOsa-template :post))

(s/defschema HankittavaPaikallinenTutkinnonOsaPatch
             (g/generate HankittavaPaikallinenTutkinnonOsa-template :patch))

(def AiemminHankittuPaikallinenTutkinnonOsa-template
  (with-meta
    (hankittava->aiemmin-hankittu
      HankittavaPaikallinenTutkinnonOsa-template
      :prosessi-required)
    {:doc "Aiemmin hankitun paikallisen tutkinnon osan schema."
     :type ::g/schema-template
     :name "AiemminHankittuPaikallinenTutkinnonOsa"}))

(s/defschema AiemminHankittuPaikallinenTutkinnonOsa
             (g/generate AiemminHankittuPaikallinenTutkinnonOsa-template :get))

(s/defschema AiemminHankittuPaikallinenTutkinnonOsaLuontiJaMuokkaus
             (g/generate AiemminHankittuPaikallinenTutkinnonOsa-template :post))

(s/defschema
  AiemminHankittuPaikallinenTutkinnonOsaPatch
  (g/generate AiemminHankittuPaikallinenTutkinnonOsa-template :patch))

(def AiemminHankittuYhteinenTutkinnonOsa-template
  (with-meta
    (assoc (hankittava->aiemmin-hankittu
             HankittavaYhteinenTutkinnonOsa-template)
           :osa-alueet {:methods {:any :required}
                        :types {:any [AiemminHankitunYTOnOsaAlue-template]}
                        :description "YTOn osa-alueet"})
    {:doc "Aiemmin hankitun yhteisen tutkinnon osan schema."
     :type ::g/schema-template
     :name "AiemminHankittuYhteinenTutkinnonOsa"}))

(s/defschema AiemminHankittuYhteinenTutkinnonOsa
             (g/generate AiemminHankittuYhteinenTutkinnonOsa-template :get))

(s/defschema AiemminHankittuYhteinenTutkinnonOsaLuontiJaMuokkaus
             (g/generate AiemminHankittuYhteinenTutkinnonOsa-template :post))

(s/defschema AiemminHankittuYhteinenTutkinnonOsaPatch
             (g/generate AiemminHankittuYhteinenTutkinnonOsa-template :patch))

(def AiemminHankittuAmmatillinenTutkinnonOsa-template
  (with-meta
    (hankittava->aiemmin-hankittu HankittavaAmmatillinenTutkinnonOsa-template)
    {:doc "Aiemmin hankitun ammatillisen tutkinnon osan schema."
     :type ::g/schema-template
     :name "AiemminHankittuAmmatillinenTutkinnonOsa"}))

(s/defschema
  AiemminHankittuAmmatillinenTutkinnonOsa
  (g/generate AiemminHankittuAmmatillinenTutkinnonOsa-template :get))

(s/defschema
  AiemminHankittuAmmatillinenTutkinnonOsaLuontiJaMuokkaus
  (g/generate AiemminHankittuAmmatillinenTutkinnonOsa-template :post))

(s/defschema
  AiemminHankittuAmmatillinenTutkinnonOsaPatch
  (g/generate AiemminHankittuAmmatillinenTutkinnonOsa-template :patch))

(s/defschema
  HankittavaKoulutuksenOsa
  "Hankittavan koulutuksen osan schema."
  (describe
    "Hankittava koulutuksen osa"
    (s/optional-key :id) s/Int "Tunniste eHOKS-järjestelmässä"
    :koulutuksen-osa-koodi-uri KoulutuksenOsaKoodiUri
    "TUVA perusteen koulutuksen osan koodiuri"
    :koulutuksen-osa-koodi-versio s/Int
    "TUVA perusteen koulutuksen osan koodiurin versio"
    :alku LocalDate "Alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Loppupäivämäärä muodossa YYYY-MM-DD"
    :laajuus
    (s/constrained s/Num
                   #(not (neg? %))
                   "Koulutuksen osan laajuus ei saa olla negatiivinen.")
    (str "Tutkintoon valmentavan koulutuksen koulutuksen osan laajuus "
         "TUVA-viikkoina.")))

(s/defschema
  OsaamisenSaavuttamisenPvm
  "Aika, jonka saa kirjoittaa osaamisen-saavuttamisen-pvm-kenttään."
  (s/constrained
    LocalDate
    #(and (.isAfter % (LocalDate/of 2018 1 1))
          (.isBefore % (.plusDays (LocalDate/now) 15)))
    "Osaaminen voidaan merkitä saavutetuksi enintään kaksi viikkoa
    tulevaisuuteen ja vähintään vuodelle 2018."))

(def ^:private oto-part-of-hoks
  "Opiskeluvalmiuksia tukevien opintojen HOKS-osa schemana."
  {:methods {:any :optional :patch :excluded}
   :types {:any [OpiskeluvalmiuksiaTukevatOpinnot]}
   :description (str "Opiskeluvalmiuksia tukevat opinnot. Ei sallittu "
                     "TUVA-HOKSilla.")})

(def ^:private hankittava-koulutuksen-osa
  "TUVA HOKSin hankittava koulutuksen osa."
  {:methods {:any :optional
             :patch :excluded}
   :types {:any [HankittavaKoulutuksenOsa]}
   :description "Hankittava koulutuksen osa. Sallittu vain TUVA-HOKSilla."})

(def HOKSModel
  "HOKS-schema."
  ^{:doc "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti"
    :type ::g/schema-template
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
                          :post :required} ; FIXME: should be required for :put
                :types {:any Oid}
                :description "Oppijan tunniste Opintopolku-ympäristössä"}
   :sahkoposti {:methods {:any :optional}
                :types {:any s/Str}
                :description "Oppijan sähköposti, merkkijono."}
   :puhelinnumero {:methods {:any :optional}
                   :types {:any s/Str}
                   :description "Oppijan puhelinnumero, merkkijono."}
   :opiskeluoikeus-oid
   {:methods {:any :optional
              :post :required} ; FIXME: should be required for :put
    :types {:any OpiskeluoikeusOid}
    :description (str "Opiskeluoikeuden oid-tunniste Koski-järjestelmässä "
                      "muotoa '1.2.246.562.15.00000000001'.")}
   :tuva-opiskeluoikeus-oid
   {:methods {:any :optional}
    :types {:any OpiskeluoikeusOid}
    :description (str "TUVA-opiskeluoikeuden oid-tunniste Koski-järjestelmässä "
                      "muotoa '1.2.246.562.15.00000000001'. Ei sallittu "
                      "TUVA-HOKSilla.")}
   :urasuunnitelma-koodi-uri
   {:methods {:any :optional}
    :types {:any UrasuunnitelmaKoodiUri}
    :description (str "Opiskelijan tavoitteen Koodisto-koodi-URI, koodisto "
                      "Urasuunnitelma, muotoa urasuunnitelma_xxxx, esim. "
                      "urasuunnitelma_0001")}
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
                                (str "HOKS-dokumentin ensimmäinen "
                                     "hyväksymisaika muodossa YYYY-MM-DD")}
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
                                 :types {:any OsaamisenSaavuttamisenPvm}
                                 :description
                                 (str "HOKSin osaamisen saavuttamisen "
                                      "ajankohta muodossa YYYY-MM-DD")}
   :osaamisen-hankkimisen-tarve {:methods {:any :required
                                           :patch :optional
                                           :get :optional}
                                 :types {:any s/Bool}
                                 :description
                                 (str "Tutkintokoulutuksen ja muun tarvittavan "
                                      "ammattitaidon hankkimisen tarve; "
                                      "osaamisen tunnistamis- ja "
                                      "tunnustamisprosessin lopputulos.")}
   :manuaalisyotto {:methods {:any :excluded
                              :get :optional}
                    :types {:any s/Bool}
                    :description "Tieto, onko HOKS tuotu manuaalisyötön kautta"}
   :aiemmin-hankitut-ammat-tutkinnon-osat
   {:methods {:any :optional :patch :excluded}
    :types {:any [AiemminHankittuAmmatillinenTutkinnonOsa-template]}
    :description (str "Aiemmin hankittu ammatillinen osaaminen. Ei sallittu "
                      "TUVA-HOKSilla.")}
   :aiemmin-hankitut-yhteiset-tutkinnon-osat
   {:methods {:any :optional :patch :excluded}
    :types {:any [AiemminHankittuYhteinenTutkinnonOsa-template]}
    :description (str "Aiemmin hankitut yhteiset tutkinnon osat (YTO). Ei "
                      "sallittu TUVA-HOKSilla.")}
   :aiemmin-hankitut-paikalliset-tutkinnon-osat
   {:methods {:any :optional :patch :excluded}
    :types {:any [AiemminHankittuPaikallinenTutkinnonOsa-template]}
    :description (str "Aiemmin hankittu paikallinen tutkinnon osa. Ei sallittu "
                      "TUVA-HOKSilla.")}
   :opiskeluvalmiuksia-tukevat-opinnot oto-part-of-hoks
   :hankittavat-ammat-tutkinnon-osat
   {:methods {:any :optional :patch :excluded}
    :types {:any [HankittavaAmmatillinenTutkinnonOsa-template]}
    :description (str "Hankittavan ammatillisen osaamisen hankkimisen "
                      "tiedot. Ei sallittu TUVA-HOKSilla.")}
   :hankittavat-yhteiset-tutkinnon-osat
   {:methods {:any :optional
              :patch :excluded}
    :types {:any [HankittavaYhteinenTutkinnonOsa-template]}
    :description (str "Hankittavan yhteisen tutkinnon osan hankkimisen tiedot. "
                      "Ei sallittu TUVA-HOKSilla.")}
   :hankittavat-paikalliset-tutkinnon-osat
   {:methods {:any :optional
              :patch :excluded}
    :types {:any [HankittavaPaikallinenTutkinnonOsa-template]}
    :description (str "Hankittavat paikallisen tutkinnon osat. Ei sallittu "
                      "TUVA-HOKSilla.")}
   :hankittavat-koulutuksen-osat hankittava-koulutuksen-osa})

(defn generate-hoks-schema [schema-name method doc]
  (with-meta (g/generate HOKSModel method) {:doc doc :name schema-name}))

(def HOKS
  "HOKSin schema."
  (generate-hoks-schema
    "HOKS" :get "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti"))

(def HOKSPaivitys
  "HOKSin päivitysschema."
  (generate-hoks-schema
    "HOKSPaivitys" :patch "HOKS-dokumentin osittainen päivittäminen (PATCH)"))

(def HOKSKorvaus
  "HOKSin korvausschema."
  (generate-hoks-schema
    "HOKSKorvaus" :put "HOKS-dokumentin ylikirjoitus (PUT)"))

(def HOKSLuonti
  "HOKSin luontischema."
  (generate-hoks-schema "HOKSLuonti" :post "HOKS-dokumentin luominen (POST)"))

(s/defschema
  kyselylinkki
  "Kyselylinkkischema."
  {:kyselylinkki s/Str
   :alkupvm LocalDate
   :tyyppi s/Str
   (s/optional-key :lahetyspvm) LocalDate
   (s/optional-key :sahkoposti) s/Str
   (s/optional-key :lahetystila) s/Str})

(s/defschema
  kyselylinkki-lahetys
  "Kyselylinkin lähetysschema."
  {:kyselylinkki s/Str
   :lahetyspvm LocalDate
   :sahkoposti s/Str
   (s/optional-key :lahetystila) s/Str})

(s/defschema
  palaute-resend
  "Palautteen uudelleenlähetysschema."
  {:tyyppi s/Str})

(s/defschema
  shallow-delete-hoks
  "Schema HOKSin poistoon oppilaitos-OID:n perusteella."
  {:oppilaitos-oid s/Str})
