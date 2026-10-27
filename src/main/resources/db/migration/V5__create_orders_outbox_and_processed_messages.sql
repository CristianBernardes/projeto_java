-- V5__create_orders_outbox_and_processed_messages.sql
-- Creates the asynchronous order processing schema:
--
--   - orders / order_items: the order aggregate. Stock is NOT debited synchronously;
--     it is reserved asynchronously by a RabbitMQ consumer after the order is persisted.
--   - outbox_events: Transactional Outbox pattern — the domain event is written in the
--     SAME transaction as the order, avoiding the dual-write problem (DB commit succeeds
--     but the message broker publish fails, or vice-versa). A poller reads this table and
--     publishes to RabbitMQ/Kafka, then marks the row as published.
--   - processed_messages: idempotent consumer guard. RabbitMQ/Kafka give at-least-once
--     delivery, so a message can be redelivered; this table records processed message IDs
--     so a redelivered message is a no-op instead of debiting stock twice.

CREATE TABLE orders (
    id              BIGSERIAL      PRIMARY KEY,
    user_id         BIGINT         NOT NULL REFERENCES users(id),
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_amount    NUMERIC(10, 2) NOT NULL,
    delivery_street VARCHAR(255)   NOT NULL,
    delivery_city   VARCHAR(100)   NOT NULL,
    delivery_state  VARCHAR(2)     NOT NULL,
    delivery_zip    VARCHAR(20)    NOT NULL,
    failure_reason  VARCHAR(500),
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status  ON orders (status);

CREATE TABLE order_items (
    id         BIGSERIAL      PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES products(id),
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id   ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);

CREATE TABLE outbox_events (
    id             BIGSERIAL      PRIMARY KEY,
    aggregate_type VARCHAR(50)    NOT NULL,
    aggregate_id   BIGINT         NOT NULL,
    event_type     VARCHAR(100)   NOT NULL,
    payload        TEXT           NOT NULL,
    published      BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMP
);

CREATE INDEX idx_outbox_events_unpublished ON outbox_events (published, created_at)
    WHERE published = FALSE;

CREATE TABLE processed_messages (
    message_id   VARCHAR(100) PRIMARY KEY,
    processed_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
