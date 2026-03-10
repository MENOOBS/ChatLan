package chatlan.ui;

import chatlan.model.Message;
import chatlan.server.ChatServer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerUI extends JFrame implements ChatServer.ServerListener {
    // ─── Color Palette ────────────────────────────────────────────────
    private static final Color BG_DARK = new Color(17, 24, 39);
    private static final Color BG_PANEL = new Color(31, 41, 55);
    private static final Color BG_INPUT = new Color(55, 65, 81);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_GREEN = new Color(34, 197, 94);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_YELLOW = new Color(250, 204, 21);
    private static final Color TEXT_PRIMARY = new Color(243, 244, 246);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color BORDER_COLOR = new Color(75, 85, 99);

    // ─── Components ───────────────────────────────────────────────────
    private JTextPane logArea;
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private JLabel ipLabel;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private ChatServer server;

    public ServerUI() {
        setTitle("ChatLAN Server");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (server != null) {
                    server.stop();
                }
                dispose();
                System.exit(0);
            }
        });

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        setVisible(true);
    }

    // ─── Header ───────────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 58, 138), getWidth(), 0, new Color(88, 28, 135));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Left: Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel icon = new JLabel("🖥️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        leftPanel.add(icon);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("ChatLAN Server");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        titlePanel.add(title);

        JLabel subtitle = new JLabel("Local Network Chat Server");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(191, 219, 254));
        titlePanel.add(subtitle);

        leftPanel.add(titlePanel);
        header.add(leftPanel, BorderLayout.WEST);

        // Right: Status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);

        statusLabel = createStatusBadge("⛔ OFFLINE", ACCENT_RED);
        rightPanel.add(statusLabel);

        clientCountLabel = new JLabel("👥 0 client");
        clientCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clientCountLabel.setForeground(Color.WHITE);
        rightPanel.add(clientCountLabel);

        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JLabel createStatusBadge(String text, Color color) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(color);
        badge.setBorder(new EmptyBorder(5, 12, 5, 12));
        return badge;
    }

    // ─── Main Content ─────────────────────────────────────────────────
    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setBackground(BG_DARK);
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        main.add(createLogPanel(), BorderLayout.CENTER);
        main.add(createSidePanel(), BorderLayout.EAST);

        return main;
    }

    // ─── Log Panel ────────────────────────────────────────────────────
    private JPanel createLogPanel() {
        JPanel panel = createStyledPanel("📋 Server Log");

        logArea = new JTextPane();
        logArea.setEditable(false);
        logArea.setBackground(new Color(17, 24, 39));
        logArea.setForeground(TEXT_PRIMARY);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setCaretColor(TEXT_PRIMARY);
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(17, 24, 39));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Style scrollbar
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ─── Side Panel ───────────────────────────────────────────────────
    private JPanel createSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(250, 0));
        side.setBackground(BG_DARK);

        // Server Controls
        JPanel controlPanel = createStyledPanel("⚙️ Server Controls");
        controlPanel.setPreferredSize(new Dimension(250, 220));
        controlPanel.setMaximumSize(new Dimension(250, 220));

        JPanel controlContent = new JPanel();
        controlContent.setLayout(new BoxLayout(controlContent, BoxLayout.Y_AXIS));
        controlContent.setBackground(BG_PANEL);
        controlContent.setBorder(new EmptyBorder(10, 10, 10, 10));

        // IP Label
        ipLabel = new JLabel("IP: Belum Aktif");
        ipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ipLabel.setForeground(TEXT_SECONDARY);
        ipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlContent.add(ipLabel);
        controlContent.add(Box.createVerticalStrut(10));

        // Port input
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        portLabel.setForeground(TEXT_PRIMARY);
        portLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlContent.add(portLabel);
        controlContent.add(Box.createVerticalStrut(5));

        portField = createStyledTextField("5000");
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        portField.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlContent.add(portField);
        controlContent.add(Box.createVerticalStrut(15));

        // Buttons
        startButton = createStyledButton("▶  Start Server", ACCENT_GREEN);
        startButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        startButton.addActionListener(e -> startServer());
        controlContent.add(startButton);
        controlContent.add(Box.createVerticalStrut(8));

        stopButton = createStyledButton("⏹  Stop Server", ACCENT_RED);
        stopButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stopButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopServer());
        controlContent.add(stopButton);

        controlPanel.add(controlContent, BorderLayout.CENTER);
        side.add(controlPanel);
        side.add(Box.createVerticalStrut(15));

        // Connected Clients
        JPanel clientPanel = createStyledPanel("👥 Connected Clients");

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setBackground(BG_PANEL);
        clientList.setForeground(TEXT_PRIMARY);
        clientList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientList.setSelectionBackground(ACCENT_BLUE);
        clientList.setSelectionForeground(Color.WHITE);
        clientList.setBorder(new EmptyBorder(5, 10, 5, 10));
        clientList.setCellRenderer(new ClientListRenderer());

        JScrollPane clientScroll = new JScrollPane(clientList);
        clientScroll.setBorder(BorderFactory.createEmptyBorder());
        clientScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        clientScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        clientPanel.add(clientScroll, BorderLayout.CENTER);
        side.add(clientPanel);

        return side;
    }

    // ─── Styled Components ────────────────────────────────────────────
    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(10, 10, 8, 10));
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    private JTextField createStyledTextField(String defaultText) {
        JTextField field = new JTextField(defaultText);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(BG_INPUT);
                } else if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        return button;
    }

    // ─── Server Actions ───────────────────────────────────────────────
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            server = new ChatServer(port, this);
            server.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);
        } catch (NumberFormatException e) {
            appendLog("❌ Port tidak valid!", ACCENT_RED);
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            portField.setEnabled(true);
            clientListModel.clear();
        }
    }

    // ─── Log ──────────────────────────────────────────────────────────
    private void appendLog(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.text.StyledDocument doc = logArea.getStyledDocument();
                javax.swing.text.SimpleAttributeSet attrs = new javax.swing.text.SimpleAttributeSet();
                javax.swing.text.StyleConstants.setForeground(attrs, color);
                javax.swing.text.StyleConstants.setFontFamily(attrs, "Consolas");
                javax.swing.text.StyleConstants.setFontSize(attrs, 13);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                doc.insertString(doc.getLength(), "[" + timestamp + "] " + text + "\n", attrs);
                logArea.setCaretPosition(doc.getLength());
            } catch (Exception e) {}
        });
    }

    // ─── ServerListener Implementation ────────────────────────────────
    @Override
    public void onServerStarted(int port, String ipAddress) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("✅ ONLINE");
            statusLabel.setForeground(ACCENT_GREEN);
            ipLabel.setText("IP: " + ipAddress + ":" + port);
        });
        appendLog("Server dimulai di " + ipAddress + ":" + port, ACCENT_GREEN);
        appendLog("Menunggu koneksi client...", TEXT_SECONDARY);
    }

    @Override
    public void onServerStopped() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("⛔ OFFLINE");
            statusLabel.setForeground(ACCENT_RED);
            ipLabel.setText("IP: Belum Aktif");
            clientCountLabel.setText("👥 0 client");
        });
        appendLog("Server dihentikan.", ACCENT_RED);
    }

    @Override
    public void onClientJoined(String username, int totalClients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.addElement(username);
            clientCountLabel.setText("👥 " + totalClients + " client");
        });
        appendLog("➕ " + username + " bergabung. Total: " + totalClients, ACCENT_GREEN);
    }

    @Override
    public void onClientLeft(String username, int totalClients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.removeElement(username);
            clientCountLabel.setText("👥 " + totalClients + " client");
        });
        appendLog("➖ " + username + " keluar. Total: " + totalClients, ACCENT_YELLOW);
    }

    @Override
    public void onMessageReceived(Message message) {
        switch (message.getType()) {
            case CHAT:
                appendLog(message.getSender() + ": " + message.getContent(), TEXT_PRIMARY);
                break;
            case PRIVATE:
                appendLog("[PM] " + message.getSender() + " → " + message.getRecipient() + ": " + message.getContent(),
                    new Color(168, 85, 247));
                break;
            case JOIN:
                appendLog(message.getContent(), ACCENT_GREEN);
                break;
            case LEAVE:
                appendLog(message.getContent(), ACCENT_YELLOW);
                break;
            default:
                appendLog(message.getContent(), TEXT_SECONDARY);
        }
    }

    @Override
    public void onError(String error) {
        appendLog("❌ Error: " + error, ACCENT_RED);
    }

    // ─── Custom List Renderer ─────────────────────────────────────────
    private class ClientListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText("  🟢  " + value.toString());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(6, 5, 6, 5));

            if (isSelected) {
                label.setBackground(ACCENT_BLUE);
            } else {
                label.setBackground(BG_PANEL);
            }
            label.setForeground(TEXT_PRIMARY);
            return label;
        }
    }

    // ─── Custom Scrollbar ─────────────────────────────────────────────
    static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = BORDER_COLOR;
            trackColor = BG_PANEL;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 116, 139));
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y, thumbBounds.width - 2, thumbBounds.height, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(BG_PANEL);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }
}
