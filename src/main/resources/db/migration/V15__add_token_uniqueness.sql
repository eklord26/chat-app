-- Migration V15: Keep auth tokens unique for reliable bearer-token lookup

CREATE UNIQUE INDEX IF NOT EXISTS idx_tokens_auth_token_unique
    ON tokens (auth_token)
    WHERE deleted_at IS NULL;

ALTER TABLE tokens
    DROP COLUMN IF EXISTS encrypt_token;
