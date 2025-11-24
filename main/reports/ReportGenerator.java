package main.reports;

import java.sql.*;
import main.database.DatabaseConnection;

public class ReportGenerator {

    public static ResultSet generateSalesReport(Date from, Date to) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT DATE(order_date) as date, SUM(final_amount) as total_sales FROM orders WHERE order_date BETWEEN ? AND ? AND status = 'COMPLETED' GROUP BY DATE(order_date) ORDER BY DATE(order_date) ASC";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setDate(1, from);
        pst.setDate(2, to);
        return pst.executeQuery();
    }

    public static ResultSet generateMonthlySales(Date from, Date to) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT DATE_FORMAT(order_date, '%Y-%m') as month, SUM(final_amount) as total_sales FROM orders WHERE order_date BETWEEN ? AND ? AND status = 'COMPLETED' GROUP BY DATE_FORMAT(order_date, '%Y-%m') ORDER BY DATE_FORMAT(order_date, '%Y-%m') ASC";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setDate(1, from);
        pst.setDate(2, to);
        return pst.executeQuery();
    }

    public static ResultSet generateOrderReport(Date from, Date to) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT order_id, table_id, final_amount, payment_method, order_date FROM orders WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setDate(1, from);
        pst.setDate(2, to);
        return pst.executeQuery();
    }
}