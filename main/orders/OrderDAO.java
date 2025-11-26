package main.orders;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.database.DatabaseConnection;
import main.models.Order;

public class OrderDAO {
    
    public int createOrder(int tableId, int waiterId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "INSERT INTO orders (table_id, waiter_id, status, total_amount, tax, final_amount) " +
                      "VALUES (?, ?, 'PENDING', 0, 0, 0)";
        
        PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, tableId);
        pst.setInt(2, waiterId);
        pst.executeUpdate();
        
        ResultSet rs = pst.getGeneratedKeys();
        int orderId = 0;
        if (rs.next()) {
            orderId = rs.getInt(1);
        }
        
        rs.close();
        pst.close();
        return orderId;
    }
    
    public void addOrderItem(int orderId, int itemId, int quantity, double unitPrice, double subtotal, String specialInstructions) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "INSERT INTO order_items (order_id, item_id, quantity, unit_price, subtotal, special_instructions) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, orderId);
        pst.setInt(2, itemId);
        pst.setInt(3, quantity);
        pst.setDouble(4, unitPrice);
        pst.setDouble(5, subtotal);
        pst.setString(6, specialInstructions);
        pst.executeUpdate();
        pst.close();
    }
    
    public void updateOrderTotals(int orderId, double totalAmount, double tax, double finalAmount) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "UPDATE orders SET total_amount = ?, tax = ?, final_amount = ? WHERE order_id = ?";
        
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setDouble(1, totalAmount);
        pst.setDouble(2, tax);
        pst.setDouble(3, finalAmount);
        pst.setInt(4, orderId);
        pst.executeUpdate();
        pst.close();
    }
    
    public void updateOrderStatus(int orderId, String status) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "UPDATE orders SET status = ? WHERE order_id = ?";
        
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, status);
        pst.setInt(2, orderId);
        pst.executeUpdate();
        pst.close();
    }
    
    public Order getOrderById(int orderId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT * FROM orders WHERE order_id = ?";
        
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, orderId);
        ResultSet rs = pst.executeQuery();
        
        Order order = null;
        if (rs.next()) {
            order = new Order(
                rs.getInt("order_id"),
                rs.getInt("table_id"),
                rs.getInt("waiter_id"),
                rs.getTimestamp("order_date"),
                rs.getString("status"),
                rs.getDouble("total_amount"),
                rs.getDouble("discount"),
                rs.getDouble("tax"),
                rs.getDouble("final_amount"),
                rs.getString("payment_method")
            );
        }
        
        rs.close();
        pst.close();
        return order;
    }
    
    public List<Order> getAllActiveOrders() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT * FROM orders WHERE status IN ('PENDING', 'PREPARING', 'READY', 'SERVED') " +
                      "ORDER BY order_date DESC";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        List<Order> orders = new ArrayList<>();
        while (rs.next()) {
            Order order = new Order(
                rs.getInt("order_id"),
                rs.getInt("table_id"),
                rs.getInt("waiter_id"),
                rs.getTimestamp("order_date"),
                rs.getString("status"),
                rs.getDouble("total_amount"),
                rs.getDouble("discount"),
                rs.getDouble("tax"),
                rs.getDouble("final_amount"),
                rs.getString("payment_method")
            );
            orders.add(order);
        }
        
        rs.close();
        stmt.close();
        return orders;
    }
    
    public void deleteOrder(int orderId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "DELETE FROM orders WHERE order_id = ?";
        
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, orderId);
        pst.executeUpdate();
        pst.close();
    }
    
    public void updateTableStatus(int tableId, String status) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "UPDATE restaurant_tables SET status = ? WHERE table_id = ?";
        
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, status);
        pst.setInt(2, tableId);
        pst.executeUpdate();
        pst.close();
    }
    
    public int getTableIdByNumber(String tableNumber) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT table_id FROM restaurant_tables WHERE table_number = ?";
        
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, tableNumber);
        ResultSet rs = pst.executeQuery();
        
        int tableId = 0;
        if (rs.next()) {
            tableId = rs.getInt("table_id");
        }
        
        rs.close();
        pst.close();
        return tableId;
    }
}
