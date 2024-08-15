(ns oph.ehoks.hoks
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]
            [clojure.tools.logging :as log]
            [clojure.walk :as walk]
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
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.tyoelama :as tep])
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
  (let [hoks-db       (jdbc/with-db-transaction
                        [conn (db-ops/get-db-connection)]
                        (let [hoks (merge hoks (db-hoks/insert-hoks! hoks conn))
                              tuva-hoks (tuva-related? hoks)]
                          (db-hoks/insert-amisherate-kasittelytilat!
                            (:id hoks) tuva-hoks conn)
                          (save-parts! hoks conn)))
        hoks           (assoc hoks :id (:id hoks-db))
        opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                         (:opiskeluoikeus-oid hoks))]
    (try
      (op/initiate-if-needed! :aloituskysely hoks)
      (op/initiate-if-needed! :paattokysely hoks)
      (tep/initiate-all-uninitiated! hoks opiskeluoikeus)
      (catch clojure.lang.ExceptionInfo e
        (if (= :organisaatio/organisation-not-found (:type (ex-data e)))
          (throw (ex-info (str "HOKS contains an unknown organisation"
                               (:organisation-oid (ex-data e)))
                          (assoc (ex-data e) :type ::disallowed-update)))
          (log/error e "exception in heräte initiation with" (ex-data e))))
      (catch Exception e
        (log/error e "exception in heräte initiation")))
    hoks-db))

(def ^:private tuva-hoks-msg-template
  "HOKS `%s` is a TUVA-HOKS or rinnakkainen ammatillinen HOKS.")

