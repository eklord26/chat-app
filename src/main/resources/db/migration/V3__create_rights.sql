-- Migration V3: Create rights table
-- Entity: RightTable (Rights/DAO/RightDAO.kt)

CREATE TABLE IF NOT EXISTS rights
(
    id         SERIAL PRIMARY KEY,
    id_role    INTEGER                  NOT NULL,
    name       TEXT                     NOT NULL,
    deleted_at TIMESTAMP                NULL,

    CONSTRAINT fk_rights_role
        FOREIGN KEY (id_role)
            REFERENCES roles (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_rights_id_role ON rights (id_role);
