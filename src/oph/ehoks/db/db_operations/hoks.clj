(ns oph.ehoks.db.db-operations.hoks
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]))

(defn oppilaitos-oid-from-sql [m]
  (:oppilaitos_oid m))

(defn hoks-from-sql [h]
  (db-ops/from-sql
    h))

(defn hoks-to-sql [h]
  (db-ops/to-sql
    h
    {:removals [:aiemmin-hankitut-ammat-tutkinnon-osat
                :aiemmin-hankitut-paikalliset-tutkinnon-osat
                :aiemmin-hankitut-yhteiset-tutkinnon-osat
                :hankittavat-ammat-tutkinnon-osat
                :hankittavat-yhteiset-tutkinnon-osat
                :opiskeluvalmiuksia-tukevat-opinnot
                :hankittavat-paikalliset-tutkinnon-osat]}))

(defn aiemmin-hankittu-ammat-tutkinnon-osa-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id]}))

(defn aiemmin-hankittu-ammat-tutkinnon-osa-to-sql [m]
  (db-ops/to-sql
    m
    {:removals [:tarkentavat-tiedot-naytto
                :tarkentavat-tiedot-osaamisen-arvioija]}))

(defn hankittava-paikallinen-tutkinnon-osa-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id]}))

(defn hankittava-paikallinen-tutkinnon-osa-to-sql [m]
  (db-ops/to-sql
    m {:removals [:osaamisen-osoittaminen :osaamisen-hankkimistavat]}))

(defn tyopaikalla-jarjestettava-koulutus-from-sql [m]
  (db-ops/from-sql
    m
    {:replaces
     {:vastuullinen_tyopaikka_ohjaaja_nimi
      [:vastuullinen-tyopaikka-ohjaaja :nimi]
      :vastuullinen_tyopaikka_ohjaaja_sahkoposti
      [:vastuullinen-tyopaikka-ohjaaja :sahkoposti]}}))

(defn tyopaikalla-jarjestettava-koulutus-to-sql [m]
  (db-ops/to-sql
    m
    {:removals [:keskeiset-tyotehtavat]
     :replaces
     {[:vastuullinen-tyopaikka-ohjaaja :nimi]
      :vastuullinen-tyopaikka-ohjaaja-nimi
      [:vastuullinen-tyopaikka-ohjaaja :sahkoposti]
      :vastuullinen-tyopaikka-ohjaaja-sahkoposti}}))

(defn henkilo-from-sql [m]
  (db-ops/from-sql
    m
    {:removals [:id :tyopaikalla_jarjestettava_koulutus_id]
     :replaces
     {:organisaatio_nimi [:organisaatio :nimi]
      :organisaatio_y_tunnus [:organisaatio :y-tunnus]}}))

(defn henkilo-to-sql [m]
  (db-ops/to-sql
    m {:replaces {[:organisaatio :nimi] :organisaatio_nimi
                  [:organisaatio :y-tunnus] :organisaatio_y_tunnus}}))

(defn osaamisen-hankkimistapa-from-sql [m]
  (db-ops/from-sql
    m
    {:replaces
     {:jarjestajan_edustaja_nimi [:jarjestajan-edustaja :nimi]
      :jarjestajan_edustaja_rooli [:jarjestajan-edustaja :rooli]
      :jarjestajan_edustaja_oppilaitos_oid
      [:jarjestajan-edustaja :oppilaitos-oid]
      :hankkijan_edustaja_nimi [:hankkijan-edustaja :nimi]
      :hankkijan_edustaja_rooli [:hankkijan-edustaja :rooli]
      :hankkijan_edustaja_oppilaitos_oid
      [:hankkijan-edustaja :oppilaitos-oid]}}))

(defn osaamisen-hankkimistapa-to-sql [m]
  (db-ops/to-sql
    m
    {:removals [:muut-oppimisymparistot
                :tyopaikalla-jarjestettava-koulutus]
     :replaces
     {[:jarjestajan-edustaja :nimi] :jarjestajan-edustaja-nimi
      [:jarjestajan-edustaja :rooli] :jarjestajan-edustaja-rooli
      [:jarjestajan-edustaja :oppilaitos-oid]
      :jarjestajan-edustaja-oppilaitos-oid
      [:hankkijan-edustaja :nimi] :hankkijan-edustaja-nimi
      [:hankkijan-edustaja :rooli] :hankkijan-edustaja-rooli
      [:hankkijan-edustaja :oppilaitos-oid]
      :hankkijan-edustaja-oppilaitos-oid}}))

