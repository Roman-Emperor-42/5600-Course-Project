import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class client {
    private Socket s = null;
    private DataOutputStream serverOut = null;
    private DataInputStream serverIn = null;
    private static Scanner mainScanner = null;
    private boolean running = true;

    // Try to get machine's Host name, if fail throw exception
    private static String getHost() {
        String hostname = null;
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            hostname = ipAddress.getHostName();
        }
        // Catch exceptions
        catch (UnknownHostException e) {
            System.err.println("Could not determine local host information: " + e.getMessage());
            e.printStackTrace();
        }
        return hostname;
    }

    // Try to get machine's IP address, if fail throw exception
    private static String getIP() {
        String ipString = null;
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            ipString = ipAddress.getHostAddress();
        }
        // Catch exceptions
        catch (UnknownHostException e) {
            System.err.println("Could not determine local host information: " + e.getMessage());
            e.printStackTrace();
        }
        return ipString;
    }

    // Simpler disconnect
    private void disconnect() {
        running = false;
        try {
            mainScanner.close();
            serverIn.close();
            serverOut.close();
            s.close();
        } catch (IOException e) {
            System.out.println("Error closing connections: " + e.getMessage());
        }
    }

    public client(String addr, int port) {
        // Establish a connection
        try {
            s = new Socket(addr, port);
            System.out.println("Connected");

            // Sends output to the socket
            serverOut = new DataOutputStream(s.getOutputStream());
            serverIn = new DataInputStream(s.getInputStream());
            mainScanner = new Scanner(System.in);

            Thread receiveThread = new Thread(() -> {
                try {
                    while (running) {
                        String serverMessage = serverIn.readUTF();
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    if (running) {
                        System.out.println("\nDisconnected from server.");
                    }
                }
            });
            receiveThread.start();

            // Read welcome messages
            System.out.println(serverIn.readUTF());  // Welcome message
            System.out.println(serverIn.readUTF());  // Client count
            System.out.println("Type 'exit' to quit\n");

            String m;
            while (running) {
                System.out.print("You: ");
                m = mainScanner.nextLine();

                // Send message to server
                serverOut.writeUTF(m);

                if (m.equalsIgnoreCase("exit")) {
                    running = false;
                    System.out.println("Disconnecting...");
                    break;
                }
            }
            Thread.sleep(100); // Small delay to receive goodbye
            System.out.println("Connection closed.");
        }
        // Print Unknown Host
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        // Print Error
        catch (IOException i) {
            System.out.println(i);
            return;
        }
        catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        finally {
            disconnect();
        }
    }

    public static void main(String[] args) {
        // Print out Ip and Hostname to differentiate computers
        System.out.println("Your IP is: " + getIP());
        System.out.println("Your hostname is: " + getHost());

        // Scanner to get IP and port
        mainScanner = new Scanner(System.in);
        System.out.println("Enter IP Address:");
        String ip = mainScanner.nextLine();
        System.out.println("Enter Port:");
        int port = mainScanner.nextInt();

        // Start client
        new client(ip, port);
        mainScanner.close();
    }
}