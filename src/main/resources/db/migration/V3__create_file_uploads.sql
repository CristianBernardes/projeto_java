-- V3__create_file_uploads.sql
-- Creates the file_uploads table for file upload tracking.
--
-- Morphological table:
--   - product_id (nullable): a product can have many files/images; a file can exist without a product
--   - user_id    (nullable): tracks who uploaded the file; a file can exist without an owner
--
-- Avatar support:
--   - users.avatar_id (nullable FK → file_uploads): the user's active profile photo
--   - Changing the avatar just updates this pointer; previous uploads remain as history

CREATE TABLE file_uploads (
    id            BIGSERIAL      PRIMARY KEY,
    original_name VARCHAR(255)   NOT NULL,
    stored_name   VARCHAR(255)   NOT NULL UNIQUE,
    content_type  VARCHAR(100)   NOT NULL,
    size_bytes    BIGINT         NOT NULL,
    path          VARCHAR(500)   NOT NULL,
    product_id    BIGINT         REFERENCES products(id) ON DELETE CASCADE,
    user_id       BIGINT         REFERENCES users(id) ON DELETE SET NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_file_uploads_product_id ON file_uploads (product_id);
CREATE INDEX idx_file_uploads_user_id    ON file_uploads (user_id);

ALTER TABLE users
    ADD COLUMN avatar_id BIGINT REFERENCES file_uploads(id) ON DELETE SET NULL;

CREATE INDEX idx_users_avatar_id ON users (avatar_id);
