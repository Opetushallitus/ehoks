(ns oph.ehoks.db.postgresql
  (:require [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.hoks :as h]
            [clj-time.coerce :as c]
            [oph.ehoks.db.queries :as queries]
            [clojure.data.json :as json])
  (:import [org.postgresql.util PGobject]))

(extend-protocol jdbc/ISQLValue
  java.time.LocalDate
  (sql-value [value] (java.sql.Date/valueOf value))
  java.util.Date
  (sql-value [value] (c/to-sql-time value))
  clojure.lang.IPersistentMap
  (sql-value [value]
    (doto (PGobject.)
      (.setType "json")
      (.setValue (json/write-str value)))))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [o _ _]
    (.toLocalDate o))
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (if (= type "json")
        (json/read-str value :key-fn keyword)
        value))))

(defn get-db-connection []
  {:dbtype (:db-type config)
   :dbname (:db-name config)
   :host (:db-server config)
   :port (:db-port config)
   :user (:db-username config)
   :password (:db-password config)})

(defn insert-empty! [t]
  (jdbc/execute!
    (get-db-connection)
    (format
      "INSERT INTO %s DEFAULT VALUES" (name t))))

(defn query
  ([queries opts]
    (jdbc/query (get-db-connection) queries opts))
  ([queries]
    (query queries {}))
  ([queries arg & opts]
    (query queries (apply hash-map arg opts))))

(defn insert! [t v]
  (if (seq v)
    (jdbc/insert! (get-db-connection) t v)
    (insert-empty! t)))

(defn insert-one! [t v] (first (insert! t v)))

(defn update!
  ([table values where-clause]
    (jdbc/update! (get-db-connection) table values where-clause))
  ([table values where-clause db]
    (jdbc/update! db table values where-clause)))

(defn shallow-delete!
  ([table where-clause]
    (update! table {:deleted_at (java.util.Date.)} where-clause))
  ([table where-clause db-conn]
    (update! table {:deleted_at (java.util.Date.)} where-clause db-conn)))

(defn delete!
  [table where-clause]
  (jdbc/delete! (get-db-connection) table where-clause))

(defn execute! [where-clause]
  (jdbc/execute! (get-db-connection) where-clause))

(defn db-do-prepared! [where-clause]
  (jdbc/db-do-prepared (get-db-connection) where-clause))

(defn insert-multi! [t v]
  (jdbc/insert-multi! (get-db-connection) t v))

(defn select-hoksit []
  (query
    [queries/select-hoksit]
    :row-fn h/hoks-from-sql))

(defn select-hoks-by-oppija-oid [oid]
  (query
    [queries/select-hoksit-by-oppija-oid oid]
    :row-fn h/hoks-from-sql))

(defn select-hoks-by-id [id]
  (first
    (query
      [queries/select-hoksit-by-id id]
      {:row-fn h/hoks-from-sql})))

(defn select-hoks-by-eid [eid]
  (first
    (query
      [queries/select-hoksit-by-eid eid]
      {:row-fn h/hoks-from-sql})))

(defn select-hoksit-eid-by-eid [eid]
  (query
    [queries/select-hoksit-eid-by-eid eid]
    {}))

; (defn select-ahyto-ids-by-hoks-id [hoks-id]
;   (vec
;     (query
;       [queries/select-ahyto-ids-by-hoks-id hoks-id]
;       {:row-fn h/id-from-sql})))
;
; (defn select-ahyto-osa-alue-ids-by-ahyto-id [ahyto-id]
;   (vec
;     (query
;       [queries/select-ahyto-osa-alue-ids-by-ahyto-id ahyto-id]
;       {:row-fn h/id-from-sql})))

; (defn select-hyto-ids-by-hoks-id [hoks-id]
;   (vec
;     (query
;       [queries/select-hyto-ids-by-hoks-id hoks-id]
;       {:row-fn h/id-from-sql})))
;
; (defn select-hyto-osa-alue-ids-by-hyto-id [hyto-id]
;   (vec
;     (query
;       [queries/select-hyto-osa-alue-ids-by-hyto-id hyto-id]
;       {:row-fn h/id-from-sql})))

