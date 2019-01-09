(ns oph.ehoks.db.memory)

(defonce hoks-store (atom '()))

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
  (let [h (assoc hoks :eid (get-next-id))]
    (swap! hoks-store conj h)
    h))

(defn get-hoks [eid]
  (some #(when (= (:eid %) eid) %) @hoks-store))
