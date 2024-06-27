(ns oph.ehoks.db.postgresql.common
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [clojure.core.memoize :as memo]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]))

(defn insert-koodisto-koodi!
  "Lisää koodistokoodin tietokantaan."
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
  "Lisää osaamisen osoittamisen osa-alueen tietokantaan."
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

(defn select-osa-alueet-by-osaamisen-osoittaminen
  "Hakee osa-alueet tietokannasta osaamisen osoittamisen ID:n perusteella."
  [naytto-id]
  (db-ops/query
    [queries/select-osa-alueet-by-osaamisen-osoittaminen naytto-id]
    {:row-fn h/koodi-uri-from-sql}))

(defn insert-osaamisen-osoittaminen!
  "Lisää yhden osaamisen osoittamisen tietokantaan."
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
  "Lisää yhden osaamisem osoittamisen koulutuksen järjestäjän osaamisen
  arvioijan tietokantaan."
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
  "Hakee hankitun osaamisen näytön koulutuksen järjestäjän arvioijat
  tietokannasta."
  [id]
  (db-ops/query
    [queries/select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/koulutuksen-jarjestaja-osaamisen-arvioija-from-sql}))

(defn insert-tyoelama-arvioija!
  "Lisää yhden työelämän arvioijan tietokantaan."
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
  "Lisää yhden osaamisen osoittamisen työelämän arvioijan tietokantaan."
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
  "Hakee hankitun osaamisen näytön työelemän arvioijat tietokannasta."
  [id]
  (db-ops/query
    [queries/select-tyoelama-osaamisen-arvioijat-by-hon-id id]
    {:row-fn h/tyoelama-arvioija-from-sql}))

(defn insert-osaamisen-osoittamisen-sisallot!
  "Lisää osaamisen osoittamisen sisällöt tietokantaan."
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

(defn select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
  "Hakee osaamisen osoittamisen sisällöt tietokannasta osaamisen osoittamisen
  ID:n perusteella."
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/sisallon-kuvaus-from-sql}))

(defn insert-osaamisen-osoittamisen-yksilolliset-kriteerit!
  "Lisää osaamisen osoittamisen yksilölliset kriteerit tietokantaan."
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

(defn select-osaamisen-osoittamisen-kriteerit-by-osaamisen-osoittaminen-id
  "Hakee osaamisen osoittamisen yksilölliset kriteerit tietokannasta osaamisen
  osoittamisen ID:n perusteella."
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamisen-kriteeri-by-osaamisen-osoittaminen-id
     id]
    {:row-fn h/yksilolliset-kriteerit-from-sql}))

(defn insert-nayttoymparisto!
  "Lisää yhden näyttöympäristön tietokantaan."
  ([m]
    (db-ops/insert-one!
      :nayttoymparistot
      (db-ops/to-sql m)))
  ([m conn]
    (db-ops/insert-one!
      :nayttoymparistot
      (db-ops/to-sql m) conn)))

(defn select-nayttoymparisto-by-id
  "Hakee näyttöympäristön tietokannasta ID:n perusteella."
  [id]
  (first
    (db-ops/query
      [queries/select-nayttoymparistot-by-id id]
      {:row-fn h/nayttoymparisto-from-sql})))

(defn select-oppilaitos-oids
  "Hakee oppilaitos OID:t tietokannasta."
  []
  (db-ops/query
    [queries/select-oppilaitos-oids]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-oppilaitos-oids-by-koulutustoimija-oid
  "Hakee oppilaitos OID:t tietokannasta koulutustoimijan OID:n perusteella."
  [oid]
  (db-ops/query
    [queries/select-oppilaitos-oids-by-koulutustoimija-oid oid]
    {:row-fn h/oppilaitos-oid-from-sql}))

(defn select-kyselylinkki
  "Hakee kyselylinkin tietokannasta linkin perusteella."
  [linkki]
  (db-ops/query
    [queries/select-kyselylinkki linkki]
    {:row-fn db-ops/from-sql}))

(defn select-kyselylinkit-by-tunnus
  "Hakee kyselylinkit tietokannasta tunnuksen perusteella."
  [tunnus]
  (db-ops/query
    [queries/select-kyselylinkit-by-fuzzy-linkki (str "%/" tunnus)]
    {:row-fn db-ops/from-sql}))

(defn delete-kyselylinkki-by-tunnus
  "Poistaa kyselylinkin tietokannasta tunnuksen perusteella."
  [tunnus]
  (db-ops/delete! :kyselylinkit ["kyselylinkki LIKE ?" (str "%/" tunnus)]))

(defn select-oht-by-tutkinto-and-oppilaitos-between
  "Hakee osaamisen hankkimistapoja tutkinnon ja koulutuksen järjestäjän
  perusteella tietylle aikavälille."
  [tutkinto oppilaitos start end]
  (db-ops/query
    [queries/select-oht-by-tutkinto-and-oppilaitos-between
     tutkinto
     oppilaitos
     start
     end]
    {:identifiers #(do %)
     :row-fn      db-ops/from-sql}))

(def get-oppilaitos-oids-cached-memoized!
  "Memoized get oppilaitos OIDs"
  (memo/ttl
    select-oht-by-tutkinto-and-oppilaitos-between
    {}
    :ttl/threshold 300000)) ; 5 minutes

(defn select-oht-by-tutkinto-between
  "Hakee osaamisen hankkimistapoja tutkinnon perusteella tietylle aikavälille."
  [tutkinto start end]
  (db-ops/query [queries/select-oht-by-tutkinto-between tutkinto start end]
                {:identifiers #(do %)
                 :row-fn      db-ops/from-sql}))

(def get-oht-by-tutkinto-between-memoized!
  "Memoized get tutkinto between"
  (memo/ttl
    select-oht-by-tutkinto-between
    {}
    :ttl/threshold 300000)) ;5 minutes