; (defn select-ahato-ids-by-hoks-id [hoks-id]
;   (vec
;     (query
;       [queries/select-ahato-ids-by-hoks-id hoks-id]
;       {:row-fn h/id-from-sql})))
;
; (defn select-ahpto-ids-by-hoks-id [hoks-id]
;   (vec
;     (query
;       [queries/select-ahpto-ids-by-hoks-id hoks-id]
;       {:row-fn h/id-from-sql})))
;
; (defn select-hato-ids-by-hoks-id [hoks-id]
;   (vec
;     (query
;       [queries/select-hato-ids-by-hoks-id hoks-id]
;       {:row-fn h/id-from-sql})))
;
; (defn select-hpto-ids-by-hoks-id [hoks-id]
;   (vec
;     (query
;       [queries/select-hpto-ids-by-hoks-id hoks-id]
;       {:row-fn h/id-from-sql})))
;
; (defn select-oo-ids-by-ahpto-id [ahpto-id]
;   (vec
;     (query
;       [queries/select-oo-ids-by-ahpto-id ahpto-id]
;       {:row-fn h/osaamisen-osoittaminen-id-from-sql})))

; (defn select-oo-ids-by-ahyto-id [ahyto-id]
;   (vec
;     (query
;       [queries/select-oo-ids-by-ahyto-id ahyto-id]
;       {:row-fn h/osaamisen-osoittaminen-id-from-sql})))
;
; (defn select-oo-ids-by-ahyto-osa-alue-id [ahyto-osa-alue-id]
;   (vec
;     (query
;       [queries/select-oo-ids-by-ahyto-osa-alue-id ahyto-osa-alue-id]
;       {:row-fn h/osaamisen-osoittaminen-id-from-sql})))

; (defn select-oo-ids-by-ahato-id [ahato-id]
;   (vec
;     (query
;       [queries/select-oo-ids-by-ahato-id ahato-id]
;       {:row-fn h/osaamisen-osoittaminen-id-from-sql})))
;
; (defn select-oo-ids-by-hato-id [hato-id]
;   (vec
;     (query
;       [queries/select-oo-ids-by-hato-id hato-id]
;       {:row-fn h/osaamisen-osoittaminen-id-from-sql})))
;
; (defn select-oo-ids-by-hpto-id [hpto-id]
;   (vec
;     (query
;       [queries/select-oo-ids-by-hpto-id hpto-id]
;       {:row-fn h/osaamisen-osoittaminen-id-from-sql})))

; (defn select-oo-ids-by-hyto-osa-alue-id [hyto-osa-alue-id]
;   (vec
;     (query
;       [queries/select-oo-ids-by-ahyto-osa-alue-id hyto-osa-alue-id]
;       {:row-fn h/osaamisen-osoittaminen-id-from-sql})))


; (defn delete-hoksit2-by-id! [hoks-id]
;   (let [
;         ahato-ids (select-ahato-ids-by-hoks-id hoks-id)
;         ahato-osaamisen-osoittamiset-ids
;         (into [] cat
;               (mapv select-oo-ids-by-ahato-id ahato-ids))
;         ahpto-ids (select-ahpto-ids-by-hoks-id hoks-id)
;         ahpto-osaamisen-osoittamiset-ids
;         (into [] cat
;               (mapv select-oo-ids-by-ahpto-id ahpto-ids))
;         hato-ids (select-hato-ids-by-hoks-id hoks-id)
;         hato-osaamisen-osoittamiset-ids
;         (into [] cat
;               (mapv select-oo-ids-by-hato-id ahyto-ids))
;         hpto-ids (select-hpto-ids-by-hoks-id hoks-id)
;         hpto-osaamisen-osoittamiset-ids
;         (into [] cat
;               (mapv select-oo-ids-by-hpto-id hpto-ids))
;
;
;         hyto-ids (select-hyto-ids-by-hoks-id hoks-id)
;         hyto-osa-alue-ids
;         (into [] cat
;               (mapv select-hyto-osa-alue-ids-by-hyto-id hyto-ids))
;         hyto-osa-alue-oo-ids
;         (into [] cat
;               (mapv select-oo-ids-by-hyto-osa-alue-id hyto-osa-alue-ids))
;         oo-ids
;         (vec
;           (concat ahyto-osaamisen-osoittamiset-ids
;                   ahyto-osa-alue-oo-ids ahpto-osaamisen-osoittamiset-ids
;                   ahato-osaamisen-osoittamiset-ids
;                   ahpto-osaamisen-osoittamiset-ids
;                   hato-osaamisen-osoittamiset-ids
;                   hpto-osaamisen-osoittamiset-ids
;                   hyto-osa-alue-oo-ids))]
;     (query
;       [queries/delete-hoksit-by-id hoks-id]
;       {})
;     (map delete-osaamisen-osoittaminen-and-nayttoymparisto-by-oo-id
;          oo-ids)))

