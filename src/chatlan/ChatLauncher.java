package chatlan;

import chatlan.client.ChatClient;
import chatlan.model.Message;
import chatlan.network.PeerDiscovery;
import chatlan.server.ChatServer;
import chatlan.ui.ClientUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ChatLauncher extends JFrame {
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color TEXT_PRIMARY = new Color(243, 244, 246);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color BG_INPUT = new Color(55, 65, 81);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color BORDER_COLOR = new Color(75, 85, 99);

    private static final int DEFAULT_PORT = 5000;

    private JTextField usernameField;
    private JLabel statusLabel;
    private JButton joinButton;
    private JProgressBar progressBar;

    // Server components (auto-managed)
    private static ChatServer server;
    private static PeerDiscovery discovery;

    public ChatLauncher() {
        setTitle("ChatLAN");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), getHeight(), new Color(30, 27, 75));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles
                g2.setColor(new Color(59, 130, 246, 15));
                g2.fillOval(-80, -80, 250, 250);
                g2.fillOval(getWidth() - 150, getHeight() - 150, 250, 250);
                g2.setColor(new Color(168, 85, 247, 10));
                g2.fillOval(getWidth() / 2 - 100, -50, 200, 200);
                g2.dispose();
            }
        };

        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(31, 41, 55, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(new Color(75, 85, 99, 100));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(false);
        formCard.setPreferredSize(new Dimension(380, 400));
        formCard.setBorder(new EmptyBorder(35, 40, 35, 40));

        // Logo
        JLabel logo = new JLabel("\uD83D\uDCAC", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(logo);
        formCard.add(Box.createVerticalStrut(8));

        JLabel title = new JLabel("ChatLAN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(title);

        JLabel subtitle = new JLabel("Masuk dan langsung ngobrol!", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(subtitle);

        formCard.add(Box.createVerticalStrut(35));

        // Username label
        JLabel nameLabel = new JLabel("Nama Kamu");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(TEXT_SECONDARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(nameLabel);
        formCard.add(Box.createVerticalStrut(6));

        // Username field
        usernameField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        usernameField.setOpaque(false);
        usernameField.setForeground(TEXT_PRIMARY);
        usernameField.setCaretColor(TEXT_PRIMARY);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBorder(new EmptyBorder(10, 16, 10, 16));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    startChat();
                }
            }
        });
        formCard.add(usernameField);

        formCard.add(Box.createVerticalStrut(25));

        // Join button
        joinButton = new JButton("\uD83D\uDE80  Masuk Chat") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp;
                if (!isEnabled()) {
                    gp = new GradientPaint(0, 0, new Color(55, 65, 81), getWidth(), 0, new Color(55, 65, 81));
                } else if (getModel().isPressed()) {
                    gp = new GradientPaint(0, 0, new Color(37, 99, 235), getWidth(), 0, new Color(124, 58, 237));
                } else if (getModel().isRollover()) {
                    gp = new GradientPaint(0, 0, new Color(96, 165, 250), getWidth(), 0, new Color(192, 132, 252));
                } else {
                    gp = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), 0, ACCENT_PURPLE);
                }
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        joinButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        joinButton.setForeground(Color.WHITE);
        joinButton.setContentAreaFilled(false);
        joinButton.setBorderPainted(false);
        joinButton.setFocusPainted(false);
        joinButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinButton.setBorder(new EmptyBorder(12, 20, 12, 20));
        joinButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinButton.addActionListener(e -> startChat());
        formCard.add(joinButton);

        formCard.add(Box.createVerticalStrut(15));

        // Progress bar (hidden by default)
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setBackground(BG_INPUT);
        progressBar.setForeground(ACCENT_BLUE);
        progressBar.setBorderPainted(false);
        formCard.add(progressBar);

        formCard.add(Box.createVerticalStrut(8));

        // Status label
        statusLabel = new JLabel("Otomatis menemukan server di jaringan", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(statusLabel);

        mainPanel.add(formCard);
        add(mainPanel);
        setVisible(true);

        // Focus on username field
        usernameField.requestFocusInWindow();
    }

    private void startChat() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("⚠️ Masukkan nama dulu!");
            statusLabel.setForeground(new Color(250, 204, 21));
            return;
        }

        joinButton.setEnabled(false);
        usernameField.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("\uD83D\uDD0D Mencari server di jaringan...");
        statusLabel.setForeground(ACCENT_BLUE);

        // Run discovery in background
        new Thread(() -> {
            String serverAddress = PeerDiscovery.discoverServer();

            if (serverAddress != null) {
                // Found existing server — connect as client
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("✅ Server ditemukan! Menghubungkan...");
                    statusLabel.setForeground(new Color(34, 197, 94));
                });

                String[] parts = serverAddress.split(":");
                String ip = parts[0];
                int port = Integer.parseInt(parts[1]);

                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new ClientUI(username, ip, port, false);
                });

            } else {
                // No server found — become the server + client
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("\uD83D\uDCE1 Tidak ada server. Membuat server baru...");
                    statusLabel.setForeground(new Color(250, 204, 21));
                });

                // Start embedded server
                server = new ChatServer(DEFAULT_PORT, new ChatServer.ServerListener() {
                    @Override public void onServerStarted(int port, String ipAddress) {
                        System.out.println("Auto-server started at " + ipAddress + ":" + port);
                    }
                    @Override public void onServerStopped() {
                        System.out.println("Auto-server stopped.");
                    }
                    @Override public void onClientJoined(String u, int total) {
                        System.out.println(u + " joined. Total: " + total);
                    }
                    @Override public void onClientLeft(String u, int total) {
                        System.out.println(u + " left. Total: " + total);
                    }
                    @Override public void onMessageReceived(Message message) {}
                    @Override public void onError(String error) {
                        System.err.println("Server error: " + error);
                    }
                });
                server.start();

                // Start broadcasting for other peers to discover
                discovery = new PeerDiscovery();
                discovery.startBroadcasting(DEFAULT_PORT);

                // Small delay then connect as client to own server
                try { Thread.sleep(500); } catch (InterruptedException e) {}

                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new ClientUI(username, "127.0.0.1", DEFAULT_PORT, true);
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(ChatLauncher::new);
    }
}
