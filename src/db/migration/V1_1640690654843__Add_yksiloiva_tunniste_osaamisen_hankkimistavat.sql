ALTER TABLE osaamisen_hankkimistavat
  ADD COLUMN yksiloiva_tunniste VARCHAR(256) DEFAULT gen_random_uuid();
