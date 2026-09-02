package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.CandidateScoreDto;
import com.smartcharge.client.model.RecommendationResponseDto;
import com.smartcharge.client.model.VehicleDto;
import com.smartcharge.client.util.SessionContext;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartFinderPanel extends JPanel {

    private final JComboBox<VehicleDto> cbVehicles;
    private final JSlider slCurrentBattery;
    private final JSlider slTargetBattery;
    private final JLabel lblCurrentVal;
    private final JLabel lblTargetVal;
    private final JComboBox<String> cbLocation;
    private final JComboBox<String> cbDeparture;
    private final JButton btnFind;

    // Recommendation Output Card Components
    private final JPanel resultCard;
    private final JLabel lblResultTitle;
    private final JLabel lblScore;
    private final JLabel lblPower;
    private final JLabel lblEnergy;
    private final JLabel lblDuration;
    private final JLabel lblCost;
    private final JLabel lblLoadAfter;
    private final JPanel matchReasonsPanel;
    private final JButton btnReserve;
    private final JButton btnJoinQueue;

    private RecommendationResponseDto lastRecommendation;
    private List<VehicleDto> userVehicles = new ArrayList<>();

    public SmartFinderPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Title
        JLabel lblTitle = new JLabel("Intelligent Load-Aware Smart Charger Finder");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        // Center Split: Inputs on Left (45%), Smart Recommendation Output on Right (55%)
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        splitPanel.setOpaque(false);

        // 1. Inputs Card
        JPanel inputCard = UIUtils.createCardPanel();
        inputCard.setLayout(new BoxLayout(inputCard, BoxLayout.Y_AXIS));

        JLabel lblInputHeader = new JLabel("Charging Parameters & Constraints");
        lblInputHeader.setFont(UIUtils.FONT_SUBTITLE);
        lblInputHeader.setForeground(UIUtils.COLOR_TEXT_MAIN);

        // Vehicle Select
        JLabel lblVeh = new JLabel("Select Registered EV:");
        lblVeh.setFont(UIUtils.FONT_BODY_BOLD);
        cbVehicles = new JComboBox<>();
        cbVehicles.setFont(UIUtils.FONT_BODY);
        cbVehicles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Current Battery Slider
        JPanel currPanel = new JPanel(new BorderLayout());
        currPanel.setOpaque(false);
        JLabel lblCurr = new JLabel("Current Battery State:");
        lblCurr.setFont(UIUtils.FONT_BODY_BOLD);
        lblCurrentVal = new JLabel("25%");
        lblCurrentVal.setFont(UIUtils.FONT_BODY_BOLD);
        lblCurrentVal.setForeground(UIUtils.COLOR_PRIMARY);
        currPanel.add(lblCurr, BorderLayout.WEST);
        currPanel.add(lblCurrentVal, BorderLayout.EAST);

        slTargetBattery = new JSlider(0, 100, 80);
        slTargetBattery.setMajorTickSpacing(25);
        slTargetBattery.setMinorTickSpacing(5);
        slTargetBattery.setPaintTicks(true);
        slTargetBattery.setPaintLabels(true);

        slCurrentBattery = new JSlider(0, 100, 25);
        slCurrentBattery.setMajorTickSpacing(25);
        slCurrentBattery.setMinorTickSpacing(5);
        slCurrentBattery.setPaintTicks(true);
        slCurrentBattery.setPaintLabels(true);
        slCurrentBattery.addChangeListener(e -> {
            lblCurrentVal.setText(slCurrentBattery.getValue() + "%");
            if (slTargetBattery != null && slTargetBattery.getValue() <= slCurrentBattery.getValue()) {
                slTargetBattery.setValue(Math.min(100, slCurrentBattery.getValue() + 20));
            }
        });

        // Target Battery Slider
        JPanel targetPanel = new JPanel(new BorderLayout());
        targetPanel.setOpaque(false);
        JLabel lblTgt = new JLabel("Target Desired Battery:");
        lblTgt.setFont(UIUtils.FONT_BODY_BOLD);
        lblTargetVal = new JLabel("80%");
        lblTargetVal.setFont(UIUtils.FONT_BODY_BOLD);
        lblTargetVal.setForeground(UIUtils.COLOR_AVAILABLE);
        targetPanel.add(lblTgt, BorderLayout.WEST);
        targetPanel.add(lblTargetVal, BorderLayout.EAST);

        slTargetBattery.addChangeListener(e -> lblTargetVal.setText(slTargetBattery.getValue() + "%"));

        // Location Preference
        JLabel lblLoc = new JLabel("Preferred Campus Location:");
        lblLoc.setFont(UIUtils.FONT_BODY_BOLD);
        cbLocation = new JComboBox<>(new String[]{
                "Engineering Block", "Main Block", "Library", "Boys Hostel", "Girls Hostel", "Main Parking"
        });
        cbLocation.setFont(UIUtils.FONT_BODY);
        cbLocation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Expected Departure
        JLabel lblDep = new JLabel("Expected Campus Departure:");
        lblDep.setFont(UIUtils.FONT_BODY_BOLD);
        cbDeparture = new JComboBox<>(new String[]{
                "In 1 Hour", "In 2 Hours", "In 3 Hours", "In 4 Hours", "In 6 Hours", "Evening (End of Day)"
        });
        cbDeparture.setFont(UIUtils.FONT_BODY);
        cbDeparture.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnFind = UIUtils.createPrimaryButton("⚡ FIND BEST CHARGER");
        btnFind.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnFind.addActionListener(e -> executeSmartRecommendation());

        inputCard.add(lblInputHeader);
        inputCard.add(Box.createVerticalStrut(14));
        inputCard.add(lblVeh);
        inputCard.add(Box.createVerticalStrut(4));
        inputCard.add(cbVehicles);
        inputCard.add(Box.createVerticalStrut(12));
        inputCard.add(currPanel);
        inputCard.add(slCurrentBattery);
        inputCard.add(Box.createVerticalStrut(12));
        inputCard.add(targetPanel);
        inputCard.add(slTargetBattery);
        inputCard.add(Box.createVerticalStrut(12));
        inputCard.add(lblLoc);
        inputCard.add(Box.createVerticalStrut(4));
        inputCard.add(cbLocation);
        inputCard.add(Box.createVerticalStrut(12));
        inputCard.add(lblDep);
        inputCard.add(Box.createVerticalStrut(4));
        inputCard.add(cbDeparture);
        inputCard.add(Box.createVerticalStrut(18));
        inputCard.add(btnFind);

        // 2. Output Card
        resultCard = UIUtils.createCardPanel();
        resultCard.setLayout(new BoxLayout(resultCard, BoxLayout.Y_AXIS));

        lblResultTitle = new JLabel("Recommendation Analysis Ready");
        lblResultTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblResultTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        lblScore = new JLabel("Score: -- / 100");
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblScore.setForeground(UIUtils.COLOR_PRIMARY);

        JPanel metricsRow1 = new JPanel(new GridLayout(1, 2, 8, 8));
        metricsRow1.setOpaque(false);
        lblPower = new JLabel("Power: -");
        lblEnergy = new JLabel("Energy Required: -");
        lblPower.setFont(UIUtils.FONT_BODY);
        lblEnergy.setFont(UIUtils.FONT_BODY);
        metricsRow1.add(lblPower);
        metricsRow1.add(lblEnergy);

        JPanel metricsRow2 = new JPanel(new GridLayout(1, 2, 8, 8));
        metricsRow2.setOpaque(false);
        lblDuration = new JLabel("Est. Duration: -");
        lblCost = new JLabel("Est. Cost: -");
        lblDuration.setFont(UIUtils.FONT_BODY);
        lblCost.setFont(UIUtils.FONT_BODY);
        metricsRow2.add(lblDuration);
        metricsRow2.add(lblCost);

        lblLoadAfter = new JLabel("Campus Load After Allocation: -");
        lblLoadAfter.setFont(UIUtils.FONT_BODY_BOLD);
        lblLoadAfter.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel lblReasonsTitle = new JLabel("WHY THIS CHARGER?");
        lblReasonsTitle.setFont(UIUtils.FONT_BODY_BOLD);
        lblReasonsTitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        matchReasonsPanel = new JPanel();
        matchReasonsPanel.setLayout(new BoxLayout(matchReasonsPanel, BoxLayout.Y_AXIS));
        matchReasonsPanel.setOpaque(false);

        btnReserve = UIUtils.createPrimaryButton("✓ RESERVE THIS CHARGER NOW");
        btnReserve.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnReserve.setEnabled(false);
        btnReserve.addActionListener(e -> executeReservation());

        btnJoinQueue = UIUtils.createSecondaryButton("⏱ Join Virtual Queue");
        btnJoinQueue.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnJoinQueue.setVisible(false);
        btnJoinQueue.addActionListener(e -> executeJoinQueue());

        resultCard.add(lblResultTitle);
        resultCard.add(Box.createVerticalStrut(10));
        resultCard.add(lblScore);
        resultCard.add(Box.createVerticalStrut(14));
        resultCard.add(metricsRow1);
        resultCard.add(Box.createVerticalStrut(6));
        resultCard.add(metricsRow2);
        resultCard.add(Box.createVerticalStrut(10));
        resultCard.add(lblLoadAfter);
        resultCard.add(Box.createVerticalStrut(14));
        resultCard.add(lblReasonsTitle);
        resultCard.add(Box.createVerticalStrut(6));
        resultCard.add(matchReasonsPanel);
        resultCard.add(Box.createVerticalGlue());
        resultCard.add(btnReserve);
        resultCard.add(Box.createVerticalStrut(6));
        resultCard.add(btnJoinQueue);

        splitPanel.add(new JScrollPane(inputCard));
        splitPanel.add(new JScrollPane(resultCard));

        add(lblTitle, BorderLayout.NORTH);
        add(splitPanel, BorderLayout.CENTER);

        loadVehicles();
    }

    public void loadVehicles() {
        SwingWorker<List<VehicleDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<VehicleDto> doInBackground() throws Exception {
                return ApiClient.getInstance().getVehiclesByUser(SessionContext.getCurrentUser().getUserId());
            }

            @Override
            protected void done() {
                try {
                    userVehicles = get();
                    cbVehicles.removeAllItems();
                    for (VehicleDto v : userVehicles) {
                        cbVehicles.addItem(v);
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void executeSmartRecommendation() {
        VehicleDto selectedVeh = (VehicleDto) cbVehicles.getSelectedItem();
        if (selectedVeh == null) {
            UIUtils.showError(this, "Please register/select a vehicle first in 'My Vehicles'.");
            return;
        }

        int curr = slCurrentBattery.getValue();
        int target = slTargetBattery.getValue();
        if (target <= curr) {
            UIUtils.showError(this, "Target battery must be greater than current battery percentage.");
            return;
        }

        btnFind.setEnabled(false);

        LocalDateTime startTime = LocalDateTime.now();
        int departureHours = cbDeparture.getSelectedIndex() + 1;
        LocalDateTime departureTime = startTime.plusHours(departureHours);

        Map<String, Object> req = new HashMap<>();
        req.put("vehicleId", selectedVeh.getVehicleId());
        req.put("currentBatteryPercent", (double) curr);
        req.put("targetBatteryPercent", (double) target);
        req.put("preferredLocation", (String) cbLocation.getSelectedItem());
        req.put("requestedStartTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        req.put("expectedDepartureTime", departureTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        SwingWorker<RecommendationResponseDto, Void> worker = new SwingWorker<>() {
            @Override
            protected RecommendationResponseDto doInBackground() throws Exception {
                return ApiClient.getInstance().getRecommendation(req);
            }

            @Override
            protected void done() {
                btnFind.setEnabled(true);
                try {
                    lastRecommendation = get();
                    renderRecommendationResult(lastRecommendation);
                } catch (Exception ex) {
                    UIUtils.showError(SmartFinderPanel.this, "Recommendation error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderRecommendationResult(RecommendationResponseDto res) {
        matchReasonsPanel.removeAll();

        if (res.isMatchFound() && res.getBestCharger() != null) {
            CandidateScoreDto best = res.getBestCharger();
            lblResultTitle.setText("BEST CHARGER: " + best.getStationName() + " – " + best.getPointName());
            lblScore.setText("Score: " + (int) best.getTotalScore() + " / 100");
            lblPower.setText("Power: " + best.getChargerPowerKw() + " kW (" + best.getConnectorType() + ")");
            lblEnergy.setText("Energy Required: " + best.getRequiredEnergyKwh() + " kWh");
            lblDuration.setText("Est. Duration: " + best.getEstimatedDurationMinutes() + " mins");
            lblCost.setText(String.format("Est. Cost: ₹%.2f", best.getEstimatedCost()));
            lblLoadAfter.setText(String.format("Campus Load After Allocation: %.1f / %.1f kW",
                    best.getCampusLoadAfterAllocationKw(), res.getMaxCampusLoadKw()));

            if (best.getMatchReasons() != null) {
                for (String reason : best.getMatchReasons()) {
                    JLabel lblR = new JLabel("✓ " + reason);
                    lblR.setFont(UIUtils.FONT_BODY);
                    lblR.setForeground(new Color(22, 101, 52));
                    matchReasonsPanel.add(lblR);
                    matchReasonsPanel.add(Box.createVerticalStrut(4));
                }
            }

            btnReserve.setEnabled(true);
            btnReserve.setVisible(true);
            btnJoinQueue.setVisible(false);
        } else {
            lblResultTitle.setText("No Immediate Charger Available");
            lblScore.setText("Capacity Constraint");
            lblScore.setForeground(UIUtils.COLOR_OCCUPIED);
            lblPower.setText("Reason: " + res.getMessage());
            lblEnergy.setText("");
            lblDuration.setText("");
            lblCost.setText("");
            lblLoadAfter.setText(String.format("Current Campus Load: %.1f / %.1f kW", res.getCurrentCampusLoadKw(), res.getMaxCampusLoadKw()));

            JLabel lblQ = new JLabel("• All compatible chargers are currently occupied or load limit is reached.");
            JLabel lblQ2 = new JLabel("• Join the priority virtual queue to receive automatic charger promotion.");
            lblQ.setFont(UIUtils.FONT_BODY);
            lblQ2.setFont(UIUtils.FONT_BODY);
            lblQ.setForeground(UIUtils.COLOR_TEXT_MUTED);
            lblQ2.setForeground(UIUtils.COLOR_PRIMARY);

            matchReasonsPanel.add(lblQ);
            matchReasonsPanel.add(Box.createVerticalStrut(4));
            matchReasonsPanel.add(lblQ2);

            btnReserve.setEnabled(false);
            btnReserve.setVisible(false);
            btnJoinQueue.setVisible(true);
        }

        resultCard.revalidate();
        resultCard.repaint();
    }

    private void executeReservation() {
        if (lastRecommendation == null || lastRecommendation.getBestCharger() == null) return;
        CandidateScoreDto best = lastRecommendation.getBestCharger();
        VehicleDto selectedVeh = (VehicleDto) cbVehicles.getSelectedItem();
        if (selectedVeh == null) return;

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(best.getEstimatedDurationMinutes());

        Map<String, Object> req = new HashMap<>();
        req.put("userId", SessionContext.getCurrentUser().getUserId());
        req.put("vehicleId", selectedVeh.getVehicleId());
        req.put("pointId", best.getPointId());
        req.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        req.put("endTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        btnReserve.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ApiClient.getInstance().createReservation(req);
                return null;
            }

            @Override
            protected void done() {
                btnReserve.setEnabled(true);
                try {
                    get();
                    UIUtils.showSuccess(SmartFinderPanel.this, "Reservation Confirmed for " + best.getPointName() + " (" + best.getStationName() + ")!\nSaved in MySQL database.");
                } catch (Exception ex) {
                    UIUtils.showError(SmartFinderPanel.this, "Reservation failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void executeJoinQueue() {
        VehicleDto selectedVeh = (VehicleDto) cbVehicles.getSelectedItem();
        if (selectedVeh == null) return;

        LocalDateTime requestedTime = LocalDateTime.now();
        int departureHours = cbDeparture.getSelectedIndex() + 1;
        LocalDateTime departureTime = requestedTime.plusHours(departureHours);

        Map<String, Object> req = new HashMap<>();
        req.put("userId", SessionContext.getCurrentUser().getUserId());
        req.put("vehicleId", selectedVeh.getVehicleId());
        req.put("preferredLocation", (String) cbLocation.getSelectedItem());
        req.put("currentBatteryPercent", (double) slCurrentBattery.getValue());
        req.put("targetBatteryPercent", (double) slTargetBattery.getValue());
        req.put("requestedTime", requestedTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        req.put("departureTime", departureTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        btnJoinQueue.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ApiClient.getInstance().joinQueue(req);
                return null;
            }

            @Override
            protected void done() {
                btnJoinQueue.setEnabled(true);
                try {
                    get();
                    UIUtils.showSuccess(SmartFinderPanel.this, "Successfully joined Campus Virtual Queue!\nYou will be automatically promoted when a charger completes.");
                } catch (Exception ex) {
                    UIUtils.showError(SmartFinderPanel.this, "Failed to join queue: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
