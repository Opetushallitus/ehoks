(ns oph.ehoks.hoks
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]
            [clojure.tools.logging :as log]
            [clojure.walk :as walk]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-oo]
            [oph.ehoks.db.postgresql.aiemmin-hankitut :as db-ah]
            [oph.ehoks.db.postgresql.hankittavat :as db-ha]
            [oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat :as db-ot]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.oppijaindex :as oppijaindex])
  (:import [java.time LocalDate]
           [java.util UUID]))

(defn tuva-related?
  [hoks]
  (or (some? (seq (:hankittavat-koulutuksen-osat hoks)))
      (some? (:tuva-opiskeluoikeus-oid hoks))))

(defn get-values
  "Hakee annetun HOKSin tutkinnon osat ja tukevat opinnot tietokannasta."
  [hoks]
  (let [hoks-id (:id hoks)]
    (assoc
      hoks
      :aiemmin-hankitut-ammat-tutkinnon-osat
      (ah/get-aiemmin-hankitut-ammat-tutkinnon-osat hoks-id)
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-paikalliset-tutkinnon-osat hoks-id)
      :hankittavat-paikalliset-tutkinnon-osat
      (ha/get-hankittavat-paikalliset-tutkinnon-osat hoks-id)
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      (ah/get-aiemmin-hankitut-yhteiset-tutkinnon-osat hoks-id)
      :hankittavat-ammat-tutkinnon-osat
      (ha/get-hankittavat-ammat-tutkinnon-osat hoks-id)
      :opiskeluvalmiuksia-tukevat-opinnot
      (ot/get-opiskeluvalmiuksia-tukevat-opinnot hoks-id)
      :hankittavat-yhteiset-tutkinnon-osat
      (ha/get-hankittavat-yhteiset-tutkinnon-osat hoks-id)
      :hankittavat-koulutuksen-osat
      (ha/get-hankittavat-koulutuksen-osat hoks-id))))

(defn get-by-id
  "Hakee yhden HOKSin ID:n perusteella."
  [id]
  (get-values (db-hoks/select-hoks-by-id id)))

(defn get-by-oppija
  "Hakee HOKSeja oppijan OID:n perusteella."
  [oid]
  (mapv get-values (db-hoks/select-hoks-by-oppija-oid oid)))

(defn- save-parts!
  "Tallentaa HOKSin osat tietokantaan."
  [hoks conn]
  (assoc
    hoks
    :aiemmin-hankitut-ammat-tutkinnon-osat
    (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
      (:id hoks)
      (:aiemmin-hankitut-ammat-tutkinnon-osat hoks)
      conn)
    :aiemmin-hankitut-paikalliset-tutkinnon-osat
    (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      (:id hoks)
      (:aiemmin-hankitut-paikalliset-tutkinnon-osat hoks)
      conn)
    :hankittavat-paikalliset-tutkinnon-osat
    (ha/save-hankittavat-paikalliset-tutkinnon-osat!
      (:id hoks)
      (:hankittavat-paikalliset-tutkinnon-osat hoks)
      conn)
    :aiemmin-hankitut-yhteiset-tutkinnon-osat
    (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
      (:id hoks)
      (:aiemmin-hankitut-yhteiset-tutkinnon-osat hoks)
      conn)
    :hankittavat-ammat-tutkinnon-osat
    (ha/save-hankittavat-ammat-tutkinnon-osat!
      (:id hoks)
      (:hankittavat-ammat-tutkinnon-osat hoks)
      conn)
    :opiskeluvalmiuksia-tukevat-opinnot
    (ot/save-opiskeluvalmiuksia-tukevat-opinnot!
      (:id hoks)
      (:opiskeluvalmiuksia-tukevat-opinnot hoks)
      conn)
    :hankittavat-yhteiset-tutkinnon-osat
    (ha/save-hankittavat-yhteiset-tutkinnon-osat!
      (:id hoks)
      (:hankittavat-yhteiset-tutkinnon-osat hoks)
      conn)
    :hankittavat-koulutuksen-osat
    (ha/save-hankittavat-koulutuksen-osat!
      (:id hoks)
      (:hankittavat-koulutuksen-osat hoks)
      conn)))

