CREATE TABLE balance_file
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_name       TEXT        NOT NULL,
    file_hash       VARCHAR(64) NOT NULL,
    file_size_bytes BIGINT      NOT NULL CHECK (file_size_bytes >= 0),
    status          VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at    TIMESTAMPTZ,

    CONSTRAINT uq_balance_file_hash UNIQUE (file_hash),
    CONSTRAINT ck_balance_file_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE balance_record
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_id      BIGINT         NOT NULL REFERENCES balance_file (id),
    line_number  INTEGER        NOT NULL CHECK (line_number > 0),
    bank         VARCHAR(20)    NOT NULL,
    agency       VARCHAR(20)    NOT NULL,
    currency     VARCHAR(3)     NOT NULL,
    balance_date DATE           NOT NULL,
    amount       NUMERIC(19, 2) NOT NULL,
    raw_line     TEXT           NOT NULL,

    CONSTRAINT uq_balance_record_file_line
        UNIQUE (file_id, line_number)
);