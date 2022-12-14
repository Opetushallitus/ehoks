(ns oph.ehoks.db.db-operations.hoks
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as oo]
            [oph.ehoks.db.db-operations.oppija :as op]
            [oph.ehoks.external.organisaatio :as org]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn oppilaitos-oid-from-sql
  "Hakee oppilaitos OID:n tietokannasta haetusta objektista."
  [m]
  (:oppilaitos_oid m))

(defn hoks-from-sql
  "Muuttaa tietokannasta haetun HOKSin siihen muotoon, joka voi palauttaa
  käyttäjälle."
  ([h keep-columns]
    (db-ops/from-sql h {} keep-columns))
  ([h]
    (db-ops/from-sql h nil)))

(defn hoks-to-sql
  "Muuttaa ehoksissa käytetyn HOKSin näin, että sen voi tallentaa hoksit
  -tauluun."
  [h]
  (db-ops/to-sql
    h
    {:removals [:aiemmin-hankitut-ammat-tutkinnon-osat
                :aiemmin-hankitut-paikalliset-tutkinnon-osat
                :aiemmin-hankitut-yhteiset-tutkinnon-osat
                :hankittavat-ammat-tutkinnon-osat
                :hankittavat-yhteiset-tutkinnon-osat
                :hankittavat-koulutuksen-osat
                :opiskeluvalmiuksia-tukevat-opinnot
                :hankittavat-paikalliset-tutkinnon-osat]}))

(defn aiemmin-hankittu-ammat-tutkinnon-osa-from-sql
  "Muuttaa tietokannasta haetun aiemmin hankitun ammatillisen tutkinnon osan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:hoks_id]}))

(defn aiemmin-hankittu-ammat-tutkinnon-osa-to-sql
  "Muuttaa palvelussa käytetyn aiemmin hankitun ammatillisen tutkinnon osan sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql
    m
    {:removals [:tarkentavat-tiedot-naytto
                :tarkentavat-tiedot-osaamisen-arvioija]}))

(defn hankittava-paikallinen-tutkinnon-osa-from-sql
  "Muuttaa tietokannasta haetun hankittavan paikallisen tutkinnon osan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m))

(defn hankittava-paikallinen-tutkinnon-osa-to-sql
  "Muuttaa palvelussa käytetyn hankittavan paikallisen tutkinnon osan sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql
    m {:removals [:osaamisen-osoittaminen :osaamisen-hankkimistavat]}))

(defn tyopaikalla-jarjestettava-koulutus-from-sql
  "Muuttaa tietokannasta haetun työpaikalla järjestettävän koulutuksen sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql
    m
    {:replaces
     {:vastuullinen_tyopaikka_ohjaaja_nimi
      [:vastuullinen-tyopaikka-ohjaaja :nimi]
      :vastuullinen_tyopaikka_ohjaaja_sahkoposti
      [:vastuullinen-tyopaikka-ohjaaja :sahkoposti]
      :vastuullinen_tyopaikka_ohjaaja_puhelinnumero
      [:vastuullinen-tyopaikka-ohjaaja :puhelinnumero]}}))

(defn tyopaikalla-jarjestettava-koulutus-to-sql
  "Muuttaa palvelussa käytetyn työpaikalla järjestettävän koulutuksen sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql
    m
    {:removals [:id :keskeiset-tyotehtavat]
     :replaces
     {[:vastuullinen-tyopaikka-ohjaaja :nimi]
      :vastuullinen-tyopaikka-ohjaaja-nimi
      [:vastuullinen-tyopaikka-ohjaaja :sahkoposti]
      :vastuullinen-tyopaikka-ohjaaja-sahkoposti
      [:vastuullinen-tyopaikka-ohjaaja :puhelinnumero]
      :vastuullinen-tyopaikka-ohjaaja-puhelinnumero}}))

