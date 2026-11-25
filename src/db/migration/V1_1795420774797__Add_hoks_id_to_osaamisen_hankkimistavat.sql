ALTER TABLE osaamisen_hankkimistavat
    ADD COLUMN hoks_id INTEGER;

ALTER TABLE osaamisen_hankkimistavat
    ADD CONSTRAINT osaamisen_hankkimistavat_hoks_id_fkey
    FOREIGN KEY (hoks_id) REFERENCES hoksit (id)
    ON DELETE SET NULL
    NOT VALID;
