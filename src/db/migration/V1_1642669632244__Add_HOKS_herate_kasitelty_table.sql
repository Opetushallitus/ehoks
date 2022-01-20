CREATE TABLE amisherate_kasittelytilat (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hoks_id SERIAL REFERENCES hoksit(id) ON DELETE CASCADE,
  aloitusherate_kasitelty BOOLEAN DEFAULT false,
  paattoherate_kasitelty BOOLEAN DEFAULT false
);
