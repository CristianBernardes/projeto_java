-- V4__make_file_uploads_nullable_and_add_user_avatar.sql
--
-- Adjusts the morphological file_uploads table:
--   - product_id becomes nullable: a file can exist without a product
--   - user_id becomes nullable: a file can exist without an owner
--
-- Adds avatar support to users:
--   - users.avatar_id (FK → file_uploads) stores the user's active profile photo
--   - A user has at most one active avatar; history is preserved in file_uploads

ALTER TABLE file_uploads
    ALTER COLUMN product_id DROP NOT NULL,
    ALTER COLUMN user_id    DROP NOT NULL;

ALTER TABLE users
    ADD COLUMN avatar_id BIGINT REFERENCES file_uploads(id) ON DELETE SET NULL;

CREATE INDEX idx_users_avatar_id ON users (avatar_id);
