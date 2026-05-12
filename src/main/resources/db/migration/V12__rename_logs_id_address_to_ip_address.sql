-- Migration V12: Rename logs address column to match its meaning
-- Entity: LogTable (Logger/DAO/LogDAO.kt)

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'logs'
          AND column_name = 'id_address'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'logs'
          AND column_name = 'ip_address'
    ) THEN
        ALTER TABLE logs RENAME COLUMN id_address TO ip_address;
    END IF;
END $$;
