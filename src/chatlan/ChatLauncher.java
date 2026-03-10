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
    private static final Color BG_PRIMARY    = new Color(250, 250, 250);
    private static final Color BG_CARD       = new Color(255, 255, 255);
    private static final Color TEXT_DARK     = new Color(28, 28, 30);
    private static final Color TEXT_LIGHT    = new Color(174, 174, 178);
    private static final Color ACCENT        = new Color(0, 122, 255);
    private static final Color INPUT_BG      = new Color(242, 242, 247);
    private static final Color SUCCESS       = new Color(52, 199, 89);
    private static final Color WARNING       = new Color(255, 149, 0);
    private static final Color BORDER_LIGHT  = new Color(229, 229, 234);

    private static final int DEFAULT_PORT = 5000;

    private JTextField usernameField;
    private JLabel statusLabel;
    private JButton joinButton;
    private JPanel progressContainer;

    private static ChatServer server;
    private static PeerDiscovery discovery;

    private float cardOpacity = 0f;
    private Timer fadeInTimer;

    public ChatLauncher() {
        setTitle("ChatLAN");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 480);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 122, 255, 6));
                g2.fillOval(-50, -50, 180, 180);
                g2.setColor(new Color(52, 199, 89, 5));
                g2.fillOval(getWidth() - 80, getHeight() - 100, 180, 180);
                g2.dispose();
            }
        };

        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardOpacity));
                g2.setColor(new Color(0, 0, 0, 4));
                for (int i = 4; i > 0; i--) {
                    g2.fillRoundRect(i, i + 2, getWidth() - i*2, getHeight() - i*2, 18, 18);
                }
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(0, 0, 0, 6));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(false);
        formCard.setPreferredSize(new Dimension(340, 400));
        formCard.setBorder(new EmptyBorder(36, 36, 32, 36));

        JLabel logo = new JLabel("\uD83D\uDCAC", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(logo);
        formCard.add(Box.createVerticalStrut(8));

        JLabel title = new JLabel("ChatLAN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(title);
        formCard.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Masuk dan langsung ngobrol!", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_LIGHT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(subtitle);
        formCard.add(Box.createVerticalStrut(32));

        JLabel nameLabel = new JLabel("NAMA KAMU", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        nameLabel.setForeground(TEXT_LIGHT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(nameLabel);
        formCard.add(Box.createVerticalStrut(8));

        usernameField = new JTextField() {
            private float focusAnim = 0f;
            private Timer ft;
            {
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { anim(true); }
                    @Override public void focusLost(FocusEvent e)   { anim(false); }
                });
            }
            private void anim(boolean in) {
                if (ft != null) ft.stop();
                ft = new Timer(16, e -> {
                    focusAnim = in ? Math.min(1f, focusAnim + 0.12f) : Math.max(0f, focusAnim - 0.12f);
                    repaint();
                    if ((in && focusAnim >= 1f) || (!in && focusAnim <= 0f)) ((Timer)e.getSource()).stop();
                });
                ft.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (focusAnim > 0) {
                    g2.setColor(new Color(0, 122, 255, (int)(focusAnim * 55)));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        usernameField.setOpaque(false);
        usernameField.setForeground(TEXT_DARK);
        usernameField.setCaretColor(ACCENT);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(new EmptyBorder(11, 14, 11, 14));
        usernameField.setMaximumSize(new Dimension(268, 44));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) startChat();
            }
        });
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(24));

        joinButton = new JButton("Masuk Chat") {
            private float hv = 0f;
            private Timer ht;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { ah(true); }
                    @Override public void mouseExited(MouseEvent e)  { ah(false); }
                });
            }
            private void ah(boolean in) {
                if (ht != null) ht.stop();
                ht = new Timer(16, e -> {
                    hv = in ? Math.min(1f, hv + 0.15f) : Math.max(0f, hv - 0.15f);
                    repaint();
                    if ((in && hv >= 1f) || (!in && hv <= 0f)) ((Timer)e.getSource()).stop();
                });
                ht.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base;
                if (!isEnabled()) {
                    base = BORDER_LIGHT;
                } else {
                    int r = (int)(ACCENT.getRed()   - 15 * hv);
                    int gb= (int)(ACCENT.getGreen() - 12 * hv);
                    int b = (int)(ACCENT.getBlue()  - 20 * hv);
                    base = new Color(Math.max(0,r), Math.max(0,gb), Math.max(0,b));
                }
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt,
                    (getWidth() - fm.stringWidth(txt)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        joinButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        joinButton.setForeground(Color.WHITE);
        joinButton.setContentAreaFilled(false);
        joinButton.setBorderPainted(false);
        joinButton.setFocusPainted(false);
        joinButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinButton.setBorder(new EmptyBorder(13, 20, 13, 20));
        joinButton.setMaximumSize(new Dimension(268, 46));
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinButton.addActionListener(e -> startChat());
        formCard.add(joinButton);
        formCard.add(Box.createVerticalStrut(16));

        progressContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        progressContainer.setOpaque(false);
        progressContainer.setVisible(false);
        progressContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        progressContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel dots = new JPanel() {
            private int phase = 0;
            private Timer dt;
            {
                setPreferredSize(new Dimension(44, 10));
                setOpaque(false);
                dt = new Timer(280, e -> { phase = (phase + 1) % 4; repaint(); });
                dt.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = 5, sp = 14;
                int sx = (getWidth() - 3 * sp) / 2;
                for (int i = 0; i < 3; i++) {
                    float a = (i < phase) ? 1f : 0.2f;
                    g2.setColor(new Color(0, 122, 255, (int)(a * 255)));
                    g2.fillOval(sx + i * sp, (getHeight() - sz) / 2, sz, sz);
                }
                g2.dispose();
            }
        };
        progressContainer.add(dots);
        formCard.add(progressContainer);
        formCard.add(Box.createVerticalStrut(6));

        statusLabel = new JLabel("Otomatis menemukan server di jaringan", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_LIGHT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(statusLabel);

        mainPanel.add(formCard);
        add(mainPanel);
        setVisible(true);

        fadeInTimer = new Timer(16, e -> {
            cardOpacity = Math.min(1f, cardOpacity + 0.07f);
            formCard.repaint();
            if (cardOpacity >= 1f) fadeInTimer.stop();
        });
        fadeInTimer.start();

        usernameField.requestFocusInWindow();
    }

    private void startChat() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Masukkan nama dulu!");
            statusLabel.setForeground(WARNING);
            shakeComponent(usernameField);
            return;
        }

        joinButton.setEnabled(false);
        usernameField.setEnabled(false);
        progressContainer.setVisible(true);
        statusLabel.setText("Mencari server di jaringan...");
        statusLabel.setForeground(ACCENT);

        new Thread(() -> {
            String serverAddress = PeerDiscovery.discoverServer();

            if (serverAddress != null) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Server ditemukan!");
                    statusLabel.setForeground(SUCCESS);
                });

                String[] parts = serverAddress.split(":");
                String ip = parts[0];
                int port = Integer.parseInt(parts[1]);

                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new ClientUI(username, ip, port, false);
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Membuat server baru...");
                    statusLabel.setForeground(WARNING);
                });

                server = new ChatServer(DEFAULT_PORT, new ChatServer.ServerListener() {
                    @Override public void onServerStarted(int port, String ip) {
                        System.out.println("Auto-server started at " + ip + ":" + port);
                    }
                    @Override public void onServerStopped() {
                        System.out.println("Auto-server stopped.");
                    }
                    @Override public void onClientJoined(String u, int t) {
                        System.out.println(u + " joined. Total: " + t);
                    }
                    @Override public void onClientLeft(String u, int t) {
                        System.out.println(u + " left. Total: " + t);
                    }
                    @Override public void onMessageReceived(Message m) {}
                    @Override public void onError(String err) {
                        System.err.println("Server error: " + err);
                    }
                });
                server.start();

                discovery = new PeerDiscovery();
                discovery.startBroadcasting(DEFAULT_PORT);

                try { Thread.sleep(500); } catch (InterruptedException e) {}

                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new ClientUI(username, "127.0.0.1", DEFAULT_PORT, true);
                });
            }
        }).start();
    }

    private void shakeComponent(JComponent comp) {
        Point orig = comp.getLocation();
        int[] offsets = {-5, 5, -3, 3, -2, 2, 0};
        Timer t = new Timer(30, null);
        int[] step = {0};
        t.addActionListener(e -> {
            if (step[0] < offsets.length) {
                comp.setLocation(orig.x + offsets[step[0]], orig.y);
                step[0]++;
            } else {
                comp.setLocation(orig);
                t.stop();
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) {}
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(ChatLauncher::new);
    }
}
