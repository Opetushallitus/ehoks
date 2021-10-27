(ns oph.ehoks.db.postgresql.common
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]))

(defn insert-koodisto-koodi!
  ([m]
    (db-ops/insert-one!
      :koodisto_koodit
      (db-ops/to-sql m)))
  ([m conn]
    (db-ops/insert-one!
      :koodisto_koodit
      (db-ops/to-sql m)
      conn)))

(defn insert-osaamisen-osoittamisen-osa-alue!
  ([naytto-id koodi-id]
    (db-ops/insert-one!
      :osaamisen_osoittamisen_osa_alueet
      {:osaamisen_osoittaminen_id naytto-id
       :koodisto_koodi_id koodi-id}))
  ([naytto-id koodi-id conn]
    (db-ops/insert-one!
      :osaamisen_osoittamisen_osa_alueet
      {:osaamisen_osoittaminen_id naytto-id
       :koodisto_koodi_id koodi-id}
      conn)))

(defn select-osa-alueet-by-osaamisen-osoittaminen [naytto-id]
  (db-ops/query
    [queries/select-osa-alueet-by-osaamisen-osoittaminen naytto-id]
    {:row-fn h/koodi-uri-from-sql}))

(defn insert-osaamisen-osoittaminen!
  ([m]
    (db-ops/insert-one!
      :osaamisen_osoittamiset
      (h/osaamisen-osoittaminen-to-sql m)))
  ([m conn]
    (db-ops/insert-one!
      :osaamisen_osoittamiset
      (h/osaamisen-osoittaminen-to-sql m)
      conn)))

(defn insert-oo-koulutuksen-jarjestaja-osaamisen-arvioija!
  ([hon c]
    (insert-oo-koulutuksen-jarjestaja-osaamisen-arvioija!
      hon c (db-ops/get-db-connection)))
  ([hon c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [kja-col (db-ops/insert-multi!
                      :koulutuksen_jarjestaja_osaamisen_arvioijat
                      (map h/koulutuksen-jarjestaja-osaamisen-arvioija-to-sql c)
                      conn)]
        (db-ops/insert-multi!
          :osaamisen_osoittamisen_koulutuksen_jarjestaja_arvioija
          (map #(hash-map
                  :osaamisen_osoittaminen_id (:id hon)
                  :koulutuksen_jarjestaja_osaamisen_arvioija_id (:id %))
               kja-col)
          conn)
        kja-col))))

(defn select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id
  "Hankitun osaamisen näytön koulutuksen järjestäjän arvioijat"
  [id]
  (db-ops/query
    [queries/select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn insert-tyoelama-arvioija!
  ([arvioija]
    (db-ops/insert-one!
      :tyoelama_osaamisen_arvioijat
      (h/tyoelama-arvioija-to-sql arvioija)))
  ([arvioija conn]
    (db-ops/insert-one!
      :tyoelama_osaamisen_arvioijat
      (h/tyoelama-arvioija-to-sql arvioija)
      conn)))

(defn insert-osaamisen-osoittamisen-tyoelama-arvioija!
  ([hon arvioija]
    (db-ops/insert-one!
      :osaamisen_osoittamisen_tyoelama_arvioija
      {:osaamisen_osoittaminen_id (:id hon)
       :tyoelama_arvioija_id (:id arvioija)}))
  ([hon arvioija conn]
    (db-ops/insert-one!
      :osaamisen_osoittamisen_tyoelama_arvioija
      {:osaamisen_osoittaminen_id (:id hon)
       :tyoelama_arvioija_id (:id arvioija)}
      conn)))

(defn select-tyoelama-osaamisen-arvioijat-by-hon-id
  "Hankitun osaamisen näytön työelemän arvioijat"
  [id]
  (db-ops/query
    [queries/select-tyoelama-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/tyoelama-arvioija-from-sql}))

(defn insert-osaamisen-osoittamisen-sisallot!
  ([hon c]
    (db-ops/insert-multi!
      :osaamisen_osoittamisen_sisallot
      (map #(hash-map :osaamisen_osoittaminen_id (:id hon) :sisallon_kuvaus %)
           c)))
  ([hon c conn]
    (db-ops/insert-multi!
      :osaamisen_osoittamisen_sisallot
      (map #(hash-map :osaamisen_osoittaminen_id (:id hon) :sisallon_kuvaus %)
           c)
      conn)))

(defn select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/sisallon-kuvaus-from-sql}))

(defn insert-osaamisen-osoittamisen-yksilolliset-kriteerit!
  ([hon c]
    (db-ops/insert-multi!
      :osaamisen_osoittamisen_yksilolliset_kriteerit
      (map #(hash-map :osaamisen_osoittaminen_id (:id hon)
                      :yksilollinen_kriteeri %) c)))
  ([hon c conn]
    (db-ops/insert-multi!
      :osaamisen_osoittamisen_yksilolliset_kriteerit
      (map #(hash-map :osaamisen_osoittaminen_id (:id hon)
                      :yksilollinen_kriteeri %) c)
      conn)))

(defn select-osaamisen-osoittamisen-kriteerit-by-osaamisen-osoittaminen-id [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamisen-kriteeri-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/yksilolliset-kriteerit-from-sql}))

(defn insert-nayttoymparisto!
  ([m]
    (db-ops/insert-one!
      :nayttoymparistot
      (db-ops/to-sql m)))
  ([m conn]
    (db-ops/insert-one!
      :nayttoymparistot
      (db-ops/to-sql m) conn)))

(defn select-nayttoymparisto-by-id [id]
  (first
    (db-ops/query
      [queries/select-nayttoymparistot-by-id id]
      {:row-fn h/nayttoymparisto-from-sql})))

(defn select-oppilaitos-oids []
  (db-ops/query
    [queries/select-oppilaitos-oids]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-oppilaitos-oids-by-koulutustoimija-oid [oid]
  (db-ops/query
    [queries/select-oppilaitos-oids-by-koulutustoimija-oid oid]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-kyselylinkit-by-tunnus [tunnus]
  (db-ops/query
    [queries/select-kyselylinkit-by-linkki (str "%" tunnus)]
    {:row-fn db-ops/from-sql}))

(defn delete-kyselylinkki-by-tunnus [tunnus]
  (db-ops/query
    [queries/delete-kyselylinkki-by-linkki (str "%" tunnus)]))
