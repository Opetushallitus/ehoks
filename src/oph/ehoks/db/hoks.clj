(ns oph.ehoks.db.hoks
  (:require [clojure.set :refer [rename-keys]]))

(defn convert-keys [f m]
  (rename-keys
    m
    (reduce
      (fn [c n]
        (assoc c n (f n)))
      {}
      (keys m))))

(defn remove-db-columns [m & others]
  (apply
    dissoc m
    :created_at
    :updated_at
    :deleted_at
    others))

(defn to-underscore-keys [m]
  (convert-keys #(keyword (.replace (name %) \- \_)) m))

(defn to-dash-keys [m]
  (convert-keys #(keyword (.replace (name %) \_ \-)) m))

(defn- replace-in [h sk tks]
  (if (some? (get h sk))
    (dissoc (assoc-in h tks (get h sk)) sk)
    h))

(defn- replace-from [h sks tk]
  (cond
    (get-in h sks)
    (if (= (count (get-in h (drop-last sks))) 1)
      (apply
        dissoc
        (assoc h tk (get-in h sks))
        (drop-last sks))
      (update-in
        (assoc h tk (get-in h sks))
        (drop-last sks)
        dissoc
        (last sks)))
    (empty? (get-in h (drop-last sks)))
    (apply dissoc h (drop-last sks))
    :else h))

(defn replace-with-in [m kss kst]
  (if (coll? kss)
    (replace-from m kss kst)
    (replace-in m kss kst)))

(defn- remove-nils [m]
  (apply dissoc m (filter #(nil? (get m %)) (keys m))))

(defn convert-sql
  [m {removals :removals replaces :replaces
      :or {removals [] replaces {}}, :as operations}]
  (as-> m x
    (reduce
      (fn [c [kss kst]]
        (replace-with-in c kss kst))
      x
      replaces)
    (apply dissoc x removals)))

(defn from-sql
  ([m operations]
    (-> (convert-sql m operations)
        remove-nils
        remove-db-columns
        to-dash-keys))
  ([m] (from-sql m {})))

(defn to-sql
  ([m operations]
    (to-underscore-keys (convert-sql m operations)))
  ([m] (to-sql m {})))

(defn hoks-to-sql [h]
  (to-sql
    h
    {:removals [:aiemmin-hankitut-ammat-tutkinnon-osat
                :aiemmin-hankitut-paikalliset-tutkinnon-osat
                :aiemmin-hankitut-yhteiset-tutkinnon-osat
                :hankittavat-ammat-tutkinnon-osat
                :hankittavat-yhteiset-tutkinnon-osat
                :opiskeluvalmiuksia-tukevat-opinnot
                :hankittavat-paikalliset-tutkinnon-osat]}))

(defn aiemmin-hankittu-ammat-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]}))

(defn aiemmin-hankittu-ammat-tutkinnon-osa-to-sql [m]
  (to-sql
    m
    {:removals [:tarkentavat-tiedot-naytto
                :tarkentavat-tiedot-arvioija]}))

(defn hankittava-paikallinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]}))

(defn hankittava-paikallinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:osaamisen-osoittaminen :osaamisen-hankkimistavat]}))

(defn tyopaikalla-jarjestettava-koulutus-from-sql [m]
  (from-sql
    m
    {:replaces
     {:vastuullinen_ohjaaja_nimi [:vastuullinen-ohjaaja :nimi]
      :vastuullinen_ohjaaja_sahkoposti [:vastuullinen-ohjaaja :sahkoposti]}}))

(defn tyopaikalla-jarjestettava-koulutus-to-sql [m]
  (to-sql
    m
    {:removals [:muut-osallistujat :keskeiset-tyotehtavat]
     :replaces
     {[:vastuullinen-ohjaaja :nimi] :vastuullinen-ohjaaja-nimi
      [:vastuullinen-ohjaaja :sahkoposti] :vastuullinen-ohjaaja-sahkoposti}}))

(defn henkilo-from-sql [m]
  (from-sql
    m
    {:removals [:id :tyopaikalla_jarjestettava_koulutus_id]
     :replaces
     {:organisaatio_nimi [:organisaatio :nimi]
      :organisaatio_y_tunnus [:organisaatio :y-tunnus]}}))

(defn henkilo-to-sql [m]
  (to-sql m {:replaces {[:organisaatio :nimi] :organisaatio_nimi
                        [:organisaatio :y-tunnus] :organisaatio_y_tunnus}}))

