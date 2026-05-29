ALTER TABLE discount_code
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
