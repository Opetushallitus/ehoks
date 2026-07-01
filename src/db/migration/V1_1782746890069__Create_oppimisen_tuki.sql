CREATE TABLE oppimisen_tuki(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  hoks_id INTEGER REFERENCES hoksit(id),
  oppimisen_tuen_tyyppi_koodi_uri TEXT,
  alku DATE,
  loppu DATE,
  tutkinnon_osan_tyyppi_koodi_uri TEXT
);

CREATE OR REPLACE FUNCTION hoksit_casc_delete_oppimisen_tuki()
RETURNS TRIGGER AS  $$
BEGIN
	UPDATE oppimisen_tuki
	SET	deleted_at = NEW.deleted_at,
		updated_at = coalesce(NEW.deleted_at, current_timestamp)
	WHERE hoks_id = OLD.id
	AND (deleted_at IS NULL OR deleted_at = OLD.deleted_at);
	RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER t_hoksit_casc_delete_oppimisen_tuki
  AFTER UPDATE OF deleted_at ON hoksit
  FOR EACH ROW EXECUTE PROCEDURE hoksit_casc_delete_oppimisen_tuki();

