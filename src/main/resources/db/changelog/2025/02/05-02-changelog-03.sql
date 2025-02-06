ALTER TABLE client
    ADD COLUMN client_id VARCHAR(255);

UPDATE client
SET client_id = CONCAT('CLIENT_', id);

ALTER TABLE client
    ALTER COLUMN client_id SET NOT NULL,
    ADD UNIQUE (client_id);