package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.QueueEntryDto;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VirtualQueuePanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnRefresh;
    private List<QueueEntryDto> queueList = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM HH:mm");

    public VirtualQueuePanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Campus EV Priority Virtual Queue");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        btnRefresh = UIUtils.createSecondaryButton("↻ Refresh Queue");
        btnRefresh.addActionListener(e -> loadQueue());
        actions.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);

        // Center Split: Table & Explanation
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);

        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BorderLayout());

        String[] columns = {"Queue ID", "Pos", "Priority Score", "Vehicle", "Connector", "Battery %", "Departure Time", "Preferred Hub", "Status", "User"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = UIUtils.createStyledTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(95);
        table.getColumnModel().getColumn(8).setCellRenderer(new UIUtils.StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        // Info / Explanation Card
        JPanel infoCard = UIUtils.createCardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

        JLabel lblInfo = new JLabel("Virtual Queue Deterministic Priority Policy:");
        lblInfo.setFont(UIUtils.FONT_BODY_BOLD);
        lblInfo.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel lblR1 = new JLabel("• Battery Urgency: ≤15% (+40 pts) | 16–30% (+30 pts) | 31–50% (+20 pts) | >50% (+10 pts)");
        JLabel lblR2 = new JLabel("• Departure Urgency: <1 Hour (+40 pts) | 1–2 Hours (+30 pts) | 2–4 Hours (+20 pts)");
        JLabel lblR3 = new JLabel("• Waiting Time: +1 bonus point per 5 minutes accumulated in queue.");
        JLabel lblR4 = new JLabel("• Automatic Promotion: Triggered immediately when any active session checks out.");

        lblR1.setFont(UIUtils.FONT_SMALL);
        lblR2.setFont(UIUtils.FONT_SMALL);
        lblR3.setFont(UIUtils.FONT_SMALL);
        lblR4.setFont(UIUtils.FONT_SMALL);

        lblR1.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblR2.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblR3.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblR4.setForeground(UIUtils.COLOR_PRIMARY);

        infoCard.add(lblInfo);
        infoCard.add(Box.createVerticalStrut(4));
        infoCard.add(lblR1);
        infoCard.add(Box.createVerticalStrut(2));
        infoCard.add(lblR2);
        infoCard.add(Box.createVerticalStrut(2));
        infoCard.add(lblR3);
        infoCard.add(Box.createVerticalStrut(2));
        infoCard.add(lblR4);

        centerPanel.add(cardPanel, BorderLayout.CENTER);
        centerPanel.add(infoCard, BorderLayout.SOUTH);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        loadQueue();
    }

    public void loadQueue() {
        btnRefresh.setEnabled(false);
        SwingWorker<List<QueueEntryDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<QueueEntryDto> doInBackground() throws Exception {
                return ApiClient.getInstance().getActiveQueue();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    queueList = get();
                    tableModel.setRowCount(0);
                    int pos = 1;
                    for (QueueEntryDto q : queueList) {
                        tableModel.addRow(new Object[]{
                                q.getQueueId(),
                                "#" + (pos++),
                                q.getPriorityScore() + " pts",
                                q.getRegistrationNumber(),
                                q.getConnectorType(),
                                (int) q.getCurrentBatteryPercent() + "% → " + (int) q.getTargetBatteryPercent() + "%",
                                q.getDepartureTime() != null ? q.getDepartureTime().format(formatter) : "-",
                                q.getPreferredLocation(),
                                q.getStatus(),
                                q.getUserName()
                        });
                    }
                } catch (Exception ex) {
                    UIUtils.showError(VirtualQueuePanel.this, "Failed to load queue: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
