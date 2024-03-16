(ns oph.ehoks.hoks.osaamisen-hankkimistapa
  (:import [java.time LocalDate]))

(defn tyopaikkajakso?
  "Onko osaamisen hankkimistapa työpaikkajakso?"
  [oht]
  (#{"osaamisenhankkimistapa_koulutussopimus"
     "osaamisenhankkimistapa_oppisopimus"}
    (:osaamisen-hankkimistapa-koodi-uri oht)))

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