(defn muu-oppimisymparisto-from-sql [m]
  (db-ops/from-sql m {:removals [:id :osaamisen_hankkimistapa_id]}))

(defn osaamisen-osoittaminen-from-sql [m]
  (db-ops/from-sql
    m
    {:replaces {:jarjestaja_oppilaitos_oid [:jarjestaja :oppilaitos-oid]}}))

(defn osaamisen-osoittaminen-to-sql [m]
  (db-ops/to-sql
    m
    {:removals [:nayttoymparisto
                :sisallon-kuvaus
                :koulutuksen-jarjestaja-osaamisen-arvioijat
                :tyoelama-osaamisen-arvioijat
                :osa-alueet
                :yksilolliset-kriteerit]
     :replaces {[:jarjestaja :oppilaitos-oid] :jarjestaja-oppilaitos-oid}}))

(defn koodi-uri-from-sql [m]
  (db-ops/from-sql m {:removals [:id]}))

(defn koulutuksen-jarjestaja-osaamisen-arvioija-from-sql [m]
  (db-ops/from-sql
    m {:replaces {:oppilaitos_oid [:organisaatio :oppilaitos-oid]}
       :removals [:id]}))

(defn koulutuksen-jarjestaja-osaamisen-arvioija-to-sql [m]
  (db-ops/to-sql
    m {:replaces {[:organisaatio :oppilaitos-oid] :oppilaitos-oid}}))

(defn tyoelama-arvioija-from-sql [m]
  (db-ops/from-sql
    m {:replaces {:organisaatio_nimi [:organisaatio :nimi]
                  :organisaatio_y_tunnus [:organisaatio :y-tunnus]}
       :removals [:id]}))

(defn tyoelama-arvioija-to-sql [m]
  (db-ops/to-sql
    m {:replaces {[:organisaatio :nimi] :organisaatio-nimi
                  [:organisaatio :y-tunnus] :organisaatio-y-tunnus}}))

(defn nayttoymparisto-from-sql [m]
  (db-ops/from-sql m {:removals [:id]}))

(defn tyotehtava-from-sql [m]
  (get m :tyotehtava))

(defn sisallon-kuvaus-from-sql [m]
  (get m :sisallon_kuvaus))

(defn yksilolliset-kriteerit-from-sql [m]
  (get m :yksilollinen_kriteeri))

(defn aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id]
                      :replaces
                      {:lahetetty_arvioitavaksi
                       [:tarkentavat_tiedot_osaamisen_arvioija
                        :lahetetty-arvioitavaksi]}}))

(defn aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql [m]
  (db-ops/to-sql m {:removals [:tarkentavat-tiedot-naytto
                               :tarkentavat-tiedot-osaamisen-arvioija]
                    :replaces {[:tarkentavat-tiedot-osaamisen-arvioija
                                :lahetetty-arvioitavaksi]
                               :lahetetty-arvioitavaksi}}))

(defn aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql [m]
  (db-ops/from-sql m {:removals [:aiemmin_hankittu_yhteinen_tutkinnon_osa_id]}))

(defn aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql [m]
  (db-ops/to-sql m {:removals [:tarkentavat-tiedot-naytto
                               :tarkentavat-tiedot-osaamisen-arvioija]}))

(defn aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql [m]
  (db-ops/to-sql m {:removals [:osa-alueet
                               :tarkentavat-tiedot-naytto
                               :tarkentavat-tiedot-osaamisen-arvioija]
                    :replaces {[:tarkentavat-tiedot-osaamisen-arvioija
                                :lahetetty-arvioitavaksi]
                               :lahetetty-arvioitavaksi}}))

(defn aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id]
                      :replaces
                      {:lahetetty_arvioitavaksi
                       [:tarkentavat_tiedot_osaamisen_arvioija
                        :lahetetty-arvioitavaksi]}}))

(defn todennettu-arviointi-lisatiedot-to-sql [m]
  (db-ops/to-sql m {:removals [:aiemmin-hankitun-osaamisen-arvioijat]}))

(defn todennettu-arviointi-lisatiedot-from-sql [m]
  (db-ops/from-sql m))

