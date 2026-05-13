-- Migration V16: Store user personal data encrypted at rest

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_cipher_text TEXT NULL,
    ADD COLUMN IF NOT EXISTS email_nonce TEXT NULL,
    ADD COLUMN IF NOT EXISTS phone_cipher_text TEXT NULL,
    ADD COLUMN IF NOT EXISTS phone_nonce TEXT NULL,
    ADD COLUMN IF NOT EXISTS fio_cipher_text TEXT NULL,
    ADD COLUMN IF NOT EXISTS fio_nonce TEXT NULL;
