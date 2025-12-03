import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {

    private Socket s = null;
    private DataOutputStream out = null;
    private DataInputStream serverIn = null;
    private static Scanner mainScanner = null;



    private static String getHost() {
        // Try to get machine's IP address, if fail throw exception
        String hostname = null;
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            hostname = ipAddress.getHostName();
        } catch (UnknownHostException e) {
            System.err.println("Could not determine local host information: " + e.getMessage());
            e.printStackTrace();
        }
        return hostname;
    }

    public client(String addr, int port) {
        // Establish a connection
        try {
            s = new Socket(addr, port);
            System.out.println("Connected");

            // Sends output to the socket
            out = new DataOutputStream(s.getOutputStream());

            serverIn = new DataInputStream(s.getInputStream());
        }
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }

        // String to read message from input
        String m = "Hello from Client-" + getHost();
        try {
            out.writeUTF(m);
            System.out.println("Client: " + m);

            String serverResponse = serverIn.readUTF();
            System.out.println("Server: " + serverResponse);
        }
        catch (IOException i) {
            System.out.println(i);
        }

        // Keep reading until "Over" is input
        while (true) {
            try {
                System.out.print("Enter message (or type 'Bye from Client-" + getHost() + "' to quit): ): ");
                String userInput = mainScanner.nextLine();

                out.writeUTF(userInput);
                System.out.println("Client: " + userInput);

                if (userInput.startsWith("Bye from Client-")) {
                    String serverBye = serverIn.readUTF();
                    System.out.println("Server: " + serverBye);
                    break;
                }

                String echo = serverIn.readUTF();
                System.out.println(echo);
            }
            catch (IOException i) {
                System.out.println(i);
                break;
            }
        }

        // Close the connection
        try {
            mainScanner.close();
            serverIn.close();
            out.close();
            s.close();
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        mainScanner = new Scanner(System.in);
        System.out.println("Enter IP Address:");
        String ip = mainScanner.nextLine();

        System.out.println("Enter Port:");
        int port = mainScanner.nextInt();
        //mainScanner.close();

        client c = new client(ip, port);
    }
}