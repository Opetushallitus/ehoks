(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql.aiemmin-hankitut :as db-ah]
            [oph.ehoks.db.postgresql.hankittavat :as db-ha]
            [oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat :as db-ot]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-oo]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.external.koski :as k]
            [clojure.tools.logging :as log])
  (:import (java.time LocalDate)))

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
                 true)]
    (map enrich-and-filter hokses)))

(defn redact-deleted-hokses
  "Jos HOKS on passivoitu, se sisältää vain id- ja eid-kentät."
  [hoks]
  (if (nil? (:deleted-at hoks))
    hoks
    (select-keys hoks [:id :eid :ensikertainen-hyvaksyminen])))

(defn- new-osaamisen-saavuttamisen-pvm-added?
  "Tarkistaa, onko uusi osaamisen saavuttamisen päivämäärä lisätty kun vanhaa ei
  ollut."
  [old-osp new-osp]
  (and (some? new-osp)
       (not= new-osp old-osp)))

(defn check-suoritus-type?
  "Tarkistaa, onko suorituksen tyyppi ammatillinen suoritus tai osittainen
  ammatillinen suoritus."
  [suoritus]
  (or (= (:koodiarvo (:tyyppi suoritus)) "ammatillinentutkinto")
      (= (:koodiarvo (:tyyppi suoritus)) "ammatillinentutkintoosittainen")))

(defn get-suoritus
  "Hakee opiskeluoikeudesta ensimmäisen suorituksen, jonka tyyppi on
  ammatillinen suoritus tai osittainen ammatillinen suoritus."
  [opiskeluoikeus]
  (reduce
    (fn [_ suoritus]
      (when (check-suoritus-type? suoritus)
        (reduced suoritus)))
    nil (:suoritukset opiskeluoikeus)))

(defn get-kysely-type
  "Muuttaa opiskeluoikeuden suorituksen tyypin sellaiseksi, minkä herätepalvelu
  voi hyväksyä."
  [opiskeluoikeus]
  (let [tyyppi (get-in
                 (get-suoritus opiskeluoikeus)
                 [:tyyppi :koodiarvo])]
    (cond
      (= tyyppi "ammatillinentutkinto")
      "tutkinnon_suorittaneet"
      (= tyyppi "ammatillinentutkintoosittainen")
      "tutkinnon_osia_suorittaneet")))

(defn- send-paattokysely
  "Lähettää AMIS päättöpalautekyselyn herätepalveluun."
  [hoks-id os-saavut-pvm hoks]
  (try (let [opiskeluoikeus (k/get-opiskeluoikeus-info
                              (:opiskeluoikeus-oid hoks))
             kyselytyyppi (get-kysely-type opiskeluoikeus)]
         (when (and
                 (some? opiskeluoikeus)
                 (some? kyselytyyppi))
           (log/infof
             (str "Sending päättökysely for hoks id %s. "
                  "Triggered by hoks post or update. "
                  "os-saavuttamisen-pvm %s. "
                  "Kyselyn tyyppi: %s")
             hoks-id os-saavut-pvm kyselytyyppi)
           (sqs/send-amis-palaute-message
             (sqs/build-hoks-osaaminen-saavutettu-msg
               hoks-id os-saavut-pvm hoks kyselytyyppi))))
       (catch Exception e
         (log/warn e)
         (log/warnf (str "Error in sending päättökysely for hoks id %s. "
                         "os-saavuttamisen-pvm %s. "
                         "opiskeluoikeus-oid %s.")
                    hoks-id os-saavut-pvm (:opiskeluoikeus-oid hoks)))))

(defn error-log-hoks-id
  "Logittaa HOKSin ID:n virheenä."
  [id]
  (log/errorf "Error caused by hoks-id: %s" id))

