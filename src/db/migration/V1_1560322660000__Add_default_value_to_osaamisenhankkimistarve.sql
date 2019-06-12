ALTER TABLE hoksit DROP COLUMN osaamisen_hankkimisen_tarve;

ALTER TABLE hoksit ADD COLUMN osaamisen_hankkimisen_tarve BOOLEAN NOT NULL
DEFAULT FALSE;
