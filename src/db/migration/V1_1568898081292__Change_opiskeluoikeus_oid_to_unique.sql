ALTER TABLE hoksit ADD UNIQUE (opiskeluoikeus_oid);
ALTER TABLE hoksit ALTER COLUMN opiskeluoikeus_oid SET NOT NULL;