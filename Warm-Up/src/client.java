import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;

public class client {
    private Socket s = null;
    private DataOutputStream ServerOut = null;
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
    private void disconnect(){
        try {
        String userInput = "Bye from Client-" + getHost();

        ServerOut.writeUTF(userInput);

        String serverBye = serverIn.readUTF();
        System.out.println("Server: " + serverBye);

        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void sendFile(String path) throws Exception
    {
        ServerOut = new DataOutputStream(s.getOutputStream());
        int bytes = 0;
        // Open the File where he located in your pc
        File file = new File(path);
        FileInputStream fileInputStream
                = new FileInputStream(file);

        // Here we send the File to Server
        ServerOut.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            ServerOut.write(buffer, 0, bytes);
            ServerOut.flush();
        }
        // close the file here
        fileInputStream.close();
    }

    public client(String addr, int port) {
        // Establish a connection
        try {
            s = new Socket(addr, port);
            System.out.println("Connected");

            // Sends output to the socket
            ServerOut = new DataOutputStream(s.getOutputStream());
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
            ServerOut.writeUTF(m);

            // Echo server response
            String serverResponse = serverIn.readUTF();
            System.out.println("Server: " + serverResponse);
        } catch (IOException i) {
            System.out.println(i);
        }

        // Java is quirky and will auto send a blank line if you don't take an input before ¯\O/¯ (this is a shrugging emoji cause idk why it does thisz)
        String userInput = mainScanner.nextLine();

        // Keep reading until "Bye from Client-'your hostname'" is input
        while (true) {
            try {
                // Take input
                System.out.print("Select from below; \n1. Enter message \n2. Send file \n 3. Exit (or type 'Bye from Client-" + getHost() + "' to quit): ");
                userInput = mainScanner.nextLine();

                if (userInput.equals("1")) {
                    //Send message
                    System.out.println("Enter a message to send: ");
                    userInput = mainScanner.nextLine();

                    if (userInput.trim().isEmpty()) {
                        System.out.println("Please enter a message.");
                        continue;
                    }

                    if (userInput.startsWith("Bye from Client-" + getHost())) {
                        disconnect();
                        break;
                    }

                    ServerOut.writeUTF(userInput);

                    // Echo response from server
                    String echo = serverIn.readUTF();
                    System.out.println(echo);
                } else if (userInput.equals("2")) {
                    // send file call here
                } else if (userInput.startsWith("Bye from Client-" + getHost()) || userInput.equals("3")) {
                    disconnect();
                    break;
                } else {
                    System.out.println("Invalid input");
                }
            }
            // Catch exceptions
            catch (IOException i) {
                System.out.println(i);
                break;
            }
        }

        // Close the connections
        try {
            mainScanner.close();
            serverIn.close();
            ServerOut.close();
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