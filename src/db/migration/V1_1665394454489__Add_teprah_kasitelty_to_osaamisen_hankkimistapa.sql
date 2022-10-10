ALTER TABLE osaamisen_hankkimistavat
  ADD COLUMN teprah_kasitelty BOOLEAN DEFAULT false;

UPDATE osaamisen_hankkimistavat SET teprah_kasitelty = false;