(defn save-hoks!
  "Tallentaa yhden HOKSin arvot tietokantaan."
  [h]
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (let [saved-hoks (db-hoks/insert-hoks! h conn)]
      (db-hoks/insert-amisherate-kasittelytilat! (:id saved-hoks) conn)
      (when (:osaamisen-hankkimisen-tarve h)
        (sqs/send-amis-palaute-message
          (sqs/build-hoks-hyvaksytty-msg
            (:id saved-hoks) h)))
      (when (:osaamisen-saavuttamisen-pvm h)
        (send-paattokysely (:id saved-hoks)
                           (:osaamisen-saavuttamisen-pvm h)
                           h))
      (assoc
        saved-hoks
        :aiemmin-hankitut-ammat-tutkinnon-osat
        (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
          (:id saved-hoks)
          (:aiemmin-hankitut-ammat-tutkinnon-osat h)
          conn)
        :aiemmin-hankitut-paikalliset-tutkinnon-osat
        (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
          (:id saved-hoks)
          (:aiemmin-hankitut-paikalliset-tutkinnon-osat h)
          conn)
        :hankittavat-paikalliset-tutkinnon-osat
        (ha/save-hankittavat-paikalliset-tutkinnon-osat!
          (:id saved-hoks)
          (:hankittavat-paikalliset-tutkinnon-osat h)
          conn)
        :aiemmin-hankitut-yhteiset-tutkinnon-osat
        (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat!
          (:id saved-hoks)
          (:aiemmin-hankitut-yhteiset-tutkinnon-osat h)
          conn)
        :hankittavat-ammat-tutkinnon-osat
        (ha/save-hankittavat-ammat-tutkinnon-osat!
          (:id saved-hoks)
          (:hankittavat-ammat-tutkinnon-osat h)
          conn)
        :opiskeluvalmiuksia-tukevat-opinnot
        (ot/save-opiskeluvalmiuksia-tukevat-opinnot!
          (:id saved-hoks)
          (:opiskeluvalmiuksia-tukevat-opinnot h)
          conn)
        :hankittavat-yhteiset-tutkinnon-osat
        (ha/save-hankittavat-yhteiset-tutkinnon-osat!
          (:id saved-hoks)
          (:hankittavat-yhteiset-tutkinnon-osat h)
          conn)
        :hankittavat-koulutuksen-osat
        (ha/save-hankittavat-koulutuksen-osat!
          (:id saved-hoks)
          (:hankittavat-koulutuksen-osat h)
          conn)))))

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

(defn get-osaamisen-hankkimistavat
  "Hakee kaikki osaamisen hankkimistavat HOKSista."
  [hoks]
  (concat
    (mapcat
      :osaamisen-hankkimistavat
      (:hankittavat-ammat-tutkinnon-osat hoks))
    (mapcat
      :osaamisen-hankkimistavat
      (:hankittavat-paikalliset-tutkinnon-osat hoks))
    (mapcat :osaamisen-hankkimistavat
            (mapcat :osa-alueet (:hankittavat-yhteiset-tutkinnon-osat hoks)))))

(defn- should-check-hankkimistapa-y-tunnus?
  "Tarkistaa, loppuuko osaamisen hankkimistapa käyttöönottopäivämäärän jälkeen."
  [oh]
  (let [kayttoonottopvm (LocalDate/parse "2021-08-25")]
    (try
      (or
        (or (.isAfter (:alku oh) kayttoonottopvm)
            (.isEqual (:alku oh) kayttoonottopvm))
        (and
          (.isBefore (:alku oh) kayttoonottopvm)
          (.isAfter (:loppu oh) kayttoonottopvm)))
      (catch Exception e
        (log/info "should-check-hankkimistapa-y-tunnus? check failed for:")
        (log/info oh)
        (log/info e)))))

(defn- y-tunnus-missing?
  "Palauttaa osaamisen hankkimistavan, jos siinä pitäisi tarkistaa Y-tunnus
  mutta se puuttuu."
  [oh]
  (when (and
          (or
            (= (:osaamisen-hankkimistapa-koodi-uri oh)
               "osaamisenhankkimistapa_koulutussopimus")
            (= (:osaamisen-hankkimistapa-koodi-uri oh)
               "osaamisenhankkimistapa_oppisopimus"))
          (should-check-hankkimistapa-y-tunnus? oh)
          (:tyopaikalla-jarjestettava-koulutus oh))
    (when (nil? (get-in oh [:tyopaikalla-jarjestettava-koulutus
                            :tyopaikan-y-tunnus]))
      oh)))

