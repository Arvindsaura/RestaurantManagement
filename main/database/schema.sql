-- Users table for authentication
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'WAITER', 'CHEF', 'CASHIER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tables in the restaurant
CREATE TABLE restaurant_tables (
    table_id INT PRIMARY KEY AUTO_INCREMENT,
    table_number VARCHAR(10) UNIQUE NOT NULL,
    capacity INT NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED') DEFAULT 'AVAILABLE',
    location VARCHAR(50)
);

-- Menu categories
CREATE TABLE menu_categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    description TEXT
);

-- Menu items
CREATE TABLE menu_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    category_id INT,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time INT, -- in minutes
    image_url VARCHAR(255),
    FOREIGN KEY (category_id) REFERENCES menu_categories(category_id)
);

-- Inventory
CREATE TABLE inventory (
    inventory_id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20),
    reorder_level DECIMAL(10, 2),
    supplier VARCHAR(100),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Orders
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    table_id INT,
    waiter_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'PREPARING', 'READY', 'SERVED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2),
    discount DECIMAL(10, 2) DEFAULT 0,
    tax DECIMAL(10, 2) DEFAULT 0,
    final_amount DECIMAL(10, 2),
    payment_method ENUM('CASH', 'CARD', 'UPI', 'PENDING') DEFAULT 'PENDING',
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id),
    FOREIGN KEY (waiter_id) REFERENCES users(user_id)
);

-- Order items
CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    item_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    status ENUM('PENDING', 'PREPARING', 'READY') DEFAULT 'PENDING',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

-- Reservations
CREATE TABLE reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20),
    table_id INT,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    number_of_guests INT NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id)
);

-- Sample data
INSERT INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'System Admin', 'ADMIN'),
('manager1', 'manager123', 'John Manager', 'MANAGER'),
('waiter1', 'waiter123', 'Sarah Waiter', 'WAITER'),
('chef1', 'chef123', 'Mike Chef', 'CHEF'),
('cashier1', 'cashier123', 'Emma Cashier', 'CASHIER');

INSERT INTO restaurant_tables (table_number, capacity, location) VALUES
('T1', 2, 'Window Side'),
('T2', 4, 'Center'),
('T3', 4, 'Corner'),
('T4', 6, 'Private Room'),
('T5', 2, 'Window Side');

INSERT INTO menu_categories (category_name, description) VALUES
('Appetizers', 'Starters and light dishes'),
('Main Course', 'Primary dishes'),
('Desserts', 'Sweet dishes'),
('Beverages', 'Drinks and refreshments');

INSERT INTO menu_items (item_name, category_id, price, description, preparation_time) VALUES
('Spring Rolls', 1, 150.00, 'Crispy vegetable spring rolls', 10),
('Chicken Tikka', 1, 250.00, 'Marinated grilled chicken', 15),
('Paneer Butter Masala', 2, 280.00, 'Cottage cheese in butter gravy', 20),
('Chicken Biryani', 2, 320.00, 'Aromatic rice with chicken', 25),
('Dal Makhani', 2, 200.00, 'Creamy black lentils', 20),
('Ice Cream', 3, 80.00, 'Assorted flavors', 5),
('Gulab Jamun', 3, 60.00, 'Traditional Indian sweet', 5),
('Soft Drink', 4, 40.00, 'Cola, Sprite, etc.', 2),
('Fresh Juice', 4, 80.00, 'Seasonal fruit juice', 5);
