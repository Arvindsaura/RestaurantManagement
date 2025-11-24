package main.orders;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import main.database.DatabaseConnection;
import main.auth.UserSession;

public class OrderFrame extends JFrame {
    private JComboBox<String> tableComboBox;
    private JTable menuTable;
    private JTable orderItemsTable;
    private DefaultTableModel menuTableModel;
    private DefaultTableModel orderItemsTableModel;
    private JLabel totalLabel;
    private ArrayList<OrderItem> currentOrderItems;
    private double totalAmount = 0.0;

    public OrderFrame() {
        setTitle("New Order");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        currentOrderItems = new ArrayList<>();
        
        initComponents();
        loadTables();
        loadMenuItems();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top Panel - Table Selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("Select Table:"));
        tableComboBox = new JComboBox<>();
        topPanel.add(tableComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel - Split into Menu and Order Items
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left - Menu Items
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu Items"));
        
        String[] menuColumns = {"ID", "Item Name", "Category", "Price", "Available"};
        menuTableModel = new DefaultTableModel(menuColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        menuTable = new JTable(menuTableModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuScrollPane = new JScrollPane(menuTable);
        
        JPanel menuButtonPanel = new JPanel();
        JButton addToOrderBtn = new JButton("Add to Order");
        addToOrderBtn.addActionListener(e -> addItemToOrder());
        menuButtonPanel.add(addToOrderBtn);
        
        menuPanel.add(menuScrollPane, BorderLayout.CENTER);
        menuPanel.add(menuButtonPanel, BorderLayout.SOUTH);
        
        // Right - Order Items
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Order Items"));
        
        String[] orderColumns = {"Item Name", "Quantity", "Unit Price", "Subtotal"};
        orderItemsTableModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 1; }
        };
        orderItemsTable = new JTable(orderItemsTableModel);
        JScrollPane orderScrollPane = new JScrollPane(orderItemsTable);
        
        JPanel orderButtonPanel = new JPanel();
        JButton removeBtn = new JButton("Remove Item");
        removeBtn.addActionListener(e -> removeItemFromOrder());
        orderButtonPanel.add(removeBtn);
        
        orderPanel.add(orderScrollPane, BorderLayout.CENTER);
        orderPanel.add(orderButtonPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(menuPanel);
        splitPane.setRightComponent(orderPanel);
        splitPane.setDividerLocation(600);
        
        add(splitPane, BorderLayout.CENTER);

        // Bottom Panel - Total and Actions
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomPanel.add(totalLabel, BorderLayout.WEST);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton placeOrderBtn = new JButton("Place Order");
        placeOrderBtn.setBackground(new Color(46, 204, 113));
        placeOrderBtn.setForeground(Color.WHITE);
        placeOrderBtn.addActionListener(e -> placeOrder());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        actionPanel.add(placeOrderBtn);
        actionPanel.add(cancelBtn);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadTables() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM restaurant_tables WHERE status = 'AVAILABLE'");
            
            while (rs.next()) {
                tableComboBox.addItem(rs.getString("table_number"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMenuItems() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT m.item_id, m.item_name, c.category_name, m.price, m.is_available " +
                          "FROM menu_items m JOIN menu_categories c ON m.category_id = c.category_id " +
                          "WHERE m.is_available = TRUE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            menuTableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getString("category_name"),
                    String.format("₹%.2f", rs.getDouble("price")),
                    rs.getBoolean("is_available") ? "Yes" : "No"
                };
                menuTableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addItemToOrder() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item");
            return;
        }

        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (quantityStr == null || quantityStr.trim().isEmpty()) return;
        
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive");
                return;
            }

            int itemId = (int) menuTable.getValueAt(selectedRow, 0);
            String itemName = (String) menuTable.getValueAt(selectedRow, 1);
            String priceStr = (String) menuTable.getValueAt(selectedRow, 3);
            double price = Double.parseDouble(priceStr.replace("₹", ""));
            double subtotal = price * quantity;

            OrderItem orderItem = new OrderItem(itemId, itemName, quantity, price, subtotal);
            currentOrderItems.add(orderItem);

            Object[] row = {itemName, quantity, String.format("₹%.2f", price), String.format("₹%.2f", subtotal)};
            orderItemsTableModel.addRow(row);

            updateTotal();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
        }
    }

    private void removeItemFromOrder() {
        int selectedRow = orderItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove");
            return;
        }

        currentOrderItems.remove(selectedRow);
        orderItemsTableModel.removeRow(selectedRow);
        updateTotal();
    }

    private void updateTotal() {
        totalAmount = 0.0;
        for (OrderItem item : currentOrderItems) {
            totalAmount += item.subtotal;
        }
        totalLabel.setText(String.format("Total: ₹%.2f", totalAmount));
    }

    private void placeOrder() {
        if (tableComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a table");
            return;
        }

        if (currentOrderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add items to order");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Get table ID
            String tableNumber = (String) tableComboBox.getSelectedItem();
            PreparedStatement tableStmt = conn.prepareStatement("SELECT table_id FROM restaurant_tables WHERE table_number = ?");
            tableStmt.setString(1, tableNumber);
            ResultSet tableRs = tableStmt.executeQuery();
            int tableId = 0;
            if (tableRs.next()) {
                tableId = tableRs.getInt("table_id");
            }

            // Insert order
            String orderQuery = "INSERT INTO orders (table_id, waiter_id, status, total_amount, tax, final_amount) VALUES (?, ?, 'PENDING', ?, ?, ?)";
            PreparedStatement orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, tableId);
            orderStmt.setInt(2, UserSession.getUserId());
            orderStmt.setDouble(3, totalAmount);
            double tax = totalAmount * 0.05; // 5% tax
            orderStmt.setDouble(4, tax);
            orderStmt.setDouble(5, totalAmount + tax);
            orderStmt.executeUpdate();

            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            }

            // Insert order items
            String itemQuery = "INSERT INTO order_items (order_id, item_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
            for (OrderItem item : currentOrderItems) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.itemId);
                itemStmt.setInt(3, item.quantity);
                itemStmt.setDouble(4, item.unitPrice);
                itemStmt.setDouble(5, item.subtotal);
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            // Update table status
            PreparedStatement updateTableStmt = conn.prepareStatement("UPDATE restaurant_tables SET status = 'OCCUPIED' WHERE table_id = ?");
            updateTableStmt.setInt(1, tableId);
            updateTableStmt.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this, "Order placed successfully! Order ID: " + orderId);
            dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error placing order: " + e.getMessage());
        }
    }

    private class OrderItem {
        int itemId;
        String itemName;
        int quantity;
        double unitPrice;
        double subtotal;

        OrderItem(int itemId, String itemName, int quantity, double unitPrice, double subtotal) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }
    }
}
