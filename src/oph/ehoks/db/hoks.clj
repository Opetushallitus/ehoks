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

(defn- remove-nils [m]
  (apply dissoc m (filter #(nil? (get m %)) (keys m))))

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

(defn hoks-from-sql [h]
  (-> h
      (replace-in :laatija_nimi [:laatija :nimi])
      (replace-in :hyvaksyja_nimi [:hyvaksyja :nimi])
      (replace-in :paivittaja_nimi [:paivittaja :nimi])
      remove-nils
      to-dash-keys))

(defn hoks-to-sql [h]
  (-> h
      (dissoc :olemassa-olevat-ammatilliset-tutkinnon-osat
              :olemassa-olevat-paikalliset-tutkinnon-osat
              :olemassa-olevat-yhteiset-tutkinnon-osat
              :puuttuvat-ammatilliset-tutkinnon-osat
              :puuttuvat-yhteiset-tutkinnon-osat
              :opiskeluvalmiuksia-tukevat-opinnot
              :puuttuvat-paikalliset-tutkinnon-osat)
      (update :eid #(if (nil? %) (str (java.util.UUID/randomUUID)) %)) ; generate and check, move to insert and lock
      (replace-from [:laatija :nimi] :laatija-nimi)
      (replace-from [:hyvaksyja :nimi] :hyvaksyja-nimi)
      (replace-from [:paivittaja :nimi] :paivittaja-nimi)
      to-underscore-keys))

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql [m]
  (to-dash-keys m))

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql [m]
  (-> m
      (dissoc :tarkentavat-tiedot-naytto :tarkentavat-tiedot-arvioija)
      remove-nils
      to-underscore-keys))

(defn puuttuva-paikallinen-tutkinnon-osa-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version :hoks_id)
      remove-nils
      to-dash-keys))

(defn puuttuva-paikallinen-tutkinnon-osa-to-sql [m]
  (-> m
      (dissoc :hankitun-osaamisen-naytto :osaamisen-hankkimistavat)
      remove-nils
      to-underscore-keys))

(defn tyopaikalla-hankittava-osaaminen-from-sql [m]
  (-> m
      remove-db-columns
      (replace-in :vastuullinen_ohjaaja_nimi [:vastuullinen-ohjaaja :nimi])
      (replace-in :vastuullinen_ohjaaja_sahkoposti
                  [:vastuullinen-ohjaaja :sahkoposti])
      to-dash-keys))

(defn tyopaikalla-hankittava-osaaminen-to-sql [m]
  (-> m
      (replace-from [:vastuullinen-ohjaaja :nimi] :vastuullinen-ohjaaja-nimi)
      (replace-from [:vastuullinen-ohjaaja :sahkoposti]
                    :vastuullinen-ohjaaja-sahkoposti)
      (dissoc :muut-osallistujat :keskeiset-tyotehtavat)
      to-underscore-keys))

(defn henkilo-from-sql [m]
  (-> m
      (remove-db-columns :id :tyopaikalla_hankittava_osaaminen_id)
      (replace-in :organisaatio_nimi [:organisaatio :nimi])
      (replace-in :organisaatio_y_tunnus [:organisaatio :y-tunnus])
      to-dash-keys))

(defn henkilo-to-sql [m]
  (-> m
      (replace-from [:organisaatio :nimi] :organisaatio_nimi)
      (replace-from [:organisaatio :y-tunnus] :organisaatio_y_tunnus)
      to-underscore-keys))

(defn osaamisen-hankkimistapa-from-sql [m]
  (-> m
      (remove-db-columns)
      (replace-in :jarjestajan_edustaja_nimi [:jarjestajan-edustaja :nimi])
      (replace-in :jarjestajan_edustaja_rooli [:jarjestajan-edustaja :rooli])
      (replace-in :jarjestajan_edustaja_oppilaitos_oid
                  [:jarjestajan-edustaja :oppilaitos-oid])
      (replace-in :hankkijan_edustaja_nimi[:hankkijan-edustaja :nimi])
      (replace-in :hankkijan_edustaja_rooli [:hankkijan-edustaja :rooli])
      (replace-in :hankkijan_edustaja_oppilaitos_oid
                  [:hankkijan-edustaja :oppilaitos-oid])
      to-dash-keys))

(defn osaamisen-hankkimistavat-to-sql [m]
  (-> m
      (dissoc :muut-oppimisymparisto)
      (replace-from [:jarjestajan-edustaja :nimi] :jarjestajan-edustaja-nimi)
      (replace-from [:jarjestajan-edustaja :rooli] :jarjestajan-edustaja-rooli)
      (replace-from [:jarjestajan-edustaja :oppilaitos-oid]
                    :jarjestajan-edustaja-oppilaitos-oid)
      (replace-from [:hankkijan-edustaja :nimi] :hankkijan-edustaja-nimi)
      (replace-from [:hankkijan-edustaja :rooli] :hankkijan-edustaja-rooli)
      (replace-from [:hankkijan-edustaja :oppilaitos-oid]
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
      (replace-in :jarjestaja_oppilaitos_oid [:jarjestaja :oppilaitos-oid])
      to-dash-keys))

(defn hankitun-osaamisen-naytto-to-sql [m]
  (to-underscore-keys m))

(defn koulutuksen-jarjestaja-arvioija-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version :id)
      (replace-in :oppilaitos_oid [:organisaatio :oppilaitos-oid])
      to-dash-keys))

(defn koulutuksen-jarjestaja-arvioija-to-sql [m]
  (-> m
      (replace-from [:organisaatio :oppilaitos-oid] :oppilaitos-oid)
      to-underscore-keys))

(defn tyoelama-arvioija-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version :id)
      (replace-in :organisaatio_nimi [:organisaatio :nimi])
      (replace-in :organisaatio_y_tunnus [:organisaatio :y-tunnus])
      to-dash-keys))

(defn tyoelama-arvioija-to-sql [m]
  (-> m
      (replace-from [:organisaatio :nimi] :organisaatio-nimi)
      (replace-from [:organisaatio :y-tunnus] :organisaatio-y-tunnus)
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
      (replace-in
        :lahetetty_arvioitavaksi
        [:todennettu_arviointi_lisatiedot :lahetetty_arvioitavaksi])
      remove-nils
      to-underscore-keys))