(defn henkilo-from-sql
  "Muuttaa tietokannasta haetun henkilön sen mukaiseksi, mitä odotetaan
  palvelussa."
  [m]
  (db-ops/from-sql
    m
    {:removals [:id :tyopaikalla_jarjestettava_koulutus_id]
     :replaces
     {:organisaatio_nimi [:organisaatio :nimi]
      :organisaatio_y_tunnus [:organisaatio :y-tunnus]}}))

(defn henkilo-to-sql
  "Muuttaa palvelussa käytetyn henkilön sen mukaiseksi, minkä voi tallentaa
  tietokantaan."
  [m]
  (db-ops/to-sql
    m {:replaces {[:organisaatio :nimi] :organisaatio_nimi
                  [:organisaatio :y-tunnus] :organisaatio_y_tunnus}}))

(defn osaamisen-hankkimistapa-from-sql
  "Muuttaa tietokannasta haetun osaamisen hankkimistavan sen mukaiseksi, mitä
  odotetaan palvelussa."
  [m]
  (db-ops/from-sql
    m
    {:removals [:tep_kasitelty]
     :replaces
     {:jarjestajan_edustaja_nimi [:jarjestajan-edustaja :nimi]
      :jarjestajan_edustaja_rooli [:jarjestajan-edustaja :rooli]
      :jarjestajan_edustaja_oppilaitos_oid
      [:jarjestajan-edustaja :oppilaitos-oid]
      :hankkijan_edustaja_nimi [:hankkijan-edustaja :nimi]
      :hankkijan_edustaja_rooli [:hankkijan-edustaja :rooli]
      :hankkijan_edustaja_oppilaitos_oid
      [:hankkijan-edustaja :oppilaitos-oid]}}))

(defn osaamisen-hankkimistapa-to-sql
  "Muuttaa palvelussa käytetyn osaamisen hankkimistavan sen mukaiseksi, minkä
  voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql
    m
    {:removals [:muut-oppimisymparistot
                :tyopaikalla-jarjestettava-koulutus
                :keskeytymisajanjaksot]
     :replaces
     {[:jarjestajan-edustaja :nimi] :jarjestajan-edustaja-nimi
      [:jarjestajan-edustaja :rooli] :jarjestajan-edustaja-rooli
      [:jarjestajan-edustaja :oppilaitos-oid]
      :jarjestajan-edustaja-oppilaitos-oid
      [:hankkijan-edustaja :nimi] :hankkijan-edustaja-nimi
      [:hankkijan-edustaja :rooli] :hankkijan-edustaja-rooli
      [:hankkijan-edustaja :oppilaitos-oid]
      :hankkijan-edustaja-oppilaitos-oid}}))

(defn muu-oppimisymparisto-from-sql
  "Muuttaa tietokannasta haetun muun oppimisympäristön sen mukaiseksi, mitä
  odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:id :osaamisen_hankkimistapa_id]}))

(defn keskeytymisajanjakso-from-sql
  "Muuttaa tietokannasta haetun keskeytymisajanjakson sen mukaiseksi, mitä
  odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:id :osaamisen_hankkimistapa_id]}))

(defn osaamisen-osoittaminen-from-sql
  "Muuttaa tietokannasta haetun osaamisen osoittamisen sen mukaiseksi, mitä
  odotetaan palvelussa."
  [m]
  (db-ops/from-sql
    m
    {:replaces {:jarjestaja_oppilaitos_oid [:jarjestaja :oppilaitos-oid]}}))

(defn osaamisen-osoittaminen-to-sql
  "Muuttaa palvelussa käytetyn osaamisen osoittamisen sen mukaiseksi, minkä voi
  tallentaa tietokantaan."
  [m]
  (db-ops/to-sql
    m
    {:removals [:nayttoymparisto
                :sisallon-kuvaus
                :koulutuksen-jarjestaja-osaamisen-arvioijat
                :tyoelama-osaamisen-arvioijat
                :osa-alueet
                :yksilolliset-kriteerit]
     :replaces {[:jarjestaja :oppilaitos-oid] :jarjestaja-oppilaitos-oid}}))

