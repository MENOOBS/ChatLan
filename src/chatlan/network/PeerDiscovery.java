package chatlan.network;

import java.io.*;
import java.net.*;
import java.util.*;

public class PeerDiscovery {
    private static final int DISCOVERY_PORT = 5999;
    private static final String MAGIC = "CHATLAN_SERVER";
    private static final int BROADCAST_INTERVAL = 2000;
    private static final int DISCOVERY_TIMEOUT = 3000;

    private DatagramSocket socket;
    private boolean broadcasting = false;
    private Thread broadcastThread;

    public static String discoverServer() {
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.setSoTimeout(DISCOVERY_TIMEOUT);
            ds.setBroadcast(true);

            byte[] request = "CHATLAN_DISCOVER".getBytes();
            InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(request, request.length, broadcastAddr, DISCOVERY_PORT);
            ds.send(packet);

            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    if (iface.isLoopback() || !iface.isUp()) continue;
                    for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                        InetAddress broadcast = addr.getBroadcast();
                        if (broadcast != null) {
                            DatagramPacket subnetPacket = new DatagramPacket(
                                request, request.length, broadcast, DISCOVERY_PORT);
                            ds.send(subnetPacket);
                        }
                    }
                }
            } catch (Exception e) {}

            byte[] buffer = new byte[256];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);

            try {
                ds.receive(response);
                String data = new String(response.getData(), 0, response.getLength()).trim();
                if (data.startsWith(MAGIC + ":")) {
                    String port = data.substring(MAGIC.length() + 1);
                    String serverIP = response.getAddress().getHostAddress();
                    return serverIP + ":" + port;
                }
            } catch (SocketTimeoutException e) {
            }
        } catch (Exception e) {
            System.err.println("Discovery error: " + e.getMessage());
        }
        return null;
    }

    public void startBroadcasting(int serverPort) {
        broadcasting = true;
        broadcastThread = new Thread(() -> {
            try {
                socket = new DatagramSocket(DISCOVERY_PORT);
                socket.setBroadcast(true);

                byte[] buffer = new byte[256];

                while (broadcasting) {
                    try {
                        socket.setSoTimeout(BROADCAST_INTERVAL);
                        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                        socket.receive(request);

                        String data = new String(request.getData(), 0, request.getLength()).trim();
                        if (data.equals("CHATLAN_DISCOVER")) {
                            String response = MAGIC + ":" + serverPort;
                            byte[] responseBytes = response.getBytes();
                            DatagramPacket responsePacket = new DatagramPacket(
                                responseBytes, responseBytes.length,
                                request.getAddress(), request.getPort()
                            );
                            socket.send(responsePacket);
                        }
                    } catch (SocketTimeoutException e) {
                    }
                }
            } catch (Exception e) {
                if (broadcasting) {
                    System.err.println("Broadcast error: " + e.getMessage());
                }
            }
        });
        broadcastThread.setDaemon(true);
        broadcastThread.start();
    }

    public void stopBroadcasting() {
        broadcasting = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
