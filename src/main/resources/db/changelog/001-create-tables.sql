--liquibase formatted sql

--changeset coffee-shop:1

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL UNIQUE,
    home_address TEXT,
    work_address TEXT,
    loyalty_score INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,    -- @CreationTimestamp managed by Hibernate
    updated_at TIMESTAMP     -- @UpdateTimestamp managed by Hibernate
);

CREATE TABLE IF NOT EXISTS coffee_shops (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    contact_number VARCHAR(20),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    opening_time TIME,
    closing_time TIME,
    max_queue_size INTEGER DEFAULT 50,
    number_of_queues INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    coffee_shop_id BIGINT NOT NULL,
    CONSTRAINT fk_menu_items_coffee_shop FOREIGN KEY (coffee_shop_id) REFERENCES coffee_shops(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    coffee_shop_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',  -- Enum as STRING, default 'PENDING'
    total_amount NUMERIC(10, 2) NOT NULL,
    queue_position INTEGER,
    estimated_wait_time INTEGER,
    created_at TIMESTAMP,    -- @CreationTimestamp
    updated_at TIMESTAMP,    -- @UpdateTimestamp
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_orders_coffee_shop FOREIGN KEY (coffee_shop_id) REFERENCES coffee_shops(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    notes TEXT,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_coffee_shop_id ON orders(coffee_shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_queue_position ON orders(queue_position);
CREATE INDEX IF NOT EXISTS idx_menu_items_coffee_shop_id ON menu_items(coffee_shop_id);
