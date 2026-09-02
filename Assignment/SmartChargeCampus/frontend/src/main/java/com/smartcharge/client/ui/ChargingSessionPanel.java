package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.ChargingSessionDto;
import com.smartcharge.client.util.SessionContext;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChargingSessionPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnCheckOut;
    private final JButton btnPay;
    private final JButton btnRefresh;
    private List<ChargingSessionDto> sessionList = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM HH:mm");

    public ChargingSessionPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Live EV Charging Sessions & Billing");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionControls.setOpaque(false);

        btnCheckOut = UIUtils.createPrimaryButton("⏹ End Session & Check-Out");
        btnCheckOut.addActionListener(e -> handleCheckOut());

        btnPay = UIUtils.createSecondaryButton("💳 Pay Due Balance");
        btnPay.addActionListener(e -> handlePay());

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadSessions());

        actionControls.add(btnCheckOut);
        actionControls.add(btnPay);
        actionControls.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Panel
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BorderLayout());

        String[] columns = {"Session ID", "Vehicle Reg", "Point Name", "Station", "Check-In", "Check-Out", "Battery (%)", "Duration", "Energy (kWh)", "Total Cost", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIUtils.createStyledTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(75);
        table.getColumnModel().getColumn(10).setCellRenderer(new UIUtils.StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        loadSessions();
    }

    public void loadSessions() {
        btnRefresh.setEnabled(false);
        SwingWorker<List<ChargingSessionDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ChargingSessionDto> doInBackground() throws Exception {
                if (SessionContext.isAdmin()) {
                    return ApiClient.getInstance().getAllSessions();
                } else {
                    return ApiClient.getInstance().getSessionsByUser(SessionContext.getCurrentUser().getUserId());
                }
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    sessionList = get();
                    tableModel.setRowCount(0);

                    for (ChargingSessionDto s : sessionList) {
                        String batteryStr = (int) s.getStartingBatteryPercent() + "% → " +
                                (s.getFinalBatteryPercent() != null ? (int) s.getFinalBatteryPercent().doubleValue() + "%" : "--");

                        tableModel.addRow(new Object[]{
                                s.getSessionId(),
                                s.getRegistrationNumber(),
                                s.getPointName(),
                                s.getStationName(),
                                s.getCheckInTime() != null ? s.getCheckInTime().format(formatter) : "-",
                                s.getCheckOutTime() != null ? s.getCheckOutTime().format(formatter) : "-",
                                batteryStr,
                                s.getDurationMinutes() > 0 ? s.getDurationMinutes() + " mins" : "Active",
                                String.format("%.2f kWh", s.getEnergyConsumedKwh()),
                                String.format("₹%.2f", s.getTotalCost()),
                                s.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    UIUtils.showError(ChargingSessionPanel.this, "Failed to load sessions: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleCheckOut() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select an ACTIVE charging session to check out.");
            return;
        }

        int sessionId = (Integer) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 10);

        if (!"ACTIVE".equalsIgnoreCase(status)) {
            UIUtils.showError(this, "Only ACTIVE sessions can be checked out.");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter Final Battery % achieved:", "Charging Check-Out", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            double finalBattery = Double.parseDouble(input.trim());
            if (finalBattery < 0 || finalBattery > 100) {
                UIUtils.showError(this, "Battery percentage must be between 0% and 100%");
                return;
            }

            ChargingSessionDto completed = ApiClient.getInstance().checkOut(sessionId, finalBattery);
            UIUtils.showSuccess(this, String.format("Session #%d Completed!\nEnergy Consumed: %.2f kWh\nDuration: %d minutes\nTotal Cost: ₹%.2f\n\nCharger is now AVAILABLE for next vehicle.",
                    completed.getSessionId(), completed.getEnergyConsumedKwh(), completed.getDurationMinutes(), completed.getTotalCost()));
            loadSessions();
        } catch (NumberFormatException nfe) {
            UIUtils.showError(this, "Please enter a valid numeric percentage.");
        } catch (Exception ex) {
            UIUtils.showError(this, "Check-out failed: " + ex.getMessage());
        }
    }

    private void handlePay() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a COMPLETED session from the table to pay.");
            return;
        }

        int sessionId = (Integer) tableModel.getValueAt(row, 0);
        ChargingSessionDto selected = sessionList.stream().filter(s -> s.getSessionId() == sessionId).findFirst().orElse(null);
        if (selected == null) return;

        if (!"COMPLETED".equalsIgnoreCase(selected.getStatus())) {
            UIUtils.showError(this, "Session must be COMPLETED before making payment.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Simulate Payment Settlement", true);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Session ID:"));
        panel.add(new JLabel("#" + sessionId));

        panel.add(new JLabel("Amount Due:"));
        panel.add(new JLabel(String.format("₹%.2f", selected.getTotalCost())));

        panel.add(new JLabel("Payment Method:"));
        JComboBox<String> cbMethod = new JComboBox<>(new String[]{"UPI", "CARD", "CAMPUS_WALLET", "CASH"});
        panel.add(cbMethod);

        JButton btnPayNow = UIUtils.createPrimaryButton("Confirm Payment");
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnPayNow.addActionListener(e -> {
            Map<String, Object> req = new HashMap<>();
            req.put("sessionId", sessionId);
            req.put("amount", selected.getTotalCost());
            req.put("paymentMethod", (String) cbMethod.getSelectedItem());

            try {
                ApiClient.getInstance().processPayment(req);
                UIUtils.showSuccess(this, "Payment of ₹" + selected.getTotalCost() + " recorded successfully in MySQL!");
                dialog.dispose();
                loadSessions();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Payment failed: " + ex.getMessage());
            }
        });

        panel.add(btnCancel);
        panel.add(btnPayNow);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
