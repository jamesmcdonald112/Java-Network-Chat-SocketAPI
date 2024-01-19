package ie.atu.sw;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ChatServer class that initializes and manages a multi-threaded chat server.
 * It listens for client connections on a specified port and handles them using ChatClientHandler instances.
 */
public class ChatServer {
    private static final Set<ChatClientHandler> activeClients = Collections.synchronizedSet(new HashSet<>());
    private static int port = 20000; // Default port
    private static final ExecutorService connectionThreadPool = Executors.newFixedThreadPool(4);
    private static volatile boolean isServerRunning = true;

    /**
     * Main method to start the chat server.
     * It loads server configuration, initialises the server socket, and starts listening for
     * client connections.
     *
     * @param args Command line arguments, optionally containing the server port.
     */
    public static void main(String[] args) {
        // Load server configuration from properties file
        loadConfiguration();

        // Override server port if provided via command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                ConsoleDisplay.displayErrorMessage("Invalid command line port number. Using port from config file.");
            }
        }

        try {
            ServerSocket serverSocket = initialiseServer();
            startShutdownListener(serverSocket);
            acceptClientConnections(serverSocket);
        } catch (IOException | InterruptedException e) {
            ConsoleDisplay.displayErrorMessage("Server error: " + e.getMessage());
        }
    }

    /**
     * Loads the server configuration from a properties file.
     * It sets the server port based on the configuration or uses a default value.
     */
    private static void loadConfiguration() {
        Properties prop = new Properties();
        try (InputStream input = ChatServer.class.getClassLoader().getResourceAsStream("ie/atu/sw/config.properties")) {
            if (input == null) {
                throw new FileNotFoundException("config.properties file not found in classpath");
            }
            prop.load(input);
            port = Integer.parseInt(prop.getProperty("server_port", "20000")); // Default to 20000 if not found
        } catch (FileNotFoundException e) {
            ConsoleDisplay.displayErrorMessage("Configuration file not found. Using default port.");
            port = 20000; // Default port if file not found
        } catch (IOException e) {
            ConsoleDisplay.displayErrorMessage("Error reading configuration file. Using default port.");
            port = 20000;
        } catch (NumberFormatException e) {
            ConsoleDisplay.displayErrorMessage("Invalid port number in configuration. Using default port.");
            port = 20000;
        }
    }


    /**
     * Initialises and returns the server socket for the chat server.
     * This method also displays server startup messages.
     *
     * @return The initialised ServerSocket.
     * @throws IOException          If an error occurs during server socket initialization.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    private static ServerSocket initialiseServer() throws IOException, InterruptedException {
        ConsoleDisplay.displayConfirmationMessage("Chat Server is starting...");
        ProgressMeter.runProgressMeter();
        ServerSocket serverSocket = new ServerSocket(port); // No IP address specified, defaults to localhost
        ConsoleDisplay.displayConfirmationMessage("Chat Server is running on port " + port);
        ConsoleDisplay.displayPrompt("Press '\\q' to shut down the server.");
        return serverSocket;
    }

    /**
     * Accepts incoming client connections and handles them using ChatClientHandler instances.
     * Continuously listens for new client connections as long as the server is running.
     *
     * @param serverSocket The server socket to listen on for incoming connections.
     */
    private static void acceptClientConnections(ServerSocket serverSocket) {
        while (isServerRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ChatClientHandler clientHandler = new ChatClientHandler(clientSocket);
                activeClients.add(clientHandler);
                connectionThreadPool.execute(clientHandler);
            } catch (SocketException e) {
                if (!isServerRunning) {
                    ConsoleDisplay.displayConfirmationMessage("Server is no longer accepting connections.");
                }
            } catch (IOException e) {
                if (isServerRunning) {
                    ConsoleDisplay.displayErrorMessage("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Starts a separate thread that listens for a shutdown command in the console.
     * When '\q' is entered, it starts the shutdown of the server.
     *
     * @param serverSocket The server socket to close upon shutdown.
     */
    private static void startShutdownListener(ServerSocket serverSocket) {
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isServerRunning) {
                    if ("\\q".equals(scanner.nextLine())) {
                        shutdownServer(serverSocket);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Shuts down the chat server, closing all client connections and the server socket.
     * Releases all resources associated with the server.
     *
     * @param serverSocket The server socket to close during the shutdown.
     * @throws IOException If an error occurs while closing the server socket.
     */
    private static void shutdownServer(ServerSocket serverSocket) throws IOException {
        isServerRunning = false;
        ChatClientHandler.notifyServerShutdown();
        ChatClientHandler.disconnectAllClients();
        serverSocket.close();
        connectionThreadPool.shutdownNow();
        ConsoleDisplay.displayConfirmationMessage("Server has been shutdown.");
    }


}
