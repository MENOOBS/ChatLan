package chatlan.ui;

import chatlan.client.ChatClient;
import chatlan.model.Message;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ClientUI extends JFrame implements ChatClient.ClientListener {
    // ─── Color Palette ────────────────────────────────────────────────
    private static final Color BG_DARK = new Color(17, 24, 39);
    private static final Color BG_PANEL = new Color(31, 41, 55);
    private static final Color BG_INPUT = new Color(55, 65, 81);
    private static final Color BG_MESSAGE_SELF = new Color(37, 99, 235);
    private static final Color BG_MESSAGE_OTHER = new Color(55, 65, 81);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_GREEN = new Color(34, 197, 94);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color ACCENT_YELLOW = new Color(250, 204, 21);
    private static final Color TEXT_PRIMARY = new Color(243, 244, 246);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color BORDER_COLOR = new Color(75, 85, 99);

    // ─── Components ───────────────────────────────────────────────────
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;
    private JLabel connectionInfo;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel userCountLabel;
    private ChatClient client;
    private String username;
    private boolean isHost;

    /**
     * Direct-connect constructor. No connect screen needed.
     * @param username  The user's display name
     * @param serverIP  Server IP to connect to
     * @param serverPort Server port
     * @param isHost    Whether this instance is also the server host
     */
    public ClientUI(String username, String serverIP, int serverPort, boolean isHost) {
        this.username = username;
        this.isHost = isHost;

        setTitle("ChatLAN - " + username);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 700);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                }
                dispose();
                System.exit(0);
            }
        });

        add(createChatHeader(), BorderLayout.NORTH);
        add(createChatBody(), BorderLayout.CENTER);
        add(createInputBar(), BorderLayout.SOUTH);

        setVisible(true);

        // Auto-connect
        connectToServer(serverIP, serverPort);
    }

    private void connectToServer(String serverIP, int serverPort) {
        client = new ChatClient(serverIP, serverPort, username, this);
        client.connect();
    }

    // ─── Chat Header ─────────────────────────────────────────────────
    private JPanel createChatHeader() {
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
        header.setPreferredSize(new Dimension(0, 65));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        JLabel icon = new JLabel("\uD83D\uDCAC");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        leftPanel.add(icon);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("ChatLAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        titlePanel.add(title);

        connectionInfo = new JLabel("Menghubungkan...");
        connectionInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        connectionInfo.setForeground(new Color(191, 219, 254));
        titlePanel.add(connectionInfo);

        leftPanel.add(titlePanel);
        header.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setOpaque(false);

        // Host badge
        if (isHost) {
            JLabel hostBadge = new JLabel("\uD83D\uDC51 Host") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(250, 204, 21, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            hostBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            hostBadge.setForeground(ACCENT_YELLOW);
            hostBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
            rightPanel.add(hostBadge);
        }

        statusLabel = new JLabel("\uD83D\uDFE2 Online");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(ACCENT_GREEN);
        rightPanel.add(statusLabel);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ─── Chat Body ────────────────────────────────────────────────────
    private JPanel createChatBody() {
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(BG_DARK);

        // Chat messages area
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG_DARK);
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.getViewport().setBackground(BG_DARK);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.getVerticalScrollBar().setUI(new ServerUI.ModernScrollBarUI());
        chatScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        body.add(chatScrollPane, BorderLayout.CENTER);

        // User list sidebar
        body.add(createUserListPanel(), BorderLayout.EAST);

        return body;
    }

    // ─── User List Panel ──────────────────────────────────────────────
    private JPanel createUserListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(12, 15, 12, 15));

        userCountLabel = new JLabel("\uD83D\uDC65 Online (0)");
        userCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userCountLabel.setForeground(TEXT_PRIMARY);
        header.add(userCountLabel, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(BG_PANEL);
        userList.setForeground(TEXT_PRIMARY);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setSelectionBackground(new Color(59, 130, 246, 50));
        userList.setSelectionForeground(TEXT_PRIMARY);
        userList.setBorder(new EmptyBorder(0, 5, 5, 5));
        userList.setCellRenderer(new UserListRenderer());

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new ServerUI.ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Tip label at bottom
        JLabel tipLabel = new JLabel("<html><center>Tip: ketik /pm nama pesan<br>untuk private message</center></html>");
        tipLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        tipLabel.setForeground(new Color(100, 116, 139));
        tipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tipLabel.setBorder(new EmptyBorder(8, 10, 10, 10));
        panel.add(tipLabel, BorderLayout.SOUTH);

        return panel;
    }

    // ─── Input Bar ────────────────────────────────────────────────────
    private JPanel createInputBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(12, 15, 12, 15)
        ));

        messageField = new JTextField() {
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
        messageField.setOpaque(false);
        messageField.setForeground(TEXT_PRIMARY);
        messageField.setCaretColor(TEXT_PRIMARY);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(new EmptyBorder(10, 15, 10, 15));
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        // Placeholder
        messageField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals("Ketik pesan...")) {
                    messageField.setText("");
                    messageField.setForeground(TEXT_PRIMARY);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setText("Ketik pesan...");
                    messageField.setForeground(TEXT_SECONDARY);
                }
            }
        });
        messageField.setText("Ketik pesan...");
        messageField.setForeground(TEXT_SECONDARY);

        sendButton = new JButton("Kirim \u27A4") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp;
                if (getModel().isPressed()) {
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
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setForeground(Color.WHITE);
        sendButton.setContentAreaFilled(false);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        sendButton.setPreferredSize(new Dimension(110, 40));
        sendButton.addActionListener(e -> sendMessage());

        bar.add(messageField, BorderLayout.CENTER);
        bar.add(sendButton, BorderLayout.EAST);

        return bar;
    }

    // ─── Actions ──────────────────────────────────────────────────────
    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || text.equals("Ketik pesan...")) return;

        if (client != null && client.isConnected()) {
            // Check for private message: /pm username message
            if (text.startsWith("/pm ")) {
                String[] parts = text.substring(4).split(" ", 2);
                if (parts.length == 2) {
                    client.sendPrivate(parts[0], parts[1]);
                } else {
                    addChatBubble(new Message("System", "Format: /pm nama pesan", Message.Type.SYSTEM));
                }
            } else {
                client.sendChat(text);
            }
            messageField.setText("");
        }
    }

    // ─── Chat Bubble ──────────────────────────────────────────────────
    private void addChatBubble(Message message) {
        SwingUtilities.invokeLater(() -> {
            JPanel bubble;
            switch (message.getType()) {
                case CHAT:
                case PRIVATE:
                    boolean isSelf = message.getSender().equals(username);
                    bubble = createMessageBubble(message, isSelf);
                    break;
                case JOIN:
                    bubble = createSystemBubble("\uD83D\uDFE2 " + message.getContent(), ACCENT_GREEN);
                    break;
                case LEAVE:
                    bubble = createSystemBubble("\uD83D\uDD34 " + message.getContent(), ACCENT_YELLOW);
                    break;
                case SYSTEM:
                    bubble = createSystemBubble("\u2139\uFE0F " + message.getContent(), TEXT_SECONDARY);
                    break;
                default:
                    bubble = createSystemBubble(message.getContent(), TEXT_SECONDARY);
            }

            chatPanel.add(bubble);
            chatPanel.add(Box.createVerticalStrut(6));
            chatPanel.revalidate();
            chatPanel.repaint();

            // Auto scroll to bottom
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        });
    }

    private JPanel createMessageBubble(Message message, boolean isSelf) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor;
                if (message.getType() == Message.Type.PRIVATE) {
                    bgColor = new Color(88, 28, 135);
                } else {
                    bgColor = isSelf ? BG_MESSAGE_SELF : BG_MESSAGE_OTHER;
                }
                g2.setColor(bgColor);

                if (isSelf) {
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.fillRect(getWidth() - 8, 0, 8, 8);
                } else {
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.fillRect(0, 0, 8, 8);
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.min(d.width, 450);
                return d;
            }
        };
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        // Sender name
        if (!isSelf) {
            JLabel senderLabel = new JLabel(message.getSender());
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            senderLabel.setForeground(getColorForUser(message.getSender()));
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(senderLabel);
            bubblePanel.add(Box.createVerticalStrut(3));
        }

        // Private message indicator
        if (message.getType() == Message.Type.PRIVATE) {
            JLabel pmLabel = new JLabel("\uD83D\uDD12 Private" + (isSelf ? " \u2192 " + message.getRecipient() : ""));
            pmLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            pmLabel.setForeground(new Color(192, 132, 252));
            pmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(pmLabel);
            bubblePanel.add(Box.createVerticalStrut(2));
        }

        // Message text
        JTextArea messageText = new JTextArea(message.getContent());
        messageText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageText.setForeground(Color.WHITE);
        messageText.setOpaque(false);
        messageText.setEditable(false);
        messageText.setWrapStyleWord(true);
        messageText.setLineWrap(true);
        messageText.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageText.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));
        messageText.setBorder(null);
        bubblePanel.add(messageText);

        // Timestamp
        JLabel timeLabel = new JLabel(message.getTimestamp());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(255, 255, 255, 128));
        timeLabel.setAlignmentX(isSelf ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        bubblePanel.add(Box.createVerticalStrut(2));
        bubblePanel.add(timeLabel);

        // Alignment
        JPanel alignPanel = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(bubblePanel);

        wrapper.add(alignPanel, isSelf ? BorderLayout.EAST : BorderLayout.WEST);
        return wrapper;
    }

    private JPanel createSystemBubble(String text, Color color) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(31, 41, 55, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(color);
        label.setBorder(new EmptyBorder(4, 14, 4, 14));

        wrapper.add(label);
        return wrapper;
    }

    private Color getColorForUser(String name) {
        Color[] colors = {
            new Color(96, 165, 250),
            new Color(52, 211, 153),
            new Color(251, 146, 60),
            new Color(167, 139, 250),
            new Color(248, 113, 113),
            new Color(45, 212, 191),
            new Color(250, 204, 21),
            new Color(244, 114, 182),
        };
        int index = Math.abs(name.hashCode()) % colors.length;
        return colors[index];
    }

    // ─── ClientListener Implementation ────────────────────────────────
    @Override
    public void onConnected(String serverIP, int port) {
        SwingUtilities.invokeLater(() -> {
            String role = isHost ? "Host" : "Client";
            connectionInfo.setText(role + "  \u2022  " + serverIP + ":" + port + "  \u2022  " + username);
            statusLabel.setText("\uD83D\uDFE2 Online");
            statusLabel.setForeground(ACCENT_GREEN);
            messageField.requestFocusInWindow();
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("\uD83D\uDD34 Offline");
            statusLabel.setForeground(ACCENT_RED);
            addChatBubble(new Message("System", "Terputus dari server.", Message.Type.SYSTEM));
        });
    }

    @Override
    public void onMessageReceived(Message message) {
        addChatBubble(message);
    }

    @Override
    public void onUserListUpdated(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    userListModel.addElement(user.trim());
                }
            }
            userCountLabel.setText("\uD83D\uDC65 Online (" + userListModel.size() + ")");
        });
    }

    @Override
    public void onError(String error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    // ─── Custom List Renderer ─────────────────────────────────────────
    private class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String user = value.toString();
            boolean isSelf = user.equals(username);
            label.setText("  " + (isSelf ? "\u2B50" : "\uD83D\uDFE2") + "  " + user + (isSelf ? " (Kamu)" : ""));
            label.setFont(new Font("Segoe UI", isSelf ? Font.BOLD : Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(8, 8, 8, 8));

            if (isSelected) {
                label.setBackground(new Color(59, 130, 246, 50));
            } else {
                label.setBackground(BG_PANEL);
            }
            label.setForeground(isSelf ? ACCENT_BLUE : TEXT_PRIMARY);
            return label;
        }
    }
}
