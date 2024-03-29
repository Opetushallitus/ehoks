(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql.aiemmin-hankitut :as db-ah]
            [oph.ehoks.db.postgresql.hankittavat :as db-ha]
            [oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat :as db-ot]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-oo]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.opiskelijapalaute :as op]
            [clojure.tools.logging :as log]
            [clojure.walk]
            [clojure.set :refer [rename-keys]])
  (:import (java.time LocalDate)
           (java.util UUID)))

(defn get-hoks-values
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

(defn ensure-yksiloiva-tunniste-in-ohts
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
  (clojure.walk/prewalk ensure-yksiloiva-tunniste-in-ohts hoks))

(defn trim-arvioijat
  "Poistaa nimi-kentän jokaisesta arvioija-objektista."
  [arvioijat]
  (mapv (fn [arvioija] (dissoc arvioija :nimi)) arvioijat))

(defn trim-osa
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

(defn trim-osaamisen-osoittaminen
  "Poistaa nimi-kenttiä joistakin objekteista osaamisen osoittamisen sisällä."
  [oo]
  (-> oo
      (update :koulutuksen-jarjestaja-osaamisen-arvioijat trim-arvioijat)
      (update :tyoelama-osaamisen-arvioijat trim-arvioijat)))

(defn trim-ohjaaja-to-boolean
  "Korvaa työpaikalla järjestettävän koulutuksen vastuullisen työpaikkaohjaajan
  booleanarvolla, joka on totta, jos työpaikkaohjaajan tiedot ovat olemassa."
  [oht]
  (if (:tyopaikalla-jarjestettava-koulutus oht)
    (update oht :tyopaikalla-jarjestettava-koulutus
            (fn [tjk] (update tjk
                              :vastuullinen-tyopaikka-ohjaaja boolean)))
    oht))

(defn trim-osaamisen-hankkimistapa
  "Poistaa järjestäjän edustajan ja hankkijan edustajan osaamisen
  hankkimistavasta, ja trimmaa myös vastuullisen työpaikkaohjaajan booleaniksi."
  [oht]
  (-> oht
      (dissoc :jarjestajan-edustaja)
      (dissoc :hankkijan-edustaja)
      trim-ohjaaja-to-boolean))

(defn trim-hao
  "Trimmaa hankittavan tutkinnon osan osaamisen hankkimistavat ja osaamisen
  osoittamiset."
  [hao]
  (-> hao
      (update :osaamisen-hankkimistavat
              (fn [ohts] (mapv trim-osaamisen-hankkimistapa ohts)))
      (update :osaamisen-osoittaminen
              (fn [oo] (mapv trim-osaamisen-osoittaminen oo)))))

(defn trim-hyto
  "Trimmaa hankittavan yhteisen tutkinnon osan osa-alueet."
  [hyto]
  (update hyto :osa-alueet (fn [osa-alueet] (mapv trim-hao osa-alueet))))

(defn trim-ahyto
  "Trimmaa aiemmin hankitun yhteisen tutkinnon osan osa-alueet."
  [ahyto]
  (update (trim-osa ahyto)
          :osa-alueet
          (fn [osa-alueet] (mapv trim-osa osa-alueet))))

(defn get-hokses-by-oppija
  "Hakee HOKSeja oppijan OID:n perusteella."
  [oid]
  (mapv
    get-hoks-values
    (db-hoks/select-hoks-by-oppija-oid oid)))

(defn get-hoks-by-id
  "Hakee yhden HOKSin ID:n perusteella."
  [id]
  (get-hoks-values (db-hoks/select-hoks-by-id id)))

(defn get-hoks-with-hankittavat-koulutuksen-osat!
  [hoks-id]
  (assoc (db-hoks/select-hoks-by-id hoks-id)
         :hankittavat-koulutuksen-osat
         (ha/get-hankittavat-koulutuksen-osat hoks-id)))

(defn filter-for-vipunen
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

(defn enrich-and-filter
  "Hakee HOKSin tutkinnon osat tietokannasta ja suodattaa ne vipusta varten."
  [hoks]
  (filter-for-vipunen (get-hoks-values hoks)))

(defn get-hokses-from-id
  "Hakee tietyn määrän HOKSeja, jotka on päivitetty tietyn ajankohdan jälkeen ja
  joiden ID:t ovat tiettyä arvoa isompia."
  [id amount updated-after]
  (let [hokses (db-hoks/select-hokses-greater-than-id
                 (or id 0)
                 amount
                 updated-after
                 #{:deleted_at})]
    (map enrich-and-filter hokses)))

(defn mark-as-deleted
  "Jos HOKS on passivoitu, se sisältää poistettu-kentän."
  [hoks]
  (if (nil? (:deleted-at hoks))
    hoks
    (rename-keys hoks {:deleted-at :poistettu})))

(defn- save-hoks-parts!
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

(defn save-hoks!
  "Tallentaa yhden HOKSin arvot tietokantaan."
  [hoks]
  (let [hoks (jdbc/with-db-transaction
               [conn (db-ops/get-db-connection)]
               (let [hoks (merge hoks (db-hoks/insert-hoks! hoks conn))
                     tuva-hoks (c/tuva-related-hoks? hoks)]
                 (db-hoks/insert-amisherate-kasittelytilat!
                   (:id hoks) tuva-hoks conn)
                 (save-hoks-parts! hoks conn)))]
    (future
      (op/send-if-needed! :aloituskysely hoks)
      (op/send-if-needed! :paattokysely hoks))
    hoks))

(defn check-and-save-hoks!
  "Tekee uuden HOKSin tarkistukset ja tallentaa sen, jos kaikki on OK."
  [hoks]
  (let [opiskeluoikeudet
        (k/fetch-opiskeluoikeudet-by-oppija-id (:oppija-oid hoks))]
    (when-not
     (oppijaindex/oppija-opiskeluoikeus-match?
       opiskeluoikeudet (:opiskeluoikeus-oid hoks))
      (throw (ex-info "Opiskeluoikeus does not match any held by oppija"
                      {:error :disallowed-update})))
    (when-not
     (oppijaindex/opiskeluoikeus-still-active? hoks opiskeluoikeudet)
      (throw (ex-info (format "Opiskeluoikeus %s is no longer active"
                              (:opiskeluoikeus-oid hoks))
                      {:error :disallowed-update})))
    (save-hoks! hoks)))

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

(defn- replace-main-hoks!
  "Korvaa HOKSin ydinsisällön annetuilla arvoilla. Ei tallenna tutkinnon osia."
  [hoks-id new-values db-conn]
  (db-hoks/update-hoks-by-id!
    hoks-id (merge-not-given-hoks-values new-values) db-conn))

(defn- replace-oto!
  "Korvaa vanhat opiskeluvalmiuksia tukevat opinnot annetuilla arviolla."
  [hoks-id new-oto-values db-conn]
  (db-ot/delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id
    hoks-id db-conn)
  (when
   new-oto-values
    (ot/save-opiskeluvalmiuksia-tukevat-opinnot!
      hoks-id new-oto-values db-conn)))

(defn- replace-hato!
  "Korvaa vanhat hankittavat ammatilliset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-hato-values db-conn]
  (db-ha/delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hato-values
    (ha/save-hankittavat-ammat-tutkinnon-osat!
      hoks-id new-hato-values db-conn)))

(defn- replace-hpto!
  "Korvaa vanhat hankittavat paikalliset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-hpto-values db-conn]
  (db-ha/delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hpto-values
    (ha/save-hankittavat-paikalliset-tutkinnon-osat!
      hoks-id new-hpto-values db-conn)))

(defn- replace-hyto!
  "Korvaa vanhat hankittavat yhteiset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-hyto-values db-conn]
  (db-ha/delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-hyto-values
    (ha/save-hankittavat-yhteiset-tutkinnon-osat!
      hoks-id new-hyto-values db-conn)))

(defn- replace-hankittavat-koulutuksen-osat!
  "Korvaa vanhat hankittavat koulutuksen osat annetuilla arvoilla."
  [hoks-id new-koulutuksen-osat-values db-conn]
  (db-ha/delete-hankittavat-koulutuksen-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-koulutuksen-osat-values
    (ha/save-hankittavat-koulutuksen-osat!
      hoks-id new-koulutuksen-osat-values db-conn)))

(defn- replace-ahato!
  "Korvaa vanhat aiemmin hankitut ammatilliset tutkinnon osat annetuilla
  arvoilla."
  [hoks-id new-ahato-values db-conn]
  (db-ah/delete-aiemmin-hankitut-ammatilliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahato-values
    (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
      hoks-id new-ahato-values db-conn)))

(defn- replace-ahpto!
  "Korvaa vanhat aiemmin hankitut paikalliset tutkinnon osat annetuilla
  arvoilla."
  [hoks-id new-ahpto-values db-conn]
  (db-ah/delete-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahpto-values
    (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
      hoks-id new-ahpto-values db-conn)))

(defn- replace-ahyto!
  "Korvaa vanhat aiemmin hankitut yhteiset tutkinnon osat annetuilla arvoilla."
  [hoks-id new-ahyto-values db-conn]
  (db-ah/delete-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id
    hoks-id db-conn)
  (when
   new-ahyto-values
    (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
      hoks-id new-ahyto-values db-conn)))

(defn- should-check-hankkimistapa-y-tunnus?
  "Tarkistaa, loppuuko osaamisen hankkimistapa käyttöönottopäivämäärän jälkeen."
  [oh]
  (.isAfter ^LocalDate (:loppu oh) (LocalDate/of 2021 8 25)))

(defn tyopaikkajakso?
  "Onko osaamisen hankkimistapa työpaikkajakso?"
  [oht]
  (#{"osaamisenhankkimistapa_koulutussopimus"
     "osaamisenhankkimistapa_oppisopimus"}
    (:osaamisen-hankkimistapa-koodi-uri oht)))

(defn y-tunnus-missing?
  "Puuttuuko Y-tunnus osaamisen hankkimistavasta, vaikka pitäisi olla?"
  [oh]
  (and (tyopaikkajakso? oh)
       (should-check-hankkimistapa-y-tunnus? oh)
       (:tyopaikalla-jarjestettava-koulutus oh)
       (-> oh :tyopaikalla-jarjestettava-koulutus :tyopaikan-y-tunnus nil?)))

(defn check-hoks-for-update!
  "Tarkistaa, saako HOKSin päivittää uusilla arvoilla."
  [old-hoks new-hoks]
  (let [new-oppija-oid (:oppija-oid new-hoks)
        old-oppija-oid (:oppija-oid old-hoks)
        new-opiskeluoikeus-oid (:opiskeluoikeus-oid new-hoks)
        old-opiskeluoikeus-oid (:opiskeluoikeus-oid old-hoks)]
    (when-not
     (oppijaindex/opiskeluoikeus-still-active? new-opiskeluoikeus-oid)
      (throw (ex-info (format "Opiskeluoikeus %s is no longer active"
                              new-opiskeluoikeus-oid)
                      {:error :disallowed-update})))
    (when (and (some? new-opiskeluoikeus-oid)
               (not= new-opiskeluoikeus-oid old-opiskeluoikeus-oid))
      (throw (ex-info "Opiskeluoikeus update not allowed!"
                      {:error :disallowed-update})))
    (when (and (some? new-oppija-oid)
               (not= new-oppija-oid old-oppija-oid))
      (throw (ex-info "Oppija-oid update not allowed!"
                      {:error :disallowed-update})))))

(defn- replace-hoks-parts!
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

(def ^:private tuva-hoks-msg-template
  "HOKS `%s` is a TUVA-HOKS or rinnakkainen ammatillinen HOKS.")

(defn replace-hoks!
  "Korvaa kokonaisen HOKSin (ml. tutkinnon osat) annetuilla arvoilla."
  [hoks-id new-values]
  (let [current-hoks (get-hoks-by-id hoks-id)
        amisherate-kasittelytila
        (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id! hoks-id)
        h (jdbc/with-db-transaction
            [db-conn (db-ops/get-db-connection)]
            (replace-main-hoks! hoks-id new-values db-conn)
            (replace-hoks-parts! (assoc new-values :id hoks-id) db-conn))
        updated-hoks (get-hoks-by-id hoks-id)]
    (if (c/tuva-related-hoks? updated-hoks)
      (db-hoks/set-amisherate-kasittelytilat-to-true!
        amisherate-kasittelytila
        (format tuva-hoks-msg-template (:id updated-hoks)))
      (do
        (when (op/send? :aloituskysely current-hoks updated-hoks)
          (db-hoks/update-amisherate-kasittelytilat!
            {:id (:id amisherate-kasittelytila)
             :aloitusherate_kasitelty false})
          (op/send! :aloituskysely updated-hoks))
        (when (op/send? :paattokysely current-hoks updated-hoks)
          (db-hoks/update-amisherate-kasittelytilat!
            {:id (:id amisherate-kasittelytila)
             :paattoherate_kasitelty false})
          (op/send! :paattokysely updated-hoks))))
    h))

(defn update-hoks!
  "Päivittää annetut arvot HOKSiin."
  [hoks-id new-values]
  (let [current-hoks (get-hoks-by-id hoks-id)
        amisherate-kasittelytila
        (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id! hoks-id)]
    (jdbc/with-db-transaction
      [db-conn (db-ops/get-db-connection)]
      (db-hoks/update-hoks-by-id! hoks-id new-values db-conn)
      (let [updated-hoks (merge current-hoks new-values)]
        (if (c/tuva-related-hoks? updated-hoks)
          (db-hoks/set-amisherate-kasittelytilat-to-true!
            amisherate-kasittelytila
            (format tuva-hoks-msg-template (:id updated-hoks)))
          (do
            (when (op/send? :aloituskysely current-hoks updated-hoks)
              (db-hoks/update-amisherate-kasittelytilat!
                {:id (:id amisherate-kasittelytila)
                 :aloitusherate_kasitelty false})
              (op/send! :aloituskysely updated-hoks))
            (when (op/send? :paattokysely current-hoks updated-hoks)
              (db-hoks/update-amisherate-kasittelytilat!
                {:id (:id amisherate-kasittelytila)
                 :paattoherate_kasitelty false})
              (op/send! :paattokysely updated-hoks))))
        updated-hoks))))

(defn insert-kyselylinkki!
  "Lisää yhden kyselylinkin tietokantatauluun."
  [m]
  (db-ops/insert-one!
    :kyselylinkit
    (db-ops/to-sql m)))

(defn update-kyselylinkki!
  "Päivittää yhden kyselylinkin tietokantarivin."
  [m]
  (db-ops/update!
    :kyselylinkit
    (db-ops/to-sql m)
    ["kyselylinkki = ?" (:kyselylinkki m)]))

(defn get-kyselylinkit-by-oppija-oid
  "Hakee kyselylinkkejä tietokannasta oppijan OID:n perusteella."
  [oid]
  (db-hoks/select-kyselylinkit-by-oppija-oid oid))

(defn delete-kyselylinkki!
  "Poistaa kyselylinkin tietokannasta."
  [kyselylinkki]
  (db-ops/delete!
    :kyselylinkit
    ["kyselylinkki = ?" kyselylinkki]))

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
              hoks-opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
              opiskeluoikeudet (k/fetch-opiskeluoikeudet-by-oppija-id
                                 oppija-oid)]
          (oppijaindex/add-oppija-hankintakoulutukset opiskeluoikeudet
                                                      hoks-opiskeluoikeus-oid
                                                      oppija-oid))
        (catch Exception _
          (log/errorf "Hankintakoulutukset-päivitys epäonnistui hoksille %s"
                      (:id hoks)))))
    (let [hokses-without-oo
          (filter
            some?
            (pmap
              (fn [x]
                (when (nil?
                        (k/get-opiskeluoikeus-info
                          (:opiskeluoikeus-oid x))) x))
              hoksit-created-in-7-days))
          oo-oids (map :opiskeluoikeus-oid hokses-without-oo)]
      (log/infof "Päivitetään opiskeluoikeus-indeksiin koski404 tietoja.
                  Löydettiin %s hoksia ilman opiskeluoikeutta Koskessa."
                 (count oo-oids))
      (doseq [oo-oid oo-oids]
        (let [opiskeluoikeus (db-oo/select-opiskeluoikeus-by-oid oo-oid)]
          (when (some? opiskeluoikeus)
            (oppijaindex/set-opiskeluoikeus-koski404 oo-oid)))))))
