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
    private int clientCount = 0;


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


    // Constructor with port
    public server(int port) {
        try {
            // start server and wait for client
            ss = new ServerSocket(port);
            System.out.println("Multi user chat server started on port " + port);
            boolean clientConnected = false;
            String m;

            while (true) {
                System.out.println("Waiting for a client ...");


                // wait for client to connect
                s = ss.accept();
                System.out.println("Client connected: " + s.getInetAddress());
                clientConnected = true;


                // Takes input from the client socket
                in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                out = new DataOutputStream(s.getOutputStream());

                out.writeUTF("Welcome to the Chat Server! Type 'exit' to quit.");
                // Reads message from client until termination is sent
                // Input message

                while (clientConnected) {
                    try {
                        // Take and print input
                        m = in.readUTF();
                        System.out.println("Client: " + m);

                        if (m.equals("exit")) {
                            System.out.println("Client disconnected.");
                            clientConnected = false;
                        }

                        // Echo the message back
                        // out.writeUTF("Server: " + m);
                    }
                    catch (EOFException e) {
                        System.out.println("Client connection lost.");
                        clientConnected = false;
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
            }


            // Close connections
            //System.out.println("Client disconnected. Closing connection...");
            //s.close();
            //in.close();
            //out.close();
            //ss.close();


        }
        // Catch any exceptions (client dies)
        catch (IOException i) {
            System.out.println(i);
        }
    }


    public static void main(String args[]) {
        // Print IP address & hostname
        System.out.println("Your IP is: " + getIP());
        System.out.println("Your Hostname is: " + getHost());


        // what port to broadcast on
        Scanner in = new Scanner(System.in);
        System.out.println("Enter Port (make sure port is open):");
        int port = in.nextInt();
        in.close();
        server s = new server(port);
    }
}
