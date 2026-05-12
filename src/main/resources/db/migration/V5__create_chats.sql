-- Migration V5: Create chats table
-- Entity: ChatTable (Chats/DAO/ChatDAO.kt)

CREATE TABLE IF NOT EXISTS chats
(
    id         SERIAL PRIMARY KEY,
    owner      INTEGER                  NOT NULL,
    name       TEXT                     NOT NULL,
    created_at TIMESTAMP                NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP                NULL,

    CONSTRAINT fk_chats_owner
        FOREIGN KEY (owner)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chats_owner ON chats (owner);
