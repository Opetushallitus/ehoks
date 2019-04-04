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
  (if (get-in h sks)
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
    h))

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

(defn hoks-from-sql [h]
  (from-sql
    h
    {:replaces {:laatija_nimi [:laatija :nimi]
                :hyvaksyja_nimi [:hyvaksyja :nimi]
                :paivittaja_nimi [:paivittaja :nimi]}}))

(defn hoks-to-sql [h]
  (to-sql
    h
    {:removals [:olemassa-olevat-ammatilliset-tutkinnon-osat
                :olemassa-olevat-paikalliset-tutkinnon-osat
                :olemassa-olevat-yhteiset-tutkinnon-osat
                :puuttuvat-ammatilliset-tutkinnon-osat
                :puuttuvat-yhteiset-tutkinnon-osat
                :opiskeluvalmiuksia-tukevat-opinnot
                :puuttuvat-paikalliset-tutkinnon-osat]
     :replaces {[:laatija :nimi] :laatija-nimi
                [:hyvaksyja :nimi] :hyvaksyja-nimi
                [:paivittaja :nimi] :paivittaja-nimi}}))

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]}))

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql [m]
  (to-sql
    m
    {:removals [:tarkentavat-tiedot-naytto
                :tarkentavat-tiedot-arvioija]}))

(defn puuttuva-paikallinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]}))

(defn puuttuva-paikallinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:hankitun-osaamisen-naytto :osaamisen-hankkimistavat]}))

(defn tyopaikalla-hankittava-osaaminen-from-sql [m]
  (from-sql
    m
    {:replaces
     {:vastuullinen_ohjaaja_nimi [:vastuullinen-ohjaaja :nimi]
      :vastuullinen_ohjaaja_sahkoposti [:vastuullinen-ohjaaja :sahkoposti]}}))

(defn tyopaikalla-hankittava-osaaminen-to-sql [m]
  (to-sql
    m
    {:removals [:muut-osallistujat :keskeiset-tyotehtavat]
     :replaces
     {[:vastuullinen-ohjaaja :nimi] :vastuullinen-ohjaaja-nimi
      [:vastuullinen-ohjaaja :sahkoposti] :vastuullinen-ohjaaja-sahkoposti}}))

(defn henkilo-from-sql [m]
  (from-sql
    m
    {:removals [:id :tyopaikalla_hankittava_osaaminen_id]
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
                :tyopaikalla-hankittava-osaaminen]
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

(defn hankitun-osaamisen-naytto-from-sql [m]
  (from-sql
    m
    {:replaces {:jarjestaja_oppilaitos_oid [:jarjestaja :oppilaitos-oid]}}))

(defn hankitun-osaamisen-naytto-to-sql [m]
  (to-sql
    m
    {:removals [:nayttoymparisto
                :keskeiset-tyotehtavat-naytto
                :koulutuksen-jarjestaja-arvioijat
                :tyoelama-arvioijat
                :osaamistavoitteet]
     :replaces {[:jarjestaja :oppilaitos-oid] :jarjestaja-oppilaitos-oid}}))

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

(defn olemassa-oleva-paikallinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]
               :replaces
               {:lahetetty_arvioitavaksi
                [:tarkentavat_tiedot_arvioija :lahetetty-arvioitavaksi]}}))

(defn olemassa-oleva-paikallinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:tarkentavat-tiedot-naytto
                        :tarkentavat-tiedot-arvioija]
             :replaces {[:tarkentavat-tiedot-arvioija :lahetetty-arvioitavaksi]
                        :lahetetty-arvioitavaksi}}))

(defn olemassa-olevan-yhteisen-tutkinnon-osan-osa-alue-from-sql [m]
  (from-sql m {:removals [:olemassa_oleva_yhteinen_tutkinnon_osa_id]}))

(defn olemassa-olevan-yhteisen-tutkinnon-osan-osa-alue-to-sql [m]
  (to-sql m {:removals [:tarkentavat-tiedot]}))

(defn olemassa-oleva-yhteinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:osa-alueet
                        :tarkentavat-tiedot-naytto
                        :tarkentavat-tiedot-arvioija]
             :replaces {[:tarkentavat-tiedot-arvioija :lahetetty-arvioitavaksi]
                        :lahetetty-arvioitavaksi}}))

(defn olemassa-oleva-yhteinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]
               :replaces
               {:lahetetty_arvioitavaksi
                [:tarkentavat_tiedot_arvioija :lahetetty-arvioitavaksi]}}))

(defn todennettu-arviointi-lisatiedot-to-sql [m]
  (to-sql m {:removals [:aiemmin-hankitun-osaamisen-arvioijat]}))

(defn todennettu-arviointi-lisatiedot-from-sql [m]
  (from-sql m))

(defn puuttuva-ammatillinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:hankitun-osaamisen-naytto
                        :osaamisen-hankkimistavat]}))

(defn puuttuva-ammatillinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]}))

(defn opiskeluvalmiuksia-tukevat-opinnot-from-sql [m]
  (from-sql m {:removals [:id :hoks_id]}))

(defn puuttuva-yhteinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id :osa-alueet]}))

(defn puuttuva-yhteinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:osa-alueet]}))

(defn yhteisen-tutkinnon-osan-osa-alue-to-sql [m]
  (to-sql m {:removals [:osaamisen-hankkimistavat
                        :hankitun-osaamisen-naytto]}))

(defn yhteisen-tutkinnon-osan-osa-alue-from-sql [m]
  (from-sql m))

(defn osaamistavoite-from-sql [m] (get m :osaamistavoite))
