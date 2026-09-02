package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.VehicleDto;
import com.smartcharge.client.util.SessionContext;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleManagementPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JButton btnAdd;
    private final JButton btnEdit;
    private final JButton btnDelete;
    private final JButton btnRefresh;
    private List<VehicleDto> vehicleList = new ArrayList<>();

    public VehicleManagementPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Electric Vehicle Registry & CRUD Management");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionControls.setOpaque(false);

        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search registration...");
        txtSearch.addActionListener(e -> filterVehicles());

        JButton btnSearch = UIUtils.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> filterVehicles());

        btnAdd = UIUtils.createPrimaryButton("+ Register New Vehicle");
        btnAdd.addActionListener(e -> showAddVehicleDialog());

        btnEdit = UIUtils.createSecondaryButton("✎ Edit Vehicle");
        btnEdit.addActionListener(e -> showEditVehicleDialog());

        btnDelete = UIUtils.createDangerButton("🗑 Delete");
        btnDelete.addActionListener(e -> handleDeleteVehicle());

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadVehicles());

        actionControls.add(txtSearch);
        actionControls.add(btnSearch);
        actionControls.add(btnAdd);
        actionControls.add(btnEdit);
        actionControls.add(btnDelete);
        actionControls.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Panel
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BorderLayout());

        String[] columns = {"ID", "Registration No.", "Manufacturer", "Model", "Battery (kWh)", "Connector Type", "Owner"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIUtils.createStyledTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        loadVehicles();
    }

    public void loadVehicles() {
        btnRefresh.setEnabled(false);
        SwingWorker<List<VehicleDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<VehicleDto> doInBackground() throws Exception {
                if (SessionContext.isAdmin()) {
                    return ApiClient.getInstance().getAllVehicles();
                } else {
                    return ApiClient.getInstance().getVehiclesByUser(SessionContext.getCurrentUser().getUserId());
                }
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    vehicleList = get();
                    populateTable(vehicleList);
                } catch (Exception e) {
                    UIUtils.showError(VehicleManagementPanel.this, "Failed to load vehicles: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<VehicleDto> list) {
        tableModel.setRowCount(0);
        for (VehicleDto v : list) {
            tableModel.addRow(new Object[]{
                    v.getVehicleId(),
                    v.getRegistrationNumber(),
                    v.getManufacturer(),
                    v.getModel(),
                    v.getBatteryCapacityKwh() + " kWh",
                    v.getConnectorType(),
                    v.getOwnerName() != null ? v.getOwnerName() : SessionContext.getCurrentUser().getName()
            });
        }
    }

    private void filterVehicles() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            populateTable(vehicleList);
            return;
        }
        List<VehicleDto> filtered = new ArrayList<>();
        for (VehicleDto v : vehicleList) {
            if (v.getRegistrationNumber().toLowerCase().contains(query) ||
                v.getModel().toLowerCase().contains(query) ||
                v.getManufacturer().toLowerCase().contains(query)) {
                filtered.add(v);
            }
        }
        populateTable(filtered);
    }

    private void showAddVehicleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Electric Vehicle", true);
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtReg = new JTextField();
        JTextField txtMake = new JTextField();
        JTextField txtModel = new JTextField();
        JSpinner spCapacity = new JSpinner(new SpinnerNumberModel(40.0, 1.0, 300.0, 0.5));
        JComboBox<String> cbConnector = new JComboBox<>(new String[]{"CCS2", "Type 2", "CHAdeMO", "GB/T"});

        panel.add(new JLabel("Registration Number:"));
        panel.add(txtReg);
        panel.add(new JLabel("Manufacturer (Make):"));
        panel.add(txtMake);
        panel.add(new JLabel("Vehicle Model:"));
        panel.add(txtModel);
        panel.add(new JLabel("Battery Capacity (kWh):"));
        panel.add(spCapacity);
        panel.add(new JLabel("Connector Type:"));
        panel.add(cbConnector);

        JButton btnSave = UIUtils.createPrimaryButton("Save Vehicle");
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String reg = txtReg.getText().trim();
            String make = txtMake.getText().trim();
            String model = txtModel.getText().trim();
            double cap = (Double) spCapacity.getValue();
            String connector = (String) cbConnector.getSelectedItem();

            if (reg.isEmpty() || make.isEmpty() || model.isEmpty()) {
                UIUtils.showError(dialog, "All fields are required");
                return;
            }

            VehicleDto dto = new VehicleDto();
            dto.setUserId(SessionContext.getCurrentUser().getUserId());
            dto.setRegistrationNumber(reg.toUpperCase());
            dto.setManufacturer(make);
            dto.setModel(model);
            dto.setBatteryCapacityKwh(cap);
            dto.setConnectorType(connector);

            try {
                ApiClient.getInstance().registerVehicle(dto);
                UIUtils.showSuccess(this, "Vehicle " + reg.toUpperCase() + " registered in MySQL database!");
                dialog.dispose();
                loadVehicles();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Registration failed: " + ex.getMessage());
            }
        });

        panel.add(btnCancel);
        panel.add(btnSave);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showEditVehicleDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Please select a vehicle from the table to edit.");
            return;
        }

        int vehicleId = (Integer) tableModel.getValueAt(selectedRow, 0);
        VehicleDto selected = vehicleList.stream().filter(v -> v.getVehicleId() == vehicleId).findFirst().orElse(null);
        if (selected == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Vehicle ID: " + vehicleId, true);
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtReg = new JTextField(selected.getRegistrationNumber());
        JTextField txtMake = new JTextField(selected.getManufacturer());
        JTextField txtModel = new JTextField(selected.getModel());
        JSpinner spCapacity = new JSpinner(new SpinnerNumberModel(selected.getBatteryCapacityKwh(), 1.0, 300.0, 0.5));
        JComboBox<String> cbConnector = new JComboBox<>(new String[]{"CCS2", "Type 2", "CHAdeMO", "GB/T"});
        cbConnector.setSelectedItem(selected.getConnectorType());

        panel.add(new JLabel("Registration Number:"));
        panel.add(txtReg);
        panel.add(new JLabel("Manufacturer (Make):"));
        panel.add(txtMake);
        panel.add(new JLabel("Vehicle Model:"));
        panel.add(txtModel);
        panel.add(new JLabel("Battery Capacity (kWh):"));
        panel.add(spCapacity);
        panel.add(new JLabel("Connector Type:"));
        panel.add(cbConnector);

        JButton btnSave = UIUtils.createPrimaryButton("Update Vehicle");
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String reg = txtReg.getText().trim();
            String make = txtMake.getText().trim();
            String model = txtModel.getText().trim();
            double cap = (Double) spCapacity.getValue();
            String connector = (String) cbConnector.getSelectedItem();

            if (reg.isEmpty() || make.isEmpty() || model.isEmpty()) {
                UIUtils.showError(dialog, "All fields are required");
                return;
            }

            VehicleDto dto = new VehicleDto();
            dto.setVehicleId(vehicleId);
            dto.setUserId(selected.getUserId());
            dto.setRegistrationNumber(reg.toUpperCase());
            dto.setManufacturer(make);
            dto.setModel(model);
            dto.setBatteryCapacityKwh(cap);
            dto.setConnectorType(connector);

            try {
                ApiClient.getInstance().updateVehicle(vehicleId, dto);
                UIUtils.showSuccess(this, "Vehicle ID " + vehicleId + " updated in MySQL database!");
                dialog.dispose();
                loadVehicles();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Update failed: " + ex.getMessage());
            }
        });

        panel.add(btnCancel);
        panel.add(btnSave);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void handleDeleteVehicle() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Please select a vehicle from the table to delete.");
            return;
        }

        int vehicleId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String reg = (String) tableModel.getValueAt(selectedRow, 1);

        if (UIUtils.showConfirm(this, "Are you sure you want to delete vehicle " + reg + "?", "Confirm Vehicle Deletion")) {
            try {
                ApiClient.getInstance().deleteVehicle(vehicleId);
                UIUtils.showSuccess(this, "Vehicle " + reg + " deleted from MySQL database.");
                loadVehicles();
            } catch (Exception e) {
                UIUtils.showError(this, "Delete failed: " + e.getMessage());
            }
        }
    }
}
