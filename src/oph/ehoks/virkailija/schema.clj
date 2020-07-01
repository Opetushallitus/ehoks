(ns oph.ehoks.virkailija.schema
  (:require [schema.core :as s]))

(s/defschema
  OrganisationPrivilege
  {:oid s/Str
   :privileges #{s/Keyword}
   :roles #{s/Keyword}
   :child-organisations [s/Str]})

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
                 :unindexedOpiskeluoikeudet Long
                 :unindexedTutkinnot Long}
   :hoksit {:amount s/Any}})

(s/defschema
  DeleteConfirmInfo
  {:nimi s/Str
   :hoksId s/Int
   :opiskeluoikeusOid s/Str
   :oppilaitosOid s/Str})
