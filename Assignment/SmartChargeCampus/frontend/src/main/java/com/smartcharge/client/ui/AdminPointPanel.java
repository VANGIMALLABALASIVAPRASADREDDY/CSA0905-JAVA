package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.ChargingPointDto;
import com.smartcharge.client.model.StationDto;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPointPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnAdd;
    private final JButton btnEdit;
    private final JButton btnToggleMaint;
    private final JButton btnDelete;
    private final JButton btnRefresh;
    private List<ChargingPointDto> pointList = new ArrayList<>();
    private List<StationDto> stationList = new ArrayList<>();

    public AdminPointPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Charging Point Hardware & Status Controls (Admin)");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionControls.setOpaque(false);

        btnAdd = UIUtils.createPrimaryButton("+ Add Charger Point");
        btnAdd.addActionListener(e -> showAddPointDialog());

        btnEdit = UIUtils.createSecondaryButton("✎ Edit Point");
        btnEdit.addActionListener(e -> showEditPointDialog());

        btnToggleMaint = UIUtils.createSecondaryButton("⚙ Toggle Maintenance");
        btnToggleMaint.addActionListener(e -> handleToggleMaintenance());

        btnDelete = UIUtils.createDangerButton("🗑 Delete");
        btnDelete.addActionListener(e -> handleDeletePoint());

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadPoints());

        actionControls.add(btnAdd);
        actionControls.add(btnEdit);
        actionControls.add(btnToggleMaint);
        actionControls.add(btnDelete);
        actionControls.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Panel
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BorderLayout());

        String[] columns = {"Point ID", "Point Name", "Station", "Campus Location", "Power (kW)", "Connector Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = UIUtils.createStyledTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(75);
        table.getColumnModel().getColumn(6).setCellRenderer(new UIUtils.StatusCellRenderer());

        cardPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        loadPoints();
    }

    public void loadPoints() {
        btnRefresh.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                pointList = ApiClient.getInstance().getAllChargingPoints();
                stationList = ApiClient.getInstance().getAllStations();
                return null;
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    get();
                    tableModel.setRowCount(0);
                    for (ChargingPointDto p : pointList) {
                        tableModel.addRow(new Object[]{
                                p.getPointId(),
                                p.getPointName(),
                                p.getStationName(),
                                p.getCampusLocation(),
                                (int) p.getChargerPowerKw() + " kW",
                                p.getConnectorType(),
                                p.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    UIUtils.showError(AdminPointPanel.this, "Failed to load points: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showAddPointDialog() {
        if (stationList.isEmpty()) {
            UIUtils.showError(this, "Please create at least one Station first.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Charging Point", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<StationDto> cbStation = new JComboBox<>(stationList.toArray(new StationDto[0]));
        JTextField txtName = new JTextField();
        JComboBox<Double> cbPower = new JComboBox<>(new Double[]{7.2, 11.0, 22.0});
        JComboBox<String> cbConnector = new JComboBox<>(new String[]{"CCS2", "Type 2", "CHAdeMO", "GB/T"});

        panel.add(new JLabel("Parent Station:"));
        panel.add(cbStation);
        panel.add(new JLabel("Point Name (e.g. ENG-CP04):"));
        panel.add(txtName);
        panel.add(new JLabel("Power Rating (kW):"));
        panel.add(cbPower);
        panel.add(new JLabel("Connector Type:"));
        panel.add(cbConnector);

        JButton btnSave = UIUtils.createPrimaryButton("Save Charger Point");
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                UIUtils.showError(dialog, "Point Name is required.");
                return;
            }
            StationDto st = (StationDto) cbStation.getSelectedItem();
            ChargingPointDto cp = new ChargingPointDto();
            cp.setStationId(st.getStationId());
            cp.setPointName(name.toUpperCase());
            cp.setChargerPowerKw((Double) cbPower.getSelectedItem());
            cp.setConnectorType((String) cbConnector.getSelectedItem());
            cp.setStatus("AVAILABLE");

            try {
                ApiClient.getInstance().createChargingPoint(cp);
                UIUtils.showSuccess(this, "Charging Point " + name + " added to MySQL database!");
                dialog.dispose();
                loadPoints();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Failed to create point: " + ex.getMessage());
            }
        });

        panel.add(btnCancel);
        panel.add(btnSave);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showEditPointDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a charging point to edit.");
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        ChargingPointDto selected = pointList.stream().filter(p -> p.getPointId() == id).findFirst().orElse(null);
        if (selected == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Point ID: " + id, true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField(selected.getPointName());
        JComboBox<Double> cbPower = new JComboBox<>(new Double[]{7.2, 11.0, 22.0});
        cbPower.setSelectedItem(selected.getChargerPowerKw());
        JComboBox<String> cbConnector = new JComboBox<>(new String[]{"CCS2", "Type 2", "CHAdeMO", "GB/T"});
        cbConnector.setSelectedItem(selected.getConnectorType());
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE"});
        cbStatus.setSelectedItem(selected.getStatus());

        panel.add(new JLabel("Point Name:"));
        panel.add(txtName);
        panel.add(new JLabel("Power Rating (kW):"));
        panel.add(cbPower);
        panel.add(new JLabel("Connector Type:"));
        panel.add(cbConnector);
        panel.add(new JLabel("Status:"));
        panel.add(cbStatus);

        JButton btnSave = UIUtils.createPrimaryButton("Update Point");
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            selected.setPointName(txtName.getText().trim());
            selected.setChargerPowerKw((Double) cbPower.getSelectedItem());
            selected.setConnectorType((String) cbConnector.getSelectedItem());
            selected.setStatus((String) cbStatus.getSelectedItem());

            try {
                ApiClient.getInstance().updateChargingPoint(id, selected);
                UIUtils.showSuccess(this, "Charging Point updated in MySQL!");
                dialog.dispose();
                loadPoints();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Error updating point: " + ex.getMessage());
            }
        });

        panel.add(btnCancel);
        panel.add(btnSave);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void handleToggleMaintenance() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a charging point.");
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);
        String newStatus = "MAINTENANCE".equalsIgnoreCase(currentStatus) ? "AVAILABLE" : "MAINTENANCE";

        try {
            ApiClient.getInstance().updatePointStatus(id, newStatus);
            UIUtils.showSuccess(this, "Point ID " + id + " status set to " + newStatus);
            loadPoints();
        } catch (Exception ex) {
            UIUtils.showError(this, "Failed to toggle status: " + ex.getMessage());
        }
    }

    private void handleDeletePoint() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a point to delete.");
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        if (UIUtils.showConfirm(this, "Delete charging point '" + name + "'?", "Confirm Deletion")) {
            try {
                ApiClient.getInstance().deleteChargingPoint(id);
                UIUtils.showSuccess(this, "Point deleted from MySQL.");
                loadPoints();
            } catch (Exception ex) {
                UIUtils.showError(this, "Error deleting point: " + ex.getMessage());
            }
        }
    }
}
