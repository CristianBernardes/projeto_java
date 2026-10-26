-- V2__create_users.sql
-- Creates the users table for JWT authentication.

CREATE TABLE users (
    id            BIGSERIAL      PRIMARY KEY,
    email         VARCHAR(255)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)   NOT NULL,
    role          VARCHAR(50)    NOT NULL DEFAULT 'USER',
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (email);
