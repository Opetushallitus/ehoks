(ns oph.ehoks.hoks.common
  (:require [oph.ehoks.db.postgresql.common :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]))

(defn- save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
  ([naytto arvioijat]
    (save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
      naytto arvioijat (db-ops/get-db-connection)))
  ([naytto arvioijat db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [arvioija (db/insert-tyoelama-arvioija! % conn)]
           (db/insert-osaamisen-osoittamisen-tyoelama-arvioija!
             naytto arvioija conn)
           arvioija)
        arvioijat))))

(defn- save-osaamisen-osoittamisen-osa-alueet!
  ([n c]
    (save-osaamisen-osoittamisen-osa-alueet! n c (db-ops/get-db-connection)))
  ([n c db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (mapv
        #(let [k (db/insert-koodisto-koodi! % conn)]
           (db/insert-osaamisen-osoittamisen-osa-alue! (:id n) (:id k) conn)
           k)
        c))))

(defn save-osaamisen-osoittaminen!
  ([n]
    (save-osaamisen-osoittaminen! n (db-ops/get-db-connection)))
  ([n db-conn]
    (jdbc/with-db-transaction
      [conn db-conn]
      (let [nayttoymparisto (db/insert-nayttoymparisto!
                              (:nayttoymparisto n) conn)
            naytto (db/insert-osaamisen-osoittaminen!
                     (assoc n :nayttoymparisto-id (:id nayttoymparisto))
                     conn)]
        (db/insert-oo-koulutuksen-jarjestaja-osaamisen-arvioija!
          naytto (:koulutuksen-jarjestaja-osaamisen-arvioijat n) conn)
        (save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
          naytto (:tyoelama-osaamisen-arvioijat n) conn)
        (db/insert-osaamisen-osoittamisen-sisallot!
          naytto (:sisallon-kuvaus n) conn)
        (db/insert-osaamisen-osoittamisen-yksilolliset-kriteerit!
          naytto (:yksilolliset-kriteerit n) conn)
        (save-osaamisen-osoittamisen-osa-alueet!
          naytto (:osa-alueet n) conn)
        naytto))))

(defn set-osaamisen-osoittaminen-values [naytto]
  (dissoc
    (assoc
      naytto
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      (db/select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id
        (:id naytto))
      :tyoelama-osaamisen-arvioijat
      (db/select-tyoelama-osaamisen-arvioijat-by-hon-id (:id naytto))
      :nayttoymparisto
      (db/select-nayttoymparisto-by-id (:nayttoymparisto-id naytto))
      :sisallon-kuvaus
      (db/select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
        (:id naytto))
      :osa-alueet
      (db/select-osa-alueet-by-osaamisen-osoittaminen (:id naytto))
      :yksilolliset-kriteerit
      (db/select-osaamisen-osoittamisen-kriteerit-by-osaamisen-osoittaminen-id
        (:id naytto)))
    :nayttoymparisto-id))
