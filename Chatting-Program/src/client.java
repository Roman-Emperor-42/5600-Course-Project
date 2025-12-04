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
        try {
            String userInput = "exit";

            serverOut.writeUTF(userInput);

            String serverBye = serverIn.readUTF();
            System.out.println("Server: " + serverBye);

        } catch (IOException e) {
            System.out.println(e.getMessage());
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

        // Send hellow message
        String m = "Hello from Client-" + getHost();
        try {
            serverOut.writeUTF(m);

            // Echo server response
            String serverResponse = serverIn.readUTF();
            System.out.println("Server: " + serverResponse);
        } catch (IOException i) {
            System.out.println(i);
        }

        // Java is quirky and will auto send a blank line if you don't take an input before ¯\O/¯ (this is a shrugging emoji cause idk why it does thisz)
        String userInput = mainScanner.nextLine();

        // Keep reading until "exit" is input
        while (true) {
            try {
                // Get and send message
                System.out.println("Enter a message to send (or exit to quit): ");
                userInput = mainScanner.nextLine();

                if (userInput.trim().isEmpty()) {
                    System.out.println("Please enter a message.");
                    continue;
                }

                if (userInput.startsWith("exit")) {
                    disconnect();
                    break;
                }

                serverOut.writeUTF(userInput);

                // Echo response from server
                // String echo = serverIn.readUTF();
                // System.out.println(echo);

            }
            // Catch IO exceptions
            catch (IOException i) {
                System.out.println(i);
                break;
            }
            // Catch other exceptions
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Close the connections
        try {
            mainScanner.close();
            serverIn.close();
            serverOut.close();
            s.close();
        }
        // Catch errors
        catch (IOException i) {
            System.out.println(i);
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
        client c = new client(ip, port);
        mainScanner.close();
    }
}