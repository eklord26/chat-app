-- Migration V7: Create messages table
-- Entity: MessageTable (Messages/DAO/MessageDAO.kt)

CREATE TABLE IF NOT EXISTS messages
(
    id             SERIAL PRIMARY KEY,
    id_chat_member INTEGER                  NOT NULL,
    value          TEXT                     NOT NULL,
    type           TEXT                     NOT NULL,
    created_at     TIMESTAMP                NOT NULL DEFAULT NOW(),
    viewed_at      TIMESTAMP                NULL,
    deleted_at     TIMESTAMP                NULL,

    CONSTRAINT fk_messages_chat_member
        FOREIGN KEY (id_chat_member)
            REFERENCES chat_members (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_messages_id_chat_member ON messages (id_chat_member);