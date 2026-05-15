-- Migration V14: Add chat encryption key storage and encrypted message metadata

CREATE TABLE IF NOT EXISTS chat_encryption_keys
(
    id               SERIAL PRIMARY KEY,
    id_chat          INTEGER     NOT NULL,
    key_cipher_text  TEXT        NOT NULL,
    nonce            TEXT        NOT NULL,
    algorithm        VARCHAR(64) NOT NULL,
    version          INTEGER     NOT NULL,
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    rotated_at       TIMESTAMP   NULL,
    revoked_at       TIMESTAMP   NULL,

    CONSTRAINT fk_chat_encryption_keys_chat
        FOREIGN KEY (id_chat)
            REFERENCES chats (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_encryption_keys_id_chat
    ON chat_encryption_keys (id_chat);

CREATE UNIQUE INDEX IF NOT EXISTS idx_chat_encryption_keys_chat_version
    ON chat_encryption_keys (id_chat, version);

CREATE UNIQUE INDEX IF NOT EXISTS idx_chat_encryption_keys_active_chat
    ON chat_encryption_keys (id_chat)
    WHERE revoked_at IS NULL;

ALTER TABLE messages
    ADD COLUMN IF NOT EXISTS is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS encryption_algorithm VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS encryption_key_version INTEGER NULL,
    ADD COLUMN IF NOT EXISTS encryption_nonce TEXT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'chat_app_key_access') THEN
        CREATE ROLE chat_app_key_access;
    END IF;
END
$$;

GRANT SELECT, INSERT, UPDATE ON TABLE chat_encryption_keys TO chat_app_key_access;
GRANT USAGE, SELECT ON SEQUENCE chat_encryption_keys_id_seq TO chat_app_key_access;
