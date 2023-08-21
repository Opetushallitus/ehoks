ALTER TABLE opiskeluoikeudet
	ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	ADD COLUMN alkamispaiva DATE,
	ADD COLUMN arvioitu_paattymispaiva DATE;
