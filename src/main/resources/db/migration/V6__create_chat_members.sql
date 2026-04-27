-- Migration V6: Create chat_members table
-- Entity: ChatMembersTable (ChatMembers/DAO/ChatMemberDAO.kt)

CREATE TABLE IF NOT EXISTS chat_members
(
    id         SERIAL PRIMARY KEY,
    id_chat    INTEGER                  NOT NULL,
    id_role    INTEGER                  NOT NULL,
    id_user    INTEGER                  NOT NULL,
    created_at TIMESTAMP                NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP                NULL,

    CONSTRAINT fk_chat_members_chat
        FOREIGN KEY (id_chat)
            REFERENCES chats (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT fk_chat_members_role
        FOREIGN KEY (id_role)
            REFERENCES roles (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT fk_chat_members_user
        FOREIGN KEY (id_user)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_members_id_chat ON chat_members (id_chat);
CREATE INDEX IF NOT EXISTS idx_chat_members_id_user ON chat_members (id_user);
CREATE INDEX IF NOT EXISTS idx_chat_members_id_role ON chat_members (id_role);

-- Пользователь может быть в чате только один раз (среди активных участников)
CREATE UNIQUE INDEX IF NOT EXISTS idx_chat_members_unique_active
    ON chat_members (id_chat, id_user)
    WHERE deleted_at IS NULL;
