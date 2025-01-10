SELECT * FROM hoksit h
    WHERE h.ensikertainen_hyvaksyminen >= ?
    AND h.ensikertainen_hyvaksyminen <= ?
    AND h.deleted_at IS NULL
    AND h.tuva_opiskeluoikeus_oid IS NULL
    AND NOT EXISTS (SELECT 1 FROM hankittavat_koulutuksen_osat hko
                        WHERE hko.deleted_at IS NULL
                        AND hko.hoks_id = h.id)