(defn koodi-uri-from-sql
  "Muuttaa tietokannasta haetun koodi URI:n sen mukaiseksi, mitä odotetaan
  palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:id]}))

(defn koulutuksen-jarjestaja-osaamisen-arvioija-from-sql
  "Muuttaa tietokannasta haetun koulutuksen järjestäjän osaamisen arvioijan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql
    m {:replaces {:oppilaitos_oid [:organisaatio :oppilaitos-oid]}
       :removals [:id]}))

(defn koulutuksen-jarjestaja-osaamisen-arvioija-to-sql
  "Muuttaa palvelussa käytetyn koulutuksen järjestäjän osaamisen arvioijan sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql
    m {:replaces {[:organisaatio :oppilaitos-oid] :oppilaitos-oid}}))

(defn tyoelama-arvioija-from-sql
  "Muuttaa tietokannasta haetun työelämän arvioijan sen mukaiseksi, mitä
  odotetaan palvelussa."
  [m]
  (db-ops/from-sql
    m {:replaces {:organisaatio_nimi [:organisaatio :nimi]
                  :organisaatio_y_tunnus [:organisaatio :y-tunnus]}
       :removals [:id]}))

(defn tyoelama-arvioija-to-sql
  "Muuttaa palvelussa käytetyn työelämän arvioijan sen mukaiseksi, minkä voi
  tallentaa tietokantaan."
  [m]
  (db-ops/to-sql
    m {:replaces {[:organisaatio :nimi] :organisaatio-nimi
                  [:organisaatio :y-tunnus] :organisaatio-y-tunnus}}))

(defn nayttoymparisto-from-sql
  "Muuttaa tietokannasta haetun näyttöympäristön sen mukaiseksi, mitä odotetaan
  palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:id]}))

(defn tyotehtava-from-sql
  "Hakee työtehtävän tietokannasta haetusta objektista."
  [m]
  (get m :tyotehtava))

(defn sisallon-kuvaus-from-sql
  "Hakee sisällön kuvauksen tietokannasta haetusta objektista."
  [m]
  (get m :sisallon_kuvaus))

(defn yksilolliset-kriteerit-from-sql
  "Hakee yksilöllisen kriteerin tietokannasta haetusta objektista."
  [m]
  (get m :yksilollinen_kriteeri))

(defn aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql
  "Muuttaa tietokannasta haetun aiemmin hankitun paikallisen tutkinnon osan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:hoks_id]
                      :replaces
                      {:lahetetty_arvioitavaksi
                       [:tarkentavat_tiedot_osaamisen_arvioija
                        :lahetetty-arvioitavaksi]}}))

(defn aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql
  "Muuttaa palvelussa käytetyn aiemmin hankitun paikallisen tutkinnon osan sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql m {:removals [:tarkentavat-tiedot-naytto
                               :tarkentavat-tiedot-osaamisen-arvioija]
                    :replaces {[:tarkentavat-tiedot-osaamisen-arvioija
                                :lahetetty-arvioitavaksi]
                               :lahetetty-arvioitavaksi}}))

(defn aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql
  "Muuttaa tietokannasta haetun hankitun yhteisen tutkinnon osan osa-alueen sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:aiemmin_hankittu_yhteinen_tutkinnon_osa_id]}))

(defn aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql
  "Muuttaa palvelussa käytetyn hankitun yhteisen tutkinnon osan osa-alueen sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql m {:removals [:tarkentavat-tiedot-naytto
                               :tarkentavat-tiedot-osaamisen-arvioija]}))

