ALTER TABLE accounts
    ADD COLUMN account_id VARCHAR(255) NOT NULL UNIQUE,
    ADD COLUMN frozen_amount DOUBLE,
    ADD COLUMN status VARCHAR(20) NOT NULL;

UPDATE accounts SET status = 'OPEN' WHERE status IS NULL;