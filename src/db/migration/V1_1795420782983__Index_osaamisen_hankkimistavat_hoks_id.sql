CREATE INDEX osaamisen_hankkimistavat_hoks_id_idx
    ON osaamisen_hankkimistavat (hoks_id)
    WHERE hoks_id IS NOT NULL;
