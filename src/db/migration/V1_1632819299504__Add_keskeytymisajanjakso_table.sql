CREATE TABLE keskeytymisajanjaksot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  osaamisen_hankkimistapa_id INTEGER REFERENCES osaamisen_hankkimistavat(id) ON DELETE CASCADE,
  alku DATE,
  loppu DATE
);
