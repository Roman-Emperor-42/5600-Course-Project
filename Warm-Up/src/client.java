import java.io.*;
import java.net.*;
import java.util.Objects;
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
    private void disconnect(){
        try {
        String userInput = "Bye from Client-" + getHost();

        serverOut.writeUTF(userInput);

        String serverBye = serverIn.readUTF();
        System.out.println("Server: " + serverBye);

        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void sendFile(String path) throws IOException {
        File file = new File(path);

        // Check if file exists
        if (!file.exists()) {
            System.out.println("Error: File does not exist!");
            return;
        }

        long fileSize = file.length();

        // Send file name
        serverOut.writeUTF(file.getName());

        // Send file size
        serverOut.writeLong(fileSize);

        // Send file data in chunks
        FileInputStream serverIn = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;

        // While file has more left
        while ((bytesRead = serverIn.read(buffer)) != -1) {
            serverOut.write(buffer, 0, bytesRead);
        }
        serverIn.close();
        System.out.println("\nFile sent successfully!");

        // Wait for server to send back updated file
        System.out.println("\nWaiting for updated file from server...");
        receiveUpdatedFile();
    }

    private void receiveUpdatedFile() throws IOException {
        // Read file name from server
        String fileName = serverIn.readUTF();
        System.out.println("Receiving updated file: " + fileName);

        // Read file size from server
        long fileSize = serverIn.readLong();

        // Create output file
        FileOutputStream fileOutputStream = new FileOutputStream("client_received_" + fileName);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalReceived = 0;

        // Receive file data
        while (totalReceived < fileSize) {
            int toRead = (int) Math.min(buffer.length, fileSize - totalReceived);
            bytesRead = serverIn.read(buffer, 0, toRead);
            if (bytesRead == -1) break;

            fileOutputStream.write(buffer, 0, bytesRead);
            totalReceived += bytesRead;

        }
        fileOutputStream.close();

        // Display file content
        System.out.println("\n=== Content of updated file ===");
        BufferedReader reader = new BufferedReader(new FileReader("client_received_" + fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();

        // Clear the leftover cache, hacky fix but it should work.
        fileName = serverIn.readUTF();
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

        // Keep reading until "Bye from Client-'your hostname'" is input
        while (true) {
            try {
                // Take input
                System.out.print("Select from below; \n1. Enter message \n2. Send file \n3. Exit (or type 'Bye from Client-" + getHost() + "' to quit): ");
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

                    serverOut.writeUTF(userInput);

                    // Echo response from server
                    String echo = serverIn.readUTF();
                    System.out.println(echo);
                } else if (userInput.equals("2")) {
                    // File identifier for the server, probably a better way to do that but probably fine for an assignment
                    serverOut.writeUTF("FILE_TRANSFER");
                    System.out.println("Enter the exact path to the file");
                    String fileName = mainScanner.nextLine();
                    sendFile(fileName);
                } else if (userInput.startsWith("Bye from Client-" + getHost()) || userInput.equals("3")) {
                    disconnect();
                    break;
                } else {
                    System.out.println("Invalid input");
                }
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