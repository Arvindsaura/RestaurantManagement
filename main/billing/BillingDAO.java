package main.billing;
import java.sql.*;
import main.database.DatabaseConnection;

public class BillingDAO {

    public static void saveBill(int orderId, double subtotal, double tax, double discount, double total, String paymentMethod) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String insert = "INSERT INTO billing (order_id, subtotal, tax, discount, total, payment_method, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(insert);
        pst.setInt(1, orderId);
        pst.setDouble(2, subtotal);
        pst.setDouble(3, tax);
        pst.setDouble(4, discount);
        pst.setDouble(5, total);
        pst.setString(6, paymentMethod);
        pst.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
        pst.executeUpdate();
        pst.close();
    }

    public static ResultSet getBillsBetween(Date from, Date to) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT * FROM billing WHERE created_at BETWEEN ? AND ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setDate(1, from);
        pst.setDate(2, to);
        return pst.executeQuery();
    }
}
