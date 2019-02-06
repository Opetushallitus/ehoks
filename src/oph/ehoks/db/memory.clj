(ns oph.ehoks.db.memory)

(defonce hoks-store (atom '()))

(defonce oppijat-store (atom '()))
(defonce ppto-store (atom '()))
(defonce ppao-store (atom '()))
(defonce pyto-store (atom '()))

(defn clear []
  (reset! hoks-store '())
  (reset! ppto-store '())
  (reset! ppao-store '())
  (reset! pyto-store '()))

(defn get-next-id []
  (if (empty? @hoks-store)
    1
    (inc (apply max (map :eid @hoks-store)))))

(defn get-hoks-by-opiskeluoikeus [oid]
  (some #(when (= (get-in % [:opiskeluoikeus :oid]) oid) %) @hoks-store))

(defn get-hoks-by-eid [eid]
  (when (some? eid)
    (let [h (filter #(= (:eid %) eid) @hoks-store)]
      (last (sort-by :versio h)))))

(defn create-hoks! [hoks]
  (let [old (or
              (get-hoks-by-eid (:eid hoks))
              (get-hoks-by-opiskeluoikeus (get-in hoks [:opiskeluoikeus :oid])))
        h (assoc
            hoks
            :eid (or (:eid old) (get-next-id))
            :luotu (java.util.Date.)
            :hyvaksytty (java.util.Date.)
            :versio (if (some? old) (inc (:versio old)) 1)
            :paivitetty (java.util.Date.))]
    (swap! hoks-store conj h)
    h))

(defn update-hoks! [eid values]
  (when-let [hoks (get-hoks-by-eid eid)]
    (let [updated-hoks
          (assoc
            values
            :paivitetty (java.util.Date.)
            :versio (inc (:versio hoks))
            :luotu (:luotu hoks)
            :luonut (:luonut hoks))]
      (swap! hoks-store conj updated-hoks)
      updated-hoks)))

(defn update-hoks-values! [eid values]
  (when-let [hoks (get-hoks-by-eid eid)]
    (let [updated-hoks
          (-> hoks
              (merge values)
              (assoc
                :paivitetty (java.util.Date.)
                :versio (inc (:versio hoks))
                :luotu (:luotu hoks)
                :luonut (:luonut hoks)))]
      (swap! hoks-store conj updated-hoks)
      updated-hoks)))

(defn get-next-ppto-id []
  (if (empty? @ppto-store)
    1
    (inc (apply max (map :eid @ppto-store)))))

(defn get-ppto-by-eid
  "Hakee puuttuvan paikallisen tutkinnon osan tietueen kannasta sen eid-arvolla"
  [eid]
  (when (some? eid)
    (let [p (filter #(= (:eid %) eid) @ppto-store)]
      (first p))))

(defn update-ppto!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [eid values]
  (let [old-ppto (get-ppto-by-eid eid)
        updated-ppto (merge old-ppto values)]
    (swap! ppto-store conj updated-ppto)
    updated-ppto))

(defn update-ppto-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [eid values]
  (when-let [ppto (get-ppto-by-eid eid)]
    (let [updated-ppto
          (-> ppto
              (merge values))]
      (swap! ppto-store conj updated-ppto)
      updated-ppto)))

(defn create-ppto! [ppto]
  (let [old (get-ppto-by-eid (:eid ppto))
        p (assoc
            ppto
            :eid (or (:eid old) (get-next-ppto-id)))]
    (swap! ppto-store conj p)
    p))

(defn get-next-ppao-id []
  (if (empty? @ppao-store)
    1
    (inc (apply max (map :eid @ppao-store)))))

(defn get-ppao-by-eid
  "Hakee puuttuvan paikallisen tutkinnon osan tietueen kannasta sen eid-arvolla"
  [eid]
  (when (some? eid)
    (let [p (filter #(= (:eid %) eid) @ppao-store)]
      (first p))))

(defn update-ppao!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [eid values]
  (let [updated-ppao values]
    (swap! ppao-store conj updated-ppao)
    updated-ppao))

(defn update-ppao-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [eid values]
  (when-let [ppao (get-ppao-by-eid eid)]
    (let [updated-ppao
          (merge ppao values)]
      (swap! ppao-store conj updated-ppao)
      updated-ppao)))

(defn create-ppao! [ppao]
  (let [old (get-ppao-by-eid (:eid ppao))
        p (assoc
            ppao
            :eid (or (:eid old) (get-next-ppao-id)))]
    (swap! ppao-store conj p)
    p))

(defn get-next-pyto-id []
  (if (empty? @pyto-store)
    1
    (inc (apply max (map :eid @pyto-store)))))

(defn get-pyto-by-eid
  "Hakee puuttuvan paikallisen tutkinnon osan tietueen kannasta sen eid-arvolla"
  [eid]
  (when (some? eid)
    (let [p (filter #(= (:eid %) eid) @pyto-store)]
      (first p))))

(defn update-pyto!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [eid values]
  (let [updated-pyto values]
    (swap! pyto-store conj updated-pyto)
    updated-pyto))

(defn update-pyto-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [eid values]
  (when-let [pyto (get-pyto-by-eid eid)]
    (let [updated-pyto
          (merge pyto values)]
      (swap! pyto-store conj updated-pyto)
      updated-pyto)))

(defn create-pyto! [pyto]
  (let [old (get-pyto-by-eid (:eid pyto))
        updated-pyto (assoc
                       pyto
                       :eid (or (:eid old) (get-next-pyto-id)))]
    (swap! pyto-store conj updated-pyto)
    updated-pyto))
