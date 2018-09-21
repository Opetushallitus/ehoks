(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]))

(s/defschema OsaamisenTyyppi
             "Osaamisen tyyppi"
             (s/enum
               :valmentava :valmistava :syventava :olemassaoleva
               :muu-todennettu :muu))

(s/defschema Osaamisala
             "Osaamisala koodistosta"
             {:versio s/Int
              :uri s/Str})

(s/defschema Osaaminen
             "HOKSin olemassa oleva osaaminen"
             {:tyyppi OsaamisenTyyppi
              :hoks-id s/Int
              :perusteet-diaarinumero s/Str
              :osaamisala Osaamisala
              :suorituspvm s/Inst
              :todentaja s/Str
              (s/optional-key :liitteet) [s/Str]})

(s/defschema SuunniteltuOsaaminen
             "HOKSin puuttuvan osaamisen hankkimisen suunnitelma"
             {:tyyppi OsaamisenTyyppi
              :hoks-id s/Int
              :perusteet-diaarinumero s/Str
              :osaamisala Osaamisala
              :suoritustapa (s/enum
                              :lahiopetus :verkko-oppimisymparisto
                              :monimuoto :tyopaikalla)
              :sisalto s/Str
              :alku s/Inst
              :loppu s/Inst
              :organisaatio s/Str
              :keskeiset-tehtavat [s/Str]
              :ohjaus-ja-tuki s/Bool
              :erityinen-tuki s/Bool})

(s/defschema HOKSArvot
             "HOKS arvot uuden HOKSin luomiseen"
             {:oppijan-oid s/Str
              :urasuunnitelma s/Str
              :tutkintotavoite s/Str
              :tutkinnon-perusteet-diaarinumero s/Str
              :osaamisala Osaamisala
              :opiskeluoikeus-alkupvm s/Inst
              :opiskeluoikeus-paattymispvm s/Inst})

(s/defschema HOKS
             "HOKS"
             (merge
               HOKSArvot
               {:id s/Int
                :versio s/Int
                :luojan-oid s/Str
                :paivittajan-oid s/Str
                :luonnin-hyvaksyjan-oid s/Str
                :paivityksen-hyvaksyjan-oid s/Str
                :luotu s/Inst
                :hyvaksytty s/Inst
                :paivitetty s/Inst
                :osaamiset [Osaaminen]
                :koulutukset [Osaaminen]
                :suunnitellut-osaamiset [SuunniteltuOsaaminen]}))
