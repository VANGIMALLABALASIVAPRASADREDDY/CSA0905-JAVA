package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.StationDto;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminStationPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnAdd;
    private final JButton btnEdit;
    private final JButton btnDelete;
    private final JButton btnRefresh;
    private List<StationDto> stationList = new ArrayList<>();

    public AdminStationPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Campus Charging Station Infrastructure (Admin)");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionControls.setOpaque(false);

        btnAdd = UIUtils.createPrimaryButton("+ Add Station");
        btnAdd.addActionListener(e -> showAddStationDialog());

        btnEdit = UIUtils.createSecondaryButton("✎ Edit Station");
        btnEdit.addActionListener(e -> showEditStationDialog());

        btnDelete = UIUtils.createDangerButton("🗑 Delete");
        btnDelete.addActionListener(e -> handleDeleteStation());

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadStations());

        actionControls.add(btnAdd);
        actionControls.add(btnEdit);
        actionControls.add(btnDelete);
        actionControls.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Panel
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BorderLayout());

        String[] columns = {"Station ID", "Station Name", "Campus Location", "Max Capacity (kW)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = UIUtils.createStyledTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusCellRenderer());

        cardPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        loadStations();
    }

    public void loadStations() {
        btnRefresh.setEnabled(false);
        SwingWorker<List<StationDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StationDto> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllStations();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    stationList = get();
                    tableModel.setRowCount(0);
                    for (StationDto s : stationList) {
                        tableModel.addRow(new Object[]{
                                s.getStationId(),
                                s.getStationName(),
                                s.getCampusLocation(),
                                (int) s.getMaximumLoadKw() + " kW",
                                s.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    UIUtils.showError(AdminStationPanel.this, "Failed to load stations: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showAddStationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Charging Station", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField();
        JTextField txtLoc = new JTextField();
        JSpinner spCap = new JSpinner(new SpinnerNumberModel(50.0, 10.0, 200.0, 5.0));

        panel.add(new JLabel("Station Name:"));
        panel.add(txtName);
        panel.add(new JLabel("Campus Location:"));
        panel.add(txtLoc);
        panel.add(new JLabel("Max Power (kW):"));
        panel.add(spCap);

        JButton btnSave = UIUtils.createPrimaryButton("Save Station");
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String loc = txtLoc.getText().trim();
            if (name.isEmpty() || loc.isEmpty()) {
                UIUtils.showError(dialog, "Name and Location are required.");
                return;
            }
            StationDto s = new StationDto();
            s.setStationName(name);
            s.setCampusLocation(loc);
            s.setMaximumLoadKw((Double) spCap.getValue());
            s.setStatus("ACTIVE");

            try {
                ApiClient.getInstance().createStation(s);
                UIUtils.showSuccess(this, "Station " + name + " created in MySQL!");
                dialog.dispose();
                loadStations();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Error creating station: " + ex.getMessage());
            }
        });

        panel.add(btnCancel);
        panel.add(btnSave);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showEditStationDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a station to edit.");
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        StationDto selected = stationList.stream().filter(s -> s.getStationId() == id).findFirst().orElse(null);
        if (selected == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Station ID: " + id, true);
        dialog.setSize(400, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField(selected.getStationName());
        JTextField txtLoc = new JTextField(selected.getCampusLocation());
        JSpinner spCap = new JSpinner(new SpinnerNumberModel(selected.getMaximumLoadKw(), 10.0, 200.0, 5.0));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ACTIVE", "MAINTENANCE", "INACTIVE"});
        cbStatus.setSelectedItem(selected.getStatus());

        panel.add(new JLabel("Station Name:"));
        panel.add(txtName);
        panel.add(new JLabel("Campus Location:"));
        panel.add(txtLoc);
        panel.add(new JLabel("Max Power (kW):"));
        panel.add(spCap);
        panel.add(new JLabel("Status:"));
        panel.add(cbStatus);

        JButton btnSave = UIUtils.createPrimaryButton("Update Station");
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            selected.setStationName(txtName.getText().trim());
            selected.setCampusLocation(txtLoc.getText().trim());
            selected.setMaximumLoadKw((Double) spCap.getValue());
            selected.setStatus((String) cbStatus.getSelectedItem());

            try {
                ApiClient.getInstance().updateStation(id, selected);
                UIUtils.showSuccess(this, "Station updated successfully!");
                dialog.dispose();
                loadStations();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Error updating station: " + ex.getMessage());
            }
        });

        panel.add(btnCancel);
        panel.add(btnSave);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void handleDeleteStation() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a station to delete.");
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        if (UIUtils.showConfirm(this, "Delete station '" + name + "' and its points?", "Confirm Deletion")) {
            try {
                ApiClient.getInstance().deleteStation(id);
                UIUtils.showSuccess(this, "Station deleted from MySQL.");
                loadStations();
            } catch (Exception ex) {
                UIUtils.showError(this, "Error deleting station: " + ex.getMessage());
            }
        }
    }
}
