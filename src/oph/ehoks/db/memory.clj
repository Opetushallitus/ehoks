(ns oph.ehoks.db.memory)

(defonce hoks-store (atom '()))

(defonce oppijat-store (atom '()))
(defonce ppto-store (atom '()))
(defonce ppao-store (atom '()))
(defonce pyto-store (atom '()))
(defonce ovatu-store (atom '()))

(defn clear []
  (reset! hoks-store '())
  (reset! ppto-store '())
  (reset! ppao-store '())
  (reset! pyto-store '())
  (reset! ovatu-store '()))

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
            :laatinut (:laatinut hoks))]
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
                :laatinut (:laatinut hoks)))]
      (swap! hoks-store conj updated-hoks)
      updated-hoks)))

(defn get-next-ppto-id []
  (if (empty? @ppto-store)
    1
    (inc (apply max (map :id @ppto-store)))))

(defn get-ppto-by-id
  "Hakee puuttuvan paikallisen tutkinnon osan tietueen kannasta sen id-arvolla"
  [id]
  (when (some? id)
    (let [p (filter #(= (:id %) id) @ppto-store)]
      (first p))))

(defn update-ppto!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [id values]
  (let [old-ppto (get-ppto-by-id id)
        updated-ppto (merge old-ppto values)]
    (swap! ppto-store conj updated-ppto)
    updated-ppto))

(defn update-ppto-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [id values]
  (when-let [ppto (get-ppto-by-id id)]
    (let [updated-ppto (merge ppto values)]
      (swap! ppto-store conj updated-ppto)
      updated-ppto)))

(defn create-ppto! [ppto]
  (let [old (get-ppto-by-id (:id ppto))
        p (assoc
            ppto
            :id (or (:id old) (get-next-ppto-id)))]
    (swap! ppto-store conj p)
    p))

(defn get-next-ppao-id []
  (if (empty? @ppao-store)
    1
    (inc (apply max (map :id @ppao-store)))))

(defn get-ppao-by-id
  "Hakee puuttuvan paikallisen tutkinnon osan tietueen kannasta sen id-arvolla"
  [id]
  (when (some? id)
    (let [p (filter #(= (:id %) id) @ppao-store)]
      (first p))))

(defn update-ppao!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [id values]
  (let [updated-ppao values]
    (swap! ppao-store conj updated-ppao)
    updated-ppao))

(defn update-ppao-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [id values]
  (when-let [ppao (get-ppao-by-id id)]
    (let [updated-ppao
          (merge ppao values)]
      (swap! ppao-store conj updated-ppao)
      updated-ppao)))

(defn create-ppao! [ppao]
  (let [old (get-ppao-by-id (:id ppao))
        p (assoc
            ppao
            :id (or (:id old) (get-next-ppao-id)))]
    (swap! ppao-store conj p)
    p))

(defn get-next-pyto-id []
  (if (empty? @pyto-store)
    1
    (inc (apply max (map :id @pyto-store)))))

(defn get-pyto-by-id
  "Hakee puuttuvan paikallisen tutkinnon osan tietueen kannasta sen id-arvolla"
  [id]
  (when (some? id)
    (let [p (filter #(= (:id %) id) @pyto-store)]
      (first p))))

(defn update-pyto!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [id values]
  (let [updated-pyto values]
    (swap! pyto-store conj updated-pyto)
    updated-pyto))

(defn update-pyto-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [id values]
  (when-let [pyto (get-pyto-by-id id)]
    (let [updated-pyto
          (merge pyto values)]
      (swap! pyto-store conj updated-pyto)
      updated-pyto)))

(defn create-pyto! [pyto]
  (let [old (get-pyto-by-id (:id pyto))
        updated-pyto (assoc
                       pyto
                       :id (or (:id old) (get-next-pyto-id)))]
    (swap! pyto-store conj updated-pyto)
    updated-pyto))

;; OPISKELUVALMIUKSIA TUKEVAT OPINNOT

(defn get-next-ovatu-id []
  (if (empty? @ovatu-store)
    1
    (inc (apply max (map :id @ovatu-store)))))

(defn get-ovatu-by-id
  "Hakee puuttuvan paikallisen tutkinnon osan tietueen kannasta sen id-arvolla"
  [id]
  (when (some? id)
    (let [p (filter #(= (:id %) id) @ovatu-store)]
      (first p))))

(defn update-ovatu!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osaa"
  [id values]
  (let [updated-ovatu values]
    (swap! ovatu-store conj updated-ovatu)
    updated-ovatu))

(defn update-ovatu-values!
  "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan joko kaikkien arvojen tai vain yhden tai useamman osalta"
  [id values]
  (when-let [ovatu (get-ovatu-by-id id)]
    (let [updated-ovatu
          (merge ovatu values)]
      (swap! ovatu-store conj updated-ovatu)
      updated-ovatu)))

(defn create-ovatu! [ovatu]
  (let [old (get-ovatu-by-id (:id ovatu))
        updated-ovatu (assoc
                        ovatu
                        :id (or (:id old) (get-next-ovatu-id)))]
    (swap! ovatu-store conj updated-ovatu)
    updated-ovatu))
