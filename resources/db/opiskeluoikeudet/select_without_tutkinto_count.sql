SELECT COUNT(*) FROM opiskeluoikeudet WHERE tutkinto_nimi->>'fi' = '' OR tutkinto_nimi->>'fi' IS NULL
