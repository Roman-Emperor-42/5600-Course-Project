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
        // Close all things
        try {
            mainScanner.close();
            serverIn.close();
            serverOut.close();
            s.close();
        }
        // Catch exception
        catch (IOException e) {
            System.out.println("Error closing connections: " + e.getMessage());
        }
    }

        public client(String addr, int port) {
            try {
                s = new Socket(addr, port);
                System.out.println("Connected to server at " + addr + ":" + port);

                serverIn = new DataInputStream(s.getInputStream());
                serverOut = new DataOutputStream(s.getOutputStream());
                mainScanner = new Scanner(System.in);

                // Read initial message from server
                System.out.println(serverIn.readUTF());
                System.out.println("Type messages (will be sent to all clients):");

                // Create thread to receive broadcast messages (essentially runs in background to read any messages sent)
                Thread receiveThread = new Thread(() -> {
                    try {
                        while (true) {
                            String message = serverIn.readUTF();
                            System.out.println(message);
                        }
                    }
                    // Catch IOException
                    catch (IOException e) {
                        System.out.println("Disconnected from server");
                    }
                });
                // Start the thread to read incoming messages
                receiveThread.start();

                // on "main" thread we will send our messages
                String userInput;
                while (true) {
                    // Sleep main thread so that the recieved message won't be on the same line
                    Thread.sleep(100);
                    userInput = mainScanner.nextLine();
                    serverOut.writeUTF(userInput);

                    if (userInput.equalsIgnoreCase("exit")) {
                        break;
                    }
                }

                disconnect();

            }
            // Catch unknown host
            catch (UnknownHostException u) {
                System.out.println("Error: Unknown host");
            }
            // Catch IOExceptions
            catch (IOException i) {
                System.out.println("Error: " + i.getMessage());
            }
            // Catch Interupted
            catch (InterruptedException e) {
                throw new RuntimeException(e);
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