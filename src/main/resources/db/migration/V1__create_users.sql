-- V1__create_users.sql
-- PostgreSQL

CREATE TABLE IF NOT EXISTS users (
    user_id       UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    updated_by    UUID NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    status        VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (email);

CREATE INDEX IF NOT EXISTS ix_users_updated_by ON users (updated_by);

-- Roles (ElementCollection)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role    VARCHAR(50) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_user_roles_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS ix_user_roles_role ON user_roles (role);