(defn save!
  "Tallentaa yhden HOKSin arvot tietokantaan."
  [hoks]
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (let [hoks (merge hoks (db-hoks/insert-hoks! hoks conn))
          tuva-hoks (tuva-related? hoks)]
      (db-hoks/insert-amisherate-kasittelytilat!
        (:id hoks) tuva-hoks conn)
      (save-parts! hoks conn))))

(defn update!
  "Päivittää HOKSin ylätason arvoja."
  [hoks]
  (let [hoks-id (:id hoks)]
    (jdbc/with-db-transaction
      [db-conn (db-ops/get-db-connection)]
      (db-hoks/update-hoks-by-id! hoks-id hoks db-conn))
    (db-hoks/select-hoks-by-id hoks-id)))

(defn- merge-not-given-hoks-values
  "Varmistaa, että tietyt kentät ovat olemassa HOKSissa, vaikka niissä olisi
  nulleja."
  [new-hoks-values]
  (let [empty-top-level-hoks {:versio nil
                              :sahkoposti nil
                              :urasuunnitelma-koodi-uri nil
                              :osaamisen-hankkimisen-tarve nil
                              :hyvaksytty nil
                              :urasuunnitelma-koodi-versio nil
                              :osaamisen-saavuttamisen-pvm nil
                              :paivitetty nil}]
    (merge empty-top-level-hoks new-hoks-values)))

(defn replace-main-hoks!
  "Korvaa HOKSin ydinsisällön annetuilla arvoilla. Ei tallenna tutkinnon osia."
  [hoks-id new-values db-conn]
  (db-hoks/update-hoks-by-id!
    hoks-id (merge-not-given-hoks-values new-values) db-conn))

(defn replace-oto!
  "Korvaa vanhat opiskeluvalmiuksia tukevat opinnot annetuilla arviolla."
  [hoks-id new-oto-values db-conn]
  (db-ot/delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id
    hoks-id db-conn)
  (when
   new-oto-values
    (ot/save-opiskeluvalmiuksia-tukevat-opinnot!
      hoks-id new-oto-values db-conn)))

(defn replace-hato!
  "Korvaa vanhat hankittavat ammatilliset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-hato-values db-conn]
  (db-ha/delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hato-values
    (ha/save-hankittavat-ammat-tutkinnon-osat!
      hoks-id new-hato-values db-conn)))

(defn replace-hpto!
  "Korvaa vanhat hankittavat paikalliset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-hpto-values db-conn]
  (db-ha/delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hpto-values
    (ha/save-hankittavat-paikalliset-tutkinnon-osat!
      hoks-id new-hpto-values db-conn)))

(defn replace-hyto!
  "Korvaa vanhat hankittavat yhteiset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-hyto-values db-conn]
  (db-ha/delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hyto-values
    (ha/save-hankittavat-yhteiset-tutkinnon-osat!
      hoks-id new-hyto-values db-conn)))

(defn replace-hankittavat-koulutuksen-osat!
  "Korvaa vanhat hankittavat koulutuksen osat annetuilla arvoilla."
  [hoks-id new-koulutuksen-osat-values db-conn]
  (db-ha/delete-hankittavat-koulutuksen-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-koulutuksen-osat-values
    (ha/save-hankittavat-koulutuksen-osat!
      hoks-id new-koulutuksen-osat-values db-conn)))

(defn replace-ahato!
  "Korvaa vanhat aiemmin hankitut ammatilliset tutkinnon osat annetuilla
  arvoilla."
  [hoks-id new-ahato-values db-conn]
  (db-ah/delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahato-values
    (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
      hoks-id new-ahato-values db-conn)))

