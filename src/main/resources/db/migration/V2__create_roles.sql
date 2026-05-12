-- Migration V2: Create roles table
-- Entity: RoleTable (Roles/DAO/RoleDAO.kt)

CREATE TABLE IF NOT EXISTS roles
(
    id         SERIAL PRIMARY KEY,
    name       TEXT                     NOT NULL,
    deleted_at TIMESTAMP                NULL
);