(defn aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql
  "Muuttaa palvelussa käytetyn aiemmin hankitun yhteisen tutkinnon osan sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql m {:removals [:osa-alueet
                               :tarkentavat-tiedot-naytto
                               :tarkentavat-tiedot-osaamisen-arvioija]
                    :replaces {[:tarkentavat-tiedot-osaamisen-arvioija
                                :lahetetty-arvioitavaksi]
                               :lahetetty-arvioitavaksi}}))

(defn aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql
  "Muuttaa tietokannasta haetun aiemmin hankitun yhteisen tutkinnon osan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:hoks_id]
                      :replaces
                      {:lahetetty_arvioitavaksi
                       [:tarkentavat_tiedot_osaamisen_arvioija
                        :lahetetty-arvioitavaksi]}}))

(defn todennettu-arviointi-lisatiedot-to-sql
  "Muuttaa palvelussa käytetyt todennetun arvioinnin lisätiedot sen mukaisiksi,
  mitkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql m {:removals [:aiemmin-hankitun-osaamisen-arvioijat]}))

(defn todennettu-arviointi-lisatiedot-from-sql
  "Muuttaa tietokannasta haetut todennetun arvioinnin lisätiedot sen mukaisiksi,
  mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m))

(defn hankittava-ammat-tutkinnon-osa-to-sql
  "Muuttaa palvelussa käytetyn hankittavan ammatillisen tutkinnon osan sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql m {:removals [:osaamisen-osoittaminen
                               :osaamisen-hankkimistavat]}))

(defn hankittava-ammat-tutkinnon-osa-from-sql
  "Muuttaa tietokannasta haetun hankittavan ammatillisen tutkinnon osan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m))

(defn opiskeluvalmiuksia-tukevat-opinnot-from-sql
  "Muuttaa tietokannasta haetut opiskeluvalmiuksia tukevat opinnot sen
  mukaisiksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:hoks_id]}))

(defn hankittava-yhteinen-tutkinnon-osa-from-sql
  "Muuttaa tietokannasta haetun hankittavan yhteisen tutkinnon osan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:osa-alueet]}))

(defn hankittava-koulutuksen-osa-from-sql
  "Muuttaa tietokannasta haetun hankittavan koulutuksen osan sen
  mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m))

(defn hankittava-yhteinen-tutkinnon-osa-to-sql
  "Muuttaa palvelussa käytetyn hankittavan yhteisen tutkinnon osan sen
  mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql m {:removals [:osa-alueet]}))

(defn yhteisen-tutkinnon-osan-osa-alue-to-sql
  "Muuttaa palvelussa käytetyn hankittavan yhteisen tutkinnon osan osa-alueen
  sen mukaiseksi, minkä voi tallentaa tietokantaan."
  [m]
  (db-ops/to-sql m {:removals [:osaamisen-hankkimistavat
                               :osaamisen-osoittaminen]}))

(defn yhteisen-tutkinnon-osan-osa-alue-from-sql
  "Muuttaa tietokannasta haetun hankittavan yhteisen tutkinnon osan osa-alueen
  sen mukaiseksi, mitä odotetaan palvelussa."
  [m]
  (db-ops/from-sql m))

(defn osaamistavoite-from-sql
  "Hakee osaamistavoitteen tietokannasta haetusta objektista."
  [m]
  (get m :osaamistavoite))

(defn select-hoksit
  "hakee HOKSit tietokannasta."
  []
  (db-ops/query
    [queries/select-hoksit]
    :row-fn hoks-from-sql))

(defn select-hoks-by-oppija-oid
  "Hakee HOKSeja tietokannasta oppijan OID:n perusteella."
  [oid]
  (db-ops/query
    [queries/select-hoksit-by-oppija-oid oid]
    :row-fn hoks-from-sql))

(defn select-hoks-by-id
  "Hakee yhden HOKSin tietokannasta sen ID:n perusteella."
  [id]
  (first
    (db-ops/query
      [queries/select-hoksit-by-id id]
      {:row-fn hoks-from-sql})))

