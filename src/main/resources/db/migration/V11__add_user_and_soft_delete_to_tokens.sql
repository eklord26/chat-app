-- Migration V11: Link tokens to users and add soft-delete marker
-- Entity: TokenTable (Tokens/DAO/TokenDAO.kt)

ALTER TABLE tokens
    ADD COLUMN id_user INTEGER NULL,
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE tokens
    ADD CONSTRAINT fk_tokens_user
        FOREIGN KEY (id_user)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

CREATE INDEX IF NOT EXISTS idx_tokens_id_user ON tokens (id_user);
