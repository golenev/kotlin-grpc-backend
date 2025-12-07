CREATE TABLE IF NOT EXISTS orders (
    order_id TEXT PRIMARY KEY,
    seller_id TEXT NOT NULL,
    customer_id TEXT NOT NULL,
    currency TEXT NOT NULL,
    total_amount NUMERIC(18, 2) NOT NULL,
    channel TEXT NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id TEXT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    sku TEXT NOT NULL,
    qty INTEGER NOT NULL,
    price NUMERIC(18, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS order_geo (
    order_id TEXT PRIMARY KEY REFERENCES orders(order_id) ON DELETE CASCADE,
    region TEXT NOT NULL,
    city TEXT NOT NULL,
    timezone TEXT NOT NULL,
    regional_coef DOUBLE PRECISION NOT NULL
);
