import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class server {

    // Initialize socket and input stream
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

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

    private void receiveFile() throws IOException {
        // Generate unique filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // Read file name from client
        String fileName = in.readUTF();
        String savedFileName = "server_received_" + timestamp + "_" + fileName;

        // Read file size from client
        long fileSize = in.readLong();

        // Save file locally
        FileOutputStream fileOutputStream = new FileOutputStream(savedFileName);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalReceived = 0;

        while (totalReceived < fileSize) {
            int toRead = (int) Math.min(buffer.length, fileSize - totalReceived);
            bytesRead = in.read(buffer, 0, toRead);
            if (bytesRead == -1) break;

            fileOutputStream.write(buffer, 0, bytesRead);
            totalReceived += bytesRead;
        }
        fileOutputStream.close();
        System.out.println("\nFile saved as: " + savedFileName);

        // Print file content
        System.out.println("\nFile Content:");
        BufferedReader reader = new BufferedReader(new FileReader(savedFileName));
        String line;
        int lineCount = 0;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            lineCount++;
        }
        reader.close();

        // Append new line to file
        System.out.println("\nAppending new line to file...");
        FileWriter fileWriter = new FileWriter(savedFileName, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        String newLine = "This is an added line from a server";
        bufferedWriter.newLine();
        bufferedWriter.write(newLine);
        bufferedWriter.close();

        // Send updated file back to client
        sendUpdatedFile(savedFileName, fileName);
    }

    private void sendUpdatedFile(String filePath, String originalName) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();

        System.out.println("\nSending updated file back to client...");

        // Send file name (prepend "updated_" to original name)
        out.writeUTF("updated_" + originalName);

        // Send file size
        out.writeLong(fileSize);

        // Send file data
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;

        // While file has file left
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fileInputStream.close();
        System.out.println("\nUpdated file sent to client!");
    }

    // Constructor with port
    public server(int port) {
        try {
            // start server and wait for client
            ss = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            // wait for client to connect
            s = ss.accept();
            System.out.println("Client accepted");

            // Takes input from the client socket
            in = new DataInputStream(
                    new BufferedInputStream(s.getInputStream())
            );
            out = new DataOutputStream(s.getOutputStream());

            // Read and print client input
            String m = in.readUTF();
            System.out.println("Client: " + m);

            // If hello message respond with hello from server
            if (m.startsWith("Hello from Client-")) {
                String serverHello = "Hello from Server-" + getHost();
                out.writeUTF(serverHello);
            }

            // Reads message from client until termination is sent
            while (true) {
                try {
                    // Take and print input
                    m = in.readUTF();
                    System.out.println("Client: " + m);


                    // Check for termination condition
                    if (m.startsWith("Bye from Client-")) {
                        m = "Bye from Server-" + getHost();
                        out.writeUTF(m);
                        System.out.println("Server: " + m);
                        break;
                    }

                    // Check for file transfer, probably a better way to do this
                    if (m.equals("FILE_TRANSFER")) {
                        receiveFile();
                    }

                    // Echo the message back
                    out.writeUTF("Server: " + m);
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

            // Close connections

            System.out.println("Closing connection");
            s.close();
            in.close();
            out.close();
            ss.close();

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