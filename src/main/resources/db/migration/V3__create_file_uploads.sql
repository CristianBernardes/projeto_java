-- V3__create_file_uploads.sql
-- Creates the file_uploads table for file upload tracking.

CREATE TABLE file_uploads (
    id            BIGSERIAL      PRIMARY KEY,
    original_name VARCHAR(255)   NOT NULL,
    stored_name   VARCHAR(255)   NOT NULL UNIQUE,
    content_type  VARCHAR(100)   NOT NULL,
    size_bytes    BIGINT         NOT NULL,
    path          VARCHAR(500)   NOT NULL,
    uploaded_by   BIGINT         REFERENCES users(id),
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);