(defn osaamisen-hankkimistapa-from-sql [m]
  (from-sql
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
  (to-sql
    m
    {:removals [:muut-oppimisymparisto
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
  (from-sql m {:removals [:id :osaamisen_hankkimistapa_id]}))

(defn osaamisen-osoittaminen-from-sql [m]
  (from-sql
    m
    {:replaces {:jarjestaja_oppilaitos_oid [:jarjestaja :oppilaitos-oid]}}))

(defn osaamisen-osoittaminen-to-sql [m]
  (to-sql
    m
    {:removals [:nayttoymparisto
                :keskeiset-tyotehtavat-naytto
                :koulutuksen-jarjestaja-arvioijat
                :tyoelama-arvioijat
                :osaamistavoitteet
                :osa-alueet]
     :replaces {[:jarjestaja :oppilaitos-oid] :jarjestaja-oppilaitos-oid}}))

(defn koodi-uri-from-sql [m]
  (from-sql m {:removals [:id]}))

(defn koulutuksen-jarjestaja-arvioija-from-sql [m]
  (from-sql m {:replaces {:oppilaitos_oid [:organisaatio :oppilaitos-oid]}
               :removals [:id]}))

(defn koulutuksen-jarjestaja-arvioija-to-sql [m]
  (to-sql m {:replaces {[:organisaatio :oppilaitos-oid] :oppilaitos-oid}}))

(defn tyoelama-arvioija-from-sql [m]
  (from-sql m {:replaces {:organisaatio_nimi [:organisaatio :nimi]
                          :organisaatio_y_tunnus [:organisaatio :y-tunnus]}
               :removals [:id]}))

(defn tyoelama-arvioija-to-sql [m]
  (to-sql m {:replaces {[:organisaatio :nimi] :organisaatio-nimi
                        [:organisaatio :y-tunnus] :organisaatio-y-tunnus}}))

(defn nayttoymparisto-from-sql [m]
  (from-sql m {:removals [:id]}))

(defn tyotehtava-from-sql [m]
  (get m :tyotehtava))

(defn aiemmin-hankittu-paikallinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]
               :replaces
               {:lahetetty_arvioitavaksi
                [:tarkentavat_tiedot_arvioija :lahetetty-arvioitavaksi]}}))

(defn aiemmin-hankittu-paikallinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:tarkentavat-tiedot-naytto
                        :tarkentavat-tiedot-arvioija]
             :replaces {[:tarkentavat-tiedot-arvioija :lahetetty-arvioitavaksi]
                        :lahetetty-arvioitavaksi}}))

(defn aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-from-sql [m]
  (from-sql m {:removals [:aiemmin_hankittu_yhteinen_tutkinnon_osa_id]}))

(defn aiemmin-hankitun-yhteisen-tutkinnon-osan-osa-alue-to-sql [m]
  (to-sql m {:removals [:tarkentavat-tiedot]}))

(defn aiemmin-hankittu-yhteinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:osa-alueet
                        :tarkentavat-tiedot-naytto
                        :tarkentavat-tiedot-arvioija]
             :replaces {[:tarkentavat-tiedot-arvioija :lahetetty-arvioitavaksi]
                        :lahetetty-arvioitavaksi}}))

(defn aiemmin-hankittu-yhteinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]
               :replaces
               {:lahetetty_arvioitavaksi
                [:tarkentavat_tiedot_arvioija :lahetetty-arvioitavaksi]}}))

(defn todennettu-arviointi-lisatiedot-to-sql [m]
  (to-sql m {:removals [:aiemmin-hankitun-osaamisen-arvioijat]}))

(defn todennettu-arviointi-lisatiedot-from-sql [m]
  (from-sql m))

(defn hankittava-ammat-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:osaamisen-osoittaminen
                        :osaamisen-hankkimistavat]}))

(defn hankittava-ammat-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]}))

(defn opiskeluvalmiuksia-tukevat-opinnot-from-sql [m]
  (from-sql m {:removals [:id :hoks_id]}))

(defn hankittava-yhteinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id :osa-alueet]}))

(defn hankittava-yhteinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:osa-alueet]}))

(defn yhteisen-tutkinnon-osan-osa-alue-to-sql [m]
  (to-sql m {:removals [:osaamisen-hankkimistavat
                        :osaamisen-osoittaminen]}))

(defn yhteisen-tutkinnon-osan-osa-alue-from-sql [m]
  (from-sql m))

(defn osaamistavoite-from-sql [m] (get m :osaamistavoite))
