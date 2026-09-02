package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.ReservationDto;
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

public class ReservationPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> cbFilter;
    private final JButton btnCheckIn;
    private final JButton btnCancel;
    private final JButton btnRefresh;
    private List<ReservationDto> reservationList = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM HH:mm");

    public ReservationPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Campus Charger Slot Reservations");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionControls.setOpaque(false);

        cbFilter = new JComboBox<>(new String[]{"ALL", "CONFIRMED", "ACTIVE", "COMPLETED", "CANCELLED"});
        cbFilter.addActionListener(e -> filterReservations());

        btnCheckIn = UIUtils.createPrimaryButton("▶ Check-In & Start Charging");
        btnCheckIn.addActionListener(e -> handleCheckIn());

        btnCancel = UIUtils.createDangerButton("✕ Cancel Reservation");
        btnCancel.addActionListener(e -> handleCancel());

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadReservations());

        actionControls.add(new JLabel("Filter:"));
        actionControls.add(cbFilter);
        actionControls.add(btnCheckIn);
        actionControls.add(btnCancel);
        actionControls.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Panel
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BorderLayout());

        String[] columns = {"Res ID", "Vehicle Reg", "Vehicle Model", "Station", "Point", "Power", "Start Time", "End Time", "Status", "User"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIUtils.createStyledTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(5).setMaxWidth(75);
        table.getColumnModel().getColumn(8).setCellRenderer(new UIUtils.StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        loadReservations();
    }

    public void loadReservations() {
        btnRefresh.setEnabled(false);
        SwingWorker<List<ReservationDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReservationDto> doInBackground() throws Exception {
                if (SessionContext.isAdmin()) {
                    return ApiClient.getInstance().getAllReservations();
                } else {
                    return ApiClient.getInstance().getReservationsByUser(SessionContext.getCurrentUser().getUserId());
                }
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    reservationList = get();
                    filterReservations();
                } catch (Exception ex) {
                    UIUtils.showError(ReservationPanel.this, "Failed to load reservations: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void filterReservations() {
        String filter = (String) cbFilter.getSelectedItem();
        tableModel.setRowCount(0);

        for (ReservationDto r : reservationList) {
            if ("ALL".equalsIgnoreCase(filter) || r.getStatus().equalsIgnoreCase(filter)) {
                tableModel.addRow(new Object[]{
                        r.getReservationId(),
                        r.getRegistrationNumber(),
                        r.getVehicleModel(),
                        r.getStationName(),
                        r.getPointName(),
                        (int) r.getChargerPowerKw() + " kW",
                        r.getStartTime() != null ? r.getStartTime().format(formatter) : "-",
                        r.getEndTime() != null ? r.getEndTime().format(formatter) : "-",
                        r.getStatus(),
                        r.getUserName()
                });
            }
        }
    }

    private void handleCheckIn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a CONFIRMED reservation to check-in.");
            return;
        }

        int resId = (Integer) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 8);

        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            UIUtils.showError(this, "Only CONFIRMED reservations can be checked in. Current status is " + status);
            return;
        }

        ReservationDto res = reservationList.stream().filter(r -> r.getReservationId() == resId).findFirst().orElse(null);
        if (res == null) return;

        Map<String, Object> req = new HashMap<>();
        req.put("reservationId", res.getReservationId());
        req.put("vehicleId", res.getVehicleId());
        req.put("pointId", res.getPointId());
        req.put("startingBatteryPercent", 20.0);
        req.put("targetBatteryPercent", 80.0);

        try {
            ApiClient.getInstance().checkIn(req);
            UIUtils.showSuccess(this, "Check-in successful! Charging session activated in MySQL.\nCharging point marked OCCUPIED.");
            loadReservations();
        } catch (Exception ex) {
            UIUtils.showError(this, "Check-in failed: " + ex.getMessage());
        }
    }

    private void handleCancel() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a reservation to cancel.");
            return;
        }

        int resId = (Integer) tableModel.getValueAt(row, 0);
        if (UIUtils.showConfirm(this, "Are you sure you want to cancel reservation #" + resId + "?", "Cancel Reservation")) {
            try {
                ApiClient.getInstance().cancelReservation(resId);
                UIUtils.showSuccess(this, "Reservation #" + resId + " cancelled.");
                loadReservations();
            } catch (Exception ex) {
                UIUtils.showError(this, "Cancel failed: " + ex.getMessage());
            }
        }
    }
}
