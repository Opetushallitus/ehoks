(ns oph.ehoks.palaute.tyoelama.nippu
  (:require [oph.ehoks.utils :as utils]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]))

(defn build-tpo-nippu-for-heratepalvelu
  [{:keys [jakso suoritus koulutustoimija niputuspvm] :as ctx}]
  {:pre [(:tyopaikalla-jarjestettava-koulutus jakso)]}
  (let [tutkinto           (suoritus/tutkintotunnus suoritus)
        tjk                (:tyopaikalla-jarjestettava-koulutus jakso)
        tyopaikan-nimi     (:tyopaikan-nimi tjk)
        tyopaikan-y-tunnus (:tyopaikan-y-tunnus tjk)
        ohjaaja            (:nimi (:vastuullinen-tyopaikka-ohjaaja tjk))]
    (-> {:ohjaaja-ytunnus-kj-tutkinto (format "%s/%s/%s/%s"
                                              ohjaaja
                                              tyopaikan-y-tunnus
                                              koulutustoimija
                                              tutkinto)
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

(defn tunniste [ctx]
  (:ohjaaja_ytunnus_kj_tutkinto (build-tpo-nippu-for-heratepalvelu ctx)))
