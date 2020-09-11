ALTER TABLE shared_modules
    ADD COLUMN hoks_eid VARCHAR(36) NOT NULL,
    ADD CONSTRAINT shared_modules__hoksit__fkey
        FOREIGN KEY (hoks_eid) REFERENCES hoksit (eid);
