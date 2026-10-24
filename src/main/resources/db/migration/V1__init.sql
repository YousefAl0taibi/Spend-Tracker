CREATE TABLE account (
    id          BIGSERIAL PRIMARY KEY,
    label       TEXT NOT NULL,
    currency    CHAR(3) NOT NULL DEFAULT 'SAR'
);

CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    name_en     TEXT NOT NULL UNIQUE,
    name_ar     TEXT NOT NULL
);

CREATE TABLE transaction (
    id                      BIGSERIAL PRIMARY KEY,
    account_id              BIGINT NOT NULL REFERENCES account(id),
    booked_on               DATE NOT NULL,
    raw_description         TEXT NOT NULL,
    normalised_description  TEXT NOT NULL,
    amount                  NUMERIC(14,2) NOT NULL,
    direction               TEXT NOT NULL CHECK (direction IN ('DEBIT','CREDIT')),
    category_id             BIGINT REFERENCES category(id),
    categorised_by          TEXT NOT NULL DEFAULT 'RULE'
                            CHECK (categorised_by IN ('RULE','MANUAL','MODEL')),
    is_internal_transfer    BOOLEAN NOT NULL DEFAULT FALSE,
    fingerprint             CHAR(64) NOT NULL,
    source_file_hash        CHAR(64) NOT NULL,
    ingested_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_transaction_fingerprint UNIQUE (fingerprint)
);

CREATE INDEX idx_transaction_booked_on ON transaction (booked_on);
CREATE INDEX idx_transaction_category  ON transaction (category_id);

INSERT INTO account (label, currency) VALUES ('Main', 'SAR');