; (defn delete-ahyto-by-id! [hoks-id]
;   (query
;     [queries/delete-oo-ahyto-by-hoks-id hoks-id]
;     {}))


(defn delete-todennettu-arviointi-lisatiedot-by-hoks-id! [hoks-id]
  (do
  (query
    [queries/delete-ahato-todennettu-arviointi-lisatiedot-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-ahpto-todennettu-arviointi-lisatiedot-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-ahyto-todennettu-arviointi-lisatiedot-by-hoks-id hoks-id]
    {})))

(defn delete-tyoelama-osaamisen-arvioijat-by-hoks-id! [hoks-id]
  (do
  (query
    [queries/delete-ahyto-tyoelama-arvioijat-by-yto-osa-alue-by-hoks-id hoks-id]
    {})
    (query
      [queries/delete-ahyto-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-hyto-tyoelama-arvioijat-by-yto-osa-alue-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-hpto-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-hato-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-ahato-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-ahpto-tyoelama-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})))

(defn delete-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hoks-id! [hoks-id]
  (do
    (query
      [queries/delete-ahyto-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-ahyto-koulutuksen-jarjestaja-arvioijat-by-yto-osa-alue-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-hyto-koulutuksen-jarjestaja-arvioijat-by-yto-osa-alue-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-hpto-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-hato-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-ahato-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-ahpto-koulutuksen-jarjestaja-arvioijat-tutkinnon-osa-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-ahpto-koulutuksen-jarjestaja-arvioijat-by-todennettu-arviointi hoks-id]
      {})
    (query
      [queries/delete-ahato-koulutuksen-jarjestaja-arvioijat-by-todennettu-arviointi hoks-id]
      {})
    (query
      [queries/delete-ahyto-koulutuksen-jarjestaja-arvioijat-by-todennettu-arviointi hoks-id]
      {})))

(defn delete-osaamisen-osoittamiset-by-hoks-id! [hoks-id]
  (do
    (query
      [queries/delete-oo-by-ahyto-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-oo-by-ahyto-yto-osa-alue-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-oo-by-hyto-yto-osa-alue-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-oo-by-ahato-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-oo-by-hato-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-oo-by-hpto-by-hoks-id hoks-id]
      {})
    (query
      [queries/delete-oo-by-ahpto-by-hoks-id hoks-id]
      {})))

(defn delete-nayttoymparistot-by-hoks-id! [hoks-id]
(do
  (query
    [queries/delete-hyto-nayttoymparisto-osa-alueet-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-ahyto-nayttoymparisto-tutkinnon-osa-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-ahyto-nayttoymparisto-osa-alueet-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-ahpto-nayttoymparisto-tutkinnon-osa-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-hpto-nayttoymparisto-tutkinnon-osa-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-ahato-nayttoymparisto-tutkinnon-osa-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-hato-nayttoymparisto-tutkinnon-osa-by-hoks-id hoks-id]
    {})))

(defn delete-osaamisen-hankimistavat-by-hoks-id! [hoks-id]
(do
  (query
    [queries/delete-hyto-osaamisen-hankkimistavat-yto-osa-alue-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-hato-osaamisen-hankkimistavat-tutkinnon-osa-by-hoks-id hoks-id]
    {})
  (query
    [queries/delete-hpto-osaamisen-hankkimistavat-tutkinnon-osa-by-hoks-id hoks-id]
    {})))

(defn delete-hoksit-by-id! [hoks-id]
(do
  (delete-tyoelama-osaamisen-arvioijat-by-hoks-id! hoks-id)
  (delete-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hoks-id! hoks-id)
  (delete-nayttoymparistot-by-hoks-id! hoks-id)
  (delete-osaamisen-hankimistavat-by-hoks-id! hoks-id)
  (delete-osaamisen-osoittamiset-by-hoks-id! hoks-id)
  (delete-todennettu-arviointi-lisatiedot-by-hoks-id! hoks-id)
  (query
    [queries/delete-hoksit-by-id hoks-id]
    {})))

(defn select-hoksit-by-opiskeluoikeus-oid [oid]
  (query
    [queries/select-hoksit-by-opiskeluoikeus-oid oid]
    {:row-fn h/hoks-from-sql}))

(defn generate-unique-eid []
  (loop [eid nil]
    (if (or (nil? eid) (seq (select-hoksit-eid-by-eid eid)))
      (recur (str (java.util.UUID/randomUUID)))
      eid)))

