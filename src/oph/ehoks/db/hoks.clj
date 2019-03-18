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
    :version
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
  (update
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
                 [:paivittaja :nimi] :paivittaja-nimi}})
    :eid #(if (nil? %) (str (java.util.UUID/randomUUID)) %))) ; generate and check, move to insert and lock

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql [m]
  (to-dash-keys m))

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql [m]
  (to-sql
    m
    {:removals [:tarkentavat-tiedot-naytto :tarkentavat-tiedot-arvioija]}))

(defn puuttuva-paikallinen-tutkinnon-osa-from-sql [m]
  (from-sql m {:removals [:hoks_id]}))

(defn puuttuva-paikallinen-tutkinnon-osa-to-sql [m]
  (to-sql m {:removals [:hankitun-osaamisen-naytto :osaamisen-hankkimistavat]}))

(defn tyopaikalla-hankittava-osaaminen-from-sql [m]
  (-> m
      remove-db-columns
      (replace-with-in :vastuullinen_ohjaaja_nimi [:vastuullinen-ohjaaja :nimi])
      (replace-with-in :vastuullinen_ohjaaja_sahkoposti
                  [:vastuullinen-ohjaaja :sahkoposti])
      to-dash-keys))

(defn tyopaikalla-hankittava-osaaminen-to-sql [m]
  (-> m
      (replace-with-in [:vastuullinen-ohjaaja :nimi] :vastuullinen-ohjaaja-nimi)
      (replace-with-in [:vastuullinen-ohjaaja :sahkoposti]
                    :vastuullinen-ohjaaja-sahkoposti)
      (dissoc :muut-osallistujat :keskeiset-tyotehtavat)
      to-underscore-keys))

(defn henkilo-from-sql [m]
  (-> m
      (remove-db-columns :id :tyopaikalla_hankittava_osaaminen_id)
      (replace-with-in :organisaatio_nimi [:organisaatio :nimi])
      (replace-with-in :organisaatio_y_tunnus [:organisaatio :y-tunnus])
      to-dash-keys))

(defn henkilo-to-sql [m]
  (-> m
      (replace-with-in [:organisaatio :nimi] :organisaatio_nimi)
      (replace-with-in [:organisaatio :y-tunnus] :organisaatio_y_tunnus)
      to-underscore-keys))

(defn osaamisen-hankkimistapa-from-sql [m]
  (-> m
      (remove-db-columns)
      (replace-with-in :jarjestajan_edustaja_nimi [:jarjestajan-edustaja :nimi])
      (replace-with-in :jarjestajan_edustaja_rooli [:jarjestajan-edustaja :rooli])
      (replace-with-in :jarjestajan_edustaja_oppilaitos_oid
                  [:jarjestajan-edustaja :oppilaitos-oid])
      (replace-with-in :hankkijan_edustaja_nimi[:hankkijan-edustaja :nimi])
      (replace-with-in :hankkijan_edustaja_rooli [:hankkijan-edustaja :rooli])
      (replace-with-in :hankkijan_edustaja_oppilaitos_oid
                  [:hankkijan-edustaja :oppilaitos-oid])
      to-dash-keys))

(defn osaamisen-hankkimistavat-to-sql [m]
  (-> m
      (dissoc :muut-oppimisymparisto)
      (replace-with-in [:jarjestajan-edustaja :nimi] :jarjestajan-edustaja-nimi)
      (replace-with-in [:jarjestajan-edustaja :rooli] :jarjestajan-edustaja-rooli)
      (replace-with-in [:jarjestajan-edustaja :oppilaitos-oid]
                    :jarjestajan-edustaja-oppilaitos-oid)
      (replace-with-in [:hankkijan-edustaja :nimi] :hankkijan-edustaja-nimi)
      (replace-with-in [:hankkijan-edustaja :rooli] :hankkijan-edustaja-rooli)
      (replace-with-in [:hankkijan-edustaja :oppilaitos-oid]
                    :hankkijan-edustaja-oppilaitos-oid)
      to-underscore-keys))

(defn muu-oppimisymparisto-from-sql [m]
  (-> m
      (remove-db-columns :id :osaamisen_hankkimistapa_id)
      to-dash-keys))

(def muu-oppimisymparisto-to-sql to-underscore-keys)

(defn hankitun-osaamisen-naytto-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version)
      (replace-with-in :jarjestaja_oppilaitos_oid [:jarjestaja :oppilaitos-oid])
      to-dash-keys))

(defn hankitun-osaamisen-naytto-to-sql [m]
  (to-underscore-keys m))

(defn koulutuksen-jarjestaja-arvioija-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version :id)
      (replace-with-in :oppilaitos_oid [:organisaatio :oppilaitos-oid])
      to-dash-keys))

(defn koulutuksen-jarjestaja-arvioija-to-sql [m]
  (-> m
      (replace-with-in [:organisaatio :oppilaitos-oid] :oppilaitos-oid)
      to-underscore-keys))

(defn tyoelama-arvioija-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version :id)
      (replace-with-in :organisaatio_nimi [:organisaatio :nimi])
      (replace-with-in :organisaatio_y_tunnus [:organisaatio :y-tunnus])
      to-dash-keys))

(defn tyoelama-arvioija-to-sql [m]
  (-> m
      (replace-with-in [:organisaatio :nimi] :organisaatio-nimi)
      (replace-with-in [:organisaatio :y-tunnus] :organisaatio-y-tunnus)
      to-underscore-keys))

(defn nayttoymparisto-to-sql [m]
  (to-underscore-keys m))

(defn nayttoymparisto-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version :id)
      to-dash-keys))

(defn tyotehtava-from-sql [m]
  (get m :tyotehtava))

(defn olemassa-oleva-paikallinen-tutkinnon-osa-from-sql [m]
  (to-dash-keys m))

(defn olemassa-oleva-paikallinen-tutkinnon-osa-to-sql [m]
  (-> m
      remove-nils
      to-underscore-keys))

(defn olemassa-oleva-yhteinen-tutkinnon-osa-from-sql [m]
  (-> m
      (replace-with-in
        :lahetetty_arvioitavaksi
        [:todennettu_arviointi_lisatiedot :lahetetty_arvioitavaksi])
      remove-nils
      to-underscore-keys))
