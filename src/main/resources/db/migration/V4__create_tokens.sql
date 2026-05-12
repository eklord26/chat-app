-- Migration V4: Create tokens table
-- Entity: TokenTable (Tokens/DAO/TokenDAO.kt)

CREATE TABLE IF NOT EXISTS tokens
(
    id             SERIAL PRIMARY KEY,
    auth_token     TEXT                     NOT NULL,
    encrypt_token  TEXT                     NOT NULL,
    date_expire    TIMESTAMP                NOT NULL,
    active         BOOLEAN                  NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_tokens_auth_token ON tokens (auth_token);
