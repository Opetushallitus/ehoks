(ns oph.ehoks.hoks.osaamisen-hankkimistapa
  (:import [java.time LocalDate]))

(def tyopaikkajakso-type?
  #{"osaamisenhankkimistapa_koulutussopimus"
    "osaamisenhankkimistapa_oppisopimus"})

(defn tyopaikkajakso?
  "Whether osaamisen hankkimistapa `oht` is tyopaikkajakso."
  [oht]
  (tyopaikkajakso-type? (:osaamisen-hankkimistapa-koodi-uri oht)))

(defn palautteenkeruu-allowed-tyopaikkajakso?
  "Whether oht is a työpaikkajakso from which we can collect feedback."
  [oht]
  (and (tyopaikkajakso? oht)
       (every?
         #(get-in oht %)
         [[:tyopaikalla-jarjestettava-koulutus :vastuullinen-tyopaikka-ohjaaja]
          [:tyopaikalla-jarjestettava-koulutus :tyopaikan-nimi]
          [:tyopaikalla-jarjestettava-koulutus :tyopaikan-y-tunnus]])))

(defn- should-check-hankkimistapa-y-tunnus?
  "Tarkistaa, loppuuko osaamisen hankkimistapa käyttöönottopäivämäärän jälkeen."
  [oh]
  (.isAfter ^LocalDate (:loppu oh) (LocalDate/of 2021 8 25)))

(defn y-tunnus-missing?
  "Puuttuuko Y-tunnus osaamisen hankkimistavasta, vaikka pitäisi olla?"
  [oht]
  (and (tyopaikkajakso? oht)
       (should-check-hankkimistapa-y-tunnus? oht)
       (:tyopaikalla-jarjestettava-koulutus oht)
       (-> oht :tyopaikalla-jarjestettava-koulutus :tyopaikan-y-tunnus nil?)))

(defn has-required-osa-aikaisuustieto?
  "Onko jaksossa osa-aikaisuustieto, jos pitäisi olla?"
  [oht]
  (let [osa-aikaisuustieto (:osa-aikaisuustieto oht)]
    (or (not (.isAfter ^LocalDate (:loppu oht) (LocalDate/of 2023 6 30)))
        (not (tyopaikkajakso? oht))
        (and (some? osa-aikaisuustieto)
             (<= 1 osa-aikaisuustieto 100)))))
