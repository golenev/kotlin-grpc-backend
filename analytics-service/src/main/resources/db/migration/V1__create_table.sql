CREATE TABLE IF NOT EXISTS seller_aggregates (
    seller_id TEXT PRIMARY KEY,
    total_orders BIGINT NOT NULL,
    total_items BIGINT NOT NULL,
    total_revenue NUMERIC(18, 2) NOT NULL,
    avg_check NUMERIC(18, 2) NOT NULL,
    last_order_at TIMESTAMPTZ NOT NULL
);
