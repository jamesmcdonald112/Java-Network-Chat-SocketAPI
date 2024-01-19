package ie.atu.sw;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Properties;

/**
 * The ChatClient class establishes a connection to the chat server and
 * handles sending and receiving messages.
 */
public class ChatClient {
    private static String serverAddress;
    private static int serverPort;

    /**
     * The main method to start the chat client.
     * It loads configuration, establishes a connection, and handles user input and server messages.
     *
     * @param args Command line arguments which may contain server address and port.
     */
    public static void main(String[] args) {
        // Load server configuration from properties file
        loadConfiguration();

        // Override server address and port if provided via command line arguments
        if (args.length > 0) {
            serverAddress = args[0];
        }
        if (args.length > 1) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                ConsoleDisplay.displayErrorMessage("Invalid command line port number. Using port from config file or default port.");
            }
        }

        try (Socket socket = establishConnection();
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter serverOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             Scanner scanner = new Scanner(System.in)
        ) {
            // Start a thread to listen for messages from the server
            Thread listenerThread = new Thread(() -> listenForServerMessages(serverInput));
            listenerThread.start();

            // Handle user input and send messages to the server
            handleUserInput(scanner, serverOutput);

            // Wait for the listener thread to finish
            listenerThread.join();
        } catch (IOException | InterruptedException e) {
            ConsoleDisplay.displayErrorMessage("Connection error: " + e.getMessage());
        }
    }


    /**
     * Loads server configuration from a properties file.
     * Sets the server address and port based on the properties file or uses default values.
     */
    private static void loadConfiguration() {
        Properties prop = new Properties();
        try (InputStream input = ChatClient.class.getClassLoader().getResourceAsStream("ie/atu/sw/config.properties")) {
            if (input == null) {
                throw new FileNotFoundException("config.properties file not found in classpath");
            }
            prop.load(input);
            serverAddress = prop.getProperty("server_address", "localhost");
            serverPort = Integer.parseInt(prop.getProperty("server_port", "20000"));
        } catch (IOException e) {
            ConsoleDisplay.displayErrorMessage("Error reading configuration file: " + e.getMessage());
        } catch (NumberFormatException e) {
            ConsoleDisplay.displayErrorMessage("Invalid port number in configuration. Using default port.");
            serverPort = 20000;
        }
    }


    /**
     * Establishes a connection to the chat server, retries if necessary.
     *
     * @return A connected socket to the server.
     * @throws IOException If unable to connect after several attempts.
     */
    private static Socket establishConnection() throws IOException {
        int attempts = 0;
        while (true) {
            try {
                ConsoleDisplay.displayConfirmationMessage("Connecting to the chat server...");
                ProgressMeter.runProgressMeter();
                return new Socket(serverAddress, serverPort);
            } catch (IOException e) {
                ConsoleDisplay.displayErrorMessage("Cannot connect to the server: " + e.getMessage());
                if (++attempts >= 3) {
                    ConsoleDisplay.displayErrorMessage("Failed to connect after multiple attempts. Exiting.");
                    System.exit(1);
                }
                ConsoleDisplay.displayConfirmationMessage("Retrying connection...");
            } catch (InterruptedException e) {
                ConsoleDisplay.displayErrorMessage("Connection attempt interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                throw new IOException("Connection attempt interrupted", e);
            }
        }
    }

    /**
     * Handles user input, sending messages to the server.
     * It reads user input from the console and sends it to the server.
     *
     * @param scanner      A Scanner object for reading user input.
     * @param serverOutput A BufferedWriter to send messages to the server.
     * @throws IOException If an I/O error occurs.
     */
    private static void handleUserInput(Scanner scanner, BufferedWriter serverOutput) throws IOException {
        ConsoleDisplay.displayPrompt("Enter your username: ");
        String username = scanner.nextLine();
        serverOutput.write(username);
        serverOutput.newLine();
        serverOutput.flush();

        ConsoleDisplay.displayConfirmationMessage("Welcome to the Chat, " + username + "!");
        ConsoleDisplay.displayMessage("Type a message and hit Enter to send. Type '\\q' to quit.");

        String userInput;
        while (!(userInput = scanner.nextLine()).equals("\\q")) {
            serverOutput.write(userInput);
            serverOutput.newLine();
            serverOutput.flush();
        }
        ConsoleDisplay.displayConfirmationMessage("You have left the chat.");
        safelyCloseResource(serverOutput);
    }


    /**
     * Listens for messages from the server and displays them to the user.
     * It continuously reads messages from the server and prints them to the console.
     *
     * @param serverInput A BufferedReader to read messages from the server.
     */
    private static void listenForServerMessages(BufferedReader serverInput) {
        try {
            String message;
            while ((message = serverInput.readLine()) != null) {
                if (message.startsWith("You:")) {
                    ConsoleDisplay.displayConfirmationMessage(message);
                } else {
                    ConsoleDisplay.displayMessage(message);
                }
            }
        } catch (IOException e) {
            ConsoleDisplay.displayMessage("Disconnected from server.");
        }
    }


    /**
     * Safely closes a resource, handling any IOException that might occur.
     * This method is used to close resources like streams and sockets.
     *
     * @param resource The resource to close.
     */
    private static void safelyCloseResource(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                ConsoleDisplay.displayErrorMessage("Failed to close resource: " + e.getMessage());
            }
        }
    }
}
