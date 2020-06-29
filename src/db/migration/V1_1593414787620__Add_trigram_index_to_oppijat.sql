CREATE INDEX oppijat_nimi_idx ON oppijat USING GIN(nimi gin_trgm_ops);
