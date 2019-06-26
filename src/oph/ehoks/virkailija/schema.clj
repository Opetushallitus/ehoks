(ns oph.ehoks.virkailija.schema
  (:require [schema.core :as s]))

(s/defschema
  OrganisationPrivilege
  {:oid s/Str
   :privileges #{s/Keyword}
   :roles #{s/Keyword}})

(s/defschema
  VirkailijaSession
  {:oidHenkilo s/Str
   :organisation-privileges [OrganisationPrivilege]})

(s/defschema
  SystemInfo
  {:cache {:size s/Int}
   :memory {:total Long
            :free Long
            :max Long}
   :oppijaindex {:unindexedOppijat Long
                 :unindexedOpiskeluoikeudet Long}})