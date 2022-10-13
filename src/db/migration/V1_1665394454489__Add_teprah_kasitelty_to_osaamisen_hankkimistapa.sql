ALTER TABLE osaamisen_hankkimistavat
  ADD COLUMN IF NOT EXISTS teprah_kasitelty BOOLEAN DEFAULT false;
