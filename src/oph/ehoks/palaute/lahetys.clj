(ns oph.ehoks.palaute.lahetys
  (:require [oph.ehoks.palaute.viestit :as v]
            [oph.ehoks.external.viestinvalityspalvelu :as vvp]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]))

(defn send-palaute-initial-email!
  "Lähettää viestin, jossa kutsutaan vastaamaan Arvossa olevaan kyselyyn."
  [{:keys [existing-palaute hoks suoritus] :as ctx}]
  (let [recipient (:sahkoposti hoks)
        kyselytyyppi (:kyselytyyppi existing-palaute)
        kyselylinkki (:kyselylinkki existing-palaute)
        suorituskieli (suoritus/kieli suoritus)
        email-body (v/amispalaute-html {:suorituskieli suorituskieli
                                        :kyselytyyppi kyselytyyppi
                                        :kyselylinkki kyselylinkki})
        email-subject (v/palaute-message-subject ctx)]
    (vvp/send-message! recipient email-subject email-body)))
