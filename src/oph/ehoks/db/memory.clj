(ns oph.ehoks.db.memory)

(defonce hoks-store (atom '()))

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

(defn get-hoks [eid]
  (some #(when (= (:eid %) eid) %) @hoks-store))
