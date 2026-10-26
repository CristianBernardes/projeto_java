-- V1__create_products.sql
-- Initial migration: creates the products table.

CREATE TABLE products (
    id            BIGSERIAL      PRIMARY KEY,
    name          VARCHAR(255)   NOT NULL,
    description   TEXT,
    price         NUMERIC(10, 2) NOT NULL,
    stock         INTEGER        NOT NULL DEFAULT 0,
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_active ON products (active);