(defn select-hokses-greater-than-id
  "Hakee tietokannasta tietyn määrän HOKSeja, joiden ID:t ovat annettu arvo tai
  sitä isompia, ja jotka on muokattu tietyn ajankohdan jälkeen."
  ([from-id amount updated-after keep-columns]
    (db-ops/query [queries/select-hoksit-by-id-paged
                   from-id
                   updated-after
                   updated-after
                   amount]
                  {:row-fn #(hoks-from-sql % keep-columns)}))
  ([from-id amount updated-after]
    (select-hokses-greater-than-id from-id amount updated-after nil)))

(defn select-hoks-by-eid
  "Hakee HOKSin tietokannasta EID:n perustella."
  [eid]
  (first (db-ops/query
           [queries/select-hoksit-by-eid eid]
           {:row-fn hoks-from-sql})))

(defn select-hoksit-by-opiskeluoikeus-oid
  "Hakee HOKSeja tietokannasta opiskeluoikeuden OID:n perusteella."
  [oid]
  (db-ops/query
    [queries/select-hoksit-by-opiskeluoikeus-oid oid]
    {:row-fn hoks-from-sql}))

(defn- select-hoksit-eid-by-eid
  "Hakee EID:t noista HOKSeista, joissa EID täsmää annetun EID:n kanssa."
  [eid]
  (db-ops/query
    [queries/select-hoksit-eid-by-eid eid]
    {}))

(defn select-hoksit-created-between
  "Hakee tietokannasta ne HOKSit, jotka on luotu annettujen ajankohtien
  välillä."
  [from to]
  (db-ops/query
    [queries/select-hoksit-created-between from to]
    {:row-fn hoks-from-sql}))

(defn select-hoksit-finished-between
  "Hakee tietokannasta ne HOKSit, jotka on merkattu valmiiksi annettujen
  ajankohtien välilla."
  [from to]
  (db-ops/query
    [queries/select-hoksit-finished-between from to]
    {:row-fn hoks-from-sql}))

(defn- generate-unique-eid
  "Luo EID, jota ei ole vielä käytössä missään tietokannassa olevassa HOKSissa."
  []
  (loop [eid nil]
    (if (or (nil? eid) (seq (select-hoksit-eid-by-eid eid)))
      (recur (str (java.util.UUID/randomUUID)))
      eid)))

(defn insert-hoks!
  "Tallentaa HOKSin tietokantaan."
  ([hoks]
    (insert-hoks! hoks (db-ops/get-db-connection)))
  ([hoks db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [hoks-by-oo
            (first
              (jdbc/query
                conn
                [queries/select-hoksit-by-opiskeluoikeus-oid-deleted-at-included
                 (:opiskeluoikeus-oid hoks)]))]
        (when hoks-by-oo
          (let [error-info (if (some? (:deleted_at hoks-by-oo))
                             (str "Archived HOKS with given opiskeluoikeus "
                                  "oid found. Contact eHOKS support for more "
                                  "information.")
                             (str "HOKS with the same opiskeluoikeus-oid "
                                  "already exists"))]
            (throw (ex-info error-info {:error :duplicate})))))
      (let [eid (generate-unique-eid)]
        (first
          (jdbc/insert! conn :hoksit (hoks-to-sql (assoc hoks :eid eid))))))))

(defn update-hoks-by-id!
  "Päivittää tietokannassa olevan HOKSin id:n perusteella."
  ([id hoks]
    (db-ops/update! :hoksit
                    (assoc
                      (hoks-to-sql hoks)
                      :updated_at
                      (java.util.Date.))
                    ["id = ? AND deleted_at IS NULL" id]))
  ([id hoks db-conn]
    (db-ops/update! :hoksit
                    (assoc
                      (hoks-to-sql hoks)
                      :updated_at
                      (java.util.Date.))
                    ["id = ? AND deleted_at IS NULL" id] db-conn)))

(defn update-hoks-by-oppija-oid!
  "Päivittää tietokannassa olevan HOKSin oppija_oidin perusteella."
  ([oppija_oid hoks]
    (db-ops/update! :hoksit
                    (assoc
                      (hoks-to-sql hoks)
                      :updated_at
                      (java.util.Date.))
                    ["oppija_oid = ? AND deleted_at IS NULL" oppija_oid]))
  ([oppija_oid hoks db-conn]
    (db-ops/update! :hoksit
                    (assoc
                      (hoks-to-sql hoks)
                      :updated_at
                      (java.util.Date.))
                    ["oppija_oid = ? AND deleted_at IS NULL" oppija_oid]
                    db-conn)))

(defn select-hoks-oppijat-without-index
  "Hakee tietokannasta HOKSien oppijan OID:t, joilla ei ole tietoja
  tietokannassa."
  []
  (db-ops/query
    [queries/select-hoks-oppijat-without-index]))

(defn select-hoks-oppijat-without-index-count
  "Hakee määrän oppijan OID:istä, joilla ei ole tietoja tietokannassa."
  []
  (db-ops/query
    [queries/select-hoks-oppijat-without-index-count]))

(defn select-hoks-opiskeluoikeudet-without-index
  "Hakee tietokannasta opiskeluoikeus OID:t, joilla ei ole tietoja."
  []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-index]))

