CREATE TABLE IF NOT EXISTS media_files
(
    id                  SERIAL PRIMARY KEY,
    uploader_user_id    INTEGER      NOT NULL,
    original_file_name  TEXT         NOT NULL,
    stored_file_name    TEXT         NOT NULL,
    extension           VARCHAR(32)  NOT NULL,
    mime_type           VARCHAR(128) NOT NULL,
    media_type          VARCHAR(32)  NOT NULL,
    size_bytes          BIGINT       NOT NULL,
    storage_path        TEXT         NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP    NULL,

    CONSTRAINT fk_media_files_uploader
        FOREIGN KEY (uploader_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_media_files_uploader
    ON media_files (uploader_user_id);

CREATE INDEX IF NOT EXISTS idx_media_files_deleted_at
    ON media_files (deleted_at);

CREATE TABLE IF NOT EXISTS message_attachments
(
    id             SERIAL PRIMARY KEY,
    id_message     INTEGER   NOT NULL,
    id_media_file  INTEGER   NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_message_attachments_message
        FOREIGN KEY (id_message)
            REFERENCES messages (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_message_attachments_media_file
        FOREIGN KEY (id_media_file)
            REFERENCES media_files (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_message_attachments_message
    ON message_attachments (id_message);

CREATE INDEX IF NOT EXISTS idx_message_attachments_media_file
    ON message_attachments (id_media_file);