(defn missing-tyopaikan-y-tunnus?
  "Tarkistaa, onko osaamisen hankkimistapojen joukossa yksi tai useampi, josta
  Y-tunnus puuttuu (ja joka loppuu käyttöönottopäivämäärän jälkeen)."
  [osaamisen-hankkimistavat]
  (when (seq osaamisen-hankkimistavat)
    (some y-tunnus-missing? osaamisen-hankkimistavat)))

(defn replace-hoks!
  "Korvaa kokonaisen HOKSin (ml. tutkinnon osat) annetuilla arvoilla."
  [hoks-id new-values]
  (let [current-hoks (get-hoks-by-id hoks-id)
        old-opiskeluoikeus-oid (:opiskeluoikeus-oid current-hoks)
        old-oppija-oid (:oppija-oid current-hoks)
        old-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                          current-hoks)
        old-osaamisen-hankkimisen-tarve (:osaamisen-hankkimisen-tarve
                                          current-hoks)
        old-sahkoposti (:sahkoposti current-hoks)
        old-puhelinnumero (:puhelinnumero current-hoks)
        new-opiskeluoikeus-oid (:opiskeluoikeus-oid new-values)
        new-oppija-oid (:oppija-oid new-values)
        new-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                          new-values)
        new-osaamisen-hankkimisen-tarve (:osaamisen-hankkimisen-tarve
                                          new-values)
        new-sahkoposti (:sahkoposti new-values)
        new-puhelinnumero (:puhelinnumero new-values)
        osaamisen-hankkimistavat (get-osaamisen-hankkimistavat new-values)
        oh-missing-tyopaikan-y-tunnus (missing-tyopaikan-y-tunnus?
                                        osaamisen-hankkimistavat)
        amisherate-kasittelytila
        (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id! hoks-id)
        h (jdbc/with-db-transaction
            [db-conn (db-ops/get-db-connection)]
            (cond
              (and (some? new-opiskeluoikeus-oid)
                   (not= new-opiskeluoikeus-oid
                         old-opiskeluoikeus-oid))
              (throw (ex-info
                       "Opiskeluoikeus update not allowed!"
                       {:error :disallowed-update}))
              (and (some? new-oppija-oid)
                   (not= new-oppija-oid old-oppija-oid))
              (throw (ex-info
                       "Oppija-oid update not allowed!"
                       {:error :disallowed-update}))
              (some? oh-missing-tyopaikan-y-tunnus)
              (throw (ex-info
                       (str "tyopaikan-y-tunnus missing for "
                            "osaamisen hankkimistapa: "
                            oh-missing-tyopaikan-y-tunnus)
                       {:error :disallowed-update}))
              :else
              (do
                (replace-main-hoks! hoks-id new-values db-conn)
                (replace-oto! hoks-id
                              (:opiskeluvalmiuksia-tukevat-opinnot
                                new-values)
                              db-conn)
                (replace-hato! hoks-id
                               (:hankittavat-ammat-tutkinnon-osat
                                 new-values)
                               db-conn)
                (replace-hpto!
                  hoks-id
                  (:hankittavat-paikalliset-tutkinnon-osat
                    new-values)
                  db-conn)
                (replace-hyto! hoks-id
                               (:hankittavat-yhteiset-tutkinnon-osat
                                 new-values)
                               db-conn)
                (replace-hankittavat-koulutuksen-osat!
                  hoks-id
                  (:hankittavat-koulutuksen-osat
                    new-values)
                  db-conn)
                (replace-ahato!
                  hoks-id
                  (:aiemmin-hankitut-ammat-tutkinnon-osat
                    new-values)
                  db-conn)
                (replace-ahpto!
                  hoks-id
                  (:aiemmin-hankitut-paikalliset-tutkinnon-osat
                    new-values)
                  db-conn)
                (replace-ahyto!
                  hoks-id
                  (:aiemmin-hankitut-yhteiset-tutkinnon-osat
                    new-values)
                  db-conn))))
        updated-hoks (get-hoks-by-id hoks-id)]
    (do
      (when (new-osaamisen-saavuttamisen-pvm-added?
              old-osaamisen-saavuttamisen-pvm
              new-osaamisen-saavuttamisen-pvm)
        (db-hoks/update-amisherate-kasittelytilat!
          {:id (:id amisherate-kasittelytila)
           :paattoherate_kasitelty false})
        (send-paattokysely hoks-id
                           new-osaamisen-saavuttamisen-pvm
                           updated-hoks))
      (when (or (and
                  (true? new-osaamisen-hankkimisen-tarve)
                  (not (true? old-osaamisen-hankkimisen-tarve)))
                (and
                  (some? new-sahkoposti)
                  (not (some? old-sahkoposti)))
                (and
                  (some? new-puhelinnumero)
                  (not (some? old-puhelinnumero))))
        (db-hoks/update-amisherate-kasittelytilat!
          {:id (:id amisherate-kasittelytila)
           :aloitusherate_kasitelty false})
        (sqs/send-amis-palaute-message
          (sqs/build-hoks-hyvaksytty-msg hoks-id updated-hoks))))
    h))