(defn select-hoks-opiskeluoikeudet-without-index-count
  "Hakee tietokannasta määrän opiskeluoikeus OID:istä, joilla ei ole tietoja."
  []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-index-count]))

(defn select-kyselylinkit-by-oppija-oid
  "Hakee tietokannasta kyselylinkit oppijan OID:n perusteella."
  [oid]
  (db-ops/query
    [queries/select-kyselylinkit-by-oppija-oid oid]
    {:row-fn db-ops/from-sql}))

(defn- select-keskeytymisajanjaksot
  "Hakee tietokannasta keskeytymisajanjaksot OHT:n ID:n perusteella."
  [oht-id]
  (db-ops/query
    [queries/select-keskeytymisajanjaksot-by-osaamisen-hankkimistapa-id oht-id]
    {:row-fn db-ops/from-sql}))

(defn- get-and-assoc-data
  "Apufunktio, joka hakee keskeytymisajanjaksot tietokannasta ja assosioi ne
  työpaikkajakson kanssa."
  [osa-type]
  (fn [jakso]
    (assoc jakso
           :tyyppi osa-type
           :keskeytymisajanjaksot
           (select-keskeytymisajanjaksot (:hankkimistapa_id jakso)))))

(defn select-paattyneet-tyoelamajaksot
  "Hakee tietokannasta työelämäjaksot, jotka päättyivät tietyn aikavälin
  sisällä."
  [osa start end limit]
  (case osa
    "hpto" (map (get-and-assoc-data "hpto")
                (db-ops/query
                  [queries/select-paattyneet-tyoelamajaksot-hpto
                   start end limit]))
    "hato" (map (get-and-assoc-data "hato")
                (db-ops/query
                  [queries/select-paattyneet-tyoelamajaksot-hato
                   start end limit]))
    "hyto" (map (get-and-assoc-data "hyto")
                (db-ops/query
                  [queries/select-paattyneet-tyoelamajaksot-hyto
                   start end limit]))))

(defn select-tyoelamajaksot-active-between
  "Hakee tietokannasta työelämäjaksot, jotka ovat tai olivat voimassa tietyn
  aikavälin sisällä tietyllä oppijalla."
  [osa oppija start end]
  (map (get-and-assoc-data osa)
       (db-ops/query
         [(case osa
            "hato" queries/select-hato-tyoelamajaksot-active-between
            "hpto" queries/select-hpto-tyoelamajaksot-active-between
            "hyto" queries/select-hyto-tyoelamajaksot-active-between)
          oppija
          end
          start])))

