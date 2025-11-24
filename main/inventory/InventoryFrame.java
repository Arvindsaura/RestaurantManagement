package main.inventory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import main.database.DatabaseConnection;

public class InventoryFrame extends JFrame {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField itemNameField, quantityField, unitField, reorderField, supplierField;

    public InventoryFrame() {
        setTitle("Inventory Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        initComponents();
        loadInventory();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top Panel - Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Inventory Item Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        itemNameField = new JTextField(20);
        formPanel.add(itemNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(20);
        formPanel.add(quantityField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 1;
        unitField = new JTextField(20);
        formPanel.add(unitField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Reorder Level:"), gbc);
        gbc.gridx = 1;
        reorderField = new JTextField(20);
        formPanel.add(reorderField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        supplierField = new JTextField(20);
        formPanel.add(supplierField, gbc);

        JPanel formButtonPanel = new JPanel();
        JButton addBtn = new JButton("Add Item");
        addBtn.setBackground(new Color(46, 204, 113));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> addInventoryItem());
        
        JButton updateBtn = new JButton("Update Item");
        updateBtn.setBackground(new Color(52, 152, 219));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.addActionListener(e -> updateInventoryItem());
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearForm());
        
        formButtonPanel.add(addBtn);
        formButtonPanel.add(updateBtn);
        formButtonPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(formButtonPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // Center Panel - Table
        String[] columns = {"ID", "Item Name", "Quantity", "Unit", "Reorder Level", "Supplier", "Last Updated"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    loadSelectedItem();
                }
            }
        });
        
        // Color code rows for low stock
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    try {
                        double quantity = Double.parseDouble(table.getValueAt(row, 2).toString());
                        double reorderLevel = Double.parseDouble(table.getValueAt(row, 4).toString());
                        
                        if (quantity <= reorderLevel) {
                            c.setBackground(new Color(255, 235, 235));
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel - Actions
        JPanel bottomPanel = new JPanel();
        JButton deleteBtn = new JButton("Delete Item");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteInventoryItem());
        
        JButton lowStockBtn = new JButton("Show Low Stock");
        lowStockBtn.addActionListener(e -> showLowStock());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadInventory());
        
        bottomPanel.add(deleteBtn);
        bottomPanel.add(lowStockBtn);
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadInventory() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM inventory ORDER BY inventory_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("inventory_id"),
                    rs.getString("item_name"),
                    rs.getDouble("quantity"),
                    rs.getString("unit"),
                    rs.getDouble("reorder_level"),
                    rs.getString("supplier"),
                    rs.getTimestamp("last_updated")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addInventoryItem() {
        if (!validateInput()) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO inventory (item_name, quantity, unit, reorder_level, supplier) VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, itemNameField.getText().trim());
            pst.setDouble(2, Double.parseDouble(quantityField.getText().trim()));
            pst.setString(3, unitField.getText().trim());
            pst.setDouble(4, Double.parseDouble(reorderField.getText().trim()));
            pst.setString(5, supplierField.getText().trim());
            
            pst.executeUpdate();
            pst.close();
            
            JOptionPane.showMessageDialog(this, "Inventory item added successfully!");
            clearForm();
            loadInventory();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding item: " + e.getMessage());
        }
    }

    private void updateInventoryItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update");
            return;
        }

        if (!validateInput()) return;

        try {
            int inventoryId = (int) inventoryTable.getValueAt(selectedRow, 0);
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE inventory SET item_name = ?, quantity = ?, unit = ?, reorder_level = ?, supplier = ? WHERE inventory_id = ?";
            
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, itemNameField.getText().trim());
            pst.setDouble(2, Double.parseDouble(quantityField.getText().trim()));
            pst.setString(3, unitField.getText().trim());
            pst.setDouble(4, Double.parseDouble(reorderField.getText().trim()));
            pst.setString(5, supplierField.getText().trim());
            pst.setInt(6, inventoryId);
            
            pst.executeUpdate();
            pst.close();
            
            JOptionPane.showMessageDialog(this, "Inventory item updated successfully!");
            clearForm();
            loadInventory();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage());
        }
    }

    private void deleteInventoryItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            int inventoryId = (int) inventoryTable.getValueAt(selectedRow, 0);
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("DELETE FROM inventory WHERE inventory_id = ?");
            pst.setInt(1, inventoryId);
            pst.executeUpdate();
            pst.close();
            
            JOptionPane.showMessageDialog(this, "Inventory item deleted successfully!");
            loadInventory();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage());
        }
    }

    private void showLowStock() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM inventory WHERE quantity <= reorder_level ORDER BY quantity ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("inventory_id"),
                    rs.getString("item_name"),
                    rs.getDouble("quantity"),
                    rs.getString("unit"),
                    rs.getDouble("reorder_level"),
                    rs.getString("supplier"),
                    rs.getTimestamp("last_updated")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No low stock items!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectedItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            itemNameField.setText((String) inventoryTable.getValueAt(selectedRow, 1));
            quantityField.setText(String.valueOf(inventoryTable.getValueAt(selectedRow, 2)));
            unitField.setText((String) inventoryTable.getValueAt(selectedRow, 3));
            reorderField.setText(String.valueOf(inventoryTable.getValueAt(selectedRow, 4)));
            supplierField.setText((String) inventoryTable.getValueAt(selectedRow, 5));
        }
    }

    private boolean validateInput() {
        if (itemNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter item name");
            return false;
        }
        try {
            Double.parseDouble(quantityField.getText().trim());
            Double.parseDouble(reorderField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and reorder level");
            return false;
        }
        return true;
    }

    private void clearForm() {
        itemNameField.setText("");
        quantityField.setText("");
        unitField.setText("");
        reorderField.setText("");
        supplierField.setText("");
    }
}
