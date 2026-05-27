ALTER TABLE user_preferences
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
