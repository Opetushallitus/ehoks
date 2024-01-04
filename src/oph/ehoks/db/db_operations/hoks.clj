(ns oph.ehoks.db.db-operations.hoks
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as oo]
            [oph.ehoks.db.db-operations.oppija :as op]
            [oph.ehoks.external.organisaatio :as org]
            [oph.ehoks.external.koski :as k]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [clojure.set :as s])
  (:import (java.time LocalDate)))

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

(defn select-non-tuva-hoksit-created-between
  "Hakee tietokannasta ne HOKSit, jotka on luotu annettujen ajankohtien
  välillä ja jotka eivät ole TUVA-HOKSeja tai TUVA-HOKSien kanssa
  rinnakkaisia ammatillisia HOKSeja."
  [from to]
  (db-ops/query
    [queries/select-non-tuva-hoksit-created-between from to]
    {:row-fn hoks-from-sql}))

(defn select-non-tuva-hoksit-finished-between
  "Hakee tietokannasta ne HOKSit, jotka on merkattu valmiiksi annettujen
  ajankohtien välillä ja jotka eivät ole TUVA-HOKSeja tai TUVA-HOKSien kanssa
  rinnakkaisia ammatillisia HOKSeja."
  [from to]
  (db-ops/query
    [queries/select-non-tuva-hoksit-finished-between from to]
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

(defn set-amisherate-kasittelytilat-to-true!
  "Set both `aloitusherate_kasitelty` and `paattoherate_kasitelty` to `true` if
  both or either one had previsouly value `false`. For log printing purposes,
  `reason-msg` should tell why kasittelytilat will be set to `true`."
  [kasittelytilat reason-msg]
  ; Only set kasittelytilat if either kasittelytila is `false`.
  (when-not (and (:aloitusherate_kasitelty kasittelytilat)
                 (:paattoherate_kasitelty kasittelytilat))
    (log/info "Setting `aloitusherate_kasitelty` and `paattoherate_kasitelty`"
              "to `true`. Reason: " reason-msg)
    (update-amisherate-kasittelytilat!
      {:id (:id kasittelytilat)
       :aloitusherate_kasitelty true
       :paattoherate_kasitelty true})))

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

(defn delete-hoks-part!
  [part-type data connection timestamp]
  (case part-type
    :hyto
    (do
      (db-ops/shallow-delete-marking-updated!
        :hankittavat_yhteiset_tutkinnon_osat
        ["id = ? AND deleted_at IS NULL" (:id data)] connection timestamp)
      (doseq [hyto-osa (:osa-alueet data)]
        (delete-hoks-part! :hyto-osa hyto-osa connection timestamp)))
    :hyto-osa
    (do
      (db-ops/shallow-delete-marking-updated!
        :yhteisen_tutkinnon_osan_osa_alueet
        ["id = ? AND deleted_at IS NULL" (:id data)] connection timestamp)
      (db-ops/shallow-delete!
        :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
        ["yhteisen_tutkinnon_osan_osa_alue_id = ? AND deleted_at IS NULL"
         (:id data)] connection timestamp)
      (doseq [oh (:osaamisen-hankkimistavat data)]
        (delete-hoks-part! :oh oh connection timestamp)))
    :hato
    (do
      (db-ops/shallow-delete-marking-updated!
        :hankittavat_ammat_tutkinnon_osat
        ["id = ? AND deleted_at IS NULL" (:id data)] connection timestamp)
      (db-ops/shallow-delete!
        :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
        ["hankittava_ammat_tutkinnon_osa_id = ? AND deleted_at IS NULL"
         (:id data)] connection timestamp)
      (doseq [oh (:osaamisen-hankkimistavat data)]
        (delete-hoks-part! :oh oh connection timestamp)))
    :hpto
    (do
      (db-ops/shallow-delete-marking-updated!
        :hankittavat_paikalliset_tutkinnon_osat
        ["id = ? AND deleted_at IS NULL" (:id data)] connection timestamp)
      (db-ops/shallow-delete!
        :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
        ["hankittava_paikallinen_tutkinnon_osa_id = ? AND deleted_at IS NULL"
         (:id data)] connection timestamp)
      (doseq [oh (:osaamisen-hankkimistavat data)]
        (delete-hoks-part! :oh oh connection timestamp)))
    :oh
    (do
      (db-ops/shallow-delete-marking-updated!
        :osaamisen_hankkimistavat
        ["id = ? AND deleted_at IS NULL" (:id data)] connection timestamp)
      (when-let [tjk (:tyopaikalla-jarjestettava-koulutus data)]
        (db-ops/shallow-delete-marking-updated!
          :tyopaikalla_jarjestettavat_koulutukset
          ["id = ? AND deleted_at IS NULL" (:id data)] connection timestamp)))))

(defn shallow-delete-hoks
  "Asettaa HOKSin ja sen hankittavat tutkinnon osat poistetuiksi
  hoksin perusteella (shallow delete)."
  [hoks]
  (jdbc/with-db-transaction
    [connection (db-ops/get-db-connection)]
    (let [timestamp (java.util.Date.)]
      (db-ops/shallow-delete-marking-updated!
        :hoksit ["id = ? AND deleted_at IS NULL" (:id hoks)] connection
        timestamp)
      (doseq [hyto (:hankittavat-yhteiset-tutkinnon-osat hoks)]
        (delete-hoks-part! :hyto hyto connection timestamp))
      (doseq [hato (:hankittavat-ammat-tutkinnon-osat hoks)]
        (delete-hoks-part! :hato hato connection timestamp))
      (doseq [hpto (:hankittavat-paikalliset-tutkinnon-osat hoks)]
        (delete-hoks-part! :hpto hpto connection timestamp)))))

(defn undo-shallow-delete
  "Merkitsee HOKSin palautetuksi asettamalla nil:in sen deleted_at -kenttään."
  [hoks-id]
  (jdbc/with-db-transaction
    [connection (db-ops/get-db-connection)]
    (when-let [hoks (first (db-ops/query-in-tx
                             [(str "SELECT id, deleted_at FROM hoksit "
                                   "WHERE id = ? AND deleted_at IS NOT NULL")
                              hoks-id]
                             {:row-fn #(hoks-from-sql % #{:deleted_at})}
                             connection))]
      (when-let [deleted-at (:deleted-at hoks)]
        (let [now (java.util.Date.)
              undo-delete!
              (fn [table id]
                (db-ops/update! table {:deleted_at nil :updated_at now}
                                [(str "id = ? AND deleted_at = ?") id
                                 deleted-at] connection))
              undo-delete-junction-table!
              (fn [table id-field id]
                (db-ops/update! table {:deleted_at nil}
                                [(str id-field " = ? AND deleted_at = ?") id
                                 deleted-at] connection))]
          (undo-delete! :hoksit hoks-id)
          ; hyto
          (doseq [hyto (db-ops/query
                         [(str "SELECT id FROM "
                               "hankittavat_yhteiset_tutkinnon_osat "
                               "WHERE hoks_id = ? AND deleted_at = ?")
                          hoks-id deleted-at] {:row-fn db-ops/from-sql})]
            (undo-delete! :hankittavat_yhteiset_tutkinnon_osat (:id hyto))
            (doseq [hyto-osa (db-ops/query
                               [(str "SELECT id FROM "
                                     "yhteisen_tutkinnon_osan_osa_alueet "
                                     "WHERE yhteinen_tutkinnon_osa_id = ? "
                                     "AND deleted_at = ?")
                                (:id hyto) deleted-at]
                               {:row-fn db-ops/from-sql})]
              (undo-delete! :yhteisen_tutkinnon_osan_osa_alueet (:id hyto-osa))
              (undo-delete-junction-table!
                :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
                "yhteisen_tutkinnon_osan_osa_alue_id" (:id hyto-osa))
              (doseq [oh (db-ops/query
                           [(str "SELECT DISTINCT oh.id, "
                                 "oh.tyopaikalla_jarjestettava_koulutus_id "
                                 "FROM osaamisen_hankkimistavat oh "
                                 "JOIN yhteisen_tutkinnon_osan_osa_alueen_"
                                 "osaamisen_hankkimistavat o ON "
                                 "o.osaamisen_hankkimistapa_id = oh.id WHERE "
                                 "o.yhteisen_tutkinnon_osan_osa_alue_id = ? "
                                 "AND oh.deleted_at = ?")
                            (:id hyto-osa) deleted-at]
                           {:row-fn db-ops/from-sql})]
                (undo-delete! :osaamisen_hankkimistavat (:id oh))
                (when-let [tjk-id (:tyopaikalla-jarjestettava-koulutus-id oh)]
                  (undo-delete! :tyopaikalla_jarjestettavat_koulutukset
                                tjk-id)))))
          ; hato
          (doseq [hato (db-ops/query
                         [(str "SELECT id FROM "
                               "hankittavat_ammat_tutkinnon_osat "
                               "WHERE hoks_id = ? AND deleted_at = ?")
                          hoks-id deleted-at] {:row-fn db-ops/from-sql})]
            (undo-delete! :hankittavat_ammat_tutkinnon_osat (:id hato))
            (undo-delete-junction-table!
              :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
              "hankittava_ammat_tutkinnon_osa_id" (:id hato))
            (doseq [oh (db-ops/query
                         [(str "SELECT DISTINCT oh.id, "
                               "oh.tyopaikalla_jarjestettava_koulutus_id FROM "
                               "osaamisen_hankkimistavat oh "
                               "JOIN hankittavan_ammat_tutkinnon_osan_"
                               "osaamisen_hankkimistavat o ON "
                               "o.osaamisen_hankkimistapa_id = oh.id WHERE "
                               "o.hankittava_ammat_tutkinnon_osa_id = ? "
                               "AND oh.deleted_at = ?") (:id hato) deleted-at]
                         {:row-fn db-ops/from-sql})]
              (undo-delete! :osaamisen_hankkimistavat (:id oh))
              (when-let [tjk-id (:tyopaikalla-jarjestettava-koulutus-id oh)]
                (undo-delete! :tyopaikalla_jarjestettavat_koulutukset tjk-id))))
          ; hpto
          (doseq [hpto (db-ops/query
                         [(str "SELECT id FROM "
                               "hankittavat_paikalliset_tutkinnon_osat "
                               "WHERE hoks_id = ? AND deleted_at = ?")
                          hoks-id deleted-at] {:row-fn db-ops/from-sql})]
            (undo-delete! :hankittavat_paikalliset_tutkinnon_osat (:id hpto))
            (undo-delete-junction-table!
              :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
              "hankittava_paikallinen_tutkinnon_osa_id" (:id hpto))
            (doseq [oh (db-ops/query
                         [(str "SELECT DISTINCT oh.id, "
                               "oh.tyopaikalla_jarjestettava_koulutus_id FROM "
                               "osaamisen_hankkimistavat oh "
                               "JOIN hankittavan_paikallisen_tutkinnon_osan_"
                               "osaamisen_hankkimistavat o ON "
                               "o.osaamisen_hankkimistapa_id = oh.id WHERE "
                               "o.hankittava_ammat_tutkinnon_osa_id = ? "
                               "AND oh.deleted_at = ?") (:id hpto) deleted-at]
                         {:row-fn db-ops/from-sql})]
              (undo-delete! :osaamisen_hankkimistavat (:id oh))
              (when-let [tjk-id
                         (:tyopaikalla-jarjestettava-koulutus-id oh)]
                (undo-delete! :tyopaikalla_jarjestettavat_koulutukset
                              tjk-id)))))))))

(defn delete-opiskeluoikeus-by-oid
  "Poistaa opiskeluoikeuden tiedot indeksistä oidin perusteella."
  [opiskeluoikeus-oid]
  (db-ops/delete! :opiskeluoikeudet ["oid = ?" opiskeluoikeus-oid]))

(defn delete-hoks-by-hoks-id
  "Poistaa HOKSin pysyvästi id:n perusteella"
  [hoks-id]
  (let [hoks (select-hoks-by-id hoks-id)]
    (delete-opiskeluoikeus-by-oid (:opiskeluoikeus-oid hoks))
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

(defn delete-tyopaikkaohjaajan-yhteystiedot!
  "Poistaa työpaikkaohjaajan yhteystiedot yli kolme kuukautta sitten
   päättyneistä työpaikkajaksoista. Käsittelee max 5000 jaksoa kerrallaan.
   Palauttaa kyseisten jaksojen id:t (hankkimistapa-id) herätepalvelua varten."
  []
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (let [jaksot (jdbc/query conn
                             [queries/select-paattyneet-tyoelamajaksot-3kk 5000]
                             {:row-fn db-ops/from-sql})]
      (doseq [jakso jaksot]
        (log/info (str "Poistetaan ohjaajan yhteystiedot "
                       "(tyopaikalla_jarjestettavat_koulutukset.id = "
                       (:tjk-id jakso)
                       ")"))
        (db-ops/update! :tyopaikalla_jarjestettavat_koulutukset
                        {:vastuullinen_tyopaikka_ohjaaja_sahkoposti nil
                         :vastuullinen_tyopaikka_ohjaaja_puhelinnumero nil}
                        ["id = ?" (:tjk-id jakso)]
                        conn))
      (set (map :hankkimistapa-id jaksot)))))

(defn filter-by-oo-paattymispaiva
  "Suodattaa hoksin opiskeluoikeuden päättymispäivämäärän perusteella. Yli
   kolme kuukautta sitten päättyneet opiskeluoikeudet = true."
  [hoks]
  (if-let [opiskeluoikeus
           (k/get-opiskeluoikeus-info (:opiskeluoikeus-oid hoks))]
    (if-let [paattymispaiva (:päättymispäivä opiskeluoikeus)]
      (.isBefore (LocalDate/parse paattymispaiva)
                 (.minusMonths (LocalDate/now) 3))
      false)
    false))

(defn delete-opiskelijan-yhteystiedot!
  "Poistaa opiskelijan yhteystiedot yli kolme kuukautta sitten
   päättyneistä hokseista. Käsittelee max 500 hoksia kerrallaan. Palauttaa
   kyseisten tapausten hoks id:t herätepalvelua varten."
  []
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (let [hoksit (jdbc/query conn
                             [queries/select-vanhat-hoksit-having-yhteystiedot
                              500]
                             {:row-fn db-ops/from-sql})
          _ (log/info (str "Haettiin " (count hoksit) " hoksia kannasta"))
          paattyneet (filter filter-by-oo-paattymispaiva hoksit)]
      (log/info (str "Käsitellään " (count paattyneet) " päättynyttä hoksia"))
      (doseq [hoks paattyneet]
        (log/info (str "Poistetaan opiskelijan yhteystiedot (hoks.id = "
                       (:id hoks)
                       ")"))
        (update-hoks-by-id! (:id hoks) {:sahkoposti nil
                                        :puhelinnumero nil} conn))
      (let [ei-paattyneet (s/difference (set hoksit) (set paattyneet))]
        (log/info (str "Päivitetään aikaleima "
                       (count ei-paattyneet)
                       " hoksiin"))
        (doseq [hoks ei-paattyneet]
          (update-hoks-by-id! (:id hoks) {} conn)))
      (set (map :id paattyneet)))))
