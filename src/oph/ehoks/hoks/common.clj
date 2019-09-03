(ns oph.ehoks.hoks.common
  (:require [oph.ehoks.db.postgresql :as db]))

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

(defn get-tarkentavat-tiedot-osaamisen-arvioija [ttoa-id]
  (let [tta (db/select-todennettu-arviointi-lisatiedot-by-id ttoa-id)]
    (dissoc
      (assoc
        tta
        :aiemmin-hankitun-osaamisen-arvioijat
        (db/select-arvioijat-by-todennettu-arviointi-id ttoa-id))
      :id)))

(defn save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
  [naytto arvioijat]
  (mapv
    #(let [arvioija (db/insert-tyoelama-arvioija! %)]
       (db/insert-osaamisen-osoittamisen-tyoelama-arvioija!
         naytto arvioija)
       arvioija)
    arvioijat))

(defn save-osaamisen-osoittamisen-osa-alueet! [n c]
  (mapv
    #(let [k (db/insert-koodisto-koodi! %)]
       (db/insert-osaamisen-osoittamisen-osa-alue! (:id n) (:id k))
       k)
    c))

(defn save-osaamisen-osoittaminen! [n]
  (let [nayttoymparisto (db/insert-nayttoymparisto! (:nayttoymparisto n))
        naytto (db/insert-osaamisen-osoittaminen!
                 (assoc n :nayttoymparisto-id (:id nayttoymparisto)))]
    (db/insert-osaamisen-osoittamisen-koulutuksen-jarjestaja-osaamisen-arvioija!
      naytto (:koulutuksen-jarjestaja-osaamisen-arvioijat n))
    (save-osaamisen-osoittamisen-tyoelama-osaamisen-arvioijat!
      naytto (:tyoelama-osaamisen-arvioijat n))
    (db/insert-osaamisen-osoittamisen-sisallot!
      naytto (:sisallon-kuvaus n))
    (db/insert-osaamisen-osoittamisen-yksilolliset-kriteerit!
      naytto (:yksilolliset-kriteerit n))
    (save-osaamisen-osoittamisen-osa-alueet!
      naytto (:osa-alueet n))
    naytto))