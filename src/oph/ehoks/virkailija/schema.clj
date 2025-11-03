(ns oph.ehoks.virkailija.schema
  (:require [schema.core :as s]
            [oph.ehoks.external.schema :as exs]))

(s/defschema
  OrganisationPrivilege
  "Organisaatio-oikeuden schema."
  {:oid s/Str
   :privileges #{s/Keyword}
   :roles #{s/Keyword}})

(s/defschema
  VirkailijaSession
  "Virkailijan session schema."
  {:oidHenkilo s/Str
   :organisation-privileges [OrganisationPrivilege]
   :isSuperuser s/Bool})

(s/defschema
  SystemInfoCache
  "Järjestelmätietojen schema: Cache."
  {:size s/Int})

(s/defschema
  SystemInfoMemory
  "Järjestelmätietojen schema: Muisti."
  {:total Long
   :free Long
   :max Long})

(s/defschema
  SystemInfoOppijaindex
  "Järjestelmätietojen schema: Oppijaindex."
  {:unindexedOppijat Long
   :unindexedOpiskeluoikeudet Long
   :unindexedTutkinnot Long})

(s/defschema
  SystemInfoHoksit
  "Järjestelmätietojen schema: Hoksit."
  {:amount s/Any})

(s/defschema
  DeleteConfirmInfo
  "Poistovahvistuksen tietojen schema."
  {:nimi (s/maybe s/Str)
   :hoksId s/Int
   :oppilaitosNimi exs/Nimi
   :tutkinnonNimi exs/Nimi
   :opiskeluoikeusOid s/Str
   :oppilaitosOid (s/maybe s/Str)})

(s/defschema
  UpdateOppija
  "Oppijan päivityksen schema."
  {:oppija-oid s/Str})

(s/defschema
  UpdateOpiskeluoikeus
  "Opiskeluoikeuden päivityksen schema."
  {:opiskeluoikeus-oid s/Str})

(s/defschema
  UpdateOpiskeluoikeudet
  "Opiskeluoikeuksien päivityksen schema."
  {:koulutustoimija-oid s/Str})
