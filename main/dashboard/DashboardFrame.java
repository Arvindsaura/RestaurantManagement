package main.dashboard;

import javax.swing.*;
import java.awt.*;
import main.auth.UserSession;
import main.orders.OrderFrame;
import main.menu.MenuFrame;
import main.inventory.InventoryFrame;
import main.billing.BillingFrame;
import main.reports.ReportsFrame;
import main.kitchen.KitchenDisplayFrame;

public class DashboardFrame extends JFrame {
    
    public DashboardFrame() {
        setTitle("Restaurant Management Dashboard - " + UserSession.getFullName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }

    private void initComponents() {
        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenu ordersMenu = new JMenu("Orders");
        JMenuItem newOrder = new JMenuItem("New Order");
        newOrder.addActionListener(e -> new OrderFrame().setVisible(true));
        ordersMenu.add(newOrder);
        
        JMenu kitchenMenu = new JMenu("Kitchen");
        JMenuItem kitchenDisplay = new JMenuItem("Kitchen Display");
        kitchenDisplay.addActionListener(e -> new KitchenDisplayFrame().setVisible(true));
        kitchenMenu.add(kitchenDisplay);
        
        JMenu menuManagement = new JMenu("Menu");
        JMenuItem manageMenu = new JMenuItem("Manage Menu");
        manageMenu.addActionListener(e -> new MenuFrame().setVisible(true));
        menuManagement.add(manageMenu);
        
        JMenu inventoryMenu = new JMenu("Inventory");
        JMenuItem manageInventory = new JMenuItem("Manage Inventory");
        manageInventory.addActionListener(e -> new InventoryFrame().setVisible(true));
        inventoryMenu.add(manageInventory);
        
        JMenu billingMenu = new JMenu("Billing");
        JMenuItem generateBill = new JMenuItem("Generate Bill");
        generateBill.addActionListener(e -> new BillingFrame().setVisible(true));
        billingMenu.add(generateBill);
        
        JMenu reportsMenu = new JMenu("Reports");
        JMenuItem viewReports = new JMenuItem("View Reports");
        viewReports.addActionListener(e -> new ReportsFrame().setVisible(true));
        reportsMenu.add(viewReports);
        
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> {
            UserSession.logout();
            dispose();
            new main.auth.LoginFrame().setVisible(true);
        });
        accountMenu.add(logout);
        
        menuBar.add(ordersMenu);
        menuBar.add(kitchenMenu);
        menuBar.add(menuManagement);
        menuBar.add(inventoryMenu);
        menuBar.add(billingMenu);
        menuBar.add(reportsMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(accountMenu);
        
        setJMenuBar(menuBar);

        // Main Panel with Dashboard Cards
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + UserSession.getFullName() + " (" + UserSession.getRole() + ")");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);
        
        // Dashboard Cards
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        cardsPanel.add(createDashboardCard("New Order", "Take new orders", new Color(52, 152, 219), e -> new OrderFrame().setVisible(true)));
        cardsPanel.add(createDashboardCard("Kitchen Display", "View orders in kitchen", new Color(230, 126, 34), e -> new KitchenDisplayFrame().setVisible(true)));
        cardsPanel.add(createDashboardCard("Menu Management", "Manage menu items", new Color(155, 89, 182), e -> new MenuFrame().setVisible(true)));
        cardsPanel.add(createDashboardCard("Inventory", "Manage inventory", new Color(46, 204, 113), e -> new InventoryFrame().setVisible(true)));
        cardsPanel.add(createDashboardCard("Billing", "Generate bills", new Color(241, 196, 15), e -> new BillingFrame().setVisible(true)));
        cardsPanel.add(createDashboardCard("Reports", "View reports", new Color(231, 76, 60), e -> new ReportsFrame().setVisible(true)));
        
        mainPanel.add(cardsPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createDashboardCard(String title, String description, Color color, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setForeground(Color.WHITE);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        
        card.add(textPanel, BorderLayout.CENTER);
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.actionPerformed(null);
            }
        });
        
        return card;
    }
}
