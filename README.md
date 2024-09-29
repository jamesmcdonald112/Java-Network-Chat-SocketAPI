# Network Technologies Chat Application 
**Author:** James McDonald  
**Java Version Requirement:**  
- Minimum Required: Java 8  
- Recommended: Java 17 or higher

## Introduction 
This document outlines the design of a Java-based chat application, enabling text-based communication between clients via a server, developed for network technologies coursework.

## Final Module Grade: 90%

## Architecture 
The application follows a client-server model, utilising TCP socket programming for reliable data transmission and a multi-threaded server architecture to handle multiple simultaneous client connections. The application provides a user-friendly console-based interface with colour enhancement for improved readability and interaction.

## Design Overview and Features
- **Real-Time Messaging:** Enables immediate text-based communication between clients and the server.
- **Dynamic Server Configuration:** Server settings can be configured via a config.properties file.
- **User-Friendly Console Interface:** Intuitive and easy to navigate.
- **Robust Error Handling and Reconnection Strategy:** Comprehensive approach to error management, including reconnection attempts.
- **Graceful Session Termination:** Users can end their chat session using a specific command (“\q”).

## Key Design Choices
- **Multi-Threaded Server with Multi-User Support:** Efficiently manages multiple concurrent client connections.
- **Session Management and Broadcast Messaging:** Effectively manage client sessions and support broadcasting messages.
- **Structured Concurrency and Code Quality:** Utilises Java concurrency practices and maintains high code quality standards.
- **Error Logging and Monitoring:** Incorporates basic logging for error handling and system monitoring.

## How to Run
### Server Setup and Execution
- **Compilation:** `javac ie/atu/sw/*.java`
- **Execution:** `java ie.atu.sw.ChatServer` or `java ie.atu.sw.ChatServer [port_number]`

### Client Setup and Execution
- **Execution:** `java ie.atu.sw.ChatClient` or `java ie.atu.sw.ChatClient localhost [port_number]`

## Conclusion 
This chat application project was challenging yet rewarding, with key learnings in socket programming and managing concurrent client connections.

## Acknowledgements and References
- **Socket Programming and Client-Server Architecture:** Adapted concepts from Oracle's All About Sockets.
- **Multithreading for Handling Multiple Clients:** Based on principles from Oracle's Concurrency tutorial and Dr. John Healy's module lectures.
- **Configuration Management:** Guided by Baeldung's Guide to java.util.Properties.
- **Error Handling Strategies:** Insights from Java Code Geeks on Exception Handling in Java.
- **User Interface Elements (Progress Meter, Console Colors):** Code examples from Dr. John Healy.
