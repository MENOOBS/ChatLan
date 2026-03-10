package chatlan.client;

import chatlan.model.Message;

import java.io.*;
import java.net.*;

public class ChatClient {
    private String serverIP;
    private int serverPort;
    private String username;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected;
    private ClientListener listener;

    public interface ClientListener {
        void onConnected(String serverIP, int port);
        void onDisconnected();
        void onMessageReceived(Message message);
        void onUserListUpdated(String[] users);
        void onError(String error);
    }

    public ChatClient(String serverIP, int serverPort, String username, ClientListener listener) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.username = username;
        this.listener = listener;
    }

    public void connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIP, serverPort), 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;

            sendMessage(new Message(username, "", Message.Type.JOIN));
            listener.onConnected(serverIP, serverPort);

            Thread listenThread = new Thread(this::listenForMessages);
            listenThread.setDaemon(true);
            listenThread.start();

        } catch (IOException e) {
            listener.onError("Gagal terhubung ke server " + serverIP + ":" + serverPort + "\n" + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            while (connected) {
                Message message = (Message) in.readObject();
                if (message.getType() == Message.Type.USER_LIST) {
                    String[] users = message.getContent().split(",");
                    listener.onUserListUpdated(users);
                } else {
                    listener.onMessageReceived(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                connected = false;
                listener.onDisconnected();
            }
        }
    }

    public void sendChat(String content) {
        if (connected) {
            sendMessage(new Message(username, content, Message.Type.CHAT));
        }
    }

    public void sendPrivate(String recipient, String content) {
        if (connected) {
            sendMessage(new Message(username, content, Message.Type.PRIVATE, recipient));
        }
    }

    private void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            listener.onError("Gagal mengirim pesan: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {}
        listener.onDisconnected();
    }

    public boolean isConnected() {
        return connected;
    }

    public String getUsername() {
        return username;
    }
}