(defn hankittava-ammat-tutkinnon-osa-to-sql [m]
  (db-ops/to-sql m {:removals [:osaamisen-osoittaminen
                               :osaamisen-hankkimistavat]}))

(defn hankittava-ammat-tutkinnon-osa-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id]}))

(defn opiskeluvalmiuksia-tukevat-opinnot-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id]}))

(defn hankittava-yhteinen-tutkinnon-osa-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id :osa-alueet]}))

(defn hankittava-yhteinen-tutkinnon-osa-to-sql [m]
  (db-ops/to-sql m {:removals [:osa-alueet]}))

(defn yhteisen-tutkinnon-osan-osa-alue-to-sql [m]
  (db-ops/to-sql m {:removals [:osaamisen-hankkimistavat
                               :osaamisen-osoittaminen]}))

(defn yhteisen-tutkinnon-osan-osa-alue-from-sql [m]
  (db-ops/from-sql m))

(defn osaamistavoite-from-sql [m] (get m :osaamistavoite))

(defn select-hoksit []
  (db-ops/query
    [queries/select-hoksit]
    :row-fn hoks-from-sql))

(defn select-hoks-by-oppija-oid [oid]
  (db-ops/query
    [queries/select-hoksit-by-oppija-oid oid]
    :row-fn hoks-from-sql))

(defn select-hoks-by-id [id]
  (first
    (db-ops/query
      [queries/select-hoksit-by-id id]
      {:row-fn hoks-from-sql})))

(defn select-hoks-by-eid [eid]
  (first (db-ops/query
           [queries/select-hoksit-by-eid eid]
           {:row-fn hoks-from-sql})))

(defn select-hoksit-by-opiskeluoikeus-oid [oid]
  (db-ops/query
    [queries/select-hoksit-by-opiskeluoikeus-oid oid]
    {:row-fn hoks-from-sql}))

(defn- select-hoksit-eid-by-eid [eid]
  (db-ops/query
    [queries/select-hoksit-eid-by-eid eid]
    {}))

(defn- generate-unique-eid []
  (loop [eid nil]
    (if (or (nil? eid) (seq (select-hoksit-eid-by-eid eid)))
      (recur (str (java.util.UUID/randomUUID)))
      eid)))

(defn insert-hoks! [hoks]
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (when
     (seq (jdbc/query conn [queries/select-hoksit-by-opiskeluoikeus-oid
                            (:opiskeluoikeus-oid hoks)]))
      (throw (ex-info
               "HOKS with given opiskeluoikeus already exists"
               {:error :duplicate})))
    (let [eid (generate-unique-eid)]
      (first
        (jdbc/insert! conn :hoksit (hoks-to-sql (assoc hoks :eid eid)))))))

(defn update-hoks-by-id!
  ([id hoks]
    (db-ops/update! :hoksit (hoks-to-sql hoks)
                    ["id = ? AND deleted_at IS NULL" id]))
  ([id hoks db]
    (db-ops/update! :hoksit (hoks-to-sql hoks)
                    ["id = ? AND deleted_at IS NULL" id] db)))

(defn select-hoks-oppijat-without-index []
  (db-ops/query
    [queries/select-hoks-oppijat-without-index]))

(defn select-hoks-oppijat-without-index-count []
  (db-ops/query
    [queries/select-hoks-oppijat-without-index-count]))

(defn select-hoks-opiskeluoikeudet-without-index []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-index]))

(defn select-hoks-opiskeluoikeudet-without-index-count []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-index-count]))

(defn select-kyselylinkit-by-oppija-oid [oid]
  (db-ops/query
    [queries/select-kyselylinkit-by-oppija-oid oid]
    {:row-fn db-ops/from-sql}))

(defn select-paattyneet-tyoelamajaksot [osa]
  (case osa
    "hpto" (map #(assoc % :tyyppi "hpto")
                (db-ops/query
                  [queries/select-paattyneet-tyoelamajaksot-hpto]))
    "hato" (map #(assoc % :tyyppi "hato")
                (db-ops/query
                  [queries/select-paattyneet-tyoelamajaksot-hato]))
    "hyto" (map #(assoc % :tyyppi "hyto")
                (db-ops/query
                  [queries/select-paattyneet-tyoelamajaksot-hyto]))))
