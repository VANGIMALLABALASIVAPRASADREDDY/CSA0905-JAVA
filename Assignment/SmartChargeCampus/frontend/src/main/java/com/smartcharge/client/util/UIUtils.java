package com.smartcharge.client.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIUtils {

    // Modern Color Palette
    public static final Color COLOR_PRIMARY = new Color(14, 116, 144);       // Deep Cyan/Teal #0E7490
    public static final Color COLOR_PRIMARY_HOVER = new Color(8, 145, 178); // Bright Teal #0891B2
    public static final Color COLOR_ACCENT = new Color(2, 132, 199);        // Azure Blue #0284C7
    public static final Color COLOR_BG = new Color(241, 245, 249);          // Light Gray Slate #F1F5F9
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_SIDEBAR_BG = new Color(15, 23, 42);     // Slate 900 #0F172A
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);  // Slate 800 #1E293B
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(2, 132, 199);
    
    // Status Colors
    public static final Color COLOR_AVAILABLE = new Color(16, 185, 129);    // Emerald Green #10B981
    public static final Color COLOR_OCCUPIED = new Color(239, 68, 68);      // Red #EF4444
    public static final Color COLOR_RESERVED = new Color(245, 158, 11);     // Amber/Orange #F59E0B
    public static final Color COLOR_MAINTENANCE = new Color(100, 116, 139); // Gray Slate #64748B
    public static final Color COLOR_RECOMMENDED = new Color(14, 165, 233);  // Sky Blue #0EA5E9
    public static final Color COLOR_PROMOTED = new Color(168, 85, 247);     // Purple #A855F7

    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color COLOR_BORDER = new Color(226, 232, 240);

    // Modern Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_METRIC_VALUE = new Font("Segoe UI", Font.BOLD, 24);

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JPanel createMetricCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_BORDER, 1),
                        new EmptyBorder(12, 16, 12, 16)
                )
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_SMALL);
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(FONT_METRIC_VALUE);
        lblValue.setForeground(COLOR_TEXT_MAIN);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setBackground(COLOR_BG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_OCCUPIED);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTable createStyledTable() {
        JTable table = new JTable();
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(COLOR_TEXT_MAIN);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));

        return table;
    }

    public static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(FONT_BODY_BOLD);
            String text = value != null ? value.toString() : "";
            lbl.setText(text);

            Color badgeColor;
            Color textColor = Color.WHITE;

            if ("AVAILABLE".equalsIgnoreCase(text) || "ACTIVE".equalsIgnoreCase(text) || "PAID".equalsIgnoreCase(text) || "COMPLETED".equalsIgnoreCase(text)) {
                badgeColor = COLOR_AVAILABLE;
            } else if ("OCCUPIED".equalsIgnoreCase(text) || "FAILED".equalsIgnoreCase(text) || "CANCELLED".equalsIgnoreCase(text)) {
                badgeColor = COLOR_OCCUPIED;
            } else if ("RESERVED".equalsIgnoreCase(text) || "PENDING".equalsIgnoreCase(text) || "WAITING".equalsIgnoreCase(text)) {
                badgeColor = COLOR_RESERVED;
            } else if ("MAINTENANCE".equalsIgnoreCase(text) || "INACTIVE".equalsIgnoreCase(text)) {
                badgeColor = COLOR_MAINTENANCE;
            } else if ("PROMOTED".equalsIgnoreCase(text)) {
                badgeColor = COLOR_PROMOTED;
            } else {
                badgeColor = COLOR_BORDER;
                textColor = COLOR_TEXT_MAIN;
            }

            if (!isSelected) {
                lbl.setBackground(badgeColor);
                lbl.setForeground(textColor);
                lbl.setOpaque(true);
            }
            return lbl;
        }
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "SmartCharge Campus - Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "SmartCharge Campus - Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int res = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return res == JOptionPane.YES_OPTION;
    }
}