(defn insert-hoks! [hoks]
  (jdbc/with-db-transaction
    [conn (get-db-connection)]
    (when
     (seq (jdbc/query conn [queries/select-hoksit-by-opiskeluoikeus-oid
                            (:opiskeluoikeus-oid hoks)]))
      (throw (ex-info
               "HOKS with given opiskeluoikeus already exists"
               {:error :duplicate})))
    (let [eid (generate-unique-eid)]
      (first
        (jdbc/insert! conn :hoksit (h/hoks-to-sql (assoc hoks :eid eid)))))))

(defn update-hoks-by-id!
  ([id hoks]
    (update! :hoksit (h/hoks-to-sql hoks) ["id = ? AND deleted_at IS NULL" id]))
  ([id hoks db]
    (update! :hoksit (h/hoks-to-sql hoks) ["id = ? AND deleted_at IS NULL" id]
             db)))

(defn select-hoks-oppijat-without-index []
  (query
    [queries/select-hoks-oppijat-without-index]))

(defn select-hoks-oppijat-without-index-count []
  (query
    [queries/select-hoks-oppijat-without-index-count]))

(defn select-hoks-opiskeluoikeudet-without-index []
  (query
    [queries/select-hoks-opiskeluoikeudet-without-index]))

(defn select-hoks-opiskeluoikeudet-without-index-count []
  (query
    [queries/select-hoks-opiskeluoikeudet-without-index-count]))

(defn select-opiskeluoikeudet-without-tutkinto []
  (query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto]))

(defn select-opiskeluoikeudet-without-tutkinto-count []
  (query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto-count]))

(defn select-opiskeluoikeudet-by-oppija-oid [oppija-oid]
  (query
    [queries/select-opiskeluoikeudet-by-oppija-oid oppija-oid]
    {:row-fn h/from-sql}))

(defn select-oppija-by-oid [oppija-oid]
  (first
    (query
      [queries/select-oppijat-by-oid oppija-oid]
      {:row-fn h/from-sql})))

(defn select-opiskeluoikeus-by-oid [oid]
  (first
    (query
      [queries/select-opiskeluoikeudet-by-oid oid]
      {:row-fn h/from-sql})))

(defn insert-oppija [oppija]
  (insert-one! :oppijat (h/to-sql oppija)))

(defn update-oppija! [oid oppija]
  (update!
    :oppijat
    (h/to-sql oppija)
    ["oid = ?" oid]))

(defn insert-opiskeluoikeus [opiskeluoikeus]
  (insert-one! :opiskeluoikeudet (h/to-sql opiskeluoikeus)))

(defn update-opiskeluoikeus! [oid opiskeluoikeus]
  (update!
    :opiskeluoikeudet
    (h/to-sql opiskeluoikeus)
    ["oid = ?" oid]))

(defn select-todennettu-arviointi-lisatiedot-by-id [id]
  (first
    (query
      [queries/select-todennettu-arviointi-lisatiedot-by-id id]
      {:row-fn h/todennettu-arviointi-lisatiedot-from-sql})))

(defn insert-todennettu-arviointi-lisatiedot! [m]
  (insert-one!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql m)))

(defn select-arvioijat-by-todennettu-arviointi-id [id]
  (query
    [queries/select-arvioijat-by-todennettu-arviointi-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn insert-todennettu-arviointi-arvioijat! [tta-id arvioija-id]
  (insert-one!
    :todennettu_arviointi_arvioijat
    {:todennettu_arviointi_lisatiedot_id tta-id
     :koulutuksen_jarjestaja_osaamisen_arvioija_id arvioija-id}))

(defn insert-koulutuksen-jarjestaja-osaamisen-arvioija! [m]
  (insert-one!
    :koulutuksen_jarjestaja_osaamisen_arvioijat
    (h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql m)))

(defn insert-koulutuksen-jarjestaja-osaamisen-arvioijat! [c]
  (insert-multi!
    :koulutuksen_jarjestaja_osaamisen_arvioijat
    (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c)))

(defn select-tarkentavat-tiedot-naytto-by-ahpto-id [oopto-id]
  (query [queries/select-osaamisen-osoittamiset-by-oopto-id oopto-id]
         {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ooato-id
  "Aiemmin hankitun ammat tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ooato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-aiemmin-hankitun-ammat-tutkinnon-osan-naytto! [ooato-id n]
  (insert-one!
    :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    {:aiemmin_hankittu_ammat_tutkinnon_osa_id ooato-id
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-ahyto-osaamisen-osoittaminen! [ahyto-id n]
  (insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id ahyto-id
     :osaamisen_osoittaminen_id (:id n)}))

(defn insert-koodisto-koodi! [m]
  (insert-one!
    :koodisto_koodit
    (h/to-sql m)))

(defn insert-osaamisen-osoittamisen-osa-alue! [naytto-id koodi-id]
  (insert-one!
    :osaamisen_osoittamisen_osa_alueet
    {:osaamisen_osoittaminen_id naytto-id
     :koodisto_koodi_id koodi-id}))

(defn select-osa-alueet-by-osaamisen-osoittaminen [naytto-id]
  (query
    [queries/select-osa-alueet-by-osaamisen-osoittaminen naytto-id]
    {:row-fn h/koodi-uri-from-sql}))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id [id]
  (->
    (query [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-id
            id])
    first
    h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-ammat-tutkinnon-osa-from-sql}))

(defn insert-aiemmin-hankittu-ammat-tutkinnon-osa! [m]
  (insert-one!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql m)))

(defn insert-aiemmin-hankitut-ammat-tutkinnon-osat! [c]
  (insert-multi!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (map h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql c)))

(defn select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql}))

