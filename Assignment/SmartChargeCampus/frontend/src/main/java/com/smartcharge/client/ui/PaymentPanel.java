package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.PaymentDto;
import com.smartcharge.client.util.SessionContext;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PaymentPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnRefresh;
    private List<PaymentDto> paymentList = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public PaymentPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Campus EV Charging Payment Transactions");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionControls.setOpaque(false);

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadPayments());
        actionControls.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Panel
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BorderLayout());

        String[] columns = {"Payment ID", "Session ID", "Vehicle Reg", "Point", "Energy Delivered", "Amount (INR)", "Payment Method", "Status", "Payment Date", "User"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIUtils.createStyledTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(80);
        table.getColumnModel().getColumn(7).setCellRenderer(new UIUtils.StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        loadPayments();
    }

    public void loadPayments() {
        btnRefresh.setEnabled(false);
        SwingWorker<List<PaymentDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PaymentDto> doInBackground() throws Exception {
                if (SessionContext.isAdmin()) {
                    return ApiClient.getInstance().getAllPayments();
                } else {
                    return ApiClient.getInstance().getPaymentsByUser(SessionContext.getCurrentUser().getUserId());
                }
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    paymentList = get();
                    tableModel.setRowCount(0);

                    for (PaymentDto p : paymentList) {
                        tableModel.addRow(new Object[]{
                                p.getPaymentId(),
                                "#" + p.getSessionId(),
                                p.getRegistrationNumber(),
                                p.getPointName(),
                                String.format("%.2f kWh", p.getEnergyConsumedKwh()),
                                String.format("₹%.2f", p.getAmount()),
                                p.getPaymentMethod(),
                                p.getPaymentStatus(),
                                p.getPaymentTime() != null ? p.getPaymentTime().format(formatter) : "-",
                                p.getUserName()
                        });
                    }
                } catch (Exception ex) {
                    UIUtils.showError(PaymentPanel.this, "Failed to load payments: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
