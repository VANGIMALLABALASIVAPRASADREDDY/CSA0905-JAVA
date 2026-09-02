package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.DashboardMetricsDto;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final JPanel metricsGrid;
    private final JProgressBar pbCampusLoad;
    private final JLabel lblLoadText;
    private final JLabel lblLoadStatus;
    private final JButton btnRefresh;

    public DashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Campus EV Infrastructure Overview");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh Live Data");
        btnRefresh.addActionListener(e -> loadDashboardData());

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);

        // Center Container
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // 1. Campus Load Card
        JPanel loadCard = UIUtils.createCardPanel();
        loadCard.setLayout(new BorderLayout(12, 12));
        loadCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel loadTitlePanel = new JPanel(new BorderLayout());
        loadTitlePanel.setOpaque(false);

        JLabel lblLoadTitle = new JLabel("REAL-TIME CAMPUS EV ELECTRICAL LOAD LIMIT (MAX: 100 kW)");
        lblLoadTitle.setFont(UIUtils.FONT_BODY_BOLD);
        lblLoadTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        lblLoadText = new JLabel("0.0 / 100.0 kW (0.0%)");
        lblLoadText.setFont(UIUtils.FONT_BODY_BOLD);
        lblLoadText.setForeground(UIUtils.COLOR_PRIMARY);

        loadTitlePanel.add(lblLoadTitle, BorderLayout.WEST);
        loadTitlePanel.add(lblLoadText, BorderLayout.EAST);

        pbCampusLoad = new JProgressBar(0, 100);
        pbCampusLoad.setValue(0);
        pbCampusLoad.setStringPainted(true);
        pbCampusLoad.setFont(UIUtils.FONT_BODY_BOLD);
        pbCampusLoad.setPreferredSize(new Dimension(pbCampusLoad.getPreferredSize().width, 24));
        pbCampusLoad.setForeground(UIUtils.COLOR_AVAILABLE);

        lblLoadStatus = new JLabel("Campus Grid Status: Normal load. Capacity is safely within maximum threshold.");
        lblLoadStatus.setFont(UIUtils.FONT_SMALL);
        lblLoadStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);

        loadCard.add(loadTitlePanel, BorderLayout.NORTH);
        loadCard.add(pbCampusLoad, BorderLayout.CENTER);
        loadCard.add(lblLoadStatus, BorderLayout.SOUTH);

        // 2. Metrics 8-Card Grid
        metricsGrid = new JPanel(new GridLayout(2, 4, 14, 14));
        metricsGrid.setOpaque(false);
        metricsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Initial empty state
        renderMetricCards(new DashboardMetricsDto());

        // 3. System Highlights Card
        JPanel infoCard = UIUtils.createCardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

        JLabel lblInfoTitle = new JLabel("SmartCharge Campus — Key Capabilities");
        lblInfoTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblInfoTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel lblItem1 = new JLabel("• Intelligent 5-Factor Recommendation Engine (Energy, Connectors, Load, Schedules, Urgency)");
        JLabel lblItem2 = new JLabel("• Real-Time Campus Load Protection (Guaranteed 100 kW Electrical Capacity Safety Ceiling)");
        JLabel lblItem3 = new JLabel("• Priority-Based Virtual Queue & Automated Promotion on Session Checkout");
        JLabel lblItem4 = new JLabel("• Pure JDBC DAO Layer (Connection, Statement, PreparedStatement, CallableStatement, ResultSet)");
        
        lblItem1.setFont(UIUtils.FONT_BODY);
        lblItem2.setFont(UIUtils.FONT_BODY);
        lblItem3.setFont(UIUtils.FONT_BODY);
        lblItem4.setFont(UIUtils.FONT_BODY);
        
        lblItem1.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblItem2.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblItem3.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblItem4.setForeground(UIUtils.COLOR_TEXT_MUTED);

        infoCard.add(lblInfoTitle);
        infoCard.add(Box.createVerticalStrut(8));
        infoCard.add(lblItem1);
        infoCard.add(Box.createVerticalStrut(4));
        infoCard.add(lblItem2);
        infoCard.add(Box.createVerticalStrut(4));
        infoCard.add(lblItem3);
        infoCard.add(Box.createVerticalStrut(4));
        infoCard.add(lblItem4);

        centerContainer.add(loadCard);
        centerContainer.add(Box.createVerticalStrut(16));
        centerContainer.add(metricsGrid);
        centerContainer.add(Box.createVerticalStrut(16));
        centerContainer.add(infoCard);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(centerContainer), BorderLayout.CENTER);

        loadDashboardData();
    }

    public void loadDashboardData() {
        btnRefresh.setEnabled(false);
        SwingWorker<DashboardMetricsDto, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardMetricsDto doInBackground() throws Exception {
                return ApiClient.getInstance().getDashboardMetrics();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    DashboardMetricsDto dto = get();
                    renderMetricCards(dto);

                    // Update Load Bar
                    double currentLoad = dto.getCurrentCampusLoadKw();
                    double maxLoad = dto.getMaxCampusLoadKw() > 0 ? dto.getMaxCampusLoadKw() : 100.0;
                    int percent = (int) Math.round((currentLoad / maxLoad) * 100.0);

                    pbCampusLoad.setValue(percent);
                    lblLoadText.setText(String.format("%.1f / %.1f kW (%d%%)", currentLoad, maxLoad, percent));

                    if (percent >= 90) {
                        pbCampusLoad.setForeground(UIUtils.COLOR_OCCUPIED);
                        lblLoadStatus.setText("CRITICAL: Campus grid load is near 100 kW maximum! New high-power allocations will be deferred to virtual queue.");
                        lblLoadStatus.setForeground(UIUtils.COLOR_OCCUPIED);
                    } else if (percent >= 70) {
                        pbCampusLoad.setForeground(UIUtils.COLOR_RESERVED);
                        lblLoadStatus.setText("MODERATE: High campus EV activity. Smart recommendation will prioritize power efficiency.");
                        lblLoadStatus.setForeground(UIUtils.COLOR_RESERVED);
                    } else {
                        pbCampusLoad.setForeground(UIUtils.COLOR_AVAILABLE);
                        lblLoadStatus.setText("NORMAL: Campus grid capacity is healthy and within optimal operational envelope.");
                        lblLoadStatus.setForeground(UIUtils.COLOR_AVAILABLE);
                    }

                } catch (Exception ex) {
                    // non-fatal
                }
            }
        };
        worker.execute();
    }

    private void renderMetricCards(DashboardMetricsDto dto) {
        metricsGrid.removeAll();

        metricsGrid.add(UIUtils.createMetricCard("Available Chargers", String.valueOf(dto.getAvailableChargers()), UIUtils.COLOR_AVAILABLE));
        metricsGrid.add(UIUtils.createMetricCard("Occupied Chargers", String.valueOf(dto.getOccupiedChargers()), UIUtils.COLOR_OCCUPIED));
        metricsGrid.add(UIUtils.createMetricCard("Reserved Chargers", String.valueOf(dto.getReservedChargers()), UIUtils.COLOR_RESERVED));
        metricsGrid.add(UIUtils.createMetricCard("Under Maintenance", String.valueOf(dto.getMaintenanceChargers()), UIUtils.COLOR_MAINTENANCE));

        metricsGrid.add(UIUtils.createMetricCard("Active Sessions", String.valueOf(dto.getActiveSessions()), UIUtils.COLOR_ACCENT));
        metricsGrid.add(UIUtils.createMetricCard("Queue Waiting", String.valueOf(dto.getQueueLength()), UIUtils.COLOR_PROMOTED));
        metricsGrid.add(UIUtils.createMetricCard("Today's Energy", String.format("%.1f kWh", dto.getTodayEnergyKwh()), UIUtils.COLOR_PRIMARY));
        metricsGrid.add(UIUtils.createMetricCard("Today's Revenue", String.format("₹%.2f", dto.getTodayRevenueInr()), UIUtils.COLOR_AVAILABLE));

        metricsGrid.revalidate();
        metricsGrid.repaint();
    }
}
