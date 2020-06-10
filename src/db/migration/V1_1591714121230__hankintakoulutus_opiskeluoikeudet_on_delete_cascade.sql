ALTER TABLE opiskeluoikeudet
    ADD UNIQUE (oid);

ALTER TABLE opiskeluoikeudet
    ADD CONSTRAINT opiskeluoikeudet_hankintakoulutus_opiskeluoikeus_oid_fkey
        FOREIGN KEY (hankintakoulutus_opiskeluoikeus_oid)
            REFERENCES opiskeluoikeudet(oid)
            ON DELETE CASCADE;
