(ns oph.ehoks.palaute.tyoelama.nippu
  (:require [oph.ehoks.utils :as utils]))

(defn tunniste
  [ctx tep-palaute]
  {:pre [(or (:vastuullinen-tyopaikka-ohjaaja-nimi tep-palaute)
             (:ohjaaja-nimi tep-palaute))
         (or (:tyopaikan-y-tunnus tep-palaute)
             (:tyopaikan-ytunnus tep-palaute))
         (:koulutustoimija ctx)
         (get-in (:suoritus ctx) [:koulutusmoduuli :tunniste :koodiarvo])]}
  (format "%s/%s/%s/%s"
          (or (:vastuullinen-tyopaikka-ohjaaja-nimi tep-palaute)
              (:ohjaaja-nimi tep-palaute))
          (or (:tyopaikan-y-tunnus tep-palaute)
              (:tyopaikan-ytunnus tep-palaute))
          (:koulutustoimija ctx)
          (get-in (:suoritus ctx) [:koulutusmoduuli :tunniste :koodiarvo])))

(defn build-tpo-nippu-for-heratepalvelu
  [{:keys [existing-palaute suoritus koulutustoimija niputuspvm] :as ctx}]
  {:pre [(:tyopaikan-nimi existing-palaute)]}
  (let [tutkinto (get-in suoritus [:koulutusmoduuli :tunniste :koodiarvo])
        tyopaikan-nimi     (:tyopaikan-nimi existing-palaute)
        tyopaikan-y-tunnus (:tyopaikan-y-tunnus existing-palaute)
        ohjaaja            (:vastuullinen-tyopaikka-ohjaaja-nimi
                             existing-palaute)]
    (-> {:ohjaaja-ytunnus-kj-tutkinto (tunniste ctx existing-palaute)
         :ohjaaja                     ohjaaja
         :tyopaikka                   tyopaikan-nimi
         :ytunnus                     tyopaikan-y-tunnus
         :koulutuksenjarjestaja       koulutustoimija
         :tutkinto                    tutkinto
         :kasittelytila               "ei_niputettu"
         :sms-kasittelytila           "ei_lahetetty"
         :niputuspvm                  niputuspvm}
        (utils/remove-nils)
        (utils/to-underscore-keys))))