(defn select-hankittava-paikallinen-tutkinnon-osa-by-id [id]
  (first
    (query
      [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-id id]
      {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql})))

(defn insert-hankittavat-paikalliset-tutkinnon-osat! [c]
  (insert-multi!
    :hankittavat_paikalliset_tutkinnon_osat
    (map h/hankittava-paikallinen-tutkinnon-osa-to-sql c)))

(defn insert-hankittava-paikallinen-tutkinnon-osa! [m]
  (insert-one!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)))

(defn update-hankittava-paikallinen-tutkinnon-osa-by-id! [id m]
  (update!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id [hoks-id db-conn]
  (shallow-delete!
    :hankittavat_paikalliset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn select-osaamisen-osoittamiset-by-ppto-id
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ppto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn delete-osaamisen-hankkimistavat-by-hpto-id!
  "hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn delete-osaamisen-osoittamiset-by-ppto-id!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn insert-tho-tyotehtavat!
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [tho c]
  (insert-multi!
    :tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
    (map
      #(hash-map
         :tyopaikalla_jarjestettava_koulutus_id (:id tho)
         :tyotehtava %)
      c)))

(defn select-tyotehtavat-by-tho-id
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [id]
  (query
    [queries/select-tyotehtavat-by-tho-id id]
    {:row-fn h/tyotehtava-from-sql}))

(defn insert-tyopaikalla-jarjestettava-koulutus! [o]
  (when (some? o)
    (let [o-db (insert-one!
                 :tyopaikalla_jarjestettavat_koulutukset
                 (h/tyopaikalla-jarjestettava-koulutus-to-sql o))]
      (insert-tho-tyotehtavat! o-db (:keskeiset-tyotehtavat o))
      o-db)))

(defn select-tyopaikalla-jarjestettava-koulutus-by-id [id]
  (first
    (query
      [queries/select-tyopaikalla-jarjestettavat-koulutukset-by-id id]
      {:row-fn h/tyopaikalla-jarjestettava-koulutus-from-sql})))

(defn insert-osaamisen-hankkimistavan-muut-oppimisymparistot! [oh c]
  (insert-multi!
    :muut_oppimisymparistot
    (map
      #(h/to-sql
         (assoc % :osaamisen-hankkimistapa-id (:id oh)))
      c)))

(defn select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id [id]
  (query
    [queries/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id id]
    {:row-fn h/muu-oppimisymparisto-from-sql}))

(defn insert-osaamisen-hankkimistapa! [oh]
  (insert-one!
    :osaamisen_hankkimistavat
    (h/osaamisen-hankkimistapa-to-sql oh)))

(defn insert-hankittavan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
  [ppto oh]
  (insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_hankkimistapa_id (:id oh)}))

(defn select-osaamisen-hankkimistavat-by-hpto-id
  "hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (query
    [queries/select-osaamisen-hankkmistavat-by-ppto-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-osaamisen-osoittaminen! [m]
  (insert-one!
    :osaamisen_osoittamiset
    (h/osaamisen-osoittaminen-to-sql m)))

(defn insert-hpto-osaamisen-osoittaminen!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näyttö"
  [ppto h]
  (insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_osoittaminen_id (:id h)}))

(defn insert-ppto-osaamisen-osoittamiset!
  "hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [ppto c]
  (let [h-col (insert-multi!
                :osaamisen_osoittamiset
                (map h/osaamisen-osoittaminen-to-sql c))]
    (insert-multi!
      :hankittavan_paikallisen_tutkinnon_osan_naytto
      (map #(hash-map
              :hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
              :osaamisen_osoittaminen_id (:id %))
           h-col))
    h-col))

