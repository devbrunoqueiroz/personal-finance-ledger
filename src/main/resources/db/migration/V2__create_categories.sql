CREATE TABLE IF NOT EXISTS categories (
    category_id  UUID PRIMARY KEY,
    owner_id     UUID NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  TEXT NULL,
    type         VARCHAR(50) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_categories_owner
        FOREIGN KEY (owner_id) REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_categories_type
        CHECK (type IN ('INCOME', 'EXPENSE'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_categories_owner_name_type
    ON categories (owner_id, name, type);

CREATE INDEX IF NOT EXISTS ix_categories_owner_id ON categories (owner_id);