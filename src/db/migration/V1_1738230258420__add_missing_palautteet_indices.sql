CREATE INDEX CONCURRENTLY IF NOT EXISTS palautteet_nippu_id_key
ON palautteet(nippu_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS palautteet_hoks_id_key
ON palautteet(hoks_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS palaute_tapahtumat_palaute_id_key
ON palaute_tapahtumat(palaute_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS palaute_viestit_palaute_id_key
ON palaute_viestit(palaute_id);