(defn replace-ahpto!
  "Korvaa vanhat aiemmin hankitut paikalliset tutkinnon osat annetuilla
  arvoilla."
  [hoks-id new-ahpto-values db-conn]
  (db-ah/delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahpto-values
    (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id new-ahpto-values db-conn)))

(defn replace-ahyto!
  "Korvaa vanhat aiemmin hankitut yhteiset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-ahyto-values db-conn]
  (db-ah/delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahyto-values
    (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
      hoks-id new-ahyto-values db-conn)))

(defn- replace-parts!
  [hoks conn]
  (replace-oto! (:id hoks)
                (:opiskeluvalmiuksia-tukevat-opinnot hoks)
                conn)
  (replace-hato! (:id hoks)
                 (:hankittavat-ammat-tutkinnon-osat hoks)
                 conn)
  (replace-hpto! (:id hoks)
                 (:hankittavat-paikalliset-tutkinnon-osat hoks)
                 conn)
  (replace-hyto! (:id hoks)
                 (:hankittavat-yhteiset-tutkinnon-osat hoks)
                 conn)
  (replace-hankittavat-koulutuksen-osat! (:id hoks)
                                         (:hankittavat-koulutuksen-osat hoks)
                                         conn)
  (replace-ahato! (:id hoks)
                  (:aiemmin-hankitut-ammat-tutkinnon-osat hoks)
                  conn)
  (replace-ahpto! (:id hoks)
                  (:aiemmin-hankitut-paikalliset-tutkinnon-osat hoks)
                  conn)
  (replace-ahyto! (:id hoks)
                  (:aiemmin-hankitut-yhteiset-tutkinnon-osat hoks)
                  conn))

(defn replace!
  "Korvaa kokonaisen HOKSin (ml. tutkinnon osat) annetuilla arvoilla."
  [hoks]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (replace-main-hoks! (:id hoks) hoks db-conn)
    (replace-parts! hoks db-conn))
  (get-by-id (:id hoks)))

(defn- oppija-oid-changed?
  [new-oppija-oid old-oppija-oid]
  (and (some? new-oppija-oid)
       (not= new-oppija-oid old-oppija-oid)))

(defn handle-oppija-oid-changes-in-indexes!
  [new-hoks old-hoks]
  (let [new-oppija-oid (:oppija-oid new-hoks)
        old-oppija-oid (:oppija-oid old-hoks)]
    (when (oppija-oid-changed? new-oppija-oid old-oppija-oid)
      (oppijaindex/add-oppija! new-oppija-oid)
      (db-oo/update-opiskeluoikeus!
        (:opiskeluoikeus-oid old-hoks) {:oppija-oid new-oppija-oid}))))

(defn check-for-update!
  "Tarkistaa, saako HOKSin päivittää uusilla arvoilla."
  [old-hoks new-hoks opiskeluoikeus]
  (let [new-oppija-oid (:oppija-oid new-hoks)
        old-oppija-oid (:oppija-oid old-hoks)
        new-opiskeluoikeus-oid (:opiskeluoikeus-oid new-hoks)
        old-opiskeluoikeus-oid (:opiskeluoikeus-oid old-hoks)]
    (when-not (opiskeluoikeus/still-active? opiskeluoikeus)
      (throw (ex-info (format "Opiskeluoikeus `%s` is no longer active."
                              new-opiskeluoikeus-oid)
                      {:type               ::disallowed-update
                       :opiskeluoikeus-oid new-opiskeluoikeus-oid})))
    (when (and (some? new-opiskeluoikeus-oid)
               (not= new-opiskeluoikeus-oid old-opiskeluoikeus-oid))
      (throw (ex-info (format
                        (str "Tried to update `opiskeluoikeus-oid` from `%s` "
                             "to `%s` but updating `opiskeluoikeus-oid` in "
                             "HOKS is not allowed!")
                        old-opiskeluoikeus-oid
                        new-opiskeluoikeus-oid)
                      {:type                   ::disallowed-update
                       :old-opiskeluoikeus-oid old-opiskeluoikeus-oid
                       :new-opiskeluoikeus-oid new-opiskeluoikeus-oid})))
    (when (oppija-oid-changed? new-oppija-oid old-oppija-oid)
      (let [master-oid (-> (onr/get-master-of-slave-oppija-oid
                             old-oppija-oid)
                           :body
                           :oidHenkilo)]
        (when (not= master-oid new-oppija-oid)
          (throw (ex-info
                   (format
                     (str "Tried to update `oppija-oid` from `%s` to `%s` but "
                          "updating `oppija-oid` in HOKS is only allowed with "
                          "latest master oppija oid!")
                     old-oppija-oid
                     new-oppija-oid)
                   {:type           ::disallowed-update
                    :old-oppija-oid old-oppija-oid
                    :new-oppija-oid new-oppija-oid})))))))

