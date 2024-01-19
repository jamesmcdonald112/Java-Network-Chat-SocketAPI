package ie.atu.sw;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * The ChatClientHandler class manages the communication with an individual chat client.
 * It handles incoming messages from the client and broadcasts them to other clients.
 */
public class ChatClientHandler implements Runnable {
    // ConcurrentHashMap to hold active client handlers
    private static final ConcurrentHashMap<String, ChatClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static volatile boolean isServerShuttingDown = false; // Flag for server shutdown
    private final Socket clientSocket;
    private final String clientUsername;
    private final BufferedReader clientInputReader;
    private final BufferedWriter clientOutputWriter;

    /**
     * Constructor for ChatClientHandler.
     * Initialises the client socket and sets up input/output streams.
     *
     * @param socket The socket connected to the client.
     * @throws IOException If an I/O error occurs while setting up streams.
     */
    public ChatClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.clientInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.clientOutputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.clientUsername = clientInputReader.readLine();
        if (this.clientUsername == null || this.clientUsername.trim().isEmpty()) {
            throw new IOException("Invalid username received");
        }
        clientHandlers.put(this.clientUsername, this);
        broadcastSystemMessage(clientUsername + " has joined the chat.");
    }

    /**
     * Main run method for the thread.
     * Continuously listens for client messages and broadcasts them to other clients.
     */
    @Override
    public void run() {
        String message;
        try {
            while ((message = clientInputReader.readLine()) != null && !clientSocket.isClosed()) {
                broadcastMessage(clientUsername + ": " + message);
            }
        } catch (SocketException e) {
            if (!isServerShuttingDown) {
                ConsoleDisplay.displayErrorMessage("Unexpected client disconnection: " + e.getMessage());
            }
        } catch (IOException e) {
            ConsoleDisplay.displayErrorMessage("Error in client communication: " + e.getMessage());
        } finally {
            closeResources();
            if (!isServerShuttingDown) {
                broadcastSystemMessage(clientUsername + " has left the chat.");
            }
            clientHandlers.remove(this.clientUsername);
        }
    }

    /**
     * Notifies all client handlers of the server shutdown.
     */
    public static void notifyServerShutdown() {
        isServerShuttingDown = true;
    }

    /**
     * Disconnects all connected clients.
     * Iterates through all active client handlers and starts their disconnection process.
     */
    public static void disconnectAllClients() {
        for (ChatClientHandler handler : clientHandlers.values()) {
            handler.disconnectClient();
        }
    }

    /**
     * Disconnects this client from the server.
     * Sends a disconnection message to the client and closes the socket.
     */
    private void disconnectClient() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientOutputWriter.write("Server is shutting down, disconnecting... press '\\q' " + "to exit");
                clientOutputWriter.newLine();
                clientOutputWriter.flush();
                clientSocket.close();
            }
        } catch (IOException e) {
            ConsoleDisplay.displayErrorMessage("Error closing client socket: " + e.getMessage());
        }
    }

    /**
     * Broadcasts a given message to all connected clients.
     * The message is sent to each client connected to the server.
     *
     * @param message The message to be broadcast.
     */
    private void broadcastMessage(String message) {
        for (ChatClientHandler clientHandler : clientHandlers.values()) {
            try {
                if (!clientHandler.clientSocket.isClosed()) {
                    String formattedMessage;
                    if (this.clientUsername.equals(clientHandler.clientUsername)) {
                        // If the sender is the same as the receiver, prefix with "You: "
                        formattedMessage = "You: " + message.substring(message.indexOf(":") + 2);
                    } else {
                        // Otherwise, send the message as is
                        formattedMessage = message;
                    }
                    clientHandler.clientOutputWriter.write(formattedMessage);
                    clientHandler.clientOutputWriter.newLine();
                    clientHandler.clientOutputWriter.flush();
                }
            } catch (IOException e) {
                ConsoleDisplay.displayErrorMessage("Error sending message: " + e.getMessage());
                clientHandler.closeResources();
            }
        }
    }


    /**
     * Broadcasts a system message to all connected clients.
     *
     * @param systemMessage The system message to be broadcast.
     */
    private void broadcastSystemMessage(String systemMessage) {
        if (!isServerShuttingDown) {
            for (ChatClientHandler clientHandler : clientHandlers.values()) {
                if (this != clientHandler) {  // Avoid sending the message to the user who just joined
                    clientHandler.sendMessage("[SYSTEM]: " + systemMessage);
                }
            }
        }
    }

    /**
     * Sends a message to the connected client.
     * Writes the message to the client's output stream and flushes it to ensure delivery.
     * If an IOException occurs during sending, it closes the resources associated with this client.
     *
     * @param message The message to be sent to the client.
     */
    private void sendMessage(String message) {
        try {
            if (!clientSocket.isClosed()) {
                clientOutputWriter.write(message);
                clientOutputWriter.newLine();
                clientOutputWriter.flush();
            }
        } catch (IOException e) {
            ConsoleDisplay.displayErrorMessage("Error sending message: " + e.getMessage());
            closeResources();
        }
    }


    /**
     * Closes resources associated with this client handler.
     * Ensures that the input reader, output writer, and client socket are closed properly.
     */
    private void closeResources() {
        try {
            if (clientInputReader != null) clientInputReader.close();
            if (clientOutputWriter != null) clientOutputWriter.close();
            if (clientSocket != null) clientSocket.close();
            clientHandlers.remove(this.clientUsername);
        } catch (IOException e) {
            ConsoleDisplay.displayErrorMessage("Error closing resources: " + e.getMessage());
        }
    }

}