package main.database;

import java.sql.*;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            System.out.println("Initializing database schema...");
            
            // Check if tables exist, if not create them
            if (!tableExists(conn, "users")) {
                createUsersTable(stmt);
            }
            
            if (!tableExists(conn, "restaurant_tables")) {
                createRestaurantTablesTable(stmt);
            }
            
            if (!tableExists(conn, "menu_categories")) {
                createMenuCategoriesTable(stmt);
            }
            
            if (!tableExists(conn, "menu_items")) {
                createMenuItemsTable(stmt);
            }
            
            if (!tableExists(conn, "inventory")) {
                createInventoryTable(stmt);
            }
            
            if (!tableExists(conn, "orders")) {
                createOrdersTable(stmt);
            }
            
            if (!tableExists(conn, "order_items")) {
                createOrderItemsTable(stmt);
            }
            
            if (!tableExists(conn, "reservations")) {
                createReservationsTable(stmt);
            }
            
            // Insert sample data if tables are empty
            insertSampleData(conn);
            
            stmt.close();
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database initialization failed!");
        }
    }
    
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
    
    private static void createUsersTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE users (" +
                    "user_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "password VARCHAR(255) NOT NULL," +
                    "full_name VARCHAR(100) NOT NULL," +
                    "role ENUM('ADMIN', 'MANAGER', 'WAITER', 'CHEF', 'CASHIER') NOT NULL," +
                    "is_active BOOLEAN DEFAULT TRUE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        stmt.executeUpdate(sql);
        System.out.println("Created users table");
    }
    
    private static void createRestaurantTablesTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE restaurant_tables (" +
                    "table_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "table_number VARCHAR(10) UNIQUE NOT NULL," +
                    "capacity INT NOT NULL," +
                    "status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED') DEFAULT 'AVAILABLE'," +
                    "location VARCHAR(50))";
        stmt.executeUpdate(sql);
        System.out.println("Created restaurant_tables table");
    }
    
    private static void createMenuCategoriesTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE menu_categories (" +
                    "category_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "category_name VARCHAR(50) NOT NULL," +
                    "description TEXT)";
        stmt.executeUpdate(sql);
        System.out.println("Created menu_categories table");
    }
    
    private static void createMenuItemsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE menu_items (" +
                    "item_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "item_name VARCHAR(100) NOT NULL," +
                    "category_id INT," +
                    "price DECIMAL(10, 2) NOT NULL," +
                    "description TEXT," +
                    "is_available BOOLEAN DEFAULT TRUE," +
                    "preparation_time INT," +
                    "image_url VARCHAR(255)," +
                    "FOREIGN KEY (category_id) REFERENCES menu_categories(category_id))";
        stmt.executeUpdate(sql);
        System.out.println("Created menu_items table");
    }
    
    private static void createInventoryTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE inventory (" +
                    "inventory_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "item_name VARCHAR(100) NOT NULL," +
                    "quantity DECIMAL(10, 2) NOT NULL," +
                    "unit VARCHAR(20)," +
                    "reorder_level DECIMAL(10, 2)," +
                    "supplier VARCHAR(100)," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
        stmt.executeUpdate(sql);
        System.out.println("Created inventory table");
    }
    
    private static void createOrdersTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE orders (" +
                    "order_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "table_id INT," +
                    "waiter_id INT," +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "status ENUM('PENDING', 'PREPARING', 'READY', 'SERVED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING'," +
                    "total_amount DECIMAL(10, 2)," +
                    "discount DECIMAL(10, 2) DEFAULT 0," +
                    "tax DECIMAL(10, 2) DEFAULT 0," +
                    "final_amount DECIMAL(10, 2)," +
                    "payment_method ENUM('CASH', 'CARD', 'UPI', 'PENDING') DEFAULT 'PENDING'," +
                    "FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id)," +
                    "FOREIGN KEY (waiter_id) REFERENCES users(user_id))";
        stmt.executeUpdate(sql);
        System.out.println("Created orders table");
    }
    
    private static void createOrderItemsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE order_items (" +
                    "order_item_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "order_id INT," +
                    "item_id INT," +
                    "quantity INT NOT NULL," +
                    "unit_price DECIMAL(10, 2) NOT NULL," +
                    "subtotal DECIMAL(10, 2) NOT NULL," +
                    "special_instructions TEXT," +
                    "status ENUM('PENDING', 'PREPARING', 'READY') DEFAULT 'PENDING'," +
                    "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (item_id) REFERENCES menu_items(item_id))";
        stmt.executeUpdate(sql);
        System.out.println("Created order_items table");
    }
    
    private static void createReservationsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE reservations (" +
                    "reservation_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "customer_name VARCHAR(100) NOT NULL," +
                    "customer_phone VARCHAR(20)," +
                    "table_id INT," +
                    "reservation_date DATE NOT NULL," +
                    "reservation_time TIME NOT NULL," +
                    "number_of_guests INT NOT NULL," +
                    "status ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'CONFIRMED'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id))";
        stmt.executeUpdate(sql);
        System.out.println("Created reservations table");
    }
    
    private static void insertSampleData(Connection conn) throws SQLException {
        // Check if data already exists
        Statement checkStmt = conn.createStatement();
        ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users");
        rs.next();
        int userCount = rs.getInt(1);
        rs.close();
        checkStmt.close();
        
        if (userCount > 0) {
            System.out.println("Sample data already exists, skipping insertion");
            return;
        }
        
        System.out.println("Inserting sample data...");
        
        // Insert users
        PreparedStatement pst = conn.prepareStatement(
            "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)"
        );
        
        String[][] users = {
            {"admin", "admin123", "System Admin", "ADMIN"},
            {"manager1", "manager123", "John Manager", "MANAGER"},
            {"waiter1", "waiter123", "Sarah Waiter", "WAITER"},
            {"waiter2", "waiter123", "Mike Waiter", "WAITER"},
            {"chef1", "chef123", "Gordon Chef", "CHEF"},
            {"cashier1", "cashier123", "Emma Cashier", "CASHIER"}
        };
        
        for (String[] user : users) {
            pst.setString(1, user[0]);
            pst.setString(2, user[1]);
            pst.setString(3, user[2]);
            pst.setString(4, user[3]);
            pst.addBatch();
        }
        pst.executeBatch();
        pst.close();
        
        // Insert restaurant tables
        pst = conn.prepareStatement(
            "INSERT INTO restaurant_tables (table_number, capacity, location) VALUES (?, ?, ?)"
        );
        
        String[][] tables = {
            {"T1", "2", "Window Side"},
            {"T2", "4", "Center"},
            {"T3", "4", "Corner"},
            {"T4", "6", "Private Room"},
            {"T5", "2", "Window Side"},
            {"T6", "8", "Garden"},
            {"T7", "4", "Center"},
            {"T8", "2", "Balcony"}
        };
        
        for (String[] table : tables) {
            pst.setString(1, table[0]);
            pst.setInt(2, Integer.parseInt(table[1]));
            pst.setString(3, table[2]);
            pst.addBatch();
        }
        pst.executeBatch();
        pst.close();
        
        // Insert menu categories
        pst = conn.prepareStatement(
            "INSERT INTO menu_categories (category_name, description) VALUES (?, ?)"
        );
        
        String[][] categories = {
            {"Appetizers", "Starters and light dishes"},
            {"Main Course", "Primary dishes"},
            {"Desserts", "Sweet dishes"},
            {"Beverages", "Drinks and refreshments"},
            {"Soups", "Hot and cold soups"},
            {"Salads", "Fresh salads"}
        };
        
        for (String[] category : categories) {
            pst.setString(1, category[0]);
            pst.setString(2, category[1]);
            pst.addBatch();
        }
        pst.executeBatch();
        pst.close();
        
        // Insert menu items
        pst = conn.prepareStatement(
            "INSERT INTO menu_items (item_name, category_id, price, description, preparation_time) VALUES (?, ?, ?, ?, ?)"
        );
        
        Object[][] menuItems = {
            {"Spring Rolls", 1, 150.00, "Crispy vegetable spring rolls", 10},
            {"Chicken Tikka", 1, 250.00, "Marinated grilled chicken", 15},
            {"Paneer Tikka", 1, 220.00, "Grilled cottage cheese", 15},
            {"Paneer Butter Masala", 2, 280.00, "Cottage cheese in butter gravy", 20},
            {"Chicken Biryani", 2, 320.00, "Aromatic rice with chicken", 25},
            {"Dal Makhani", 2, 200.00, "Creamy black lentils", 20},
            {"Butter Chicken", 2, 350.00, "Chicken in butter sauce", 25},
            {"Veg Biryani", 2, 240.00, "Aromatic rice with vegetables", 20},
            {"Ice Cream", 3, 80.00, "Assorted flavors", 5},
            {"Gulab Jamun", 3, 60.00, "Traditional Indian sweet", 5},
            {"Chocolate Brownie", 3, 120.00, "Warm brownie with ice cream", 8},
            {"Soft Drink", 4, 40.00, "Cola, Sprite, etc.", 2},
            {"Fresh Juice", 4, 80.00, "Seasonal fruit juice", 5},
            {"Lassi", 4, 60.00, "Sweet or salted yogurt drink", 3},
            {"Tomato Soup", 5, 90.00, "Classic tomato soup", 10},
            {"Hot and Sour Soup", 5, 100.00, "Spicy Chinese soup", 12},
            {"Caesar Salad", 6, 180.00, "Fresh lettuce with Caesar dressing", 8},
            {"Greek Salad", 6, 200.00, "Mediterranean salad", 8}
        };
        
        for (Object[] item : menuItems) {
            pst.setString(1, (String) item[0]);
            pst.setInt(2, (Integer) item[1]);
            pst.setDouble(3, (Double) item[2]);
            pst.setString(4, (String) item[3]);
            pst.setInt(5, (Integer) item[4]);
            pst.addBatch();
        }
        pst.executeBatch();
        pst.close();
        
        // Insert inventory items
        pst = conn.prepareStatement(
            "INSERT INTO inventory (item_name, quantity, unit, reorder_level, supplier) VALUES (?, ?, ?, ?, ?)"
        );
        
        Object[][] inventoryItems = {
            {"Rice", 50.0, "kg", 10.0, "ABC Suppliers"},
            {"Chicken", 30.0, "kg", 8.0, "Fresh Meat Co"},
            {"Paneer", 15.0, "kg", 5.0, "Dairy Fresh"},
            {"Tomatoes", 25.0, "kg", 5.0, "Veggie Mart"},
            {"Onions", 20.0, "kg", 5.0, "Veggie Mart"},
            {"Oil", 10.0, "liters", 3.0, "Golden Oil"},
            {"Flour", 40.0, "kg", 10.0, "ABC Suppliers"},
            {"Sugar", 20.0, "kg", 5.0, "Sweet Suppliers"},
            {"Salt", 15.0, "kg", 3.0, "ABC Suppliers"},
            {"Milk", 25.0, "liters", 8.0, "Dairy Fresh"}
        };
        
        for (Object[] item : inventoryItems) {
            pst.setString(1, (String) item[0]);
            pst.setDouble(2, (Double) item[1]);
            pst.setString(3, (String) item[2]);
            pst.setDouble(4, (Double) item[3]);
            pst.setString(5, (String) item[4]);
            pst.addBatch();
        }
        pst.executeBatch();
        pst.close();
        
        System.out.println("Sample data inserted successfully!");
    }
    
    public static void main(String[] args) {
        initializeDatabase();
    }
}