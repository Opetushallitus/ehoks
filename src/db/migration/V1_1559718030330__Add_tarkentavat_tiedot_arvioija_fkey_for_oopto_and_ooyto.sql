ALTER TABLE olemassa_olevat_paikalliset_tutkinnon_osat
  ADD COLUMN tarkentavat_tiedot_arvioija_id integer,
  ADD CONSTRAINT olemassa_olevat_paikalliset__tarkentavat_tiedot_arvioija__fkey
    FOREIGN KEY (tarkentavat_tiedot_arvioija_id)
      REFERENCES todennettu_arviointi_lisatiedot (id);

ALTER TABLE olemassa_olevat_yhteiset_tutkinnon_osat
  ADD COLUMN tarkentavat_tiedot_arvioija_id integer,
  ADD CONSTRAINT olemassa_olevat_yhteiset__tarkentavat_tiedot_arvioija__fkey
    FOREIGN KEY (tarkentavat_tiedot_arvioija_id)
      REFERENCES todennettu_arviointi_lisatiedot (id);
