-- Migration V1: Create users table
-- Entity: UserTable (Users/DAO/UserDAO.kt)

CREATE TABLE IF NOT EXISTS users
(
    id            SERIAL PRIMARY KEY,
    name          TEXT                     NOT NULL,
    login         VARCHAR(255)             NOT NULL UNIQUE,
    password_hash VARCHAR(65)              NOT NULL,
    is_admin      BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at   TIMESTAMP                NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_login ON users (login);
