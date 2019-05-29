ALTER TABLE hoksit ADD COLUMN osaamisen_hankkimisen_tarve BOOLEAN;

ALTER TABLE muut_oppimisymparistot DROP COLUMN lisatiedot;

ALTER TABLE osaamisen_osoittamiset ADD COLUMN
vaatimuksista_tai_tavoitteista_poikkeaminen TEXT;