(defn insert-osaamisen-osoittamisen-koulutuksen-jarjestaja-osaamisen-arvioija!
  [hon c]
  (let [kja-col (insert-multi!
                  :koulutuksen_jarjestaja_osaamisen_arvioijat
                  (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c))]
    (insert-multi!
      :osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
      (map #(hash-map
              :osaamisen_osoittaminen_id (:id hon)
              :koulutuksen_jarjestaja_osaamisen_arvioija_id (:id %))
           kja-col))
    kja-col))

(defn select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id
  "Hankitun osaamisen näytön koulutuksen järjestäjän arvioijat"
  [id]
  (query
    [queries/select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn insert-tyoelama-arvioija! [arvioija]
  (insert-one!
    :tyoelama_osaamisen_arvioijat
    (h/tyoelama-arvioija-to-sql arvioija)))

(defn insert-osaamisen-osoittamisen-tyoelama-arvioija! [hon arvioija]
  (insert-one!
    :osaamisen_osoittamisen_tyoelama_arvioija
    {:osaamisen_osoittaminen_id (:id hon)
     :tyoelama_arvioija_id (:id arvioija)}))

(defn select-tyoelama-osaamisen-arvioijat-by-hon-id
  "Hankitun osaamisen näytön työelemän arvioijat"
  [id]
  (query
    [queries/select-tyoelama-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/tyoelama-arvioija-from-sql}))

(defn insert-osaamisen-osoittamisen-sisallot! [hon c]
  (insert-multi!
    :osaamisen_osoittamisen_sisallot
    (map #(hash-map :osaamisen_osoittaminen_id (:id hon) :sisallon_kuvaus %)
         c)))

(defn select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id [id]
  (query
    [queries/select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/sisallon-kuvaus-from-sql}))

(defn insert-osaamisen-osoittamisen-yksilolliset-kriteerit! [hon c]
  (insert-multi!
    :osaamisen_osoittamisen_yksilolliset_kriteerit
    (map #(hash-map :osaamisen_osoittaminen_id (:id hon)
                    :yksilollinen_kriteeri %) c)))

(defn select-osaamisen-osoittamisen-kriteerit-by-osaamisen-osoittaminen-id [id]
  (query
    [queries/select-osaamisen-osoittamisen-kriteeri-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/yksilolliset-kriteerit-from-sql}))

(defn insert-nayttoymparisto! [m]
  (insert-one!
    :nayttoymparistot
    (h/to-sql m)))

(defn insert-nayttoymparistot! [c]
  (insert-multi!
    :nayttoymparistot
    (map h/to-sql c)))

(defn select-nayttoymparisto-by-id [id]
  (first
    (query
      [queries/select-nayttoymparistot-by-id id]
      {:row-fn h/nayttoymparisto-from-sql})))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id [id]
  (->
    (query [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-id
            id])
    first
    h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql}))

(defn insert-aiemmin-hankittu-paikallinen-tutkinnon-osa! [m]
  (insert-one!
    :aiemmin_hankitut_paikalliset_tutkinnon_osat
    (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql m)))