(defn update!
  "Päivittää HOKSin ylätason arvoja."
  [hoks-id new-values]
  (let [current-hoks (db-hoks/select-hoks-by-id hoks-id)]
    (jdbc/with-db-transaction
      [db-conn (db-ops/get-db-connection)]
      (db-hoks/update-hoks-by-id! hoks-id new-values db-conn)
      (let [updated-hoks (merge current-hoks new-values)
            opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                             (:opiskeluoikeus-oid updated-hoks))]
        (if (tuva-related? updated-hoks)
          (db-hoks/set-amisherate-kasittelytilat-to-true!
            hoks-id (format tuva-hoks-msg-template (:id updated-hoks)))
          (do
            (op/initiate-if-needed! :aloituskysely updated-hoks)
            (op/initiate-if-needed! :paattokysely updated-hoks)
            (tep/initiate-all-uninitiated! updated-hoks opiskeluoikeus)))))
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
  [hoks-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (replace-main-hoks! hoks-id new-values db-conn)
    (replace-parts! (assoc new-values :id hoks-id) db-conn))
  (let [updated-hoks   (get-by-id hoks-id)
        opiskeluoikeus (koski/get-existing-opiskeluoikeus!
                         (:opiskeluoikeus-oid updated-hoks))]
    (if (tuva-related? updated-hoks)
      (db-hoks/set-amisherate-kasittelytilat-to-true!
        hoks-id (format tuva-hoks-msg-template (:id updated-hoks)))
      (do
        (op/initiate-if-needed! :aloituskysely updated-hoks)
        (op/initiate-if-needed! :paattokysely updated-hoks)
        (tep/initiate-all-uninitiated! updated-hoks opiskeluoikeus)))
    updated-hoks))

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
  [old-hoks new-hoks]
  (let [new-oppija-oid (:oppija-oid new-hoks)
        old-oppija-oid (:oppija-oid old-hoks)
        new-opiskeluoikeus-oid (:opiskeluoikeus-oid new-hoks)
        old-opiskeluoikeus-oid (:opiskeluoikeus-oid old-hoks)]
    (when-not (opiskeluoikeus/still-active? new-opiskeluoikeus-oid)
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

(defn check-and-save!
  "Tekee uuden HOKSin tarkistukset ja tallentaa sen, jos kaikki on OK."
  [hoks]
  (let [opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
        oppija-oid         (:oppija-oid hoks)
        opiskeluoikeudet (koski/fetch-opiskeluoikeudet-by-oppija-id oppija-oid)]
    (when-not (oppijaindex/oppija-opiskeluoikeus-match? opiskeluoikeudet
                                                        opiskeluoikeus-oid)
      (-> (format "Opiskeluoikeus `%s` does not match any held by oppija `%s`"
                  opiskeluoikeus-oid
                  oppija-oid)
          (ex-info {:type               ::disallowed-update
                    :opiskeluoikeus-oid opiskeluoikeus-oid
                    :oppija-oid         oppija-oid})
          throw))
    (when-not (opiskeluoikeus/still-active? hoks opiskeluoikeudet)
      (throw (ex-info (format "Opiskeluoikeus `%s` is no longer active"
                              opiskeluoikeus-oid)
                      {:type               ::disallowed-update
                       :opiskeluoikeus-oid opiskeluoikeus-oid})))
    (save! hoks)))

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

(defn- trim-arvioijat
  "Poistaa nimi-kentän jokaisesta arvioija-objektista."
  [arvioijat]
  (mapv (fn [arvioija] (dissoc arvioija :nimi)) arvioijat))

(defn- trim-osa
  "Poistaa nimi-kenttiä tutkinnon osan useista sisälletyistä objekteista."
  [osa]
  (-> osa
      (update-in [:tarkentavat-tiedot-osaamisen-arvioija
                  :aiemmin-hankitun-osaamisen-arvioijat] trim-arvioijat)
      (update :tarkentavat-tiedot-naytto
              (fn [ttn]
                (mapv #(-> %
                           (update
                             :koulutuksen-jarjestaja-osaamisen-arvioijat
                             trim-arvioijat)
                           (update :tyoelama-osaamisen-arvioijat
                                   trim-arvioijat)) ttn)))))

(defn- trim-osaamisen-osoittaminen
  "Poistaa nimi-kenttiä joistakin objekteista osaamisen osoittamisen sisällä."
  [oo]
  (-> oo
      (update :koulutuksen-jarjestaja-osaamisen-arvioijat trim-arvioijat)
      (update :tyoelama-osaamisen-arvioijat trim-arvioijat)))

(defn- trim-ohjaaja-to-boolean
  "Korvaa työpaikalla järjestettävän koulutuksen vastuullisen työpaikkaohjaajan
  booleanarvolla, joka on totta, jos työpaikkaohjaajan tiedot ovat olemassa."
  [oht]
  (if (:tyopaikalla-jarjestettava-koulutus oht)
    (update oht :tyopaikalla-jarjestettava-koulutus
            (fn [tjk] (update tjk
                              :vastuullinen-tyopaikka-ohjaaja boolean)))
    oht))

(defn- trim-osaamisen-hankkimistapa
  "Poistaa järjestäjän edustajan ja hankkijan edustajan osaamisen
  hankkimistavasta, ja trimmaa myös vastuullisen työpaikkaohjaajan booleaniksi."
  [oht]
  (-> oht
      (dissoc :jarjestajan-edustaja)
      (dissoc :hankkijan-edustaja)
      trim-ohjaaja-to-boolean))

(defn- trim-hao
  "Trimmaa hankittavan tutkinnon osan osaamisen hankkimistavat ja osaamisen
  osoittamiset."
  [hao]
  (-> hao
      (update :osaamisen-hankkimistavat
              (fn [ohts] (mapv trim-osaamisen-hankkimistapa ohts)))
      (update :osaamisen-osoittaminen
              (fn [oo] (mapv trim-osaamisen-osoittaminen oo)))))

(defn- trim-hyto
  "Trimmaa hankittavan yhteisen tutkinnon osan osa-alueet."
  [hyto]
  (update hyto :osa-alueet (fn [osa-alueet] (mapv trim-hao osa-alueet))))

(defn- trim-ahyto
  "Trimmaa aiemmin hankitun yhteisen tutkinnon osan osa-alueet."
  [ahyto]
  (update (trim-osa ahyto)
          :osa-alueet
          (fn [osa-alueet] (mapv trim-osa osa-alueet))))

(defn- filter-for-vipunen
  "Trimmaa kaikki HOKSin tutkinnon osat paitsi hankittavat paikalliset tutkinnon
  osat vipusta varten."
  [hoks]
  (-> hoks
      (dissoc :sahkoposti)
      (dissoc :puhelinnumero)
      (update :aiemmin-hankitut-ammat-tutkinnon-osat
              (fn [ahato] (mapv trim-osa ahato)))
      (update :aiemmin-hankitut-paikalliset-tutkinnon-osat
              (fn [ahpto] (mapv trim-osa ahpto)))
      (update :aiemmin-hankitut-yhteiset-tutkinnon-osat
              (fn [ahyto] (mapv trim-ahyto ahyto)))
      (update :hankittavat-ammat-tutkinnon-osat
              (fn [hao] (mapv trim-hao hao)))
      (update :hankittavat-yhteiset-tutkinnon-osat
              (fn [hyto] (mapv trim-hyto hyto)))))

(defn- enrich-and-filter
  "Hakee HOKSin tutkinnon osat tietokannasta ja suodattaa ne vipusta varten."
  [hoks]
  (filter-for-vipunen (get-values hoks)))

(defn get-starting-from-id!
  "Hakee tietyn määrän HOKSeja, jotka on päivitetty tietyn ajankohdan jälkeen ja
  joiden ID:t ovat tiettyä arvoa isompia."
  [id amount updated-after]
  (let [hokses (db-hoks/select-hokses-greater-than-id
                 (or id 0)
                 amount
                 updated-after
                 #{:deleted_at})]
    (map enrich-and-filter hokses)))

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
