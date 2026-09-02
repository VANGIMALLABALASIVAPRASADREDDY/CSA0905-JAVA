package com.smartcharge.client.ui;

import com.smartcharge.client.model.UserDto;
import com.smartcharge.client.util.SessionContext;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentCard = "DASHBOARD";

    // Panels
    private final DashboardPanel dashboardPanel;
    private final VehicleManagementPanel vehiclePanel;
    private final CampusMapPanel campusMapPanel;
    private final SmartFinderPanel smartFinderPanel;
    private final ReservationPanel reservationPanel;
    private final VirtualQueuePanel virtualQueuePanel;
    private final ChargingSessionPanel chargingSessionPanel;
    private final PaymentPanel paymentPanel;
    private final ReportsPanel reportsPanel;
    private AdminStationPanel adminStationPanel;
    private AdminPointPanel adminPointPanel;

    public DashboardFrame() {
        super("SmartCharge Campus — Intelligent Load-Aware EV Charging Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.COLOR_BG);

        // 1. Header (NORTH)
        JPanel header = createHeader();
        root.add(header, BorderLayout.NORTH);

        // 2. Sidebar (WEST)
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 3. Content Area with CardLayout (CENTER)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtils.COLOR_BG);

        // Instantiate Panels
        dashboardPanel = new DashboardPanel();
        vehiclePanel = new VehicleManagementPanel();
        campusMapPanel = new CampusMapPanel();
        smartFinderPanel = new SmartFinderPanel();
        reservationPanel = new ReservationPanel();
        virtualQueuePanel = new VirtualQueuePanel();
        chargingSessionPanel = new ChargingSessionPanel();
        paymentPanel = new PaymentPanel();
        reportsPanel = new ReportsPanel();

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(vehiclePanel, "VEHICLES");
        contentPanel.add(campusMapPanel, "MAP");
        contentPanel.add(smartFinderPanel, "FINDER");
        contentPanel.add(reservationPanel, "RESERVATIONS");
        contentPanel.add(virtualQueuePanel, "QUEUE");
        contentPanel.add(chargingSessionPanel, "SESSIONS");
        contentPanel.add(paymentPanel, "PAYMENTS");
        contentPanel.add(reportsPanel, "REPORTS");

        if (SessionContext.isAdmin()) {
            adminStationPanel = new AdminStationPanel();
            adminPointPanel = new AdminPointPanel();
            contentPanel.add(adminStationPanel, "ADMIN_STATIONS");
            contentPanel.add(adminPointPanel, "ADMIN_POINTS");
        }

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        // Select initial card
        selectNav("DASHBOARD");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        header.setBorder(new EmptyBorder(12, 24, 12, 24));
        header.setPreferredSize(new Dimension(0, 60));

        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("⚡");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(UIUtils.COLOR_ACCENT);

        JLabel lblTitle = new JLabel("SmartCharge Campus");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("  |  Intelligent Load-Aware EV Charging");
        lblSub.setFont(UIUtils.FONT_BODY);
        lblSub.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblLogo);
        brandPanel.add(lblTitle);
        brandPanel.add(lblSub);

        // User Info & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        UserDto user = SessionContext.getCurrentUser();
        String roleBadge = user != null ? user.getRole() : "STUDENT";
        String userName = user != null ? user.getName() : "Guest";

        JLabel lblUser = new JLabel("👤 " + userName + " [" + roleBadge + "]");
        lblUser.setFont(UIUtils.FONT_BODY_BOLD);
        lblUser.setForeground(new Color(224, 242, 254));

        JButton btnLogout = UIUtils.createDangerButton("Log Out");
        btnLogout.setFont(UIUtils.FONT_SMALL);
        btnLogout.addActionListener(e -> {
            if (UIUtils.showConfirm(this, "Are you sure you want to log out?", "Logout")) {
                SessionContext.clear();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        userPanel.add(lblUser);
        userPanel.add(btnLogout);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new EmptyBorder(16, 12, 16, 12));

        addSidebarButton(sidebar, "DASHBOARD", "📊 Dashboard Overview");
        addSidebarButton(sidebar, "VEHICLES", "🚗 My Registered EVs");
        addSidebarButton(sidebar, "MAP", "🗺 Campus Map Grid");
        addSidebarButton(sidebar, "FINDER", "⚡ Smart Charger Finder");
        addSidebarButton(sidebar, "RESERVATIONS", "📅 Slot Reservations");
        addSidebarButton(sidebar, "QUEUE", "⏱ Virtual Priority Queue");
        addSidebarButton(sidebar, "SESSIONS", "🔌 Charging Sessions");
        addSidebarButton(sidebar, "PAYMENTS", "💳 Payment History");
        addSidebarButton(sidebar, "REPORTS", "📈 Reports & Analytics");

        if (SessionContext.isAdmin()) {
            sidebar.add(Box.createVerticalStrut(16));
            JLabel lblAdmin = new JLabel("  ADMINISTRATION");
            lblAdmin.setFont(UIUtils.FONT_SMALL);
            lblAdmin.setForeground(new Color(148, 163, 184));
            sidebar.add(lblAdmin);
            sidebar.add(Box.createVerticalStrut(6));

            addSidebarButton(sidebar, "ADMIN_STATIONS", "🏢 Station Management");
            addSidebarButton(sidebar, "ADMIN_POINTS", "🔌 Charger Point Config");
        }

        sidebar.add(Box.createVerticalGlue());

        // Academic Footer Badge
        JLabel lblCourse = new JLabel("CSA0905: Java Viva Project");
        lblCourse.setFont(UIUtils.FONT_SMALL);
        lblCourse.setForeground(new Color(100, 116, 139));
        lblCourse.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblCourse);

        return sidebar;
    }

    private void addSidebarButton(JPanel container, String cardName, String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_BODY);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> selectNav(cardName));
        navButtons.put(cardName, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void selectNav(String cardName) {
        currentCard = cardName;
        cardLayout.show(contentPanel, cardName);

        // Highlight active sidebar button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(cardName)) {
                entry.getValue().setBackground(UIUtils.COLOR_SIDEBAR_ACTIVE);
                entry.getValue().setForeground(Color.WHITE);
                entry.getValue().setFont(UIUtils.FONT_BODY_BOLD);
            } else {
                entry.getValue().setBackground(UIUtils.COLOR_SIDEBAR_BG);
                entry.getValue().setForeground(new Color(203, 213, 225));
                entry.getValue().setFont(UIUtils.FONT_BODY);
            }
        }

        // Trigger dynamic refresh on panel switch
        switch (cardName) {
            case "DASHBOARD" -> dashboardPanel.loadDashboardData();
            case "VEHICLES" -> vehiclePanel.loadVehicles();
            case "MAP" -> campusMapPanel.loadMapData();
            case "FINDER" -> smartFinderPanel.loadVehicles();
            case "RESERVATIONS" -> reservationPanel.loadReservations();
            case "QUEUE" -> virtualQueuePanel.loadQueue();
            case "SESSIONS" -> chargingSessionPanel.loadSessions();
            case "PAYMENTS" -> paymentPanel.loadPayments();
            case "REPORTS" -> reportsPanel.loadAllReports();
            case "ADMIN_STATIONS" -> { if (adminStationPanel != null) adminStationPanel.loadStations(); }
            case "ADMIN_POINTS" -> { if (adminPointPanel != null) adminPointPanel.loadPoints(); }
        }
    }
}
