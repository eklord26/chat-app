-- Migration V13: Drop duplicate login index created on top of the UNIQUE constraint
-- Entity: UserTable (Users/DAO/UserDAO.kt)

DROP INDEX IF EXISTS idx_users_login;
