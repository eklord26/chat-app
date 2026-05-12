-- Migration V10: Normalize users soft-delete column name
-- Entity: UserTable (Users/DAO/UserDAO.kt)

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'deletedAt'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'deleted_at'
    ) THEN
        ALTER TABLE users RENAME COLUMN "deletedAt" TO deleted_at;
    END IF;
END $$;
