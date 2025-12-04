import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class server {
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static int clientCount = 0;

    private static String getIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    private static String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private int clientId;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        public void sendMessage(String message) {
            try {
                if (out != null) {
                    out.writeUTF(message);
                }
            } catch (IOException e) {
                System.out.println("Error sending to Client #" + clientId);
            }
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // Send welcome message
                out.writeUTF("Welcome! You are Client #" + clientId);

                System.out.println("Client #" + clientId + " connected from " +
                        socket.getInetAddress().getHostAddress());

                // Syncronized is a lock so that clients isn't changed while being added to
                synchronized(clients) {
                    clients.add(this);
                }

                // Notify all clients (including self) that new client joined
                broadcast("Client #" + clientId + " has joined the chat", this);

                String message;
                while (true) {
                    try {
                        // Take message
                        message = in.readUTF();

                        // Disconnect
                        if (message.equalsIgnoreCase("exit")) {
                            System.out.println("Client #" + clientId + " disconnected.");
                            break;
                        }

                        // Log message & client #
                        System.out.println("Client #" + clientId + ": " + message);

                        // Broadcast to ALL clients (including sender)
                        broadcast("Client #" + clientId + ": " + message, null);

                    } catch (EOFException e) {
                        System.out.println("Client #" + clientId + " disconnected unexpectedly.");
                        break;
                    }
                }

            }
            // catch error
            catch (IOException e) {
                System.out.println("Client #" + clientId + " error: " + e.getMessage());
            }
            // remove client when leave
            finally {
                // Remove from list
                synchronized(clients) {
                    clients.remove(this);
                }
                // Notify remaining clients
                broadcast("Client #" + clientId + " has left the chat", null);

                // Close client's stuff
                try {
                    in.close();
                    out.close();
                    socket.close();
                }

                // Error closing client
                catch (IOException e) {
                    System.out.println("Error closing resources for Client #" + clientId);
                }
            }
        }
    }


    // Broadcast to all users
    private static void broadcast(String message, ClientHandler exclude) {
        List<ClientHandler> clientsCopy;
        // Lock clients while broadcasting
        synchronized(clients) {
            clientsCopy = new ArrayList<>(clients);
        }
        // send to all
        for (ClientHandler client : clientsCopy) {
            // If exclude is not null, skip that client
            if (exclude == null || client != exclude) {
                client.sendMessage(message);
            }
        }
    }

    public static void main(String args[]) {
        System.out.println("Chat Server");
        System.out.println("Your IP is: " + getIP());
        System.out.println("Your Hostname is: " + getHost());

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Port (make sure port is open): ");
        int port = scanner.nextInt();
        scanner.close();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("\nServer started on port " + port);
            System.out.println("Waiting for clients...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;

                ClientHandler handler = new ClientHandler(clientSocket, clientCount);
                pool.execute(handler);
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}