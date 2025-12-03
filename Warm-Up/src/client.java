import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {

    private Socket s = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;


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

    public client(String addr, int port)
    {
        // Establish a connection
        try {
            s = new Socket(addr, port);
            System.out.println("Connected");

            // Takes input from terminal
            in = new DataInputStream(System.in);

            // Sends output to the socket
            out = new DataOutputStream(s.getOutputStream());
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
        }
        catch (IOException i) {
            System.out.println(i);
        }

        // Keep reading until "Over" is input
        while (!m.equals("Over")) {
            try {
                m = in.readLine();
                out.writeUTF(m);
            }
            catch (IOException i) {
                System.out.println(i);
            }
        }

        // Close the connection
        try {
            in.close();
            out.close();
            s.close();
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter IP Address:");
        String ip = in.nextLine();

        System.out.println("Enter Port:");
        int port = in.nextInt();

        client c = new client(ip, port);
    }
}