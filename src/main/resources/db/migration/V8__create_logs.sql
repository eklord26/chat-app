-- Migration V8: Create logs table
-- Entity: LogTable (Logger/DAO/LogDAO.kt)

CREATE TABLE IF NOT EXISTS logs
(
    id          SERIAL PRIMARY KEY,
    log_type    VARCHAR(50)              NOT NULL,
    event       VARCHAR(255)             NOT NULL,
    id_user     INTEGER                  NOT NULL,
    date        TIMESTAMP                NOT NULL DEFAULT NOW(),
    life_time   INTEGER                  NOT NULL,
    id_address  VARCHAR(50)              NOT NULL,
    description TEXT                     NOT NULL,

    CONSTRAINT fk_logs_user
        FOREIGN KEY (id_user)
            REFERENCES users (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_logs_id_user ON logs (id_user);
CREATE INDEX IF NOT EXISTS idx_logs_date ON logs (date);
