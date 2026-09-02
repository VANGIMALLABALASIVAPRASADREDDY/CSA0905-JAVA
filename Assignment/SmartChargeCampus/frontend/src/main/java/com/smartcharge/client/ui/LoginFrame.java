package com.smartcharge.client.ui;

import com.smartcharge.client.api.ApiClient;
import com.smartcharge.client.model.UserDto;
import com.smartcharge.client.util.SessionContext;
import com.smartcharge.client.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private final JTextField txtEmail;
    private final JPasswordField txtPassword;
    private final JButton btnLogin;
    private final JLabel lblStatus;

    public LoginFrame() {
        super("SmartCharge Campus — Intelligent EV Charging System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtils.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(30, 35, 30, 35));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("SmartCharge Campus");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(UIUtils.COLOR_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Intelligent Load-Aware EV Charging System");
        lblSubtitle.setFont(UIUtils.FONT_BODY);
        lblSubtitle.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAcademic = new JLabel("CSA0905 – Programming in Java");
        lblAcademic.setFont(UIUtils.FONT_SMALL);
        lblAcademic.setForeground(UIUtils.COLOR_ACCENT);
        lblAcademic.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSubtitle);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblAcademic);
        headerPanel.add(Box.createVerticalStrut(25));

        // Form Card
        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(UIUtils.FONT_BODY_BOLD);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtEmail = new JTextField("student@campus.edu", 20);
        txtEmail.setFont(UIUtils.FONT_BODY);
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(UIUtils.FONT_BODY_BOLD);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField("student123", 20);
        txtPassword.setFont(UIUtils.FONT_BODY);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnLogin = UIUtils.createPrimaryButton("Sign In to Campus Portal");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(this::handleLogin);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UIUtils.FONT_SMALL);
        lblStatus.setForeground(UIUtils.COLOR_OCCUPIED);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Quick Demo Account Buttons
        JLabel lblQuick = new JLabel("Demo Accounts (Click to Autofill):");
        lblQuick.setFont(UIUtils.FONT_SMALL);
        lblQuick.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblQuick.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel demoBtns = new JPanel(new GridLayout(1, 3, 6, 0));
        demoBtns.setOpaque(false);
        demoBtns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        demoBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnStudent = UIUtils.createSecondaryButton("Student");
        btnStudent.setFont(UIUtils.FONT_SMALL);
        btnStudent.addActionListener(e -> setCredentials("student@campus.edu", "student123"));

        JButton btnAdmin = UIUtils.createSecondaryButton("Admin");
        btnAdmin.setFont(UIUtils.FONT_SMALL);
        btnAdmin.addActionListener(e -> setCredentials("admin@campus.edu", "admin123"));

        JButton btnStaff = UIUtils.createSecondaryButton("Staff");
        btnStaff.setFont(UIUtils.FONT_SMALL);
        btnStaff.addActionListener(e -> setCredentials("staff@campus.edu", "staff123"));

        demoBtns.add(btnStudent);
        demoBtns.add(btnAdmin);
        demoBtns.add(btnStaff);

        cardPanel.add(lblEmail);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(txtEmail);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(lblPassword);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(txtPassword);
        cardPanel.add(Box.createVerticalStrut(18));
        cardPanel.add(btnLogin);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(lblStatus);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(lblQuick);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(demoBtns);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Enter key submits login
        getRootPane().setDefaultButton(btnLogin);
    }

    private void setCredentials(String email, String password) {
        txtEmail.setText(email);
        txtPassword.setText(password);
        lblStatus.setText(" ");
    }

    private void handleLogin(ActionEvent e) {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter both email and password");
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblStatus.setText("Connecting to backend...");

        SwingWorker<UserDto, Void> worker = new SwingWorker<>() {
            @Override
            protected UserDto doInBackground() throws Exception {
                return ApiClient.getInstance().login(email, password);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    UserDto user = get();
                    SessionContext.setCurrentUser(user);
                    dispose(); // Close Login Frame
                    DashboardFrame dashboard = new DashboardFrame();
                    dashboard.setVisible(true);
                } catch (Exception ex) {
                    lblStatus.setForeground(UIUtils.COLOR_OCCUPIED);
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    lblStatus.setText(msg != null ? msg : "Login failed");
                }
            }
        };
        worker.execute();
    }
}
