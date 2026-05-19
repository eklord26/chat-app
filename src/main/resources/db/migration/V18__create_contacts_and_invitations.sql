-- Migration V18: Create contacts and invitation tables
-- Entities:
--   ContactTable (Contacts/DAO/ContactDAO.kt)
--   ContactInvitationTable (Invitations/DAO/ContactInvitationDAO.kt)
--   ChatInvitationTable (Invitations/DAO/ChatInvitationDAO.kt)

CREATE TABLE IF NOT EXISTS contacts
(
    id              SERIAL PRIMARY KEY,
    owner_user_id   INTEGER                  NOT NULL,
    contact_user_id INTEGER                  NOT NULL,
    display_name    TEXT                     NULL,
    created_at      TIMESTAMP                NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP                NULL,

    CONSTRAINT fk_contacts_owner_user
        FOREIGN KEY (owner_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT fk_contacts_contact_user
        FOREIGN KEY (contact_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT chk_contacts_not_self
        CHECK (owner_user_id <> contact_user_id)
);

CREATE INDEX IF NOT EXISTS idx_contacts_owner_user_id ON contacts (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_contacts_contact_user_id ON contacts (contact_user_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_contacts_unique_active
    ON contacts (owner_user_id, contact_user_id)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS contact_invitations
(
    id               SERIAL PRIMARY KEY,
    sender_user_id   INTEGER                  NOT NULL,
    receiver_user_id INTEGER                  NOT NULL,
    status           TEXT                     NOT NULL DEFAULT 'pending',
    message          TEXT                     NULL,
    created_at       TIMESTAMP                NOT NULL DEFAULT NOW(),
    responded_at     TIMESTAMP                NULL,
    deleted_at       TIMESTAMP                NULL,

    CONSTRAINT fk_contact_invitations_sender_user
        FOREIGN KEY (sender_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT fk_contact_invitations_receiver_user
        FOREIGN KEY (receiver_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT chk_contact_invitations_not_self
        CHECK (sender_user_id <> receiver_user_id),

    CONSTRAINT chk_contact_invitations_status
        CHECK (status IN ('pending', 'accepted', 'rejected', 'cancelled'))
);

CREATE INDEX IF NOT EXISTS idx_contact_invitations_sender_user_id ON contact_invitations (sender_user_id);
CREATE INDEX IF NOT EXISTS idx_contact_invitations_receiver_user_id ON contact_invitations (receiver_user_id);
CREATE INDEX IF NOT EXISTS idx_contact_invitations_status ON contact_invitations (status);

CREATE UNIQUE INDEX IF NOT EXISTS idx_contact_invitations_unique_pending
    ON contact_invitations (sender_user_id, receiver_user_id)
    WHERE status = 'pending' AND deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS chat_invitations
(
    id              SERIAL PRIMARY KEY,
    id_chat         INTEGER                  NOT NULL,
    inviter_user_id INTEGER                  NOT NULL,
    invitee_user_id INTEGER                  NOT NULL,
    id_role         INTEGER                  NOT NULL,
    status          TEXT                     NOT NULL DEFAULT 'pending',
    message         TEXT                     NULL,
    created_at      TIMESTAMP                NOT NULL DEFAULT NOW(),
    responded_at    TIMESTAMP                NULL,
    deleted_at      TIMESTAMP                NULL,

    CONSTRAINT fk_chat_invitations_chat
        FOREIGN KEY (id_chat)
            REFERENCES chats (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT fk_chat_invitations_inviter_user
        FOREIGN KEY (inviter_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT fk_chat_invitations_invitee_user
        FOREIGN KEY (invitee_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT fk_chat_invitations_role
        FOREIGN KEY (id_role)
            REFERENCES roles (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT chk_chat_invitations_not_self
        CHECK (inviter_user_id <> invitee_user_id),

    CONSTRAINT chk_chat_invitations_status
        CHECK (status IN ('pending', 'accepted', 'rejected', 'cancelled'))
);

CREATE INDEX IF NOT EXISTS idx_chat_invitations_id_chat ON chat_invitations (id_chat);
CREATE INDEX IF NOT EXISTS idx_chat_invitations_inviter_user_id ON chat_invitations (inviter_user_id);
CREATE INDEX IF NOT EXISTS idx_chat_invitations_invitee_user_id ON chat_invitations (invitee_user_id);
CREATE INDEX IF NOT EXISTS idx_chat_invitations_status ON chat_invitations (status);

CREATE UNIQUE INDEX IF NOT EXISTS idx_chat_invitations_unique_pending
    ON chat_invitations (id_chat, invitee_user_id)
    WHERE status = 'pending' AND deleted_at IS NULL;
