(ns oph.ehoks.virkailija.schema
  (:require [schema.core :as s]
            [oph.ehoks.external.schema :as exs]))

(s/defschema
  OrganisationPrivilege
  {:oid s/Str
   :privileges #{s/Keyword}
   :roles #{s/Keyword}
   :child-organisations [s/Str]})

(s/defschema
  VirkailijaSession
  {:oidHenkilo s/Str
   :organisation-privileges [OrganisationPrivilege]
   :isSuperuser s/Bool})

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
  {:nimi (s/maybe s/Str)
   :hoksId s/Int
   :oppilaitosNimi exs/Nimi
   :tutkinnonNimi exs/Nimi
   :opiskeluoikeusOid s/Str
   :oppilaitosOid (s/maybe s/Str)})

(s/defschema
  UpdateOppija
  {:oppija-oid s/Str})

(s/defschema
  UpdateOpiskeluoikeus
  {:opiskeluoikeus-oid s/Str})

(s/defschema
  UpdateOpiskeluoikeudet
  {:koulutustoimija-oid s/Str})
