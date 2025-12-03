import java.net.*;
import java.io.*;
import java.util.Scanner;

public class server {

    // Initialize socket and input stream
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private static String getIP() {
        // Try to get machine's IP address, if fail throw exception
        String ipString = null;
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            ipString = ipAddress.getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("Could not determine local host information: " + e.getMessage());
            e.printStackTrace();
        }
        return ipString;
    }

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

    // Constructor with port
    public server(int port) {

        // Starts server and waits for a connection
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            s = ss.accept();
            System.out.println("Client accepted");

            // Takes input from the client socket
            in = new DataInputStream(
                    new BufferedInputStream(s.getInputStream())
            );
            out = new DataOutputStream(s.getOutputStream());

            String m = in.readUTF();
            System.out.println("Client: " + m);

            if (m.startsWith("Hello from Client-")) {
                // Send initial server response
                String serverHello = "Hello from Server-" + getHost();
                out.writeUTF(serverHello);
                System.out.println("Server: " + serverHello);
            }

            // Reads message from client until "Over" is sent
            while (true)
            {
                try {
                    m = in.readUTF();
                    System.out.println("Client: " + m);

                    // Check for termination condition
                    if (m.startsWith("Bye from Client-")) {
                        String serverBye = "Bye from Server-" + getHost();
                        out.writeUTF(serverBye);
                        System.out.println("Server: " + serverBye);
                        break;
                    }

                    // Echo the message back (this was missing)
                    out.writeUTF("Server: " + m);

                } catch (IOException i) {
                    System.out.println(i);
                    break;
                }
            }
            System.out.println("Closing connection");

            // Close connection
            s.close();
            in.close();
            out.close();
            ss.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[]) {

        System.out.println("Local IP Address: " + getIP());

        // what port to broadcast on
        Scanner in = new Scanner(System.in);
        System.out.println("Enter Port (make sure port is open):");
        int port = in.nextInt();
        in.close();
        server s = new server(port);
    }
}