import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class server {


    // Initialize socket and input stream
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static int clientCount = 0;


    // Try to get machine's IP address, if fail throw exception
    private static String getIP() {
        String ipString = null;
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            ipString = ipAddress.getHostAddress();
        }
        // Catch Unknown Host
        catch (UnknownHostException e) {
            System.err.println("Could not determine local host information: " + e.getMessage());
            e.printStackTrace();
        }
        return ipString;
    }


    // Try to get machine's Host name, if fail throw exception
    private static String getHost() {
        String hostname = null;
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            hostname = ipAddress.getHostName();
        }
        // Catch Unknown Host
        catch (UnknownHostException e) {
            System.err.println("Could not determine local host information: " + e.getMessage());
            e.printStackTrace();
        }
        return hostname;
    }


    private static class ClientHandler implements Runnable {
        private Socket socket;
        private int clientId;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            DataInputStream in = null;
            DataOutputStream out = null;

            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // Send welcome message
                out.writeUTF("Welcome to Multi-Client Chat Server! You are Client #" +
                        clientId + ". Type 'exit' to disconnect.");

                System.out.println("Client #" + clientId + " handler started.");

                String message;
                while (true) {
                    message = in.readUTF();

                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println("Client #" + clientId + " disconnected.");
                        break;
                    }

                    System.out.println("Client #" + clientId + ": " + message);

                    // Send acknowledgment back to client
                    out.writeUTF("Server: Message received from Client #" + clientId);
                }

            } catch (IOException e) {
                System.out.println("Client #" + clientId + " connection error: " + e.getMessage());
            } finally {
                // Close resources
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing resources for Client #" + clientId);
                }
            }
        }
    }


    // Constructor with port

    public static void main(String args[]) {
        // Print IP address & hostname
        System.out.println("Multi-Client Chat Server");
        System.out.println("Your IP is: " + getIP());
        System.out.println("Your Hostname is: " + getHost());

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Port (make sure port is open): ");
        int port = scanner.nextInt();
        scanner.close();

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("\nServer started on port " + port);
            System.out.println("Waiting for client connections...");
            System.out.println("This server can handle multiple clients simultaneously.");
            System.out.println("Press Ctrl+C to stop the server.\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;

                System.out.println("New client #" + clientCount + " connected from: " +
                        clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientCount);
                pool.execute(clientHandler);
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            // Clean up
            pool.shutdown();
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
}

