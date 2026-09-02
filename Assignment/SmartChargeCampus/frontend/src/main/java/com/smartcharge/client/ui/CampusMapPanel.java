package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.ChargingPointDto;
import com.smartcharge.client.model.StationDto;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CampusMapPanel extends JPanel {

    private final JPanel stationsGrid;
    private final JPanel detailsPanel;
    private final JLabel lblSelectedPointName;
    private final JLabel lblSelectedStation;
    private final JLabel lblSelectedLocation;
    private final JLabel lblSelectedPower;
    private final JLabel lblSelectedConnector;
    private final JLabel lblSelectedStatus;
    private final JButton btnRefresh;

    private List<StationDto> stations = new ArrayList<>();
    private List<ChargingPointDto> points = new ArrayList<>();

    public CampusMapPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Legend and Title
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Visual Campus Charging Network Map");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("AVAILABLE", UIUtils.COLOR_AVAILABLE));
        legendPanel.add(createLegendItem("OCCUPIED", UIUtils.COLOR_OCCUPIED));
        legendPanel.add(createLegendItem("RESERVED", UIUtils.COLOR_RESERVED));
        legendPanel.add(createLegendItem("MAINTENANCE", UIUtils.COLOR_MAINTENANCE));

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh Map");
        btnRefresh.addActionListener(e -> loadMapData());
        legendPanel.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(legendPanel, BorderLayout.EAST);

        // Center Split: Map on Left (70%), Details Card on Right (30%)
        JPanel contentSplit = new JPanel(new BorderLayout(16, 0));
        contentSplit.setOpaque(false);

        stationsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        stationsGrid.setOpaque(false);

        JScrollPane scrollGrid = new JScrollPane(stationsGrid);
        scrollGrid.setBorder(BorderFactory.createEmptyBorder());
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);

        // Right details card
        detailsPanel = UIUtils.createCardPanel();
        detailsPanel.setPreferredSize(new Dimension(300, 0));
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        JLabel lblDetailHeader = new JLabel("Charger Point Details");
        lblDetailHeader.setFont(UIUtils.FONT_SUBTITLE);
        lblDetailHeader.setForeground(UIUtils.COLOR_TEXT_MAIN);

        lblSelectedPointName = new JLabel("Click any charging point to inspect");
        lblSelectedPointName.setFont(UIUtils.FONT_BODY_BOLD);
        lblSelectedPointName.setForeground(UIUtils.COLOR_PRIMARY);

        lblSelectedStation = new JLabel("Station: -");
        lblSelectedLocation = new JLabel("Location: -");
        lblSelectedPower = new JLabel("Power Rating: -");
        lblSelectedConnector = new JLabel("Connector: -");
        lblSelectedStatus = new JLabel("Status: -");

        lblSelectedStation.setFont(UIUtils.FONT_BODY);
        lblSelectedLocation.setFont(UIUtils.FONT_BODY);
        lblSelectedPower.setFont(UIUtils.FONT_BODY);
        lblSelectedConnector.setFont(UIUtils.FONT_BODY);
        lblSelectedStatus.setFont(UIUtils.FONT_BODY_BOLD);

        detailsPanel.add(lblDetailHeader);
        detailsPanel.add(Box.createVerticalStrut(12));
        detailsPanel.add(lblSelectedPointName);
        detailsPanel.add(Box.createVerticalStrut(14));
        detailsPanel.add(lblSelectedStation);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(lblSelectedLocation);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(lblSelectedPower);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(lblSelectedConnector);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(lblSelectedStatus);
        detailsPanel.add(Box.createVerticalGlue());

        contentSplit.add(scrollGrid, BorderLayout.CENTER);
        contentSplit.add(detailsPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(contentSplit, BorderLayout.CENTER);

        loadMapData();
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dot.setForeground(color);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.COLOR_TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    public void loadMapData() {
        btnRefresh.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                stations = ApiClient.getInstance().getAllStations();
                points = ApiClient.getInstance().getAllChargingPoints();
                return null;
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                renderStations();
            }
        };
        worker.execute();
    }

    private void renderStations() {
        stationsGrid.removeAll();

        Map<Integer, List<ChargingPointDto>> pointsByStation = points.stream()
                .collect(Collectors.groupingBy(ChargingPointDto::getStationId));

        for (StationDto station : stations) {
            JPanel card = UIUtils.createCardPanel();
            card.setLayout(new BorderLayout(8, 8));

            // Station Card Header
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);

            JLabel lblName = new JLabel(station.getStationName());
            lblName.setFont(UIUtils.FONT_BODY_BOLD);
            lblName.setForeground(UIUtils.COLOR_TEXT_MAIN);

            JLabel lblLoc = new JLabel(station.getCampusLocation());
            lblLoc.setFont(UIUtils.FONT_SMALL);
            lblLoc.setForeground(UIUtils.COLOR_TEXT_MUTED);

            header.add(lblName, BorderLayout.NORTH);
            header.add(lblLoc, BorderLayout.SOUTH);

            // Points Grid inside Card
            List<ChargingPointDto> stationPoints = pointsByStation.getOrDefault(station.getStationId(), new ArrayList<>());
            JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            pointsPanel.setOpaque(false);

            for (ChargingPointDto cp : stationPoints) {
                JButton pointBtn = new JButton(cp.getPointName() + " (" + (int) cp.getChargerPowerKw() + "kW)");
                pointBtn.setFont(UIUtils.FONT_SMALL);
                pointBtn.setFocusPainted(false);
                pointBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                pointBtn.setForeground(Color.WHITE);

                if ("AVAILABLE".equalsIgnoreCase(cp.getStatus())) {
                    pointBtn.setBackground(UIUtils.COLOR_AVAILABLE);
                } else if ("OCCUPIED".equalsIgnoreCase(cp.getStatus())) {
                    pointBtn.setBackground(UIUtils.COLOR_OCCUPIED);
                } else if ("RESERVED".equalsIgnoreCase(cp.getStatus())) {
                    pointBtn.setBackground(UIUtils.COLOR_RESERVED);
                } else {
                    pointBtn.setBackground(UIUtils.COLOR_MAINTENANCE);
                }

                pointBtn.addActionListener(e -> displayPointDetails(cp, station));
                pointsPanel.add(pointBtn);
            }

            JLabel lblCapacity = new JLabel("Max Capacity: " + (int) station.getMaximumLoadKw() + " kW");
            lblCapacity.setFont(UIUtils.FONT_SMALL);
            lblCapacity.setForeground(UIUtils.COLOR_TEXT_MUTED);

            card.add(header, BorderLayout.NORTH);
            card.add(pointsPanel, BorderLayout.CENTER);
            card.add(lblCapacity, BorderLayout.SOUTH);

            stationsGrid.add(card);
        }

        stationsGrid.revalidate();
        stationsGrid.repaint();
    }

    private void displayPointDetails(ChargingPointDto cp, StationDto st) {
        lblSelectedPointName.setText(cp.getPointName());
        lblSelectedStation.setText("Station: " + st.getStationName());
        lblSelectedLocation.setText("Campus Location: " + st.getCampusLocation());
        lblSelectedPower.setText("Power Rating: " + cp.getChargerPowerKw() + " kW");
        lblSelectedConnector.setText("Connector: " + cp.getConnectorType());
        lblSelectedStatus.setText("Status: " + cp.getStatus());

        if ("AVAILABLE".equalsIgnoreCase(cp.getStatus())) {
            lblSelectedStatus.setForeground(UIUtils.COLOR_AVAILABLE);
        } else if ("OCCUPIED".equalsIgnoreCase(cp.getStatus())) {
            lblSelectedStatus.setForeground(UIUtils.COLOR_OCCUPIED);
        } else if ("RESERVED".equalsIgnoreCase(cp.getStatus())) {
            lblSelectedStatus.setForeground(UIUtils.COLOR_RESERVED);
        } else {
            lblSelectedStatus.setForeground(UIUtils.COLOR_MAINTENANCE);
        }
    }
}
