ALTER TABLE osaamisen_hankkimistavat
    ADD COLUMN tep_kasitelty BOOLEAN DEFAULT false;

UPDATE osaamisen_hankkimistavat SET tep_kasitelty = false;
