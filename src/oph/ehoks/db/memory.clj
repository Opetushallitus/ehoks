(ns oph.ehoks.db.memory)

(defonce hoks-store (atom '()))

(defonce oppijat-store (atom '()))
(defonce ppto-store (atom '()))

(defn clear []
  (reset! hoks-store '()))

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
      (last (sort-by :versio p)))))

(defn update-ppto!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [eid values]
  (let [updated-ppto values]
    (swap! ppto-store conj updated-ppto)
    updated-ppto))

(defn update-ppto-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [eid values]
  (when-let [ppto (get-ppto-by-eid eid)]
    (let [updated-ppto
          (merge ppto values)]
      (swap! ppto-store conj updated-ppto)
      updated-ppto)))

(defn create-ppto! [ppto]
  (let [old (get-ppto-by-eid (:eid ppto))
        p (assoc
            ppto
            :eid (or (:eid old) (get-next-ppto-id)))]
    (swap! ppto-store conj p)
    p))
