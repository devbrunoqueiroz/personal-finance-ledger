CREATE TABLE IF NOT EXISTS transactions (
    transaction_id UUID PRIMARY KEY,
    owner_id       UUID NOT NULL,
    description    VARCHAR(255) NOT NULL,
    notes          TEXT NULL,
    amount         NUMERIC(19,2) NOT NULL,
    type           VARCHAR(50) NOT NULL,
    status         VARCHAR(50) NOT NULL,
    due_date       DATE NULL,
    settled_at     DATE NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    category_id    UUID NULL,

    CONSTRAINT fk_transactions_owner
        FOREIGN KEY (owner_id) REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE SET NULL,

    CONSTRAINT chk_transactions_type
        CHECK (type IN ('INCOME', 'EXPENSE')),

    CONSTRAINT chk_transactions_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS ix_transactions_owner_id ON transactions (owner_id);
CREATE INDEX IF NOT EXISTS ix_transactions_category_id ON transactions (category_id);
CREATE INDEX IF NOT EXISTS ix_transactions_due_date ON transactions (due_date);