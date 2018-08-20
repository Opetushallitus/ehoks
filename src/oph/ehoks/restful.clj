(ns oph.ehoks.restful)

(defn response [body & data]
  (let [data-map (apply hash-map data)]
    {:meta (get :meta data-map {})
     :data body}))
