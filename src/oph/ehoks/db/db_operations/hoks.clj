(ns oph.ehoks.db.db-operations.hoks
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as oo]
            [oph.ehoks.db.db-operations.oppija :as op]
            [oph.ehoks.external.organisaatio :as org]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

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
  (db-ops/from-sql m))

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
      [:vastuullinen-tyopaikka-ohjaaja :sahkoposti]
      :vastuullinen_tyopaikka_ohjaaja_puhelinnumero
      [:vastuullinen-tyopaikka-ohjaaja :puhelinnumero]}}))

(defn tyopaikalla-jarjestettava-koulutus-to-sql [m]
  (db-ops/to-sql
    m
    {:removals [:keskeiset-tyotehtavat]
     :replaces
     {[:vastuullinen-tyopaikka-ohjaaja :nimi]
      :vastuullinen-tyopaikka-ohjaaja-nimi
      [:vastuullinen-tyopaikka-ohjaaja :sahkoposti]
      :vastuullinen-tyopaikka-ohjaaja-sahkoposti
      [:vastuullinen-tyopaikka-ohjaaja :puhelinnumero]
      :vastuullinen-tyopaikka-ohjaaja-puhelinnumero}}))

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

(defn osaamisen-hankkimistapa-to-sql [m]
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

(defn muu-oppimisymparisto-from-sql [m]
  (db-ops/from-sql m {:removals [:id :osaamisen_hankkimistapa_id]}))

(defn keskeytymisajanjakso-from-sql [m]
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
  (db-ops/from-sql m))

(defn opiskeluvalmiuksia-tukevat-opinnot-from-sql [m]
  (db-ops/from-sql m {:removals [:hoks_id]}))

(defn hankittava-yhteinen-tutkinnon-osa-from-sql [m]
  (db-ops/from-sql m {:removals [:osa-alueet]}))

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

(defn select-hokses-greater-than-id [from-id amount updated-after]
  (db-ops/query [queries/select-hoksit-by-id-paged
                 from-id
                 updated-after
                 updated-after
                 amount]
                {:row-fn hoks-from-sql}))

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

(defn select-hoksit-created-between [from to]
  (db-ops/query
    [queries/select-hoksit-created-between from to]
    {:row-fn hoks-from-sql}))

(defn select-hoksit-finished-between [from to]
  (db-ops/query
    [queries/select-hoksit-finished-between from to]
    {:row-fn hoks-from-sql}))

(defn- generate-unique-eid []
  (loop [eid nil]
    (if (or (nil? eid) (seq (select-hoksit-eid-by-eid eid)))
      (recur (str (java.util.UUID/randomUUID)))
      eid)))

(defn insert-hoks!
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

(defn- select-keskeytymisajanjaksot [oht-id]
  (db-ops/query
    [queries/select-keskeytymisajanjaksot-by-osaamisen-hankkimistapa-id oht-id]
    {:row-fn db-ops/from-sql}))

(defn- get-and-assoc-data [osa-type]
  (fn [jakso]
    (assoc jakso
           :tyyppi osa-type
           :keskeytymisajanjaksot
           (select-keskeytymisajanjaksot (:hankkimistapa_id jakso)))))

(defn select-paattyneet-tyoelamajaksot [osa start end limit]
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

(defn update-osaamisen-hankkimistapa-tep-kasitelty [id to]
  (db-ops/update!
    :osaamisen_hankkimistavat
    {:tep_kasitelty to}
    ["id = ?" id]))

(defn update-amisherate-kasittelytilat-aloitusherate-kasitelty [hoks-id to]
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

(defn update-amisherate-kasittelytilat-paattoherate-kasitelty [hoks-id to]
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
  ([hoks-id]
    (insert-amisherate-kasittelytilat! hoks-id (db-ops/get-db-connection)))
  ([hoks-id db-conn]
    (first (jdbc/insert! db-conn
                         :amisherate_kasittelytilat
                         (db-ops/to-sql {:hoks-id hoks-id})))))

(defn update-amisherate-kasittelytilat!
  ([tilat]
    (update-amisherate-kasittelytilat! tilat (db-ops/get-db-connection)))
  ([tilat db-conn]
    (db-ops/update! :amisherate_kasittelytilat
                    (db-ops/to-sql (dissoc tilat :id))
                    ["id = ?" (:id tilat)] db-conn)))

(defn select-hoksit-with-kasittelemattomat-aloitusheratteet [start end limit]
  (db-ops/query
    [queries/select-hoksit-with-kasittelemattomat-aloitusheratteet
     start end limit]
    {:row-fn hoks-from-sql}))

(defn select-hoksit-with-kasittelemattomat-paattoheratteet [start end limit]
  (db-ops/query
    [queries/select-hoksit-with-kasittelemattomat-paattoheratteet
     start end limit]
    {:row-fn hoks-from-sql}))

(defn get-or-create-amisherate-kasittelytila-by-hoks-id! [hoks-id]
  (let [tila (first
               (db-ops/query
                 [queries/select-amisherate-kasittelytilat-by-hoks-id hoks-id]
                 {:row-fn db-ops/from-sql}))]
    (if (some? tila)
      tila
      (insert-amisherate-kasittelytilat! hoks-id))))

(defn select-count-all-hoks []
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

(defn undo-shallow-delete [hoks-id]
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
  [alkupvm alkupvm-loppu last-id limit]
  (db-ops/query
    [queries/select-paattyneet-kyselylinkit-by-date-and-type-temp
     alkupvm alkupvm-loppu last-id limit]
    {:row-fn db-ops/from-sql}))

(defn select-hoksit-by-ensikert-hyvaks-and-saavutettu-tiedot []
  (db-ops/query
    [queries/select-hoksit-by-ensikert-hyvaks-and-saavutettu-tiedot]
    {:row-fn db-ops/from-sql}))

(defn get-map [coll k]
  (if (sequential? k)
    (vec (map #(get coll %) k))
    (get coll k)))

(defn extract-from-joined-rows [unique-on fields rows]
  (mapv (fn [row] (reduce-kv #(assoc %1 %3 (get row %2)) {} fields))
        (sort #(compare (get-map %1 unique-on) (get-map %2 unique-on))
              (vals
                (dissoc (reduce #(assoc %1 (get-map %2 unique-on) %2) {} rows)
                        nil)))))
