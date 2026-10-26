-- V3__create_file_uploads.sql
-- Creates the file_uploads table for file upload tracking.
--
-- Morphological table: each row belongs to one product (product_id) and was
-- uploaded by one user (user_id), allowing a product to have many images/files.

CREATE TABLE file_uploads (
    id            BIGSERIAL      PRIMARY KEY,
    original_name VARCHAR(255)   NOT NULL,
    stored_name   VARCHAR(255)   NOT NULL UNIQUE,
    content_type  VARCHAR(100)   NOT NULL,
    size_bytes    BIGINT         NOT NULL,
    path          VARCHAR(500)   NOT NULL,
    product_id    BIGINT         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id       BIGINT         NOT NULL REFERENCES users(id),
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_file_uploads_product_id ON file_uploads (product_id);
CREATE INDEX idx_file_uploads_user_id    ON file_uploads (user_id);
