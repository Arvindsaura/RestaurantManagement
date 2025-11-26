package main.reports;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import main.database.DatabaseConnection;

public class ReportsFrame extends JFrame {
    private JTable reportsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeCombo;
    private JSpinner fromDateSpinner, toDateSpinner;

    public ReportsFrame() {
        setTitle("Reports");
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypeCombo = new JComboBox<>(new String[] {"Daily Sales", "Monthly Sales", "Orders"});
        fromDateSpinner = new JSpinner(new SpinnerDateModel());
        toDateSpinner = new JSpinner(new SpinnerDateModel());
        JButton generateBtn = new JButton("Generate");
        generateBtn.addActionListener(e -> generateReport());

        top.add(new JLabel("Report:")); top.add(reportTypeCombo);
        top.add(new JLabel("From:")); top.add(fromDateSpinner);
        top.add(new JLabel("To:")); top.add(toDateSpinner);
        top.add(generateBtn);

        String[] cols = {"Report Item", "Value"};
        tableModel = new DefaultTableModel(cols, 0);
        reportsTable = new JTable(tableModel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(reportsTable), BorderLayout.CENTER);
    }

    private void generateReport() {
        tableModel.setRowCount(0);
        String type = (String) reportTypeCombo.getSelectedItem();
        Date from = (Date) fromDateSpinner.getValue();
        Date to = (Date) toDateSpinner.getValue();

        try {
            if ("Daily Sales".equals(type)) {
                ResultSet rs = ReportGenerator.generateSalesReport(new java.sql.Date(from.getTime()), new java.sql.Date(to.getTime()));
                while (rs.next()) {
                    tableModel.addRow(new Object[] { rs.getString("date"), rs.getDouble("total_sales") });
                }
            } else if ("Monthly Sales".equals(type)) {
                ResultSet rs = ReportGenerator.generateMonthlySales(new java.sql.Date(from.getTime()), new java.sql.Date(to.getTime()));
                while (rs.next()) {
                    tableModel.addRow(new Object[] { rs.getString("month"), rs.getDouble("total_sales") });
                }
            } else {
                ResultSet rs = ReportGenerator.generateOrderReport(new java.sql.Date(from.getTime()), new java.sql.Date(to.getTime()));
                while (rs.next()) {
                    tableModel.addRow(new Object[] { rs.getInt("order_id"), rs.getDouble("final_amount") });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
        }
    }
}