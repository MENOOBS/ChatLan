package chatlan.ui;

import chatlan.client.ChatClient;
import chatlan.model.Message;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ClientUI extends JFrame implements ChatClient.ClientListener {
    private static final Color BG_PRIMARY   = new Color(250, 250, 250);
    private static final Color BG_WHITE     = new Color(255, 255, 255);
    private static final Color BG_CHAT      = new Color(245, 245, 247);
    private static final Color BG_MSG_SELF  = new Color(0, 122, 255);
    private static final Color BG_MSG_OTHER = new Color(255, 255, 255);
    private static final Color BG_INPUT     = new Color(242, 242, 247);
    private static final Color TEXT_DARK    = new Color(28, 28, 30);
    private static final Color TEXT_LIGHT   = new Color(174, 174, 178);
    private static final Color ACCENT       = new Color(0, 122, 255);
    private static final Color ACCENT_GREEN = new Color(52, 199, 89);
    private static final Color ACCENT_RED   = new Color(255, 59, 48);
    private static final Color ACCENT_PURPLE= new Color(175, 82, 222);
    private static final Color ACCENT_ORANGE= new Color(255, 149, 0);
    private static final Color BORDER_LIGHT = new Color(229, 229, 234);
    private static final Color SIDEBAR_BG   = new Color(255, 255, 255);

    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusDot;
    private JLabel statusText;
    private JLabel connectionInfo;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel userCountLabel;
    private ChatClient client;
    private String username;
    private boolean isHost;

    public ClientUI(String username, String serverIP, int serverPort, boolean isHost) {
        this.username = username;
        this.isHost = isHost;
        setTitle("ChatLAN - " + username);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_PRIMARY);
        setLayout(new BorderLayout(0, 0));
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (client != null && client.isConnected()) client.disconnect();
                dispose();
                System.exit(0);
            }
        });
        add(createHeader(), BorderLayout.NORTH);
        add(createChatBody(), BorderLayout.CENTER);
        add(createInputBar(), BorderLayout.SOUTH);
        setVisible(true);
        connectToServer(serverIP, serverPort);
    }

    private void connectToServer(String serverIP, int serverPort) {
        client = new ChatClient(serverIP, serverPort, username, this);
        client.connect();
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
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel icon = new JLabel("\uD83D\uDCAC ");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        leftPanel.add(icon);
        leftPanel.add(Box.createHorizontalStrut(6));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("ChatLAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(title);

        connectionInfo = new JLabel("Menghubungkan...");
        connectionInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        connectionInfo.setForeground(TEXT_LIGHT);
        connectionInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(connectionInfo);

        leftPanel.add(titleBlock);
        header.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);

        if (isHost) {
            JLabel hostBadge = new JLabel("HOST") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 122, 255, 18));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            hostBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            hostBadge.setForeground(ACCENT);
            hostBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
            rightPanel.add(hostBadge);
            rightPanel.add(Box.createHorizontalStrut(12));
        }

        statusDot = new JLabel("\u25CF ");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        statusDot.setForeground(ACCENT_GREEN);
        rightPanel.add(statusDot);

        statusText = new JLabel("Online");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusText.setForeground(ACCENT_GREEN);
        rightPanel.add(statusText);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createChatBody() {
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(BG_CHAT);
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG_CHAT);
        chatPanel.setBorder(new EmptyBorder(14, 20, 14, 20));
        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.getViewport().setBackground(BG_CHAT);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.getVerticalScrollBar().setUI(new MinimalScrollBarUI());
        chatScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        body.add(chatScrollPane, BorderLayout.CENTER);
        body.add(createUserListPanel(), BorderLayout.EAST);
        return body;
    }

    private JPanel createUserListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(190, 0));
        panel.setBackground(SIDEBAR_BG);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_LIGHT));
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SIDEBAR_BG);
        header.setBorder(new EmptyBorder(14, 16, 10, 16));
        userCountLabel = new JLabel("Online (0)");
        userCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userCountLabel.setForeground(TEXT_LIGHT);
        header.add(userCountLabel, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(SIDEBAR_BG);
        userList.setForeground(TEXT_DARK);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setFixedCellHeight(36);
        userList.setSelectionBackground(new Color(0, 122, 255, 15));
        userList.setSelectionForeground(TEXT_DARK);
        userList.setBorder(new EmptyBorder(0, 4, 4, 4));
        userList.setCellRenderer(new UserListRenderer());
        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new MinimalScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel tipLabel = new JLabel("<html><center><span style='font-size:9px;color:#AEAEB2'>ketik /pm nama pesan<br>untuk private message</span></center></html>");
        tipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tipLabel.setBorder(new EmptyBorder(6, 8, 12, 8));
        panel.add(tipLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createInputBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_LIGHT);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));

        messageField = new JTextField() {
            private float focusAnim = 0f;
            private Timer focusTimer;
            {
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { animFocus(true); }
                    @Override public void focusLost(FocusEvent e)   { animFocus(false); }
                });
            }
            private void animFocus(boolean in) {
                if (focusTimer != null) focusTimer.stop();
                focusTimer = new Timer(16, e -> {
                    focusAnim = in ? Math.min(1f, focusAnim + 0.15f) : Math.max(0f, focusAnim - 0.15f);
                    repaint();
                    if ((in && focusAnim >= 1f) || (!in && focusAnim <= 0f)) ((Timer)e.getSource()).stop();
                });
                focusTimer.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                if (focusAnim > 0) {
                    g2.setColor(new Color(0, 122, 255, (int)(focusAnim * 45)));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        messageField.setOpaque(false);
        messageField.setForeground(TEXT_DARK);
        messageField.setCaretColor(ACCENT);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(new EmptyBorder(10, 18, 10, 18));
        messageField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
            }
        });
        messageField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (messageField.getText().equals("Ketik pesan...")) {
                    messageField.setText("");
                    messageField.setForeground(TEXT_DARK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setText("Ketik pesan...");
                    messageField.setForeground(TEXT_LIGHT);
                }
            }
        });
        messageField.setText("Ketik pesan...");
        messageField.setForeground(TEXT_LIGHT);

        sendButton = new JButton("Kirim") {
            private float hoverAnim = 0f;
            private Timer hoverTimer;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { animH(true); }
                    @Override public void mouseExited(MouseEvent e)  { animH(false); }
                });
            }
            private void animH(boolean in) {
                if (hoverTimer != null) hoverTimer.stop();
                hoverTimer = new Timer(16, e -> {
                    hoverAnim = in ? Math.min(1f, hoverAnim + 0.15f) : Math.max(0f, hoverAnim - 0.15f);
                    repaint();
                    if ((in && hoverAnim >= 1f) || (!in && hoverAnim <= 0f)) ((Timer)e.getSource()).stop();
                });
                hoverTimer.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = (int)(ACCENT.getRed() - 15 * hoverAnim);
                int gr = (int)(ACCENT.getGreen() - 15 * hoverAnim);
                int b = (int)(ACCENT.getBlue() - 20 * hoverAnim);
                g2.setColor(new Color(Math.max(0,r), Math.max(0,gr), Math.max(0,b)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setForeground(Color.WHITE);
        sendButton.setContentAreaFilled(false);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.addActionListener(e -> sendMessage());
        bar.add(messageField, BorderLayout.CENTER);
        bar.add(sendButton, BorderLayout.EAST);
        return bar;
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || text.equals("Ketik pesan...")) return;
        if (client != null && client.isConnected()) {
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

    private void addChatBubble(Message message) {
        SwingUtilities.invokeLater(() -> {
            JPanel bubble;
            switch (message.getType()) {
                case CHAT:
                case PRIVATE:
                    bubble = createMessageBubble(message, message.getSender().equals(username));
                    break;
                case JOIN:
                    bubble = createSystemBubble(message.getContent(), ACCENT_GREEN);
                    break;
                case LEAVE:
                    bubble = createSystemBubble(message.getContent(), ACCENT_ORANGE);
                    break;
                default:
                    bubble = createSystemBubble(message.getContent(), TEXT_LIGHT);
            }

            JPanel animWrapper = new JPanel(new BorderLayout()) {
                private float opacity = 0f;
                private float offsetY = 10f;
                private Timer anim;
                {
                    setOpaque(false);
                    anim = new Timer(16, e -> {
                        opacity = Math.min(1f, opacity + 0.12f);
                        offsetY = Math.max(0f, offsetY - 1.2f);
                        repaint();
                        if (opacity >= 1f) ((Timer)e.getSource()).stop();
                    });
                    anim.start();
                }
                @Override public void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    g2.translate(0, (int)offsetY);
                    super.paintComponent(g2);
                    g2.dispose();
                }
                @Override public void paintChildren(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    g2.translate(0, (int)offsetY);
                    super.paintChildren(g2);
                    g2.dispose();
                }
            };
            animWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            animWrapper.add(bubble, BorderLayout.CENTER);
            chatPanel.add(animWrapper);
            chatPanel.add(Box.createVerticalStrut(4));
            chatPanel.revalidate();
            chatPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar vb = chatScrollPane.getVerticalScrollBar();
                vb.setValue(vb.getMaximum());
            });
        });
    }

    private JPanel createMessageBubble(Message message, boolean isSelf) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        boolean isPrivate = message.getType() == Message.Type.PRIVATE;

        JPanel bubblePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isPrivate ? ACCENT_PURPLE : (isSelf ? BG_MSG_SELF : BG_MSG_OTHER);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                if (!isSelf && !isPrivate) {
                    g2.setColor(BORDER_LIGHT);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                }
                g2.dispose();
            }
        };
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        if (!isSelf) {
            JLabel senderLabel = new JLabel(message.getSender());
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            senderLabel.setForeground(getColorForUser(message.getSender()));
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(senderLabel);
            bubblePanel.add(Box.createVerticalStrut(2));
        }

        if (isPrivate) {
            String pmText = "Private" + (isSelf ? " \u2192 " + message.getRecipient() : "");
            JLabel pmLabel = new JLabel(pmText);
            pmLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            pmLabel.setForeground(new Color(255, 255, 255, 170));
            pmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(pmLabel);
            bubblePanel.add(Box.createVerticalStrut(2));
        }

        boolean isDark = isSelf || isPrivate;
        JLabel messageLabel = new JLabel("<html><body style='width:auto; max-width:280px'>" + escapeHtml(message.getContent()) + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(isDark ? Color.WHITE : TEXT_DARK);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubblePanel.add(messageLabel);

        JLabel timeLabel = new JLabel(message.getTimestamp());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isDark ? new Color(255,255,255,130) : TEXT_LIGHT);
        timeLabel.setAlignmentX(isSelf ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        bubblePanel.add(Box.createVerticalStrut(2));
        bubblePanel.add(timeLabel);

        JPanel alignPanel = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(bubblePanel);
        wrapper.add(alignPanel, isSelf ? BorderLayout.EAST : BorderLayout.WEST);
        return wrapper;
    }

    private String escapeHtml(String text) {
        return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private JPanel createSystemBubble(String text, Color color) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel label = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 14));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(color);
        label.setBorder(new EmptyBorder(3, 12, 3, 12));
        wrapper.add(label);
        return wrapper;
    }

    private Color getColorForUser(String name) {
        Color[] colors = {
            new Color(0, 122, 255), new Color(52, 199, 89), new Color(255, 149, 0),
            new Color(175, 82, 222), new Color(255, 59, 48), new Color(90, 200, 250),
            new Color(255, 204, 0), new Color(255, 45, 85),
        };
        return colors[Math.abs(name.hashCode()) % colors.length];
    }

    @Override public void onConnected(String serverIP, int port) {
        SwingUtilities.invokeLater(() -> {
            String role = isHost ? "Host" : "Client";
            connectionInfo.setText(role + " \u2022 " + serverIP + ":" + port);
            statusDot.setForeground(ACCENT_GREEN);
            statusText.setText("Online");
            statusText.setForeground(ACCENT_GREEN);
            messageField.requestFocusInWindow();
        });
    }

    @Override public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            statusDot.setForeground(ACCENT_RED);
            statusText.setText("Offline");
            statusText.setForeground(ACCENT_RED);
            addChatBubble(new Message("System", "Terputus dari server.", Message.Type.SYSTEM));
        });
    }

    @Override public void onMessageReceived(Message message) { addChatBubble(message); }

    @Override public void onUserListUpdated(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.trim().isEmpty()) userListModel.addElement(user.trim());
            }
            userCountLabel.setText("Online (" + userListModel.size() + ")");
        });
    }

    @Override public void onError(String error) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private class UserListRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String user = value.toString();
            boolean isSelf = user.equals(username);
            label.setText("  \u25CF  " + user + (isSelf ? " (Kamu)" : ""));
            label.setFont(new Font("Segoe UI", isSelf ? Font.BOLD : Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(6, 8, 6, 8));
            label.setBackground(isSelected ? new Color(0, 122, 255, 15) : SIDEBAR_BG);
            label.setForeground(isSelf ? ACCENT : TEXT_DARK);
            return label;
        }
    }

    static class MinimalScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(199, 199, 204);
            trackColor = new Color(0, 0, 0, 0);
        }
        @Override protected JButton createDecreaseButton(int o) { return zBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zBtn(); }
        private JButton zBtn() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(199, 199, 204, 100));
            g2.fillRoundRect(r.x + 1, r.y, r.width - 2, r.height, 6, 6);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}
    }
}
