ALTER TABLE aiemmin_hankitut_yto_osa_alueet
    ADD COLUMN tarkentavat_tiedot_arvioija_id integer,
    ADD CONSTRAINT ahyto_osa_alue__tarkentavat_tiedot_arvioija__fkey
        FOREIGN KEY (tarkentavat_tiedot_arvioija_id)
            REFERENCES todennettu_arviointi_lisatiedot (id);
