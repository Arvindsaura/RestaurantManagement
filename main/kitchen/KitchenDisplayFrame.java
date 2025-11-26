package main.kitchen;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import main.database.DatabaseConnection;

public class KitchenDisplayFrame extends JFrame {
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private Timer refreshTimer;

    public KitchenDisplayFrame() {
        setTitle("Kitchen Display System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        initComponents();
        loadOrders();
        startAutoRefresh();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JLabel titleLabel = new JLabel("Kitchen Orders", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Order ID", "Table", "Item Name", "Quantity", "Special Instructions", "Status", "Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(30);
        ordersTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Color code rows based on status
        ordersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 5);
                
                if (!isSelected) {
                    if ("PENDING".equals(status)) {
                        c.setBackground(new Color(255, 235, 235));
                    } else if ("PREPARING".equals(status)) {
                        c.setBackground(new Color(255, 250, 205));
                    } else if ("READY".equals(status)) {
                        c.setBackground(new Color(220, 255, 220));
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton markPreparingBtn = new JButton("Mark as Preparing");
        markPreparingBtn.setBackground(new Color(255, 193, 7));
        markPreparingBtn.addActionListener(e -> updateOrderStatus("PREPARING"));
        
        JButton markReadyBtn = new JButton("Mark as Ready");
        markReadyBtn.setBackground(new Color(76, 175, 80));
        markReadyBtn.setForeground(Color.WHITE);
        markReadyBtn.addActionListener(e -> updateOrderStatus("READY"));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadOrders());
        
        buttonPanel.add(markPreparingBtn);
        buttonPanel.add(markReadyBtn);
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadOrders() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT o.order_id, t.table_number, mi.item_name, oi.quantity, " +
                          "oi.special_instructions, oi.status, o.order_date " +
                          "FROM orders o " +
                          "JOIN restaurant_tables t ON o.table_id = t.table_id " +
                          "JOIN order_items oi ON o.order_id = oi.order_id " +
                          "JOIN menu_items mi ON oi.item_id = mi.item_id " +
                          "WHERE o.status IN ('PENDING', 'PREPARING') " +
                          "ORDER BY o.order_date ASC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("order_id"),
                    rs.getString("table_number"),
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getString("special_instructions"),
                    rs.getString("status"),
                    rs.getTimestamp("order_date")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateOrderStatus(String newStatus) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order item");
            return;
        }

        int orderId = (int) ordersTable.getValueAt(selectedRow, 0);
        String itemName = (String) ordersTable.getValueAt(selectedRow, 2);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE order_items SET status = ? WHERE order_id = ? AND item_id = (SELECT item_id FROM menu_items WHERE item_name = ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, newStatus);
            pst.setInt(2, orderId);
            pst.setString(3, itemName);
            pst.executeUpdate();
            pst.close();

            loadOrders();
            JOptionPane.showMessageDialog(this, "Order status updated!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating status");
        }
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> loadOrders()); // Refresh every 30 seconds
        refreshTimer.start();
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
}