(defn select-osaamisen-osoittaminen-by-oopto-id [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-oopto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-ahpto-osaamisen-osoittaminen! [oopto-id naytto-id]
  (insert-one!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    {:aiemmin_hankittu_paikallinen_tutkinnon_osa_id oopto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-ooyto-arvioija! [yto-id a-id]
  (insert-one!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat
    {:aiemmin_hankittu_yhteinen_tutkinnon_osa_id yto-id
     :koulutuksen_jarjestaja_osaamisen_arvioija_id a-id}))

(defn select-arvioija-by-ooyto-id [id]
  (query
    [queries/select-arvioijat-by-ooyto-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ahyto-id
  "Aiemmin hankitun yhteisen tutkinnon osan näytön tarkentavat tiedot
   (hankitun osaamisen näytöt)"
  [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ooyto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-tarkentavat-tiedot-naytto-by-ahyto-osa-alue-id [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-ahyto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn insert-ooyto-osa-alue-osaamisen-osoittaminen! [osa-alue-id naytto-id]
  (insert-one!
    :aiemmin_hankitun_yto_osa_alueen_naytto
    {:aiemmin_hankittu_yto_osa_alue_id osa-alue-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osa-alueet-by-ahyto-id [id]
  (query
    [queries/select-osa-alueet-by-ooyto-id id]
    {:row-fn h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn insert-aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue! [m]
  (insert-one!
    :aiemmin_hankitut_yto_osa_alueet
    (h/aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql m)))

(defn insert-aiemmin-hankittu-yhteinen-tutkinnon-osa! [m]
  (insert-one!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id [id]
  (->
    (query [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-id id])
    first
    h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql))

(defn select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql}))

(defn insert-hankittava-ammat-tutkinnon-osa! [m]
  (insert-one!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)))

(defn select-hankittava-ammat-tutkinnon-osa-by-id [id]
  (->
    (query
      [queries/select-hankittavat-ammat-tutkinnon-osat-by-id id])
    first
    h/hankittava-ammat-tutkinnon-osa-from-sql))

(defn select-hankittavat-ammat-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-hankittavat-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-ammat-tutkinnon-osa-from-sql}))

(defn insert-hato-osaamisen-osoittaminen! [hato-id naytto-id]
  (insert-one!
    :hankittavan_ammat_tutkinnon_osan_naytto
    {:hankittava_ammat_tutkinnon_osa_id hato-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osaamisen-osoittamiset-by-hato-id [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-pato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn delete-osaamisen-hankkimistavat-by-hato-id!
  "Hankittavan ammatillisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (shallow-delete!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_ammat_tutkinnon_osa_id = ?" id]))

(defn delete-osaamisen-osoittamiset-by-pato-id!
  "Hankittavan ammatillisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (shallow-delete!
    :hankittavan_ammat_tutkinnon_osan_naytto
    ["hankittava_ammat_tutkinnon_osa_id = ?" id]))

(defn update-hankittava-ammat-tutkinnon-osa-by-id! [id m]
  (update!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-hankittava-yhteinen-tutkinnon-osa-by-id! [hyto-id new-values]
  (update!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" hyto-id]))

(defn delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id
  [hoks-id db-conn]
  (shallow-delete!
    :hankittavat_ammat_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-hyto-osa-alueet! [hyto-id]
  (shallow-delete!
    :yhteisen_tutkinnon_osan_osa_alueet
    ["yhteinen_tutkinnon_osa_id = ?" hyto-id]))

(defn update-aiemmin-hankittu-ammat-tutkinnon-osa-by-id! [id new-values]
  (update!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    (h/aiemmin-hankittu-ammat-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-aiemmin-hankittu-paikallinen-tutkinnon-osa-by-id! [id new-values]
  (when-let
   [new-ahpt (h/aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql new-values)]
    (update!
      :aiemmin_hankitut_paikalliset_tutkinnon_osat
      new-ahpt
      ["id = ? AND deleted_at IS NULL" id])))

(defn update-aiemmin-hankittu-yhteinen-tutkinnon-osa-by-id! [id new-values]
  (update!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    (h/aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn update-todennettu-arviointi-lisatiedot-by-id! [id new-values]
  (update!
    :todennettu_arviointi_lisatiedot
    (h/todennettu-arviointi-lisatiedot-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" id]))

(defn delete-todennettu-arviointi-arvioijat-by-tta-id! [id]
  (shallow-delete!
    :todennettu_arviointi_arvioijat
    ["todennettu_arviointi_lisatiedot_id = ?" id]))

(defn delete-aiemmin-hankitun-ammat-tutkinnon-osan-naytto-by-id! [id]
  (shallow-delete!
    :aiemmin_hankitun_ammat_tutkinnon_osan_naytto
    ["aiemmin_hankittu_ammat_tutkinnon_osa_id = ?" id]))

(defn delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
  [hoks-id db-conn]
  (shallow-delete!
    :aiemmin_hankitut_ammat_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-aiemmin-hankitun-paikallisen-tutkinnon-osan-naytto-by-id! [id]
  (shallow-delete!
    :aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto
    ["aiemmin_hankittu_paikallinen_tutkinnon_osa_id = ?" id]))

(defn delete-aiemmin-hankitun-yhteisen-tutkinnon-osan-naytto-by-id! [id]
  (shallow-delete!
    :aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto
    ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ?" id]))

(defn delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
  [hoks-id db-conn]
  (shallow-delete!
    :aiemmin_hankitut_paikalliset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id [hoks-id]
  (shallow-delete!
    :aiemmin_hankitut_yhteiset_tutkinnon_osat
    ["hoks_id = ?" hoks-id]))

(defn delete-aiemmin-hankitut-yto-osa-alueet-by-id! [id]
  (shallow-delete!
    :aiemmin_hankitut_yto_osa_alueet
    ["aiemmin_hankittu_yhteinen_tutkinnon_osa_id = ?" id]))

(defn insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
  [pato-id oh-id]
  (insert-one!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_ammat_tutkinnon_osa_id pato-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn select-osaamisen-hankkimistavat-by-hato-id
  "hankittavan ammat tutkinnon osan osaamisen hankkimistavat"
  [id]
  (query
    [queries/select-osaamisen-hankkmistavat-by-pato-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-opiskeluvalmiuksia-tukeva-opinto! [new-value]
  (insert-one!
    :opiskeluvalmiuksia_tukevat_opinnot
    (h/to-sql new-value)))

(defn insert-opiskeluvalmiuksia-tukevat-opinnot! [c]
  (insert-multi!
    :opiskeluvalmiuksia_tukevat_opinnot
    (mapv h/to-sql c)))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-id [oto-id]
  (->
    (query [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-id oto-id])
    first
    h/opiskeluvalmiuksia-tukevat-opinnot-from-sql))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id [id]
  (query
    [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id id]
    {:row-fn h/opiskeluvalmiuksia-tukevat-opinnot-from-sql}))

(defn delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id [hoks-id db-conn]
  (shallow-delete!
    :opiskeluvalmiuksia_tukevat_opinnot
    ["hoks_id = ?" hoks-id] db-conn))

(defn update-opiskeluvalmiuksia-tukevat-opinnot-by-id! [oto-id new-values]
  (update!
    :opiskeluvalmiuksia_tukevat_opinnot
    (h/to-sql new-values)
    ["id = ? AND deleted_at IS NULL" oto-id]))

(defn insert-hankittava-yhteinen-tutkinnon-osa! [m]
  (insert-one!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql m)))

(defn select-hankittava-yhteinen-tutkinnon-osa-by-id [hyto-id]
  (->
    (query [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-id hyto-id])
    first
    h/hankittava-yhteinen-tutkinnon-osa-from-sql))

(defn select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (query
    [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-yhteinen-tutkinnon-osa-from-sql}))

(defn select-osaamisen-hankkimistavat-by-hyto-osa-alue-id [id]
  (query
    [queries/select-osaamisen-hankkimistavat-by-yto-osa-alue-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn insert-hyto-osa-alueen-osaamisen-hankkimistapa! [hyto-osa-alue-id oh-id]
  (insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    {:yhteisen_tutkinnon_osan_osa_alue_id hyto-osa-alue-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn insert-yhteisen-tutkinnon-osan-osa-alue! [osa-alue]
  (insert-one!
    :yhteisen_tutkinnon_osan_osa_alueet
    (h/yhteisen-tutkinnon-osan-osa-alue-to-sql osa-alue)))

(defn delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id [hoks-id db-conn]
  (shallow-delete!
    :hankittavat_yhteiset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn select-yto-osa-alueet-by-yto-id [id]
  (query
    [queries/select-yto-osa-alueet-by-yto-id id]
    {:row-fn h/yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn insert-yto-osa-alueen-osaamisen-osoittaminen! [yto-id naytto-id]
  (insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_naytot
    {:yhteisen_tutkinnon_osan_osa_alue_id yto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn select-osaamisen-osoittamiset-by-yto-osa-alue-id [id]
  (query
    [queries/select-osaamisen-osoittamiset-by-yto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-oppilaitos-oids []
  (query
    [queries/select-oppilaitos-oids]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-oppilaitos-oids-by-koulutustoimija-oid [oid]
  (query
    [queries/select-oppilaitos-oids-by-koulutustoimija-oid oid]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-sessions-by-session-key [session-key]
  (first (query [queries/select-sessions-by-session-key session-key])))

(defn generate-session-key [conn]
  (loop [session-key nil]
    (if (or (nil? session-key)
            (seq (jdbc/query
                   conn
                   [queries/select-sessions-by-session-key session-key])))
      (recur (str (java.util.UUID/randomUUID)))
      session-key)))

(defn insert-or-update-session! [session-key data]
  (jdbc/with-db-transaction
    [conn (get-db-connection)]
    (let [k (or session-key (generate-session-key conn))
          db-sessions (jdbc/query
                        conn
                        [queries/select-sessions-by-session-key k])]
      (if (empty? db-sessions)
        (jdbc/insert!
          conn
          :sessions
          {:session_key k :data data})
        (jdbc/update!
          conn
          :sessions
          {:data data
           :updated_at (java.util.Date.)}
          ["session_key = ?" k]))
      k)))

(defn delete-session! [session-key]
  (delete! :sessions ["session_key = ?" session-key]))

(defn delete-sessions-by-ticket! [ticket]
  (delete! :sessions ["data->>'ticket' = ?" ticket]))
