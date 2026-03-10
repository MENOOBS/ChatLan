package chatlan.server;

import chatlan.model.Message;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients;
    private ExecutorService pool;
    private boolean running;
    private ServerListener listener;

    public interface ServerListener {
        void onServerStarted(int port, String ipAddress);
        void onServerStopped();
        void onClientJoined(String username, int totalClients);
        void onClientLeft(String username, int totalClients);
        void onMessageReceived(Message message);
        void onError(String error);
    }

    public ChatServer(int port, ServerListener listener) {
        this.port = port;
        this.listener = listener;
        this.clients = new ConcurrentHashMap<>();
        this.pool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            String ipAddress = getLocalIPAddress();
            listener.onServerStarted(port, ipAddress);

            pool.execute(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket);
                        pool.execute(handler);
                    } catch (IOException e) {
                        if (running) {
                            listener.onError("Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (IOException e) {
            listener.onError("Could not start server on port " + port + ": " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            // Notify all clients
            broadcast(new Message("Server", "Server shutting down...", Message.Type.SYSTEM));
            // Close all client connections
            for (ClientHandler handler : clients.values()) {
                handler.disconnect();
            }
            clients.clear();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdown();
            listener.onServerStopped();
        } catch (IOException e) {
            listener.onError("Error stopping server: " + e.getMessage());
        }
    }

    private void broadcast(Message message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }

    private void broadcastExcept(Message message, String excludeUser) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    private void sendUserList() {
        String userList = String.join(",", clients.keySet());
        Message msg = new Message("Server", userList, Message.Type.USER_LIST);
        broadcast(msg);
    }

    private String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            listener.onError("Could not get IP: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    public int getClientCount() {
        return clients.size();
    }

    // ─── Inner Class: ClientHandler ───────────────────────────────────
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // First message should be the username
                Message joinMsg = (Message) in.readObject();
                username = joinMsg.getSender();

                // Check if username is taken
                if (clients.containsKey(username)) {
                    sendMessage(new Message("Server", "Username '" + username + "' sudah dipakai. Silakan gunakan nama lain.", Message.Type.SYSTEM));
                    socket.close();
                    return;
                }

                clients.put(username, this);
                listener.onClientJoined(username, clients.size());

                // Broadcast join notification
                Message notification = new Message(username, username + " bergabung ke chat!", Message.Type.JOIN);
                broadcast(notification);
                listener.onMessageReceived(notification);
                sendUserList();

                // Listen for messages
                while (running) {
                    Message message = (Message) in.readObject();
                    if (message.getType() == Message.Type.CHAT) {
                        broadcast(message);
                        listener.onMessageReceived(message);
                    } else if (message.getType() == Message.Type.PRIVATE) {
                        // Send to recipient
                        ClientHandler recipientHandler = clients.get(message.getRecipient());
                        if (recipientHandler != null) {
                            recipientHandler.sendMessage(message);
                            // Also send back to sender
                            sendMessage(message);
                        } else {
                            sendMessage(new Message("Server", "User '" + message.getRecipient() + "' tidak ditemukan.", Message.Type.SYSTEM));
                        }
                        listener.onMessageReceived(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                // Client disconnected
            } finally {
                if (username != null) {
                    clients.remove(username);
                    Message leaveMsg = new Message(username, username + " meninggalkan chat.", Message.Type.LEAVE);
                    broadcast(leaveMsg);
                    listener.onMessageReceived(leaveMsg);
                    listener.onClientLeft(username, clients.size());
                    sendUserList();
                }
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }

        public void sendMessage(Message message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                // Failed to send
            }
        }

        public void disconnect() {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }
}