(defn update-osaamisen-hankkimistapa-tep-kasitelty
  "Merkitsee osaamisen hankkimistavan käsitellyksi tai ei käsitellyksi."
  [id to]
  (db-ops/update!
    :osaamisen_hankkimistavat
    {:tep_kasitelty to}
    ["id = ?" id]))

(defn update-amisherate-kasittelytilat-aloitusherate-kasitelty
  "Merkitsee AMIS aloitusherätteen käsitellyksi."
  [hoks-id to]
  (let [tila (first
               (db-ops/query
                 [queries/select-amisherate-kasittelytilat-by-hoks-id hoks-id]
                 {:row-fn db-ops/from-sql}))]
    (if (some? tila)
      (db-ops/update!
        :amisherate_kasittelytilat
        {:aloitusherate_kasitelty to}
        ["hoks_id = ?" hoks-id])
      (first (jdbc/insert! (db-ops/get-db-connection)
                           :amisherate_kasittelytilat
                           (db-ops/to-sql {:hoks-id hoks-id
                                           :aloitusherate_kasitelty to}))))))

(defn update-amisherate-kasittelytilat-paattoherate-kasitelty
  "Merkitsee AMIS päättöherätteen käsitellyksi."
  [hoks-id to]
  (let [tila (first
               (db-ops/query
                 [queries/select-amisherate-kasittelytilat-by-hoks-id hoks-id]
                 {:row-fn db-ops/from-sql}))]
    (if (some? tila)
      (db-ops/update!
        :amisherate_kasittelytilat
        {:paattoherate_kasitelty to}
        ["hoks_id = ?" hoks-id])
      (first (jdbc/insert! (db-ops/get-db-connection)
                           :amisherate_kasittelytilat
                           (db-ops/to-sql {:hoks-id hoks-id
                                           :paattoherate_kasitelty to}))))))

(defn insert-amisherate-kasittelytilat!
  "Luo uusi AMIS-heräterivin käsittelytilatauluun."
  ([hoks-id]
    (insert-amisherate-kasittelytilat!
      hoks-id false (db-ops/get-db-connection)))
  ([hoks-id to]
    (insert-amisherate-kasittelytilat! hoks-id to (db-ops/get-db-connection)))
  ([hoks-id to db-conn]
    (first (jdbc/insert! db-conn
                         :amisherate_kasittelytilat
                         (db-ops/to-sql {:hoks-id hoks-id
                                         :aloitusherate_kasitelty to
                                         :paattoherate_kasitelty to})))))

(defn update-amisherate-kasittelytilat!
  "Päivittää AMIS-herätteen käsittelytilat tietokantaan."
  ([tilat]
    (update-amisherate-kasittelytilat! tilat (db-ops/get-db-connection)))
  ([tilat db-conn]
    (db-ops/update! :amisherate_kasittelytilat
                    (db-ops/to-sql (dissoc tilat :id))
                    ["id = ?" (:id tilat)] db-conn)))

(defn select-hoksit-with-kasittelemattomat-aloitusheratteet
  "Hakee tietokannasta HOKSit, joissa on käsittelemättömiä aloitusherätteitä."
  [start end limit]
  (db-ops/query
    [queries/select-hoksit-with-kasittelemattomat-aloitusheratteet
     start end limit]
    {:row-fn hoks-from-sql}))

(defn select-hoksit-with-kasittelemattomat-paattoheratteet
  "Hakee tietokannasta HOKSit, joissa on käsittelemättömiä päättöherätteitä."
  [start end limit]
  (db-ops/query
    [queries/select-hoksit-with-kasittelemattomat-paattoheratteet
     start end limit]
    {:row-fn hoks-from-sql}))

