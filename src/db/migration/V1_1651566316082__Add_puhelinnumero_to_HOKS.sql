ALTER TABLE hoksit ADD COLUMN puhelinnumero VARCHAR(256);

ALTER TABLE kyselylinkit ADD COLUMN puhelinnumero VARCHAR(256);
ALTER TABLE kyselylinkit ADD COLUMN sms_lahetyspvm DATE;
ALTER TABLE kyselylinkit ADD COLUMN sms_lahetystila VARCHAR(256);