(defn check
  "Tekee uuden HOKSin tarkistukset ja nostaa poikkeuksen jos HOKS ei läpäise
  jotain tarkistuksista."
  [hoks opiskeluoikeus]
  (let [opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
        oppija-oid         (:oppija-oid hoks)]
    (when (and (nil? opiskeluoikeus) (:enforce-opiskeluoikeus-match? config))
      (-> (format "Opiskeluoikeus `%s` does not match any held by oppija `%s`"
                  opiskeluoikeus-oid
                  oppija-oid)
          (ex-info {:type               ::disallowed-update
                    :opiskeluoikeus-oid opiskeluoikeus-oid
                    :oppija-oid         oppija-oid})
          throw))
    (when-not (opiskeluoikeus/still-active? opiskeluoikeus)
      (throw (ex-info (format "Opiskeluoikeus `%s` is no longer active"
                              opiskeluoikeus-oid)
                      {:type               ::disallowed-update
                       :opiskeluoikeus-oid opiskeluoikeus-oid})))))

(defn get-with-hankittavat-koulutuksen-osat!
  [hoks-id]
  (assoc (db-hoks/select-hoks-by-id hoks-id)
         :hankittavat-koulutuksen-osat
         (ha/get-hankittavat-koulutuksen-osat hoks-id)))