(defn update-hoks!
  "Päivittää annetut arvot HOKSiin."
  [hoks-id new-values]
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (let [hoks (get-hoks-by-id hoks-id)
          old-opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
          old-oppija-oid (:oppija-oid hoks)
          old-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                            hoks)
          old-osaamisen-hankkimisen-tarve (:osaamisen-hankkimisen-tarve
                                            hoks)
          old-sahkoposti (:sahkoposti hoks)
          old-puhelinnumero (:puhelinnumero hoks)
          new-opiskeluoikeus-oid (:opiskeluoikeus-oid new-values)
          new-oppija-oid (:oppija-oid new-values)
          new-osaamisen-saavuttamisen-pvm (:osaamisen-saavuttamisen-pvm
                                            new-values)
          new-osaamisen-hankkimisen-tarve (:osaamisen-hankkimisen-tarve
                                            new-values)
          new-sahkoposti (:sahkoposti new-values)
          new-puhelinnumero (:puhelinnumero new-values)
          osaamisen-hankkimistavat (get-osaamisen-hankkimistavat new-values)
          oh-missing-tyopaikan-y-tunnus (missing-tyopaikan-y-tunnus?
                                          osaamisen-hankkimistavat)
          amisherate-kasittelytila
          (db-hoks/get-or-create-amisherate-kasittelytila-by-hoks-id! hoks-id)]
      (cond
        (and (some? new-opiskeluoikeus-oid)
             (not= new-opiskeluoikeus-oid old-opiskeluoikeus-oid))
        (throw (ex-info
                 "Opiskeluoikeus update not allowed!"
                 {:error :disallowed-update}))
        (and (some? new-oppija-oid) (not= new-oppija-oid old-oppija-oid))
        (throw (ex-info
                 "Oppija-oid update not allowed!"
                 {:error :disallowed-update}))
        (some? oh-missing-tyopaikan-y-tunnus)
        (throw (ex-info
                 (str "tyopaikan-y-tunnus missing for "
                      "osaamisen hankkimistapa: " oh-missing-tyopaikan-y-tunnus)
                 {:error :disallowed-update}))
        :else
        (let [h (db-hoks/update-hoks-by-id! hoks-id new-values db-conn)]
          (when (new-osaamisen-saavuttamisen-pvm-added?
                  old-osaamisen-saavuttamisen-pvm
                  new-osaamisen-saavuttamisen-pvm)
            (db-hoks/update-amisherate-kasittelytilat!
              {:id (:id amisherate-kasittelytila)
               :paattoherate_kasitelty false})
            (send-paattokysely hoks-id new-osaamisen-saavuttamisen-pvm hoks))
          (when (or (and
                      (true? new-osaamisen-hankkimisen-tarve)
                      (not (true? old-osaamisen-hankkimisen-tarve)))
                    (and
                      (some? new-sahkoposti)
                      (not (some? old-sahkoposti)))
                    (and
                      (some? new-puhelinnumero)
                      (not (some? old-puhelinnumero))))
            (db-hoks/update-amisherate-kasittelytilat!
              {:id (:id amisherate-kasittelytila)
               :aloitusherate_kasitelty false})
            (sqs/send-amis-palaute-message
              (sqs/build-hoks-hyvaksytty-msg hoks-id hoks)))
          h)))))

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
        (catch Exception e
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
