(ns oph.ehoks.db.memory)

(defonce hoks-store (atom '()))

(defn get-next-id []
  (if (empty? @hoks-store)
    1
    (inc (apply max (map :eid @hoks-store)))))

(defn create-hoks! [hoks]
  (let [h (assoc hoks :eid (get-next-id))]
    (swap! hoks-store conj h)
    h))

(defn get-hoks [eid]
  (some #(when (= (:eid %) eid) %) @hoks-store))
