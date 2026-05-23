-- Migration V19: Seed base chat roles
-- These roles are used by chat membership automation.

INSERT INTO roles (name, deleted_at)
SELECT 'Участник', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'Участник' AND deleted_at IS NULL
);

INSERT INTO roles (name, deleted_at)
SELECT 'Модератор', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'Модератор' AND deleted_at IS NULL
);

INSERT INTO roles (name, deleted_at)
SELECT 'Администратор', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'Администратор' AND deleted_at IS NULL
);
