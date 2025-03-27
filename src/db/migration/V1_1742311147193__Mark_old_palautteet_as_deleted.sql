-- This migration might need to be done again when we actually enable
-- Arvo calls and Herätepalvelu sync for palaute-backend.  It depends on
-- the perceived quality of palautteet bookkeeping after its
-- installation.
UPDATE palautteet
SET deleted_at=now()
WHERE deleted_at IS NULL;  -- yep, every palaute
