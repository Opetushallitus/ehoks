(ns oph.ehoks.db.memory)

(defonce hoks-store (atom '()))

(defonce oppijat-store (atom '()))

(defn clear []
  (reset! hoks-store '()))

(defn get-next-id []
  (if (empty? @hoks-store)
    1
    (inc (apply max (map :id @hoks-store)))))

(defn get-hoks-by-opiskeluoikeus [oid]
  (some #(when (= (get-in % [:opiskeluoikeus :oid]) oid) %) @hoks-store))

(defn find-hoks [p]
  (let [h (filter p @hoks-store)]
    (last (sort-by :versio h))))

(defn get-hoks-by-id [id]
  (when (some? id)
    (find-hoks #(= (:id %) id))))

(defn get-all-hoks-by-oppija [oppija-oid]
  (when (some? oppija-oid)
    (->> (filter #(= (:oppija-oid %) oppija-oid) @hoks-store)
         (group-by :id)
         (map (fn [[id h]] (last (sort-by :versio h)))))))

(defn create-hoks! [hoks]
  (let [old (or
              (get-hoks-by-id (:id hoks))
              (get-hoks-by-opiskeluoikeus (get-in hoks [:opiskeluoikeus :oid])))
        h (assoc
            hoks
            :id (or (:id old) (get-next-id))
            :luotu (java.util.Date.)
            :hyvaksytty (java.util.Date.)
            :versio (if (some? old) (inc (:versio old)) 1)
            :paivitetty (java.util.Date.))]
    (swap! hoks-store conj h)
    h))

(defn update-hoks! [id values]
  (when-let [hoks (get-hoks-by-id id)]
    (let [updated-hoks
          (assoc
            values
            :paivitetty (java.util.Date.)
            :versio (inc (:versio hoks))
            :luotu (:luotu hoks)
            :luonut (:luonut hoks))]
      (swap! hoks-store conj updated-hoks)
      updated-hoks)))

(defn update-hoks-values! [id values]
  (when-let [hoks (get-hoks-by-id id)]
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
