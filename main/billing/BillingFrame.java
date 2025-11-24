package main.billing;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import main.database.DatabaseConnection;

public class BillingFrame extends JFrame {
    private JComboBox<String> orderComboBox;
    private JTable billItemsTable;
    private DefaultTableModel tableModel;
    private JLabel subtotalLabel, taxLabel, discountLabel, totalLabel;
    private JTextField discountField;
    private JComboBox<String> paymentMethodComboBox;
    private int currentOrderId = 0;
    private double subtotal = 0.0;
    private double taxAmount = 0.0;
    private double discountAmount = 0.0;
    private double finalTotal = 0.0;

    public BillingFrame() {
        setTitle("Billing System");
        setSize(900, 700);
        setLocationRelativeTo(null);

        initComponents();
        loadPendingOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        //Top Panel-Order Selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("Select Order:"));
        orderComboBox = new JComboBox<>();

        //UI building for table and labels
        String[] columns = {"Item", "Qty", "Unit Price", "Subtotal"};
        tableModel = new DefaultTableModel(columns, 0);
        billItemsTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(billItemsTable);

        subtotalLabel = new JLabel("Subtotal: 0.00");
        taxLabel = new JLabel("Tax: 0.00");
        discountLabel = new JLabel("Discount: 0.00");
        totalLabel = new JLabel("Total: 0.00");

        discountField = new JTextField(6);
        paymentMethodComboBox = new JComboBox<>(new String[] {"CASH", "CARD", "UPI"});

        JButton calculateBtn = new JButton("Calculate");
        calculateBtn.addActionListener(e -> calculateTotals());

        JButton finalizeBtn = new JButton("Finalize Bill");
        finalizeBtn.addActionListener(e -> finalizeBill());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(new JLabel("Discount:")); bottomPanel.add(discountField);
        bottomPanel.add(new JLabel("Payment:")); bottomPanel.add(paymentMethodComboBox);
        bottomPanel.add(calculateBtn); bottomPanel.add(finalizeBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.add(tableScroll, BorderLayout.CENTER);

        JPanel totalsPanel = new JPanel(new GridLayout(4,1));
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(taxLabel);
        totalsPanel.add(discountLabel);
        totalsPanel.add(totalLabel);

        center.add(totalsPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadPendingOrders() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT order_id FROM orders WHERE status = 'PENDING'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            orderComboBox.removeAllItems();
            while (rs.next()) {
                orderComboBox.addItem(String.valueOf(rs.getInt("order_id")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrderItems(int orderId) {
        tableModel.setRowCount(0);
        subtotal = 0.0;
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT mi.item_name, oi.quantity, oi.unit_price, oi.subtotal FROM order_items oi JOIN menu_items mi ON oi.item_id = mi.item_id WHERE oi.order_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, orderId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String name = rs.getString("item_name");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("unit_price");
                double sub = rs.getDouble("subtotal");
                tableModel.addRow(new Object[] {name, qty, price, sub});
                subtotal += sub;
            }
            rs.close();
            pst.close();
            subtotalLabel.setText(String.format("Subtotal: %.2f", subtotal));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void calculateTotals() {
        taxAmount = subtotal * 0.05; // 5%tax example
        try {
            discountAmount = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException e) {
            discountAmount = 0.0;
        }
        finalTotal = subtotal + taxAmount - discountAmount;
        taxLabel.setText(String.format("Tax: %.2f", taxAmount));
        discountLabel.setText(String.format("Discount: %.2f", discountAmount));
        totalLabel.setText(String.format("Total: %.2f", finalTotal));
    }

    private void finalizeBill() {
        if (currentOrderId == 0) {
            JOptionPane.showMessageDialog(this, "Select an order first");
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            String insert = "UPDATE orders SET status = 'COMPLETED', final_amount = ?, payment_method = ? WHERE order_id = ?";
            PreparedStatement pst = conn.prepareStatement(insert);
            pst.setDouble(1, finalTotal);
            pst.setString(2, (String) paymentMethodComboBox.getSelectedItem());
            pst.setInt(3, currentOrderId);
            pst.executeUpdate();
            pst.close();
            JOptionPane.showMessageDialog(this, "Bill finalized and saved.");
            loadPendingOrders();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error finalizing bill: " + ex.getMessage());
        }
    }
}
