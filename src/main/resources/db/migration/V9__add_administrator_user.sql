-- Migration V9: Create admin user
-- Entity: UserTable (Users/DAO/UserDAO.kt)

INSERT INTO users (name, login, password_hash, is_admin) values
(
    'Администратор',
    'admin',

)