CREATE TABLE amisherate_kasittelytilat (
  id SERIAL PRIMARY KEY,
  hoks_id SERIAL REFERENCES hoksit(id) ON DELETE CASCADE,
  aloitusherate_kasitelty BOOLEAN DEFAULT false,
  paattoherate_kasitelty BOOLEAN DEFAULT false
);
