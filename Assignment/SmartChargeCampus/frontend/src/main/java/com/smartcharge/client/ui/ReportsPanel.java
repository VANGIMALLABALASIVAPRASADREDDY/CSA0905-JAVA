package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.EnergyReportDto;
import com.smartcharge.client.model.StationUtilizationDto;
import com.smartcharge.client.model.SustainabilityReportDto;
import com.smartcharge.client.model.UsageReportDto;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportsPanel extends JPanel {

    private final JTabbedPane tabbedPane;
    private final JButton btnRefresh;

    // Tab 1: Station Utilization
    private final DefaultTableModel stationTableModel;
    private final JTable stationTable;

    // Tab 2: Energy & Revenue
    private final JPanel energyMetricsGrid;
    private final DefaultTableModel energyDistTableModel;
    private final JTable energyDistTable;

    // Tab 3: Usage
    private final JPanel usageGrid;

    // Tab 4: Sustainability & SDGs
    private final JPanel sustainabilityGrid;

    public ReportsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Campus Analytics, Stored Procedure Reports & Sustainability");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh All Reports");
        btnRefresh.addActionListener(e -> loadAllReports());

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BODY_BOLD);

        // ==========================================
        // Tab 1: Station Utilization (CallableStatement)
        // ==========================================
        JPanel tab1 = new JPanel(new BorderLayout(12, 12));
        tab1.setBackground(UIUtils.COLOR_BG);
        tab1.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblTab1Note = new JLabel("★ Powered by MySQL Stored Procedure 'GetStationUtilization' invoked via plain JDBC CallableStatement");
        lblTab1Note.setFont(UIUtils.FONT_BODY_BOLD);
        lblTab1Note.setForeground(UIUtils.COLOR_PRIMARY);

        String[] stCols = {"Station ID", "Station Name", "Campus Location", "Max Load (kW)", "Total Chargers", "Active Now", "Total Sessions", "Energy (kWh)", "Revenue (INR)", "Utilization %"};
        stationTableModel = new DefaultTableModel(stCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        stationTable = UIUtils.createStyledTable();
        stationTable.setModel(stationTableModel);

        JPanel p1Card = UIUtils.createCardPanel();
        p1Card.setLayout(new BorderLayout());
        p1Card.add(new JScrollPane(stationTable), BorderLayout.CENTER);

        tab1.add(lblTab1Note, BorderLayout.NORTH);
        tab1.add(p1Card, BorderLayout.CENTER);

        // ==========================================
        // Tab 2: Energy & Revenue
        // ==========================================
        JPanel tab2 = new JPanel(new BorderLayout(14, 14));
        tab2.setBackground(UIUtils.COLOR_BG);
        tab2.setBorder(new EmptyBorder(12, 12, 12, 12));

        energyMetricsGrid = new JPanel(new GridLayout(1, 4, 12, 12));
        energyMetricsGrid.setOpaque(false);

        String[] distCols = {"Campus Charging Station", "Total Energy Delivered (kWh)"};
        energyDistTableModel = new DefaultTableModel(distCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        energyDistTable = UIUtils.createStyledTable();
        energyDistTable.setModel(energyDistTableModel);

        JPanel p2Card = UIUtils.createCardPanel();
        p2Card.setLayout(new BorderLayout());
        p2Card.add(new JScrollPane(energyDistTable), BorderLayout.CENTER);

        tab2.add(energyMetricsGrid, BorderLayout.NORTH);
        tab2.add(p2Card, BorderLayout.CENTER);

        // ==========================================
        // Tab 3: Usage Analytics
        // ==========================================
        JPanel tab3 = new JPanel(new BorderLayout(14, 14));
        tab3.setBackground(UIUtils.COLOR_BG);
        tab3.setBorder(new EmptyBorder(12, 12, 12, 12));

        usageGrid = new JPanel(new GridLayout(3, 2, 14, 14));
        usageGrid.setOpaque(false);
        tab3.add(usageGrid, BorderLayout.CENTER);

        // ==========================================
        // Tab 4: Sustainability & SDGs
        // ==========================================
        JPanel tab4 = new JPanel(new BorderLayout(14, 14));
        tab4.setBackground(UIUtils.COLOR_BG);
        tab4.setBorder(new EmptyBorder(12, 12, 12, 12));

        sustainabilityGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        sustainabilityGrid.setOpaque(false);

        JPanel sdgCard = UIUtils.createCardPanel();
        sdgCard.setLayout(new BoxLayout(sdgCard, BoxLayout.Y_AXIS));

        JLabel lblSdg = new JLabel("Alignment with UN Sustainable Development Goals (SDGs):");
        lblSdg.setFont(UIUtils.FONT_SUBTITLE);
        lblSdg.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel lblSdg7 = new JLabel("• SDG 7 (Affordable & Clean Energy): Real-time load-aware EV scheduling maximizes energy efficiency.");
        JLabel lblSdg9 = new JLabel("• SDG 9 (Industry, Innovation & Infrastructure): Smart campus grid protection prevents transformer overload.");
        JLabel lblSdg11 = new JLabel("• SDG 11 (Sustainable Cities & Communities): Virtual queue and clean EV transit reduce campus congestion.");

        lblSdg7.setFont(UIUtils.FONT_BODY);
        lblSdg9.setFont(UIUtils.FONT_BODY);
        lblSdg11.setFont(UIUtils.FONT_BODY);

        sdgCard.add(lblSdg);
        sdgCard.add(Box.createVerticalStrut(8));
        sdgCard.add(lblSdg7);
        sdgCard.add(Box.createVerticalStrut(4));
        sdgCard.add(lblSdg9);
        sdgCard.add(Box.createVerticalStrut(4));
        sdgCard.add(lblSdg11);

        tab4.add(sustainabilityGrid, BorderLayout.CENTER);
        tab4.add(sdgCard, BorderLayout.SOUTH);

        tabbedPane.addTab("Station Utilization (Procedure)", tab1);
        tabbedPane.addTab("Energy & Revenue", tab2);
        tabbedPane.addTab("Usage Analytics", tab3);
        tabbedPane.addTab("Sustainability & SDGs", tab4);

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        loadAllReports();
    }

    public void loadAllReports() {
        btnRefresh.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<StationUtilizationDto> stationUtils;
            private EnergyReportDto energyReport;
            private UsageReportDto usageReport;
            private SustainabilityReportDto sustReport;

            @Override
            protected Void doInBackground() throws Exception {
                stationUtils = ApiClient.getInstance().getStationUtilization();
                energyReport = ApiClient.getInstance().getEnergyReport();
                usageReport = ApiClient.getInstance().getUsageReport();
                sustReport = ApiClient.getInstance().getSustainabilityReport();
                return null;
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    get();

                    // 1. Populate Station Utilization Table
                    stationTableModel.setRowCount(0);
                    if (stationUtils != null) {
                        for (StationUtilizationDto s : stationUtils) {
                            stationTableModel.addRow(new Object[]{
                                    s.getStationId(),
                                    s.getStationName(),
                                    s.getCampusLocation(),
                                    (int) s.getMaximumLoadKw() + " kW",
                                    s.getTotalPoints(),
                                    s.getActivePoints(),
                                    s.getTotalSessions(),
                                    String.format("%.2f", s.getTotalEnergyKwh()),
                                    String.format("₹%.2f", s.getTotalRevenue()),
                                    String.format("%.1f%%", s.getUtilizationPercent())
                            });
                        }
                    }

                    // 2. Populate Energy & Revenue
                    energyMetricsGrid.removeAll();
                    if (energyReport != null) {
                        energyMetricsGrid.add(UIUtils.createMetricCard("Today Energy", String.format("%.1f kWh", energyReport.getTodayEnergyKwh()), UIUtils.COLOR_PRIMARY));
                        energyMetricsGrid.add(UIUtils.createMetricCard("Week Energy", String.format("%.1f kWh", energyReport.getWeekEnergyKwh()), UIUtils.COLOR_ACCENT));
                        energyMetricsGrid.add(UIUtils.createMetricCard("Month Energy", String.format("%.1f kWh", energyReport.getMonthEnergyKwh()), UIUtils.COLOR_PROMOTED));
                        energyMetricsGrid.add(UIUtils.createMetricCard("Total Revenue", String.format("₹%.2f", energyReport.getTotalRevenue()), UIUtils.COLOR_AVAILABLE));

                        energyDistTableModel.setRowCount(0);
                        if (energyReport.getStationEnergyDistribution() != null) {
                            for (Map.Entry<String, Double> e : energyReport.getStationEnergyDistribution().entrySet()) {
                                energyDistTableModel.addRow(new Object[]{e.getKey(), String.format("%.2f kWh", e.getValue())});
                            }
                        }
                    }
                    energyMetricsGrid.revalidate();
                    energyMetricsGrid.repaint();

                    // 3. Populate Usage Analytics
                    usageGrid.removeAll();
                    if (usageReport != null) {
                        usageGrid.add(UIUtils.createMetricCard("Total Sessions", String.valueOf(usageReport.getTotalSessions()), UIUtils.COLOR_ACCENT));
                        usageGrid.add(UIUtils.createMetricCard("Avg Duration", String.format("%.1f mins", usageReport.getAverageDurationMinutes()), UIUtils.COLOR_PRIMARY));
                        usageGrid.add(UIUtils.createMetricCard("Most Used Station", usageReport.getMostUsedStation(), UIUtils.COLOR_AVAILABLE));
                        usageGrid.add(UIUtils.createMetricCard("Top Charger Point", usageReport.getMostUsedCharger(), UIUtils.COLOR_PROMOTED));
                        usageGrid.add(UIUtils.createMetricCard("Peak Period", usageReport.getPeakChargingPeriod(), UIUtils.COLOR_RESERVED));
                        usageGrid.add(UIUtils.createMetricCard("Avg Wait Time", String.format("%.1f mins", usageReport.getAverageWaitingTimeMinutes()), UIUtils.COLOR_MAINTENANCE));
                    }
                    usageGrid.revalidate();
                    usageGrid.repaint();

                    // 4. Populate Sustainability
                    sustainabilityGrid.removeAll();
                    if (sustReport != null) {
                        sustainabilityGrid.add(UIUtils.createMetricCard("Total EV Clean Energy Delivered", String.format("%.2f kWh", sustReport.getTotalEnergyDeliveredKwh()), UIUtils.COLOR_AVAILABLE));
                        sustainabilityGrid.add(UIUtils.createMetricCard("CO2 Emissions Avoided", String.format("%.2f kg CO2", sustReport.getCo2SavedKg()), UIUtils.COLOR_PRIMARY));
                        sustainabilityGrid.add(UIUtils.createMetricCard("Avg Charger Utilization", String.format("%.1f%%", sustReport.getAverageChargerUtilizationPercent()), UIUtils.COLOR_ACCENT));
                        sustainabilityGrid.add(UIUtils.createMetricCard("Campus Grid Peak Load", String.format("%.1f / %.1f kW", sustReport.getPeakCampusChargingLoadKw(), sustReport.getMaxCampusCapacityKw()), UIUtils.COLOR_RESERVED));
                    }
                    sustainabilityGrid.revalidate();
                    sustainabilityGrid.repaint();

                } catch (Exception ex) {
                    UIUtils.showError(ReportsPanel.this, "Failed to load analytical reports: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