(defn get-or-create-amisherate-kasittelytila-by-hoks-id!
  "Hakee tietokannasta AMIS-herätteen käsittelytilan, tai luo uuden, jos sitä
  ei löydy."
  [hoks-id]
  (let [tila (first
               (db-ops/query
                 [queries/select-amisherate-kasittelytilat-by-hoks-id hoks-id]
                 {:row-fn db-ops/from-sql}))]
    (if (some? tila)
      tila
      (insert-amisherate-kasittelytilat! hoks-id))))

(defn select-count-all-hoks
  "Hakee HOKSien määrän tietokannasta."
  []
  (db-ops/query
    [queries/select-count-all-hoks]))

(defn select-hoks-delete-confirm-info
  "Hakee HOKSiin liittyviä tietoja poistamisen varmistusdialogia varten"
  [hoks-id]
  (let [hoks (select-hoks-by-id hoks-id)
        oppija (op/select-oppija-by-oid (:oppija-oid hoks))
        oo (oo/select-opiskeluoikeus-by-oid (:opiskeluoikeus-oid hoks))
        organisaatio (if-let [oppilaitos-oid (:oppilaitos-oid oo)]
                       (org/get-organisaatio-info oppilaitos-oid))]
    {:nimi (:nimi oppija)
     :hoksId (:id hoks)
     :oppilaitosNimi (get-in organisaatio [:nimi] {:fi "" :sv ""})
     :tutkinnonNimi (get-in oo [:tutkinto-nimi] {:fi "" :sv ""})
     :opiskeluoikeusOid (:opiskeluoikeus-oid hoks)
     :oppilaitosOid (:oppilaitos-oid oo)}))

(defn shallow-delete-hoks-by-hoks-id
  "Asettaa HOKSin poistetuksi(shallow delete) id:n perusteella."
  [hoks-id]
  (db-ops/shallow-delete!
    :hoksit
    ["id = ?" hoks-id]))

(defn undo-shallow-delete
  "Merkitsee HOKSin palautetuksi asettamalla nil:in sen deleted_at -kenttään."
  [hoks-id]
  (db-ops/update!
    :hoksit
    {:deleted_at nil}
    ["id = ?" hoks-id]))

(defn delete-hoks-by-hoks-id
  "Poistaa HOKSin pysyvästi id:n perusteella"
  [hoks-id]
  (let [hoks (select-hoks-by-id hoks-id)]
    (db-ops/delete! :opiskeluoikeudet ["oid = ?" (:opiskeluoikeus-oid hoks)])
    (db-ops/delete! :hoksit ["id = ?" hoks-id])))

(defn select-kyselylinkit-by-date-and-type-temp
  "Hakee tietokannasta kyselylinkit, joiden alkupäivämäärät ovat annetun
  aikavälin sisällä ja joiden HOKS ID:t ovat annettua arvoa isompia."
  [alkupvm alkupvm-loppu last-id limit]
  (db-ops/query
    [queries/select-paattyneet-kyselylinkit-by-date-and-type-temp
     alkupvm alkupvm-loppu last-id limit]
    {:row-fn db-ops/from-sql}))

(defn select-hoksit-by-ensikert-hyvaks-and-saavutettu-tiedot
  "Hakee ID, oppijan OID ja opiskeluoikeuden OID HOKSeista, jotka aloitettiin
  viime kahden vuoden aikana ja jotka eivät ole päättyneet vielä."
  []
  (db-ops/query
    [queries/select-hoksit-by-ensikert-hyvaks-and-saavutettu-tiedot]
    {:row-fn db-ops/from-sql}))

(defn select-hoksit-by-oo-oppilaitos-and-koski404
  "Hakee tietokannasta oppilaitoksen perusteella HOKSit, joilla ei ole
   opiskeluoikeustietoja Koskessa."
  [oppilaitos-oid]
  (db-ops/query
    [queries/select-hoksit-by-oo-oppilaitos-and-koski404
     oppilaitos-oid]
    {:identifiers #(do %)
     :row-fn      db-ops/from-sql}))