(defn- ensure-yksiloiva-tunniste-in-ohts
  "If the given data structure has an :osaamisen-hankkimistavat key,
  adds an :yksiloiva-tunniste to all its children where it's missing."
  [data]
  (if-not (:osaamisen-hankkimistavat data) data
          (update data :osaamisen-hankkimistavat
                  (partial map (fn [oht]
                                 (update oht :yksiloiva-tunniste
                                         #(or % (str (UUID/randomUUID)))))))))

(defn add-missing-oht-yksiloiva-tunniste
  "Lisää yksilöivät tunnisteet osaamisen hankkimistavoille, jos niitä ei ole.
  Tätä on tarkoitus käyttää vain manuaalisyötetyille uusille HOKSeille;
  muille API-kutsuille on tulossa pakolliseksi, että yksilöivä tunniste
  tulee lähdejärjestelmästä - tai muokkauksen tapauksessa vastaa vanhaa."
  [hoks]
  (walk/prewalk ensure-yksiloiva-tunniste-in-ohts hoks))

(defn redact-field
  "Return obj with the given field redacted (with a bit extra smart on
  the field name)."
  [obj field]
  (if (and (= field :nimi) (:nimi obj))
    (assoc obj :nimi "<REDACTED>")
    (dissoc obj field)))

(defn redact-fields
  "return a function that will redact all the given fields if they exist"
  [& fields]
  (fn [obj] (reduce redact-field obj fields)))

(def vipunen-redaction-based-on-schema-name
  {"HOKSVipunen" (redact-fields :sahkoposti :puhelinnumero),
   "OsaamisenHankkimistapa-get-vipunen"
   (redact-fields :jarjestajan-edustaja :hankkijan-edustaja),
   ;; :vastuullinen-tyopaikka-ohjaaja cannot really be false as it's a
   ;; required field in every schema
   "TyopaikallaJarjestettavaKoulutus-get-vipunen"
   #(update % :vastuullinen-tyopaikka-ohjaaja boolean),
   'VastuullinenTyopaikkaOhjaaja
   (redact-fields :nimi :sahkoposti :puhelinnumero),
   'Oppilaitoshenkilo (redact-fields :nimi),
   'KoulutuksenJarjestajaArvioija (redact-fields :nimi),
   'TyoelamaOsaamisenArvioija (redact-fields :nimi)})

(defn vipunen-redaction-coercion-matcher
  "Based on schema, find a transformation function for HOKS parts that
  implements the correct vipunen coercions."
  [schema]
  (vipunen-redaction-based-on-schema-name (:name (meta schema))))

(defn get-starting-from-id!
  "Hakee tietyn määrän HOKSeja, jotka on päivitetty tietyn ajankohdan jälkeen ja
  joiden ID:t ovat tiettyä arvoa isompia."
  [id amount updated-after]
  (let [hokses (db-hoks/select-hokses-greater-than-id
                 (or id 0)
                 amount
                 updated-after
                 #{:deleted_at})]
    (map get-values hokses)))

(defn update-opiskeluoikeudet
  "Päivittää opiskeluoikeustiedot Koskesta tietokantaan."
  []
  (let [hoksit
        (db-hoks/select-hoksit-by-ensikert-hyvaks-and-saavutettu-tiedot)
        hoksit-created-in-7-days
        (db-hoks/select-hoksit-created-between
          (.minusDays (LocalDate/now) 7)
          (LocalDate/now))]
    (log/infof "Päivitetään %s hoksin opiskeluoikeus-hankintakoulutukset"
               (count hoksit))
    (doseq [hoks hoksit]
      (try
        (let [oppija-oid (:oppija-oid hoks)
              opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
              opiskeluoikeudet (koski/fetch-opiskeluoikeudet-by-oppija-id
                                 oppija-oid)]
          (oppijaindex/add-oppija-hankintakoulutukset opiskeluoikeudet
                                                      opiskeluoikeus-oid
                                                      oppija-oid))
        (catch Exception _
          (log/errorf "Hankintakoulutukset-päivitys epäonnistui hoksille %s"
                      (:id hoks)))))
    (let [hokses-without-oo
          (filter
            some?
            (pmap
              (fn [hoks]
                (when (nil? (koski/get-opiskeluoikeus!
                              (:opiskeluoikeus-oid hoks)))
                  hoks))
              hoksit-created-in-7-days))
          oo-oids (map :opiskeluoikeus-oid hokses-without-oo)]
      (log/infof "Päivitetään opiskeluoikeus-indeksiin koski404 tietoja.
                  Löydettiin %s hoksia ilman opiskeluoikeutta Koskessa."
                 (count oo-oids))
      (doseq [oo-oid oo-oids]
        (let [opiskeluoikeus (db-oo/select-opiskeluoikeus-by-oid oo-oid)]
          (when (some? opiskeluoikeus)
            (oppijaindex/set-opiskeluoikeus-koski404 oo-oid)))))))

(defn mark-as-deleted
  "Jos HOKS on passivoitu, se sisältää poistettu-kentän."
  [hoks]
  (if (nil? (:deleted-at hoks))
    hoks
    (rename-keys hoks {:deleted-at :poistettu})))

(defn get-oppilaitos!
  "Given existing `hoks`, get oppilaitos linked to it. Returns `nil` if
  opiskeluoikeus of `hoks` doesn't have an oppilaitos linked to it."
  [hoks]
  (some-> (:opiskeluoikeus-oid hoks)
          oppijaindex/get-existing-opiskeluoikeus-by-oid! ; throws if not found
          :oppilaitos-oid
          organisaatio/get-organisaatio!))
