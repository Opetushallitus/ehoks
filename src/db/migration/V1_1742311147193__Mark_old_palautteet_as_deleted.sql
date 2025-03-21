UPDATE palautteet
SET deleted_at=now()
WHERE deleted_at IS NULL;  -- yep, every palaute
