--liquibase formatted sql

--changeset coffee-shop:2

-- Insert sample customers
INSERT INTO customers (name, mobile_number, home_address, work_address, loyalty_score) VALUES
('John Doe', '+94771234567', '123 Main St, Colombo 03', 'World Trade Center, Colombo 01', 5),
('Jane Smith', '+94772345678', '456 Galle Rd, Colombo 04', 'Liberty Plaza, Colombo 03', 3),
('Bob Wilson', '+94773456789', '789 Kandy Rd, Colombo 07', 'Crescat Blvd, Colombo 03', 2);

-- Insert sample coffee shops
INSERT INTO coffee_shops (name, address, contact_number, latitude, longitude, opening_time, closing_time, max_queue_size, number_of_queues) VALUES
('Coffee Bean - Colombo City Centre', 'Colombo City Centre, Level 2', '+94112345678', 6.9271, 79.8612, '07:00:00', '22:00:00', 30, 2),
('Starbucks - Liberty Plaza', 'Liberty Plaza, Ground Floor', '+94112345679', 6.9147, 79.8574, '06:30:00', '23:00:00', 40, 3),
('Barista - World Trade Center', 'World Trade Center, Level 1', '+94112345680', 6.9349, 79.8437, '08:00:00', '20:00:00', 25, 1);

-- Insert sample menu items for Coffee Bean
INSERT INTO menu_items (name, description, price, available, coffee_shop_id) VALUES
('Americano', 'Classic black coffee', 450.00, true, 1),
('Cappuccino', 'Espresso with steamed milk and foam', 550.00, true, 1),
('Latte', 'Espresso with steamed milk', 600.00, true, 1),
('Mocha', 'Chocolate flavored coffee drink', 650.00, true, 1),
('Espresso', 'Strong black coffee shot', 350.00, true, 1);

-- Insert sample menu items for Starbucks
INSERT INTO menu_items (name, description, price, available, coffee_shop_id) VALUES
('Pike Place Roast', 'Signature medium roast coffee', 500.00, true, 2),
('Caramel Macchiato', 'Vanilla syrup, steamed milk, espresso, caramel', 750.00, true, 2),
('Frappuccino', 'Blended coffee drink', 800.00, true, 2),
('Green Tea Latte', 'Matcha green tea with steamed milk', 700.00, true, 2),
('Hot Chocolate', 'Rich chocolate drink', 600.00, true, 2);

-- Insert sample menu items for Barista
INSERT INTO menu_items (name, description, price, available, coffee_shop_id) VALUES
('Filter Coffee', 'Traditional South Indian coffee', 400.00, true, 3),
('Masala Chai', 'Spiced tea latte', 350.00, true, 3),
('Cold Coffee', 'Iced coffee with milk', 500.00, true, 3),
('Black Tea', 'Classic Ceylon black tea', 250.00, true, 3),
('Iced Tea', 'Refreshing cold tea', 300.00, true, 3);

-- Insert sample orders
INSERT INTO orders (customer_id, coffee_shop_id, status, total_amount, queue_position, estimated_wait_time) VALUES
(1, 1, 'PENDING', 1000.00, 1, 5),
(2, 1, 'CONFIRMED', 650.00, 2, 10),
(3, 2, 'IN_PROGRESS', 1500.00, 1, 3);

-- Insert sample order items
INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, total_price, notes) VALUES
(1, 1, 1, 450.00, 450.00, 'No sugar'),
(1, 3, 1, 550.00, 550.00, 'Extra hot'),
(2, 4, 1, 650.00, 650.00, 'Less chocolate'),
(3, 7, 2, 750.00, 1500.00, 'Extra caramel');
