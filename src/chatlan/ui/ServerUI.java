package chatlan.ui;

import chatlan.model.Message;
import chatlan.server.ChatServer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerUI extends JFrame implements ChatServer.ServerListener {
    private static final Color BG_PRIMARY    = new Color(250, 250, 250);
    private static final Color BG_WHITE      = new Color(255, 255, 255);
    private static final Color BG_INPUT      = new Color(242, 242, 247);
    private static final Color TEXT_DARK     = new Color(28, 28, 30);
    private static final Color TEXT_MID      = new Color(99, 99, 102);
    private static final Color TEXT_LIGHT    = new Color(174, 174, 178);
    private static final Color ACCENT        = new Color(0, 122, 255);
    private static final Color ACCENT_GREEN  = new Color(52, 199, 89);
    private static final Color ACCENT_RED    = new Color(255, 59, 48);
    private static final Color ACCENT_ORANGE = new Color(255, 149, 0);
    private static final Color ACCENT_PURPLE = new Color(175, 82, 222);
    private static final Color BORDER_LIGHT  = new Color(229, 229, 234);

    private JTextPane logArea;
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusDot;
    private JLabel statusText;
    private JLabel clientCountLabel;
    private JLabel ipLabel;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private ChatServer server;

    public ServerUI() {
        setTitle("ChatLAN Server");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(880, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_PRIMARY);
        setLayout(new BorderLayout(0, 0));
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (server != null) server.stop();
                dispose();
                System.exit(0);
            }
        });
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_LIGHT);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(new EmptyBorder(0, 24, 0, 24));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel icon = new JLabel("\uD83D\uDDA5\uFE0F ");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        leftPanel.add(icon);
        leftPanel.add(Box.createHorizontalStrut(6));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("ChatLAN Server");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(title);

        JLabel subtitle = new JLabel("Local Network Chat Server");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(TEXT_LIGHT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(subtitle);

        leftPanel.add(titleBlock);
        header.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);

        statusDot = new JLabel("\u25CF ");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        statusDot.setForeground(ACCENT_RED);
        rightPanel.add(statusDot);

        statusText = new JLabel("OFFLINE");
        statusText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusText.setForeground(ACCENT_RED);
        rightPanel.add(statusText);
        rightPanel.add(Box.createHorizontalStrut(16));

        clientCountLabel = new JLabel("0 client");
        clientCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientCountLabel.setForeground(TEXT_MID);
        rightPanel.add(clientCountLabel);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(14, 0));
        main.setBackground(BG_PRIMARY);
        main.setBorder(new EmptyBorder(14, 14, 14, 14));
        main.add(createLogPanel(), BorderLayout.CENTER);
        main.add(createSidePanel(), BorderLayout.EAST);
        return main;
    }

    private JPanel createLogPanel() {
        JPanel panel = createCard("Server Log");
        logArea = new JTextPane();
        logArea.setEditable(false);
        logArea.setBackground(BG_WHITE);
        logArea.setForeground(TEXT_DARK);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setCaretColor(ACCENT);
        logArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_WHITE);
        scrollPane.getVerticalScrollBar().setUI(new ClientUI.MinimalScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(230, 0));
        side.setBackground(BG_PRIMARY);

        JPanel controlCard = createCard("Controls");
        controlCard.setPreferredSize(new Dimension(230, 230));
        controlCard.setMaximumSize(new Dimension(230, 230));

        JPanel cContent = new JPanel();
        cContent.setLayout(new BoxLayout(cContent, BoxLayout.Y_AXIS));
        cContent.setBackground(BG_WHITE);
        cContent.setBorder(new EmptyBorder(10, 14, 14, 14));

        ipLabel = new JLabel("IP: Belum Aktif");
        ipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ipLabel.setForeground(TEXT_LIGHT);
        ipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cContent.add(ipLabel);
        cContent.add(Box.createVerticalStrut(12));

        JLabel portLabel = new JLabel("PORT");
        portLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        portLabel.setForeground(TEXT_LIGHT);
        portLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cContent.add(portLabel);
        cContent.add(Box.createVerticalStrut(5));

        portField = new JTextField("5000") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        portField.setOpaque(false);
        portField.setForeground(TEXT_DARK);
        portField.setCaretColor(ACCENT);
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        portField.setBorder(new EmptyBorder(8, 12, 8, 12));
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        portField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cContent.add(portField);
        cContent.add(Box.createVerticalStrut(14));

        startButton = createMinimalBtn("Start Server", ACCENT_GREEN);
        startButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        startButton.addActionListener(e -> startServer());
        cContent.add(startButton);
        cContent.add(Box.createVerticalStrut(6));

        stopButton = createMinimalBtn("Stop Server", ACCENT_RED);
        stopButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stopButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopServer());
        cContent.add(stopButton);

        controlCard.add(cContent, BorderLayout.CENTER);
        side.add(controlCard);
        side.add(Box.createVerticalStrut(12));

        JPanel clientCard = createCard("Connected Clients");
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setBackground(BG_WHITE);
        clientList.setForeground(TEXT_DARK);
        clientList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientList.setFixedCellHeight(34);
        clientList.setSelectionBackground(new Color(0, 122, 255, 15));
        clientList.setSelectionForeground(TEXT_DARK);
        clientList.setBorder(new EmptyBorder(2, 8, 4, 8));
        clientList.setCellRenderer(new ClientListRenderer());
        JScrollPane cs = new JScrollPane(clientList);
        cs.setBorder(BorderFactory.createEmptyBorder());
        cs.getVerticalScrollBar().setUI(new ClientUI.MinimalScrollBarUI());
        cs.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        clientCard.add(cs, BorderLayout.CENTER);
        side.add(clientCard);

        return side;
    }

    private JPanel createCard(String titleText) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 4));
                g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.setColor(BG_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0, 0, 0, 8));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        JLabel label = new JLabel(titleText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_LIGHT);
        label.setBorder(new EmptyBorder(12, 14, 6, 14));
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JButton createMinimalBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            private float h = 0f;
            private Timer ht;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { anim(true); }
                    @Override public void mouseExited(MouseEvent e)  { anim(false); }
                });
            }
            private void anim(boolean in) {
                if (ht != null) ht.stop();
                ht = new Timer(16, e -> {
                    h = in ? Math.min(1f, h + 0.15f) : Math.max(0f, h - 0.15f);
                    repaint();
                    if ((in && h >= 1f) || (!in && h <= 0f)) ((Timer)e.getSource()).stop();
                });
                ht.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                if (!isEnabled()) {
                    bg = BG_INPUT;
                } else {
                    int r = (int)(BG_INPUT.getRed() + (color.getRed() - BG_INPUT.getRed()) * h);
                    int gr= (int)(BG_INPUT.getGreen() + (color.getGreen() - BG_INPUT.getGreen()) * h);
                    int b = (int)(BG_INPUT.getBlue() + (color.getBlue() - BG_INPUT.getBlue()) * h);
                    bg = new Color(r, gr, b);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                Color tc;
                if (!isEnabled()) {
                    tc = TEXT_LIGHT;
                } else {
                    int r = (int)(color.getRed() + (255 - color.getRed()) * h);
                    int gr= (int)(color.getGreen() + (255 - color.getGreen()) * h);
                    int b = (int)(color.getBlue() + (255 - color.getBlue()) * h);
                    tc = new Color(r, gr, b);
                }
                g2.setColor(tc);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            server = new ChatServer(port, this);
            server.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);
        } catch (NumberFormatException e) {
            appendLog("Port tidak valid!", ACCENT_RED);
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

    private void appendLog(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.text.StyledDocument doc = logArea.getStyledDocument();
                javax.swing.text.SimpleAttributeSet attrs = new javax.swing.text.SimpleAttributeSet();
                javax.swing.text.StyleConstants.setForeground(attrs, color);
                javax.swing.text.StyleConstants.setFontFamily(attrs, "Consolas");
                javax.swing.text.StyleConstants.setFontSize(attrs, 12);
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                doc.insertString(doc.getLength(), "[" + ts + "]  " + text + "\n", attrs);
                logArea.setCaretPosition(doc.getLength());
            } catch (Exception e) {}
        });
    }

    @Override public void onServerStarted(int port, String ipAddress) {
        SwingUtilities.invokeLater(() -> {
            statusDot.setForeground(ACCENT_GREEN);
            statusText.setText("ONLINE");
            statusText.setForeground(ACCENT_GREEN);
            ipLabel.setText("IP: " + ipAddress + ":" + port);
        });
        appendLog("Server dimulai di " + ipAddress + ":" + port, ACCENT_GREEN);
        appendLog("Menunggu koneksi client...", TEXT_LIGHT);
    }

    @Override public void onServerStopped() {
        SwingUtilities.invokeLater(() -> {
            statusDot.setForeground(ACCENT_RED);
            statusText.setText("OFFLINE");
            statusText.setForeground(ACCENT_RED);
            ipLabel.setText("IP: Belum Aktif");
            clientCountLabel.setText("0 client");
        });
        appendLog("Server dihentikan.", ACCENT_RED);
    }

    @Override public void onClientJoined(String username, int totalClients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.addElement(username);
            clientCountLabel.setText(totalClients + " client");
        });
        appendLog("+ " + username + " bergabung. Total: " + totalClients, ACCENT_GREEN);
    }

    @Override public void onClientLeft(String username, int totalClients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.removeElement(username);
            clientCountLabel.setText(totalClients + " client");
        });
        appendLog("- " + username + " keluar. Total: " + totalClients, ACCENT_ORANGE);
    }

    @Override public void onMessageReceived(Message message) {
        switch (message.getType()) {
            case CHAT:
                appendLog(message.getSender() + ": " + message.getContent(), TEXT_DARK);
                break;
            case PRIVATE:
                appendLog("[PM] " + message.getSender() + " \u2192 " + message.getRecipient() + ": " + message.getContent(), ACCENT_PURPLE);
                break;
            case JOIN:
                appendLog(message.getContent(), ACCENT_GREEN);
                break;
            case LEAVE:
                appendLog(message.getContent(), ACCENT_ORANGE);
                break;
            default:
                appendLog(message.getContent(), TEXT_MID);
        }
    }

    @Override public void onError(String error) {
        appendLog("Error: " + error, ACCENT_RED);
    }

    private class ClientListRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText("  \u25CF  " + value.toString());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(6, 6, 6, 6));
            label.setBackground(isSelected ? new Color(0, 122, 255, 15) : BG_WHITE);
            label.setForeground(TEXT_DARK);
            return label;
        }
    }

    public static class ModernScrollBarUI extends ClientUI.MinimalScrollBarUI {}
}
