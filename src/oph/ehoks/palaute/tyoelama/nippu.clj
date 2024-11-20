(ns oph.ehoks.palaute.tyoelama.nippu
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.utils :as utils]))

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
  [{:keys [suoritus koulutustoimija niputuspvm] :as ctx}
   tep-palaute keskeytymisajanjaksot]
  {:pre [(:tyopaikan-nimi tep-palaute)]}
  (let [tutkinto (get-in suoritus [:koulutusmoduuli :tunniste :koodiarvo])
        tyopaikan-nimi     (:tyopaikan-nimi tep-palaute)
        tyopaikan-y-tunnus (:tyopaikan-y-tunnus tep-palaute)
        ohjaaja            (:vastuullinen-tyopaikka-ohjaaja-nimi tep-palaute)
        nippu-data {:ohjaaja-ytunnus-kj-tutkinto (tunniste ctx tep-palaute)
                    :ohjaaja                     ohjaaja
                    :tyopaikka                   tyopaikan-nimi
                    :ytunnus                     tyopaikan-y-tunnus
                    :koulutuksenjarjestaja       koulutustoimija
                    :tutkinto                    tutkinto
                    :kasittelytila               "ei_niputettu"
                    :sms-kasittelytila           "ei_lahetetty"
                    :niputuspvm                  niputuspvm}]
    (utils/to-underscore-keys
      (utils/remove-nils
        (if (not-every? #(some? (:loppu %)) keskeytymisajanjaksot)
          (do (log/info "Jakso on keskeytynyt, tätä ei niputeta.")
              (assoc nippu-data
                     :kasittelytila     "ei_niputeta"
                     :sms-kasittelytila "ei_niputeta"))
          nippu-data)))))
