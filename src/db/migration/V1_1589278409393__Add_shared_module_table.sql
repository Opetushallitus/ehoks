CREATE TABLE shared_modules (
  id SERIAL PRIMARY KEY,
  share_id UUID NOT NULL DEFAULT gen_random_uuid(),
  tutkinnonosa_module_uuid UUID NOT NULL,
  tutkinnonosa_tyyppi VARCHAR(256) NOT NULL,
  shared_module_uuid UUID NOT NULL,
  shared_module_tyyppi VARCHAR(256) NOT NULL,
  voimassaolo_alku DATE NOT NULL,
  voimassaolo_loppu DATE NOT NULL
)
